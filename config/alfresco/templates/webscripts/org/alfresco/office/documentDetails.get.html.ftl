<#assign doc_actions="${url.context}/service/office/docActions">
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
   <script type="text/javascript" src="${url.context}/scripts/ajax/mootools.v1.1.js"></script>
   <script type="text/javascript" src="${url.context}/scripts/office/office_addin.js"></script>
</head>
<body>

<div id="tabBar">
   <ul>
      <li><a title="My Alfresco" href="${url.context}/service/office/myAlfresco?p=${path}"><span><img src="${url.context}/images/office/my_alfresco.gif" alt="My Alfresco" /></span></a></li>
      <li><a title="Browse Spaces and Documents" href="${url.context}/service/office/navigation?p=${path}"><span><img src="${url.context}/images/office/navigator.gif" alt="Browse Spaces and Documents" /></span></a></li>
      <li><a title="Search Alfresco" href="${url.context}/service/office/search?p=${path}"><span><img src="${url.context}/images/office/search.gif" alt="Search Alfresco" /></span></a></li>
      <li id="current"><a title="View Details" href="${url.context}/service/office/documentDetails?p=${path}"><span><img src="${url.context}/images/office/document_details.gif" alt="View Details" /></span></a></li>
      <li><a title="My Tasks" href="${url.context}/service/office/myTasks?p=${path}"><span><img src="${url.context}/images/office/my_tasks.gif" alt="My Tasks" /></span></a></li>
   </ul>
</div>

<div class="header">Current Document Details</div>

<div class="listMedium">
   <table>
      <tbody>
         <tr>
            <td valign="top">
<#if d.isDocument>
               <img src="${url.context}${d.icon32}" border="0" alt="${d.name}" />
            </td>
            <td width="100%" style="padding-top: 4px;">
               <span style="font-weight:bold; vertical-align: top;">${d.name}
   <#if d.isLocked >
                  <img src="${url.context}/images/office/lock.gif" alt="Locked" title="Locked" style="margin: -2px 0px;" />
   </#if>
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

<div class="listMedium">
   <table>
<#if d.isDocument >
   <#if hasAspect(d, "cm:versionable") == 1>
      <#assign versionRow=0>
      <#list d.versionHistory?sort_by("versionLabel")?reverse as record>
      <#assign versionRow=versionRow+1>
      <tr class="${(versionRow % 2 = 0)?string("odd", "even")}">
         <td valign="top">
            <a title="Open ${record.versionLabel}" href="$(url.context}${d.url}"><img src="${url.context}/images/office/document.gif" alt="Open ${record.versionLabel}" /></a>
         </td>
         <td width="100%">
            <a title="Open ${record.versionLabel}" href="#"><span style="font-weight:bold;">${record.versionLabel}</span></a><br />
            Author: ${record.creator}<br/>
            Date: ${record.createdDate?datetime}<br/>
         <#if record.description?exists>
            Notes: ${record.description}<br/>
         </#if>
<!--                       <a href="#" onClick="window.external.compareDocument('/alfresco${d.url}')" title="Compare with current">Compare with current</a><br/> -->
         </td>
      </tr>
   </#list>
   <#else>
      <tr>
         <td valign="top">
            The current document is not versioned.<br />
            <br />
            <ul>
               <li><a title="Make Versionable" href="#" onClick="OfficeAddin.runAction('${doc_actions}','makeversion','${d.id}', '');">
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
<#if d.isDocument>
      <ul>
         <#if d.isLocked >
         <#elseif hasAspect(d, "cm:workingcopy") == 1>
         <li><a href="#" onClick="OfficeAddin.runAction('${doc_actions}','checkin','${d.id}', '');"><img src="${url.context}/images/office/checkin.gif" style="padding-right:6px;" alt="Check In">Check In</a></li>
         <#else>
         <li><a href="#" onClick="OfficeAddin.runAction('${doc_actions}','checkout','${d.id}', '');"><img src="${url.context}/images/office/checkout.gif" style="padding-right:6px;" alt="Check Out">Check Out</a></li>
         </#if>
         <li><a href="#" onClick="OfficeAddin.runAction('${doc_actions}','makepdf','${d.id}', '');"><img src="${url.context}/images/office/makepdf.gif" style="padding-right:6px;" alt="Transform to PDF">Transform to PDF</a></li>
         <li><a href="${url.context}/navigate/showOfficeAddin/workspace/SpacesStore/${d.id}?ticket=${session.ticket}" target="_blank"><img src="${url.context}/images/office/document_details.gif" border="0" style="padding-right:6px;" alt="Open Full Details">Open Full Details</a></li>
         <li><a href="${url.context}/service/office/myTasks?p=${path}&w=new"><img src="${url.context}/images/office/new_workflow.gif" style="padding-right:6px;" alt="Start Workflow">Start Workflow</a></li>
      </ul>

<#else>
      No actions available.
</#if>
   </div>
   
   <div id="statusText"></div>

</div>


</body>
</html>

