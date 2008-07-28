<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<item>
    <title>Discussion topic updated: ${(topicTitle!"unknown")?html?xml}</title>
    <link>${(browseTopicUrl!'')?xml}</link>
    <guid>${id}</guid>
    <description>${username?xml} updated topic '${(topicTitle!'unknown')?html?xml}'.</description>
</item>

