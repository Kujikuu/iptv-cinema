package com.afifistudio.iptvcinema.ui.common

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TvFocusCoordinatorTest {

    @Test
    fun isFirstGridRow_trueForFirstRow() {
        assertTrue(TvFocusCoordinator.isFirstGridRow(selectedPosition = 0, columns = 4))
        assertTrue(TvFocusCoordinator.isFirstGridRow(selectedPosition = 3, columns = 4))
    }

    @Test
    fun isFirstGridRow_falseForLaterRows() {
        assertFalse(TvFocusCoordinator.isFirstGridRow(selectedPosition = 4, columns = 4))
        assertFalse(TvFocusCoordinator.isFirstGridRow(selectedPosition = 7, columns = 3))
    }

    @Test
    fun isFirstGridRow_handlesSingleColumn() {
        assertTrue(TvFocusCoordinator.isFirstGridRow(selectedPosition = 0, columns = 1))
        assertFalse(TvFocusCoordinator.isFirstGridRow(selectedPosition = 1, columns = 1))
    }
}
