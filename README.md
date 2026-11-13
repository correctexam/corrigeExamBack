# CorrectExam back-end

The back-end of the app CorrectExam.

## CorrectExam

**Deliver and Grade Your Assessments Anywhere**

This application helps you seamlessly administer and grade all of your in-class assessments (long exam or short). Save time grading and get a clear picture of how your students are doing. Provide a clear feedback to your students.

### Feature

Main feature are:

- automatically align your scan
- automatically recognize your student name and firstname on sheet
- identify zones for question to support horizontal or vertical correction
- create generic comment per question with associated bonus or malus to efficiently correct each question
- use your tablet and your pen to annotate student sheet
- provide clear feedback to your students

### Technical stack


- Java 17+ (enforced range: 17 to 25), Maven wrapper (`./mvnw`)
- Quarkus 3.40 (see `quarkus.platform.version` in `pom.xml`): REST + Jackson, Hibernate ORM with Panache, MapStruct, Liquibase, SmallRye JWT, Micrometer, OpenAPI
- MySQL / MariaDB in dev and prod, H2 for tests and the standalone build
- MinIO (S3 API) for PDFs, scans and caches (`correctexam.uses3`, `correctexam.bucketname`)
- RabbitMQ (`quarkus-messaging-rabbitmq`) to send server-side scan alignment jobs (`scan_jobs_queue`) to the `pdf_to_webp` worker
- Authentication: JWT, CAS and Shibboleth
- Front-end: [corrigeExamFront](https://github.com/correctexam/corrigeExamFront) (Angular)

The project was initially generated with JHipster 6.10.5 and JHipster Quarkus 1.1.1, then upgraded manually; the JHipster links below are kept for reference ([documentation archive](https://www.jhipster.tech/documentation-archive/v6.10.5)).


### Architecture decisions

Architecture decisions are recorded in [`docs/adr`](docs/adr/README.md), e.g. [ADR 0001 — scan alignment in the browser or in the Rust worker](docs/adr/0001-alignement-des-scans-client-ou-serveur.md).

### Build and deploy

Documentation to deploy your own instance is available [here](https://correctexam.readthedocs.io/en/latest/Install.html)



## Development

### Install

The back-end requires Minio to store the PDF files.
An easy way to install and launch Minio on your computer is to use an official Docker image:

```
podman run -p 9000:9000 -d -p 9090:9090 -e "MINIO_ROOT_USER=admin" -e "MINIO_ROOT_PASSWORD=minioadmin" -v /media/data/miniofiles:/data:Z quay.io/minio/minio server /data --console-address ":9090"
```

The back-end also requires a database.
On Linux you can install MariaDB.
On Fedora, you can run:

```
sudo dnf install mariadb-server
sudo systemctl enable mariadb
sudo systemctl start mariadb
sudo mysql_secure_installation

sudo mysql -u root
CREATE DATABASE correctexam;
```

The `dev` profile expects the database `correctexam` on `localhost:3308` (user `root`, see `%dev.quarkus.datasource.*` in `src/main/resources/application.properties`); the `prod` profile uses `gradeScopeIstic` on port 3306. Adapt the port or these properties to your installation.

Server-side scan alignment also requires RabbitMQ (`rabbitmq-host`, `rabbitmq-port` properties):

```
podman run -d -p 5672:5672 -p 15672:15672 -e RABBITMQ_DEFAULT_USER=rabbitmq -e RABBITMQ_DEFAULT_PASS=rabbitmq rabbitmq:management
```

The first run of the back-end must initial and populate the DB with fake data. To do so, run:
```
./mvnw quarkus:dev -Dquarkus.liquibase.migrate-at-start=true
```

It creates two users `user:user` and `admin:admin`.
After that you can launch the back-end classically `./mvnw`


### Run in development mode

To start your application in the dev profile, run:

```
./mvnw
```

For further instructions on how to develop with JHipster, have a look at [Using JHipster in development][].

### Building for production

#### Packaging as thin jar

To build the final jar and optimize the correctExam application for production, run:

```
./mvnw -Pprod clean package
```

To ensure everything worked, run:

    java -jar target/quarkus-app/*.jar

**Before any deployment reachable from a network**, generate a JWT key pair of your own and pass it with
`JWT_PRIVATE_KEY_LOCATION` / `JWT_PUBLIC_KEY_LOCATION`: the application refuses to start with the development
pair of the repository. See [docs/security/jwt-keys.md](docs/security/jwt-keys.md).

Refer to [Using JHipster in production][] for more details.

#### Packaging as native executable

_Targeting your Operation System_
In order to build a native image locally, your need to have [GraalVM](https://www.graalvm.org/) installed and `GRAALVM_HOME` defined.
You can use the `native` profile as follow to build native executable.

```
./mvnw package -Pnative
```

Keep in mind that the generated native executable is dependent on your Operating System.

_Targeting a container environment_
If you plan to run your application in a container, run:

```
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

It will use a Docker container with GraalVM installed and produce an 64 bit Linux executable.

### Testing

To launch your application's tests, run:

    ./mvnw verify

For more information, refer to the [Running tests page][].

### Using Docker to simplify development (optional)

You can use Docker to improve your JHipster development experience. A number of docker-compose configuration are available in the [src/main/docker](src/main/docker) folder to launch required third party services.

For example, to start a mysql database in a docker container, run:

    docker-compose -f src/main/docker/mysql.yml up -d

To stop it and remove the container, run:

    docker-compose -f src/main/docker/mysql.yml down

[jhipster homepage and latest documentation]: https://www.jhipster.tech
[jhipster 6.10.5 archive]: https://www.jhipster.tech/documentation-archive/v6.10.5
[using jhipster in development]: https://www.jhipster.tech/documentation-archive/v6.10.5/development/
[using docker and docker-compose]: https://www.jhipster.tech/documentation-archive/v6.10.5/docker-compose
[using jhipster in production]: https://www.jhipster.tech/documentation-archive/v6.10.5/production/
[running tests page]: https://www.jhipster.tech/documentation-archive/v6.10.5/running-tests/
[code quality page]: https://www.jhipster.tech/documentation-archive/v6.10.5/code-quality/
[setting up continuous integration]: https://www.jhipster.tech/documentation-archive/v6.10.5/setting-up-ci/



-- upgrade database script


create table prediction (id bigint not null auto_increment, json_data tinytext, confidence float(53), question_number varchar(255), text varchar(2048), question_id bigint, sheet_id bigint, primary key (id));
alter table template add column casename bit DEFAULT 1;
alter table course add column archived bit not null DEFAULT 0;


alter table prediction add constraint FK1xsmwx00gk7213kwfeah9lcjx foreign key (question_id) references question (id);
alter table prediction add constraint FK8nv2hkm3mhxll6be9mwj5402t foreign key (sheet_id) references exam_sheet (id);
 

./mvnw liquibase:generateChangeLog -Dliquibase.outputChangeLogFile=/tmp/standalone.xml


1.7
alter table exam add column nbgrader bit  not null DEFAULT 0;

-- T18: number of pages of a scan (see docs/db/T18-scan-page-count.sql)
ALTER TABLE scan ADD COLUMN page_count INT NULL, ALGORITHM=INSTANT;
