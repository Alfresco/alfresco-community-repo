  <entry xmlns='http://www.w3.org/2005/Atom'>
    <title><#if memberFirstName??>${memberFirstName?xml}</#if><#if memberLastName??> ${memberLastName?xml}</#if> role changed for ${siteNetwork?xml} site</title>
    <link rel="alternate" type="text/html"/>
    <icon></icon>
    <id>${id}</id>
    <updated>${xmldate(date)}</updated>
    <summary><#if memberFirstName??>${memberFirstName?xml}</#if><#if memberLastName??> ${memberLastName?xml}</#if> role changed to ${role} for ${siteNetwork?xml} site</summary>
    <author>
      <name>${userId}</name>
    </author> 
  </entry>