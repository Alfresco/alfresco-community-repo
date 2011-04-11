<link rel="stylesheet" href="${url.context}/css/main.css" TYPE="text/css">

<script type="text/javascript" src="${url.context}/scripts/ajax/yahoo/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="${url.context}/scripts/ajax/yahoo/connection/connection-min.js"></script>
<script type="text/javascript" src="${url.context}/scripts/ajax/mootools.v1.11.js"></script>
<script type="text/javascript" src="${url.context}/scripts/ajax/common.js"></script>
<script type="text/javascript" src="${url.context}/scripts/ajax/summary-info.js"></script>
<script type="text/javascript" src="${url.context}/scripts/ajax/mytasks.js"></script>
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
                        <td>
                           <div id="taskFilterBar">
                              <ul>
                                 <li <#if filter=0>class="taskCurrent"</#if>><a href="#" onclick="MyTasks.filter(0); return false;"><span>${message("portlets.mytasks.tasks_to_do")}</span></a></li>
                                 <li <#if filter=1>class="taskCurrent"</#if>><a href="#" onclick="MyTasks.filter(1); return false;"><span style="color: #399DF7;">${message("portlets.mytasks.due_today")}</span></a></li>
                                 <li <#if filter=2>class="taskCurrent"</#if>><a href="#" onclick="MyTasks.filter(2); return false;"><span>${message("portlets.mytasks.next_7_days")}</span></a></li>
                                 <li <#if filter=3>class="taskCurrent"</#if>><a href="#" onclick="MyTasks.filter(3); return false;"><span>${message("portlets.mytasks.no_due_date")}</span></a></li>
                                 <li <#if filter=4>class="taskCurrent"</#if>><a href="#" onclick="MyTasks.filter(4); return false;"><span style="color: #DF3704;">${message("portlets.mytasks.overdue")}</span></a></li>
                              </ul>
                           </div>
                        </td>
                        <td width="150" align="right" style="padding: 5px 5px 0px 0px;">
                           <a class="refreshViewLink" href="#" onclick="MyTasks.refreshList(); return false;"><img src="${url.context}/images/icons/reset.gif" border="0" width="16" height="16" style="vertical-align:-25%;padding-right:4px"><span>${message("portlets.refresh")}</span></a>
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
                     <span class="taskFooterText">${message("portlets.message.showing")} <span id="taskCount">0</span> <#if filter=4>${message("portlets.mytasks.showing_overdue")}</#if> ${message("portlets.mytasks.showing_task")}<#if filter=1> ${message("portlets.mytasks.showing_due_today")}</#if><#if filter=2> ${message("portlets.mytasks.showing_due_next_week")}</#if><#if filter=3> ${message("portlets.mytasks.showing_with_no_due_date_set")}</#if></span>
                  </div>
               </td>
            </tr>
         </table>
      </td>
      <td class="paperEdgeRight">&nbsp;</td>
   </tr>
</table>
<div id="taskMessagePanel">
   <div class="taskMessagePanelClose"><img id="taskMessagePanelCloseImage" src="${url.context}/images/icons/close_portlet_static.gif" onclick="MyTasks.closeMessage();" /></div>
   <div class="taskMessagePanelLabel"></div>
</div>
<div style="font-size: 3px;">
   <span class="paperLeftCorner"></span>
   <span class="paperBottomEdge"></span>
   <span class="paperRightCorner"></span>
</div>

<style type="text/css">
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
   scrollbar-face-color: #fcf49a; 
   scrollbar-3dlight-color: #ede591;
   scrollbar-highlight-color: #fcf49a;
   scrollbar-shadow-color: #d5cc75;
   scrollbar-darkshadow-color: #d5cc75;
   scrollbar-arrow-color: #c2bb70;
   scrollbar-track-color: #f3e985;
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

#taskFilterBar {
   float: left;
   width: 100%;
}
#taskFilterBar ul {
   margin: 0px;
   padding: 4px 10px 0px 4px;
   list-style: none;
}
#taskFilterBar li {
   display: inline;
   margin: 0px;
   padding: 0px;
   height: 27px;
}
#taskFilterBar a {
   background: none;
   float:left;
   margin: 0px;
   padding: 0px 0px 0px 4px;
   text-decoration: none;
   outline: none;
}
#taskFilterBar a span {
   background: none;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 13px;
   font-weight: bold;
   color: #5A5741;
   display: block;
   float: none;
   padding: 5px 15px 4px 6px;
}

#taskFilterBar a:hover {
   background: url("${url.context}/images/parts/marker_effect_left.png") no-repeat left top;
}

