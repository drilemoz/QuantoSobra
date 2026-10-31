package br.com.quantosobra.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class StartupFlowTest {
    @Test fun freshInstallOpensWizard() {
        assertEquals(StartupDestination.WIZARD, StartupFlow.destination(false, null, "0.1.0-alpha.9"))
    }
    @Test fun clearedDataOpensWizardEvenWithSeenVersion() {
        assertEquals(StartupDestination.WIZARD, StartupFlow.destination(false, "0.1.0-alpha.9", "0.1.0-alpha.9"))
    }
    @Test fun upgradedVersionOpensNews() {
        assertEquals(StartupDestination.NEWS, StartupFlow.destination(true, "0.1.0-alpha.8", "0.1.0-alpha.9"))
    }
    @Test fun olderInstallWithoutVersionMarkerOpensNews() {
        assertEquals(StartupDestination.NEWS, StartupFlow.destination(true, null, "0.1.0-alpha.9"))
    }
    @Test fun seenReleaseOpensDashboard() {
        assertEquals(StartupDestination.DASHBOARD, StartupFlow.destination(true, "0.1.0-alpha.9", "0.1.0-alpha.9"))
    }
}
