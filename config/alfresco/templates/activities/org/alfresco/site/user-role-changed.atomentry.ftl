  <entry xmlns='http://www.w3.org/2005/Atom'>
    <title>${memberFirstName}<#if memberLastName??> ${memberLastName}</#if> role changed for ${siteNetwork} site</title>
    <link rel="alternate" type="text/html"/>
    <icon></icon>
    <id>${id}</id>
    <updated>${xmldate(date)}</updated>
    <summary>${memberFirstName}<#if memberLastName??> ${memberLastName}</#if> role changed to ${role} for ${siteNetwork} site</summary>
    <author>
      <name>${userId}</name>
    </author> 
  </entry>