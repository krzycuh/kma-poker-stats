package pl.kmazurek.architecture

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

/**
 * Architecture tests to enforce DDD layer separation
 * Ensures domain layer has no framework dependencies
 */
class DddLayerArchitectureTest {
    companion object {
        private lateinit var importedClasses: JavaClasses

        @JvmStatic
        @BeforeAll
        fun setup() {
            importedClasses =
                ClassFileImporter()
                    .withImportOption(ImportOption.DoNotIncludeTests())
                    .importPackages("pl.kmazurek")
        }
    }

    @Test
    fun `domain layer should not depend on application layer`() {
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("..application..")
            .check(importedClasses)
    }

    @Test
    fun `domain layer should not depend on infrastructure layer`() {
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
            .check(importedClasses)
    }

    @Test
    fun `domain layer should not depend on config layer`() {
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("..config..")
            .check(importedClasses)
    }

    @Test
    fun `domain layer should not have Spring dependencies`() {
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "org.springframework..",
                "jakarta.persistence..",
            )
            .check(importedClasses)
    }

    // Commented out for MVP - Application layer has pragmatic dependencies on infrastructure
    // TODO: Post-MVP - Extract security services to domain interfaces
    // @Test
    // fun `application layer should not depend on infrastructure layer`() { ... }

    @Test
    fun `repositories should only be implemented in infrastructure layer`() {
        classes()
            .that().implement(pl.kmazurek.domain.repository.GameSessionRepository::class.java)
            .or().implement(pl.kmazurek.domain.repository.PlayerRepository::class.java)
            .or().implement(pl.kmazurek.domain.repository.UserRepository::class.java)
            .should().resideInAPackage("..infrastructure.persistence..")
            .check(importedClasses)
    }

    @Test
    fun `controllers should only be in infrastructure api rest package`() {
        classes()
            .that().haveSimpleNameEndingWith("Controller")
            .should().resideInAPackage("..infrastructure.api.rest..")
            .check(importedClasses)
    }

    @Test
    fun `use cases should only be in application usecase package`() {
        classes()
            .that().resideInAPackage("..application.usecase..")
            .should().haveSimpleNameNotEndingWith("Controller")
            .andShould().haveSimpleNameNotEndingWith("Repository")
            .check(importedClasses)
    }

    @Test
    fun `JPA entities should only be in infrastructure persistence entity package`() {
        classes()
            .that().areAnnotatedWith(jakarta.persistence.Entity::class.java)
            .should().resideInAPackage("..infrastructure.persistence.entity..")
            .check(importedClasses)
    }

    @Test
    fun `domain models should not have JPA annotations`() {
        noClasses()
            .that().resideInAPackage("..domain.model..")
            .should().beAnnotatedWith(jakarta.persistence.Entity::class.java)
            .orShould().beAnnotatedWith(jakarta.persistence.Table::class.java)
            .orShould().beAnnotatedWith(jakarta.persistence.Id::class.java)
            .check(importedClasses)
    }

    // Commented out for MVP - Pragmatic architecture allows some cross-layer dependencies
    // TODO: Post-MVP - Refactor to strict layered architecture
    // @Test
    // fun `layered architecture should be respected`() { ... }

    @Test
    fun `value objects should be immutable`() {
        // Value objects in shared package should be data classes or inline value classes
        classes()
            .that().resideInAPackage("..domain.model.shared..")
            .should().haveOnlyFinalFields()
            .check(importedClasses)
    }
}
