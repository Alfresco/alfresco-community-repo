<%--
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
--%>
<%-- Title bar area --%>
<table cellspacing="0" cellpadding="2" width="100%">
   <tr>
      <%-- Top level toolbar and company logo area --%>
      <td style="width:100%;">
         <table cellspacing="0" cellpadding="0" width="100%">
            <tr>
               <td style="padding-right:4px;"><a:actionLink id="about_alfresco" image="/images/logo/AlfrescoLogo32.png" value="#{msg.title_about}" tooltip="#{msg.title_about}" showLink="false" action="dialog:aboutDialog" /></td>
               <td><img src="<%=request.getContextPath()%>/images/parts/titlebar_begin.gif" width="10" height="31" alt=""/></td>
               <td style="width: 100%; background-image: url(<%=request.getContextPath()%>/images/parts/titlebar_bg.gif); background-repeat:repeat-x">
                  <%-- Toolbar --%>
                  <a:modeList itemSpacing="3"
                              iconColumnWidth="0" 
                              horizontal="true"
                              itemStyleClass="topToolbar" 
                              itemLinkStyleClass="topToolbarLink" 
                              selectedStyleClass="topToolbar" 
                              selectedLinkStyleClass="topToolbarLink"
                              value="#{NavigationBean.toolbarLocation}" 
                              actionListener="#{NavigationBean.toolbarLocationChanged}">
                     <a:listItem value="companyhome" label="#{msg.company_home}" rendered="#{NavigationBean.companyHomeVisible}" />
                     <a:listItem value="userhome" label="#{msg.my_home}" />
                     <a:listItem value="guesthome" 
                                 label="#{msg.guest_home}"
                                 rendered="#{!NavigationBean.isGuest && NavigationBean.guestHomeVisible}" />
                     <a:listItem value="myalfresco" label="#{msg.my_alfresco}" />
                  </a:modeList>
               </td>
               <td><img src="<%=request.getContextPath()%>/images/parts/titlebar_end.gif" width="8" height="31" alt=""/></td>
            </tr>
         </table>
      </td>
      
      <%-- Help area --%>
      <td>
         <table cellspacing="2" cellpadding="0" width="100%">
            <tr>
               <td>
                  <%-- admin user only actions --%>
                  <a:booleanEvaluator value="#{NavigationBean.currentUser.admin}" id="evalA">
                     <a:actionLink value="#{msg.admin_console}" 
                                   image="/images/icons/admin_console.gif"
                                   showLink="false" 
                                   action="dialog:adminConsole" 
                                   id="alf_admin_console" />
                  </a:booleanEvaluator>
               </td>
               <td style="width:12px;">&nbsp;</td>
               <td>
                  <%-- user preferences --%>
                  <a:actionLink value="#{msg.user_console}" 
                                image="/images/icons/user_console.gif" 
                                showLink="false" 
                                action="dialog:userConsole" 
                                actionListener="#{UsersDialog.setupUserAction}" 
                                id="alf_user_console">
                     <f:param name="id" value="#{NavigationBean.currentUser.person.id}" />
                  </a:actionLink>
               </td>
               <td style="width:8px;">&nbsp;</td>
               <td>
                 <a:actionLink id="alf_toggle_shelf" 
                               value="#{msg.toggle_shelf}" 
                               image="/images/icons/shelf.gif" 
                               showLink="false" 
                               actionListener="#{NavigationBean.toggleShelf}" />
               </td>
               <td style="width:8px;">&nbsp;</td>
               <td>
                 <a:actionLink id="alf_help" 
                               value="#{msg.help}" 
                               image="/images/icons/Help_icon.gif" 
                               showLink="false" 
                               href="#{NavigationBean.helpUrl}" 
                               target="help" />
               </td>
               <td style="width:8px;">&nbsp;</td>
               <td style="white-space:nowrap;"><a href="http://www.alfresco.com/services/support/issues/" target="new"><h:outputText value="#{msg.raise_issue}" /></a></td>
               <td style="width:8px;">&nbsp;</td>
               <td style="white-space:nowrap;">
                  <a:actionLink id="logout" image="/images/icons/logout.gif" value="#{msg.logout} (#{NavigationBean.currentUser.userName})" rendered="#{!NavigationBean.isGuest}" action="#{LoginBean.logout}" immediate="true">
                     <f:param name="outcome" value="#{LoginBean.logoutOutcome}" />
                  </a:actionLink>
                  <a:actionLink id="login" image="/images/icons/login.gif" value="#{msg.login} (#{NavigationBean.currentUser.userName})" rendered="#{NavigationBean.isGuest}" action="#{LoginBean.logout}" />
               </td>
            </tr>
         </table>
      </td>
      
      <%-- Search area --%>
      <td>
         <table cellspacing="0" cellpadding="0" width="100%">
            <tr>
               <td><img src="<%=request.getContextPath()%>/images/parts/searchbar_begin.gif" width="6" height="31" alt=""/></td>
               <td style="width:100%; background-image: url(<%=request.getContextPath()%>/images/parts/searchbar_bg.gif);">
                  <r:simpleSearch id="search" actionListener="#{BrowseBean.search}" />
               </td>
               <td><img src="<%=request.getContextPath()%>/images/parts/searchbar_end.gif" width="6" height="31" alt=""/></td>
            </tr>
         </table>
      </td>
   </tr>
</table>
