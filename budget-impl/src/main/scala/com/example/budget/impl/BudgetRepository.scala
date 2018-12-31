package com.example.budget.impl

import java.util.UUID

import slick.jdbc.PostgresProfile.api._

class BudgetRepository {
  val createTable = sqlu"""
    CREATE TABLE IF NOT EXISTS budget (
      id uuid PRIMARY KEY
    , department_id uuid NOT NULL
    , project_id uuid NOT NULL
    , allocation_term int NOT NULL
    , amount numeric(11, 2) NOT NULL
    )
  """

  class BudgetTable(tag: Tag) extends Table[DBBudgetEntry](tag, "budget") {

    def * =
      (id, departmentId, projectId, allocationTerm, amount) <> (DBBudgetEntry.tupled, DBBudgetEntry.unapply)

    def id = column[UUID]("id", O.PrimaryKey)

    def departmentId = column[UUID]("department_id")

    def projectId = column[UUID]("project_id")

    def allocationTerm = column[Int]("allocation_term")

    def amount = column[BigDecimal]("amount")
  }

  val budgetEntries = TableQuery[BudgetTable]

  def selectBudgetEntries() = budgetEntries.result

  def save(id: UUID, e: BudgetEntry) = {
    val dbEntry = DBBudgetEntry(
      id = id,
      departmentId = e.departmentId,
      projectId = e.projectId,
      allocationTerm = e.allocationTerm,
      amount = e.amount)
    budgetEntries.insertOrUpdate(dbEntry)
  }
}

final case class DBBudgetEntry(id: UUID,
                               departmentId: UUID,
                               projectId: UUID,
                               allocationTerm: Int,
                               amount: BigDecimal)
