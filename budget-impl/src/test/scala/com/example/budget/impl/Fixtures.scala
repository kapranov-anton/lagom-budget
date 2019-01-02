package com.example.budget.impl

import java.time.LocalDate
import java.util.UUID

object Fixtures {
  val entry = BudgetEntry(
    UUID.randomUUID(),
    UUID.randomUUID(),
    12,
    999.99,
    Some(UUID.randomUUID()),
    Some(LocalDate.of(1970, 1, 1)))
}
