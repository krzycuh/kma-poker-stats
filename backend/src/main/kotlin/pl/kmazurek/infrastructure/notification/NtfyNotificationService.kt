package pl.kmazurek.infrastructure.notification

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import pl.kmazurek.application.service.NotificationService

@Service
@ConditionalOnProperty(name = ["ntfy.enabled"], havingValue = "true")
class NtfyNotificationService(
    @Value("\${ntfy.url}") private val ntfyUrl: String,
    @Value("\${ntfy.topic}") private val ntfyTopic: String,
    @Value("\${ntfy.token}") private val ntfyToken: String,
) : NotificationService {
    private val logger = LoggerFactory.getLogger(NtfyNotificationService::class.java)
    private val restTemplate = RestTemplate()

    @Async
    override fun notifyUserRegistered(
        userName: String,
        userEmail: String,
    ) {
        try {
            val url = "$ntfyUrl/$ntfyTopic"

            val headers = HttpHeaders()
            headers.contentType = MediaType.TEXT_PLAIN
            headers.set("Title", "Nowe konto w Poker Stats")
            headers.set("Tags", "wave,new")
            headers.set("Authorization", "Bearer ${ntfyToken}")

            val body = "Nowy użytkownik: $userName ($userEmail)"
            val request = HttpEntity(body, headers)

            restTemplate.postForEntity(url, request, String::class.java)
            logger.info("ntfy notification sent for new user registration: {}", userName)
        } catch (e: Exception) {
            logger.error("Failed to send ntfy notification for user registration: {}", userName, e)
        }
    }
}
