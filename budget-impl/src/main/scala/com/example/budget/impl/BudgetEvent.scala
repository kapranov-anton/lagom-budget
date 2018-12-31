package com.example.budget.impl

import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventShards, AggregateEventTag, AggregateEventTagger}

sealed trait BudgetEvent extends AggregateEvent[BudgetEvent] {
  override def aggregateTag: AggregateEventTagger[BudgetEvent] =
    BudgetEvent.Tag
}

object BudgetEvent {
  // TODO move to config
  val numberOfShards = 4
  val Tag: AggregateEventShards[BudgetEvent] =
    AggregateEventTag.sharded[BudgetEvent](numberOfShards)
}

final case class BudgetChanged(entry: BudgetEntry) extends BudgetEvent

case object BudgetDeleted extends BudgetEvent
