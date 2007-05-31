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
<#-- filters: 0=all, 1=spaces, 2=docs, 3=mine, 4=recent -->
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
            <a class="spaceBreadcrumbLink" href="${scripturl("?f=${filter}&p=${bcpath?url}")}"><img src="${url.context}/images/icons/space-icon-default-16.png" border="0" width="16" height="16" alt="" style="vertical-align:-25%;padding-right:2px">${c}</a>
            <#if c_index < (crumbs?size - 1)>&nbsp;&gt;&nbsp;</#if>
         </#if>
      </#list>
   </div>
   <div class="spaceTitle">
      <img src="${url.context}${home.icon16}" width="16" height="16" alt="" class="spaceImageIcon">${home.name?html}
   </div>
   <div class="spaceToolbar">
      <#-- TODO: permission checks on the actions! -->
      <#-- Upload File action -->
      <div class="spaceToolbarAction spaceToolbarActionUpload" title="Upload a new document" <#if home.hasPermission("CreateChildren")>onclick="MySpaces.upload(this);"</#if>>Upload</div>
      <div class="spaceUploadPanel">
         <#-- Url encode the path value, and encode any single quotes to generate valid string -->
         <input class="spaceFormItem" type="submit" value="OK" onclick='MySpaces.uploadOK(this, "${path?url?replace("'","_%_")}");'>
         <input class="spaceFormItem" type="button" value="Cancel" onclick="MySpaces.closePopupPanel();">
      </div>
      <#-- Create Space action -->
      <div class="spaceToolbarAction spaceToolbarActionCreateSpace" title="Create a new Space" <#if home.hasPermission("CreateChildren")>onclick="MySpaces.createSpace(this);"</#if>>Create Space</div>
      <div class="spaceCreateSpacePanel">
         <table cellspacing="2" cellpadding="2" border="0">
            <tr><td class="spaceFormLabel">Name:</td><td><input class="spaceFormItem" type="text" size="32" maxlength="1024" id="space-name"></td></tr>
            <tr><td class="spaceFormLabel">Title:</td><td><input class="spaceFormItem" type="text" size="32" maxlength="1024" id="space-title"></td></tr>
            <tr><td class="spaceFormLabel">Description:</td><td><input class="spaceFormItem" type="text" size="32" maxlength="1024" id="space-description"></td></tr>
         </table>
         <input class="spaceFormItem" type="button" value="OK" onclick='MySpaces.createSpaceOK(this, "${path?url?replace("'","_%_")}");'>
         <input class="spaceFormItem" type="button" value="Cancel" onclick="MySpaces.closePopupPanel();">
      </div>
   </div>
   <div>
      <table border=0 cellspacing=8 cellpadding=0 width=100%>
         <tr>
            <th><a class="spacefilterLink <#if filter=0>spacefilterLinkSelected</#if>" href="${scripturl("?f=0&p=${path?url}")}">All Items</a></th>
            <th><a class="spacefilterLink <#if filter=1>spacefilterLinkSelected</#if>" href="${scripturl("?f=1&p=${path?url}")}">Spaces</a></th>
            <th><a class="spacefilterLink <#if filter=2>spacefilterLinkSelected</#if>" href="${scripturl("?f=2&p=${path?url}")}">Documents</a></th>
            <th><a class="spacefilterLink <#if filter=3>spacefilterLinkSelected</#if>" href="${scripturl("?f=3&p=${path?url}")}">My Items</a></th>
            <th><a class="spacefilterLink <#if filter=4>spacefilterLinkSelected</#if>" href="${scripturl("?f=4&p=${path?url}")}">Recently Modified</a></th>
            <td align=right>
               <a href="${scripturl("?f=${filter}&p=${path?url}")}" class="refreshViewLink"><img src="${url.context}/images/icons/reset.gif" border="0" width="16" height="16" class="spaceImageIcon">Refresh</a>
            </td>
         </tr>
      </table>
   </div>
   <div id="spacePanelOverlay"></div>
   <div id="spacePanel">
      <#-- populated via an AJAX call to 'myspacespanel' webscript -->
      <#-- resolved path, filter and home.noderef required as arguments -->
      <script>MySpaces.Path="${path?replace("\"","\\\"")}";MySpaces.Filter="${filter}";MySpaces.Home="${home.nodeRef}";</script>
   </div>
   <div class="spaceFooter">
      <#-- the count value is retrieved and set dynamically from the AJAX webscript output above -->
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

