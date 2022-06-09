package com.udacity.project4.locationreminders.data.local

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest : AutoCloseKoinTest() {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var repository: RemindersLocalRepository
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
            single { RemindersLocalRepository(get()) }
            single { (get() as RemindersDatabase).reminderDao() }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        database = get()
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }


    // Test saved reminders
    @Test
    fun saveReminderAndGetById() = runBlocking {

        // Given Save a Reminder
        val newReminder = ReminderDTO(
            "Title1",
            "Description1",
            "Location1",
            null,
            null
        )
        repository.saveReminder(newReminder)

        // WHEN - Get the Reminder by id from the repository.
        val result = repository.getReminder(newReminder.id)

        // THEN - The loaded data contains the expected values.
        assertThat(result is Result.Success, `is`(true))
        result as Result.Success
        assertThat(result.data.title, `is`("Title1"))
        assertThat(result.data.description, `is`("Description1"))
        assertThat(result.data.location, `is`("Location1"))

    }

    // Test update reminders
    @Test
    fun updateReminderAndGetById() = runBlocking {

        // Given Save a Reminder
        val newReminder = ReminderDTO(
            "Title1",
            "Description1",
            "Location1",
            null,
            null
        )
        repository.saveReminder(newReminder)

        // WHEN - Update Reminder and Get the Reminder by id from the repository.
        val updatedReminder = ReminderDTO(
            "Title1Updated",
            "Description1Updated",
            "Location1Updated",
            null,
            null,
            newReminder.id
        )
        repository.saveReminder(updatedReminder)
        val result = repository.getReminder(newReminder.id)

        // THEN - The loaded data contains the expected values.
        assertThat(result is Result.Success, `is`(true))
        result as Result.Success
        assertThat(result.data.title, `is`("Title1Updated"))
        assertThat(result.data.description, `is`("Description1Updated"))
        assertThat(result.data.location, `is`("Location1Updated"))
    }

    // Test get reminder by id error
    @Test
    fun getReminderByIdAndGetError() {

        // Given a Reminder not in Repository
        val newReminder = ReminderDTO(
            "Title1",
            "Description1",
            "Location1",
            null,
            null
        )


        // WHEN - Get the Reminder by id from the repository.
        var result : Any? = null

        runBlocking {
            result = repository.getReminder(newReminder.id)
        }


        // THEN - A error is returned
        assertThat(result is Result.Error, `is`(true))
        assertThat((result as Result.Error).message, `is`("Reminder not found!"))

    }

    // Test database reminders
    @Test
    fun getRemindersAndCheckData() = runBlocking {

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
        repository.saveReminder(newReminder1)
        repository.saveReminder(newReminder2)

        // WHEN - Get all Reminders from the repository.
        val result = repository.getReminders()

        // THEN - The loaded data contains the expected values.
        assertThat(result is Result.Success, `is`(true))
        result as Result.Success
        assertThat(result.data.size, `is`(2))

    }

    // Test delete all database reminders
    @Test
    fun deleteRemindersAndCheckData() = runBlocking {

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
        repository.saveReminder(newReminder1)
        repository.saveReminder(newReminder2)

        // WHEN - Get delete all Reminders from the repository.
        repository.deleteAllReminders()
        val result = repository.getReminders()

        // THEN - The loaded data contains the expected values.
        assertThat(result is Result.Success, `is`(true))
        result as Result.Success
        assertThat(result.data.size, `is`(0))

    }

    // Close database after testing
    @After
    fun closeDb() = database.close()

}