<#assign days = DaysArray>

<table id="tabMonthView" bordercolor="#FF00FF" bordercolordark="#FFFFFF" bordercolorlight="#99CCFF" border="1" cellspacing="0" cellpadding="2" width="100%">
	<TR>
		<#list days as item>
			<TD align="center" valign="top" width="13%">${item}</TD>
		</#list>
	</TR>

	<#assign i=0>
	<#list eventList?chunk(7, '-') as row>
		<#if i % 2 == 0>
			<#assign tdclass = "alternateRow">
		<#else>
			<#assign tdclass = "">
		</#if>
		<#assign i = i+1>
		<TR class="${tdclass}">
			<#list row as cell>
				<#if cell?exists>
					<TD valign="top" width="13%">
						<DIV class='divDates'>
<a href="#" onclick="CalendarUtil.setDayView(${cell.datePart}, tabView);">${cell.datePart}</a>
                                                </DIV>
						<#if cell.object?exists>
							<#list cell.object as eachEvent>
								<#if eachEvent.isEditable == 1>
										<DIV class='eventCaption' style='border-left: 3px solid ${eachEvent.color};' title='${eachEvent.object.properties["ia:descriptionEvent"]}' onclick="editEvent('${eachEvent.object.nodeRef}')">${eachEvent.object.properties["ia:whatEvent"]}</DIV>
								<#else>
										<DIV class='eventCaptionNonEditable' style='border-left: 3px solid ${eachEvent.color};' title='${eachEvent.object.properties["ia:descriptionEvent"]}'>${eachEvent.object.properties["ia:whatEvent"]}</DIV>
								</#if>
							</#list>
						</#if>
					</TD>
				<#else>
					<TD>&nbsp;</TD>
				</#if>
			</#list>
		</TR>
	</#list>
</table>
