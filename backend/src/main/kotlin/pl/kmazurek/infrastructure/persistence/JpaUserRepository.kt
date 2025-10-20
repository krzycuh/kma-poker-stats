package pl.kmazurek.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
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
}
