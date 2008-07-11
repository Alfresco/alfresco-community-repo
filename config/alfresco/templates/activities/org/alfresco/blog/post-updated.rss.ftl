<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<item>
    <title>Blog post updated: ${title!"unknown"}</title>
    <link>${(browsePostUrl!'')?replace("&", "&amp;")}</link>
    <guid>${id}</guid>
    <description>${username} updated blog post ${title}.</description>
</item>

