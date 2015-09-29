<#macro usersecurityclearanceJSON item>
    <#local cl=item.clearanceLevel>
    <#local pi=item.personInfo>
    {
        <#escape x as jsonUtils.encodeJSONString(x)>
        "classificationId": "${cl.highestClassificationLevel.id}",
        "clearanceLabel": "${cl.displayLabel}",
        "userName": <#if pi.userName??>"${pi.userName}"<#else>null</#if>,
        "firstName": <#if pi.firstName??>"${pi.firstName}"<#else>null</#if>,
        "lastName": <#if pi.lastName??>"${pi.lastName}"<#else>null</#if>,
        "fullName": <#if pi.firstName?? && pi.lastName??>"${pi.firstName} ${pi.lastName}"<#else>"${pi.userName}"</#if>,
        "completeName": <#if pi.firstName?? && pi.lastName?? && pi.userName??>"${pi.firstName} ${pi.lastName} (${pi.userName})"<#else>"${pi.userName}"</#if>,
        "isEditable": <#if people.isAdmin(people.getPerson(pi.userName))>false<#else>true</#if>
        </#escape>
    }
</#macro>