package pl.kmazurek.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.kmazurek.domain.service.StatsCalculator

/**
 * Configuration for domain services
 * Domain services are stateless and can be Spring beans
 */
@Configuration
class ServiceConfig {
    @Bean
    fun statsCalculator(): StatsCalculator = StatsCalculator()
}
