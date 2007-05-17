<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<link rel="stylesheet" href="${url.context}/css/main.css" TYPE="text/css">

<script type="text/javascript" src="/alfresco/scripts/ajax/yahoo/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/yahoo/connection/connection-min.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/mootools.v1.1.js"></script>
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

<table border=0 cellspacing=0 cellpadding=0 class="docTable">
<tr>
   <td align=center height=40>
      <table border=0 cellspacing=8 cellpadding=0>
         <tr>
            <th><a class="docfilterLink <#if filter=0>docfilterLinkSelected</#if>" href="${scripturl("?f=0&p=${path}")}">All Items</a></th>
            <th><a class="docfilterLink <#if filter=1>docfilterLinkSelected</#if>" href="${scripturl("?f=1&p=${path}")}">Word Documents</a></th>
            <th><a class="docfilterLink <#if filter=2>docfilterLinkSelected</#if>" href="${scripturl("?f=2&p=${path}")}">HTML Documents</a></th>
            <th><a class="docfilterLink <#if filter=3>docfilterLinkSelected</#if>" href="${scripturl("?f=3&p=${path}")}">PDF Documents</a></th>
            <th><a class="docfilterLink <#if filter=4>docfilterLinkSelected</#if>" href="${scripturl("?f=4&p=${path}")}">Recently Modified</a></th>
         </tr>
      </table>
   </td>
</tr>
<tr><td>
   <div id="docPanel">
      <#assign weekms=1000*60*60*24*7>
      <#assign count=0>
      <#list home.children?sort_by('name') as d>
         <#if d.isDocument>
            <#if (filter=0) ||
                 (filter=1 && d.mimetype="application/msword") ||
                 (filter=2 && d.mimetype="text/html") ||
                 (filter=3 && d.mimetype="application/pdf") ||
                 (filter=4 && (dateCompare(d.properties["cm:modified"],date,weekms) == 1 || dateCompare(d.properties["cm:created"], date, weekms) == 1))>
            <#assign count=count+1>
            <div class="docRow">
               <div class="docIcon">
                  <a href="${url.context}${d.url}" target="new"><img class="docIconImage" alt="" width="16" height="16" src="${url.context}${d.icon16?replace(".gif",".png")}" border=0></a>
               </div>
               <div style="display:none"><img class="docIconImage64" alt="" width="64" height="64" src="${url.context}${d.icon64}"></div>
               <div class="docItem">
                  ${d.name?html}
                  <span class="docInfo" onclick="event.cancelBubble=true; AlfNodeInfoMgr.toggle('${d.nodeRef}',this);">
                     <img src="${url.context}/images/icons/popup.gif" class="popupImage" width="16" height="16" />
                  </span>
               </div>
               <div class="docDetail">
                  <table cellpadding="2" cellspacing="0" border="0">
      	            <tr>
      	               <td>
      	                  <span class="docMetaprop">Description:</span>&nbsp;<span class="docMetadata"><#if d.properties.description?exists>${d.properties.description?html}<#else>&nbsp;</#if></span><br />
         	               <span class="docMetaprop">Modified:</span>&nbsp;<span class="docMetadata">${d.properties.modified?datetime}</span><br />
         	               <span class="docMetaprop">Modified By:</span>&nbsp;<span class="docMetadata">${d.properties.modifier}</span>
      	               </td>
      	               <td width="24">&nbsp;</td>
      	               <td>
      	                  <span class="docMetaprop">Created:</span>&nbsp;<span class="docMetadata">${d.properties.created?datetime}</span><br />
         	               <span class="docMetaprop">Created By:</span>&nbsp;<span class="docMetadata">${d.properties.creator}</span><br />
      	                  <span class="docMetaprop">Size:</span>&nbsp;<span class="docMetadata">${(d.size/1000)?string("0.##")} KB</span>
      	               </td>
      	            </tr>
      	         </table>
               </div>
               <div class="docResource doclistAjaxWait" id="${d.nodeRef}"></div>
            </div>
            </#if>
         </#if>
      </#list>
   </div>
</td>
</tr>
<tr>
<td>
   <div class="docFooter">
      Showing ${count} items(s)
   </div>
</td>
</tr>
</table>

<STYLE type="text/css">
a.docfilterLink:link, a.docfilterLink:visited
{
   color: #8EA1B3;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 13px;
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
   color: #168ECE;
   background-color: #EEF7FB;
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
   border-top: 1px solid #CCD4DB;
   border-bottom: 1px solid #CCD4DB;
   visibility: hidden;
}

.docRow
{
   padding-top: 4px;
   border-top: 1px solid #F8FCFD;
   border-bottom: 1px solid #CCD4DB;
}

.docFooter
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
   margin: 0px 0px 0px 0px;
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

.docMetapreview
{
   color: #515D6B;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   overflow: hidden;
   height: 140px;
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
   height: 28px;
   border: 1px solid #fff;
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

.docListAjaxWait
{
   background-image: url(${url.context}/images/icons/ajax_anim.gif);
   background-position: center;
   background-repeat: no-repeat;
   width: 696px;
   height: 150px;
   overflow: hidden;
}

</STYLE>