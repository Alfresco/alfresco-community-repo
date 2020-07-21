[
<#escape x as jsonUtils.encodeJSONString(x)>
	<#list nodes as node>
		{
			"nodeRef" : "${node.storeType + "://" + node.storeId + "/" + node.id}",
			"url" : "${url.serviceContext + "/api/node/" + node.storeType + "/" + node.storeId + "/" + node.id}"
		}<#if node_has_next>,</#if>
	</#list>
</#escape>
]