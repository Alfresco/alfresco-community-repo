<#import "../rmconstraint.lib.ftl" as rmconstraintLib/>

<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data":  <@rmconstraintLib.constraintWithValueJSON constraint=constraint value=value/>
}
</#escape>