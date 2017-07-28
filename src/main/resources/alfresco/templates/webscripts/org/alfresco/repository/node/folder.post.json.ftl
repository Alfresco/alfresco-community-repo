<#escape x as jsonUtils.encodeJSONString(x)>
  {
     "nodeRef": "${nodeRef.nodeRef}"
     <#if site??>
        ,
        "site": "${site.shortName}",
        "container": "${container}",
     </#if>
  }
</#escape>
