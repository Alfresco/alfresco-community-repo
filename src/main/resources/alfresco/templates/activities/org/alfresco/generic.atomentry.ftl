  <entry xmlns='http://www.w3.org/2005/Atom'>
    <title>${name!""}</title>
    <link rel="alternate" type="text/html" 
        <#if nodeRef?? && nodeRef != "">
        href="${repoEndPoint}/d/d/${nodeRef?replace("://","/")}/${name!""}"  
        <#else>
         href="${repoEndPoint}"
        </#if>
    />
    <icon></icon>
    <id>http://www.alfresco.org/rss/atom/${id}</id>
    <updated>${xmldate(date)}</updated>
    <summary>${userId!""} ${activityType!""} ${displayPath!""} <#if siteNetwork?? && siteNetwork != "">(${siteNetwork} site)</#if></summary>
    <author>
      <name>${userId!""}</name>
    </author>
  </entry>