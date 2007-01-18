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

<%-- Shelf component --%>
<%-- IMPORTANT NOTE: All inner components must be given an explicit ID! --%>
<%--                 This is because they are wrapped in a Panel component --%>
<r:shelf id="shelf" groupPanel="lbgrey" groupBgcolor="white" selectedGroupPanel="lbgrey" selectedGroupBgcolor="white"
      groupExpandedActionListener="#{NavigationBean.shelfGroupToggled}">
   <r:shelfGroup label="#{msg.clipboard}" id="shelf-group-1" expanded="#{NavigationBean.shelfItemExpanded[0]}">
      <r:clipboardShelfItem id="clipboard-shelf" collections="#{ClipboardBean.items}" pasteActionListener="#{ClipboardBean.pasteItem}" />
   </r:shelfGroup>
   
   <%-- NOTE: this component is exanded=true as default so the RecentSpaces managed Bean is
              instantied early - otherwise it will not be seen until this shelf component is
              first expanded. There is no config setting to do this in JSF by default --%>
   <r:shelfGroup label="#{msg.recent_spaces}" id="shelf-group-2" expanded="#{NavigationBean.shelfItemExpanded[1]}">
      <r:recentSpacesShelfItem id="recent-shelf" value="#{RecentSpacesBean.recentSpaces}" navigateActionListener="#{RecentSpacesBean.navigate}" /> 
   </r:shelfGroup>
   
   <r:shelfGroup label="#{msg.shortcuts}" id="shelf-group-3" expanded="#{NavigationBean.shelfItemExpanded[2]}">
      <r:shortcutsShelfItem id="shortcut-shelf" value="#{UserShortcutsBean.shortcuts}" clickActionListener="#{UserShortcutsBean.click}" removeActionListener="#{UserShortcutsBean.removeShortcut}" />
   </r:shelfGroup>
</r:shelf>