<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<item>
    <title>New blog post: ${postTitle!"unknown"}</title>
    <link>${(browsePostUrl!'')?replace("&", "&amp;")}</link>
    <guid>${id}</guid>
    <description>${username} added blog post ${postTitle!'unknown'}.</description>
</item>

