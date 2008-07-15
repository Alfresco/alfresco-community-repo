<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<item>
    <title>Reply added: ${title!"unknown"}</title>
    <link>${(browseTopicUrl!'')?replace("&", "&amp;")}</link>
    <guid>${id}</guid>
    <description>${username} added a reply to topic '${title!'unknown'}'.</description>
</item>

