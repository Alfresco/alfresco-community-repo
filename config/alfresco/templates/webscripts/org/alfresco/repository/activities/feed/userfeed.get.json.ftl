[
<#if feedEntries??>
<#list feedEntries as feedEntry>
    ${feedEntry}<#if feedEntry_has_next>,</#if>
</#list>
</#if>
]