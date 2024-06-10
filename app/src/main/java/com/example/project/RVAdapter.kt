package com.example.planner

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class RVAdapter(
    //Отвечает за отображения списка.
    private val context: Context, //доступ к ресурсам.
    private val taskClickDeleteInterface: TaskClickDeleteInterface, //обработска события удаления
    private val taskClickInterface: TaskClickInterface,//обработка собяия нажатия.
    private val taskStatusChangeInterface: TaskStatusChangeInterface//обработка изменения
) : RecyclerView.Adapter<RVAdapter.TaskViewHolder>() {//отображение элемента из-за вью холдера.

    private val allTasks = ArrayList<Task>()

    interface TaskClickDeleteInterface {//при клике вызов кнопки
    fun onDeleteIconClick(task: Task)
    }

    interface TaskClickInterface {//при клике на задачу вызывает саму задачу
    fun onTaskClick(task: Task)
    }

    interface TaskStatusChangeInterface {//при изменения статуса смотрится какой статус у задачи теперь.
    fun onTaskStatusChange(task: Task, isChecked: Boolean)
    }


    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {//отображения элементов.
    val taskTitleTextView: TextView = view.findViewById(R.id.title)
        val taskCategoryTextView: TextView = view.findViewById(R.id.category)
        val taskDeadlineTextView: TextView = view.findViewById(R.id.deadline)
        val deleteImageView: ImageView = view.findViewById(R.id.idIVDelete)
        val taskStatusCheckBox: CheckBox = view.findViewById(R.id.checkBox)
    }

    //Таким образом, метод onCreateViewHolder отвечает за создание новых экземпляров TaskViewHolder для элементов списка задач в RecyclerView,
    // что позволяет адаптеру отображать элементы списка с помощью соответствующих ViewHolder.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {//создание новых экземпляром golders для списка задач
        //Метод onCreateViewHolder создает новый элемент (View) для списка задач, используя метод inflate класса LayoutInflater.
        // Метод inflate создает новый элемент, загружая layout из ресурсов (R.layout.task_item), и добавляет его в группу элементов (parent).
        //Метод onCreateViewHolder возвращает новый экземпляр класса TaskViewHolder, созданный для нового элемента.
        // Этот экземпляр будет использоваться для отображения данных задачи в RecyclerView.
        val view = LayoutInflater.from(parent.context).inflate(getLayoutForType(viewType), parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = allTasks[position] //позиция обновления задачи.
        holder.taskTitleTextView.text = task.title //bvz pflfxb
        holder.taskCategoryTextView.text = task.category
        val deadlineToView = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(task.deadline)
        holder.taskDeadlineTextView.text = deadlineToView.toString()
        holder.taskStatusCheckBox.isChecked = task.done

        holder.deleteImageView.setOnClickListener {//при нажатии на иконку вызывается.
            taskClickDeleteInterface.onDeleteIconClick(task)
        }

        holder.itemView.setOnClickListener {
            taskClickInterface.onTaskClick(task)
        }

        holder.taskStatusCheckBox.setOnCheckedChangeListener { _, isChecked ->
            //taskStatusCheckBox: чекбокс для отображения статуса выполнения задачи.
            // При изменении статуса вызывается метод onTaskStatusChange интерфейса TaskStatusChangeInterface.
            taskStatusChangeInterface.onTaskStatusChange(task, isChecked)
        }
        //етод onBindViewHolder отвечает за обновление данных
        // в существующих экземплярах TaskViewHolder и обрабатывает события, связанные с элементами списка задач.
    }

    override fun getItemCount(): Int {//метод ищект количество элементов в списке задач.
        return allTasks.size
    }

    override fun getItemViewType(position: Int): Int {//происходит определение для каждого элемента ресайкл.
        val task = allTasks[position]
        return if (task.done) {//выполнена
            R.layout.done_task_item
        } else { //иначе вызываются категории
            when (task.priority) {
                1 -> R.layout.task_item1 //если приоритет 1, вызывается следующая
                2 -> R.layout.task_item2
                3 -> R.layout.task_item3
                else -> R.layout.base_task_item //в противном случае.
            }
        }
    }

    fun updateList(newList: List<Task>?) { //метод пнинимает новый лист задач.
        val oldList = ArrayList(allTasks)//создается временный список для хранения
        allTasks.clear()//контент текящего списка очищается.
        newList?.let {
            //если новый список задач (newList) не пустой (not null), то добавляется в текущий список задач (allTasks) (addAll).
            allTasks.addAll(it)
            Log.d("RVAdapter", "Updated list with ${allTasks.size} items")
        }?: Log.d("RVAdapter", "Updated list with null or empty list")

        val diffCallback = TaskDiffCallback(oldList, allTasks)//создание для сравнения задачс предыдщем списком.
        //calculateDiff класса DiffUtil сравнивает списки задач и возвращает объект DiffResult, который содержит информацию о изменениях в списках задач.
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        diffResult.dispatchUpdatesTo(this)//обновление списка результатами result/
    }

    private fun getLayoutForType(viewType: Int): Int {//для определения layout-файла, который будет использоваться для создания нового элемента списка задач в RecyclerView.
        return when (viewType) {//Если viewType равен R.layout.task_item1, то возвращается R.layout.task_item1
            R.layout.task_item1 -> R.layout.task_item1
            R.layout.task_item2 -> R.layout.task_item2
            R.layout.task_item3 -> R.layout.task_item3
            R.layout.done_task_item -> R.layout.done_task_item
            else -> R.layout.base_task_item
        }
    }

    class TaskDiffCallback(private val oldList: List<Task>, private val newList: List<Task>) : DiffUtil.Callback() {
        // этом коде реализуется класс TaskDiffCallback, который является callback-ом для DiffUtil в Android.
        // DiffUtil - это утилита, которая помогает сравнивать два списка и определить, какие элементы были добавлены, удалены или изменены.
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id//сравнивает два элемента по их идентификаторам (id
            //аким образом, класс TaskDiffCallback помогает DiffUtil сравнивать списки задач и определить,
            // какие элементы были добавлены, удалены или изменены, что позволяет эффективно обновлять список задач в RecyclerView.
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldTask = oldList[oldItemPosition]//позия в старом списке задач.
            val newTask = newList[newItemPosition]
            return oldTask.title == newTask.title &&//сравнение следующих харектеристик.
                    oldTask.category == newTask.category &&
                    oldTask.deadline == newTask.deadline &&
                    oldTask.done == newTask.done &&
                    oldTask.priority == newTask.priority
            //Таким образом, метод areContentsTheSame помогает DiffUtil э
            // ффективно обновлять список задач в RecyclerView, обновляя только те элементы, содержимое которых изменилось.
        }
    }
}


