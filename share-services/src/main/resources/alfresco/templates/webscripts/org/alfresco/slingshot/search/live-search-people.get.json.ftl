<#import "../../repository/person/person.lib.ftl" as personLib/>
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"totalRecords": ${data.totalRecords?c},
	"startIndex": ${data.startIndex?c},
	"items":
	[
		<#list data.items as person>
		<@personLib.personSummaryJSON person=person/><#if person_has_next>,</#if>
		</#list>
	]
}
</#escape>