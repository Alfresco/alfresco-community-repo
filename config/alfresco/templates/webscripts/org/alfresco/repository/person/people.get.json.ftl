<#import "person.lib.ftl" as personLib/>
{
"people" : [
	<#list peoplelist as person>
		<@personLib.personJSON person=person/>
		<#if person_has_next>,</#if>
	</#list>
]
}