<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<item>
    <title>${fileCount!"Many"} files uploaded</title>
    <link>${browseURL?replace("&", "&amp;")}</link>
    <guid>${id}</guid>
    <description>${username} added ${fileCount!"multiple"} files to the Document Library.</description>
</item>

