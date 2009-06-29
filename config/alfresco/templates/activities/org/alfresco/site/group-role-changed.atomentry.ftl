  <entry xmlns='http://www.w3.org/2005/Atom'>
    <title>${groupName?xml} role changed for ${siteNetwork?xml} site</title>
    <link rel="alternate" type="text/html"/>
    <icon></icon>
    <id>${id}</id>
    <updated>${xmldate(date)}</updated>
    <summary> site ${siteNetwork?xml} group ${groupName?xml} role changed to ${role?xml}</summary>
    <author>
      <name>${userId}</name>
    </author> 
  </entry>