<%--
  Copyright (C) 2005 Alfresco, Inc.
 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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