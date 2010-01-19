<#assign doc_actions="${url.serviceContext}/office/docActions">
<#assign path=args.p!"">
<#assign extn=args.e!"doc"><#assign extnx=extn+"x">
<#assign nav=args.n!"">
<#if docWorkflow??>
   <#assign d=docWorkflow>
<#else>
   <#-- resolve the path (from Company Home) into a node -->
   <#if companyhome.childByNamePath[path]??>
      <#assign d=companyhome.childByNamePath[path]>
   <#else>
      <#assign d=companyhome>
   </#if>
</#if>
<#assign defaultQuery="?p=" + path?url + "&e=" + extn + "&n=" + nav>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
   <meta http-equiv="content-type" content="text/html; charset=UTF-8" />
   <title>${message("office.title.my_tasks")}</title>
   <link rel="stylesheet" type="text/css" href="${url.context}/css/office.css" />
<!--[if IE 6]>
   <link rel="stylesheet" type="text/css" href="${url.context}/css/office_ie6.css" />
<![endif]-->
   <script type="text/javascript" src="${url.context}/scripts/ajax/mootools.v1.11.js"></script>
   <script type="text/javascript" src="${url.context}/scripts/ajax/autocompleter.js"></script>
   <script type="text/javascript" src="${url.context}/scripts/ajax/date_picker.js"></script>
   <script type="text/javascript" src="${url.context}/scripts/office/office_addin.js"></script>
   <script type="text/javascript" src="${url.context}/scripts/office/my_tasks.js"></script>
   <script type="text/javascript" src="${url.context}/scripts/office/external_component.js"></script>
   <script type="text/javascript">//<![CDATA[
      OfficeAddin.defaultQuery = "${defaultQuery}";
      ExternalComponent.init(
      {
         fullUrl: "${url.full}",
         folderPath: "${url.serviceContext}/office/",
         ticket: "${session.ticket}"
      });
   //]]></script>
</head>
<body>

<div class="tabBar">
   <ul>
      <li><a title="${message("office.title.my_alfresco")}" href="${url.serviceContext}/office/myAlfresco${defaultQuery?html}"><span><img src="${url.context}/images/office/my_alfresco.gif" alt="${message("office.title.my_alfresco")}" /></span></a></li>
      <li><a title="${message("office.title.navigation")}" href="${url.serviceContext}/office/navigation${defaultQuery?html}"><span><img src="${url.context}/images/office/navigator.gif" alt="${message("office.title.navigation")}" /></span></a></li>
      <li><a title="${message("office.title.search")}" href="${url.serviceContext}/office/search${defaultQuery?html}"><span><img src="${url.context}/images/office/search.gif" alt="${message("office.title.search")}" /></span></a></li>
      <li><a title="${message("office.title.document_details")}" href="${url.serviceContext}/office/documentDetails${defaultQuery?html}"><span><img src="${url.context}/images/office/document_details.gif" alt="${message("office.title.document_details")}" /></span></a></li>
      <li id="current"><a title="${message("office.title.my_tasks")}" href="${url.serviceContext}/office/myTasks${defaultQuery?html}"><span><img src="${url.context}/images/office/my_tasks.gif" alt="${message("office.title.my_tasks")}" /></span></a></li>
      <li><a title="${message("office.title.document_tags")}" href="${url.serviceContext}/office/tags${defaultQuery?html}"><span><img src="${url.context}/images/office/tag.gif" alt="${message("office.title.document_tags")}" /></span></a></li>
   </ul>
   <span class="help">
      <a title="${message("office.help.title")}" href="${message("office.help.url")}" target="alfrescoHelp"><img src="${url.context}/images/office/help.gif" alt="${message("office.help.title")}" /></a>
   </span>
</div>

<div class="headerRow">
   <div class="headerWrapper">
      <div class="header">${message("office.header.my_tasks")}
         <span class="taskKey">
            <img src="${url.context}/images/office/task_overdue.gif" alt="${message("office.task.overdue")}" />=${message("office.task.overdue")},
            <img src="${url.context}/images/office/task_today.gif" alt="${message("office.task.due_today")}" />=${message("office.task.due_today")}
         </span>
      </div>
   </div>
</div>

