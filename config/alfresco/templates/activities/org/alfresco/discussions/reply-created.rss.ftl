<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<item>
    <title>Reply added: ${topicTitle!"unknown"}</title>
    <link>${(browseTopicUrl!'')?replace("&", "&amp;")}</link>
    <guid>${id}</guid>
    <description>${username} added a reply to topic '${topicTitle!'unknown'}'.</description>
</item>

