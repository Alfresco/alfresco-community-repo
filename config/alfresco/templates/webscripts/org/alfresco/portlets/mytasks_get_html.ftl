<link rel="stylesheet" href="${url.context}/css/main.css" TYPE="text/css">

<script type="text/javascript" src="/alfresco/scripts/ajax/yahoo/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/yahoo/connection/connection-min.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/yahoo/dom/dom-min.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/yahoo/event/event-min.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/yahoo/animation/animation-min.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/yahoo/dragdrop/dragdrop-min.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/common.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/summary-info.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/mootools.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/mytasks.js"></script>
<script type="text/javascript">setContextPath('${url.context}');</script>

<script>
   // create manager object for the pop-up summary panels
   var TaskInfoMgr = new Alfresco.PanelManager("TaskInfoBean.sendTaskInfo", "taskId");
</script>

<#-- get the filter mode from the passed in args -->
<#-- filters: 0=all, 1=today, 2=next week, 3=no due date, 4=overdue -->
<#if args.f?exists && args.f?length!=0><#assign filter=args.f?number><#else><#assign filter=0></#if>

<table border=0 cellspacing=0 cellpadding=0 class="taskTable">
<tr>
   <td width=36 bgcolor="#F9F3B0">&nbsp;</td>
   <td align=center height=40 style="border-left: 1px solid #EBE398;">
      <table border=0 cellspacing=8 cellpadding=0>
         <tr>
            <th><a class="taskfilterLink <#if filter=0>taskfilterLinkSelected</#if>" href="${scripturl("?f=0")}">Tasks to do</a></th>
            <th><a class="taskfilterLink <#if filter=1>taskfilterLinkSelected</#if>" href="${scripturl("?f=1")}">Due Today</a></th>
            <th><a class="taskfilterLink <#if filter=2>taskfilterLinkSelected</#if>" href="${scripturl("?f=2")}">Next 7 days</a></th>
            <th><a class="taskfilterLink <#if filter=3>taskfilterLinkSelected</#if>" href="${scripturl("?f=3")}">No due date</a></th>
            <th><a class="taskfilterLink <#if filter=4>taskfilterLinkSelected</#if>" href="${scripturl("?f=4")}">Overdue</a></th>
         </tr>
      </table>
   </td>
   <td width=4 class="paperEdgeRight"><td>
</tr>
<tr><td bgcolor="#F9F3B0">&nbsp;</td><td>
   <div id="taskPanel">
      <#assign weekms=1000*60*60*24*7>
      <#assign count=0>
      <#list workflow.assignedTasks?sort_by('startDate') as t>
         <#-- TODO: is it better to use a js script to pre-filter the list? -->
         <#assign hasDue=t.properties["bpm:dueDate"]?exists>
         <#if hasDue>
            <#assign due=t.properties["bpm:dueDate"]>
         </#if>
         <#if (filter=0) ||
              (filter=3 && !hasDue) ||
              (filter=1 && hasDue && (dateCompare(date?date, due?date, 0, "==") == 1)) ||
              (filter=2 && hasDue && (dateCompare(due?date, date?date) == 1 && dateCompare(date?date, due?date, weekms) == 1)) ||
              (filter=4 && hasDue && (dateCompare(date?date, due?date) == 1))>
         <#assign count=count+1>
         <div class="taskRow" id="${t.id}">
            <div class="taskTitle">
               <div class="taskIndicator">
               <#if hasDue>
                  <#-- items due today? -->
                  <#if (filter=0 || filter=1) && (dateCompare(date?date, due?date, 0, "==") == 1)>
                     <img src="${url.context}/images/icons/task_today.gif"></div><div class="taskItem taskItemToday">
                  <#-- items overdue? -->
                  <#elseif (filter=0 || filter=4) && (dateCompare(date?date, due?date) == 1)>
                     <img src="${url.context}/images/icons/task_overdue.gif"></div><div class="taskItem taskItemOverdue">
                  <#else>
                     </div><div class="taskItem">
                  </#if>
               <#else>
                  </div><div class="taskItem">
               </#if>
                  ${t.description?html} (${t.type?html})
                  <#if hasDue>
                     (Due: ${due?date})
                  </#if>
                  <span class="taskInfo" onclick="event.cancelBubble=true; TaskInfoMgr.toggle('${t.id}',this);">
                     <img src="${url.context}/images/icons/popup.gif" class="popupImage" width="16" height="16" />
                  </span>
               </div>
            </div>
            <div class="taskDetail">
               <div style="float:left">
                  <table cellpadding='2' cellspacing='0' style="margin-left:32px;margin-top:16px">
                     <tr><td class="taskMetaprop">Status:</td><td class="taskMetadata">${t.properties["bpm:status"]}</td>
                     <tr><td class="taskMetaprop">Priority:</td><td class="taskMetadata">${t.properties["bpm:priority"]}</td>
                     <tr><td class="taskMetaprop">Start Date:</td><td class="taskMetadata">${t.startDate?date}</td></tr>
      	            <tr><td class="taskMetaprop">Type:</td><td class="taskMetadata">${t.type?html}</td></tr>
                     <tr><td class="taskMetaprop">Complete:</td><td class="taskMetadata">${t.properties["bpm:percentComplete"]}%</td>
      	         </table>
   	         </div>
   	         <div class="taskResourceHeader">${t.name?html}:</div>
   	         <div class="taskResources"></div>
   	         <div>
      	         <table class="taskActions" style="padding-left:16px">
      	            <tr>
      	               <#list t.transitions as wt>
      	               <td><a class="taskAction" href="#" onclick="MyTasks.transitionTask('/command/task/end/${t.id}/${wt.id}', '${scripturl("?f=${filter}")}', 'Workflow action \'${wt.label?html}\' completed.');">${wt.label?html}</a></td>
      	               </#list>
      	            </tr>
      	         </table>
      	      </div>
            </div>
         </div>
         </#if>
      </#list>
   </div>
