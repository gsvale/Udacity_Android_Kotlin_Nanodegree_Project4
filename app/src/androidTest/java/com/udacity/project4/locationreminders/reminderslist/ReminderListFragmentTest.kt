package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragmentDirections
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.SelectLocationFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.not
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
import org.mockito.Mockito.*
import java.lang.Thread.sleep


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {


    // test the navigation of the fragments.
    // test the displayed data on the UI.
    // add testing for the error messages.

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        repository = get()
        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }


    @Test
    fun savedRemindersNoData_DisplayedInUi() {

        // GIVEN - Launch Reminder List Fragment
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)


        // WHEN - Fragment is displayed
        onView(withId(R.id.refreshLayout)).check(matches(isDisplayed()))


        // THEN - No Data view is displayed
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))

    }

    @Test
    fun savedRemindersDisplayed_DisplayedInUi() {

        // GIVEN - Save/Add Reminder
        val newReminder = ReminderDTO(
            "Title1",
            "Description1",
            "Location1",
            null,
            null
        )
        runBlocking {
            repository.saveReminder(newReminder)
        }


        // WHEN - Reminder List Fragment Launch is displayed
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        onView(withId(R.id.refreshLayout)).check(matches(isDisplayed()))


        // THEN - No Data view is not displayed
        onView(withId(R.id.noDataTextView)).check(matches(not(isDisplayed())))

    }

    @Test
    fun clickAddReminderButton_navigateToSaveReminderFragment() {

        // GIVEN - Launch Reminder List Fragment / Get Mock NavController
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - Click on the "addReminderFAB" button
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN - Verify that we navigate to the add/save reminder screen
        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    @Test
    fun clickSaveReminderButtonNoTitle_saveReminder() {

        // GIVEN - Launch Save Reminder Fragment /
        launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)

        // WHEN - Click on the "saveReminder" button
        onView(withId(R.id.saveReminder)).perform(click())

        // THEN - Verify the correct message is shown
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_enter_title)))

    }

    @Test
    fun clickSaveReminderButtonNoLocation_saveReminder() {

        // GIVEN - Launch Save Reminder Fragment / Set Title and close keyboard
        launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
        onView(withId(R.id.reminderTitle)).perform(typeText("Title1"))
        closeSoftKeyboard()

        // WHEN - Click on the "saveReminder" button
        onView(withId(R.id.saveReminder)).perform(click())

        // THEN - Verify the correct message is shown
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_select_location)))

    }

    @Test
    fun clickSelectLocationButton_navigateToSelectLocationFragment() {

        // GIVEN - Launch Save Reminder Fragment / Get Mock NavController
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - Click on the "selectLocation" textView
        onView(withId(R.id.selectLocation)).perform(click())

        // THEN - Verify that we navigate to the select location screen
        verify(navController).navigate(
            SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment()
        )
        
    }

    @Test
    fun clickSaveButtonError_saveLocation() {

        // GIVEN - Launch Select Location Fragment / Get Mock NavController
        val scenario = launchFragmentInContainer<SelectLocationFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - Click on the "save" button
        onView(withId(R.id.save_btn)).perform(click())

        // THEN - Verify nothing happens
        verifyZeroInteractions(navController)

    }

    @Test
    fun clickSaveButtonLocation_saveLocation() {

        // GIVEN - Launch Select Location Fragment / Get Mock NavController and perform click on Map
        val scenario = launchFragmentInContainer<SelectLocationFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        onView(withId(R.id.map)).perform(click())

        // WHEN - Click on the "save" button
        onView(withId(R.id.save_btn)).perform(click())

        // THEN - Verify fragment pops back to Save Reminder Fragment
        verify(navController).popBackStack()

    }

    @Test
    fun clickSaveReminderButton_saveReminder() {

        // GIVEN - Launch Save Reminder Fragment / Get Mock NavController / Set Reminder data needed
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
            it._viewModel.reminderTitle.value = "Title1"
            it._viewModel.reminderSelectedLocationStr.value = "Location1"
            it._viewModel.latitude.value = 0.0
            it._viewModel.longitude.value = 0.0
        }

        // WHEN - Click on the "saveReminder" button
        onView(withId(R.id.saveReminder)).perform(click())

        // THEN - Check correct toast message is displayed and Verify fragment pops back to Reminder List Fragment
        scenario.onFragment {
            check(it._viewModel.showToast.value == appContext.getString(R.string.reminder_saved))
        }
        verify(navController).popBackStack()

    }


}