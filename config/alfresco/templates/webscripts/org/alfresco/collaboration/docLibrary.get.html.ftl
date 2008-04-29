<#assign doclib = companyhome.nodeByReference[args["nodeRef"]]>
<#assign datetimeformat="EEE, dd MMM yyyy HH:mm">
<div id="collabContainer">

   <div class="collabHeader">
      <span>Recent Changes</span>
   </div>

<table width="100%" cellpadding="0" cellspacing="0">
   <tr valign="top">
      <td width="1" style="background-color:#b9bec4;"></td>
      <td>

   <div class="collabContent">

   <#assign count=0>
   <#list doclib.childrenByXPath[".//*[subtypeOf('cm:content')]"] as c>
      <#assign count=count+1>
      <#assign curl=url.serviceContext + c.serviceUrl>
      <div class="collab${(count%2=0)?string("RowEven", "RowOdd")}">
         <div style="float:left">
            <a href="${curl}" target="new"><img src="${url.context}${c.icon32}" width="32" height="32" border="0" alt="${c.name?html}" title="${c.name?html}"></a>
         </div>
         <div style="margin-left:36px;padding: 4px 0px 4px 0px">
            <div>
               <div class="collabNodeActions">
                  <a href="${url.context}/n/showDocDetails/${c.nodeRef.storeRef.protocol}/${c.nodeRef.storeRef.identifier}/${c.id}" alt="Details" title="Details"><img src="${url.context}/images/icons/View_details.gif" border="0"></a>
               </div>
               <a class="collabNodeLink" href="${curl}" target="new">${c.name?html}</a>
            </div>
            <div>
               <span class="metaData"><#if c.properties.description?exists>${c.properties.description?html}</#if></span>
            </div>
            <div>
               <span class="metaTitle">Modified:</span>&nbsp;<span class="metaData">${c.properties.modified?string(datetimeformat)}</span>&nbsp;
               <span class="metaTitle">Modified&nbsp;By:</span>&nbsp;<span class="metaData">${c.properties.modifier}</span>
               <span class="metaTitle">Size:</span>&nbsp;<span class="metaData">${(c.size/1000)?string("0.##")}&nbsp;KB</span>
            </div>
         </div>
      </div>
   </#list>

   </div>

      </td>
      <td width="1" style="background-color:#b9bec4;"></td>
   </tr>
</table>

   <div class="collabFooter">
      <span>&nbsp;</span>
   </div>
</div>

<style>
/* Main Container elements */
#collabContainer {
   width: 100%;
}

.collabHeader {
   background: url(${url.context}/images/parts/collab_topleft.png) no-repeat left top;
   margin: 0px;
   padding: 0px 0px 0px 2px;
}
.collabHeader span {
   background: url(${url.context}/images/parts/collab_topright.png) no-repeat right top;
   display: block;
   float: none;
   padding: 5px 15px 4px 6px;
   font-weight: bold;
   font-size: 10pt;
}

.collabContent {
   padding: 8px;
}

.collabFooter {
   background: url(${url.context}/images/parts/collab_bottomleft.png) no-repeat left top;
   margin: 0px;
   padding: 0px 0px 0px 4px;
}

.collabFooter span {
   background: url(${url.context}/images/parts/collab_bottomright.png) no-repeat right top;
   display: block;
   float: none;
   padding: 5px 15px 4px 6px;
}

div.collabRowOdd {
   padding: 4px 2px;
}

div.collabRowEven {
   padding: 4px 2px;
   background-color: #F1F7FD;
}

span.metaTitle {
   font-size: 11px;
   color: #666677;
}

span.metaData {
   font-size: 11px;
   color: #515D6B;
}

a.collabNodeLink, a.collabNodeLink:hover {
   font-size: 12px;
   font-weight: bold;
}

div.collabNodeActions {
   float: right;
   padding: 0px 4px;
}
</style>
