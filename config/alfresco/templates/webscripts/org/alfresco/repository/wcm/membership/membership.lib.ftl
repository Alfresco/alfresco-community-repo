<#macro membershipJSON webproject role person>
   <#escape x as jsonUtils.encodeJSONString(x)>
      {
         "role" : "${role}",
         "person":
         {
            "userName" : "${person.properties.userName}",
            "firstName" : "${person.properties.firstName}",
            "lastName" : "${person.properties.lastName}",
            <#if person.assocs["cm:avatar"]??>
            "avatar" : "${"api/node/" + person.assocs["cm:avatar"][0].nodeRef?string?replace('://','/') + "/content/thumbnails/avatar"}",
            </#if>
            <#if person.properties.jobtitle??>
            "jobtitle" : "${person.properties.jobtitle}",
            </#if>
            <#if person.properties.organization??>
            "organization" : "${person.properties.organization}",
            </#if>
            "url" : "${url.serviceContext + "/api/people/" + person.properties.userName}"
         },
         "url" : "${url.serviceContext + "/api/wcm/webproject/" + webproject.webProjectRef + "/membership/" + person.properties.userName}"
      }
   </#escape>
</#macro>