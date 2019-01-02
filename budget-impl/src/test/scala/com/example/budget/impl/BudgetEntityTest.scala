package com.example.budget.impl

import java.time.LocalDate
import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.InvalidCommandException
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import scala.collection.immutable

object BudgetEntityTestSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: immutable.Seq[JsonSerializer[_]] =
    BudgetSerializerRegistry.serializers :+ JsonSerializer(BudgetEntry.format)
}

class BudgetEntityTest extends WordSpec with Matchers with BeforeAndAfterAll {
  private val system = ActorSystem("BudgetEntitySpec",
    JsonSerializerRegistry.actorSystemSetupFor(BudgetEntityTestSerializerRegistry))

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  private def withTestDriver(block: PersistentEntityTestDriver[BudgetCommand[_], BudgetEvent, BudgetState] => Unit): Unit = {
    val driver = new PersistentEntityTestDriver(system, new BudgetEntity, "budget-1")
    block(driver)
    driver.getAllIssues should have size 0
  }

  "BudgetEntity" should {
    "allow updating the budget entry" in withTestDriver { driver =>
      val commandOutcome = driver.run(ChangeBudget(Fixtures.entry))
      commandOutcome.events should contain only BudgetChanged(Fixtures.entry)

      val queryOutcome = driver.run(GetBudget)
      queryOutcome.replies should contain only Fixtures.entry
    }

    "allow deleting the budget entry" in withTestDriver { driver =>
      driver.run(ChangeBudget(Fixtures.entry))
      val commandOutcome = driver.run(DeleteBudget)
      commandOutcome.events should contain only BudgetDeleted

      val queryOutcome = driver.run(GetBudget)
      queryOutcome.replies should contain only DeletedEntry
    }

    "report error on reading deleted entry" in withTestDriver { driver =>
      val queryOutcome = driver.run(GetBudget)
      queryOutcome.replies should contain only NoSuchEntry
    }
  }

}
