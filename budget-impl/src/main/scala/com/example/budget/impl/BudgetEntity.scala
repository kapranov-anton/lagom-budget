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
