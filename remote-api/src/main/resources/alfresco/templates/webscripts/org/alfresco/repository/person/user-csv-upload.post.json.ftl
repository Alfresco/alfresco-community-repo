{
  "data": 
  {
<#escape x as jsonUtils.encodeJSONString(x)>
    "totalUsers": ${totalUsers?c},
    "addedUsers": ${addedUsers?c},
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
