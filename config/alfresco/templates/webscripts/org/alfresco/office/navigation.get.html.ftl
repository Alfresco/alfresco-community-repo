<#assign doc_actions="${url.context}/service/office/docActions">
<#if node.isDocument>
   <#assign thisSpace = node.parent>
<#else>
   <#assign thisSpace = node>
</#if>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
	<title>Browse Spaces and Documents</title>
	<link rel="stylesheet" type="text/css" href="${url.context}/css/office.css" />
   <script type="text/javascript" src="${url.context}/scripts/ajax/mootools.v1.1.js"></script>
	<script type="text/javascript" src="${url.context}/scripts/office/office_addin.js"></script>
</head>
<body>

<div id="tabBar">
   <ul>
      <li><a title="My Alfresco" href="${url.context}/service/office/myAlfresco?p=${path}"><span><img src="${url.context}/images/office/my_alfresco.gif" alt="My Alfresco" /></span></a></li>
      <li id="current"><a title="Browse Spaces and Documents" href="${url.context}/service/office/navigation?p=${path}&n=${node.id}"><span><img src="${url.context}/images/office/navigator.gif" alt="Browse Spaces and Documents" /></span></a></li>
      <li><a title="Search Alfresco" href="${url.context}/service/office/search?p=${path}"><span><img src="${url.context}/images/office/search.gif" alt="Search Alfresco" /></span></a></li>
      <li><a title="View Details" href="${url.context}/service/office/documentDetails?p=${path}"><span><img src="${url.context}/images/office/document_details.gif" alt="View Details" /></span></a></li>
      <li><a title="My Tasks" href="${url.context}/service/office/myTasks?p=${path}"><span><img src="${url.context}/images/office/my_tasks.gif" alt="My Tasks" /></span></a></li>
   </ul>
</div>

<div id="currentSpaceInfo">
   <table width="100%">
      <tr>
         <td width="10">In:</td>
         <td width="16"><img src="${url.context}${thisSpace.icon16}" alt="${thisSpace.name}" /></td>
         <td>
            <span class="bold">${thisSpace.name}</span>
<#if thisSpace.properties.description?exists>
            <br />${thisSpace.properties.description}
</#if>
         </td>
         <td width="34" align="right">
<#if thisSpace=companyhome>
<#else>
            <a title="Up to Parent Space" href="${url.context}/service/office/navigation?p=${path}&n=${thisSpace.parent.id}">
               <img src="${url.context}/images/office/go_up.gif" alt="Up to Parent Space" />
               <span>Up</span>
            </a>
</#if>
         </td>
      </tr>
   </table>
</div>

<div class="header">Spaces in ${thisSpace.name}<span class="headerExtra"><span class="toggle"></span></span></div>

<div id="spaceList" class="listMedium">
   <table>
<#assign spacesFound = 0>
<#list thisSpace.children as child>
   <#if child.isContainer>
      <#assign spacesFound = spacesFound + 1>
      <tr class="${(spacesFound % 2 = 0)?string("odd", "even")}">
         <td>
            <a href="${url.context}/service/office/navigation?p=${path}&n=${child.id}"><img src="${url.context}${child.icon32}" alt="Open ${child.name}" /></a>
         </td>
         <td width="100%">
            <a href="${url.context}/service/office/navigation?p=${path}&n=${child.id}" title="Open ${child.name}">
               <span class="bold">${child.name}</span>
            </a>
      <#if child.properties.description?exists>
      		<br/>${child.properties.description}
      </#if>
         </td>
      </tr>
   </#if>
</#list>
<#if spacesFound = 0>
      <tr>
         <td>(No subspaces)</td>
      </tr>
</#if>
   </table>
</div>

<div class="header">Documents in ${thisSpace.name}<span class="headerExtra"><span class="toggle"></span></span></div>

<div id="documentList" class="listMedium">
   <table>
