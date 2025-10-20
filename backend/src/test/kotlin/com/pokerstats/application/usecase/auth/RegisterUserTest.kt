package com.pokerstats.application.usecase.auth

import com.pokerstats.domain.model.user.Email
import com.pokerstats.domain.model.user.HashedPassword
import com.pokerstats.domain.repository.UserRepository
import com.pokerstats.infrastructure.security.PasswordEncoderService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Unit tests for RegisterUser use case
 */
class RegisterUserTest {
    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: PasswordEncoderService
    private lateinit var registerUser: RegisterUser

    @BeforeEach
    fun setup() {
        userRepository = mockk()
        passwordEncoder = mockk()
        registerUser = RegisterUser(userRepository, passwordEncoder)
    }

    @Test
    fun `should register new user successfully`() {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val name = "Test User"
        val hashedPassword = HashedPassword("hashed_password")

        every { userRepository.existsByEmail(Email(email)) } returns false
        every { passwordEncoder.hash(password) } returns hashedPassword
        every { userRepository.save(any()) } answers { firstArg() }

        // When
        val user = registerUser.execute(email, password, name)

        // Then
        assertEquals(email, user.email.value)
        assertEquals(name, user.name)
        verify { userRepository.save(any()) }
    }

    @Test
    fun `should throw exception when email already exists`() {
        // Given
        val email = "existing@example.com"
        every { userRepository.existsByEmail(Email(email)) } returns true

        // When/Then
        assertThrows<EmailAlreadyExistsException> {
            registerUser.execute(email, "password123", "Test User")
        }
    }
}
