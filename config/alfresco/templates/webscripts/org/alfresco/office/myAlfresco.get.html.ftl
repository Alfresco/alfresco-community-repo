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
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
	<title>My Alfresco</title>
	<link rel="stylesheet" type="text/css" href="${url.context}/css/office.css" />
<!--[if IE 6]>
   <link rel="stylesheet" type="text/css" href="${url.context}/css/office_ie6.css" />
<![endif]-->
   <script type="text/javascript" src="${url.context}/scripts/ajax/mootools.v1.11.js"></script>
	<script type="text/javascript" src="${url.context}/scripts/office/office_addin.js"></script>
	<script type="text/javascript" src="${url.context}/scripts/office/my_alfresco.js"></script>
</head>
<body>

<div id="tabBar">
   <ul>
      <li id="current"><a title="My Alfresco" href="${url.serviceContext}/office/myAlfresco?p=${path?url}&amp;e=${extn}&amp;n=${nav}"><span><img src="${url.context}/images/office/my_alfresco.gif" alt="My Alfresco" /></span></a></li>
      <li><a title="Browse Spaces and Documents" href="${url.serviceContext}/office/navigation?p=${path?url}&amp;e=${extn}&amp;n=${nav}"><span><img src="${url.context}/images/office/navigator.gif" alt="Browse Spaces and Documents" /></span></a></li>
      <li><a title="Search Alfresco" href="${url.serviceContext}/office/search?p=${path?url}&amp;e=${extn}&amp;n=${nav}&amp;ticket=${session.ticket}"><span><img src="${url.context}/images/office/search.gif" alt="Search Alfresco" /></span></a></li>
      <li><a title="View Details" href="${url.serviceContext}/office/documentDetails?p=${path?url}&amp;e=${extn}&amp;n=${nav}"><span><img src="${url.context}/images/office/document_details.gif" alt="View Details" /></span></a></li>
      <li><a title="My Tasks" href="${url.serviceContext}/office/myTasks?p=${path?url}&amp;e=${extn}&amp;n=${nav}"><span><img src="${url.context}/images/office/my_tasks.gif" alt="My Tasks" /></span></a></li>
   </ul>
</div>

<div class="header">My Checked Out Documents<span class="headerExtra"><span class="toggle">&nbsp;</span></span></div>

<div id="checkedoutList" class="containerMedium togglePanel">
<#assign rowNum=0>
<#assign query="@cm\\:workingCopyOwner:${person.properties.userName}">
   <#list companyhome.childrenByLuceneSearch[query] as child>
      <#if child.isDocument>
         <#assign rowNum=rowNum+1>
         <#assign relativePath = (child.displayPath?substring(companyhome.name?length+1) + '/' + child.name)?url?replace('%2F', '/')?replace('\'', '\\\'') />
   <div class="documentItem ${(rowNum % 2 = 0)?string("odd", "even")}">
         <span class="documentItemIcon">
            <img src="${url.context}${child.icon32}" alt="${child.name}" />
         </span>
         <span class="documentItemDetails">
         <#if child.name?ends_with(extn) || child.name?ends_with(extnx)>
            <a href="#" onclick="window.external.openDocument('${relativePath}')" title="Open ${child.name}" style="font-weight: bold;">${child.name}</a><br />
         <#else>
            <a href="${url.context}${child.url}?ticket=${session.ticket}" target="_blank" title="Open ${child.name}" style="font-weight: bold;">${child.name}</a><br />
         </#if>
         <#if child.properties.description?exists>
            <#if (child.properties.description?length > 0)>
               ${child.properties.description}<br />
            </#if>
         </#if>
            Modified: ${child.properties.modified?datetime} (${(child.size / 1024)?int}Kb)<br />
            <a href="#" onclick="OfficeAddin.runAction('${doc_actions}','checkin','${child.id}', '');"><img src="${url.context}/images/office/checkin.gif" style="padding:3px 6px 2px 0px;" alt="Check In" title="Check In" /></a>
            <a href="${url.serviceContext}/office/myTasks?p=${path?url}&amp;w=new&amp;wd=${child.id}"><img src="${url.context}/images/office/new_workflow.gif" style="padding:3px 6px 2px 0px;" alt="Create Workflow..." title="Create Workflow..." /></a>
            <a href="#" onclick="window.external.insertDocument('${relativePath}')"><img src="${url.context}/images/office/insert_document.gif" style="padding:3px 6px 2px 0px;" alt="Insert File into Current Document" title="Insert File into Current Document" /></a>
         <#if !child.name?ends_with(".pdf")>
            <a href="#" onclick="OfficeAddin.runAction('${doc_actions}','makepdf','${child.id}', '');"><img src="${url.context}/images/office/makepdf.gif" style="padding:3px 6px 2px 0px;" alt="Make PDF..." title="Make PDF" /></a>
         </#if>
         </span>
      </div>
      </#if>
   </#list>
   <#if rowNum = 0>
      <div>
         <span class="noItems">(No documents)</span>
      </div>
   </#if>
</div>

<div class="header">My Tasks<span class="headerExtra"><span class="taskKey"><img src="${url.context}/images/office/task_overdue.gif" alt="overdue" />=overdue, <img src="${url.context}/images/office/task_today.gif" alt="due today" />=due today</span><span class="toggle">&nbsp;</span></span></div>

<div id="taskList" class="containerMedium togglePanel">
<#assign taskNum=0>
<#list workflow.assignedTasks as t>
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
         <span style="font-weight: bold;">${t.description!""?html}</span> (${t.type?html})
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
            <a title="Create Collaboration Space" href="${url.serviceContext}/office/navigation?p=${path?url}&amp;n=${nav}&amp;cc=true">
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