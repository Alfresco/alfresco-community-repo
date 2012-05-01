<#import "rmevent.lib.ftl" as rmEventLib/>

<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data":
    <@rmEventLib.eventJSON event=event />
}
</#escape>