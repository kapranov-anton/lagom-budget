package com.example.budget.impl

import java.sql.Timestamp
import java.util.UUID

object DBBudgetEntryConverters {
  def fromDB(e: DBBudgetEntry): (UUID, BudgetEntry) =
    e.id -> BudgetEntry(
      departmentId = e.departmentId,
      projectId = e.projectId,
      allocationTerm = e.allocationTerm,
      amount = e.amount,
      createdBy = e.createdBy,
      createDate = e.createDate.toLocalDateTime.toLocalDate)

  def toDB(id: UUID, e: BudgetEntry): DBBudgetEntry =
    DBBudgetEntry(
      id = id,
      departmentId = e.departmentId,
      projectId = e.projectId,
      allocationTerm = e.allocationTerm,
      amount = e.amount,
      createdBy = e.createdBy,
      createDate = Timestamp.valueOf(e.createDate.atStartOfDay()))
}
