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
   
   function taskMouseOver(el)
   {
      el._prevbg = el.style.backgroundColor;
      el.style.backgroundColor = "#FFE500";
      el.style.borderBottom = "1px solid #82770B";
      el.style.borderTop = "1px solid #82770B";
   }
   
   function taskMouseOut(el)
   {
      el.style.backgroundColor = el._prevbg;
      el.style.borderBottom = "1px solid #EBE398";
      el.style.borderTop = "1px solid #FEF8BC";
   }
</script>

<#-- get the filter mode from the passed in args -->
<#-- filters: 0=all, 1=today, 2=next week, 3=no due date, 4=overdue -->
<#if args.f?exists><#assign filter=args.f?number><#else><#assign filter=0></#if>

<table border=0 cellspacing=0 cellpadding=0 class="taskTable">
<tr>
   <td width=36 bgcolor="#F9F3B0">&nbsp;</td>
   <td align=center height=40 style="border-left: 1px solid #EBE398;">
      <table border=0 cellspacing=8 cellpadding=0>
         <tr>
            <th><a class="filterLink <#if filter=0>filterLinkSelected</#if>" href="${url.service}?f=0">Tasks to do</a></th>
            <th><a class="filterLink <#if filter=1>filterLinkSelected</#if>" href="${url.service}?f=1">Due Today</a></th>
            <th><a class="filterLink <#if filter=2>filterLinkSelected</#if>" href="${url.service}?f=2">Next 7 days</a></th>
            <th><a class="filterLink <#if filter=3>filterLinkSelected</#if>" href="${url.service}?f=3">No due date</a></th>
            <th><a class="filterLink <#if filter=4>filterLinkSelected</#if>" href="${url.service}?f=4">Overdue</a></th>
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
         <div class="taskRow">
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
                  ${t.description?html} [${t.name?html}]
                  <#if hasDue>
                     (Due: ${due?date})
                  </#if>
                  <span class="taskInfo" onclick="TaskInfoMgr.toggle('${t.id}',this);">
                     <img src="${url.context}/images/icons/popup.gif" class="popupImage" width="16" height="16" />
                  </span>
               </div>
            </div>
            <div class="taskDetail">
               <table cellpadding='2' cellspacing='0' style="margin-left:32px;">
   	            <tr><td class="taskMetadata">Type:</td><td class="taskMetadata">${t.type?html}</td></tr>
   	            <tr><td class="taskMetadata">Start Date:</td><td class="taskMetadata">${t.startDate?date}</td></tr>
   	            <tr><td class="taskMetadata">Priority:</td><td class="taskMetadata">${t.properties["bpm:priority"]}</td>
                  <tr><td class="taskMetadata">Status:</td><td class="taskMetadata">${t.properties["bpm:status"]}</td>
                  <tr><td class="taskMetadata">Percent Complete:</td><td class="taskMetadata">${t.properties["bpm:percentComplete"]}%</td>
   	         </table>
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
   <div class="taskFooter">
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

<STYLE type="text/css">
a.filterLink:link, a.filterLink:visited
{
   color: #5A5741;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 13px;
   font-weight: bold;
   text-decoration: none;
   padding-left: 4px;
   padding-right: 4px;
}

a.filterLink:hover
{
   color: #FFFFFF;
   background-color: #FDB64F;
}

a.filterLinkSelected:link, a.filterLinkSelected:visited
{
   color: #FFFFFF;
   background-color: #FDB64F;
}

.taskTable
{
   background-color: #FEF8BC;
   border-top: 1px solid #EBE398;
   border-left: 1px solid #EBE398;
   background-image: url(../images/logo/alf_task_bg.png);
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

.taskFooter
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
   background-color: #DFC900;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 12px;
   color: #000000;
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

.taskMetadata
{
   color: #5A5741;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
}

.paperEdgeRight
{
   background-image: url(../images/parts/paper_rightedge.png);
}

.paperLeftCorner
{
   background-image: url(../images/parts/paper_leftcorner.png);
}

.paperBottomEdge
{
   background-image: url(../images/parts/paper_bottomedge.png);
}

.paperRightCorner
{
   background-color: white;
   background-image: url(../images/parts/paper_rightcorner.gif);
}
</STYLE>