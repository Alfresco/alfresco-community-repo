<#assign username=userId>
<#if firstName?exists>
   <#assign username = firstName + " " + lastName>
</#if>
<item>
    <title>${fileName!"File uploaded"}</title>
    <link>${browseURL?replace("&", "&amp;")}</link>
    <guid>${id}</guid>
    <description>${username} added ${fileName} to the Document Library.</description>
</item>

