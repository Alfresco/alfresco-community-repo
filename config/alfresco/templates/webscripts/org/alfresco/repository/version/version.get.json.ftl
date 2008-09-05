[
<#list versions as v>
   {
      nodeRef: "${v.nodeRef}",
      name: "${v.name}",
      label: "${v.label}",
      createdDate: "${v.createdDate?datetime}",
      creator: {
         userName: "${v.creator.userName}",
         firstName: "${v.creator.firstName}",
         lastName: "${v.creator.lastName}"
      }
   }<#if (v_has_next)>,</#if>
</#list>
]