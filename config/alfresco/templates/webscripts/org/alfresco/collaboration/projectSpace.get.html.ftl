<script type="text/javascript" src="${url.context}/scripts/ajax/project_space.js"></script>

<div class="collabHeader">
   <span>
      <img id="refreshProjectSpace" src="${url.context}/images/icons/reset.gif" align="top" alt="Refresh">
      ${projectSpace.title} Summary
   </span>
</div>
<div class="collabContainer">

<table width="100%" cellpadding="0" cellspacing="0">
   <tr valign="top">
      <td width="1" style="background-color:#cacfd3;"></td>
      <td>
         <table width="100%" cellpadding="8" cellspacing="0">
            <tr valign="top">
               <td>
<#assign panelCount = 0>
<#list projectSpace.subSpaces?keys?sort as key>
   <#assign node = projectSpace.subSpaces[key]>
   <#assign summary = node.properties["cm:summaryWebscript"]!"">
                  <div class="projectSpace">
                     <span>
                        <a class="projectSpaceArrow" href="${url.context}${node.url}"><img src="${url.context}/images/parts/collab_arrow.png" height="16" width="16" alt=">"></a>
                        <div class="projectSpaceIcon">
                           <a href="${url.context}${node.url}"><img src="${url.context}${node.icon64}" height="64" width="64"></a>
                        </div>
                        <div><a class="projectSpaceTitle" href="${url.context}${node.url}">${node.name}</a></div>
                        <div class="projectSpaceSummary" rel="<#if summary != "">${url.context}${summary}?nodeRef=${node.nodeRef}</#if>"></div>
                     </span>
                  </div>
   <#assign panelCount = panelCount + 1>
   <#if (panelCount % 2 = 0)>
         <div style="clear:left;"></div>
   </#if>
</#list>      
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

<style>
/* Main Container elements */
#projectContainer {
   width: 100%;
}

#projectContainer a img {
   border: none;
}

#projectSummary {
   vertical-align: top;
}

#projectColleagues {
   vertical-align: top;
   width: 240px;
}

/* Project Summary */
.projectTitle {
	font-family: "Trebuchet MS", Verdana, Helvetica, sans-serif;
	font-size: medium;
	font-weight: bold;
	margin: -8px 0px 4px;
	float: left;
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

#refreshProjectSpace {
   cursor: pointer;
}

.collabContainer {
   min-height: 289px;
}

.projectSpace {
   background: url(${url.context}/images/parts/collab_gradleft.png) no-repeat left top;
   float: left;
   margin: 1em 0px;
   padding: 0px;
   width: 50%;
}
.projectSpace span {
   background: url(${url.context}/images/parts/collab_gradright.png) no-repeat right top;
   display: block;
   float: none;
   margin: 0px 0px 0px 16px;
   padding: 8px 0px 0px;
}

.projectSpaceArrow {
   float: right;
   margin: 0px 16px 0px 0px;
}

.projectSpaceIcon {
   float: left;
   padding-right: 8px;
}
a.projectSpaceTitle:link, a.projectSpaceTitle:visited {
   font-weight: bold;
   font-size: 10pt;
}

a.projectSpaceTitle:hover {
   font-weight: bold;
   font-size: 10pt;
   text-decoration: underline;
}

.projectSpaceSummary {
   margin: 0px 8px;
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
