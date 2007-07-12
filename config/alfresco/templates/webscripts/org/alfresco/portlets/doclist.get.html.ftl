<link rel="stylesheet" href="${url.context}/css/main.css" TYPE="text/css">

<script type="text/javascript" src="/alfresco/scripts/ajax/yahoo/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/yahoo/connection/connection-min.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/mootools.v1.11.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/common.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/summary-info.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/doclist.js"></script>
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
<#else>
   <#assign query=args.q>
</#if>

<table border=0 cellspacing=0 cellpadding=0 class="docTable">
<tr>
   <td height="30" class="docHeader">
      <table border="0" cellspacing="6" cellpadding="0" width="100%">
         <tr>
            <th><a id="docFilter0" class="docfilterLink <#if filter=0>docfilterLinkSelected</#if>" href="#" onclick="MyDocs.filter(0); return false;">All Items</a></th>
            <th><a id="docFilter1" class="docfilterLink <#if filter=1>docfilterLinkSelected</#if>" href="#" onclick="MyDocs.filter(1); return false;">Word Documents</a></th>
            <th><a id="docFilter2" class="docfilterLink <#if filter=2>docfilterLinkSelected</#if>" href="#" onclick="MyDocs.filter(2); return false;">HTML Documents</a></th>
            <th><a id="docFilter3" class="docfilterLink <#if filter=3>docfilterLinkSelected</#if>" href="#" onclick="MyDocs.filter(3); return false;">PDF Documents</a></th>
            <th><a id="docFilter4" class="docfilterLink <#if filter=4>docfilterLinkSelected</#if>" href="#" onclick="MyDocs.filter(4); return false;">Recently Modified</a></th>
            <td align=right>
               <a href="#" onclick="MyDocs.start(); return false;" class="refreshViewLink"><img src="${url.context}/images/icons/reset.gif" border="0" width="16" height="16" style="vertical-align:-25%;padding-right:4px">Refresh</a>
            </td>
         </tr>
      </table>
   </td>
</tr>
<tr>
   <td>
      <div id="docUpdatePanel">
         <input class="docFormItem" type="button" value="OK" onclick="MyDocs.updateOK(this);">
         <input class="docFormItem" type="button" value="Cancel" onclick="MyDocs.closePopupPanel();">
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
   </td>
</tr>
<tr>
   <td>
      <div class="docFooter">
         <span class="docFooterText">Showing <span id="docCount">0</span> items(s)</span>
      </div>
   </td>
</tr>
</table>

<style type="text/css">
a.docfilterLink:link, a.docfilterLink:visited
{
   color: #8EA1B3;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 12px;
   font-weight: bold;
   text-decoration: none;
   padding-left: 4px;
   padding-right: 4px;
}

a.docfilterLink:hover
{
   color: #168ECE;
   background-color: #EEF7FB;
}

a.docfilterLinkSelected:link, a.docfilterLinkSelected:visited
{
   color: #0085CA;
}

.docTable
{
   background-color: #F8FCFD;
   border: 1px solid #CCD4DB;
}

#docPanel
{
   height: 320px;
   width: 716px;
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

.docItem
{
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 14px;
   color: #515D6B;
   padding: 0px 8px 6px 40px;
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
   background-color: #CCE7F3 !important;
   border-bottom: 1px solid #0092DD !important;
   border-top: 1px solid #0092DD !important;
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
   color: #515D6B;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   overflow: hidden;
   height: 144px;
   width: 410px;
   border: 1px solid #0092dd;
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
   float: left;
   display: block;
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

a.refreshViewLink:link, a.refreshViewLink:visited, a.refreshViewLink:hover
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
</style>