<link rel="stylesheet" href="${url.context}/css/main.css" TYPE="text/css">

<script type="text/javascript" src="/alfresco/scripts/ajax/yahoo/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/yahoo/connection/connection-min.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/mootools.v1.11.js"></script>
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

<table border="0" cellspacing="0" cellpadding="0" class="taskTable">
   <tr>
      <td width="36" bgcolor="#F8F2AC">&nbsp;</td>
      <td>
         <table border="0" cellspacing="0" cellpadding="0">
            <tr>
               <td align=center height=40 style="border-left: 1px solid #EBE398;">
                  <table border="0" cellspacing="8" cellpadding="0" width="100%">
                     <tr>
                        <th><a class="taskfilterLink <#if filter=0>taskfilterLinkSelected</#if>" href="#" onclick="MyTasks.filter(0); return false;">Tasks to do</a></th>
                        <th><a class="taskfilterLink <#if filter=1>taskfilterLinkSelected</#if>" href="#" onclick="MyTasks.filter(1); return false;" style="color: #399DF7;">Due Today</a></th>
                        <th><a class="taskfilterLink <#if filter=2>taskfilterLinkSelected</#if>" href="#" onclick="MyTasks.filter(2); return false;">Next 7 days</a></th>
                        <th><a class="taskfilterLink <#if filter=3>taskfilterLinkSelected</#if>" href="#" onclick="MyTasks.filter(3); return false;">No due date</a></th>
                        <th><a class="taskfilterLink <#if filter=4>taskfilterLinkSelected</#if>" href="#" onclick="MyTasks.filter(4); return false;" style="color: #DF3704;">Overdue</a></th>
                        <td width="150" align="right">
                           <a href="#" onclick="MyTasks.refreshList(); return false;" class="refreshViewLink"><img src="${url.context}/images/icons/reset.gif" border="0" width="16" height="16" style="vertical-align:-25%;padding-right:4px">Refresh</a>
                        </td>
                     </tr>
                  </table>
               </td>
            </tr>
            <tr>
               <td>
                  <div id="taskPanelOverlay"></div>
                  <div id="taskPanel">
                     <#-- populated via an AJAX call to 'mytaskspanel' webscript -->
                     <#-- resolved filter required as argument -->
                     <script>MyTasks.ServiceContext="${url.serviceContext}";MyTasks.Filter="${filter}";</script>
                  </div>
               </td>
            </tr>
            <tr>
               <td>
                  <div id="taskFooter">
                     <#-- the count value is retrieved and set dynamically from the AJAX webscript output above -->
                     <span class="taskFooterText">Showing <span id="taskCount">0</span> <#if filter=4>overdue</#if> task(s)<#if filter=1> due today</#if><#if filter=2> due next week</#if><#if filter=3> with no due date set</#if></span>
                  </div>
               </td>
            </tr>
         </table>
      </td>
      <td class="paperEdgeRight">&nbsp;</td>
   </tr>
</table>
<div style="font-size: 3px;">
   <span class="paperLeftCorner"></span>
   <span class="paperBottomEdge"></span>
   <span class="paperRightCorner"></span>
</div>

<style type="text/css">
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
   color: #FFFFFF !important;
   background-color: #FDB64F;
}

a.taskfilterLinkSelected:link, a.taskfilterLinkSelected:visited
{
   color: #FFFFFF !important;
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
   width: 716px;
}

#taskPanel
{
   height: 300px;
   width: 672px;
   overflow: auto;
   overflow-y: scroll;
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
   width: 672px;
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
   height: 36px;
   width: 656px;
   padding: 0px;
   border-top: 1px solid #EBE398;
   border-left: 1px solid #F6DEA0;
   text-align: center;
   color: #5A5741;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 13px;
   font-weight: bold;
}

.taskFooterText
{
   display: block;
   margin-top: 8px;
}

.taskTitle
{
   cursor: pointer;
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
   margin: 4px 0px 0px 0px;
   width: 360px;
   height: 80px;
   display: block;
   overflow: hidden;
}
.taskResourceOdd
{
   background-color: #F8FCFD;
}
.taskResourceEven
{
   background-color: #EEF7FB;
}

.taskResourceHeader
{
   color: #5A5741;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   padding: 0px;
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
   float: left;
   height: 6px;
   width: 36px;
}

.paperBottomEdge
{
   background-image: url(${url.context}/images/parts/paper_bottomedge.png);
   float: left;
   height: 6px;
   width: 676px;
}

.paperRightCorner
{
   background-image: url(${url.context}/images/parts/paper_rightcorner.gif);
   float: left;
   height: 6px;
   width: 4px;
}
</style>