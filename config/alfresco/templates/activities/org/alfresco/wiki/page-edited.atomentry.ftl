<entry xmlns='http://www.w3.org/2005/Atom'>
    <title>${(pageName!"")?xml}</title>
    <link rel="alternate" type="text/html" 
	href="${pageContext?xml}"
    />
    <id>${id}</id>
    <updated>${xmldate(date)}</updated>
    <summary type="html">
<![CDATA[${firstName!"anon"} ${lastName!""} edited the wiki page: <a href="${pageContext}">${pageName}</a>.]]></summary>
    <author>
      <name>${userId!""}</name>
    </author> 
</entry>

