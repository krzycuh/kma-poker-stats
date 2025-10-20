package com.pokerstats.infrastructure.persistence.mapper

import com.pokerstats.domain.model.user.Email
import com.pokerstats.domain.model.user.HashedPassword
import com.pokerstats.domain.model.user.User
import com.pokerstats.domain.model.user.UserId
import com.pokerstats.domain.model.user.UserRole
import com.pokerstats.infrastructure.persistence.entity.UserJpaEntity
import com.pokerstats.infrastructure.persistence.entity.UserRoleJpa

/**
 * Mapper between Domain User and JPA UserEntity
 * Anti-corruption layer - keeps domain and infrastructure separated
 */
object UserMapper {
    fun toDomain(jpa: UserJpaEntity): User {
        return User(
            id = UserId(jpa.id),
            email = Email(jpa.email),
            passwordHash = HashedPassword(jpa.passwordHash),
            name = jpa.name,
            role = toDomainRole(jpa.role),
            avatarUrl = jpa.avatarUrl,
            createdAt = jpa.createdAt,
            updatedAt = jpa.updatedAt,
        )
    }

    fun toJpa(domain: User): UserJpaEntity {
        return UserJpaEntity(
            id = domain.id.value,
            email = domain.email.value,
            passwordHash = domain.passwordHash.value,
            name = domain.name,
            role = toJpaRole(domain.role),
            avatarUrl = domain.avatarUrl,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
        )
    }

    private fun toDomainRole(jpaRole: UserRoleJpa): UserRole {
        return when (jpaRole) {
            UserRoleJpa.CASUAL_PLAYER -> UserRole.CASUAL_PLAYER
            UserRoleJpa.ADMIN -> UserRole.ADMIN
        }
    }

    private fun toJpaRole(domainRole: UserRole): UserRoleJpa {
        return when (domainRole) {
            UserRole.CASUAL_PLAYER -> UserRoleJpa.CASUAL_PLAYER
            UserRole.ADMIN -> UserRoleJpa.ADMIN
        }
    }
}
