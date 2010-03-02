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
<table cellpadding="2" cellspacing="2" border="0">
<tr>
<td class="mainSubTitle">
</f:verbatim><h:outputText value="#{msg.delete_listed_items_confirm}" />
<f:verbatim>
</td>
</tr>
<tr>
<td>
</f:verbatim><h:outputText value="#{TrashcanDeleteListedItemsDialog.listedItemsTable}" escape="false" />
<f:verbatim>
</td>
</tr>
<tr>
<td>
<%-- Error Messages --%>
<%-- messages tag to show messages not handled by other specific message tags --%>
</f:verbatim><h:messages globalOnly="true" styleClass="errorMessage" layout="table" />
<f:verbatim>
<td>
</tr>
</table>
</f:verbatim>