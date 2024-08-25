package dev.rdcl

import kotlin.test.Test
import kotlin.test.assertNotNull

class AppTest {
    @Test fun `App has a greeting`() {
        val classUnderTest = App()
        assertNotNull(classUnderTest.greeting, "app should have a greeting")
    }
}
