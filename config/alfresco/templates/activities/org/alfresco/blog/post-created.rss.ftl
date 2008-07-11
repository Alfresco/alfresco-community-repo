<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<item>
    <title>New blog post: ${title!"unknown"}</title>
    <link>${(browsePostUrl!'')?replace("&", "&amp;")}</link>
    <guid>${id}</guid>
    <description>${username} added blog post ${title!'unknown'}.</description>
</item>

