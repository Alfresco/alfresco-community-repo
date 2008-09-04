<#macro links cursor pageArg="pageNo" skipArg="skipCount">
<#if cursor.pageType = "PAGE">
<#if cursor.hasFirstPage>
<link rel="first" href="${absurl(encodeuri(scripturl(argreplace(url.args, pageArg, cursor.firstPage))))?xml}" type="${format.type}"/>
</#if>  
<#if cursor.hasLastPage>
<link rel="last" href="${absurl(encodeuri(scripturl(argreplace(url.args, pageArg, cursor.lastPage))))?xml}" type="${format.type}"/>
</#if>  
<#if cursor.hasPrevPage>
<link rel="prev" href="${absurl(encodeuri(scripturl(argreplace(url.args, pageArg, cursor.prevPage))))?xml}" type="${format.type}"/>
</#if>  
<#if cursor.hasNextPage>
<link rel="next" href="${absurl(encodeuri(scripturl(argreplace(url.args, pageArg, cursor.nextPage))))?xml}" type="${format.type}"/>
</#if>
<#else>  
<#if cursor.hasFirstPage>
<link rel="first" href="${absurl(encodeuri(scripturl(argreplace(url.args, skipArg, cursor.firstPage))))?xml}" type="${format.type}"/>
</#if>  
<#if cursor.hasLastPage>
<link rel="last" href="${absurl(encodeuri(scripturl(argreplace(url.args, skipArg, cursor.lastPage))))?xml}" type="${format.type}"/>
</#if>  
<#if cursor.hasPrevPage>
<link rel="prev" href="${absurl(encodeuri(scripturl(argreplace(url.args, skipArg, cursor.prevPage))))?xml}" type="${format.type}"/>
</#if>  
<#if cursor.hasNextPage>
<link rel="next" href="${absurl(encodeuri(scripturl(argreplace(url.args, skipArg, cursor.nextPage))))?xml}" type="${format.type}"/>
</#if>  
</#if>
</#macro>

<#macro opensearch cursor>
<#-- NOTE: this macro requires the definition of xmlns:opensearch="http://a9.com/-/spec/opensearch/1.1/" in -->
<#--       the enclosing document                                                                           -->
<opensearch:totalResults>${cursor.totalRows}</opensearch:totalResults>
<opensearch:startIndex>${cursor.startRow}</opensearch:startIndex>
<opensearch:itemsPerPage>${cursor.pageSize}</opensearch:itemsPerPage>
</#macro>
