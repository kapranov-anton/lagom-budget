package com.example.budget.impl

import java.time.LocalDate
import java.util.UUID

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import play.api.libs.json._

trait BudgetService extends Service {
  def create(): ServiceCall[BudgetEntry, UUID]
  def getAll(): ServiceCall[NotUsed, Map[UUID, BudgetEntry]]
  def get(id: UUID): ServiceCall[NotUsed, BudgetEntry]
  def update(id: UUID): ServiceCall[BudgetEntry, Done]
  def delete(id: UUID): ServiceCall[NotUsed, Done]

  override def descriptor: Descriptor = {
    import Service._
    named("budget").withCalls(
      restCall(Method.POST, "/budget", create),
      restCall(Method.GET, "/budget", getAll),
      restCall(Method.GET, "/budget/:id", get _),
      restCall(Method.POST, "/budget/:id", update _),
      restCall(Method.DELETE, "/budget/:id", delete _),
    ).withAutoAcl(true)
  }
}

final case class BudgetEntry(
  departmentId: UUID,
  projectId: UUID,
  allocationTerm: Int,
  amount: BigDecimal,
  createdBy: Option[UUID],
  createDate: Option[LocalDate])

object BudgetEntry {
  implicit val format: Format[BudgetEntry] = Json.format[BudgetEntry]

  implicit val mapReads: Reads[Map[UUID, BudgetEntry]] = (jv: JsValue) =>
    JsSuccess(jv.as[Map[String, BudgetEntry]].map { case (k, v) =>
      UUID.fromString(k) -> v
    })

  implicit val mapWrites: Writes[Map[UUID, BudgetEntry]] = (map: Map[UUID, BudgetEntry]) =>
    Json.toJson(map.map { case (s, o) =>
      s.toString -> o
    })

  implicit val jsonMapFormat: Format[Map[UUID, BudgetEntry]] = Format(mapReads, mapWrites)

}

