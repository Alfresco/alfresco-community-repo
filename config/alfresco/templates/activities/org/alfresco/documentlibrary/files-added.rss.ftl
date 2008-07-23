<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<item>
    <title>${fileCount!"Many"} files uploaded</title>
    <link>${browseURL?xml}</link>
    <guid>${id}</guid>
    <description>${username?xml} added ${fileCount!"multiple"} files to the Document Library.</description>
</item>

