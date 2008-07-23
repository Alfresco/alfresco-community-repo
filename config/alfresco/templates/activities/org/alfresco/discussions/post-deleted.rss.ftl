<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<item>
    <title>Discussion topic deleted: ${(topicTitle!"unknown")?xml}</title>
    <guid>${id}</guid>
    <description>${username?xml} deleted topic ${(topicTitle!'unknown')?xml}.</description>
</item>

