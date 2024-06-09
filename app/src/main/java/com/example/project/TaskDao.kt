package com.example.planner

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(task :Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("Select * from tasksTable order by id ASC")
    fun getAllTasks(): LiveData<List<Task>>

    @Query("SELECT * FROM tasksTable ORDER BY id ASC")
    suspend fun getTasksById(): List<Task?>?

    @Query("SELECT * FROM tasksTable ORDER BY deadline ASC")
    suspend fun getTasksByDeadline1(): List<Task?>?

    @Query("SELECT * FROM tasksTable ORDER BY deadline DESC")
    suspend fun getTasksByDeadline0(): List<Task?>?

    @Query("SELECT * FROM tasksTable ORDER BY priority ASC")
    suspend fun getTasksByPriority1(): List<Task?>?

    @Query("SELECT * FROM tasksTable ORDER BY priority DESC")
    suspend fun getTasksByPriority0(): List<Task?>?

    @Query("SELECT * FROM tasksTable WHERE category = :category")
    suspend fun getTasksWhereCategory(category: String?): List<Task?>?

    @Query("SELECT * FROM tasksTable WHERE done = :status")
    suspend fun getTasksWhereStatus(status: Boolean): List<Task?>?


}