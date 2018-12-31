package com.example.budget.impl

import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import play.api.libs.json.Json

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
}
