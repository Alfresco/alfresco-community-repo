<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<item>
    <title>Reply updated: ${topicTitle!"unknown"}</title>
    <link>${(browseTopicUrl!'')?replace("&", "&amp;")}</link>
    <guid>${id}</guid>
    <description>${username} updated a reply of topic '${topicTitle!'unknown'}'.</description>
</item>

