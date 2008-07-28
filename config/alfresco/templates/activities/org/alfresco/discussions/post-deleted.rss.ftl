<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<item>
    <title>Discussion topic deleted: ${(topicTitle!"unknown")?html?xml}</title>
    <guid>${id}</guid>
    <description>${username?xml} deleted topic ${(topicTitle!'unknown')?html?xml}.</description>
</item>

