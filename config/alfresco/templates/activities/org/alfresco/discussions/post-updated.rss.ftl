<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<item>
    <title>Discussion topic updated: ${title!"unknown"}</title>
    <link>${(browseTopicUrl!'')?replace("&", "&amp;")}</link>
    <guid>${id}</guid>
    <description>${username} updated topic '${title!'unknown'}'.</description>
</item>

