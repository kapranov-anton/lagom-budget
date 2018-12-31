package com.example.budget.impl

import java.util.UUID

import com.lightbend.lagom.scaladsl.persistence.{AggregateEventTag, ReadSideProcessor}
import com.lightbend.lagom.scaladsl.persistence.slick.SlickReadSide

import scala.concurrent.ExecutionContext

class BudgetEventProcessor(readSide: SlickReadSide,
                           budgetRepo: BudgetRepository)(implicit ec: ExecutionContext) extends ReadSideProcessor[BudgetEvent] {

  override def buildHandler(): ReadSideProcessor.ReadSideHandler[BudgetEvent] = {
    readSide.builder[BudgetEvent]("budgetEventOffset")
      .setGlobalPrepare(budgetRepo.createTable)
      .setEventHandler[BudgetChanged](e => budgetRepo.save(UUID.fromString(e.entityId), e.event.entry))
      .build()
  }

  override def aggregateTags: Set[AggregateEventTag[BudgetEvent]] = {
    BudgetEvent.Tag.allTags
  }
}
