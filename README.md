### Alfresco Data Model
Data model is a library packaged as a jar file which is part of [Alfresco Content Services Repository](https://community.alfresco.com/docs/DOC-6385-project-overview-repository).
The library contains the following:
* Dictionary, Repository and Search Services interfaces
* Models for data types and Dictionary implementation
* Parsers

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