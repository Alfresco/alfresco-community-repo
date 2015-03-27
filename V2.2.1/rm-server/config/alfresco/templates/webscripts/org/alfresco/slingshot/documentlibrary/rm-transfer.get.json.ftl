<#escape x as jsonUtils.encodeJSONString(x)>
{
   <#if transfer??>
	"transfer":
	{
	   "nodeRef": "${transfer.nodeRef}",
		"name": "${transfer.name}",
		"rma:transferAccessionIndicator": ${(transfer.properties["rma:transferAccessionIndicator"]!false)?string},
		"rma:transferPDFIndicator": ${(transfer.properties["rma:transferPDFIndicator"]!false)?string}
	}
	</#if>
}
</#escape>