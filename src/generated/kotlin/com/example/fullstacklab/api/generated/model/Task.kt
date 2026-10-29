package com.example.fullstacklab.api.generated.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import jakarta.validation.Valid

/**
 * 
 * @param id 
 * @param title 
 * @param createdBy 
 * @param createdByName 
 * @param createdAt 
 * @param description 
 * @param dueDate 
 */
data class Task(

    @param:JsonProperty("id", required = true)
    @get:JsonProperty("id", required = true) val id: kotlin.Long,

    @param:JsonProperty("title", required = true)
    @get:JsonProperty("title", required = true) val title: kotlin.String,

    @param:JsonProperty("createdBy", required = true)
    @get:JsonProperty("createdBy", required = true) val createdBy: kotlin.String,

    @param:JsonProperty("createdByName", required = true)
    @get:JsonProperty("createdByName", required = true) val createdByName: kotlin.String,

    @param:JsonProperty("createdAt", required = true)
    @get:JsonProperty("createdAt", required = true) val createdAt: java.time.OffsetDateTime,

    @param:JsonProperty("description")
    @get:JsonProperty("description") val description: kotlin.String? = null,

    @field:Valid
    @param:JsonProperty("dueDate")
    @get:JsonProperty("dueDate") val dueDate: java.time.LocalDate? = null
) {

}

