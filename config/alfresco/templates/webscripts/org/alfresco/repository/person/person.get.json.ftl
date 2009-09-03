<#import "person.lib.ftl" as personLib/>
<#if groups??>
   <@personLib.personGroupsJSON person=person groups=groups />
<#else>
   <@personLib.personJSON person=person />
</#if>