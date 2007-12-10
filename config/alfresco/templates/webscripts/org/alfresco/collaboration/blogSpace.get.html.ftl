<div id="collabContainer">

<#if blogSpace.actionResult != "">
   <div class="collabError">${blogSpace.actionResult}</div>
</#if>

   <div class="collabHeader">
      <span>Articles pending publishing (${blogSpace.pending?size})</span>
   </div>

   <div class="collabContent">
   
<table width="100%" cellpadding="0" cellspacing="0">
   <tr valign="top">
      <td width="1" style="background-color:#cacfd3;"></td>
      <td>

         <table width="100%" cellpadding="16" cellspacing="0">
            <tr valign="top">
               <td>
   
                  <table width="100%" cellpadding="0" cellspacing="0">
                     <tr class="blogRowHeader">
                        <td>&nbsp;</td>
                        <td>Name</td>
                        <td>Created on</td>
                        <td>Created by</td>
                        <td>Actions</td>
                     </tr>
<#assign rowNum = 0>
<#list blogSpace.pending as article>
   <#assign n = article.node>
   <#assign p = article.person>
   <#assign rowNum = rowNum + 1>
                  	<tr class="blogRowPost${(rowNum % 2 = 0)?string("Even", "Odd")}">
                  	   <td><img src="${url.context}${n.icon32}"></td>
                  	   <td>${n.name}</td>
                  	   <td>${n.properties["cm:created"]?datetime}</td>
                  	   <td>${p.properties["cm:firstName"]} ${p.properties["cm:lastName"]}</td>
                  	   <td>
                  	      <a class="blogAction" title="Post this article" href="${scripturl("?nodeRef=" + n.parent.nodeRef + "&n=" + n.nodeRef + "&a=p")}"><img src="${url.context}/images/icons/blog_post.png" alt="details"></a>
                           <a class="blogAction" title="Details" href="${url.context}/n/showDocDetails/${n.nodeRef.storeRef.protocol}/${n.nodeRef.storeRef.identifier}/${n.id}"><img src="${url.context}/images/icons/View_details.gif" alt="details"></a>
                  	   </td>
                  	</tr>
</#list>
                  </table>
               </td>
            </tr>
         </table>
      </td>
      <td width="1" style="background-color:#cacfd3;"></td>
   </tr>
</table>

   </div>
   <div class="collabFooter">
      <span>&nbsp;</span>
   </div>

   <div class="collabHeader">
      <span>Articles to be updated (${blogSpace.updates?size})</span>
   </div>

   <div class="collabContent">
   
<table width="100%" cellpadding="0" cellspacing="0">
   <tr valign="top">
      <td width="1" style="background-color:#b9bec4;"></td>
      <td>
         <table width="100%" cellpadding="16" cellspacing="0">
            <tr valign="top">
               <td>
   
                  <table width="100%" cellpadding="0" cellspacing="0">
                     <tr class="blogRowHeader">
                        <td>&nbsp;</td>
                        <td>Name</td>
                        <td>Published on</td>
                        <td>Modified on</td>
                        <td>Actions</td>
                     </tr>
<#assign rowNum = 0>
<#list blogSpace.updates as article>
   <#assign n = article.node>
   <#assign p = article.person>
   <#assign rowNum = rowNum + 1>
                  	<tr class="blogRowPost${(rowNum % 2 = 0)?string("Even", "Odd")}">
                  	   <td><img src="${url.context}${n.icon32}"></td>
                  	   <td>${n.name}</td>
                  	   <td>${n.properties["blg:lastUpdate"]?datetime}</td>
                  	   <td>${n.properties["cm:modified"]?datetime}</td>
                  	   <td>
                  	      <a class="blogAction" title="Update blog" href="${scripturl("?nodeRef=" + n.parent.nodeRef + "&n=" + n.nodeRef + "&a=u")}"><img src="${url.context}/images/icons/blog_update.png"></a>
                  	      <a class="blogAction" title="Remove from blog" href="${scripturl("?nodeRef=" + n.parent.nodeRef + "&n=" + n.nodeRef + "&a=r")}"><img src="${url.context}/images/icons/blog_remove.png"></a>
                           <a class="blogAction" title="Details" href="${url.context}/n/showDocDetails/${n.nodeRef.storeRef.protocol}/${n.nodeRef.storeRef.identifier}/${n.id}"><img src="${url.context}/images/icons/View_details.gif" alt="details"></a>
                  	   </td>
                  	</tr>
</#list>
                  </table>
               </td>
            </tr>
         </table>
      </td>
      <td width="1" style="background-color:#b9bec4;"></td>
   </tr>
</table>

   </div>
   <div class="collabFooter">
      <span>&nbsp;</span>
   </div>

</div>

<style>
/* Main Container elements */
#collabContainer {
   width: 100%;
}

.collabError {
   background-color: #FFFF90;
   border: 1px dashed red;
   font-weight: bold;
   color: red;
   padding: 4px;
   margin: 4px;
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

.blogRowHeader {
}
.blogRowHeader td {
   border-bottom: 1px solid #ccc;
   font-size: 12px;
   font-weight: bold;
}

.blogRowPostEven {
   background-color: #f0f0f0;
}
.blogRowPostOdd {
}

.blogAction img {
   border: none;
}
</style>