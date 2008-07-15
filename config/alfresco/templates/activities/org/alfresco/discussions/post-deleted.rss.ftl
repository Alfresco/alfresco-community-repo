<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<item>
    <title>Discussion topic deleted: ${title!"unknown"}</title>
    <link>${(browseTopicListURL!'')?replace("&", "&amp;")}</link>
    <guid>${id}</guid>
    <description>${username} deleted topic ${title!'unknown'}.</description>
</item>

