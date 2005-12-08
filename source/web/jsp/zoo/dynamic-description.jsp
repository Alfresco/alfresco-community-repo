<%--
  Copyright (C) 2005 Alfresco, Inc.
 
  Licensed under the Mozilla Public License version 1.1 
  with a permitted attribution clause. You may obtain a
  copy of the License at
 
    http://www.alfresco.org/legal/license.txt
 
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  either express or implied. See the License for the specific
  language governing permissions and limitations under the
  License.
--%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>

<r:page>

<f:view>

   <h2>Dynamic Description</h2>
   
   <h:form acceptCharset="UTF-8" id="dyna-desc">

      <h:selectOneMenu onchange="javascript:listItemSelected(this);">
         <f:selectItem itemLabel="Choice One" itemValue="one" />
         <f:selectItem itemLabel="Choice Two" itemValue="two" />
         <f:selectItem itemLabel="Choice Three" itemValue="three" />
         <f:selectItem itemLabel="Choice Four" itemValue="four" />
      </h:selectOneMenu>
      
      <br/><br/>
      <a:dynamicDescription selected="one" functionName="listItemSelected">
         <a:descriptions value="#{DummyBean.properties}" />
      </a:dynamicDescription>
      
      <br/><br/>
      <a:imagePickerRadio columns="4" spacing="4" value="container"
                            onclick="javascript:itemSelected(this);">
         <a:listItem value="container" label="Container" tooltip="Container"
                           image="/images/icons/space.gif" />
         <a:listItem value="wiki" label="Wiki" tooltip="Wiki"
                           image="/images/icons/wiki.gif" />
         <a:listItem value="discussion" label="Discussion" tooltip="Discussion"
                           image="/images/icons/discussion.gif" />
      </a:imagePickerRadio>
      
      <br/><br/>
      <a:dynamicDescription selected="container">
         <a:description controlValue="container" text="Container" />
         <a:description controlValue="wiki" text="Wiki" />
         <a:description controlValue="discussion" text="Discussion" />
      </a:dynamicDescription>
      
      <br/><br/>
      <h:commandButton id="show-zoo-page" value="Show Zoo" action="showZoo" />

   </h:form>

</f:view>

</r:page>