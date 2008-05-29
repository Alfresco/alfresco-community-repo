{
<#list eventList?chunk(7, '-') as row>
   <#list row as cell>
	<#if cell?exists>
			<#if cell.object?exists && cell.object?size &gt; 0>
			"${cell.datePart}" : [
				<#-- List the events for the current date -->
				<#list cell.object as event>
					"${event.object.properties["ia:whatEvent"]}"<#if event_has_next>,</#if>
				</#list>
			],	
			</#if>
	</#if>
   </#list>
</#list>
}
