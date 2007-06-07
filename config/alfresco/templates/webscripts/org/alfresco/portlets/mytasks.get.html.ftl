<link rel="stylesheet" href="${url.context}/css/main.css" TYPE="text/css">

<script type="text/javascript" src="/alfresco/scripts/ajax/yahoo/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/yahoo/connection/connection-min.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/mootools.v1.1.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/common.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/summary-info.js"></script>
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
   <div id="taskPanelOverlay"></div>
   <div id="taskPanel">
      <#-- populated via an AJAX call to 'mytaskspanel' webscript -->
      <#-- resolved filter required as argument -->
      <script>MyTasks.ServiceContext="${url.serviceContext}";MyTasks.Filter="${filter}";</script>
   </div>
</td>
<td width=4 class="paperEdgeRight"><td>
</tr>
<tr>
<td bgcolor="#F9F3B0">&nbsp;</td>
<td>
   <div id="taskFooter">
      <#-- the count value is retrieved and set dynamically from the AJAX webscript output above -->
      Showing <span id="taskCount">0</span> <#if filter=4>overdue</#if> task(s)<#if filter=1> due today</#if><#if filter=2> due next week</#if><#if filter=3> with no due date set</#if>.
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
   visibility: hidden;
}

#taskPanelOverlay
{
   background-image: url(${url.context}/images/icons/ajax_anim.gif);
   background-position: center;
   background-repeat: no-repeat;
   position: absolute;
   border-top: 1px solid #EBE398;
   border-left: 1px solid #F6DEA0;
   height: 300px;
   width: 716px;
   overflow: hidden;
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
   border: 1px solid #FFE500;
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