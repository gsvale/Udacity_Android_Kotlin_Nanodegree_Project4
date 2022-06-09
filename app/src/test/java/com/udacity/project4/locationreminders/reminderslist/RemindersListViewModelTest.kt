package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.koin.test.inject
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.P])
class RemindersListViewModelTest :
    AutoCloseKoinTest() {

    private val newReminder1 = ReminderDTO(
        "Title1",
        "Description1",
        "Location1",
        null,
        null
    )

    private val newReminder2 = ReminderDTO(
        "Title2",
        "Description2",
        "Location2",
        null,
        null
    )

    private lateinit var remindersDataSource: FakeDataSource
    private lateinit var appContext: Application

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private val reminderListViewModel: RemindersListViewModel by inject()

    // Init variables with koin

    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as FakeDataSource
                )
            }
            single { FakeDataSource(mutableListOf(newReminder1, newReminder2)) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        remindersDataSource = get()

    }

    // Provide testing to the RemindersListViewModel and its live data objects

    @Test
    fun getReminderList_getsReminderListEvent() = runBlockingTest {

        // Pause dispatcher so we can verify initial values
        mainCoroutineRule.pauseDispatcher()

        // When getting reminder list
        reminderListViewModel.loadReminders()

        // Then progress indicator is shown
        assertThat(reminderListViewModel.showLoading.value, `is`(true))

        // Execute pending coroutines actions
        mainCoroutineRule.resumeDispatcher()

        // Verify live data item list
        val liveDataToast = reminderListViewModel.remindersList
        assert(!liveDataToast.value.isNullOrEmpty())

        val item = remindersDataSource.getReminders() as Result.Success<*>

        // // Then progress indicator is hidden
        assertThat(reminderListViewModel.showLoading.value, `is`(false))

        // Check reminder list matches
        assert((item.data as List<*>).size == liveDataToast.value!!.size)

    }

    @Test
    fun getReminderList_getsDeleteAllEvent() = runBlockingTest {

        // When delete all reminder list
        remindersDataSource.deleteAllReminders()

        reminderListViewModel.loadReminders()
/**/
        // Verify live data item list
        val liveDataToast = reminderListViewModel.remindersList
        assert(liveDataToast.value.isNullOrEmpty())

        val item = remindersDataSource.getReminders() as Result.Success<*>

        // Check reminder list is empty
        assert((item.data as List<*>).size == 0)

    }

    @Test
    fun getReminderList_getsErrorResultEvent() = runBlockingTest {

        // When getting reminder list
        reminderListViewModel.loadReminders()

        assert(reminderListViewModel.showSnackBar.value.isNullOrEmpty())

        remindersDataSource.setReturnError(true)

        val item = remindersDataSource.getReminders()

        // Check reminder list result error
        assert(item is Result.Error)
        assertThat(reminderListViewModel.showLoading.value, `is`(false))


    }

}

