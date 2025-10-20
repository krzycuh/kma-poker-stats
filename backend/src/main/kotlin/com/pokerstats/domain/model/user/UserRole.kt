package com.pokerstats.domain.model.user

/**
 * Value Object representing user roles in the system
 */
enum class UserRole {
    CASUAL_PLAYER,
    ADMIN,
    ;

    fun hasAdminPrivileges() = this == ADMIN
}
