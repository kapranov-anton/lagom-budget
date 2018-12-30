package com.example.budget.impl

import java.util.UUID

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.concurrent.ExecutionContext

class BudgetServiceImpl(persistentEntityRegistry: PersistentEntityRegistry)(implicit ec: ExecutionContext) extends BudgetService {
  override def create(): ServiceCall[BudgetEntry, UUID] = ServiceCall { budgetEntry =>
    val id = UUID.randomUUID()
    val ref = persistentEntityRegistry.refFor[BudgetEntryEntity](id.toString)
    ref.ask(ChangeBudgetEntry(budgetEntry)).map(_ => id)
  }

  override def get(id: UUID): ServiceCall[NotUsed, BudgetEntry] = ServiceCall { _ =>
    val ref = persistentEntityRegistry.refFor[BudgetEntryEntity](id.toString)
    ref.ask(GetBudgetEntry)
  }

  override def update(id: UUID): ServiceCall[BudgetEntry, Done] = ServiceCall { budgetEntry =>
    val ref = persistentEntityRegistry.refFor[BudgetEntryEntity](id.toString)
    ref.ask(ChangeBudgetEntry(budgetEntry))
  }

  override def delete(id: UUID): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    val ref = persistentEntityRegistry.refFor[BudgetEntryEntity](id.toString)
    ref.ask(DeleteBudgetEntry)
  }
}
