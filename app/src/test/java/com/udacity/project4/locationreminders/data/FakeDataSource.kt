package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()) :
    ReminderDataSource {


    fun updateRemindersList(list : List<ReminderDTO>?) {
        reminders = list as MutableList<ReminderDTO>?
    }

    // Create a fake data source to act as a double to the real data source

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        // Return the reminders
        reminders?.let { return Result.Success(ArrayList(it)) }
        return Result.Error(
            "Reminders not found"
        )
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        // Save the reminder
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        // Return the reminder with the id
        reminders?.let {
            for (item in it) {
                if (item.id == id) {
                    return Result.Success(item)
                }
            }
        }
        return Result.Error(
            "Reminder not found"
        )
    }

    override suspend fun deleteAllReminders() {
        // Delete all the reminders
        reminders?.clear()
    }


}