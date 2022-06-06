package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Build
import android.text.TextUtils
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.koin.test.inject
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class SaveReminderViewModelTest:
    AutoCloseKoinTest() {


    private val newReminder1 = ReminderDataItem(
        "Title1",
        "Description1",
        "Location1",
        null,
        null
    )

    private val newReminder2 = ReminderDataItem(
        "Title2",
        "Description2",
        null,
        null,
        null
    )

    private lateinit var remindersDataSource: FakeDataSource
    private lateinit var appContext: Application

    // Init variables with koin

    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()
        val myModule = module {
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as FakeDataSource
                )
            }
            single { FakeDataSource(mutableListOf())}
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        remindersDataSource = get()

    }


    // Provide testing to the SaveReminderViewModel and its live data objects

    @Test
    fun saveNewReminder_setsNewReminderEvent() = runBlockingTest {

        // Given a SaveReminderViewModel
        val saveReminderViewModel : SaveReminderViewModel by inject()

        // Verify data inserted
        assert(saveReminderViewModel.validateEnteredData(newReminder1))

        // When saving a new reminder
        saveReminderViewModel.validateAndSaveReminder(newReminder1)

        // Verify live data toast message
        val liveDataToast = saveReminderViewModel.showToast
        assert(!TextUtils.isEmpty(liveDataToast.value))

        val item = remindersDataSource.getReminder(newReminder1.id)

        // Then the save reminder event is triggered
        assert(item is Result.Success)

    }

    @Test
    fun saveNewReminder_setsInvalidDataEvent() = runBlockingTest {

        // Given a SaveReminderViewModel
        val saveReminderViewModel : SaveReminderViewModel by inject()

        // Verify data inserted, that fails due to invalid data (missing location)
        assert(!saveReminderViewModel.validateEnteredData(newReminder2))

        // When saving a new reminder
        saveReminderViewModel.validateAndSaveReminder(newReminder2)

        // Verify live data toast message
        val liveDataToast = saveReminderViewModel.showToast
        assert(TextUtils.isEmpty(liveDataToast.value))

        val item = remindersDataSource.getReminder(newReminder1.id)

        // Then the save reminder event is triggered
        assert(item is Result.Error)

    }


}