package com.example.fullstacklab.api.task

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.OffsetDateTime

@Repository
class TaskRepository(private val jdbc: JdbcClient) {

    /** ownerId を指定すると、その利用者が登録したタスクだけを返す */
    fun findAll(ownerId: String?): List<Task> {
        val statement = if (ownerId == null) {
            jdbc.sql("select $COLUMNS from tasks order by id desc")
        } else {
            jdbc.sql("select $COLUMNS from tasks where created_by = :ownerId order by id desc")
                .param("ownerId", ownerId)
        }
        return statement.query(rowMapper).list()
    }

    fun findById(id: Long): Task? =
        jdbc.sql("select $COLUMNS from tasks where id = :id")
            .param("id", id)
            .query(rowMapper)
            .optional()
            .orElse(null)

    fun insert(newTask: NewTask): Task =
        jdbc.sql(
            """
            insert into tasks (title, description, due_date, created_by, created_by_name)
            values (:title, :description, :dueDate, :createdBy, :createdByName)
            returning $COLUMNS
            """.trimIndent(),
        )
            .param("title", newTask.title)
            .param("description", newTask.description)
            .param("dueDate", newTask.dueDate)
            .param("createdBy", newTask.createdBy)
            .param("createdByName", newTask.createdByName)
            .query(rowMapper)
            .single()

    companion object {
        private const val COLUMNS = "id, title, description, due_date, created_by, created_by_name, created_at"

        private val rowMapper = RowMapper { rs, _ ->
            Task(
                id = rs.getLong("id"),
                title = rs.getString("title"),
                description = rs.getString("description"),
                dueDate = rs.getObject("due_date", LocalDate::class.java),
                createdBy = rs.getString("created_by"),
                createdByName = rs.getString("created_by_name"),
                createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
            )
        }
    }
}
