package pl.kmazurek.domain.model.gamesession

/**
 * Value Object representing the type of poker game
 */
enum class GameType {
    TEXAS_HOLDEM,
    OMAHA,
    OMAHA_HI_LO,
    SEVEN_CARD_STUD,
    FIVE_CARD_DRAW,
    MIXED_GAMES,
    OTHER,
    ;

    companion object {
        fun fromString(value: String): GameType {
            return values().find { it.name == value.uppercase() }
                ?: throw IllegalArgumentException("Unknown game type: $value")
        }
    }
}
