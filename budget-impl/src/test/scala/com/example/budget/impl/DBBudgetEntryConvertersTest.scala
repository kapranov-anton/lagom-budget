package com.example.budget.impl

import java.util.UUID

import org.scalatest.{Matchers, WordSpec}

class DBBudgetEntryConvertersTest extends WordSpec with Matchers {
  "DBBudgetEntryConverters" should {
    "be able to convert entry to db format and from it" in {
      import DBBudgetEntryConverters._
      val id = UUID.randomUUID()
      val result = fromDB(toDB(id, Fixtures.entry))
      result shouldEqual (id, Fixtures.entry)
    }
  }
}
