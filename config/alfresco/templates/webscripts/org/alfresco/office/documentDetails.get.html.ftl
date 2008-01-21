<#assign doc_actions="${url.serviceContext}/office/docActions">

<#if args.p?exists><#assign path=args.p><#else><#assign path=""></#if>
<#if args.e?exists><#assign extn=args.e><#else><#assign extn="doc"></#if><#assign extnx=extn+"x">
<#if args.n?exists><#assign nav=args.n><#else><#assign nav=""></#if>
<#-- resolve the path (from Company Home) into a node -->
<#if companyhome.childByNamePath[path]?exists>
   <#assign d=companyhome.childByNamePath[path]>
<#else>
   <#assign d=companyhome>
</#if>
<#assign defaultQuery="?p=" + path?url + "&e=" + extn + "&n=" + nav>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
   <title>${message("office.title.document_details")}</title>
   <link rel="stylesheet" type="text/css" href="${url.context}/css/office.css" />
<!--[if IE 6]>
   <link rel="stylesheet" type="text/css" href="${url.context}/css/office_ie6.css" />
<![endif]-->
   <script type="text/javascript" src="${url.context}/scripts/ajax/mootools.v1.11.js"></script>
   <script type="text/javascript" src="${url.context}/scripts/office/office_addin.js"></script>
   <script type="text/javascript" src="${url.context}/scripts/office/doc_details.js"></script>
   <script type="text/javascript">//<![CDATA[
      OfficeAddin.defaultQuery = '${defaultQuery}';
   //]]></script>
</head>
<body>

<div class="tabBar">
   <ul>
      <li><a title="${message("office.title.my_alfresco")}" href="${url.serviceContext}/office/myAlfresco${defaultQuery?html}"><span><img src="${url.context}/images/office/my_alfresco.gif" alt="${message("office.title.my_alfresco")}" /></span></a></li>
      <li><a title="${message("office.title.navigation")}" href="${url.serviceContext}/office/navigation${defaultQuery?html}"><span><img src="${url.context}/images/office/navigator.gif" alt="${message("office.title.navigation")}" /></span></a></li>
      <li><a title="${message("office.title.search")}" href="${url.serviceContext}/office/search${defaultQuery?html}"><span><img src="${url.context}/images/office/search.gif" alt="${message("office.title.search")}" /></span></a></li>
      <li id="current"><a title="${message("office.title.document_details")}" href="${url.serviceContext}/office/documentDetails${defaultQuery?html}"><span><img src="${url.context}/images/office/document_details.gif" alt="${message("office.title.document_details")}" /></span></a></li>
      <li><a title="${message("office.title.my_tasks")}" href="${url.serviceContext}/office/myTasks${defaultQuery?html}"><span><img src="${url.context}/images/office/my_tasks.gif" alt="${message("office.title.my_tasks")}" /></span></a></li>
      <li><a title="${message("office.title.document_tags")}" href="${url.serviceContext}/office/tags${defaultQuery?html}"><span><img src="${url.context}/images/office/tag.gif" alt="${message("office.title.document_tags")}" /></span></a></li>
   </ul>
</div>

<div class="header">Current Document Details</div>

<div class="containerMedium">
   <div id="nonStatusText">
      <table width="265">
         <tbody>
            <tr>
               <td valign="top">
