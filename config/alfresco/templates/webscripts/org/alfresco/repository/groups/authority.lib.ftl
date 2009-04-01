<#-- renders an authority object which can be either a GROUP or USER (and possibly ROLE in future)-->
<#macro formJSON authority>
    <#escape x as jsonUtils.encodeJSONString(x)>
{
        "authorityType" : "${authority.authorityType}",
        "shortName" : "${authority.shortName}",
        "fullName" : "${authority.fullName}",
        "displayName" : "${authority.displayName}",
        "url" : "/api/groups/${authority.shortName}"
}
	</#escape>
</#macro>