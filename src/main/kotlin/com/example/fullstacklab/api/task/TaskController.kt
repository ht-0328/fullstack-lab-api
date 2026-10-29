package com.example.fullstacklab.api.task

import com.example.fullstacklab.api.calendar.CalendarClient
import com.example.fullstacklab.api.common.NotFoundException
import com.example.fullstacklab.api.generated.api.TasksApi
import com.example.fullstacklab.api.generated.model.CreateTaskRequest
import com.example.fullstacklab.api.generated.model.DueDateInfo as DueDateInfoDto
import com.example.fullstacklab.api.generated.model.Task as TaskDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.net.URI

/**
 * 業務データ（タスク）の API。openapi/api.yaml から生成した TasksApi を実装する（仕様が正）。
 * 画面の都合は知らず、BFF からのみ呼ばれる想定。
 * 認証・認可は BFF が済ませ、利用者 ID と表示名を X-User-Id / X-User-Name で渡す（API 自体は外部公開しない）。
 */
@RestController
class TaskController(
    private val repository: TaskRepository,
    private val calendarClient: CalendarClient,
) : TasksApi {

    override fun listTasks(ownerId: String?): ResponseEntity<List<TaskDto>> =
        ResponseEntity.ok(repository.findAll(ownerId).map { it.toDto() })

    override fun getTask(id: Long): ResponseEntity<TaskDto> = ResponseEntity.ok(findTask(id).toDto())

    override fun createTask(
        xUserId: String,
        xUserName: String,
        createTaskRequest: CreateTaskRequest,
    ): ResponseEntity<TaskDto> {
        val task = repository.insert(createTaskRequest.toNewTask(createdBy = xUserId, createdByName = xUserName))
        return ResponseEntity.created(URI.create("/api/tasks/${task.id}")).body(task.toDto())
    }

    /** タスクの期限日について、外部カレンダーサービスから補足情報を取得する */
    override fun getTaskDueDateInfo(id: Long): ResponseEntity<DueDateInfoDto> {
        val dueDate = findTask(id).dueDate ?: throw NotFoundException("task $id has no due date")
        return ResponseEntity.ok(calendarClient.getDay(dueDate).toDto())
    }

    private fun findTask(id: Long): Task = repository.findById(id) ?: throw NotFoundException("task $id not found")
}