</td>
<td width=4 class="paperEdgeRight"><td>
</tr>
<tr>
<td bgcolor="#F9F3B0">&nbsp;</td>
<td>
   <div id="taskFooter">
      Showing ${count} <#if filter=4>overdue</#if> task(s)<#if filter=1> due today</#if><#if filter=2> due next week</#if><#if filter=3> with no due date set</#if>.
   </div>
</td>
<td class="paperEdgeRight"><td>
</tr>
<tr>
<td class="paperLeftCorner"><div style="height:6px"></div></td>
<td class="paperBottomEdge"></td>
<td class="paperRightCorner"></td>
</tr>
</table>

<#-- display status message if provided -->
<#if args.m?exists><script>window.addEvent('load', MyTasks.displayMessage("${args.m}"));</script></#if>

<STYLE type="text/css">
a.taskfilterLink:link, a.taskfilterLink:visited
{
   color: #5A5741;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 13px;
   font-weight: bold;
   text-decoration: none;
   padding-left: 4px;
   padding-right: 4px;
}

a.taskfilterLink:hover
{
   color: #FFFFFF;
   background-color: #FDB64F;
}

a.taskfilterLinkSelected:link, a.taskfilterLinkSelected:visited
{
   color: #FFFFFF;
   background-color: #FDB64F;
}

.taskTable
{
   background-color: #FEF8BC;
   border-top: 1px solid #EBE398;
   border-left: 1px solid #EBE398;
   background-image: url(${url.context}/images/logo/alf_task_bg.png);
   background-repeat: no-repeat;
   background-position: 72 64;
}

#taskPanel
{
   height: 300px;
   width: 716px;
   overflow: auto;
   border-top: 1px solid #EBE398;
   border-left: 1px solid #F6DEA0;
}

.taskRow
{
   padding-top: 4px;
   border-bottom: 1px solid #EBE398;
   border-top: 1px solid #FEF8BC;
}

#taskFooter
{
   width: 700px;
   padding: 8px;
   border-top: 1px solid #EBE398;
   border-left: 1px solid #F6DEA0;
   text-align: right;
   color: #5A5741;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 13px;
   font-weight: bold;
}

.taskItem
{
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 14px;
   color: #5A5741;
   margin: 0 0 0 24;
   padding: 0px 8px 6px 8px;
}

.taskItemOverdue
{
   color: #DF3704;
   font-weight: bold;
}

.taskItemToday
{
   color: #399DF7;
}

.taskIndicator
{
   float: left;
   padding-top:6px;
   padding-left:8px;
}

.taskDetail
{
   color: #5A5741;
   background-color: #DFC900;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 12px;
   margin: 0px;
   display: none;
   overflow: hidden;
}

.taskItemSelected
{
   background-color: #FFE500 !important;
   border-bottom: 1px solid #82770B !important;
   border-top: 1px solid #82770B !important;
}

.taskResources
{
   border-left: 1px solid #0092DD;
   border-top: 1px solid #0092DD;
   border-bottom: 1px solid #CCD4DB;
   border-right: 1px solid #CCD4DB;
   background-color: #FEF8BC;
   margin: 4px 0px 0px 16px;
   width: 400px;
   height: 80px;
   overflow: hidden;
}

.taskResourceHeader
{
   color: #5A5741;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   padding: 0px 0px 0px 16px;
   font-weight: bold;
   display: inline;
}

a.resourceLink:link, a.resourceLink:visited
{
   color: #5A5741;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
}

.resourceLink
{
   color: #5A5741;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
}

.taskActions td
{
   padding: 4px;
}

a.taskAction:link, a.taskAction:visited
{
   color: #5A5741;
   font-size: 13px;
   font-weight: bold;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   border: 1px solid #F6F1BA;
   padding-left: 4px;
   padding-right: 4px;
}

a.taskAction:hover
{
   font-size: 13px;
   font-weight: bold;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   border: 1px solid #F6F1BA;
   padding-left: 4px;
   padding-right: 4px;
   color: #FFFFFF;
   background-color: #FDB64F;
   text-decoration: none;
}

.taskMetadata
{
   color: #5A5741;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
}

.taskMetaprop
{
   color: #5A5741;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-weight: bold;
}

.paperEdgeRight
{
   background-image: url(${url.context}/images/parts/paper_rightedge.png);
}

.paperLeftCorner
{
   background-image: url(${url.context}/images/parts/paper_leftcorner.png);
}

.paperBottomEdge
{
   background-image: url(${url.context}/images/parts/paper_bottomedge.png);
}

.paperRightCorner
{
   background-color: white;
   background-image: url(${url.context}/images/parts/paper_rightcorner.gif);
}
</STYLE>