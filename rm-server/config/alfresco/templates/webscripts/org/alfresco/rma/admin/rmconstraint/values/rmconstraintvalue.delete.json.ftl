<#import "../rmconstraint.lib.ftl" as rmconstraintLib/>

<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data": <@rmconstraintLib.constraintWithValuesJSON constraint=constraint />
}
</#escape>