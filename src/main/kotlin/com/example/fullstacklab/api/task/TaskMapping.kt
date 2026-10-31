package com.example.fullstacklab.api.task

import com.example.fullstacklab.api.calendar.DueDateInfo
import com.example.fullstacklab.api.generated.model.CreateTaskRequest
import com.example.fullstacklab.api.generated.model.DueDateInfo as DueDateInfoDto
import com.example.fullstacklab.api.generated.model.Task as TaskDto

/**
 * 業務モデル ⇄ OpenAPI から生成した DTO の変換。
 * 生成コードを業務ロジックに直接広げず、境界（コントローラー）で変換する。
 */
fun Task.toDto() = TaskDto(
    id = id,
    title = title,
    description = description,
    dueDate = dueDate,
    createdBy = createdBy,
    createdByName = createdByName,
    createdAt = createdAt,
    completedAt = completedAt,
)

fun CreateTaskRequest.toNewTask(createdBy: String, createdByName: String) = NewTask(
    title = title,
    description = description,
    dueDate = dueDate,
    createdBy = createdBy,
    createdByName = createdByName,
)

fun DueDateInfo.toDto() = DueDateInfoDto(date = date, businessDay = businessDay, holidayName = holidayName)