<#assign documentsFound = 0>
<#list thisSpace.children as child>
   <#if child.isDocument>
      <#assign documentsFound = documentsFound + 1>
      <#assign webdavPath = (child.displayPath?substring(13) + '/' + child.name)?url('ISO-8859-1')?replace('%2F', '/')?replace('\'', '\\\'') />
      <tr class="${(documentsFound % 2 = 0)?string("odd", "even")}">
         <td valign="top">
      <#if child.name?ends_with(".doc")>
            <a href="#" onClick="window.external.openDocument('${webdavPath}')"><img src="${url.context}${child.icon32}" alt="Open ${child.name}" /></a>
      <#else>
            <a href="${url.context}${child.url}?ticket=${session.ticket}" target="_blank"><img src="${url.context}${child.icon32}" alt="Open ${child.name}" /></a>
      </#if>
         </td>
         <td width="100%">
      <#if child.name?ends_with(".doc")>
            <a href="#" onClick="window.external.openDocument('${webdavPath}')" title="Open ${child.name}">
               <span class="bold">${child.name}</span>
            </a><br/>
      <#else>
            <a href="${url.context}${child.url}?ticket=${session.ticket}" target="_blank" title="Open ${child.name}">
               <span class="bold">${child.name}</span>
            </a><br/>
      </#if>
      <#if child.properties.description?exists>
		      ${child.properties.description}<br/>
      </#if>
            Modified: ${child.properties.modified?datetime}, Size: ${(child.size / 1024)?int}Kb<br/>
      <#if child.isLocked >
            <img src="${url.context}/images/office/lock.gif" style="padding:3px 6px 2px 0px;" alt="Locked">
      <#elseif hasAspect(child, "cm:workingcopy") == 1>
            <a href="#" onClick="OfficeAddin.runAction('${doc_actions}','checkin','${child.id}', '');"><img src="${url.context}/images/office/checkin.gif" style="padding:3px 6px 2px 0px;" alt="Check In" title="Check In"></a>
      <#else>
            <a href="#" onClick="OfficeAddin.runAction('${doc_actions}','checkout','${child.id}', '');"><img src="${url.context}/images/office/checkout.gif" style="padding:3px 6px 2px 0px;" alt="Check Out" title="Check Out"></a>
      </#if>
            <a href="#" onClick="OfficeAddin.runAction('${doc_actions}','makepdf','${child.id}', '');"><img src="${url.context}/images/office/makepdf.gif" style="padding:3px 6px 2px 0px;" alt="Make PDF..." title="Make PDF"></a>
      <#if !child.isLocked >
            <a href="#" onClick="OfficeAddin.runAction('${doc_actions}','delete','${child.id}', 'Are you sure you want to delete this document?');"><img src="${url.context}/images/office/delete.gif" style="padding:3px 6px 2px 0px;" alt="Delete..." title="Delete"></a>
      </#if>
         </td>
      </tr>
   </#if>
</#list>
<#if documentsFound = 0>
      <tr>
         <td>(No documents)</td>
      </tr>
</#if>
   </table>
</div>

<div class="header">Document Actions</div>

<div id="documentActionsNavigation">
   <div id="nonStatusText">
      <ul>
<#assign currentPath = thisSpace.displayPath  + '/' + thisSpace.name />
<#assign webdavPath = currentPath?substring(13)?url('ISO-8859-1')?replace('%2F', '/')?replace('\'', '\\\'') />
         <li><a href="#" onClick="window.external.saveToAlfresco('${webdavPath}')"><img src="${url.context}/images/office/save_to_alfresco.gif" style="padding-right:6px;" alt="Save to Alfresco">Save to Alfresco</a></li>
    <#if args.search?exists>
         <li><a href="${url.context}/service/office/search?p=${path}&searchagain=${args.search}&maxresults=${args.maxresults}"><img src="${url.context}/images/office/search_again.gif" style="padding-right:6px;" alt="Back to results">Back to search results</a></li>
    </#if>
      </ul>
   </div>
   
   <div id="statusText"></div>
</div>

</body>
</html>