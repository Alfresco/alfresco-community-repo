<#if facets??>
[
   <#list facets as facet>
      "${facet}"<#if facet_has_next>,</#if>
   </#list>
]
<#else>
{
    "resultset": [
      <#list result.stats as item>
         ["${jsonUtils.encodeJSONString(item.name)}",${item.sum?c}, ${item.count?c}, ${item.min?c}, ${item.max?c}, ${item.mean?c}]
         <#if item_has_next>,</#if>
      </#list>   
    ],
    "queryInfo": {
      "numberFound": "${result.numberFound?c}"
      ,"totalRows": "${resultSize?c}"
      <#if result.sum??>
         ,"sum": "${result.sum?c}"
         ,"max": "${result.max?c}"
         ,"mean": "${result.mean?c}"
      </#if>    
    },
    "metadata": [
        {
            "colIndex": 0,
            "colType": "String",
            "colName": "name"
        },
        {
            "colIndex": 1,
            "colType": "Numeric",
            "colName": "sum"
        },
        {
            "colIndex": 2,
            "colType": "Numeric",
            "colName": "count"
        },
        {
            "colIndex": 3,
            "colType": "Numeric",
            "colName": "min"
        },
        {
            "colIndex": 4,
            "colType": "Numeric",
            "colName": "max"
        },
        {
            "colIndex": 5,
            "colType": "Numeric",
            "colName": "mean"
        }
    ]
}
</#if>