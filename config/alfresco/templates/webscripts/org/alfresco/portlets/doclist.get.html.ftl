<link rel="stylesheet" href="${url.context}/css/main.css" TYPE="text/css">

<script type="text/javascript" src="${url.context}/scripts/ajax/yahoo/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="${url.context}/scripts/ajax/yahoo/connection/connection-min.js"></script>
<script type="text/javascript" src="${url.context}/scripts/ajax/mootools.v1.11.js"></script>
<script type="text/javascript" src="${url.context}/scripts/ajax/common.js"></script>
<script type="text/javascript" src="${url.context}/scripts/ajax/summary-info.js"></script>
<script type="text/javascript" src="${url.context}/scripts/ajax/doclist.js"></script>
<script type="text/javascript">setContextPath('${url.context}');</script>

<script>
   // create manager object for the pop-up summary panels
   var AlfNodeInfoMgr = new Alfresco.PanelManager("NodeInfoBean.sendNodeInfo", "noderef", "portlet_node_summary_panel.ftl");
</script>

<#-- get the filter mode from the passed in args -->
<#-- filters: 0=all, 1=word, 2=html, 3=pdf, 4=recent -->
<#if args.f?exists && args.f?length!=0><#assign filter=args.f?number><#else><#assign filter=0></#if>

<#-- get the path location from the passed in args -->
<#if args.p?exists><#assign path=args.p><#else><#assign path=""></#if>

<#-- see if lucene query specified - this overrides any path argument -->
<#if !args.q?exists || args.q?length=0>
   <#assign query="">
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
<#else>
   <#assign query=args.q>
</#if>

<table border=0 cellspacing=0 cellpadding=0 class="docTable">
<tr>
   <td class="docHeader">
      <table border="0" cellspacing="0" cellpadding="0" width="100%">
         <tr>
            <th><a id="docFilter0" class="docfilterLink <#if filter=0>docfilterLinkSelected</#if>" href="#" onclick="MyDocs.filter(0); return false;"><span>${message("portlets.doclist.all_items")}</span></a></th>
            <th><a id="docFilter1" class="docfilterLink <#if filter=1>docfilterLinkSelected</#if>" href="#" onclick="MyDocs.filter(1); return false;"><span>${message("portlets.doclist.word_documents")}</span></a></th>
            <th><a id="docFilter2" class="docfilterLink <#if filter=2>docfilterLinkSelected</#if>" href="#" onclick="MyDocs.filter(2); return false;"><span>${message("portlets.doclist.html_documents")}</span></a></th>
            <th><a id="docFilter3" class="docfilterLink <#if filter=3>docfilterLinkSelected</#if>" href="#" onclick="MyDocs.filter(3); return false;"><span>${message("portlets.doclist.pdf_documents")}</span></a></th>
            <th><a id="docFilter4" class="docfilterLink <#if filter=4>docfilterLinkSelected</#if>" href="#" onclick="MyDocs.filter(4); return false;"><span>${message("portlets.doclist.recently_modified")}</span></a></th>
            <th align=right>
               <a href="#" onclick="MyDocs.refreshList(); return false;" class="docRefreshViewLink"><img src="${url.context}/images/icons/reset.gif" border="0" width="16" height="16" style="vertical-align:-25%;padding-right:4px">${message("portlets.refresh")}</a>
            </th>
         </tr>
      </table>
   </td>
</tr>
<tr>
   <td>
      <div id="docUpdatePanel">
         <input class="docFormItem" type="button" value="${message('portlets.button.ok')}" onclick="MyDocs.updateOK(this);">
         <input class="docFormItem" type="button" value="${message('portlets.button.cancel')}" onclick="MyDocs.closePopupPanel();">
      </div>
      <div id="docPanelOverlay"></div>
      <div id="docPanelOverlayAjax"></div>
      <div id="docPanel">
         <#-- populated via an AJAX call to 'doclistpanel' webscript -->
         <#-- resolved path, filter and home.noderef required as arguments -->
         <script>
            MyDocs.ServiceContext="${url.serviceContext}";
            MyDocs.Filter="${filter}";
            <#if home?exists>MyDocs.Home="${home.nodeRef}";</#if>
            MyDocs.Query="${query?replace("\"","\\\"")}";
         </script>
      </div>
      <div id="docMessagePanel">
         <div class="docMessagePanelClose"><img id="docMessagePanelCloseImage" src="${url.context}/images/icons/close_portlet_static.gif" onclick="MyDocs.closeMessage();" /></div>
         <div class="docMessagePanelLabel"></div>
      </div>
   </td>
</tr>
<tr>
   <td>
      <div class="docFooter">
         <span class="docFooterText" id="docCount" />
      </div>
   </td>
