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

<h:form id="node-browser-titlebar">

   <table width="100%">
      <tr>
         <td>
            <h:graphicImage value="/images/logo/AlfrescoLogo32.png" alt="Alfresco" />
         </td>
         <td>
            <nobr><h:outputText styleClass="mainTitle" value="#{msg.title_admin_node_browser}"/></nobr>
         </td>
         <td width="100%" align="right">
            <h:commandButton value="#{msg.close}" action="adminConsole" />
         </td>
      </tr>
   </table>

</h:form>

<br>
