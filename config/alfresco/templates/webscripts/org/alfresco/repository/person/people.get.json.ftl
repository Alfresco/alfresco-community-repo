<#import "person.lib.ftl" as personLib/>

[
   <#list people as person>
      <@personLib.personJSON person=person/>
      <#if person_has_next>,</#if>
   </#list>
]