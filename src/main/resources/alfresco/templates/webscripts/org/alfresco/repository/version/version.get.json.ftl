<#escape x as jsonUtils.encodeJSONString(x)>
[
<#list versions as v>
   {
      "nodeRef": "${v.nodeRef}",
      "name": "${v.name}",
      "label": "${v.label}",
      "description": "${v.description}",
      "createdDate": "${v.createdDate?string("dd MMM yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
      "createdDateISO": "${xmldate(v.createdDate)}",
      "creator":
      {
         "userName": "${v.creator.userName}",
         "firstName": "${v.creator.firstName!""}",
         "lastName": "${v.creator.lastName!""}"
      }
   }<#if (v_has_next)>,</#if>
</#list>
]
</#escape>