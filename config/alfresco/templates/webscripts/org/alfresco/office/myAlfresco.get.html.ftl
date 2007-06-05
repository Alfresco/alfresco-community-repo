<#assign doc_actions="${url.context}/service/office/docActions">
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
      <li id="current"><a title="My Alfresco" href="${url.context}/service/office/myAlfresco?p=${path}"><span><img src="${url.context}/images/office/my_alfresco.gif" alt="My Alfresco" /></span></a></li>
      <li><a title="Browse Spaces and Documents" href="${url.context}/service/office/navigation?p=${path}&n=${d.id}"><span><img src="${url.context}/images/office/navigator.gif" alt="Browse Spaces and Documents" /></span></a></li>
      <li><a title="Search Alfresco" href="${url.context}/service/office/search?p=${path}"><span><img src="${url.context}/images/office/search.gif" alt="Search Alfresco" /></span></a></li>
      <li><a title="View Details" href="${url.context}/service/office/documentDetails?p=${path}"><span><img src="${url.context}/images/office/document_details.gif" alt="View Details" /></span></a></li>
      <li><a title="My Tasks" href="${url.context}/service/office/myTasks?p=${path}"><span><img src="${url.context}/images/office/my_tasks.gif" alt="My Tasks" /></span></a></li>
   </ul>
</div>

<div class="header">My Checked Out Documents</div>

<div class="listMedium">
   <table>
<#assign rowNum=0>
<#assign query="@cm\\:workingCopyOwner:${person.properties.userName}">
   <#list companyhome.childrenByLuceneSearch[query] as child>
      <#if child.isDocument>
      <#assign rowNum=rowNum+1>
      <tr class="${(rowNum % 2 = 0)?string("odd", "even")}">
         <td valign="top">
            <img src="${url.context}${child.icon32}" alt="${child.name}" />
         </td>
         <td style="line-height:16px;" width="100%">
            <a href="#" title="Open ${child.name}" style="font-weight: bold;">${child.name}</a><br/>
         <#if child.properties.description?exists>
            ${child.properties.description}<br/>
         </#if>
            Modified: ${child.properties.modified?datetime} (${(child.size / 1024)?int}Kb)<br/>
            <a href="#" onClick="OfficeAddin.runAction('checkin','${child.id}', '');"><img src="${url.context}/images/office/checkin.gif" border="0" style="padding:3px 6px 2px 0px;" alt="Check In" title="Check In"></a>
            <a href="#" onClick="OfficeAddin.runAction('makepdf','${child.id}', '');"><img src="${url.context}/images/office/makepdf.gif" border="0" style="padding:3px 6px 2px 0px;" alt="Make PDF..." title="Make PDF"></a>
         </td>
      </tr>
      </#if>
   </#list>
   </table>
</div>

<div class="header">My Tasks<span class="headerExtra"><img src="${url.context}/images/office/task_overdue.gif" alt="overdue">=overdue, <img src="${url.context}/images/office/task_today.gif" alt="due today">=due today</span></div>

<div id="taskList" class="listMedium">
   <table width="100%">
<#assign taskNum=0>
<#list workflow.assignedTasks?sort_by('startDate') as t>
   <#assign taskNum=taskNum+1>
      <tr id="${t.id}" class="taskItem ${(taskNum % 2 = 0)?string("odd", "even")}">
         <td>
   <#assign hasDue=t.properties["bpm:dueDate"]?exists>
   <#if hasDue>
      <#assign due=t.properties["bpm:dueDate"]>
      <#-- items due today? -->
      <#if (dateCompare(date?date, due?date, 0, "==") == 1)>
            <img src="${url.context}/images/office/task_today.gif">
      <#-- items overdue? -->
      <#elseif (dateCompare(date?date, due?date) == 1)>
            <img src="${url.context}/images/office/task_overdue.gif">
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
   </table>
</div>

<div class="header">Other Actions</div>

<div id="documentActions">
   <ul>
      <li><a title="Save to Alfresco" href="${url.context}/service/office/navigation?p=${path}"><img src="${url.context}/images/office/save_to_alfresco.gif" alt="Save to Alfresco"><b>&nbsp;Save to Alfresco</b></a><br />Allows you to place the current document under Alfresco management.</li>
      <li><a title="Browse Alfresco" href="${url.context}/service/office/navigation?p=${path}"><img src="${url.context}/images/office/navigator.gif" alt="Browse Alfresco"><b>&nbsp;Browse Alfresco</b></a><br />Navigate around the Alfresco repository for documents.</li>
      <li><a title="Search" href="${url.context}/service/office/search?p=${path}"><img src="${url.context}/images/office/search.gif" alt="Search"><b>&nbsp;Find Documents</b></a><br />Search Alfresco for documents by name and content.</li>
      <li><a title="Launch Alfresco" href="${url.context}/navigate/browse?ticket=${session.ticket}" target="_blank"><img src="${url.context}/images/logo/AlfrescoLogo16.gif" alt="Launch Alfresco"><b>&nbsp;Launch Alfresco</b></a><br />Start the Alfresco Web Client.</li>
   </ul>
</div>

<div id="statusText"></div>

</body>
</html>