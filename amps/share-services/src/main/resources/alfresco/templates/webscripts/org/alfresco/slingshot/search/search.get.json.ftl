<#escape x as jsonUtils.encodeJSONString(x)>
{
	"totalRecords": ${data.paging.totalRecords?c},
	"totalRecordsUpper": ${data.paging.totalRecordsUpper?c},
	"startIndex": ${data.paging.startIndex?c},
	"numberFound": ${(data.paging.numberFound!-1)?c},
	"facets":
	{
		<#if data.facets??><#list data.facets?keys as field>
		"${field}":
		[
			<#assign facets=data.facets[field]><#list facets as f>
			{
			"label": "${f.facetLabel}",
			"value": "${f.facetValue}",
			"hits": ${f.hits?c},
			"index": ${f.facetLabelIndex?c}
			}<#if f_has_next>,</#if>
			</#list>
		]<#if field_has_next>,</#if>
		</#list></#if>
	},
   "highlighting":
   {
      <#if data.highlighting??><#list data.highlighting?keys as nodeRef>
      "${nodeRef}":
      {
         <#assign fields=data.highlighting[nodeRef]><#list fields?keys as property>
         "${property}": "${fields[property][0]}"
         <#if property_has_next>,</#if>
         </#list>
      }<#if nodeRef_has_next>,</#if>
      </#list></#if>
   },
	"items":
	[
		<#list data.items as item>
		{
         "node": <#noescape>${item.nodeJSON}</#noescape>,
			"nodeRef": "${item.nodeRef}",
			"type": "${item.type}",
			"name": "${item.name!''}",
			"displayName": "${item.displayName!''}",
			<#if item.title??>
			"title": "${item.title}",
			</#if>
			"description": "${item.description!''}",
			"modifiedOn": "${xmldate(item.modifiedOn)}",
			"modifiedByUser": "${item.modifiedByUser}",
			"modifiedBy": "${item.modifiedBy}",
			"fromDate": "${xmldate(item.fromDate)}",
			"size": ${item.size?c},
			"mimetype": "${item.mimetype!''}",
			<#if item.site??>
			"site":
			{
				"shortName": "${item.site.shortName}",
				"title": "${item.site.title}"
			},
			"container": "${item.container}",
			</#if>
			<#if item.path??>
			"path": "${item.path}",
			</#if>
			"lastThumbnailModification":
			[
			 <#if item.lastThumbnailModification??>
			 <#list item.lastThumbnailModification as lastThumbnailMod>
			 "${lastThumbnailMod}"
			 <#if lastThumbnailMod_has_next>,</#if>
			 </#list>
			 </#if>
			],
			"tags": [<#list item.tags as tag>"${tag}"<#if tag_has_next>,</#if></#list>],
         "highlighting":
            {
               <#list item.highlighting?keys as property>
               "${property}": "${item.highlighting[property][0]}"
               <#if property_has_next>,</#if>
               </#list>
            }
   		}<#if item_has_next>,</#if>
		</#list>
	],
	"spellcheck":
	{
	  <#if data.spellcheck?? && data.spellcheck.spellCheckExist>
			"searchRequest": "${data.spellcheck.originalSearchTerm}",
	  	<#if data.spellcheck.searchedFor>
	       <#list data.spellcheck.results as collationQueryStr>
	  		"searchedFor": "${collationQueryStr?string}"
	  		<#break>
	 		</#list>
	  	<#else>
	  		"searchSuggestions": [
	  		<#list data.spellcheck.results as suggestion>
	  		"${suggestion?string}"<#if suggestion_has_next>,</#if>
	  		</#list>
	  		]
	  	</#if>
	  </#if>
	}
}
</#escape>