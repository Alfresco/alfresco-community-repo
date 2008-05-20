[
<#if feedControls??>
	<#list feedControls as feedControl>
		${feedControl}<#if feedControl_has_next>,</#if>
	</#list>
</#if>
]