#spacePanelOverlay
{
   background-image: url(${url.context}/images/icons/ajax_anim.gif);
   background-position: center;
   background-repeat: no-repeat;
   position: absolute;
   height: 320px;
   width: 720px;
   overflow: hidden;
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

.spaceResource
{
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 12px;
   background-color: #bad7e4;
   color: #000000;
   margin: 0px;
   border-top: 1px dotted #0092dd;
   visibility: hidden;
   overflow: hidden;
}

.spacesAjaxWait
{
   background-image: url(${url.context}/images/icons/ajax_anim.gif);
   background-position: center;
   background-repeat: no-repeat;
   width: 696px;
   height: 150px;
   overflow: hidden;
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

.spaceFormLabel
{
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 11px;
   color: #515D6B;
}

.spaceFormItem
{
   margin: 4px;
   padding: 2px;
   background-color: #F8FCFD;
   border: 1px solid #CCD4DB;
}

.spaceToolbar
{
   background-color: #EEF7FB;
   height: 4em;
   border-bottom: 1px solid #CCD4DB;
}

.spaceToolbarAction
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

.spaceToolbarActionUpload
{
   background-image: url(${url.context}/images/icons/doclist_action_upload.png);
}

.spaceToolbarActionCreateSpace
{
   background-image: url(${url.context}/images/icons/doclist_action_createspace.png);
}

.spaceAction
{
   color: #515D6B;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 9pt;
   font-weight: bolder;
   background-color: #c3dce7;
   background-repeat: no-repeat;
   background-position: left;
   width: 87px;
   height: 28px;
   border: 1px solid #ffffff;
   float: left;
   display: block;
   padding: 10px 0px 0px 36px;
   cursor: pointer;
}

.docActionCheckout
{
   background-image: url(${url.context}/images/icons/doclist_action_checkout.png);
   border-bottom: none;
   border-right: none;
}

.docActionEditDetails
{
   background-image: url(${url.context}/images/icons/doclist_action_edit.png);
   border-bottom: none;
}

.docActionUpdate
{
   background-image: url(${url.context}/images/icons/doclist_action_update.png);
   border-bottom: none;
   border-right: none;
}

.docActionViewContent
{
   background-image: url(${url.context}/images/icons/doclist_action_view.png);
   border-bottom: none;
}

.docActionDelete
{
   background-image: url(${url.context}/images/icons/doclist_action_delete.png);
   border-right: none;
}

.docActionMoreActions
{
   padding-left: 20px;
   padding-right: 16px;
}

.spaceActionMoreActions
{
   padding-left: 20px;
   padding-right: 16px;
   border-top: none;
}

.spaceActionEditDetails
{
   background-image: url(${url.context}/images/icons/doclist_action_edit.png);
}

.spaceActionDelete
{
   background-image: url(${url.context}/images/icons/doclist_action_delete.png);
   border-left: none;
}

.spaceUploadPanel
{
   position: absolute;
   border: 1px solid #CCD4DB;
   background-color: #EEF7FB;
   width: 24em;
   height: 5em;
   padding: 8px;
   margin: 8px;
   display: none;
   left: 8px;
   -moz-border-radius: 5px;
}

.spaceCreateSpacePanel
{
   position: absolute;
   border: 1px solid #CCD4DB;
   background-color: #EEF7FB;
   width: 24em;
   height: 11.4em;
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

.spacePreview
{
   color: #515D6B;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   overflow: hidden;
   height: 144px;
   width: 410px;
   border: 1px solid #0092dd;
}

a.childSpaceLink:link, a.childSpaceLink:visited, a.childSpaceLink:hover
{
   color: #5A5741;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
}

img.spaceImageIcon
{
   vertical-align: -25%;
   padding-right:4px;
}

</STYLE>