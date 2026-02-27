package pl.kmazurek.infrastructure.notification

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import pl.kmazurek.application.service.NotificationService

@Service
@ConditionalOnProperty(name = ["ntfy.enabled"], havingValue = "false", matchIfMissing = true)
class NoOpNotificationService : NotificationService {
    private val logger = LoggerFactory.getLogger(NoOpNotificationService::class.java)

    override fun notifyUserRegistered(
        userName: String,
        userEmail: String,
    ) {
        logger.debug("Notifications disabled - skipping user registration notification for: {}", userName)
    }
}
