# TODO

## Create new README for the combined project

Original README files:

### Alfresco Core
[![Build Status](https://travis-ci.com/Alfresco/alfresco-core.svg?branch=master)](https://travis-ci.com/Alfresco/alfresco-core)

Alfresco Core is a library packaged as a jar file which is part of [Alfresco Content Services Repository](https://community.alfresco.com/docs/DOC-6385-project-overview-repository).
The library contains the following:
* Various helpers and utils
* Canned queries interface and supporting classes
* Generic encryption supporting classes

Version 7 of the library uses Spring 5, Quartz 2.3 and does not have Hibernate dependency.

### Building and testing
The project can be built and tested by running Maven command:
~~~
mvn clean install
~~~

### Artifacts
The artifacts can be obtained by:
* downloading from [Alfresco repository](https://artifacts.alfresco.com/nexus/content/groups/public)
* getting as Maven dependency by adding the dependency to your pom file:
~~~
<dependency>
  <groupId>org.alfresco</groupId>
  <artifactId>alfresco-core</artifactId>
  <version>version</version>
</dependency>
~~~
and Alfresco repository:
~~~
<repository>
  <id>alfresco-maven-repo</id>
  <url>https://artifacts.alfresco.com/nexus/content/groups/public</url>
</repository>
~~~
The SNAPSHOT version of the artifact is **never** published.

### Old version history
The history for older versions can be found in [Alfresco SVN](https://svn.alfresco.com/repos/alfresco-open-mirror/services/alfresco-core/)

### Contributing guide
Please use [this guide](CONTRIBUTING.md) to make a contribution to the project.

### Alfresco Data Model
[![Build Status](https://travis-ci.com/Alfresco/alfresco-data-model.svg?branch=master)](https://travis-ci.com/Alfresco/alfresco-data-model)

Data model is a library packaged as a jar file which is part of [Alfresco Content Services Repository](https://community.alfresco.com/docs/DOC-6385-project-overview-repository).
The library contains the following:
* Dictionary, Repository and Search Services interfaces
* Models for data types and Dictionary implementation
* Parsers

Please note that the data model uses version 2 of the Jackson libraries. 
The upgrade from version 1 was not backward compatible, any projects
that are dependent on data model using Jackson 1.x should use the data-model 6.N branch. 

Version 8.0 of data-model depends on alfresco-core 7.0 which is based on Spring 5.
 

### Building and testing
The project can be built and tested by running Maven command:
~~~
mvn clean install
~~~

### Artifacts
The artifacts can be obtained by:
* downloading from [Alfresco repository](https://artifacts.alfresco.com/nexus/content/groups/public)
* getting as Maven dependency by adding the dependency to your pom file:
~~~
<dependency>
  <groupId>org.alfresco</groupId>
  <artifactId>alfresco-data-model</artifactId>
  <version>version</version>
</dependency>
~~~
and Alfresco repository:
~~~
<repository>
  <id>alfresco-maven-repo</id>
  <url>https://artifacts.alfresco.com/nexus/content/groups/public</url>
</repository>
~~~
The SNAPSHOT version of the artifact is **never** published.

### Old version history
The history for older versions can be found in [Alfresco SVN](https://svn.alfresco.com/repos/alfresco-open-mirror/alfresco/HEAD/root/projects/data-model)

### Contributing guide
Please use [this guide](CONTRIBUTING.md) to make a contribution to the project.

### Alfresco Repository
[![Build Status](https://travis-ci.com/Alfresco/alfresco-repository.svg?branch=master)](https://travis-ci.com/Alfresco/alfresco-repository)

Repository is a library packaged as a jar file which is part of [Alfresco Content Services Repository](https://community.alfresco.com/docs/DOC-6385-project-overview-repository).
The library contains the following:
* DAOs and SQL scripts
* Various Service implementations
* Utility classes

### Building and testing 
The project can be built by running Maven command:
~~~
mvn clean install
~~~
The tests are combined in test classes split by test type or Spring application context used in the test, see classes in _src/test/java/org/alfresco_. All of these classes as well as individual tests can be run by specifying the test class name and a set of DB connection properties, for example:
~~~
mvn clean test -Dtest=SomeRepoTest -Ddb.driver=org.postgresql.Driver -Ddb.name=alfresco -Ddb.url=jdbc:postgresql:alfresco -Ddb.username=alfresco -Ddb.password=alfresco
~~~

### Artifacts
The artifacts can be obtained by:
* downloading from [Alfresco repository](https://artifacts.alfresco.com/nexus/content/groups/public)
* getting as Maven dependency by adding the dependency to your pom file:
~~~
<dependency>
  <groupId>org.alfresco</groupId>
  <artifactId>alfresco-repository</artifactId>
  <version>version</version>
</dependency>
~~~
and Alfresco Maven repository:
~~~
<repository>
  <id>alfresco-maven-repo</id>
  <url>https://artifacts.alfresco.com/nexus/content/groups/public</url>
</repository>
~~~
The SNAPSHOT version of the artifact is **never** published.

### Contributing guide
Please use [this guide](CONTRIBUTING.md) to make a contribution to the project.

### Alfresco Remote API
[![Build Status](https://travis-ci.com/Alfresco/alfresco-remote-api.svg?branch=master)](https://travis-ci.com/Alfresco/alfresco-remote-api)

Remote API is a library packaged as a jar file which is part of [Alfresco Content Services Repository](https://community.alfresco.com/docs/DOC-6385-project-overview-repository).
The library contains the following:
* REST API framework
* WebScript implementations including [V1 REST APIs](https://community.alfresco.com/community/ecm/blog/2017/05/02/v1-rest-api-10-things-you-should-know)
* [OpenCMIS](https://chemistry.apache.org/java/opencmis.html) implementations

### Building and testing
The project can be built by running Maven command:
~~~
mvn clean install
~~~
The tests are combined in test classes split by test type or Spring application context used in the test, see classes in _src/test/java/org/alfresco_. All of these classes as well as individual tests can be run by specifying the test class name and a set of DB connection properties, for example:
~~~
mvn clean test -Dtest=SomeTest -Ddb.driver=org.postgresql.Driver -Ddb.name=alfresco -Ddb.url=jdbc:postgresql:alfresco -Ddb.username=alfresco -Ddb.password=alfresco
~~~

### Artifacts
The artifacts can be obtained by:
* downloading from [Alfresco repository](https://artifacts.alfresco.com/nexus/content/groups/public)
* getting as Maven dependency by adding the dependency to your pom file:
~~~
<dependency>
  <groupId>org.alfresco</groupId>
  <artifactId>alfresco-remote-api</artifactId>
  <version>version</version>
</dependency>
~~~
and Alfresco Maven repository:
~~~
<repository>
  <id>alfresco-maven-repo</id>
  <url>https://artifacts.alfresco.com/nexus/content/groups/public</url>
</repository>
~~~
The SNAPSHOT version of the artifact is **never** published.

### Contributing guide
Please use [this guide](CONTRIBUTING.md) to make a contribution to the project.
