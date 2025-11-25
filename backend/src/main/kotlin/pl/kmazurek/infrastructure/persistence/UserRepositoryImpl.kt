package pl.kmazurek.infrastructure.persistence

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import pl.kmazurek.domain.model.user.Email
import pl.kmazurek.domain.model.user.User
import pl.kmazurek.domain.model.user.UserId
import pl.kmazurek.domain.repository.UserRepository
import pl.kmazurek.infrastructure.persistence.mapper.UserMapper

/**
 * Implementation of UserRepository (domain interface)
 * Adapter that connects domain to JPA infrastructure
 */
@Component
class UserRepositoryImpl(
    private val jpaRepository: JpaUserRepository,
) : UserRepository {
    override fun findById(id: UserId): User? {
        return jpaRepository.findById(id.value)
            .map { UserMapper.toDomain(it) }
            .orElse(null)
    }

    override fun findByEmail(email: Email): User? {
        return jpaRepository.findByEmail(email.value)
            ?.let { UserMapper.toDomain(it) }
    }

    override fun existsByEmail(email: Email): Boolean {
        return jpaRepository.existsByEmail(email.value)
    }

    override fun save(user: User): User {
        val jpaEntity = UserMapper.toJpa(user)
        val saved = jpaRepository.save(jpaEntity)
        return UserMapper.toDomain(saved)
    }

    override fun deleteById(id: UserId) {
        jpaRepository.deleteById(id.value)
    }

    override fun findUnlinkedUsers(
        searchTerm: String?,
        page: Int,
        pageSize: Int,
    ): Pair<List<User>, Long> {
        val normalizedSearch = searchTerm?.trim()?.takeIf { it.isNotEmpty() }
        val searchPattern = normalizedSearch?.lowercase()?.let { "%$it%" }
        val safePage = page.coerceAtLeast(0)
        val safePageSize = pageSize.coerceAtLeast(1)
        val pageable = PageRequest.of(safePage, safePageSize)

        val resultPage = jpaRepository.findUnlinkedUsers(searchPattern, pageable)
        val users = resultPage.content.map { UserMapper.toDomain(it) }
        return users to resultPage.totalElements
    }

    override fun countUnlinkedUsers(): Long {
        return jpaRepository.countUnlinkedUsers()
    }
}
