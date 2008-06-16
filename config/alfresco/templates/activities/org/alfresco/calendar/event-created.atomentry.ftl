<entry xmlns='http://www.w3.org/2005/Atom'>
    <title>${eventName!""}</title>
    <link rel="alternate" type="text/html" 
        <#if nodeRef?? && nodeRef != "">
        href="${repoEndPoint}/d/d/${nodeRef?replace("://","/")}/${name!""}"  
        <#else>
         href="${repoEndPoint}"
        </#if>
    />
    <icon></icon>
    <id>${id}</id>
    <updated>${xmldate(date)}</updated>
    <summary>
${firstName!"anon"} ${lastName!""} just added an event "${eventName}" to the calendar.</summary>
    <author>
      <name>${userId!""}</name>
    </author> 
</entry>

