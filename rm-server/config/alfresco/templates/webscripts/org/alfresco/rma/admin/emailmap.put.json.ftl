<#import "emailmap.lib.ftl" as emailmapLib/>

<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data":
	<@emailmapLib.emailmapJSON emailmap=emailmap />       
}
</#escape>