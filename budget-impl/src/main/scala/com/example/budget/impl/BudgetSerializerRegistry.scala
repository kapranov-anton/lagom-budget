package com.example.budget.impl

import java.time.LocalDate
import java.util.UUID

import com.lightbend.lagom.scaladsl.playjson.{JsonMigration, JsonMigrations, JsonSerializer, JsonSerializerRegistry}
import play.api.libs.json._

import scala.collection.immutable

object BudgetSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: immutable.Seq[JsonSerializer[_]] = immutable.Seq(
    // Failures
    JsonSerializer(JsonSerializer.emptySingletonFormat(DeletedEntry)),
    JsonSerializer(JsonSerializer.emptySingletonFormat(NoSuchEntry)),
    // Commands
    JsonSerializer(Json.format[ChangeBudget]),
    JsonSerializer(JsonSerializer.emptySingletonFormat(DeleteBudget)),
    JsonSerializer(JsonSerializer.emptySingletonFormat(GetBudget)),
    // Events
    JsonSerializer(Json.format[BudgetChanged]),
    JsonSerializer(JsonSerializer.emptySingletonFormat(BudgetDeleted)),
    // State
    JsonSerializer(Json.format[BudgetState]),
  )

  val defaultUUID: JsValue = Json.toJson(new UUID(0, 0))
  val defaultDate: JsValue = Json.toJson(LocalDate.of(1970, 1, 1))
  val addDefaultCreationInfo: Reads[JsObject] =
    JsPath.json.update(
      (__ \ 'entry \ 'createdBy).json.put(defaultUUID)).andThen(
    JsPath.json.update(
      (__ \ 'entry \ 'createDate).json.put(defaultDate)))

  override def migrations: Map[String, JsonMigration] = Map[String, JsonMigration](
    JsonMigrations.transform[ChangeBudget](immutable.SortedMap(
      1 -> addDefaultCreationInfo
    )),
    JsonMigrations.transform[BudgetChanged](immutable.SortedMap(
      1 -> addDefaultCreationInfo
    )),
  )
}

