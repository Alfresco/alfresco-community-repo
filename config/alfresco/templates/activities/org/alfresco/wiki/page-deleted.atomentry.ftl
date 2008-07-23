<entry xmlns='http://www.w3.org/2005/Atom'>
    <title>${(pageName!"")?xml}</title>
    <link rel="alternate" type="text/html" href="" /> 
    <id>${id}</id>
    <updated>${xmldate(date)}</updated>
    <summary type="html">
<![CDATA[${firstName!"anon"} ${lastName!""} just deleted the wiki page: ${pageName}.]]></summary>
    <author>
      <name>${userId!""}</name>
    </author> 
</entry>

