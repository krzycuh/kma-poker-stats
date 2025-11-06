package pl.kmazurek.infrastructure.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import pl.kmazurek.domain.model.user.Email
import pl.kmazurek.domain.model.user.UserId
import pl.kmazurek.domain.model.user.UserRole
import java.time.Duration
import java.time.Instant
import java.util.Date
import javax.crypto.SecretKey

/**
 * Service for JWT token generation and validation
 */
@Service
class JwtService(
    @Value("\${jwt.secret:changeme-this-is-a-development-secret-key-only-must-be-at-least-256-bits-long}")
    private val secret: String,
    @Value("\${jwt.expiration:0}") private val expirationMs: Long,
    @Value("\${jwt.expiration-hours:-1}") private val expirationHours: Long,
    @Value("\${jwt.refresh-expiration:0}") private val refreshExpirationMs: Long,
    @Value("\${jwt.refresh-expiration-days:-1}") private val refreshExpirationDays: Long,
) {
    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    private val accessTokenDuration: Duration by lazy {
        when {
            expirationMs > 0 -> Duration.ofMillis(expirationMs)
            expirationHours > 0 -> Duration.ofHours(expirationHours)
            else -> Duration.ofHours(DEFAULT_ACCESS_TOKEN_EXPIRATION_HOURS)
        }
    }

    private val refreshTokenDuration: Duration by lazy {
        when {
            refreshExpirationMs > 0 -> Duration.ofMillis(refreshExpirationMs)
            refreshExpirationDays > 0 -> Duration.ofDays(refreshExpirationDays)
            else -> Duration.ofDays(DEFAULT_REFRESH_TOKEN_EXPIRATION_DAYS)
        }
    }

    /**
     * Generate access token
     */
    fun generateAccessToken(
        userId: UserId,
        email: Email,
        role: UserRole,
    ): String {
        val now = Instant.now()
        val expiration = now.plus(accessTokenDuration)

        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email.value)
            .claim("role", role.name)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(secretKey)
            .compact()
    }

    /**
     * Generate refresh token
     */
    fun generateRefreshToken(userId: UserId): String {
        val now = Instant.now()
        val expiration = now.plus(refreshTokenDuration)

        return Jwts.builder()
            .subject(userId.toString())
            .claim("type", "refresh")
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(secretKey)
            .compact()
    }

    /**
     * Extract user ID from token
     */
    fun extractUserId(token: String): UserId {
        val claims = extractAllClaims(token)
        return UserId.fromString(claims.subject)
    }

    /**
     * Extract email from token
     */
    fun extractEmail(token: String): Email {
        val claims = extractAllClaims(token)
        return Email(claims["email"] as String)
    }

    /**
     * Extract role from token
     */
    fun extractRole(token: String): UserRole {
        val claims = extractAllClaims(token)
        return UserRole.valueOf(claims["role"] as String)
    }

    /**
     * Validate token
     */
    fun isTokenValid(token: String): Boolean {
        return try {
            val claims = extractAllClaims(token)
            !isTokenExpired(claims)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if token is a refresh token
     */
    fun isRefreshToken(token: String): Boolean {
        return try {
            val claims = extractAllClaims(token)
            claims["type"] == "refresh"
        } catch (e: Exception) {
            false
        }
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    private fun isTokenExpired(claims: Claims): Boolean {
        return claims.expiration.before(Date())
    }
    companion object {
        private const val DEFAULT_ACCESS_TOKEN_EXPIRATION_HOURS = 24L
        private const val DEFAULT_REFRESH_TOKEN_EXPIRATION_DAYS = 7L
    }
}
