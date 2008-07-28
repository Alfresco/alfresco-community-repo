<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<item>
    <title>Blog post deleted: ${(postTitle!"unknown")?html?xml}</title>
    <guid>${id}</guid>
    <description>${username?xml} deleted blog post ${(postTitle!"unknown")?html?xml}.</description>
</item>

