<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<item>
    <title>Blog post updated: ${(postTitle!"unknown")?xml}</title>
    <link>${(browsePostUrl!'')?xml}</link>
    <guid>${id}</guid>
    <description>${username?xml} updated blog post ${(postTitle!"unknown")?xml}.</description>
</item>

