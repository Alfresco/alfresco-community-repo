<item>
    <title>${(eventName!"")?xml}</title>
    <link> 
        <#if nodeRef?? && nodeRef != "">
        ${repoEndpoint}/d/d/${nodeRef?replace("://","/")}/${(name!"")?xml}  
        <#else>
         ${repoEndPoint}
        </#if>
    </link>
    <guid>${id}</guid>
    <description>${(firstName!"anon")?xml} ${(lastName!"")?xml} just added an event "${(eventName!"")?xml}" to the calendar.</description>
</item>

