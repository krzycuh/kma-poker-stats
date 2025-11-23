package pl.kmazurek.infrastructure.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import pl.kmazurek.infrastructure.persistence.entity.UserJpaEntity
import java.util.UUID

/**
 * Spring Data JPA repository interface
 */
@Repository
interface JpaUserRepository : JpaRepository<UserJpaEntity, UUID> {
    fun findByEmail(email: String): UserJpaEntity?

    fun existsByEmail(email: String): Boolean

    @Query(
        """
        SELECT u FROM UserJpaEntity u
        WHERE NOT EXISTS (
            SELECT 1 FROM PlayerJpaEntity p
            WHERE p.userId = u.id
        )
        AND (
            :searchTerm IS NULL
            OR LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        )
        ORDER BY u.createdAt DESC
        """,
    )
    fun findUnlinkedUsers(
        @Param("searchTerm") searchTerm: String?,
        pageable: Pageable,
    ): Page<UserJpaEntity>

    @Query(
        """
        SELECT COUNT(u) FROM UserJpaEntity u
        WHERE NOT EXISTS (
            SELECT 1 FROM PlayerJpaEntity p
            WHERE p.userId = u.id
        )
        """,
    )
    fun countUnlinkedUsers(): Long
}
