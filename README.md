### DATA MODEL
Data model is a library packaged as a jar file, which contains the following:
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

### License
This project is part of the Alfresco software. 
If the software was purchased under a paid Alfresco license, the terms of the paid license agreement will prevail.  Otherwise, the software is provided under the following open source license terms:
 
Alfresco is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 
Alfresco is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 
You should have received a copy of the GNU Lesser General Public License along with Alfresco. If not, see <http://www.gnu.org/licenses/>.