package pl.kmazurek.infrastructure.api.rest.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pl.kmazurek.application.dto.PagedResponse
import pl.kmazurek.application.dto.UserSummaryDto
import pl.kmazurek.application.usecase.user.CountUnlinkedUsers
import pl.kmazurek.application.usecase.user.ListUnlinkedUsers
import pl.kmazurek.application.usecase.user.ListUnlinkedUsersQuery

/**
 * Admin-only endpoints for managing user accounts.
 */
@RestController
@RequestMapping("/api/admin/users")
class AdminUserController(
    private val listUnlinkedUsers: ListUnlinkedUsers,
    private val countUnlinkedUsers: CountUnlinkedUsers,
) {
    @GetMapping("/unlinked")
    @PreAuthorize("hasRole('ADMIN')")
    fun getUnlinkedUsers(
        @RequestParam(required = false) searchTerm: String?,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") pageSize: Int,
    ): ResponseEntity<PagedResponse<UserSummaryDto>> {
        val query = ListUnlinkedUsersQuery(searchTerm = searchTerm, page = page, pageSize = pageSize)
        val result = listUnlinkedUsers.execute(query)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/unlinked/count")
    @PreAuthorize("hasRole('ADMIN')")
    fun getUnlinkedUsersCount(): ResponseEntity<Map<String, Long>> {
        val count = countUnlinkedUsers.execute()
        return ResponseEntity.ok(mapOf("count" to count))
    }
}
