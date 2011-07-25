<#setting locale="${locale}">

<div class="taskPopupPanel">
	<table cellpadding='3' cellspacing='0'>
	   <tr>
	      <td colspan='2' class='mainSubTitle'>
	         <table cellspacing='0' cellpadding='0' width='100%' style='cursor:move' id='dragable'>
	            <tr>
	               <td class='mainSubTitle'>${task.description?html}</td>
	               <td width=14 align=right><img src='${url.context}/images/icons/close_panel.gif' onclick="TaskInfoMgr.close('${task.id}');" style='cursor:pointer' width=14 height=14 border=0 title='${msg("task_summary_panel.close")}' alt='${msg("task_summary_panel.close")}'></td>
	            </tr>
	         </table>
	      </td>
	   </tr>
	   
	   <tr>
	      <td valign='top'>
	         <table cellpadding='2' cellspacing='0' class='taskPopupData'>
	            <tr><td>${msg("task_summary_panel.type")}:</td><td>${task.type?html}</td></tr>
	            <tr><td>${msg("task_summary_panel.name")}:</td><td>${task.name?html}</td></tr>
	            <tr><td>${msg("task_summary_panel.start_date")}:</td><td>${task.startDate?string(msg("date_pattern"))}</td></tr>
	            <tr><td>${msg("task_summary_panel.due_date")}:</td><td>
	            <#if task.properties["bpm:dueDate"]?exists>
	               ${task.properties["bpm:dueDate"]?string(msg("date_pattern"))}
	            <#else>
	               <i>${msg("task_summary_panel.none")}</i>
	            </#if>
	            </td></tr>
	            <tr><td>${msg("task_summary_panel.priority")}:</td><td>${task.properties["bpm:priority"]}</td>
               <tr><td>${msg("task_summary_panel.percent_completed")}:</td><td>${task.properties["bpm:percentComplete"]}</td>
               <tr><td>${msg("task_summary_panel.status")}:</td><td>${task.properties["bpm:status"]}</td>
               <tr><td>${msg("task_summary_panel.completed")}:</td><td>${task.isCompleted?string('${msg("task_summary_panel.yes")}', '${msg("task_summary_panel.no")}')}</td>
	         </table>
	      </td>
	   </tr>
	</table>
</div>

<style type="text/css">
   .taskPopupPanel
   {
      background-image: url(../images/parts/popup_bg.gif);
      background-repeat: repeat-x;
      background-color: #F9F1A8;
      border: 1px solid #F6DEA0;
      padding: 4px;
      max-width: 700px;
      margin: 0px 0px 0px 0px;
   }
   
   .taskPopupData td
   {
      font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   }
</style>