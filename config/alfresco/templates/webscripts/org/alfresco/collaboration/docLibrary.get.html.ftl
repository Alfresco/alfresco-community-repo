<#assign doclib = companyhome.nodeByReference[args["nodeRef"]]>
<table id="collabContainer" cellspacing="0" cellpadding="2" width="100%">
   <tr>
      <td valign="top">

         <div class="collabHeader">
            <span>Recent Changes</span>
         </div>

         <div class="collabContent">
         
            <table width="100%" cellpadding="16" cellspacing="0">
               <tr valign="top">
                  <td>
   <#list doclib.childrenByXPath[".//*[subtypeOf('cm:content')]"] as child>
      <#if (dateCompare(child.properties["cm:modified"], date, 1000*60*60*24*7) == 1) || (dateCompare(child.properties["cm:created"], date, 1000*60*60*24*7) == 1)>
   	<div class="docLib">
         <a href="${url.context}${child.url}" target="new"><img src="${url.context}${child.icon16}" border=0></a>
   	   <div class="docLibName">
            <a href="${url.context}${child.url}" target="new">${child.properties.name}</a>
   	   </div>
   	   <div class="docLibProperty">
   	   <#if dateCompare(child.properties["cm:modified"], child.properties["cm:created"], 100, "==") == 1>
            Created
         <#else>
            Modified
         </#if>
   	   </div>
   	   <div class="docLibProperty">
            ${child.properties["cm:modified"]?datetime}
   	   </div>
   	</div>
      </#if>
   </#list>
                  </td>
               </tr>
            </table>

         </div>
         <div class="collabFooter">
            <span>&nbsp;</span>
         </div>

      </td>
   </tr>
</table>

<style>
/* Main Container elements */
#collabContainer {
   width: 100%;
}

.collabHeader {
   background: url(${url.context}/images/parts/collab_topleft.png) no-repeat left top;
   margin: 0px -1px;
   padding: 0px 0px 0px 8px;
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
   border-left: 1px solid #B9BEC4;
   border-right: 1px solid #B9BEC4;
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
</style>
