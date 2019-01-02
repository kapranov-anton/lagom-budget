package com.example.budget.impl

import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json._

class BudgetSerializerRegistryTest extends WordSpec with Matchers {
  "BudgetSerializerRegistry" should {
    "be able to migrate json" in {
      implicit val formatEvent: OFormat[BudgetChanged] = Json.format[BudgetChanged]
      implicit val formatCommand: OFormat[ChangeBudget] = Json.format[ChangeBudget]
      val oldFormat = Json.parse("""{"entry": {
        "departmentId":"ae82473f-b8a2-433a-a8ac-3e4ab4707c8"
      , "projectId": "f41645b3-3cd3-4fad-936b-f169b5157681"
      , "allocationTerm": 10
      , "amount": 1000.23
      }}""")

      val migrated = oldFormat
        .transform(BudgetSerializerRegistry.addDefaultCreationInfo)
        .get

      Json.fromJson[BudgetChanged](migrated).asOpt should not be empty
      Json.fromJson[ChangeBudget](migrated).asOpt should not be empty
    }
  }
}
