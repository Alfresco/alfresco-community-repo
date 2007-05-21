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
            <#if c_index < (crumbs?size - 1)>&nbsp;&gt;&nbsp;</#if>
         </#if>
      </#list>
   </div>
   <span style="float:right;margin:6px 6px 0 0;"><a href="${scripturl("?f=${filter}&p=${path}")}" class="refreshViewLink"><img src="${url.context}/images/icons/reset.gif" border="0" width="16" height="16" style="vertical-align:-25%;padding-right:4px">Refresh</a></span>
   <div class="spaceTitle">
      <img src="${url.context}${home.icon16}" width="16" height="16" alt="" style="vertical-align:-25%;padding-right:4px">${home.name?html}
   </div>
   <div class="spaceActions">
      <#-- TODO: haspermission check! -->
      <div class="spaceAction spaceActionUpload" title="Upload a new document" onclick="MySpaces.upload(this);">Upload</div>
      <div class="spaceUploadPanel">
         <#-- Url encode the path value, and encode any single quotes to generate valid string -->
         <input style="margin:4px" type="submit" value="OK" onclick='MySpaces.uploadOK(this, "${path?url?replace("'","_%_")}");'>
         <input style="margin:4px" type="button" value="Cancel" onclick="MySpaces.uploadClose(this);">
      </div>
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
      <#-- populated via an AJAX call myspacecontent webscript -->
      <#-- resolved path, filter and home.noderef required as arguments! -->
      <script>MySpaces.Path="${path?replace("\"","\\\"")}";MySpaces.Filter="${filter}";MySpaces.Home="${home.nodeRef}";</script>
   </div>
   <div class="spaceFooter">
      <#-- TODO: get the count value dynamically from the AJAX webscript output above... -->
      Showing <span id="spaceCount">0</span> items(s)
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
   overflow-y: scroll;
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
   padding: 0px 8px 6px 40px;
}

.spaceIcon
{
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
   padding-left: 16px;
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

.spaceUploadPanel
{
   position: absolute;
   border: 1px solid #CCD4DB;
   background-color: #EEF7FB;
   width: 24em;
   height: 4em;
   padding: 8px;
   margin: 8px;
   display: none;
   left: 8px;
   -moz-border-radius: 5px;
}

a.refreshViewLink:link, a.refreshViewLink:visited, a.refreshViewLink:hover
{
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 12px;
   color: #515D6B;
   text-decoration: none;
}

</STYLE>