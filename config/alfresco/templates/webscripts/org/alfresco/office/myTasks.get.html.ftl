<#assign doc_actions="${url.context}/service/office/docActions">
<#if args.p?exists><#assign path=args.p><#else><#assign path=""></#if>
<#if args.n?exists><#assign node=args.n><#else><#assign node=companyhome></#if>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
	<title>My Tasks</title>
	<link rel="stylesheet" type="text/css" href="${url.context}/css/office.css" />
   <script type="text/javascript" src="${url.context}/scripts/ajax/mootools.v1.1.js"></script>
	<script type="text/javascript" src="${url.context}/scripts/office/office_addin.js"></script>
	<script type="text/javascript" src="${url.context}/scripts/office/my_tasks.js"></script>
</head>
<body>

<div id="tabBar">
   <ul>
      <li><a title="My Alfresco" href="${url.context}/service/office/myAlfresco?p=${path}"><span><img src="${url.context}/images/office/my_alfresco.gif" alt="My Alfresco" /></span></a></li>
      <li><a title="Browse Spaces and Documents" href="${url.context}/service/office/navigation?p=${path}&n=${node.id}"><span><img src="${url.context}/images/office/navigator.gif" alt="Browse Spaces and Documents" /></span></a></li>
      <li><a title="Search Alfresco" href="${url.context}/service/office/search?p=${path}"><span><img src="${url.context}/images/office/search.gif" alt="Search Alfresco" /></span></a></li>
      <li><a title="View Details" href="${url.context}/service/office/documentDetails?p=${path}"><span><img src="${url.context}/images/office/document_details.gif" alt="View Details" /></span></a></li>
      <li id="current"><a title="My Tasks" href="${url.context}/service/office/myTasks?p=${path}"><span><img src="${url.context}/images/office/my_tasks.gif" alt="My Tasks" /></span></a></li>
   </ul>
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
            <span style="font-weight: bold;">${t.description?html}</span> (${t.type?html})
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

<div class="header">Workflow</div>

<div class="listBig">
   <div id="nonStatusText">
      <div id="taskDetails"></div>
   </div>
   
   <div id="statusText"></div>

</div>

</body>
</html>