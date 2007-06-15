<#assign doc_actions="${url.serviceContext}/office/docActions">
<#if args.p?exists><#assign path=args.p><#else><#assign path=""></#if>
<#if args.n?exists><#assign node=args.n><#else><#assign node=companyhome></#if>
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
	<title>My Alfresco</title>
	<link rel="stylesheet" type="text/css" href="${url.context}/css/office.css" />
   <script type="text/javascript" src="${url.context}/scripts/ajax/mootools.v1.1.js"></script>
	<script type="text/javascript" src="${url.context}/scripts/office/office_addin.js"></script>
	<script type="text/javascript" src="${url.context}/scripts/office/my_alfresco.js"></script>
</head>
<body>

<div id="tabBar">
   <ul>
      <li id="current"><a title="My Alfresco" href="${url.serviceContext}/office/myAlfresco?p=${path?url}"><span><img src="${url.context}/images/office/my_alfresco.gif" alt="My Alfresco" /></span></a></li>
      <li><a title="Browse Spaces and Documents" href="${url.serviceContext}/office/navigation?p=${path?url}&amp;n=${d.id}"><span><img src="${url.context}/images/office/navigator.gif" alt="Browse Spaces and Documents" /></span></a></li>
      <li><a title="Search Alfresco" href="${url.serviceContext}/office/search?p=${path?url}"><span><img src="${url.context}/images/office/search.gif" alt="Search Alfresco" /></span></a></li>
      <li><a title="View Details" href="${url.serviceContext}/office/documentDetails?p=${path?url}"><span><img src="${url.context}/images/office/document_details.gif" alt="View Details" /></span></a></li>
      <li><a title="My Tasks" href="${url.serviceContext}/office/myTasks?p=${path?url}"><span><img src="${url.context}/images/office/my_tasks.gif" alt="My Tasks" /></span></a></li>
   </ul>
</div>

<div class="header">My Checked Out Documents<span class="headerExtra"><span class="toggle">&nbsp;</span></span></div>

<div id="checkedoutList" class="containerMedium togglePanel">
   <table width="265">
<#assign rowNum=0>
<#assign query="@cm\\:workingCopyOwner:${person.properties.userName}">
   <#list companyhome.childrenByLuceneSearch[query] as child>
      <#if child.isDocument>
      <#assign rowNum=rowNum+1>
      <tr class="checkedoutItem ${(rowNum % 2 = 0)?string("odd", "even")}">
         <td valign="top">
            <img src="${url.context}${child.icon32}" alt="${child.name}" />
         </td>
         <td style="line-height:16px;">
         <#if child.name?ends_with(".doc")>
            <#assign webdavPath = (child.displayPath?substring(13) + '/' + child.name)?url('ISO-8859-1')?replace('%2F', '/')?replace('\'', '\\\'') />
            <a href="#" onclick="window.external.openDocument('${webdavPath}')" title="Open ${child.name}" style="font-weight: bold;">${child.name}</a><br/>
         <#else>
            <a href="${url.context}${child.url}?ticket=${session.ticket}" title="Open ${child.name}" style="font-weight: bold;">${child.name}</a><br/>
         </#if>
         <#if child.properties.description?exists>
            ${child.properties.description}<br/>
         </#if>
            Modified: ${child.properties.modified?datetime} (${(child.size / 1024)?int}Kb)<br/>
            <a href="#" onclick="OfficeAddin.runAction('checkin','${child.id}', '');"><img src="${url.context}/images/office/checkin.gif" style="padding:3px 6px 2px 0px;" alt="Check In" title="Check In" /></a>
            <a href="#" onclick="OfficeAddin.runAction('makepdf','${child.id}', '');"><img src="${url.context}/images/office/makepdf.gif" style="padding:3px 6px 2px 0px;" alt="Make PDF..." title="Make PDF" /></a>
         </td>
      </tr>
      </#if>
   </#list>
   <#if rowNum = 0>
      <tr>
         <td class="noItems">(No documents)</td>
      </tr>
   </#if>
   </table>
</div>

<div class="header">My Tasks<span class="headerExtra"><span class="taskKey"><img src="${url.context}/images/office/task_overdue.gif" alt="overdue" />=overdue, <img src="${url.context}/images/office/task_today.gif" alt="due today" />=due today</span><span class="toggle">&nbsp;</span></span></div>

<div id="taskList" class="containerMedium togglePanel">
   <table width="265">
<#assign taskNum=0>
<#list workflow.assignedTasks?sort_by('startDate') as t>
   <#assign taskNum=taskNum+1>
      <tr id="${t.id?replace("$", ".")}" class="taskItem ${(taskNum % 2 = 0)?string("odd", "even")}">
         <td>
   <#assign hasDue=t.properties["bpm:dueDate"]?exists>
   <#if hasDue>
      <#assign due=t.properties["bpm:dueDate"]>
      <#-- items due today? -->
      <#if (dateCompare(date?date, due?date, 0, "==") == 1)>
            <img src="${url.context}/images/office/task_today.gif" alt="due today" />
      <#-- items overdue? -->
      <#elseif (dateCompare(date?date, due?date) == 1)>
            <img src="${url.context}/images/office/task_overdue.gif" alt="overdue" />
      </#if>
   <#else>
            &nbsp;
   </#if>
         </td>
         <td>
            ${t.description?html} (${t.type?html})
            <#if hasDue>
               <br />Due date: ${due?date}
            <#else>
               <br />(No due date)
            </#if>
         </td>
      </tr>
</#list>
<#if taskNum = 0>
      <tr>
         <td class="noItems">(No tasks)</td>
      </tr>
</#if>
   </table>
</div>

<div class="header">Actions</div>

<div id="documentActions">
   <div id="nonStatusText">
      <ul>
<#if d.isDocument>
         <li>
            <a title="Save to Alfresco" href="${url.serviceContext}/office/navigation?p=${path?url}">
               <img src="${url.context}/images/office/save_to_alfresco.gif" alt="Save to Alfresco" />
               Save to Alfresco
            </a>
            <br />Allows you to place the current document under Alfresco management.
         </li>
</#if>
         <li>
            <a title="Create Collaboration Space" href="${url.serviceContext}/office/navigation?p=${path?url}&amp;n=${d.id}&amp;cc=true">
               <img src="${url.context}/images/office/create_space.gif" alt="Create Collaboration Space" />
               Create Collaboration Space
            </a>
            <br />Create a new Collaboration Space in the Alfresco Repository
         </li>
         <li>
            <a title="Launch Alfresco" href="${url.context}/navigate/browse?ticket=${session.ticket}" rel="_blank">
               <img src="${url.context}/images/logo/AlfrescoLogo16.gif" alt="Launch Alfresco" />
               Launch Alfresco
            </a>
            <br />Start the Alfresco Web Client.
         </li>
      </ul>
   </div>

   <div id="statusText"></div>

</div>

</body>
</html>