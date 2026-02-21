package pl.kmazurek.infrastructure.logging

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class AuditLogger {
    private val logger = LoggerFactory.getLogger("audit")

    fun log(event: String, userId: String?, details: Map<String, Any?> = emptyMap()) {
        val detailsStr = if (details.isNotEmpty()) {
            details.entries.joinToString(", ") { "${it.key}=${it.value}" }
        } else {
            ""
        }

        if (detailsStr.isNotEmpty()) {
            logger.info("AUDIT | event={} | userId={} | {}", event, userId ?: "anonymous", detailsStr)
        } else {
            logger.info("AUDIT | event={} | userId={}", event, userId ?: "anonymous")
        }
    }
}
