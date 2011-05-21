<link rel="stylesheet" href="${url.context}/css/main.css" TYPE="text/css">

<script type="text/javascript" src="${url.context}/scripts/ajax/yahoo/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="${url.context}/scripts/ajax/yahoo/connection/connection-min.js"></script>
<script type="text/javascript" src="${url.context}/scripts/ajax/mootools.v1.11.js"></script>
<script type="text/javascript" src="${url.context}/scripts/ajax/common.js"></script>
<script type="text/javascript" src="${url.context}/scripts/ajax/summary-info.js"></script>
<script type="text/javascript" src="${url.context}/scripts/ajax/myspaces.js"></script>
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
<#assign chLen=companyhome.name?length>
<#if path?starts_with("/" + companyhome.name)>
   <#if path?length=chLen+1>
      <#assign home=companyhome>
   <#elseif companyhome.childByNamePath[path[(chLen+2)..]]?exists>
      <#assign home=companyhome.childByNamePath[path[(chLen+2)..]]>
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
            <a class="spaceBreadcrumbLink<#if c_index = (crumbs?size - 1)> spaceCurrentSpace</#if>" href="${scripturl("?f=${filter}&p=${bcpath?url}")}"><img src="${url.context}/images/icons/space-icon-default-16.png" border="0" width="16" height="16" alt="" style="vertical-align:-25%;margin-right:4px">${c}</a>
            <#if c_index < (crumbs?size - 1)>&nbsp;&gt;&nbsp;</#if>
         </#if>
      </#list>
   </div>
   <div class="spaceToolbar">
      <#-- TODO: permission checks on the actions! -->
      <#-- Upload File action -->
      <div class="spaceToolbarAction spaceToolbarActionUpload" title="${message('portlets.myspaces.upload.title')}" <#if home.hasPermission("CreateChildren")>onclick="MySpaces.upload(this);"</#if>>${message("portlets.myspaces.upload")}</div>
      <div class="spaceUploadPanel">
         <#-- Url encode the path value, and encode any single quotes to generate valid string -->
         <input class="spaceFormItem" type="button" value="${message('portlets.button.ok')}" onclick='MySpaces.uploadOK(this, "${path?url?replace("'","_%_")}");'>
         <input class="spaceFormItem" type="button" value="${message('portlets.button.cancel')}" onclick="MySpaces.closePopupPanel();">
      </div>
      <div id="spaceUpdateDocPanel">
         <input class="spaceFormItem" type="button" value="${message('portlets.button.ok')}" onclick="MySpaces.updateOK(this);">
         <input class="spaceFormItem" type="button" value="${message('portlets.button.cancel')}" onclick="MySpaces.closePopupPanel();">
      </div>
      <#-- Create Space action -->
      <div class="spaceToolbarAction spaceToolbarActionCreateSpace" title="${message('portlets.myspaces.create_space.title')}" <#if home.hasPermission("CreateChildren")>onclick="MySpaces.createSpace(this);"</#if>>${message("portlets.myspaces.create_space")}</div>
      <div class="spaceCreateSpacePanel">
         <table cellspacing="2" cellpadding="2" border="0">
            <tr><td class="spaceFormLabel">${message("portlets.myspaces.name")}:</td><td><input class="spaceFormItem" type="text" size="32" maxlength="1024" id="space-name"></td></tr>
            <tr><td class="spaceFormLabel">${message("portlets.myspaces.title")}:</td><td><input class="spaceFormItem" type="text" size="32" maxlength="1024" id="space-title"></td></tr>
            <tr><td class="spaceFormLabel">${message("portlets.myspaces.description")}:</td><td><input class="spaceFormItem" type="text" size="32" maxlength="1024" id="space-description"></td></tr>
         </table>
         <input class="spaceFormItem" type="button" value="${message('portlets.button.ok')}" onclick='MySpaces.createSpaceOK(this, "${path?url?replace("'","_%_")}");'>
         <input class="spaceFormItem" type="button" value="${message('portlets.button.cancel')}" onclick="MySpaces.closePopupPanel();">
      </div>
   </div>
   <div class="spaceHeader">
      <table border="0" cellspacing="6" cellpadding="0" width="100%">
         <tr>
            <th><a class="spacefilterLink <#if filter=0>spacefilterLinkSelected</#if>" href="#" onclick="MySpaces.filter(0); return false;">${message("portlets.myspaces.all_items")}</a></th>
            <th><a class="spacefilterLink <#if filter=1>spacefilterLinkSelected</#if>" href="#" onclick="MySpaces.filter(1); return false;">${message("portlets.myspaces.spaces")}</a></th>
            <th><a class="spacefilterLink <#if filter=2>spacefilterLinkSelected</#if>" href="#" onclick="MySpaces.filter(2); return false;">${message("portlets.myspaces.documents")}</a></th>
            <th><a class="spacefilterLink <#if filter=3>spacefilterLinkSelected</#if>" href="#" onclick="MySpaces.filter(3); return false;">${message("portlets.myspaces.my_items")}</a></th>
            <th><a class="spacefilterLink <#if filter=4>spacefilterLinkSelected</#if>" href="#" onclick="MySpaces.filter(4); return false;">${message("portlets.myspaces.recently_modified")}</a></th>
            <td align=right>
               <a href="#" onclick="MySpaces.refreshList(); return false;" class="refreshViewLink"><img src="${url.context}/images/icons/reset.gif" border="0" width="16" height="16" class="spaceImageIcon">${message("portlets.refresh")}</a>
            </td>
         </tr>
      </table>
   </div>
   <div id="spacePanelOverlay"></div>
   <div id="spacePanelOverlayAjax"></div>
   <div id="spacePanel">
      <#-- populated via an AJAX call to 'myspacespanel' webscript -->
      <#-- resolved path, filter and home.noderef required as arguments -->
      <script>
         MySpaces.ServiceContext="${url.serviceContext}";
         MySpaces.ScriptUrlEncoder=eval("MySpaces.ScriptUrlEncoder=" + unescape("${clienturlfunction("encUrl")}"));
         MySpaces.Path="${path?replace("\"","\\\"")}";
         MySpaces.Filter="${filter}";
         MySpaces.Home="${home.nodeRef}";
      </script>
   </div>
   <div id="spaceMessagePanel" class="spaceMessagePanel">
      <div style="margin:2px;float:right"><img src="${url.context}/images/icons/close_portlet_panel.gif" style="cursor:pointer;" width="16" height="16" onclick="MySpaces.closeMessage();"></div>
      <div class="spaceMessagePanelLabel"></div>
   </div>
   <div class="spaceFooter">
      <#-- the count value is retrieved and set dynamically from the AJAX webscript output above -->
      <span class="spaceFooterText" id="spaceCount" />0</span>
   </div>
