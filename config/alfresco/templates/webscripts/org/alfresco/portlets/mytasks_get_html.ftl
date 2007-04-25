<link rel="stylesheet" href="${url.context}/css/main.css" TYPE="text/css">

<script type="text/javascript" src="/alfresco/scripts/ajax/yahoo/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/yahoo/connection/connection-min.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/yahoo/dom/dom-min.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/yahoo/event/event-min.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/yahoo/animation/animation-min.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/yahoo/dragdrop/dragdrop-min.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/common.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/summary-info.js"></script>
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
</tr>
<tr><td bgcolor="#F9F3B0">&nbsp;</td><td>
   <div class="taskPanel">
      <#assign weekms=1000*60*60*24*7>
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
         <div class="taskRow" onmouseover="taskMouseOver(this)" onmouseout="taskMouseOut(this)">
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
               <div style="display:inline" onclick="TaskInfoMgr.toggle('${t.id}',this);">
                  <img src="${url.context}/images/icons/popup.gif" styleClass="popupImage" width="16" height="16" />
               </div>
            </div>
         </div>
         </#if>
      </#list>
   </div>
</td></tr>
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
   border: 1px solid #EBE398;
}

.taskPanel
{
   height: 300px;
   width: 700px;
   overflow: auto;
   border-top: 1px solid #EBE398;
   border-left: 1px solid #F6DEA0;
   background-image: url(../images/logo/alf_task_bg.png);
   background-repeat: no-repeat;
   background-position: 0 0;
}

.taskRow
{
   padding-top: 4px;
   padding-left: 8px;
   padding-right: 8px;
   padding-bottom: 4px;
   border-bottom: 1px solid #EBE398;
   border-top: 1px solid #FEF8BC;
}

.taskItem
{
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 14px;
   color: #5A5741;
   margin: 0 0 0 24;
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
   padding-left:4px;
}
</STYLE>