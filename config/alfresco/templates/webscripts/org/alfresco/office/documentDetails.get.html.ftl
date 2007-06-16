<#assign doc_actions="${url.serviceContext}/office/docActions">
<#if args.p?exists><#assign path=args.p><#else><#assign path=""></#if>
<#-- resolve the path (from Company Home) into a node -->
<#if path?starts_with("/Company Home")>
   <#if path?length=13>
      <#assign d=companyhome>
   <#elseif companyhome.childByNamePath[args.p[14..]]?exists>
      <#assign d=companyhome.childByNamePath[args.p[14..]]>
   <#else>
      <#assign d=companyhome>
   </#if>
<#else>
   <#assign d=companyhome>
</#if>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
   <title>Document Details</title>
   <link rel="stylesheet" type="text/css" href="${url.context}/css/office.css" />
<!--[if IE 6]>
   <link rel="stylesheet" type="text/css" href="${url.context}/css/office_ie6.css" />
<![endif]-->
   <script type="text/javascript" src="${url.context}/scripts/ajax/mootools.v1.1.js"></script>
   <script type="text/javascript" src="${url.context}/scripts/office/office_addin.js"></script>
</head>
<body>

<div id="tabBar">
   <ul>
      <li><a title="My Alfresco" href="${url.serviceContext}/office/myAlfresco?p=${path?url}"><span><img src="${url.context}/images/office/my_alfresco.gif" alt="My Alfresco" /></span></a></li>
      <li><a title="Browse Spaces and Documents" href="${url.serviceContext}/office/navigation?p=${path?url}"><span><img src="${url.context}/images/office/navigator.gif" alt="Browse Spaces and Documents" /></span></a></li>
      <li><a title="Search Alfresco" href="${url.serviceContext}/office/search?p=${path?url}"><span><img src="${url.context}/images/office/search.gif" alt="Search Alfresco" /></span></a></li>
      <li id="current"><a title="View Details" href="${url.serviceContext}/office/documentDetails?p=${path?url}"><span><img src="${url.context}/images/office/document_details.gif" alt="View Details" /></span></a></li>
      <li><a title="My Tasks" href="${url.serviceContext}/office/myTasks?p=${path?url}"><span><img src="${url.context}/images/office/my_tasks.gif" alt="My Tasks" /></span></a></li>
   </ul>
</div>

<div class="header">Current Document Details</div>

<div class="containerMedium">
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
                  <tr><td>Title:</td><td>${d.properties.title}</td></tr>
   <#else>
                  <tr><td>Title:</td><td>&nbsp;</td></tr>
   </#if>
   <#if d.properties.description?exists>
                  <tr><td>Description:</td><td>${d.properties.description}</td></tr>
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
   <#if d.hasAspect("cm:generalclassifiable")>
      <#list d.properties.categories as category>
                        ${companyhome.nodeByReference[category].name};
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

<div class="header">Version History
<#if d.isDocument>
                                    for ${d.name}
</#if>
</div>

<div id="versionList" class="containerMedium">
   <table width="265">
<#if d.isDocument >
   <#if hasAspect(d, "cm:versionable") == 1>
      <#assign versionRow=0>
      <#list d.versionHistory?sort_by("versionLabel")?reverse as record>
      <#assign versionRow=versionRow+1>
      <tr class="${(versionRow % 2 = 0)?string("odd", "even")}">
         <td valign="top">
            <a title="Open ${record.versionLabel}" href="$(url.context}${d.url}"><img src="${url.context}/images/office/document.gif" alt="Open ${record.versionLabel}" /></a>
         </td>
         <td>
            <a title="Open ${record.versionLabel}" href="#"><span style="font-weight:bold;">${record.versionLabel}</span></a><br />
            Author: ${record.creator}<br/>
            Date: ${record.createdDate?datetime}<br/>
         <#if record.description?exists>
            Notes: ${record.description}<br/>
         </#if>
<!--                       <a href="#" onclick="window.external.compareDocument('/alfresco${d.url}')" title="Compare with current">Compare with current</a><br/> -->
         </td>
      </tr>
   </#list>
   <#else>
      <tr>
         <td valign="top">
            The current document is not versioned.<br />
            <br />
            <ul>
               <li><a title="Make Versionable" href="#" onclick="OfficeAddin.runAction('${doc_actions}','makeversion','${d.id}', '');">
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

<div class="header">Document Actions</div>

<div id="documentActions">
   <div id="nonStatusText">
      <ul>
<#if d.isDocument>
   <#if d.isLocked >
   <#elseif hasAspect(d, "cm:workingcopy") == 1>
         <li>
            <a href="#" onclick="OfficeAddin.runAction('${doc_actions}','checkin','${d.id}', '');">
               <img src="${url.context}/images/office/checkin.gif" alt="Check In">
               Check In
            </a>
            <br />Check in the current document.
         </li>
   <#else>
         <li>
            <a href="#" onclick="OfficeAddin.runAction('${doc_actions}','checkout','${d.id}', '');">
               <img src="${url.context}/images/office/checkout.gif" alt="Check Out">
               Check Out
            </a>
            <br />Check out the current document to a working copy.
         </li>
   </#if>
         <li>
            <a href="${url.serviceContext}/office/myTasks?p=${path?url}&amp;w=new">
               <img src="${url.context}/images/office/new_workflow.gif" alt="Start Workflow" />
               Start Workflow
            </a>
            <br />Start Advanced Workflow for the current document.
         </li>
   <#if d.name?ends_with(".doc")>
         <li>
            <a href="#" onclick="OfficeAddin.runAction('${doc_actions}','makepdf','${d.id}', '');">
               <img src="${url.context}/images/office/makepdf.gif" alt="Transform to PDF" />
               Transform to PDF
            </a>
            <br />Transform the current document to Adobe PDF format.
         </li>
    </#if>
         <li>
            <a href="${url.context}/navigate/showOfficeAddin/workspace/SpacesStore/${d.id}?ticket=${session.ticket}" rel="_blank">
               <img src="${url.context}/images/office/document_details.gif" alt="Open Full Details" />
               Open Full Details
            </a>
            <br />Open the document details in the Alfresco Web Client.
         </li>
<#else>
         <li>
            <a title="Save to Alfresco" href="${url.serviceContext}/office/navigation?p=${path?url}">
               <img src="${url.context}/images/office/save_to_alfresco.gif" alt="Save to Alfresco" />
               Save to Alfresco
            </a>
            <br />Allows you to place the current document under Alfresco management.
         </li>
</#if>
      </ul>
   </div>
   
   <div id="statusText"></div>

</div>


</body>
</html>

