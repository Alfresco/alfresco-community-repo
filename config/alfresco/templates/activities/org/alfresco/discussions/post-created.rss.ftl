<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<item>
    <title>New discussion: ${topicTitle!"unknown"}</title>
    <link>${(browseTopicUrl!'')?replace("&", "&amp;")}</link>
    <guid>${id}</guid>
    <description>${username} added topic ${topicTitle!'unknown'}.</description>
</item>

