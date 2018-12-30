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

class BudgetEntryEntity extends PersistentEntity {
  override type Command = BudgetEntryCommand[_]
  override type Event = BudgetEntryEvent
  override type State = BudgetEntryState

  override def initialState: BudgetEntryState = BudgetEntryState()

  private final val log: Logger =
    LoggerFactory.getLogger(classOf[BudgetEntryEntity])

  override def behavior: Behavior = {
    case BudgetEntryState(_, _) =>
      Actions()
        .onCommand[ChangeBudgetEntry, Done] {
          case (ChangeBudgetEntry(newEntry), ctx, state) =>
            if (state.deleted)
              log.info("Attempt to update already deleted entry")

            ctx.thenPersist(BudgetEntryChanged(newEntry)) { _ =>
              ctx.reply(Done)
            }
        }
        .onCommand[DeleteBudgetEntry.type, Done] {
          case (_, ctx, state) =>
            if (state.deleted)
              log.info("Attempt to delete already deleted entry")

            ctx.thenPersist(BudgetEntryDeleted) { _ =>
              ctx.reply(Done)
            }
        }
        .onReadOnlyCommand[GetBudgetEntry.type, BudgetEntry] {
          case (_, ctx, BudgetEntryState(_, true)) =>
            ctx.invalidCommand("Deleted entry")
          case (_, ctx, BudgetEntryState(None, false)) =>
            ctx.invalidCommand("No such entry")
          case (_, ctx, BudgetEntryState(Some(entry), false)) =>
            ctx.reply(entry)
        }
        .onEvent {
          case (BudgetEntryChanged(newEntry), state) =>
            state.copy(entry = Some(newEntry))
          case (BudgetEntryDeleted, state) =>
            state.copy(deleted = true)
        }
  }
}

// COMMANDS ///////////////////////////////////////////////////////////////////
sealed trait BudgetEntryCommand[R] extends ReplyType[R]

final case class ChangeBudgetEntry(entry: BudgetEntry)
    extends BudgetEntryCommand[Done]

case object DeleteBudgetEntry extends BudgetEntryCommand[Done]

case object GetBudgetEntry extends BudgetEntryCommand[BudgetEntry]

// EVENTS /////////////////////////////////////////////////////////////////////
sealed trait BudgetEntryEvent extends AggregateEvent[BudgetEntryEvent] {
  override def aggregateTag: AggregateEventTagger[BudgetEntryEvent] =
    BudgetEntryEvent.Tag
}

object BudgetEntryEvent {
  val Tag: AggregateEventTag[BudgetEntryEvent] =
    AggregateEventTag[BudgetEntryEvent]
}

final case class BudgetEntryChanged(entry: BudgetEntry) extends BudgetEntryEvent

case object BudgetEntryDeleted extends BudgetEntryEvent

// STATE //////////////////////////////////////////////////////////////////////
final case class BudgetEntryState(entry: Option[BudgetEntry] = None,
                                  deleted: Boolean = false)

// JSON ///////////////////////////////////////////////////////////////////////
object BudgetSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: immutable.Seq[JsonSerializer[_]] = immutable.Seq(
    // Commands
    JsonSerializer(Json.format[ChangeBudgetEntry]),
    JsonSerializer(JsonSerializer.emptySingletonFormat(DeleteBudgetEntry)),
    JsonSerializer(JsonSerializer.emptySingletonFormat(GetBudgetEntry)),
    // Events
    JsonSerializer(Json.format[BudgetEntryChanged]),
    JsonSerializer(JsonSerializer.emptySingletonFormat(BudgetEntryDeleted)),
    // State
    JsonSerializer(Json.format[BudgetEntryState]),
  )
}
