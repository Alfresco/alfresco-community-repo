<entry xmlns='http://www.w3.org/2005/Atom'>
    <title>${pageName!""}</title>
    <link rel="alternate" type="text/html" 
	href="${pageContext?replace("&", "&amp;")}"
    />
    <id>${id}</id>
    <updated>${xmldate(date)}</updated>
    <summary type="html">
<![CDATA[${firstName!"anon"} ${lastName!""} just created a new wiki page: <a href="${pageContext}">${pageName}</a>.]]></summary>
    <author>
      <name>${userId!""}</name>
    </author> 
</entry>

