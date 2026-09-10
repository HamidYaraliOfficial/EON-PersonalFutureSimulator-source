package com.eon.futuresimulator

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.eon.futuresimulator.core.util.DataSource
import com.eon.futuresimulator.data.database.EonDatabase
import com.eon.futuresimulator.data.database.entity.GoalEntity
import com.eon.futuresimulator.domain.model.*
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GoalDaoTest {
    private lateinit var db: EonDatabase

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, EonDatabase::class.java).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() { db.close() }

    private fun goal(id: String, title: String) = GoalEntity(
        id = id, title = title, description = "", category = GoalCategory.LEARNING, priority = Priority.MEDIUM,
        horizon = GoalHorizon.MEDIUM_TERM, deadlineEpochMillis = null, targetValue = 100.0, currentValue = 0.0,
        unit = "%", status = GoalStatus.ACTIVE, createdAt = 0, updatedAt = 0, source = DataSource.MANUAL, version = 1,
    )

    @Test
    fun insertAndReadGoal() = runBlocking {
        db.goalDao().upsert(goal("g1", "Learn Kotlin"))
        val loaded = db.goalDao().getById("g1")
        assertThat(loaded?.title).isEqualTo("Learn Kotlin")
    }

    @Test
    fun deletingGoalCascadesToMilestones() = runBlocking {
        db.goalDao().upsert(goal("g2", "Ship MVP"))
        db.goalDao().upsertMilestone(
            com.eon.futuresimulator.data.database.entity.GoalMilestoneEntity(
                id = "m1", goalId = "g2", title = "Milestone 1", targetValue = 50.0,
                targetDateEpochMillis = 0, achievedDateEpochMillis = null, isAchieved = false, orderIndex = 1,
                createdAt = 0, updatedAt = 0, source = DataSource.DERIVED, version = 1,
            ),
        )
        db.goalDao().deleteById("g2")
        val remainingMilestones = db.goalDao().getMilestonesOnce("g2")
        assertThat(remainingMilestones).isEmpty()
    }

    @Test
    fun emptyDatabaseReturnsEmptyList() = runBlocking {
        assertThat(db.goalDao().getAllOnce()).isEmpty()
    }
}
