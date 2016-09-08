<#escape x as jsonUtils.encodeJSONString(x)>
{
    "success": ${success?string},
    "data" : {
        "referenceType": "${referenceType?string}",
        "refId": "${refId?string}",
        "url": "${url?string}"
    }
}
</#escape>