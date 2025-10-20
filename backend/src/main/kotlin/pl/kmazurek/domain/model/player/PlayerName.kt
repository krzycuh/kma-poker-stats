package pl.kmazurek.domain.model.player

/**
 * Value Object representing a player's name
 * Enforces validation rules for player names
 */
@JvmInline
value class PlayerName(val value: String) {
    init {
        require(value.isNotBlank()) { "Player name cannot be blank" }
        require(value.length <= 255) { "Player name cannot exceed 255 characters" }
        require(value.trim() == value) { "Player name cannot have leading or trailing whitespace" }
    }

    override fun toString(): String = value
}
