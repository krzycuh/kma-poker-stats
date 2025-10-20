package pl.kmazurek.domain.model.gamesession

/**
 * Value Object representing a game location
 */
@JvmInline
value class Location(val value: String) {
    init {
        require(value.isNotBlank()) { "Location cannot be blank" }
        require(value.length <= 255) { "Location cannot exceed 255 characters" }
    }

    override fun toString(): String = value
}
