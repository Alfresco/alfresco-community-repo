<div class="taskPopupPanel">
	<table cellpadding='3' cellspacing='0'>
	   <tr>
	      <td colspan='2' class='mainSubTitle'>
	         <table cellspacing='0' cellpadding='0' width='100%' style='cursor:move' id='dragable'>
	            <tr>
	               <td class='mainSubTitle'>${task.description}</td>
	               <td width=14 align=right><img src='${url.context}/images/icons/close_panel.gif' onclick="TaskInfoMgr.close('${task.id}');" style='cursor:pointer' width=14 height=14 border=0 title="Close" alt="Close"></td>
	            </tr>
	         </table>
	      </td>
	   </tr>
	   
	   <tr>
	      <td valign='top'>
	         <table cellpadding='2' cellspacing='0' class='taskPopupData'>
	            <tr><td>Type:</td><td>${task.type?html}</td></tr>
	            <tr><td>Name:</td><td>${task.name?html}</td></tr>
	            <tr><td>Start Date:</td><td>${task.startDate?date}</td></tr>
	            <tr><td>Due Date:</td><td>
	            <#if task.properties["bpm:dueDate"]?exists>
	               ${t.properties["bpm:dueDate"]?date}
	            <#else>
	               <i>None</i>
	            </#if>
	            </td></tr>
	            <tr><td>Priority:</td><td>${task.properties["bpm:priority"]}</td>
               <tr><td>Percent Completed:</td><td>${task.properties["bpm:percentComplete"]}</td>
               <tr><td>Status:</td><td>${task.properties["bpm:status"]}</td>
               <tr><td>Completed:</td><td>${task.isCompleted?string("Yes", "No")}</td>
	         </table>
	      </td>
	   </tr>
	</table>
</div>

<STYLE type="text/css">
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
</STYLE>