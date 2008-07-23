<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<item>
    <title>Blog post deleted: ${postTitle!"unknown"}</title>
    <guid>${id}</guid>
    <description>${username} deleted blog post ${postTitle}.</description>
</item>

