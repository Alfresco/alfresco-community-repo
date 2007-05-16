<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<link rel="stylesheet" href="${url.context}/css/main.css" TYPE="text/css">

<script type="text/javascript" src="/alfresco/scripts/ajax/yahoo/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/yahoo/connection/connection-min.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/mootools.v1.1.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/common.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/summary-info.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/myspaces.js"></script>
<script type="text/javascript">setContextPath('${url.context}');</script>

<script>
   // create manager object for the pop-up summary panels
   var AlfNodeInfoMgr = new Alfresco.PanelManager("NodeInfoBean.sendNodeInfo", "noderef", "portlet_node_summary_panel.ftl");
</script>

<#-- get the filter mode from the passed in args -->
<#-- filters: 0=all, 1=spaces, 2=docs, 3=mine -->
<#if args.f?exists && args.f?length!=0><#assign filter=args.f?number><#else><#assign filter=0></#if>

<#-- get the path location from the passed in args, remove trailing slash -->
<#if args.p?exists><#assign path=args.p><#else><#assign path=""></#if>
<#if path?ends_with("/")><#assign path=path[0..path?length-2]></#if>
<#-- resolve the path (from Company Home) into a node or fall back to userhome-->
<#if path?starts_with("/Company Home")>
   <#if path?length=13>
      <#assign home=companyhome>
   <#elseif companyhome.childByNamePath[args.p[14..]]?exists>
      <#assign home=companyhome.childByNamePath[args.p[14..]]>
   <#else>
      <#assign home=userhome>
   </#if>
<#else>
   <#assign home=userhome>
</#if>
<#assign path=home.displayPath + "/" + home.name>

<div class="spaceTable">
   <div class="spaceBreadcrumb">
      <#-- construct breadcrumb elements as links -->
      <#assign bcpath="/">
      <#assign crumbs=path?split("/")>
      <#list crumbs as c>
         <#if c?length != 0>
            <#assign bcpath=bcpath+c+"/">
            <a class="spaceBreadcrumbLink" href="${scripturl("?f=${filter}&p=${bcpath}")}"><img src="${url.context}/images/icons/space-icon-default-16.png" border="0" width="16" height="16" alt="" style="vertical-align:-25%;padding-right:2px">${c}</a>
            <#if c_index<crumbs?size-1>&nbsp;&gt;&nbsp;</#if>
         </#if>
      </#list>
   </div>
   <div class="spaceTitle">
      <img src="${url.context}${home.icon16}" width="16" height="16" alt="" style="vertical-align:-25%;padding-right:6px">${home.name?html}
   </div>
   <div class="spaceActions">
      <div class="spaceAction spaceActionUpload" title="Upload a new document">Upload</div>
      <div class="spaceAction spaceActionCreateSpace" title="Create a new Space">Create Space</div>
   </div>
   <div style="text-align: center;">
      <center>
      <table border=0 cellspacing=8 cellpadding=0>
         <tr>
            <th><a class="spacefilterLink <#if filter=0>spacefilterLinkSelected</#if>" href="${scripturl("?f=0&p=${path}")}">All Items</a></th>
            <th><a class="spacefilterLink <#if filter=1>spacefilterLinkSelected</#if>" href="${scripturl("?f=1&p=${path}")}">Spaces</a></th>
            <th><a class="spacefilterLink <#if filter=2>spacefilterLinkSelected</#if>" href="${scripturl("?f=2&p=${path}")}">Documents</a></th>
            <th><a class="spacefilterLink <#if filter=3>spacefilterLinkSelected</#if>" href="${scripturl("?f=3&p=${path}")}">My Items</a></th>
         </tr>
      </table>
      </center>
   </div>
   <div id="spacePanel">
      <#assign user=person.properties.userName>
      <#assign count=0>
      <#list home.children?sort_by('name') as d>
         <#if (filter=0) ||
              (filter=1 && d.isContainer) ||
              (filter=2 && d.isDocument) ||
              (filter=3 && (d.properties.creator == user || d.properties.modifier == user))>
         <#assign count=count+1>
         <div class="spaceRow">
            <div class="spaceIcon">
               <#if d.isDocument>
                  <a href="${url.context}${d.url}" target="new"><img class="spaceIconImage" alt="" width="16" height="16" src="${url.context}${d.icon16?replace(".gif",".png")}" border=0></a>
               <#else>
                  <a href="${scripturl("?f=${filter}&p=${path}/${d.name}")}"><img class="spaceIconImage" alt="" width="16" height="16" src="${url.context}${d.icon16?replace(".gif",".png")}" border=0></a>
               </#if>
            </div>
            <div class="spaceItem">
               ${d.name?html}
               <span class="spaceInfo" onclick="event.cancelBubble=true; AlfNodeInfoMgr.toggle('${d.nodeRef}',this);">
                  <img src="${url.context}/images/icons/popup.gif" class="popupImage" width="16" height="16" />
               </span>
            </div>
            <div class="spaceDetail">
               <table cellpadding="2" cellspacing="0" border="0">
   	            <tr>
   	               <td>
   	                  <span class="spaceMetaprop">Description:</span>&nbsp;<span class="spaceMetadata"><#if d.properties.description?exists>${d.properties.description?html}<#else>&nbsp;</#if></span><br />
      	               <span class="spaceMetaprop">Modified:</span>&nbsp;<span class="spaceMetadata">${d.properties.modified?datetime}</span><br />
      	               <span class="spaceMetaprop">Modified By:</span>&nbsp;<span class="spaceMetadata">${d.properties.modifier}</span>
   	               </td>
   	               <td width="24">&nbsp;</td>
   	               <td>
   	                  <span class="spaceMetaprop">Created:</span>&nbsp;<span class="spaceMetadata">${d.properties.created?datetime}</span><br />
      	               <span class="spaceMetaprop">Created By:</span>&nbsp;<span class="spaceMetadata">${d.properties.creator}</span><br />
   	                  <span class="spaceMetaprop">Size:</span>&nbsp;<span class="spaceMetadata">${(d.size/1000)?string("0.##")} KB</span>
   	               </td>
   	            </tr>
   	         </table>
            </div>
         </div>
         </#if>
      </#list>
   </div>
   <div class="spaceFooter">
      Showing ${count} items(s)
   </div>
