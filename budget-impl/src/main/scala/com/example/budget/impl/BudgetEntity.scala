package com.example.budget.impl

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{
  AggregateEvent,
  AggregateEventTag,
  AggregateEventTagger,
  PersistentEntity
}
import com.lightbend.lagom.scaladsl.playjson.{
  JsonSerializer,
  JsonSerializerRegistry
}
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{Format, Json}

import scala.collection.immutable

class BudgetEntity extends PersistentEntity {
  override type Command = BudgetCommand[_]
  override type Event = BudgetEvent
  override type State = BudgetState

  override def initialState: BudgetState = BudgetState()

  private final val log: Logger =
    LoggerFactory.getLogger(classOf[BudgetEntity])

  override def behavior: Behavior = {
    case BudgetState(_, _) =>
      Actions()
        .onCommand[ChangeBudget, Done] {
          case (ChangeBudget(newEntry), ctx, state) =>
            if (state.deleted)
              log.info("Attempt to update already deleted entry")

            ctx.thenPersist(BudgetChanged(newEntry)) { _ =>
              ctx.reply(Done)
            }
        }
        .onCommand[DeleteBudget.type, Done] {
          case (_, ctx, state) =>
            if (state.deleted)
              log.info("Attempt to delete already deleted entry")

            ctx.thenPersist(BudgetDeleted) { _ =>
              ctx.reply(Done)
            }
        }
        .onReadOnlyCommand[GetBudget.type, BudgetEntry] {
          case (_, ctx, BudgetState(_, true)) =>
            ctx.commandFailed(DeletedEntry)
          case (_, ctx, BudgetState(None, false)) =>
            ctx.commandFailed(NoSuchEntry)
          case (_, ctx, BudgetState(Some(entry), false)) =>
            ctx.reply(entry)
        }
        .onEvent {
          case (BudgetChanged(newEntry), state) =>
            state.copy(entry = Some(newEntry))
          case (BudgetDeleted, state) =>
            state.copy(deleted = true)
        }
  }
}

// FAILURES ///////////////////////////////////////////////////////////////////
case object DeletedEntry extends Throwable

case object NoSuchEntry extends Throwable

// COMMANDS ///////////////////////////////////////////////////////////////////
sealed trait BudgetCommand[R] extends ReplyType[R]

final case class ChangeBudget(entry: BudgetEntry) extends BudgetCommand[Done]

case object DeleteBudget extends BudgetCommand[Done]

case object GetBudget extends BudgetCommand[BudgetEntry]

// EVENTS /////////////////////////////////////////////////////////////////////
sealed trait BudgetEvent extends AggregateEvent[BudgetEvent] {
  override def aggregateTag: AggregateEventTagger[BudgetEvent] =
    BudgetEvent.Tag
}

object BudgetEvent {
  val Tag: AggregateEventTag[BudgetEvent] =
    AggregateEventTag[BudgetEvent]
}

final case class BudgetChanged(entry: BudgetEntry) extends BudgetEvent

case object BudgetDeleted extends BudgetEvent

// STATE //////////////////////////////////////////////////////////////////////
final case class BudgetState(entry: Option[BudgetEntry] = None,
                             deleted: Boolean = false)

// JSON ///////////////////////////////////////////////////////////////////////
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
