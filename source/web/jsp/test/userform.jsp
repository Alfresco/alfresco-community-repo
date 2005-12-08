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
<%-- JSP fragment - included directly by other complete JSP pages --%>
<table border=0 cellspacing=2 cellpadding=2>

   <tr>
      <td>
         <%-- use an I18N message --%>
         <h:outputText value="#{msg.username}"/>:
      </td>
      <td>
         <%-- example of the 'disabled' attribute bound to a form bean property --%>
         <h:inputText id="username" value="#{UserListBean.user.username}" required="true" disabled="#{!UserListBean.isNewUser}">
            <f:validateLength minimum="5" maximum="12" />
         </h:inputText>
         <%--  message tag to show errors for the 'username' field --%>
         <h:message id="errors1" for="username" style="color:red; font-size:10px" />
      </td>
   </tr>
   
   <tr>
      <td>
         <h:outputText value="#{msg.password}"/>:
      </td>
      <td>
         <h:inputText id="userpassword" value="#{UserListBean.user.password}" validator="#{LoginBean.validatePassword}" required="true">
            <f:validateLength minimum="5" maximum="12" />
         </h:inputText>
         <h:message id="errors2" for="userpassword" style="color:red; font-size:10px" />
      </td>
   </tr>

   <tr>
      <td>
         <h:outputText value="#{msg.name}"/>:
      </td>
      <td>
         <h:inputText id="name" value="#{UserListBean.user.name}" required="true" />
         <h:message id="errors3" for="name" style="color:red; font-size:10px" />
      </td>
   </tr>
   
   <tr>
      <td>
         <h:outputText value="#{msg.joindate}"/>:
      </td>
      <td>
         <%-- Example of a tag utilising an Input Component with a custom renderer.
              The renderer handles encoding and decoding of date values to UI elements --%>
         <a:inputDatePicker id="joined" value="#{UserListBean.user.dateJoined}" startYear="1996" yearCount="10"/>
      </td>
   </tr>
   
   <tr>
      <td>
         <h:outputText value="#{msg.roles}"/>:
      </td>
      <td>
         <%-- Show an example of a listbox populated by a server-side List of SelectItem
              objects. The preselection is controlled by the selectItems tag which contains
              a list of values that the control will try to select.
              Also shows an example of a valueChangedListener which uses onchange() Javascript
              to submit the form immediately to update the UI. --%>
         <h:selectManyListbox id="rolesListbox" value="#{UserListBean.user.roles}" valueChangeListener="#{UserListBean.roleValueChanged}" immediate="true" onchange="javascript:document.forms['userForm'].submit();">
            <f:selectItems value="#{UserListBean.user.allRolesList}" />
         </h:selectManyListbox>
         <h:message id="errors5" for="rolesListbox" style="color:red; font-size:10px" />
         <br>
         <%-- Example of a direct component binding. The setting method on the bean 
              is called and the OutputText component can be programmically modified --%>
         <h:outputText id='roles-text' binding="#{UserListBean.rolesOutputText}"/>
      </td>
   </tr>

</table>
