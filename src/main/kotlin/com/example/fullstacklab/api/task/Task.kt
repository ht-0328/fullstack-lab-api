package com.example.fullstacklab.api.task

import java.time.LocalDate
import java.time.OffsetDateTime

/** 永続化されたタスク（DB の行に対応する業務モデル）。API の DTO とは TaskMapping で変換する */
data class Task(
    val id: Long,
    val title: String,
    val description: String?,
    val dueDate: LocalDate?,
    val createdBy: String,
    val createdByName: String,
    val createdAt: OffsetDateTime,
)

/** 新規登録の入力。API の DTO（CreateTaskRequest）から変換して受け取る */
data class NewTask(
    val title: String,
    val description: String?,
    val dueDate: LocalDate?,
    val createdBy: String,
    val createdByName: String,
)