#taskFilterBar a:hover span {
   background: url("${url.context}/images/parts/marker_effect_right.png") no-repeat right top;
   color: #ffffff !important;
}

#taskFilterBar .taskCurrent a {
   background: url("${url.context}/images/parts/marker_effect_left.png") no-repeat left top;
}
#taskFilterBar .taskCurrent a span {
   background: url("${url.context}/images/parts/marker_effect_right.png") no-repeat right top;
   color: #ffffff !important;
}

.refreshViewLink, .refreshViewLink
{
   text-decoration: none !important;
}
.refreshViewLink span
{
   color: #5A5741;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 13px;
   font-weight: bold;
}

.taskRow
{
   padding-top: 4px;
   border-top: 1px solid transparent;
   border-bottom: 1px solid #EBE398;
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
   margin: 0px 0px 0px 24px;
   padding: 0px 8px 8px;
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
   padding: 0px;
   overflow: hidden;
}

.taskDetailTopSpacer {
   padding: 4px 0px 0px;
}

.taskDetailSeparator
{
   border-right: 1px solid #c1af05;
}

.taskItemSelected
{
   background: #FFE500 url("${url.context}/images/parts/mytasks_arrow_down.png") no-repeat right top;
   border-bottom: 1px solid #82770B !important;
   border-top: 1px solid #82770B !important;
}

.taskItemSelectedOpen
{
   background-image: url("${url.context}/images/parts/mytasks_arrow_up.png") !important;
}

.taskResources
{
   border: 1px solid #FFE500;
   background-color: #fff;
   margin: 4px 0px 0px 0px;
   width: 300px;
   height: 80px;
   display: block;
   overflow: hidden;
}

.taskResourceEven
{
}

.taskResourceOdd
{
   background-color: #FEF8BC;
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

.taskAction
{
   margin: 0px;
   padding: 0px;
}

.taskAction ul {
   margin: 0px;
   padding: 0px;
   list-style: none;
   text-align: center;
}
.taskAction ul li {
   display: inline;
   margin: 0px;
   padding: 0px;
   list-style: none;
}

.taskAction a {
   background: url("${url.context}/images/parts/task_btn_normal_right.png") no-repeat 100% 50%;
   float: left;
   margin: 4px;
   padding: 0px;
   text-decoration: none;
   cursor: pointer;
}
.taskAction a span {
   background: url("${url.context}/images/parts/task_btn_normal_left.png") no-repeat 0 50%;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 11px;
   font-weight: bold;
   color: #5A5741;
   padding: 4px 1.5em;
   float: left;
}

.taskAction a:hover {
   background: url("${url.context}/images/parts/task_btn_rollover_right.png") no-repeat 100% 50%;
   text-decoration: none;
}

.taskAction a:hover span {
   background: url("${url.context}/images/parts/task_btn_rollover_left.png") no-repeat 0 50%;
}

.taskManage ul {
   margin: 0px;
   padding: 4px 0px 0px;
   list-style: none;
}
.taskManage li {
   display: inline;
   margin: 0px;
   padding: 0px;
   height: 27px;
}

.taskManage a {
   background: url("${url.context}/images/parts/task_btn_normal_left.png") no-repeat left top;
   float:left;
   margin: 0px;
   padding: 0px 0px 0px 8px;
   text-decoration: none;
   cursor: pointer;
}
.taskManage a span {
   background: url("${url.context}/images/parts/task_btn_normal_right.png") no-repeat right top;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 11px;
   font-weight: bold;
   color: #5A5741;
   display: block;
   float: none;
   padding: 4px 15px 4px 6px;
}

.taskManage a:hover {
   background: url("${url.context}/images/parts/task_btn_rollover_left.png") no-repeat left top;
   text-decoration: none;
}

.taskManage a:hover span {
   background: url("${url.context}/images/parts/task_btn_rollover_right.png") no-repeat right top;
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

#taskMessagePanel
{
   position: absolute;
   border: 1px solid #65696C;
   background-color: #eeeeee;
   width: 250px;
   height: 72px;
   padding: 8px;
   margin-left: 440px;
   display: none;
   z-index: 1;
   -moz-border-radius: 7px;
}

.taskMessagePanelClose
{
   margin: -4px -4px 2px 2px;
   float:right;
}

#taskMessagePanelCloseImage
{
   cursor: pointer;
   display: block;
   height: 23px;
   width: 23px;
}

.taskMessagePanelLabel
{
   color: #45494C;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 12px;
   font-weight: bold;
}

</style>