</tr>
</table>

<style type="text/css">
a.docfilterLink, a.docfilterLink:hover
{
   color: #8EA1B3 !important;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif !important;
   font-size: 12px !important;
   font-weight: bold !important;
   text-decoration: none !important;
   padding: 8px 4px 16px;
   outline: none;
   display: block;
}

a.docfilterLink:hover span
{
   color: #168ECE;
   background-color: #EEF7FB;
   text-decoration: none;
}

a.docfilterLinkSelected
{
   background: transparent url("${url.context}/images/parts/doclist_item_marker.png") no-repeat scroll center 29px !important;
   padding-bottom: 16px;
}

a.docfilterLinkSelected:link, a.docfilterLinkSelected:visited
{
   color: #0085CA;
}

.docTable
{
   background-color: #FFFFFF;
   border: 1px solid #CCD4DB;
}

#docPanel
{
   height: 320px;
   width: 716px;
   overflow: none;
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

#docPanelOverlay
{
   background-color: #fff;
   position: absolute;
   height: 320px;
   width: 716px;
   overflow: hidden;
}

#docPanelOverlayAjax
{
   background-color: #fff;
   background-image: url(${url.context}/images/icons/ajax_anim.gif);
   background-position: center;
   background-repeat: no-repeat;
   position: absolute;
   height: 320px;
   width: 716px;
   overflow: hidden;
}

.docRow
{
   padding-top: 4px;
}
.docRowOdd
{
   background-color: #F1F7FD;
}
.docRowEven
{
   background-color: #FFFFFF;
}

.docHeader
{
   background-image: url(${url.context}/images/parts/doclist_headerbg.png);
   height: 40px;
   vertical-align: top;
}

.docHeader th
{
	text-align: center;
}

.docFooter
{
   height: 34px;
   width: 716px;
   padding: 0px;
   border: 1px solid #F8FCFD;
   background-image: url(${url.context}/images/parts/doclist_footerbg.png);
   text-align: center;
   color: #515D6B;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 13px;
   font-weight: bold;
}

.docFooterText
{
   display: block;
   margin-top: 8px;
}

a.docItem, a.docItem:hover
{
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif !important;
   font-size: 14px !important;
   color: #515D6B !important;
   padding: 0px 8px 6px 8px;
   text-decoration: none !important;
}

.docIcon
{
   float: left;
   padding-left: 16px;
   padding-top: 4px;
}

.docInfo
{
   visibility: hidden;
}

.docDetail
{
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 12px;
   color: #000000;
   display: none;
   overflow: hidden;
   padding-left: 16px;
}

.docItemSelected
{
   background: #CCE7F3 url("${url.context}/images/parts/doclist_arrow_down.png") no-repeat right top;
   border-bottom: 1px solid #0092DD !important;
   border-top: 1px solid #0092DD !important;
}

.docItemSelectedOpen
{
   background-image: url("${url.context}/images/parts/doclist_arrow_up.png") !important;
}

.docResource
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

.docMetadata
{
   color: #515D6B;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
}

.docMetaprop
{
   color: #515D6B;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-weight: bolder;
}

.docPreview
{
   background-color: #ddebf2;
   color: #515D6B;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   overflow: hidden;
   height: 140px;
   width: 406px;
   border: 1px solid #75badd;
   padding: 2px;
}

.docAction
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
   border: 1px solid #fff;
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

.docListAjaxWait
{
   background-image: url(${url.context}/images/icons/ajax_anim.gif);
   background-position: center;
   background-repeat: no-repeat;
   width: 696px;
   height: 150px;
   overflow: hidden;
}

a.docRefreshViewLink
{
   padding: 8px 4px 0px 0px;
   display: block;
   outline: none;
}

a.docRefreshViewLink:link, a.docRefreshViewLink:visited, a.docRefreshViewLink:hover
{
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 12px;
   color: #515D6B;
   text-decoration: none;
}

#docUpdatePanel
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

.docFormItem
{
   margin: 4px;
   padding: 2px;
   background-color: #F8FCFD;
   border: 1px solid #CCD4DB;
}

#docMessagePanel
{
   position: absolute;
   border: 1px solid #65696C;
   background-color: #eeeeee;
   width: 250px;
   height: 72px;
   padding: 8px;
   margin-left: 440px;
   display: none;
   z-index: 1;
   -moz-border-radius: 7px;
}

.docMessagePanelClose
{
   margin: -4px -4px 2px 2px;
   float:right;
}

#docMessagePanelCloseImage
{
   cursor: pointer;
   display: block;
   height: 23px;
   width: 23px;
}

.docMessagePanelLabel
{
   color: #45494C;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 12px;
   font-weight: bold;
}
</style>