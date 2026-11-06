package br.com.quantosobra.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseCatalogTest {
    @Test fun fourthFeatureHasIconAndDoesNotCrash() {
        // Regressão: alpha.16 tinha 4 novidades e apenas 3 ícones.
        val previous = ReleaseCatalog.recent.first { it.version == "0.1.0-alpha.16" }
        assertEquals(4, previous.features.size)
        assertEquals("▣", ReleaseCatalog.featureIcon(3))
    }

    @Test fun additionalFeaturesUseSafeFallback() {
        assertEquals("✦", ReleaseCatalog.featureIcon(0))
        assertEquals("✓", ReleaseCatalog.featureIcon(4))
        assertEquals("✓", ReleaseCatalog.featureIcon(100))
        assertEquals("✓", ReleaseCatalog.featureIcon(-1))
    }

    @Test fun everyCurrentFeatureCanBeRendered() {
        val current = ReleaseCatalog.recent.first()
        assertEquals(ReleaseCatalog.currentVersion, current.version)
        current.features.indices.forEach { i ->
            assertTrue(ReleaseCatalog.featureIcon(i).isNotBlank())
        }
    }
}
