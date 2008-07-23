<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<item>
    <title>Discussion topic deleted: ${topicTitle!"unknown"}</title>
    <guid>${id}</guid>
    <description>${username} deleted topic ${topicTitle!'unknown'}.</description>
</item>

