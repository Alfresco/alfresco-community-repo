<#if result?exists>
{
	"name" : "${result.name}"
	,
	"id" : "${result.webProjectId}"
	,
	"stagingSandboxId" : "${result.sandboxId}"
	,
	"stagingStoreId" : "${result.storeId}"
}
</#if>
