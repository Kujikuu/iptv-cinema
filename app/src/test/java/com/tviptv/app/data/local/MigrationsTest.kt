package com.tviptv.app.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

class MigrationsTest {

    @Test
    fun migration1To2_hasExpectedVersionRange() {
        assertEquals(1, MIGRATION_1_2.startVersion)
        assertEquals(2, MIGRATION_1_2.endVersion)
    }

    @Test
    fun migration4To5_hasExpectedVersionRange() {
        assertEquals(4, MIGRATION_4_5.startVersion)
        assertEquals(5, MIGRATION_4_5.endVersion)
    }

    @Test
    fun migration5To6_hasExpectedVersionRange() {
        assertEquals(5, MIGRATION_5_6.startVersion)
        assertEquals(6, MIGRATION_5_6.endVersion)
    }
}
