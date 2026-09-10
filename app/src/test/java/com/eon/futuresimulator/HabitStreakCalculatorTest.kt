package com.eon.futuresimulator

import com.eon.futuresimulator.core.util.DataSource
import com.eon.futuresimulator.data.database.entity.HabitCheckInEntity
import com.eon.futuresimulator.domain.model.CheckInStatus
import com.eon.futuresimulator.habits.HabitStreakCalculator
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HabitStreakCalculatorTest {

    private val calculator = HabitStreakCalculator()

    private fun checkIn(day: Int, status: CheckInStatus) = HabitCheckInEntity(
        id = "c$day", habitId = "h1", dateEpochMillis = day * 86_400_000L, status = status, note = "",
        createdAt = 0, updatedAt = 0, source = DataSource.MANUAL, version = 1,
    )

    @Test
    fun `empty history yields zero streaks`() {
        val info = calculator.calculate(emptyList())
        assertThat(info.currentStreak).isEqualTo(0)
        assertThat(info.longestStreak).isEqualTo(0)
        assertThat(info.completionRate).isEqualTo(0.0)
    }

    @Test
    fun `consecutive completions build current and longest streak`() {
        val checkIns = (1..5).map { checkIn(it, CheckInStatus.COMPLETED) }
        val info = calculator.calculate(checkIns)
        assertThat(info.currentStreak).isEqualTo(5)
        assertThat(info.longestStreak).isEqualTo(5)
        assertThat(info.completionRate).isEqualTo(1.0)
    }

    @Test
    fun `a missed day resets current streak but not longest`() {
        val checkIns = (1..3).map { checkIn(it, CheckInStatus.COMPLETED) } +
            checkIn(4, CheckInStatus.MISSED) +
            checkIn(5, CheckInStatus.COMPLETED)
        val info = calculator.calculate(checkIns)
        assertThat(info.currentStreak).isEqualTo(1)
        assertThat(info.longestStreak).isEqualTo(3)
    }

    @Test
    fun `skipped day within recovery grace does not reset streak`() {
        val checkIns = (1..3).map { checkIn(it, CheckInStatus.COMPLETED) } +
            checkIn(4, CheckInStatus.SKIPPED) +
            checkIn(5, CheckInStatus.COMPLETED)
        val info = calculator.calculate(checkIns, recoveryGraceDays = 1)
        assertThat(info.currentStreak).isEqualTo(5)
    }
}