<#if d.isDocument>
                  <img src="${url.context}${d.icon32}" alt="${d.name}" />
               </td>
               <td style="padding-top: 4px;">
                  <span style="font-weight:bold; vertical-align: top;">
   <#if d.isLocked >
                     <img src="${url.context}/images/office/lock.gif" alt="Locked" title="Locked" style="margin: -2px 0px;" />
   </#if>
                     ${d.name}
                  </span>
                  <br />
                  <table style="margin-top: 4px;">
   <#if d.properties.title?exists>
                     <tr><td valign="top">Title:</td><td>${d.properties.title}</td></tr>
   <#else>
                     <tr><td valign="top">Title:</td><td>&nbsp;</td></tr>
   </#if>
   <#if d.properties.description?exists>
                     <tr><td valign="top">Description:</td><td>${d.properties.description}</td></tr>
   <#else>
                     <tr><td valign="top">Description:</td><td>&nbsp;</td></tr>
   </#if>
                     <tr><td>Creator:</td><td>${d.properties.creator}</td></tr>
                     <tr><td>Created:</td><td>${d.properties.created?datetime}</td></tr>
                     <tr><td>Modifier:</td><td>${d.properties.modifier}</td></tr>
                     <tr><td>Modified:</td><td>${d.properties.modified?datetime}</td></tr>
                     <tr><td>Size:</td><td>${d.size / 1024} Kb</td></tr>
                     <tr><td valign="top">Categories:</td>
                        <td>
   <#if d.properties.categories?exists>
      <#list d.properties.categories as category>
                           ${category.name}<#if category != d.properties.categories?last>,</#if>
      </#list>
   <#else>
                           None.
   </#if>
                        </td>
                     </tr>
                  </table>
<#else>
                  The current document is not managed by Alfresco.
</#if>
               </td>
            </tr>
         </tbody>
      </table>
   </div>
   <div id="statusText"></div>
</div>

<div class="tabBarInline">
   <ul>
      <li class="current"><a id="tabLinkTags" title="Document Tags" href="#"><span><img src="${url.context}/images/office/document_tag.gif" alt="Document Tags" /></span></a></li>
      <li><a id="tabLinkVersion" title="Version History" href="#"><span><img src="${url.context}/images/office/version.gif" alt="Version History" /></span></a></li>
   </ul>
</div>

<div id="panelTags" class="tabPanel">
   <div class="tabHeader">Tags<#if d.isDocument> for ${d.name}</#if></div>
   <div id="tagList" class="containerTabMedium">
   <#if d.isDocument >
      <div class="addTagIcon"></div>
      <div id="addTagLinkContainer">
         <a href="#" onclick="OfficeDocDetails.showAddTagForm(); return false;">Add a tag</a>
      </div>
      <div id="addTagFormContainer">
         <form id="addTagForm" action="#" onsubmit="return OfficeDocDetails.addTag('${d.id}', this.tag.value);">
            <fieldset class="addTagFieldset">
               <input id="addTagBox" name="tag" type="text" />
               <input class="addTagImage" type="image" src="${url.context}/images/office/action_successful.gif" onclick="return ($('addTagBox').value.length > 0);" />
               <input class="addTagImage" type="image" src="${url.context}/images/office/action_failed.gif" onclick="return OfficeDocDetails.hideAddTagForm();" />
            </fieldset>
         </form>
      </div>
      <#if d.hasAspect("cm:taggable")>
         <#if (d.properties["cm:taggable"]?size > 0)>
            <#list d.properties["cm:taggable"]?sort_by("name") as tag>
               <#if tag?exists>
      <div class="tagListEntry">
         <a class="tagListDelete" href="#" title="Remove tag &quot;${tag.name}&quot;" onclick="OfficeDocDetails.removeTag('${d.id}', '${tag.name}');">[x]</a>
         <a class="tagListName" href="${url.serviceContext}/office/tags${defaultQuery?html}&amp;tag=${tag.name}">${tag.name}</a>
      </div>
               </#if>
            </#list>
         </#if>
      </#if>
   <#else>
      <table width="265">
         <tr>
            <td valign="top">
               The current document is not managed by Alfresco.
            </td>
         </tr>
      </table>
   </#if>
   </div>
</div>