<div id="taskList" class="containerMedium">
<#assign taskNum=0>
<#list workflow.assignedTasks as t>
   <#assign taskNum=taskNum+1>
   <#assign hasDue=t.properties["bpm:dueDate"]??>
   <#if hasDue>
      <#assign due=t.properties["bpm:dueDate"]>
   </#if>
   <div id="${t.id?replace("$", ".")}" class="taskItem" rel="<#if hasDue>${due?date?string("yyyyMMddHHmmss")}<#else>99999999999999</#if>">
      <span class="taskIndicator">
   <#if hasDue>
      <#-- items due today? -->
      <#if (dateCompare(date?date, due?date, 0, "==") == 1)>
         <img src="${url.context}/images/office/task_today.gif" alt="${message("office.task.due_today")}" />
      <#-- items overdue? -->
      <#elseif (dateCompare(date?date, due?date) == 1)>
         <img src="${url.context}/images/office/task_overdue.gif" alt="${message("office.task.overdue")}" />
      </#if>
   <#else>
         &nbsp;
   </#if>
      </span>
      <span class="taskItemDetails">
         <span style="font-weight: bold;">${(t.description!"")?html}</span> (${t.type?html})
   <#if hasDue>
            <br />${message("office.property.due_date")}: ${due?date}
   <#else>
            <br />(${message("office.message.no_due_date")})
   </#if>
      </span>
   </div>
</#list>
<#if taskNum = 0>
   <div>
      <span class="noItems">(${message("office.message.no_tasks")})</span>
   </div>
</#if>
</div>

<div class="headerRow">
   <div class="headerWrapper"><div class="header">${message("office.header.task_details")}</div></div>
</div>

<div class="containerBig">
   <div id="nonStatusText">
<#if args.w?? && d.isDocument>
      <div id="taskDetails">
         <table width="260">
            <tr>
               <td valign="top"><img src="${url.context}/images/office/new_workflow_large.gif" alt="${message("office.action.start_workflow")}" /></td>
               <td>
                  ${message("office.action.start_workflow")}:<br /><p class="ellipsis" style="font-weight: bold; padding-left: 8px; width: 232px;" title="${d.name?html}">${d.name?html}</p>
               </td>
            </tr>
         </table>
      
         <div style="margin-top: 8px; font-weight: bold;">${message("office.message.enter_workflow_details")}</div>
         <div class="taskParameters">
            <div class="taskParam">${message("office.property.workflow")}:</div>
            <div class="taskValue">
               <select id="wrkType" style="width: 178px;">
                  <option value="review" selected>${message("office.workflow.review")}</option>
                  <option value="adhoc">${message("office.workflow.adhoc")}</option>
               </select>
            </div>
            <div class="taskParam">${message("office.property.assign_to")}:</div>
            <div class="taskValue">
               <input id="wrkAssignTo" type="text" value="" />
               <img id="ajxAssignTo" src="${url.context}/images/office/ajax_anim.gif" alt="*" style="display: none;" />
            </div>
            <div class="taskParam">${message("office.property.due_on")}:</div>
            <div class="taskValue">
               <input type="text" id="wrkDueDate" value="" readonly="readonly" />
               <img src="${url.context}/images/office/date.gif" alt="${message("office.property.date")}" />
            </div>
            <div class="taskParam">${message("office.property.description")}:</div>
            <div class="taskValue"><textarea id="wrkDescription" rows="4" style="height:54px;"></textarea></div>
            <div class="taskParam">&nbsp;</div>
            <div class="taskValue">
               <a class="taskAction" href="#" onclick="OfficeMyTasks.startWorkflow('${url.serviceContext}/office/docActions', '${d.id}');">${message("office.button.submit")}</a>
               <a class="taskAction" href="${url.serviceContext}/office/myTasks${defaultQuery?html}">${message("office.button.cancel")}</a>
            </div>

         </div>
      </div>
<#else>
      <div id="taskDetails"></div>
</#if>
   </div>
   
   <div id="statusText"></div>

</div>

<div style="position: absolute; top: 0px; left: 0px; z-index: 100; display: none">
   <iframe id="if_externalComponenetMethodCall" name="if_externalComponenetMethodCall" src="" style="visibility: hidden;" width="0" height="0"></iframe>
</div>

</body>
</html>