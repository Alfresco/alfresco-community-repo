<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<item>
    <title>Reply updated: ${(topicTitle!"unknown")?xml}</title>
    <link>${(browseTopicUrl!'')?xml}</link>
    <guid>${id}</guid>
    <description>${username?xml} updated a reply of topic '${(topicTitle!'unknown')?xml}'.</description>
</item>

