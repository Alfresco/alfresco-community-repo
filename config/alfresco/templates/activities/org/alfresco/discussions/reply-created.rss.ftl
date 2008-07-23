<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<item>
    <title>Reply added: ${(topicTitle!"unknown")?xml}</title>
    <link>${(browseTopicUrl!'')?xml}</link>
    <guid>${id}</guid>
    <description>${username?xml} added a reply to topic '${(topicTitle!'unknown')?xml}'.</description>
</item>

