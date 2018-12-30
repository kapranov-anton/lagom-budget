package com.example.budget.impl

import java.util.UUID

import com.lightbend.lagom.scaladsl.api.transport.NotFound
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

class BudgetServiceTest extends AsyncWordSpec with Matchers with BeforeAndAfterAll {
  private val server =
    ServiceTest.startServer(ServiceTest.defaultSetup.withCassandra()) { ctx =>
      new BudgetApplication(ctx) with LocalServiceLocator
    }

  val client: BudgetService = server.serviceClient.implement[BudgetService]

  override protected def afterAll(): Unit = server.stop()

  val testEntry = BudgetEntry(UUID.randomUUID(), UUID.randomUUID(), 12, 999.99)

  "Budget service" should {
    "allow create entry" in {
      for {
        entryId <- client.create().invoke(testEntry)
        answer <- client.get(entryId).invoke()
      } yield {
        answer shouldEqual testEntry
      }
    }

    "allow update entry" in {
      val updated = testEntry.copy(allocationTerm = 6)
      for {
        entryId <- client.create().invoke(testEntry)
        _ <- client.update(entryId).invoke(updated)
        answer <- client.get(entryId).invoke()
      } yield {
        answer shouldEqual updated
      }
    }

    "allow delete entry" in {
      val updated = testEntry.copy(allocationTerm = 6)
      (for {
        entryId <- client.create().invoke(testEntry)
        _ <- client.delete(entryId).invoke()
        answer <- client.get(entryId).invoke()
      } yield fail()).recover {
        case _: NotFound => succeed
      }
    }
  }

}
