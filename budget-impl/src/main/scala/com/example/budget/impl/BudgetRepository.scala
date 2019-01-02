package com.example.budget.impl

import java.sql.Timestamp
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

  val addColumns = sqlu"""
    ALTER TABLE budget
      ADD COLUMN IF NOT EXISTS created_by uuid NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000',
      ADD COLUMN IF NOT EXISTS create_date date NOT NULL DEFAULT '1970-01-01';

  """

  val migrations = DBIO.seq(
    createTable,
    addColumns,
  )

  class BudgetTable(tag: Tag) extends Table[DBBudgetEntry](tag, "budget") {

    def * =
      (id, departmentId, projectId, allocationTerm, amount, createdBy, createDate) <>
        (DBBudgetEntry.tupled, DBBudgetEntry.unapply)

    def id = column[UUID]("id", O.PrimaryKey)

    def departmentId = column[UUID]("department_id")

    def projectId = column[UUID]("project_id")

    def allocationTerm = column[Int]("allocation_term")

    def amount = column[BigDecimal]("amount")

    def createdBy = column[UUID]("created_by")

    def createDate = column[Timestamp]("create_date")
  }

  val budgetEntries = TableQuery[BudgetTable]

  def selectBudgetEntries() = budgetEntries.result

  def save(id: UUID, e: BudgetEntry) = {
    val dbEntry = DBBudgetEntryConverters.toDB(id, e)
    budgetEntries.insertOrUpdate(dbEntry)
  }
}

final case class DBBudgetEntry(id: UUID,
                               departmentId: UUID,
                               projectId: UUID,
                               allocationTerm: Int,
                               amount: BigDecimal,
                               createdBy: UUID,
                               createDate: Timestamp)

