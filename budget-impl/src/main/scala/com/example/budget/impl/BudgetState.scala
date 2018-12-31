package com.example.budget.impl

final case class BudgetState(entry: Option[BudgetEntry] = None,
                             deleted: Boolean = false)
