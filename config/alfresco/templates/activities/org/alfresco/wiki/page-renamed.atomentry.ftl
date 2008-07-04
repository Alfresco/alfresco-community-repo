<entry xmlns='http://www.w3.org/2005/Atom'>
    <title>${currentName!""}</title>
    <link rel="alternate" type="text/html" 
	href="${pageContext?replace("&", "&amp;")}"
    />
    <id>${id}</id>
    <updated>${xmldate(date)}</updated>
    <summary type="html">
<![CDATA[${firstName!"anon"} ${lastName!""} changed the name of the wiki page from '${previousName}' to '${currentName}'.]]></summary>
    <author>
      <name>${userId!""}</name>
    </author> 
</entry>

