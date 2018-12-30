package com.example.budget.impl

import java.util.UUID

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import play.api.libs.json.{Format, Json}

trait BudgetService extends Service {
  def create(): ServiceCall[BudgetEntry, UUID]
//  def getAll(): ServiceCall[NotUsed, Map[UUID, BudgetEntry]]
  def get(id: UUID): ServiceCall[NotUsed, BudgetEntry]
  def update(id: UUID): ServiceCall[BudgetEntry, Done]
  def delete(id: UUID): ServiceCall[NotUsed, Done]

  override def descriptor: Descriptor = {
    import Service._
    named("budget").withCalls(
      restCall(Method.POST, "/budget", create),
//      restCall(Method.GET, "/budget", getAll),
      restCall(Method.GET, "/budget/:id", get _),
      restCall(Method.POST, "/budget/:id", update _),
      restCall(Method.DELETE, "/budget/:id", delete _),
    ).withAutoAcl(true)
  }
}

final case class BudgetEntry(departmentId: UUID, projectId: UUID, allocationTerm: Int, amount: BigDecimal)
object BudgetEntry {
  implicit val format: Format[BudgetEntry] = Json.format[BudgetEntry]
}
