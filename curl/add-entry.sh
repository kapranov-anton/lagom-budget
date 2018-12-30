curl -XPOST -H "Content-Type: application/json" -d '{
  "departmentId":"ae82473f-b8a2-433a-a8ac-3e4ab4707c8"
, "projectId": "f41645b3-3cd3-4fad-936b-f169b5157681"
, "allocationTerm": 10
, "amount": 1000.23
}' http://localhost:9000/budget
