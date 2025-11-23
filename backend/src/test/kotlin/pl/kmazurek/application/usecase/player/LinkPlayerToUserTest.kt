package pl.kmazurek.application.usecase.player

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pl.kmazurek.domain.model.player.Player
import pl.kmazurek.domain.model.player.PlayerId
import pl.kmazurek.domain.model.player.PlayerName
import pl.kmazurek.domain.model.user.Email
import pl.kmazurek.domain.model.user.HashedPassword
import pl.kmazurek.domain.model.user.User
import pl.kmazurek.domain.model.user.UserId
import pl.kmazurek.domain.repository.PlayerRepository
import pl.kmazurek.domain.repository.UserRepository

class LinkPlayerToUserTest {
    private val playerRepository: PlayerRepository = mockk()
    private val userRepository: UserRepository = mockk()
    private val linkPlayerToUser = LinkPlayerToUser(playerRepository, userRepository)
    private val unlinkPlayerFromUser = UnlinkPlayerFromUser(playerRepository)

    @Test
    fun `should link player to user`() {
        val player = newPlayer()
        val user = newUser()

        every { playerRepository.findById(player.id) } returns player
        every { playerRepository.findByUserId(user.id) } returns null
        every { playerRepository.save(any()) } answers { firstArg() }
        every { userRepository.findById(user.id) } returns user

        val result = linkPlayerToUser.execute(player.id, user.id)

        assertEquals(user.id, result.userId)
        verify {
            playerRepository.save(any())
        }
    }

    @Test
    fun `should throw when player already linked`() {
        val linkedPlayer = newPlayer().linkToUser(UserId.generate())
        val user = newUser()

        every { playerRepository.findById(linkedPlayer.id) } returns linkedPlayer

        assertThrows<PlayerAlreadyLinkedException> {
            linkPlayerToUser.execute(linkedPlayer.id, user.id)
        }
    }

    @Test
    fun `should throw when user already linked elsewhere`() {
        val player = newPlayer()
        val user = newUser()
        val otherPlayer =
            Player.create(PlayerName("Other"))
                .linkToUser(user.id)

        every { playerRepository.findById(player.id) } returns player
        every { userRepository.findById(user.id) } returns user
        every { playerRepository.findByUserId(user.id) } returns otherPlayer

        assertThrows<UserAlreadyLinkedException> {
            linkPlayerToUser.execute(player.id, user.id)
        }
    }

    @Test
    fun `should unlink player`() {
        val user = newUser()
        val linkedPlayer = newPlayer().linkToUser(user.id)

        every { playerRepository.findById(linkedPlayer.id) } returns linkedPlayer
        every { playerRepository.save(any()) } answers { firstArg() }

        val result = unlinkPlayerFromUser.execute(linkedPlayer.id)

        assertEquals(null, result.userId)
        verify { playerRepository.save(any()) }
    }

    @Test
    fun `should throw when unlinking player without link`() {
        val player = newPlayer()

        every { playerRepository.findById(player.id) } returns player

        assertThrows<PlayerNotLinkedException> {
            unlinkPlayerFromUser.execute(player.id)
        }
    }

    private fun newPlayer(): Player =
        Player.create(
            name = PlayerName("Player-${System.nanoTime()}"),
        ).copy(
            id = PlayerId.generate(),
        )

    private fun newUser(): User =
        User.create(
            email = Email("user${System.nanoTime()}@example.com"),
            passwordHash = HashedPassword("hashed"),
            name = "User",
        ).copy(
            id = UserId.generate(),
        )
}
