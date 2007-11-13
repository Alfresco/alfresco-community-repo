<!--
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
    * As a special exception to the terms and conditions of version 2.0 of
    * the GPL, you may redistribute this Program in connection with Free/Libre
    * and Open Source Software ("FLOSS") applications as described in Alfresco's
    * FLOSS exception.  You should have recieved a copy of the text describing
    * the FLOSS exception, and it is also available here:
    * http://www.alfresco.com/legal/licensing
  -->
<jsp:root version="1.2"
          xmlns:jsp="http://java.sun.com/JSP/Page"
 	  xmlns:c="http://java.sun.com/jsp/jstl/core"
          xmlns:fmt="http://java.sun.com/jsp/jstl/fmt"
          xmlns:a="urn:jsptld:/WEB-INF/alfresco.tld"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html">

  <jsp:output doctype-root-element="html"
	      doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
	      doctype-system="http://www.w3c.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>

  <jsp:directive.page language="java" buffer="32kb" contentType="text/html; charset=UTF-8"/>
  <jsp:directive.page isELIgnored="false"/>

  <h:panelGrid columns="1" cellpadding="2" style="padding-top: 4px; padding-bottom: 4px;"
               width="100%">
    <h:outputText id="step-1-text" 
		  value="1. #{msg.regenerate_renditions_select_renditions_select_web_project}" 
		  escape="false" />
    <h:panelGrid id="panel_grid_3"
                 columns="3" cellpadding="3" cellspacing="3" border="0" width="100%"
	         columnClasses="panelGridRequiredImageColumn,panelGridLabelColumn,panelGridValueColumn">

      <h:graphicImage id="img_web_project"
                      value="/images/icons/required_field.gif" 
                      alt="#{msg.required_field}" />
      <h:outputText id="output_text_web_project"
                    value="#{msg.web_project}:"/>
      <h:selectOneMenu id="selectone-webproject" 
		       style="width:100%;"
                       onchange="this.form.submit()"
                       value="#{WizardManager.bean.selectedWebProject}">
        <f:selectItems id="selectitems-webproject"
		       value="#{WizardManager.bean.webProjectChoices}"/>
      </h:selectOneMenu>
    </h:panelGrid>

    <h:outputText id="step-2-text" 
		  value="2. #{msg.regenerate_renditions_select_renditions_select_regenerate_scope}" 
		  escape="false" />
    <h:selectOneRadio id="selectone-regenerate-mode"
                      onclick="this.form.submit()"
                      disabled="#{empty WizardManager.bean.selectedWebProject}"
                      layout="pageDirection"
                      value="#{WizardManager.bean.regenerateScope}">
      <f:selectItem itemLabel="#{msg.regenerate_renditions_select_renditions_scope_all}"
                    itemValue="all"/>
      <f:selectItem itemLabel="#{msg.regenerate_renditions_select_renditions_scope_form}"
                    itemValue="form"/>
      <f:selectItem itemLabel="#{msg.regenerate_renditions_select_renditions_scope_rendering_engine_templates}"
                    itemValue="rendering_engine_template"/>
    </h:selectOneRadio>

    <h:panelGrid rendered="#{WizardManager.bean.regenerateScope eq 'form'}"
                 columns="3" cellpadding="3" cellspacing="3" border="0" width="100%"
	         columnClasses="panelGridRequiredImageColumn,panelGridLabelColumn,panelGridValueColumn">
      <h:graphicImage value="/images/icons/required_field.gif" 
                      alt="#{msg.required_field}" />
      <h:outputText value="#{msg.form}:"/>
      <a:selectList id="select_list_form_choices"
                    style="width:100%;"
                    onchange="this.form.submit()"
                    multiSelect="true"
                    value="#{WizardManager.bean.selectedForms}">
        <a:listItems id="list_items_form_choices" 
                     value="#{WizardManager.bean.formChoices}"/>
      </a:selectList>
    </h:panelGrid>

    <h:panelGrid rendered="#{WizardManager.bean.regenerateScope eq 'rendering_engine_template'}"
                 columns="3" cellpadding="3" cellspacing="3" border="0" width="100%"
	         columnClasses="panelGridRequiredImageColumn,panelGridLabelColumn,panelGridValueColumn">
      <h:graphicImage value="/images/icons/required_field.gif" 
                      alt="#{msg.required_field}" />
      <h:outputText value="#{msg.rendering_engine_template}:"/>
      <a:selectList id="select_list_rendering_engine_template_choices"
                    style="width:100%;"
                    multiSelect="true"
                    value="#{WizardManager.bean.selectedRenderingEngineTemplates}">
        <a:listItems id="list_items_rendering_engine_template_choices"
                     value="#{WizardManager.bean.renderingEngineTemplateChoices}"/>
      </a:selectList>
    </h:panelGrid>
  </h:panelGrid>
</jsp:root>
