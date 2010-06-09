<#import "person.lib.ftl" as personLib/>
<#if groups??>
	<@personLib.personGroupsJSON person=person groups=groups capabilities=capabilities immutability=immutability />
<#else>
	<@personLib.personCapJSON person=person capabilities=capabilities />
</#if>