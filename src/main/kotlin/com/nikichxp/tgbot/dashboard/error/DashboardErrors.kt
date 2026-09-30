package com.nikichxp.tgbot.dashboard.error

class DashboardUnauthorizedException(message: String = "Not authenticated") : RuntimeException(message)
class DashboardForbiddenException(message: String = "Access denied") : RuntimeException(message)
class DashboardConflictException(message: String) : RuntimeException(message)
class DashboardNotFoundException(message: String) : RuntimeException(message)
