# Alfresco Records Management Community REST API Explorer
Records Management Community REST API Definition Explorer


#### Building and deploying the war
- `mvn clean install`

You now have a `target/alfresco-rm-community-rest-api-explorer-2.6-SNAPSHOT.war`, drop this into your Application server that is running alfresco.war


#### For development only
You can run the project as a packaged web application using an embedded Tomcat server.
This is useful for changing documentation and endpoint descriptions but it means that the "Try it Out!" button will not work.

- ` mvn clean install -Pstart-api-explorer`

Now the application is running at [http://localhost:8080/api-explorer](http://localhost:8080/api-explorer/)

#### Config option
You can run tomcat on another port using the following command

- ` mvn clean install -Pstart-api-explorer -Dmaven.tomcat.port=8085`

Then the application will run at [http://localhost:8085/api-explorer](http://localhost:8085/api-explorer/)

### License
Copyright (C) 2016 Alfresco Software Limited

This file is part of an unsupported extension to Alfresco.

Alfresco Software Limited licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.