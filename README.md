# service-curriculum
This Java service approves or rejects applications after checking the studentlimit for that lesson

## Getting Started

This repository contains example project for using AWS kinesis with Java Spring Boot and message-driven architecture concept. I used PostgreSQL as a RDBMS.

### Prerequisites

You need to install Postgres in localhost and create a db which name is deneme_db. You will see the configuration parameters in the resources/application.properties file

```
spring.datasource.url=jdbc:postgresql://localhost/deneme_db
spring.datasource.username=ali_user
spring.datasource.password=123
spring.jpa.generate-ddl=true
```

Create lesson table and add some sample data.
```
﻿CREATE TABLE public.lesson
(
    id smallint NOT NULL,
    name character varying(255) COLLATE pg_catalog."default" NOT NULL,
    studentlimit smallint NOT NULL,
    CONSTRAINT class_pkey PRIMARY KEY (id)
);

insert into lesson values (1,'Mathematics',20);
insert into lesson values (2,'Physics',20);
insert into lesson values (3,'Biology',20);
insert into lesson values (4,'Calculus',20);
insert into lesson values (5,'Differential Equations',20);
insert into lesson values (6,'Chemistry',20);
insert into lesson values (7,'Geology',20);
```

Also You need to have an AWS account to create a aws kinesis stream and you will see the configuration parameters in the resources/application.properties file

```
kinesis.region=us-east-1
kinesis.streamName=stream-application
aws.accessKey=****
aws.secretKey=****
aws.appName=APPLICATION_STATUS_APP
```

### Summary

You need to create an application by using service-application which is also in this account. Open a postman and send a post request by using this json body. By doing this, you will make a request to apply a lesson. This lesson status will be "NEW" and service-curriculum will change its status to "APPROVED" or "REJECTED".

This code gets records from kinesis stream and filters messages (EVT_APPLICATION_CREATED). After filtering messages, updateCurriculum checks the limits of applicated lesson.
```
@Override
    public void processRecords(ProcessRecordsInput processRecordsInput) {
        List<Record> records = processRecordsInput.getRecords();

        records.stream()
            .map(r -> MessageManager.parse(r.getData().array()))
            .filter(m -> m.getName().equals(MessageType.EVT_APPLICATION_CREATED.name()))
            .map(m -> MatchApplicationData.fromJson(m.getPayload()))
            .forEach(data -> {
                //Updates curriculum and emit APPROVED/REJECTED messages
                curriculumDataService.updateCurriculum(data.getId(), data.getClassId());
            });

        updateCheckpoint(processRecordsInput);
    }
```

This code checks the limit. If there is a quota for this lesson, services will decrease the limit by 1 and emit a EVT_APPLICATION_APPROVED message.
Otherwise service emits EVT_APPLICATION_REJECTED message. These message consumes by service-application to update its state (it starts with "NEW")
```
@Override
    public void updateCurriculum(Long applicationId, Integer classId) {
        lesson = curriculumRepository.findOne(classId);

        if (lesson.getStudentlimit() > 0) {
            //there is enough space for this application, decrease the limit
            lesson.setStudentlimit(lesson.getStudentlimit() - 1);

            //decrease from postgres
            curriculumRepository.save(lesson);

            //produce EVT_APPLICATION_APPROVED event
            eventProducer.emit(Long.toString(classId), MessageManager.create(MessageType.EVT_APPLICATION_APPROVED, new ApplicationStatusUpdateEvent(applicationId, "APPROVED")).toByteArray());
        } else {
            //the lesson limit is full ! produce EVT_APPLICATION_REJECTED event
            eventProducer.emit(Long.toString(classId), MessageManager.create(MessageType.EVT_APPLICATION_REJECTED, new ApplicationStatusUpdateEvent(applicationId, "REJECTED")).toByteArray());
        }


    }
```
There is an article about Event-driven Data Management in Microservices in the Cimri tech-blog. You can find both service-application and service-curriculum in that article.
