#!/bin/bash -x

uuid() {
    cat /proc/sys/kernel/random/uuid
}

create_entry() {
    curl -XPOST -H "Content-Type: application/json" -d '{
      "departmentId":"'$1'"
    , "projectId": "'$(uuid)'"
    , "allocationTerm": '$RANDOM'
    , "amount": '$RANDOM'.'$RANDOM'
    }' http://localhost:9000/budget
}

DEPARTMENT1=$(uuid)
DEPARTMENT2=$(uuid)
DEPARTMENT3=$(uuid)

create_entry $DEPARTMENT1
create_entry $DEPARTMENT1
create_entry $DEPARTMENT2
create_entry $DEPARTMENT2
create_entry $DEPARTMENT3
create_entry $DEPARTMENT3

