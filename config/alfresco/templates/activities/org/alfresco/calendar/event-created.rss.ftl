<item>
    <title>${eventName!""}</title>
    <link> 
        <#if nodeRef?? && nodeRef != "">
        ${repoEndpoint}/d/d/${nodeRef?replace("://","/")}/${name!""}  
        <#else>
         ${repoEndPoint}
        </#if>
    </link>
    <guid>${id}</guid>
    <description>${firstName!"anon"} ${lastName!""} just added an event "${eventName}" to the calendar.</description>
</item>

