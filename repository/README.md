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
