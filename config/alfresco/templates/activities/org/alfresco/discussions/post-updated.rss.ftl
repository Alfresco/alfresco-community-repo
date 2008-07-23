<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<item>
    <title>Discussion topic updated: ${(topicTitle!"unknown")?xml}</title>
    <link>${(browseTopicUrl!'')?xml}</link>
    <guid>${id}</guid>
    <description>${username?xml} updated topic '${(topicTitle!'unknown')?xml}'.</description>
</item>

