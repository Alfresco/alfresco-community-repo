<#import "rmconstraint.lib.ftl" as rmconstraintLib/>

<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data": <@rmconstraintLib.constraintJSON constraint=constraint />       
}
</#escape>