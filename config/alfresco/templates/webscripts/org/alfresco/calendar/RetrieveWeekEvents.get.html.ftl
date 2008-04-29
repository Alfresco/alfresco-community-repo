<table width="100%" cellpadding="0" cellspacing="0" border="0">
	<TR><TD align="center"><B>${dayCaption}</B></TD></TR>
</table>
<BR>

<table id="tabWeekView" bordercolor="#FF00FF" bordercolordark="#FFFFFF" bordercolorlight="#99CCFF" border="1" cellspacing="0" cellpadding="2" width="100%">
	<TR>
		<TD width="7%">&nbsp;</TD>
		<#list daysArray as item>
			<TD align="center" valign="top" width="13%">${item}</TD>
		</#list>
	</TR>

	<TR>
		<TD width="7%" align="center"><B>All Day</B></TD>
		<#list eventListAllDay as item>
			<TD>
				<#if item?exists>
					<#list item as eachEvent>
						<#if eachEvent.isEditable == 1>
								<DIV class='eventCaptionAllDay' style='background-color: ${eachEvent.object.properties["ia:colorEvent"]};' title='${eachEvent.object.properties["ia:descriptionEvent"]}' onclick="editEvent('${eachEvent.object.nodeRef}')">${eachEvent.object.properties["ia:whatEvent"]}</DIV>
						<#else>
								<DIV class='eventCaptionAllDayNonEditable' style='background-color:${eachEvent.color};' title='${eachEvent.object.properties["ia:descriptionEvent"]}'>${eachEvent.object.properties["ia:whatEvent"]}</DIV>
						</#if>
					</#list>
				<#else>
					&nbsp;
				</#if>
			</TD>
		</#list>
	</TR>
	
	<#assign i=0>
	<#list eventList as item>
		<#if i % 2 == 0>
			<#assign tdclass = "alternateRow">
		<#else>
			<#assign tdclass = "">
		</#if>
		<#assign i = i+1>
		<TR class="${tdclass}">
			<TD align='right'>${item.timeSlot}</TD>
			<#list item.object as hourEvents>
				<TD valign='top' width='13%' onclick='createWeekTextBoxNode(event, ${hourEvents.timeSlot?string("yyyy")}, ${hourEvents.timeSlot?string("MM")?number-1}, ${hourEvents.timeSlot?string("dd")})'>
					<#if hourEvents.object?exists>
						<#list hourEvents.object as eachEvent>
							<#if eachEvent.isEditable == 1>
									<DIV class='eventCaption' style='border-left: 3px solid ${eachEvent.color};' title='${eachEvent.object.properties["ia:descriptionEvent"]}' onclick="editEvent('${eachEvent.object.nodeRef}')">${eachEvent.object.properties["ia:whatEvent"]}</DIV>
							<#else>
									<DIV class='eventCaptionNonEditable' style='border-left: 3px solid ${eachEvent.color};' title='${eachEvent.object.properties["ia:descriptionEvent"]}'>${eachEvent.object.properties["ia:whatEvent"]}</DIV>
							</#if>
						</#list>

					<#else>
						&nbsp;
					</#if>
				</TD>
			</#list>
		</TR>
	</#list>
	
</table>
