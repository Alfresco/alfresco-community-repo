<#import "usersecurityclearance.lib.ftl" as usersecurityclearance />

<#escape x as jsonUtils.encodeJSONString(x)>
{
   "data": <@usersecurityclearance.usersecurityclearanceJSON item=item />
}
</#escape>