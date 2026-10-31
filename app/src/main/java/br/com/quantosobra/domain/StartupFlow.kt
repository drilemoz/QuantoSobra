package br.com.quantosobra.domain

enum class StartupDestination { WIZARD, NEWS, DASHBOARD }

/** Decisão pura, testável sem Android. Preserva dados existentes em upgrades. */
object StartupFlow {
    fun destination(wizardCompleted: Boolean, lastSeenRelease: String?, currentRelease: String): StartupDestination = when {
        !wizardCompleted -> StartupDestination.WIZARD
        lastSeenRelease != currentRelease -> StartupDestination.NEWS
        else -> StartupDestination.DASHBOARD
    }
}
