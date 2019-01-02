### Budget management service

A budget entry consists of Department UUID, Project UUID, allocation term (month count), allocation amount.

A user can create, update, read and delete entries.

This project contains only backends. It assumed that there is the frontend which uses rest communication.

Departments and projects are beyond this service context.


### How to run
Easiest way to run a project includes using docker: 

1. Run Postgres: `docker run --net=host --name budget_db -d postgres:10`
1. Create DB: `docker exec -it budget_db psql -Upostgres -c 'create database budget'`
1. Populate DB:
```
git checkout 4daacd061da370e6a280fff1e3744f0582a483cb
sbt runAll # in a second console
./curl/populate.sh
# stop sbt
```

There are two ways of new field addition:
1. Add optional field
2. Add mandatory field + default values for old data.

#### Optional fields
```
git checkout master
sbt runAll
curl http://localhost:9000/budget # take first entry id. In my case: c73ec3b9-5f71-4e17-bb5e-6fef68ec0e5f
./curl/get-entry.sh c73ec3b9-5f71-4e17-bb5e-6fef68ec0e5f # there is no additional fields
./curl/update-entry.sh c73ec3b9-5f71-4e17-bb5e-6fef68ec0e5f
./curl/get-entry.sh c73ec3b9-5f71-4e17-bb5e-6fef68ec0e5f # additional fields should appears
```

#### Mandatory fields
```
git checkout with-default-values
sbt runAll
curl http://localhost:9000/budget # every record should have the additional fields
```

