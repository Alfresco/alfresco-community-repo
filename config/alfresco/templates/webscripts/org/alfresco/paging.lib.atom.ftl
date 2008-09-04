[#ftl]

[#macro links cursor pageNo="pageNo" pageSize="pageSize" skipCount="skipCount" maxItems="maxItems"] 
[#if cursor.pageType = "PAGE"]
[#if cursor.hasFirstPage]
<link rel="first" href="${absurl(encodeuri(scripturl(argreplace(url.args, pageNo, cursor.firstPage, pageSize, cursor.pageSize))))?xml}" type="${format.type}"/>
[/#if]  
[#if cursor.hasLastPage]
<link rel="last" href="${absurl(encodeuri(scripturl(argreplace(url.args, pageNo, cursor.lastPage, pageSize, cursor.pageSize))))?xml}" type="${format.type}"/>
[/#if]  
[#if cursor.hasPrevPage]
<link rel="prev" href="${absurl(encodeuri(scripturl(argreplace(url.args, pageNo, cursor.prevPage, pageSize, cursor.pageSize))))?xml}" type="${format.type}"/>
[/#if]  
[#if cursor.hasNextPage]
<link rel="next" href="${absurl(encodeuri(scripturl(argreplace(url.args, pageNo, cursor.nextPage, pageSize, cursor.pageSize))))?xml}" type="${format.type}"/>
[/#if]
[#else]  
[#if cursor.hasFirstPage]
<link rel="first" href="${absurl(encodeuri(scripturl(argreplace(url.args, skipCount, cursor.firstPage, maxItems, cursor.pageSize))))?xml}" type="${format.type}"/>
[/#if]  
[#if cursor.hasLastPage]
<link rel="last" href="${absurl(encodeuri(scripturl(argreplace(url.args, skipCount, cursor.lastPage, maxItems, cursor.pageSize))))?xml}" type="${format.type}"/>
[/#if]  
[#if cursor.hasPrevPage]
<link rel="prev" href="${absurl(encodeuri(scripturl(argreplace(url.args, skipCount, cursor.prevPage, maxItems, cursor.pageSize))))?xml}" type="${format.type}"/>
[/#if]  
[#if cursor.hasNextPage]
<link rel="next" href="${absurl(encodeuri(scripturl(argreplace(url.args, skipCount, cursor.nextPage, maxItems, cursor.pageSize))))?xml}" type="${format.type}"/>
[/#if]  
[/#if]
[/#macro]

[#macro opensearch cursor]
[#-- NOTE: this macro requires the definition of xmlns:opensearch="http://a9.com/-/spec/opensearch/1.1/" in --]
[#--       the enclosing document                                                                           --]
[#-- TODO: custom ns  <opensearch:totalResults>${cursor.totalRows}</opensearch:totalResults>  --]
[#-- TODO: custom ns  <opensearch:startIndex>${cursor.startRow}</opensearch:startIndex>  --]
[#-- TODO: custom ns  <opensearch:itemsPerPage>${cursor.pageSize}</opensearch:itemsPerPage>  --]
[/#macro]
