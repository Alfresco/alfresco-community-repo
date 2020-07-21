<#--
   Renders paged results information.
   The passed in data object should contain following attributes:
   pageSize: the number of elements requested to be returned (forms part of the request)
   startIndex: the index of the first element to be returned (forms part of the request)
   total: the total number of elements in the result set
   itemCount: the actual number of elements returned
   items: the result elements
   
Usage:
   <#import "generic-paged-results.lib.ftl" as gen/>
   {
   <@gen.pagedResults data=data ; item>
      output of the individual item, for example by calling another macro:
      <@yourLib.itemJSON item=item />
   </@gen.pagedResults>
   }
-->
<#macro pagedResults data>
   "total": ${data.total?c},
   "pageSize": ${data.pageSize?c},
   "startIndex": ${data.startIndex?c},
   "itemCount": ${data.itemCount?c},
   "items":
   [
   <#list data.items as item>
      <#nested item>
      <#if item_has_next>,</#if>
   </#list>
   ]
</#macro>

<#--
   Renders information on the paging of results.
   This version is paging information only, without the data.
-->
<#macro pagingJSON pagingVar="paging">
<#escape x as jsonUtils.encodeJSONString(x)>
  <#if .vars[pagingVar]??>,
    "paging": 
    {
      "maxItems": ${.vars[pagingVar].maxItems?c},
      "skipCount": ${.vars[pagingVar].skipCount?c},
      "totalItems": ${.vars[pagingVar].totalItems?c},
      "totalItemsRangeEnd": <#if .vars[pagingVar].confidence == "RANGE">${.vars[pagingVar].totalItemsRangeMax?c}<#else>null</#if>,
      "confidence": "${.vars[pagingVar].confidence?lower_case}"
    }
  </#if>
</#escape>
</#macro>

<#--
   Renders paged results information to conforms to the RESTful APIs standards.
   The passed in data object should contain following attributes:
   count: The actual number of elements returned
   hasMoreItems: True if more items on next page
   totalItems: The total result count
   skipCount: The number of elements to skip before retrieving the page
   maxItems: The number of elements requested to be returned 
   
Usage:
   <#import "generic-paged-results.lib.ftl" as gen/>
   {
   <@gen.standardRestfulPagedResults data=data ; item>
      output of the individual item, for example by calling another macro:
      <@yourLib.itemJSON item=item />
   </@gen.standardRestfulPagedResults>
   }
-->
<#macro standardRestfulPagedResults data>
	"list" : {
		"pagination" : {
			"count" : ${data.count?c},
			"hasMoreItems" : ${data.hasMoreItems?c},
			"totalItems" : ${data.totalItems?c},
			"skipCount" :${data.skipCount?c},
			"maxItems" : ${data.maxItems?c}
		},
	    "entries" : [
	<#list data.items as item>
	        {
	            "entry" : {
                    <#nested item>
	            }    
		    }<#if item_has_next>,</#if>
	</#list>
        ]
	}
</#macro>
