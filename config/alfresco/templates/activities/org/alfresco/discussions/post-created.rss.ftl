<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<item>
    <title>New discussion: ${(topicTitle!"unknown")?html?xml}</title>
    <link>${(browseTopicUrl!'')?xml}</link>
    <guid>${id}</guid>
    <description>${username?xml} added topic ${(topicTitle!'unknown')?html?xml}.</description>
</item>

