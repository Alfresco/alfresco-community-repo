  <entry xmlns='http://www.w3.org/2005/Atom'>
    <title>${memberFirstName}<#if memberLastName??> ${memberLastName}</#if> joined ${siteNetwork} site</title>
    <link rel="alternate" type="text/html"/>
    <icon></icon>
    <id>${id}</id>
    <updated>${xmldate(date)}</updated>
    <summary>${memberFirstName}<#if memberLastName??> ${memberLastName}</#if> joined ${siteNetwork} site (with role ${role})</summary>
    <author>
      <name>${userId}</name>
    </author> 
  </entry>