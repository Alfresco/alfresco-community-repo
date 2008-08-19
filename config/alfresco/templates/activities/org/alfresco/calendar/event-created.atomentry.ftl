<entry xmlns='http://www.w3.org/2005/Atom'>
    <title>${(eventName!"")?xml}</title>
    <link rel="alternate" type="text/html" 
        <#if nodeRef?? && nodeRef != "">
        href="${repoEndPoint}/d/d/${nodeRef?replace("://","/")}/${(name!"")?xml}"  
        <#else>
         href="${repoEndPoint}"
        </#if>
    />
    <id>${id}</id>
    <updated>${xmldate(date)}</updated>
    <summary>
${(firstName!"anon")?xml} ${(lastName!"")?xml} just added an event "${(eventName!"")?xml}" to the calendar.</summary>
    <author>
      <name>${userId!""}</name>
    </author> 
</entry>

