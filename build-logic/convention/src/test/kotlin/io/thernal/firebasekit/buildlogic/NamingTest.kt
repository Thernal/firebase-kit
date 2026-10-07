package io.thernal.firebasekit.buildlogic

import org.junit.Assert.assertEquals
import org.junit.Test

class NamingTest {
    @Test
    fun `namespace follows the module path splitting dashes`() {
        assertEquals("com.example.features.venue.management.impl", namespaceFor("com.example", ":features:venue-management:impl"))
        assertEquals("com.example", namespaceFor("com.example", ":"))
    }

    @Test
    fun `namespace leaves out the modules root`() {
        assertEquals("com.example.core.ui", namespaceFor("com.example", ":code:shared:core:ui", listOf("code", "shared")))
        assertEquals("com.example.tools.lint", namespaceFor("com.example", ":tools:lint", listOf("fixture")))
        assertEquals(listOf("code", "shared"), parseModulesRoot(" code/shared "))
        assertEquals(emptyList<String>(), parseModulesRoot(null))
    }
}
