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


This application was generated using JHipster 6.10.5 and JHipster Quarkus 1.1.1 (manually upgraded to quarkus 2.9.2.Final), you can find documentation and help at [https://www.jhipster.tech/documentation-archive/v6.10.5](https://www.jhipster.tech/documentation-archive/v6.10.5).


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
CREATE DATABASE gradeScopeIstic;
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
