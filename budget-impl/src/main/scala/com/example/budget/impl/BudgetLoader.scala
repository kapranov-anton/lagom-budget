package com.example.budget.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.api.{Descriptor, ServiceLocator}
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.jdbc.JdbcPersistenceComponents
import com.lightbend.lagom.scaladsl.persistence.slick.SlickPersistenceComponents
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LagomApplicationContext, LagomApplicationLoader, LagomServer}
import com.softwaremill.macwire._
import play.api.db.HikariCPComponents
import play.api.libs.ws.ahc.AhcWSComponents

class BudgetLoader extends LagomApplicationLoader {
  override def load(context: LagomApplicationContext): LagomApplication =
    new BudgetApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new BudgetApplication(context) with LagomDevModeComponents

  override def describeService: Option[Descriptor] = Some(readDescriptor[BudgetService])
}

abstract class BudgetApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with JdbcPersistenceComponents
    with SlickPersistenceComponents
    with HikariCPComponents
    with AhcWSComponents {
  override def lagomServer: LagomServer = serverFor[BudgetService](wire[BudgetServiceImpl])

  override def jsonSerializerRegistry: JsonSerializerRegistry = BudgetSerializerRegistry

  lazy val repository: BudgetRepository = wire[BudgetRepository]

  persistentEntityRegistry.register(wire[BudgetEntity])

  readSide.register(wire[BudgetEventProcessor])
}
