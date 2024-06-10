package com.example.planner

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(),
    RVAdapter.TaskClickDeleteInterface,
    RVAdapter.TaskClickInterface,
    RVAdapter.TaskStatusChangeInterface {
    lateinit var tasksRV: RecyclerView
    lateinit var addFAB: FloatingActionButton
    lateinit var viewModel: TaskViewModel
    lateinit var filterCategories: Spinner
    lateinit var filterDone: Spinner
    lateinit var sort: Spinner
    lateinit var repository: TaskRepository

    private var sortArray: Array<String> = arrayOf<String>(
        "По id",
        "По приближению дедлайна",
        "По отдалению дедлайна",
        "По возрастанию важности",
        "По убыванию важности"
    )
    private var doneArray: Array<String> =
        arrayOf<String>("Не выбрано", "Не выполненные", "Выполненные")
    private var categoriesArray: Array<String> =
        arrayOf<String>("Не выбрано", "Дом", "Работа", "Спорт", "Учеба", "Хобби", "Семья", "Друзья")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tasksRV = findViewById(R.id.tasksRV)
        addFAB = findViewById(R.id.add)
        filterCategories = findViewById(R.id.spinnerCategories)
        filterDone = findViewById(R.id.spinnerDone)
        sort = findViewById(R.id.spinnerSort)

        tasksRV.layoutManager = LinearLayoutManager(this)

        val taskRVAdapter = RVAdapter(this, this, this, this)

        tasksRV.adapter = taskRVAdapter

        val database =
            Room.databaseBuilder(applicationContext, AppDatabase::class.java, "task_database")
                .build()
        repository = TaskRepository(database.taskDao())

        viewModel =
            ViewModelProvider(this, TaskViewModel.TaskViewModelFactory(repository))[TaskViewModel::class.java]

        viewModel.allTasks.observe(this, Observer { list ->
            list?.let {
                taskRVAdapter.updateList(it)
            }
        })

        val doneArrayAdapter =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, doneArray)
        doneArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterDone.adapter = doneArrayAdapter

        filterDone.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                filterCategories.setSelection(0)
                val chosen = parent.getItemAtPosition(position).toString()

                lifecycleScope.launchWhenCreated {
                    if (chosen == "Не выполненные") {
                        viewModel.getTasksWhereStatus(false)
                    }
                    if (chosen == "Выполненные") {
                        viewModel.getTasksWhereStatus(true)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })

        val categoriesArrayAdapter =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categoriesArray)
        categoriesArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterCategories.adapter = categoriesArrayAdapter

        filterCategories.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                filterDone.setSelection(0)
                val chosen = parent.getItemAtPosition(position).toString()

                lifecycleScope.launchWhenCreated {
                    if (chosen != "Не выбрано") {
                        viewModel.getTasksWhereCategory(chosen)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })

        val sortArrayAdapter =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sortArray)
        sortArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sort.adapter = sortArrayAdapter

        sort.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                lifecycleScope.launchWhenCreated {
                    val chosen = parent.getItemAtPosition(position).toString()
                    if (chosen == "По id") {
                        viewModel.getTasksById()
                    }
                    if (chosen == "По приближению дедлайна") {
                        viewModel.getTasksByDeadline0()
                    }
                    if (chosen == "По отдалению дедлайна") {
                        viewModel.getTasksByDeadline1()
                    }
                    if (chosen == "По возрастанию важности") {
                        viewModel.getTasksByPriority1()
                    }
                    if (chosen == "По убыванию важности") {
                        viewModel.getTasksByPriority0()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })

        addFAB.setOnClickListener {
            val intent = Intent(this@MainActivity, AddEditNoteActivity::class.java)
            startActivity(intent)
            this.finish()
        }
    }

    override fun onTaskClick(task: Task) {
        val intent = Intent(this@MainActivity, AddEditNoteActivity::class.java)
        intent.putExtra("taskType", "Edit")
        intent.putExtra("taskTitle", task.title)
        intent.putExtra("taskDescription", task.description)
        intent.putExtra("taskDeadline", task.deadline)
        intent.putExtra("notePriority", task.priority)
        intent.putExtra("taskCategory", task.category)
        intent.putExtra("taskDone", task.done)
        intent.putExtra("noteId", task.id)
        startActivity(intent)
        this.finish()
    }

    override fun onDeleteIconClick(task: Task) {
        viewModel.delete(task)
    }

    override fun onTaskStatusChange(task: Task, isChecked: Boolean) {
        task.done = isChecked
        viewModel.update(task)
    }
    val NOTIFICATION_ID = 1

    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleNotification() {
        val TimeEditText = findViewById<TextView>(R.id.dateTV)
        val Times = TimeEditText.text.toString()

        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        val Time = LocalTime.parse(Times, formatter)
        var notificationDateTime = LocalDateTime.now().with(Time).minusHours(3)
        val CHANNEL_ID = "MYChannel"

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.time)
            .setContentTitle("Привет, трудяга")
            .setContentText("Время выполнения задачи приближается!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(this)

        val myExecutor = Executors.newSingleThreadScheduledExecutor()

// Проверяем дату
        if (notificationDateTime.isBefore(LocalDateTime.now())) {
            notificationDateTime = notificationDateTime.plusDays(1)
        }

// Рассчитываем разницу в миллисекундах
        val timeDifference = ChronoUnit.MILLIS.between(LocalDateTime.now(), notificationDateTime)

// Устанавливаем уведомление
        myExecutor.scheduleWithFixedDelay(
            {
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }, 0, timeDifference, TimeUnit.MILLISECONDS
        )
    }
}