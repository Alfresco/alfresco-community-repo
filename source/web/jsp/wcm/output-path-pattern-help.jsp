<jsp:root version="1.2" 
xmlns:jsp="http://java.sun.com/JSP/Page"
xmlns:h="http://java.sun.com/jsf/html"
xmlns:f="http://java.sun.com/jsf/core">

<jsp:directive.page language="java" pageEncoding="UTF-8" />

<script type="text/javascript">
   function toggleOutputPathPatternHelp()
   {
      var d = document.getElementById('wizard:wizard-body:mainPanel');
      if (d == null) 
      { 
         d = document.getElementById('dialog:dialog-body:mainPanel'); 
      } 
      d.style.display = d.style.display == 'block' ? 'none' : 'block';
   }
</script>
<h:panelGrid id="mainPanel" columns="1" border="0" style="display:none; padding: 5px;" styleClass="summary infoText statusInfoText">
   <h:panelGroup>
      <h:outputText  value="#{msg.general_info_part1}"/> <h:outputLink style="color: blue" value="http://freemarker.sourceforge.net"><h:outputText value="#{msg.general_info_part2}"/></h:outputLink> <h:outputText  value="#{msg.general_info_part3}"/>
   </h:panelGroup>

   <f:verbatim><br/></f:verbatim>
   <h:outputText style="font-weight: bold" value="#{msg.guidelines_title}"/>
   <h:outputText value="#{msg.guidelines_message}"/>

   <f:verbatim><br/></f:verbatim>
   <h:panelGrid id="tablePanel" columns="2" border="0">

      <h:panelGroup>
         <h:outputText style="font-weight: bold" value="#{msg.variables_title}"/>
      </h:panelGroup><h:panelGroup/>

      <f:verbatim><tt style="font-weight:bold;">name</tt></f:verbatim>      <h:outputText value="#{msg.description_name_field}"/>
      <f:verbatim><tt style="font-weight:bold;">webapp</tt></f:verbatim>    <h:outputText value="#{msg.description_webapp_field}"/>
      <f:verbatim><tt style="font-weight:bold;">cwd</tt></f:verbatim>       <h:outputText value="#{msg.description_cwd_field}"/>
      <f:verbatim><tt style="font-weight:bold;">extension</tt></f:verbatim> <h:outputText value="#{msg.description_extension_field}"/>
      <f:verbatim><tt style="font-weight:bold;">xml</tt></f:verbatim>       <h:outputText value="#{msg.description_xml_field}"/>
      <f:verbatim><tt style="font-weight:bold;">node</tt></f:verbatim>      <h:outputText value="#{msg.description_node_field}"/>
      <f:verbatim><tt style="font-weight:bold;">date</tt></f:verbatim>      <h:panelGroup><h:outputText value="#{msg.description_date_field_part1}"/> <h:outputLink style="color: blue" value="http://freemarker.sourceforge.net/docs/ref_builtins_date.html"><h:outputText value="#{msg.description_date_field_part2}"/></h:outputLink> <h:outputText value="#{msg.description_date_field_part3}"/></h:panelGroup>

      <h:panelGroup>
         <f:verbatim><br/></f:verbatim>
         <h:outputText style="font-weight: bold" value="#{msg.forexample_title}"/>
      </h:panelGroup><h:panelGroup/>		

      <f:verbatim><tt style="font-weight:bold;">${name}.xml</tt></f:verbatim>                       <h:outputText value="form_name.xml"/>
      <f:verbatim><tt style="font-weight:bold;">${name}.${extension}</tt></f:verbatim>              <h:outputText value="form_name.html"/>
      <f:verbatim><tt style="font-weight:bold;">${webapp}/content/${name}.xml</tt></f:verbatim>     <h:outputText value="/ROOT/content/form_name.xml"/>
      <f:verbatim><tt style="font-weight:bold;">${date?string("yyyy-MM-dd")}.xml</tt></f:verbatim>  <h:outputText value="2007-01-09.xml"/>
   </h:panelGrid>
	
   <h:panelGroup>
      <f:verbatim><br/></f:verbatim>
      <h:outputText  value="#{msg.wiki_reference_part1}"/> <h:outputLink style="color: blue" value="http://wiki.alfresco.com"><h:outputText value="#{msg.wiki_reference_part2}"/></h:outputLink>
   </h:panelGroup>
</h:panelGrid>

</jsp:root>
