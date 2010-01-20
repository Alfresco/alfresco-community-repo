<link rel="stylesheet" href="${url.context}/css/main.css" TYPE="text/css">

<script type="text/javascript" src="${url.context}/scripts/ajax/yahoo/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="${url.context}/scripts/ajax/yahoo/connection/connection-min.js"></script>
<script type="text/javascript" src="${url.context}/scripts/ajax/mootools.v1.11.js"></script>
<script type="text/javascript" src="${url.context}/scripts/ajax/common.js"></script>
<script type="text/javascript" src="${url.context}/scripts/ajax/summary-info.js"></script>
<script type="text/javascript" src="${url.context}/scripts/ajax/mywebforms.js"></script>
<script type="text/javascript">setContextPath('${url.context}');</script>

<script>
   // create manager object for the pop-up summary panels
   var AlfNodeInfoMgr = new Alfresco.PanelManager("NodeInfoBean.sendNodeInfo", "noderef", "portlet_node_summary_panel.ftl");
</script>

<#-- List the available web form objects in all web projects the user is assigned to -->
<table cellspacing=0 cellpadding=0 border=0 class="formsTable">
<tr><td>
<div id="formsPanel">
   <#assign formcount=0>
   <#assign projectcount=0>
   <#assign search="TYPE:\"{http://www.alfresco.org/model/wcmappmodel/1.0}webfolder\"">
   <#list companyhome.childrenByLuceneSearch[search]?sort_by('name') as wp>
      <#list wp.childAssocs["wca:webuser"] as user>
         <#if user.properties["wca:username"] = person.properties.userName>
            <#assign projectcount=projectcount+1>
            <#-- construct the sandbox name based on the webproject and current username -->
            <#assign storeId=wp.properties["wca:avmstore"]>
            <#assign username=person.properties.userName>
            <#assign sandbox=avm.userSandboxStore(storeId, username)>
            <#if avm.lookupStore(sandbox)?exists>
            <div class="webProjectRow">
               <div class="webProjectTitle">
                  <a class="webPreviewLink" href="${avm.websiteUserSandboxUrl(storeId, username)}" target="new"><img src="${url.context}/images/icons/website_large.gif" width=32 height=32 border=0><span class="websiteLink">${wp.name}</span></a>
                  <span class="webProjectInfo" onclick="event.cancelBubble=true; AlfNodeInfoMgr.toggle('${wp.nodeRef}',this);">
                     <img src="${url.context}/images/icons/popup.gif" class="popupImage" width="16" height="16" />
                  </span>
                  <a class="webProjectLink" href="${url.context}${wp.url}" target="new"><img class="itemImageIcon" src="${url.context}/images/icons/view_web_project.gif" width="16" height="16" border="0">${message("portlets.mywebfiles.view_web_project")}</a>
                  <#if wp.properties.description?exists && wp.properties.description?length!=0>
                  <br>
                  <span class="webprojectDesc">${wp.properties.description?html}</span>
                  </#if>
               </div>
               <div class="webProjectForms"> <#-- marker class for rollover script -->
               <#if wp.childAssocs["wca:webform"]?exists>
                  <div class="formsRowSeparator"></div>
                  <#list wp.childAssocs["wca:webform"] as form>
                     <#assign formcount=formcount+1>
                     <div class="formsRow">
                        <img src="${url.context}/images/icons/webform_large.gif" width=32 height=32 border=0>
                        <a class="webformLink" href="${url.context}/c/ui/createwebcontent?sandbox=${sandbox}&webproject=${wp.id}&form=${form.properties["wca:formname"]}&container=plain" target="new">${form.properties.title?html}</a>
                        <#if (form.properties.description?exists) && (form.properties.description?length!=0)>
                        <span style="vertical-align:50%">(${form.properties.description?html})</span>
                        </#if>
                     </div>
                  </#list>
               </#if>
               </div>
            </div>
            </#if>
         </#if>
      </#list>
   </#list>
</div>
</td>
</tr>
<tr>
<td>
   <div class="formsFooter">
      ${message("portlets.mywebfiles.showing_count_files_in_count_web_projects", formcount, projectcount)}
   </div>
</td>
</tr>
</table>

<STYLE type="text/css">
.formsTable
{
   background-color: #F8FCFD;
   border: 1px solid #CCD4DB;
}

#formsPanel
{
   height: 320px;
   width: 716px;
   overflow: auto;
   overflow-y: scroll;
   scrollbar-face-color: #fafdfd; 
   scrollbar-3dlight-color: #d2dde0;
   scrollbar-highlight-color: #d2dde0;
   scrollbar-shadow-color: #c3cdd0;
   scrollbar-darkshadow-color: #c3cdd0;
   scrollbar-arrow-color: #239ad7;
   scrollbar-track-color: #ecf1f2;
}

a.webPreviewLink:link, a.webPreviewLink:visited, a.webPreviewLink:hover
{
   color: #5A5741;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 13px;
   font-weight: bold;
}

a.webProjectLink:link, a.webProjectLink:visited, a.webProjectLink:hover
{
   color: #5A5741;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 11px;
   padding-left: 16px;
   vertical-align: 60%;
}

span.webProjectInfo
{
   vertical-align: 60%;
}

.webProjectRow
{
   background-color: #EEF7FB;
   border-bottom: 1px solid #CCD4DB;
}

.webProjectForms
{
   overflow: hidden;
}

.webprojectDesc
{
   color: #5A5741;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   padding-left: 40px;
}

.webProjectTitle
{
   padding: 8px;
}

img.itemImageIcon
{
   vertical-align: -25%;
   padding-right:4px;
}

.formsRowSeparator
{
   border-bottom: 1px dotted #CCD4DB;
}

.formsRow, a.formsRow:link, a.formsRow:visited, a.formsRow:hover
{
   background-color: #F8FCFD;
   color: #5A5741;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 13px;
   padding-left: 44px;
   padding-top: 4px;
   border-bottom: 1px solid #F8FCFD;
}

.formsRowAlt
{
}

span.websiteLink
{
   padding-left:8px;
   vertical-align:60%;
}

a.webformLink:link, a.webformLink:visited, a.webformLink:hover
{
   color: #5A5741;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 13px;
   vertical-align:50%;
}

.formsFooter
{
   width: 700px;
   padding: 8px;
   border: 1px solid #F8FCFD;
   background-image: url(${url.context}/images/parts/doclist_footerbg.png);
   text-align: center;
   color: #515D6B;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 13px;
   font-weight: bold;
}

</STYLE>