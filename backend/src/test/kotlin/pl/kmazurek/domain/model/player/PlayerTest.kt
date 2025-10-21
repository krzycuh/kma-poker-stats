package pl.kmazurek.domain.model.player

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import pl.kmazurek.domain.model.user.UserId

class PlayerTest {
    @Test
    fun `should create a new player with valid data`() {
        // Given
        val name = PlayerName("John Doe")
        val avatarUrl = "https://example.com/avatar.jpg"
        val userId = UserId.generate()

        // When
        val player = Player.create(
            name = name,
            avatarUrl = avatarUrl,
            userId = userId,
        )

        // Then
        player.id shouldNotBe null
        player.name shouldBe name
        player.avatarUrl shouldBe avatarUrl
        player.userId shouldBe userId
        player.isActive shouldBe true
        player.isLinkedToUser() shouldBe true
    }

    @Test
    fun `should create player without user link`() {
        // Given
        val name = PlayerName("Jane Doe")

        // When
        val player = Player.create(name = name)

        // Then
        player.userId shouldBe null
        player.isLinkedToUser() shouldBe false
        player.isActive shouldBe true
    }

    @Test
    fun `should update player information`() {
        // Given
        val player = Player.create(
            name = PlayerName("Old Name"),
            avatarUrl = "https://example.com/old.jpg",
        )
        val newName = PlayerName("New Name")
        val newAvatarUrl = "https://example.com/new.jpg"

        // When
        val updated = player.update(
            name = newName,
            avatarUrl = newAvatarUrl,
        )

        // Then
        updated.name shouldBe newName
        updated.avatarUrl shouldBe newAvatarUrl
    }

    @Test
    fun `should link player to user`() {
        // Given
        val player = Player.create(name = PlayerName("John Doe"))
        val userId = UserId.generate()

        // When
        val linked = player.linkToUser(userId)

        // Then
        linked.userId shouldBe userId
        linked.isLinkedToUser() shouldBe true
    }

    @Test
    fun `should reject linking already linked player`() {
        // Given
        val userId1 = UserId.generate()
        val userId2 = UserId.generate()
        val player = Player.create(
            name = PlayerName("John Doe"),
            userId = userId1,
        )

        // When/Then
        shouldThrow<IllegalArgumentException> {
            player.linkToUser(userId2)
        }
    }

    @Test
    fun `should unlink player from user`() {
        // Given
        val userId = UserId.generate()
        val player = Player.create(
            name = PlayerName("John Doe"),
            userId = userId,
        )

        // When
        val unlinked = player.unlinkFromUser()

        // Then
        unlinked.userId shouldBe null
        unlinked.isLinkedToUser() shouldBe false
    }

    @Test
    fun `should reject unlinking non-linked player`() {
        // Given
        val player = Player.create(name = PlayerName("John Doe"))

        // When/Then
        shouldThrow<IllegalArgumentException> {
            player.unlinkFromUser()
        }
    }

    @Test
    fun `should deactivate player`() {
        // Given
        val player = Player.create(name = PlayerName("John Doe"))

        // When
        val deactivated = player.deactivate()

        // Then
        deactivated.isActive shouldBe false
    }

    @Test
    fun `should reject deactivating already deactivated player`() {
        // Given
        val player = Player.create(name = PlayerName("John Doe")).deactivate()

        // When/Then
        shouldThrow<IllegalArgumentException> {
            player.deactivate()
        }
    }

    @Test
    fun `should reactivate player`() {
        // Given
        val player = Player.create(name = PlayerName("John Doe")).deactivate()

        // When
        val reactivated = player.reactivate()

        // Then
        reactivated.isActive shouldBe true
    }

    @Test
    fun `should reject reactivating already active player`() {
        // Given
        val player = Player.create(name = PlayerName("John Doe"))

        // When/Then
        shouldThrow<IllegalArgumentException> {
            player.reactivate()
        }
    }
}
