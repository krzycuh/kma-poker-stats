package pl.kmazurek.application.usecase.user

import org.springframework.stereotype.Service
import pl.kmazurek.application.dto.PagedResponse
import pl.kmazurek.application.dto.UserSummaryDto
import pl.kmazurek.domain.repository.UserRepository

data class ListUnlinkedUsersQuery(
    val searchTerm: String? = null,
    val page: Int = 0,
    val pageSize: Int = 10,
)

@Service
class ListUnlinkedUsers(
    private val userRepository: UserRepository,
) {
    fun execute(query: ListUnlinkedUsersQuery): PagedResponse<UserSummaryDto> {
        val (users, total) = userRepository.findUnlinkedUsers(query.searchTerm, query.page, query.pageSize)
        val dtos = users.map { UserSummaryDto.fromDomain(it) }
        return PagedResponse.of(dtos, query.page, query.pageSize, total)
    }
}

@Service
class CountUnlinkedUsers(
    private val userRepository: UserRepository,
) {
    fun execute(): Long = userRepository.countUnlinkedUsers()
}
