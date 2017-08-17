### Alfresco Community Distribution
This project is producing packaging for [Alfresco Content Services Repository](https://community.alfresco.com/docs/DOC-6385-project-overview-repository).
The artifacts created are:
* A _war_ file 
* A distribution zip archive containing the _war_ file and configuration required for deployment to a Web Server.
* SDK dependencies pom file, used by [Alfresco SDK](https://github.com/Alfresco/alfresco-sdk).

### Building
The project can be built by running Maven command:
~~~
mvn clean install
~~~

### Artifacts
The artifacts can be obtained by:
* downloading from [Alfresco repository](https://artifacts.alfresco.com/nexus/content/groups/public)
* getting as Maven dependency by adding the dependency to your pom file (depending on the artifact required):
~~~
<dependency>
  <groupId>org.alfresco</groupId>
  <artifactId>artefactId</artifactId>
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
The SNAPSHOT version of the artifacts is **never** published.

### Contributing guide
Please use [this guide](CONTRIBUTING.md) to make a contribution to the project.