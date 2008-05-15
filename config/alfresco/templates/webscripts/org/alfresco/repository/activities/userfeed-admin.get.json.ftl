{
<#if feedEntries??>
<#list feedEntries as feedEntry>${feedEntry.activitySummary}</#list>
<#else></#if>
}