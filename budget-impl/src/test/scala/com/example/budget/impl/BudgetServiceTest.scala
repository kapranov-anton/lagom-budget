package com.example.budget.impl

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

  "Budget service" should {
    "allow create entry" in {
      for {
        entryId <- client.create().invoke(Fixtures.entry)
        answer <- client.get(entryId).invoke()
      } yield {
        answer shouldEqual Fixtures.entry
      }
    }

    "allow update entry" in {
      val updated = Fixtures.entry.copy(allocationTerm = 6)
      for {
        entryId <- client.create().invoke(Fixtures.entry)
        _ <- client.update(entryId).invoke(updated)
        answer <- client.get(entryId).invoke()
      } yield {
        answer shouldEqual updated
      }
    }

    "allow delete entry" in {
      (for {
        entryId <- client.create().invoke(Fixtures.entry)
        _ <- client.delete(entryId).invoke()
        _ <- client.get(entryId).invoke()
      } yield fail()).recover {
        case _: NotFound => succeed
      }
    }
  }

}
