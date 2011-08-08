<#if result.page??>
${result.page.contents}
<#else>
<#-- An error occured -->
${result.error!""}
</#if>