</div>

<style type="text/css">
a.spacefilterLink:link, a.spacefilterLink:visited
{
   color: #8EA1B3;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 12px;
   font-weight: bold;
   text-decoration: none;
   outline: none;
   padding-left: 4px;
   padding-right: 4px;
}

a.spacefilterLink:hover
{
   color: #168ECE;
   background-color: #EEF7FB;
   font-weight: bold;
   text-decoration: none;
}

a.spacefilterLinkSelected:link, a.spacefilterLinkSelected:visited, .spaceCurrentSpace
{
   color: #0085CA !important;
   font-weight: bold !important;
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

.spaceHeader
{
   background-image: url(${url.context}/images/parts/doclist_headerbg.png);
   height: 30px;
}
.spaceHeader table
{
	border-collapse: separate;
	border-spacing: 6px 6px;
}

#spacePanel
{
   height: 320px;
   width: 718px;
   overflow: auto;
   overflow-y: scroll;
   border-top: 1px solid #CCD4DB;
   border-bottom: 1px solid #CCD4DB;
   visibility: hidden;
   scrollbar-face-color: #fafdfd; 
   scrollbar-3dlight-color: #d2dde0;
   scrollbar-highlight-color: #d2dde0;
   scrollbar-shadow-color: #c3cdd0;
   scrollbar-darkshadow-color: #c3cdd0;
   scrollbar-arrow-color: #239ad7;
   scrollbar-track-color: #ecf1f2;
}

#spacePanelOverlay
{
   background-color: #fff;
   position: absolute;
   height: 320px;
   width: 720px;
   overflow: hidden;
}

#spacePanelOverlayAjax
{
   background-color: #fff;
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
}
.spaceRowOdd
{
   background-color: #F1F7FD;
}
.spaceRowEven
{
   background-color: #FFFFFF;
}

.spaceFooter
{
   height: 34px;
   width: 718px;
   padding: 0px;
   border: 1px solid #F8FCFD;
   background-image: url(${url.context}/images/parts/doclist_footerbg.png);
   text-align: center;
   color: #515D6B;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 13px;
   font-weight: bold;
}

.spaceFooterText
{
   display: block;
   margin-top: 8px;
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
   background-image: url(${url.context}/images/parts/spacelist_breadbg.png);
   border-bottom: 1px solid #CCD4DB;
   padding: 6px;
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

.spaceFormItemError
{
   border-color: red !important;
}

.spaceToolbar
{
   background-color: #D0D8E0;
   border-bottom: 1px solid #CCD4DB;
   height: 28px;
}

.spaceToolbarAction
{
   background-repeat: no-repeat;
   background-position: 2px;
   color: #515D6B;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 12px;
   float: left;
   margin: 3px 3px 3px 3px;
   height: 17px;
   cursor: pointer;
   padding: 2px 4px 1px 22px;
}

.spaceToolbarActionUpload
{
   background-image: url(${url.context}/images/icons/add.gif);
}

.spaceToolbarActionCreateSpace
{
   background-image: url(${url.context}/images/icons/create_space.gif);
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
   height: 20px;
   border: 1px solid #ffffff;
   padding: 10px 0px 8px 36px;
   cursor: pointer;
}

.docActionCheckout
{
   background-image: url(${url.context}/images/icons/doclist_action_checkout.png);
   border-bottom: none;
   border-right: none;
}

.docActionCheckin
{
   background-image: url(${url.context}/images/icons/doclist_action_checkin.png);
   border-bottom: none;
   border-right: none;
}

.docActionLocked
{
   background-image: url(${url.context}/images/icons/doclist_action_locked.png) !important;
   cursor: default !important;
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
   z-index: 1;
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
   z-index: 1;
   -moz-border-radius: 5px;
}

.spaceMessagePanel
{
   position: absolute;
   border: 1px solid #65696C;
   background-color: #7E8387;
   width: 250px;
   *width: 260px;
   height: 72px;
   padding: 8px;
   margin-left: 440px;
   display: none;
   z-index: 1;
   -moz-border-radius: 7px;
}

.spaceMessagePanelLabel
{
   color: white;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 12px;
}

#spaceUpdateDocPanel
{
   position: absolute;
   border: 1px solid #CCD4DB;
   background-color: #EEF7FB;
   width: 24em;
   height: 5em;
   padding: 8px;
   margin: 8px;
   display: none;
   z-index: 1;
   -moz-border-radius: 5px;
}

a.refreshViewLink:link, a.refreshViewLink:visited, a.refreshViewLink:hover
{
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 12px;
   color: #515D6B;
   text-decoration: none;
   outline: none;
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

div.spacesNoItems
{
   color: #5A5741;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
}

img.spaceImageIcon
{
   vertical-align: -25%;
   padding-right:4px;
}
</style>