</div>

<STYLE type="text/css">
a.spacefilterLink:link, a.spacefilterLink:visited
{
   color: #8EA1B3;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 13px;
   font-weight: bold;
   text-decoration: none;
   padding-left: 4px;
   padding-right: 4px;
}

a.spacefilterLink:hover
{
   color: #168ECE;
   background-color: #EEF7FB;
}

a.spacefilterLinkSelected:link, a.spacefilterLinkSelected:visited
{
   color: #168ECE;
   background-color: #EEF7FB;
}

.spaceTable
{
   background-color: #F8FCFD;
   border: 1px solid #CCD4DB;
   width: 720px;
}

.spaceTitle
{
   background-color: #F8FCFD;
   color: #515D6B;
   text-align: center;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 14px;
   font-weight: bold;
   padding: 6px;
   border-bottom: 1px solid #CCD4DB;
}

#spacePanel
{
   height: 320px;
   width: 720px;
   overflow: auto;
   border-top: 1px solid #CCD4DB;
   border-bottom: 1px solid #CCD4DB;
   visibility: hidden;
}

.spaceRow
{
   padding-top: 4px;
   border-top: 1px solid #F8FCFD;
   border-bottom: 1px solid #CCD4DB;
}

.spaceFooter
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

.spaceItem
{
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 14px;
   color: #515D6B;
   margin: 0px 0px 0px 24px;
   padding: 0px 8px 6px 8px;
}

.spaceIcon
{
   width: 32px;
   float: left;
   padding-left: 16px;
   padding-top: 4px;
}

.spaceInfo
{
   visibility: hidden;
}

.spaceDetail
{
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 12px;
   color: #000000;
   display: none;
   overflow: hidden;
   padding-left: 48px;
}

.spaceItemSelected
{
   background-color: #CCE7F3 !important;
   border-bottom: 1px solid #0092DD !important;
   border-top: 1px solid #0092DD !important;
}

.spaceMetadata
{
   color: #515D6B;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
}

.spaceMetaprop
{
   color: #515D6B;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-weight: bolder;
}

.spaceBreadcrumb
{
   background-color: #DBE1E7;
   padding: 6px;
   border-bottom: 1px solid #CCD4DB;
}

a.spaceBreadcrumbLink:link, a.spaceBreadcrumbLink:visited, a.spaceBreadcrumbLink:hover
{
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 12px;
   color: #515D6B;
   text-decoration: none;
}

.spaceActions
{
   background-color: #EEF7FB;
   height: 4em;
   border-bottom: 1px solid #CCD4DB;
}

.spaceAction
{
   background-repeat: no-repeat;
   background-position: 2px;
   background-color: #F8FCFD;
   color: #515D6B;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 12px;
   float: left;
   margin: 3px;
   height: 2em;
   cursor: pointer;
   padding: 10px 4px 2px 34px;
   border: 1px dashed #CCD4DB;
}

.spaceActionUpload
{
   background-image: url(${url.context}/images/icons/doclist_action_upload.png);
}

.spaceActionCreateSpace
{
   background-image: url(${url.context}/images/icons/doclist_action_createspace.png);
}

</STYLE>