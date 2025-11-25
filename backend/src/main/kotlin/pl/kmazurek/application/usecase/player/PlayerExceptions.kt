package pl.kmazurek.application.usecase.player

class PlayerAlreadyExistsException(message: String) : RuntimeException(message)

class LinkedUserNotFoundException(message: String) : RuntimeException(message)

class UserAlreadyLinkedException(message: String) : RuntimeException(message)

class PlayerAlreadyLinkedException(message: String) : RuntimeException(message)

class PlayerInactiveException(message: String) : RuntimeException(message)

class PlayerNotLinkedException(message: String) : RuntimeException(message)

class PlayerNotFoundException(message: String) : RuntimeException(message)

class PlayerAccessDeniedException(message: String) : RuntimeException(message)
