package com.udacity.project4.locationreminders.data.local

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest : AutoCloseKoinTest() {

    // Add testing implementation to the RemindersDao.kt

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var appContext: Application

    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()
        val myModule = module {
            single {
                Room.inMemoryDatabaseBuilder(
                    appContext,
                    RemindersDatabase::class.java
                ).allowMainThreadQueries().build()
            }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        database = get()


    }


    // Test saved reminders
    @Test
    fun saveReminderAndGetById() = runBlockingTest {

        // Given Save a Reminder
        val newReminder = ReminderDTO(
            "Title1",
            "Description1",
            "Location1",
            null,
            null
        )
        database.reminderDao().saveReminder(newReminder)

        // WHEN - Get the Reminder by id from the database.
        val reminderSaved = database.reminderDao().getReminderById(newReminder.id)

        // THEN - The loaded data contains the expected values.
        assertThat<ReminderDTO>(reminderSaved as ReminderDTO, notNullValue())
        assertThat(reminderSaved.id, `is`(newReminder.id))
        assertThat(reminderSaved.title, `is`(newReminder.title))
        assertThat(reminderSaved.description, `is`(newReminder.description))
        assertThat(reminderSaved.location, `is`(newReminder.location))
    }

    // Test update reminders
    @Test
    fun updateReminderAndGetById() = runBlockingTest {

        // Given Save a Reminder
        val newReminder = ReminderDTO(
            "Title1",
            "Description1",
            "Location1",
            null,
            null
        )
        database.reminderDao().saveReminder(newReminder)

        // WHEN - Update Reminder and Get the Reminder by id from the database.
        val updatedReminder = ReminderDTO(
            "Title1Updated",
            "Description1Updated",
            "Location1Updated",
            null,
            null,
            newReminder.id
        )
        database.reminderDao().saveReminder(updatedReminder)
        val reminderSaved = database.reminderDao().getReminderById(newReminder.id)

        // THEN - The loaded data contains the expected values.
        assertThat<ReminderDTO>(reminderSaved as ReminderDTO, notNullValue())
        assertThat(reminderSaved.id, `is`(updatedReminder.id))
        assertThat(reminderSaved.title, `is`(updatedReminder.title))
        assertThat(reminderSaved.description, `is`(updatedReminder.description))
        assertThat(reminderSaved.location, `is`(updatedReminder.location))
    }

    // Test database reminders
    @Test
    fun getRemindersAndCheckData() = runBlockingTest {

        // Given Reminders
        val newReminder1 = ReminderDTO(
            "Title1",
            "Description1",
            "Location1",
            null,
            null
        )
        val newReminder2 = ReminderDTO(
            "Title2",
            "Description2",
            "Location2",
            null,
            null
        )
        database.reminderDao().saveReminder(newReminder1)
        database.reminderDao().saveReminder(newReminder2)

        // WHEN - Get all Reminders from the database.
        val reminders = database.reminderDao().getReminders()

        // THEN - The loaded data contains the expected values.
        assertThat(reminders.size, `is`(2))

    }

    // Test delete all database reminders
    @Test
    fun deleteRemindersAndCheckData() = runBlockingTest {

        // Given Reminders
        val newReminder1 = ReminderDTO(
            "Title1",
            "Description1",
            "Location1",
            null,
            null
        )
        val newReminder2 = ReminderDTO(
            "Title2",
            "Description2",
            "Location2",
            null,
            null
        )
        database.reminderDao().saveReminder(newReminder1)
        database.reminderDao().saveReminder(newReminder2)

        // WHEN - Get delete all Reminders from the database.
        database.reminderDao().deleteAllReminders()
        val reminders = database.reminderDao().getReminders()

        // THEN - The loaded data contains the expected values.
        assertThat(reminders.size, `is`(0))

    }


        // Close database after testing
        @After
        fun closeDb() = database.close()
    }




