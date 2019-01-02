package com.example.budget.impl

import java.util.UUID

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.NotFound
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.{ExecutionContext, Future}

class BudgetServiceImpl(db: Database,
                        budgetRepo: BudgetRepository,
                        persistentEntityRegistry: PersistentEntityRegistry)
                       (implicit ec: ExecutionContext) extends BudgetService {
  override def create(): ServiceCall[BudgetEntry, UUID] = ServiceCall {
    budgetEntry =>
      val id = UUID.randomUUID()
      val ref = persistentEntityRegistry.refFor[BudgetEntity](id.toString)
      ref.ask(ChangeBudget(budgetEntry)).map(_ => id)
  }

  override def get(id: UUID): ServiceCall[NotUsed, BudgetEntry] = ServiceCall {
    _ =>
      val ref = persistentEntityRegistry.refFor[BudgetEntity](id.toString)
      ref
        .ask(GetBudget)
        .recoverWith {
          case NoSuchEntry => Future.failed(NotFound("No such entry"))
          case DeletedEntry => Future.failed(NotFound("Deleted entry"))
        }
  }

  override def update(id: UUID): ServiceCall[BudgetEntry, Done] = ServiceCall {
    budgetEntry =>
      val ref = persistentEntityRegistry.refFor[BudgetEntity](id.toString)
      ref.ask(ChangeBudget(budgetEntry))
  }

  override def delete(id: UUID): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    val ref = persistentEntityRegistry.refFor[BudgetEntity](id.toString)
    ref.ask(DeleteBudget)
  }

  override def getAll(): ServiceCall[NotUsed, Map[UUID, BudgetEntry]] =
    ServiceCall { _ =>
      db.run(budgetRepo.selectBudgetEntries()).map { entries =>
        entries.map(DBBudgetEntryConverters.fromDB).toMap
      }
    }
}
