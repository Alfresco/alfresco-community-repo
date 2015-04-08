<#import "../../repository/site/site.lib.ftl" as siteLib/>
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"totalRecords": ${data.totalRecords?c},
	"startIndex": ${data.startIndex?c},
	"items":
	[
		<#list data.items as site>
		<@siteLib.siteJSONManagers site=site roles="none"/><#if site_has_next>,</#if>
		</#list>
	]
}
</#escape>