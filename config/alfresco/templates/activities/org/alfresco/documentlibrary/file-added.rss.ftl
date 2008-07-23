<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<item>
    <title>${(fileName!"File uploaded")?xml}</title>
    <link>${browseURL?xml}</link>
    <guid>${id}</guid>
    <description>${username?xml} added ${(fileName!'a new file')?xml} to the Document Library.</description>
</item>

