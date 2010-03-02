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
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<f:verbatim>
<table cellpadding="2" cellspacing="2" border="0" width="100%">
   <tr>
      <td colspan="2" class="paddingRow"></td></tr>
   <tr>
      <td></f:verbatim><h:outputText value="#{msg.categories}" /><f:verbatim>:</td>
      <td width="98%">
         </f:verbatim>
         <r:multiValueSelector id="multi-category-selector"
                               value="#{DialogManager.bean.categories}"
                               lastItemAdded="#{DialogManager.bean.addedCategory}"
                               selectItemMsg="#{msg.select_category}"
                               selectedItemsMsg="#{msg.selected_categories}"
                               noSelectedItemsMsg="#{msg.no_selected_categories}"
                               styleClass="multiValueSelector">
            <f:subview id="categorySelector">
               <r:ajaxCategorySelector id="catSelector" styleClass="selector"
                        value="#{DialogManager.bean.addedCategory}" 
                        label="#{msg.select_category_prompt}" />
            </f:subview>
         </r:multiValueSelector>
         <f:verbatim>
      </td>
   </tr>
   <tr><td colspan="2" class="paddingRow"></td></tr>
</table>
</f:verbatim>