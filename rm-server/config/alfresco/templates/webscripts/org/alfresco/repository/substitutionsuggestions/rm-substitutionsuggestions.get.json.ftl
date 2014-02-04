{
	"substitutions":
	[
	<#list substitutions as substitution>
		"${substitution}"<#if substitution_has_next>,</#if>
	</#list>
	]
}