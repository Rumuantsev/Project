package com.example.planner

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class AddEditNoteActivity : AppCompatActivity() {
    lateinit var titleET: EditText
    lateinit var descET: EditText
    lateinit var saveBtn: FloatingActionButton
    lateinit var dateTV: TextView
    lateinit var timeTV: TextView
    lateinit var dateBtn: Button
    lateinit var timeBtn: Button
    lateinit var spinnerPriority: Spinner
    lateinit var spinnerCategory: Spinner
    lateinit var viewModel: TaskViewModel
    lateinit var repository: TaskRepository
    var taskID = -1;
    private val calendar = Calendar.getInstance()
    var taskDeadline: Long? = null


    private var categoriesArray: Array<String> =
        arrayOf<String>("Не выбрано", "Дом", "Работа", "Спорт", "Учеба", "Хобби", "Семья", "Друзья")
    private var priorityArray: Array<Int> = arrayOf<Int>(0, 1, 2, 3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_task)

        val database =
            Room.databaseBuilder(applicationContext, AppDatabase::class.java, "task_database")
                .build()
        repository = TaskRepository(database.taskDao())

        viewModel =
            ViewModelProvider(this, TaskViewModel.TaskViewModelFactory(repository))[TaskViewModel::class.java]

        titleET = findViewById(R.id.title)
        descET = findViewById(R.id.descET)
        saveBtn = findViewById(R.id.save)
        dateBtn = findViewById(R.id.btn_date)
        dateTV = findViewById(R.id.dateTV)
        spinnerPriority = findViewById(R.id.spinnerEditPriority)
        spinnerCategory = findViewById(R.id.spinnerEditCategories)

        val taskType = intent.getStringExtra("taskType")
        val chosenTaskTitle = intent.getStringExtra("taskTitle")
        val chosenTaskDescription = intent.getStringExtra("taskDescription")
        val chosenTaskPriority = intent.getIntExtra("taskPriority", 0)
        val chosenTaskCategory = intent.getStringExtra("taskCategory")
        val chosenTaskDeadline = intent.getLongExtra("taskDeadline", 0)
        val chosenTaskDone = intent.getBooleanExtra("taskDone", false)
        taskID = intent.getIntExtra("taskId", -1)

        titleET.setText(chosenTaskTitle)
        descET.setText(chosenTaskDescription)
        val deadlineToView =
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(chosenTaskDeadline)
        dateTV.setText(deadlineToView)

        val categoriesArrayAdapter =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categoriesArray)
        categoriesArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoriesArrayAdapter
        val categoryIndex = categoriesArray.indexOf(chosenTaskCategory)
        spinnerCategory.setSelection(categoryIndex)


        val priorityArrayAdapter =
            ArrayAdapter<Int>(this, android.R.layout.simple_spinner_item, priorityArray)
        priorityArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPriority.adapter = priorityArrayAdapter
        spinnerPriority.setSelection(chosenTaskPriority)


        fun showDatePickerDialog() {
            val datePickerDialog = DatePickerDialog(
                this, { DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                    val selectedDate: Calendar = Calendar.getInstance()
                    selectedDate.set(year, monthOfYear, dayOfMonth)
                    taskDeadline = selectedDate.timeInMillis
                    val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                    val formattedDate: String = dateFormat.format(taskDeadline)
                    dateTV.setText(formattedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        dateBtn.setOnClickListener {
            showDatePickerDialog()
        }

        saveBtn.setOnClickListener {
            val taskTitle = titleET.text.toString()
            val taskDescription = descET.text.toString()
            val taskPriority = spinnerPriority.selectedItem as Int
            val taskCategory = spinnerCategory.selectedItem.toString()

            if (taskType.equals("Edit")) {
                if (taskTitle.isNotEmpty() && taskDescription.isNotEmpty()) {
                    val updatedTask = Task(
                        taskTitle,
                        taskDescription,
                        taskDeadline,
                        taskPriority,
                        taskCategory,
                        chosenTaskDone
                    )
                    updatedTask.id = taskID
                    viewModel.update(updatedTask)
                }
            } else {
                    viewModel.insert(
                        Task(
                            taskTitle,
                            taskDescription,
                            taskDeadline,
                            taskPriority,
                            taskCategory,
                            chosenTaskDone
                        )
                    )

            }
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("SCHEDULE_NOTIFICATION", true)
// startActivity(Intent(applicationContext, MainActivity::class.java))
            startActivity(intent)
            this.finish()
        }

    }
}
