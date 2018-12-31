package com.example.budget.impl

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType

sealed trait BudgetCommand[R] extends ReplyType[R]

final case class ChangeBudget(entry: BudgetEntry) extends BudgetCommand[Done]

case object DeleteBudget extends BudgetCommand[Done]

case object GetBudget extends BudgetCommand[BudgetEntry]
