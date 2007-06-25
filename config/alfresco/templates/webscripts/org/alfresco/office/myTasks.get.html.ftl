<#assign doc_actions="${url.serviceContext}/office/docActions">
<#if args.p?exists><#assign path=args.p><#else><#assign path=""></#if>
<#if docWorkflow?exists>
   <#assign d=docWorkflow>
<#else>
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
</#if>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
	<title>My Tasks</title>
	<link rel="stylesheet" type="text/css" href="${url.context}/css/office.css" />
<!--[if IE 6]>
   <link rel="stylesheet" type="text/css" href="${url.context}/css/office_ie6.css" />
<![endif]-->
   <script type="text/javascript" src="${url.context}/scripts/ajax/mootools.v1.1.js"></script>
	<script type="text/javascript" src="${url.context}/scripts/ajax/autocompleter.js"></script>
	<script type="text/javascript" src="${url.context}/scripts/ajax/date_picker.js"></script>
	<script type="text/javascript" src="${url.context}/scripts/office/office_addin.js"></script>
	<script type="text/javascript" src="${url.context}/scripts/office/my_tasks.js"></script>
</head>
<body>

<div id="tabBar">
   <ul>
      <li><a title="My Alfresco" href="${url.serviceContext}/office/myAlfresco?p=${path?url}"><span><img src="${url.context}/images/office/my_alfresco.gif" alt="My Alfresco" /></span></a></li>
      <li><a title="Browse Spaces and Documents" href="${url.serviceContext}/office/navigation?p=${path?url}"><span><img src="${url.context}/images/office/navigator.gif" alt="Browse Spaces and Documents" /></span></a></li>
      <li><a title="Search Alfresco" href="${url.serviceContext}/office/search?p=${path?url}"><span><img src="${url.context}/images/office/search.gif" alt="Search Alfresco" /></span></a></li>
      <li><a title="View Details" href="${url.serviceContext}/office/documentDetails?p=${path?url}"><span><img src="${url.context}/images/office/document_details.gif" alt="View Details" /></span></a></li>
      <li id="current"><a title="My Tasks" href="${url.serviceContext}/office/myTasks?p=${path?url}"><span><img src="${url.context}/images/office/my_tasks.gif" alt="My Tasks" /></span></a></li>
   </ul>
</div>

<div class="header">My Tasks<span class="headerExtra"><span class="taskKey"><img src="${url.context}/images/office/task_overdue.gif" alt="overdue" />=overdue, <img src="${url.context}/images/office/task_today.gif" alt="due today" />=due today</span></span></div>

<div id="taskList" class="containerMedium">
<#assign taskNum=0>
<#list workflow.assignedTasks?sort_by('startDate') as t>
   <#assign taskNum=taskNum+1>
   <#assign hasDue=t.properties["bpm:dueDate"]?exists>
   <#if hasDue>
      <#assign due=t.properties["bpm:dueDate"]>
   </#if>
   <div id="${t.id?replace("$", ".")}" class="taskItem" rel="<#if hasDue>${due?date?string("yyyyMMddHHmmss")}<#else>99999999999999</#if>">
      <span class="taskIndicator">
   <#if hasDue>
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
      </span>
      <span class="taskItemDetails">
         <span style="font-weight: bold;">${t.description?html}</span> (${t.type?html})
   <#if hasDue>
            <br />Due date: ${due?date}
   <#else>
            <br />(No due date)
   </#if>
      </span>
   </div>
</#list>
<#if taskNum = 0>
   <div>
      <span class="noItems">(No tasks)</span>
   </div>
</#if>
</div>

<div class="header">Workflow</div>

<div class="containerBig">
   <div id="nonStatusText">
<#if args.w?exists && d.isDocument>
      <div id="taskDetails">
         <table width="100%">
            <tr>
               <td valign="top"><img src="${url.context}/images/office/new_workflow_large.gif" alt="Start workflow" /></td>
               <td>
                  Start workflow on:<br /><span style="font-weight: bold; padding-left: 8px;">${d.name?html}</span>
               </td>
            </tr>
         </table>
      
         <div style="margin-top: 8px; font-weight: bold;">Enter new workflow details below</div>
         <div class="taskParameters">
            <div class="taskParam">Workflow:</div>
            <div class="taskValue">
               <select id="wrkType" style="width: 178px;">
                  <option value="review" selected>Review &amp; Approve</option>
                  <option value="adhoc">Adhoc Task</option>
               </select>
            </div>
            <div class="taskParam">Assign to:</div>
            <div class="taskValue">
               <input id="wrkAssignTo" type="text" value="" />
               <img id="ajxAssignTo" src="${url.context}/images/office/ajax_anim.gif" alt="*" style="display: none;" />
            </div>
            <div class="taskParam">Due on:</div>
            <div class="taskValue">
               <input type="text" id="wrkDueDate" value="" />
               <img src="${url.context}/images/office/date.gif" alt="date" />
            </div>
            <div class="taskParam">Description:</div>
            <div class="taskValue"><textarea id="wrkDescription" rows="4" style="height:54px;"></textarea></div>
            <div class="taskParam">&nbsp;</div>
            <div class="taskValue">
               <a class="taskAction" href="#" onclick="OfficeMyTasks.startWorkflow('${url.serviceContext}/office/docActions', '${d.id}');">
                  Submit
               </a>
            </div>

         </div>
      </div>
<#else>
      <div id="taskDetails"></div>
</#if>
   </div>
   
   <div id="statusText"></div>

</div>

</body>
</html>