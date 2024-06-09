package com.example.planner

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "tasksTable")
class Task(
    @ColumnInfo(name = "title") val title: String? = null,
    @ColumnInfo(name = "description") val description: String? = null,
    @ColumnInfo(name = "deadline")  val deadline: Long? = 0,
    @ColumnInfo(name = "priority") val priority: Int = 0,
    @ColumnInfo(name = "category") val category: String? = null,
    @ColumnInfo(name = "done") var done: Boolean = false)
{
    @PrimaryKey(autoGenerate = true)
    var id = 0
}