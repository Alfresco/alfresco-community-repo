<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<item>
    <title>Blog post deleted: ${title!"unknown"}</title>
    <link>${(browsePostListURL!'')?replace("&", "&amp;")}</link>
    <guid>${id}</guid>
    <description>${username} deleted blog post ${title}.</description>
</item>

