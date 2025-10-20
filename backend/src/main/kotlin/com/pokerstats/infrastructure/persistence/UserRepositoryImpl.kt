package com.pokerstats.infrastructure.persistence

import com.pokerstats.domain.model.user.Email
import com.pokerstats.domain.model.user.User
import com.pokerstats.domain.model.user.UserId
import com.pokerstats.domain.repository.UserRepository
import com.pokerstats.infrastructure.persistence.mapper.UserMapper
import org.springframework.stereotype.Component

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
}
