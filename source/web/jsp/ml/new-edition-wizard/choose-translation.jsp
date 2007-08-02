<%--
 * Copyright (C) 2005-2007 Alfresco Software Limited.

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

<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<h:panelGroup rendered="#{WizardManager.bean.hasTranslationCheckedOut == true}">
   <f:verbatim>
      <%PanelGenerator.generatePanelStart(out, request.getContextPath(), "yellowInner", "#ffffcc");%>
      <table><tr><td>
   </f:verbatim>
   <h:graphicImage url="/images/icons/info_icon.gif" />
   <f:verbatim>
      </td><td>
   </f:verbatim>
   <h:outputText value="#{msg.translations_checked_out_error}" />
   <f:verbatim>
      </td></tr></table>
      <%PanelGenerator.generatePanelEnd(out, request.getContextPath(), "yellowInner");%>
   </f:verbatim>
</h:panelGroup>

<h:panelGrid rendered="#{WizardManager.bean.hasTranslationCheckedOut == true}" style="padding-top:7px;">
   <div style="padding:4px"/>

   <h:dataTable value="#{WizardManager.bean.translationsCheckedOutDataModel}" var="row"
                rowClasses="selectedItemsRow,selectedItemsRowAlt"
                styleClass="selectedItems" headerClass="selectedItemsHeader"
                cellspacing="0" cellpadding="4">
      <h:column>
         <f:facet name="header">
            <h:outputText value="#{msg.language}" />
         </f:facet>
         <h:outputText value="(#{row.language})" />
      </h:column>
      <h:column>
         <f:facet name="header">
            <h:outputText value="#{msg.doc_name}" />
         </f:facet>
         <h:outputText value="#{row.name}" />
      </h:column>
      <h:column>
         <f:facet name="header">
            <h:outputText value="#{msg.checked_out_by}" />
         </f:facet>
         <h:outputText value="#{row.checkedOutBy}" />
      </h:column>
   </h:dataTable>
</h:panelGrid>

<h:panelGrid columns="1" rendered="#{WizardManager.bean.hasTranslationCheckedOut == false}">

	<h:dataTable value="#{WizardManager.bean.availableTranslationsDataModel}" var="row"
                rowClasses="selectedItemsRow,selectedItemsRowAlt"
                styleClass="selectedItems" headerClass="selectedItemsHeader"
                cellspacing="0" cellpadding="4">

      <h:column>
         <f:facet name="header">
			<h:outputText value=" " />
	     </f:facet>
		<h:selectOneRadio value="#{WizardManager.bean.selectedTranslationLanguage}"  onchange="dataTableSelectOneRadio(this);">
			<f:selectItem itemValue="#{row.language}" itemLabel=""/>
		</h:selectOneRadio>
	  </h:column>

      <h:column>
         <f:facet name="header">
            <h:outputText value="#{msg.doc_name}" />
         </f:facet>
         <h:outputText value="#{row.name}" />
      </h:column>
      <h:column>
         <f:facet name="header">
            <h:outputText value="#{msg.language}" />
         </f:facet>
         <h:outputText value="#{row.languageLabel}" />
      </h:column>
   </h:dataTable>
</h:panelGrid>


<script type="text/javascript">

    function dataTableSelectOneRadio(radio)
    {
        var id = radio.name.substring(radio.name.lastIndexOf(':'));
        var el = radio.form.elements;
        for (var i = 0; i < el.length; i++) {
            if (el[i].name.substring(el[i].name.lastIndexOf(':')) == id) {
                el[i].checked = false;
            }
        }
        radio.checked = true;
    }
</script>