<div id="panelVersion" class="tabPanel tabPanelHidden">
   <div class="tabHeader">Version History<#if d.isDocument> for ${d.name}</#if></div>
   <div id="versionList" class="containerTabMedium">
      <table width="265">
   <#if d.isDocument >
      <#if d.hasAspect("cm:versionable")>
         <#assign versionRow=0>
         <#list d.versionHistory?sort_by("versionLabel")?reverse as record>
            <#assign versionRow=versionRow+1>
         <tr class="${(versionRow % 2 = 0)?string("odd", "even")}">
            <td valign="top">
               <a title="Open ${record.versionLabel}" href="${url.context}${record.url}?ticket=${session.ticket}"><img src="${url.context}/images/office/document.gif" alt="Open ${record.versionLabel}" /></a>
            </td>
            <td>
               <a title="Open ${record.versionLabel}" href="${url.context}${record.url}?ticket=${session.ticket}"><span style="font-weight:bold;">${record.versionLabel}</span></a><br />
               Author: ${record.creator}<br />
               Date: ${record.createdDate?datetime}<br />
            <#if record.description?exists>
               Notes: ${record.description}<br />
            </#if>
            <#-- Only Word supports document compare -->
            <#if extn = "doc">
               <a class="bold" href="#" onclick="window.external.compareDocument('${record.url}')" title="Compare with current">Compare with current</a><br />
            </#if>
            </td>
         </tr>
         </#list>
      <#else>
         <tr>
            <td valign="top">
               The current document is not versioned.<br />
               <br />
               <ul>
                  <li><a title="Make Versionable" href="#" onclick="OfficeAddin.getAction('${doc_actions}','makeversion','${d.id}',null,null,'version=1');">
                     <img src="${url.context}/images/office/make_versionable.gif" alt="Make Versionable" /> Make Versionable
                  </a></li>
               </ul>
            </td>
         </tr>
      </#if>
   <#else>
         <tr>
            <td valign="top">
               The current document is not managed by Alfresco.
            </td>
         </tr>
   </#if>
      </table>
   </div>
</div>

<div class="header">Document Actions</div>

<div id="documentActions" class="actionsPanel">
   <ul>
<#if d.isDocument>
   <#if d.isLocked >
   <#elseif d.hasAspect("cm:workingcopy")>
      <li>
         <a href="#" onclick="OfficeAddin.getAction('${doc_actions}','checkin','${d.id}');">
            <img src="${url.context}/images/office/checkin.gif" alt="Check In">
            Check In
         </a>
         <br />Check in the current document.
      </li>
   <#else>
      <li>
         <a href="#" onclick="OfficeAddin.getAction('${doc_actions}','checkout','${d.id}');">
            <img src="${url.context}/images/office/checkout.gif" alt="Check Out" />
            Check Out
         </a>
         <br />Check out the current document to a working copy.
      </li>
   </#if>
      <li>
         <a href="${url.serviceContext}/office/myTasks${defaultQuery?html}&amp;w=new">
            <img src="${url.context}/images/office/new_workflow.gif" alt="Start Workflow" />
            Start Workflow
         </a>
         <br />Start Advanced Workflow for the current document.
      </li>
   <#if d.name?ends_with(extn) || d.name?ends_with(extnx)>
      <li>
         <a href="#" onclick="OfficeAddin.getAction('${doc_actions}','makepdf','${d.id}');">
            <img src="${url.context}/images/office/makepdf.gif" alt="Transform to PDF" />
            Transform to PDF
         </a>
         <br />Transform the current document to Adobe PDF format.
      </li>
    </#if>
      <li>
         <a href="${url.context}/navigate/showDocDetails/workspace/SpacesStore/${d.id}?ticket=${session.ticket}" rel="_blank">
            <img src="${url.context}/images/office/document_details.gif" alt="Open Full Details" />
            Open Full Details
         </a>
         <br />Open the document details in the Alfresco Web Client.
      </li>
<#else>
      <li>
         <a title="Save to Alfresco" href="${url.serviceContext}/office/navigation${defaultQuery?html}">
            <img src="${url.context}/images/office/save_to_alfresco.gif" alt="Save to Alfresco" />
            Save to Alfresco
         </a>
         <br />Allows you to place the current document under Alfresco management.
      </li>
</#if>
   </ul>
</div>

<#if args.version?exists>
<script>
   window.addEvent("domready", function(){$('tabLinkVersion').fireEvent('click');});
</script>
</#if>

</body>
</html>
