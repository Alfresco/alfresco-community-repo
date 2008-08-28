<#if result.page??>
${result.page.content}
<#else>
<#-- An error occured -->
${result.error!""}
</#if>