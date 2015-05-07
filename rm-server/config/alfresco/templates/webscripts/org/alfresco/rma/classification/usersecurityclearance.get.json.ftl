<#import "/org/alfresco/repository/generic-paged-results.lib.ftl" as gen/>
<#import "usersecurityclearance.lib.ftl" as usersecurityclearance />

<#escape x as jsonUtils.encodeJSONString(x)>
{
   "data":
   {
   <@gen.pagedResults data=data ; item>
       <@usersecurityclearance.usersecurityclearanceJSON item=item />
   </@gen.pagedResults>
   }
}
</#escape>