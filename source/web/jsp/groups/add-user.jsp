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
--%><%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>
                           
<f:verbatim>
<table cellpadding="2" cellspacing="2" border="0" width="100%">
 <tr>
    <td class="mainSubTitle"></f:verbatim><h:outputText value="#{msg.select_users}" /><f:verbatim></td>
 </tr>
 <tr>
    <td></f:verbatim><a:genericPicker id="picker" showFilter="false" queryCallback="#{DialogManager.bean.pickerCallback}"
             actionListener="#{DialogManager.bean.addSelectedUsers}" /><f:verbatim></td>
 </tr>
 <tr><td class="paddingRow"></td></tr>
 <tr>
    <td class="mainSubTitle"></f:verbatim><h:outputText value="#{msg.selected_users}" /><f:verbatim></td>
 </tr>
 <tr>
    <td>
       </f:verbatim><h:dataTable value="#{DialogManager.bean.usersDataModel}" var="row" 
                    rowClasses="selectedItemsRow,selectedItemsRowAlt"
                    styleClass="selectedItems" headerClass="selectedItemsHeader"
                    cellspacing="0" cellpadding="4" 
                    rendered="#{DialogManager.bean.usersDataModel.rowCount != 0}">
          <h:column>
             <f:facet name="header">
                <h:outputText value="#{msg.name}" />
             </f:facet>
             <h:outputText value="#{row.name}" />
          </h:column>
          <h:column>
             <a:actionLink actionListener="#{DialogManager.bean.removeUserSelection}" image="/images/icons/delete.gif"
                           value="#{msg.remove}" showLink="false" style="padding-left:6px" />
          </h:column>
       </h:dataTable>
       
       <a:panel id="no-items" rendered="#{DialogManager.bean.usersDataModel.rowCount == 0}"><f:verbatim>
          <table cellspacing='0' cellpadding='2' border='0' class='selectedItems'>
             <tr>
                <td colspan='2' class='selectedItemsHeader'></f:verbatim><h:outputText id="no-items-name" value="#{msg.name}" /><f:verbatim></td>
             </tr>
             <tr>
                <td class='selectedItemsRow'></f:verbatim><h:outputText id="no-items-msg" value="#{msg.no_selected_items}" /><f:verbatim></td>
             </tr>
          </table>
       </f:verbatim></a:panel><f:verbatim>
    </td>
 </tr>
</table>
</f:verbatim>                           