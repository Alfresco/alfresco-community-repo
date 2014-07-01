{
    "resultset": [
      <#list result.stats as item>
         ["${jsonUtils.encodeJSONString(item.name)}",${item.sum?c}, ${item.count?c}]
         <#if item_has_next>,</#if>
      </#list>   
    ],
    "queryInfo": {
      "numberFound": "${result.numberFound?c}",
      "totalRows": "${resultSize?c}",
      "sum": "${result.sum?c}",
      "max": "${result.max?c}",
      "mean": "${result.mean?c}"    
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
        }
    ]
}