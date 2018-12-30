package com.example.budget.impl

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

  val testEntry = BudgetEntry(UUID.randomUUID(), UUID.randomUUID(), 12, 999.99)

  "BudgetEntity" should {
    "allow updating the budget entry" in withTestDriver { driver =>
      val commandOutcome = driver.run(ChangeBudget(testEntry))
      commandOutcome.events should contain only BudgetChanged(testEntry)

      val queryOutcome = driver.run(GetBudget)
      queryOutcome.replies should contain only testEntry
    }

    "allow deleting the budget entry" in withTestDriver { driver =>
      driver.run(ChangeBudget(testEntry))
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
