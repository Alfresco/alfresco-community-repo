<link rel="stylesheet" href="${url.context}/css/main.css" TYPE="text/css">

<script type="text/javascript" src="${url.context}/scripts/ajax/yahoo/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="${url.context}/scripts/ajax/yahoo/connection/connection-min.js"></script>
<script type="text/javascript" src="${url.context}/scripts/ajax/mootools.v1.11.js"></script>
<script type="text/javascript" src="${url.context}/scripts/ajax/common.js"></script>
<script type="text/javascript" src="${url.context}/scripts/ajax/summary-info.js"></script>
<script type="text/javascript" src="${url.context}/scripts/ajax/mywebfiles.js"></script>
<script type="text/javascript">setContextPath('${url.context}');</script>

<script>
   // create manager object for the pop-up summary panels
   var AlfNodeInfoMgr = new Alfresco.PanelManager("NodeInfoBean.sendNodeInfo", "noderef", "portlet_node_summary_panel.ftl");
</script>

<#-- List the user modified files in all web projects the user is assigned to -->
<table cellspacing=0 cellpadding=0 border=0 class="webFilesTable">
<tr><td>
<div id="webFilesPanel">
   <#assign filecount=0>
   <#assign projectcount=0>
   <#assign search="TYPE:\"{http://www.alfresco.org/model/wcmappmodel/1.0}webfolder\"">
   <#list companyhome.childrenByLuceneSearch[search]?sort_by('name') as wp>
      <#list wp.getChildAssocsByType("wca:webuser") as user>
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
               <div class="webProjectFiles"> <#-- marker class for dynamic click script -->
                  <#assign moditems = avm.getModifiedItems(storeId, username, wp.properties["wca:defaultwebapp"])>
                  <div class="fileTitleRow">${message("portlets.mywebfiles.my_modified_items")}</div>
                  <div class="fileResources">
                  <#if moditems?size != 0>
                     <#assign lcount=0>
                     <#list moditems as t>
                        <#assign filecount=filecount+1>
                        <div class="fileItemRow${(lcount%2=0)?string("", "Alt")}">
                           <#if t.isDocument>
                              <a class="fileItemLink" href="${url.context}${t.url}" target="new" title="${t.path?html}"><img class="itemImageIcon" src="${url.context}${t.icon16}" border="0">${t.name?html}</a>
                           <#else>
                           <span title="${t.path?html}"><img class="itemImageIcon" src="${url.context}${t.icon16}"><span class="fileItemLink">${t.name?html}</span></span>
                           </#if>
                           </a>
                           <#if t.isDocument>
                              <#if t.isLocked>
                                 <img class="itemImageIcon" src="${url.context}/images/icons/locked${(t.isLockOwner)?string("_owner", "")}.gif" border="0">
                              </#if>
                              <#if t.hasAspect("wca:forminstancedata") && !t.hasAspect("wcmwf:submitted") && t.hasLockAccess>
                                 <a class="fileActionLink" href="${url.context}/c/ui/editwebcontent?sandbox=${sandbox}&webproject=${wp.id}&path=${t.path?url}&container=plain" target="new"><img class="itemImageIcon" src="${url.context}/images/icons/edit_icon.gif" border="0">${message("portlets.mywebfiles.edit")}</a>
                              </#if>
                           </#if>
                           <#assign lcount=lcount+1>
                        </div>
                     </#list>
                  <#else>
                     <div class="fileItemRow">${message("portlets.mywebfiles.no_items_modified")}</div>
                  </#if>
                  </div>
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
   <div class="filesFooter">
      ${message("portlets.mywebfiles.showing_count_files_in_count_web_projects", filecount, projectcount)}
   </div>
</td>
</tr>
</table>

<STYLE type="text/css">
.webFilesTable
{
   background-color: #F8FCFD;
   border: 1px solid #CCD4DB;
}

#webFilesPanel
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

a.fileActionLink:link, a.fileActionLink:visited, a.fileActionLink:hover
{
   color: #5A5741;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 11px;
   padding-left: 8px;
}

span.webProjectInfo
{
   vertical-align: 60%;
}

.webProjectRow
{
   background-color: #EEF7FB;
   border-top: 1px solid #EEF7FB;
   border-bottom: 1px solid #CCD4DB;
}

.webProjectRowSelected
{
   background-color: #CCE7F3;
   border-bottom: 1px solid #0092DD;
   border-top: 1px solid #0092DD;
}

.webProjectFiles
{
   background-color: #BAD7E4;
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

.fileResources
{
   border: 1px solid #AFBDC3;
   background-color: #F8FCFD;
   margin: 0px 0px 0px 48px;
   width: 360px;
   height: 92px;
   display: block;
   overflow: hidden;
}

.fileTitleRow
{
   border-top: 1px solid #CCD4DB;
   border-bottom: 1px dotted #CCD4DB;
   color: #5A5741;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 13px;
   font-weight: bold;
   padding: 4px 0px 4px 48px;
}

.fileItemRow
{
   background-color: #F8FCFD;
   color: #5A5741;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 13px;
   padding: 3px 0px 2px 3px;
}

.fileItemRowAlt
{
   background-color: #EEF7FB;
   color: #5A5741;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 13px;
   padding: 3px 0px 2px 3px;
}

a.fileItemLink:link, a.fileItemLink:visited, a.fileItemLink:hover
{
   color: #5A5741;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 11px;
   font-weight: bold;
}

span.fileItemLink
{
   color: #5A5741;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 11px;
}

.filesRow, a.filesRow:link, a.filesRow:visited, a.filesRow:hover
{
   background-color: #F8FCFD;
   color: #5A5741;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 13px;
   padding-left: 44px;
   padding-top: 4px;
   border-bottom: 1px solid #F8FCFD;
}

.filesRowAlt
{
}

img.itemImageIcon
{
   vertical-align: -25%;
   padding-right:4px;
}

span.websiteLink
{
   padding-left:8px;
   vertical-align:60%;
}

.filesFooter
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