package pl.kmazurek.application.service

interface NotificationService {
    fun notifyUserRegistered(
        userName: String,
        userEmail: String,
    )
}
