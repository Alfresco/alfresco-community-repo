<#import "../../person/person.lib.ftl" as personLib/>
<#import "../../groups/authority.lib.ftl" as authorityLib/>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "people":
   [
   <#if peopleFound??>
      <#list peopleFound as person>
         <@personLib.personJSON person=person/><#if person_has_next>,</#if>
      </#list>
   </#if>
   ],
   "data":
   [
   <#if groupsFound??>
      <#list groupsFound as group>   
         <@authorityLib.authorityJSON authority=group /><#if group_has_next>,</#if>
      </#list>
   </#if>
     ],
   "notAllowed":
   [
   <#if notAllowed??>
      <#list notAllowed as na>   
         "${na}"<#if na_has_next>,</#if>
      </#list>
   </#if>
   ]
}
</#escape>