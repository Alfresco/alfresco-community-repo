<entry xmlns='http://www.w3.org/2005/Atom'>
    <title>Event deleted: ${(eventName!"")?xml}</title>
    <icon></icon>
    <id>${id}</id>
    <updated>${xmldate(date)}</updated>
    <summary>
${(firstName!"anon")?xml} ${(lastName!"")?xml} just updated the event ${(eventName!"")?xml}.</summary>
    <author>
      <name>${userId!""}</name>
    </author> 
</entry>

