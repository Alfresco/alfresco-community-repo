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
<%-- Shelf area --%>
<a:panel id="shelfPanel" expanded="#{NavigationBean.shelfExpanded}">
   
   <table cellspacing=0 cellpadding=0 width=100% bgcolor='#ffffff'>
      <tr>
         <td><img src="<%=request.getContextPath()%>/images/parts/headbar_begin.gif" width=4 height=33></td>
         <td align=center width=100% style="background-image: url(<%=request.getContextPath()%>/images/parts/headbar_bg.gif)">
            <div class="headbarTitle"><h:outputText id="shelfText" value="#{msg.shelf}"/></div>
         </td>
         <td><img src="<%=request.getContextPath()%>/images/parts/headbar_end.gif" width=4 height=33></td>
      </tr>
      <tr>
         <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width=4></td>
         <td valign=top width=100%>
            
            <%-- Shelf component --%>
            <%-- IMPORTANT NOTE: All inner components must be given an explicit ID! --%>
            <%--                 This is because they are wrapped in a Panel component --%>
            <r:shelf id="shelf" groupPanel="ballongrey" groupBgcolor="#eeeeee" selectedGroupPanel="bluetoolbar" selectedGroupBgcolor="#e9f0f4"
                  innerGroupPanel="white" innerGroupBgcolor="#ffffff" groupExpandedActionListener="#{NavigationBean.shelfGroupToggled}">
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
               
               <%-- TBD
               <r:shelfGroup label="Drop Zone" id="shelf-group-4" expanded="#{NavigationBean.shelfItemExpanded[3]}">
                  
               </r:shelfGroup>
               
               <r:shelfGroup label="Actions in Progress" id="shelf-group-5" expanded="#{NavigationBean.shelfItemExpanded[4]}">
                  
               </r:shelfGroup>
               --%>
            </r:shelf>
            
         </td>
         <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width=4></td>
      </tr>
      <tr>
         <td><img src="<%=request.getContextPath()%>/images/parts/whitepanel_7.gif" width=4 height=4></td>
         <td width=100% align=center style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_8.gif)"></td>
         <td><img src="<%=request.getContextPath()%>/images/parts/whitepanel_9.gif" width=4 height=4></td>
      </tr>
   </table>
   
</a:panel>
