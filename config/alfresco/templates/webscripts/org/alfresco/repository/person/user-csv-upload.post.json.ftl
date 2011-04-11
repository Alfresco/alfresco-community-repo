{
  "data": 
  {
<#escape x as jsonUtils.encodeJSONString(x)>
    "totalUsers": ${totalUsers},
    "addedUsers": ${addedUsers},
    "users": 
    [
      <#list users?keys as username>
         {
             "username": "${username}",
             "uploadStatus": "${users[username]}"
         }
         <#if username_has_next>,</#if>
      </#list>
    ]
</#escape>
  }
}
