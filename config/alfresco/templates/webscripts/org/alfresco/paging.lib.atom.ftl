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

[#macro linksredirect cursor serviceUrl urlargs="" type="" pageNo="pageNo" pageSize="pageSize" skipCount="skipCount" maxItems="maxItems"] 
[#if cursor.pageType = "PAGE"]
[#if cursor.hasFirstPage]
<link rel="first" href="${absurl(encodeuri(url.serviceContext + serviceUrl + "?" + argreplace(urlargs, pageNo, cursor.firstPage, pageSize, cursor.pageSize)))?xml}"[#if type != ""] type="${type}"[/#if]/>
[/#if]  
[#if cursor.hasLastPage]
<link rel="last" href="${absurl(encodeuri(url.serviceContext + serviceUrl + "?" + argreplace(urlargs, pageNo, cursor.lastPage, pageSize, cursor.pageSize)))?xml}"[#if type != ""] type="${type}"[/#if]/>
[/#if]  
[#if cursor.hasPrevPage]
<link rel="prev" href="${absurl(encodeuri(url.serviceContext + serviceUrl + "?" + argreplace(urlargs, pageNo, cursor.prevPage, pageSize, cursor.pageSize)))?xml}"[#if type != ""] type="${type}"[/#if]/>
[/#if]  
[#if cursor.hasNextPage]
<link rel="next" href="${absurl(encodeuri(url.serviceContext + serviceUrl + "?" + argreplace(urlargs, pageNo, cursor.nextPage, pageSize, cursor.pageSize)))?xml}"[#if type != ""] type="${type}"[/#if]/>
[/#if]
[#else]  
[#if cursor.hasFirstPage]
<link rel="first" href="${absurl(encodeuri(url.serviceContext + serviceUrl + "?" + argreplace(urlargs, skipCount, cursor.firstPage, maxItems, cursor.pageSize)))?xml}"[#if type != ""] type="${type}"[/#if]/>
[/#if]  
[#if cursor.hasLastPage]
<link rel="last" href="${absurl(encodeuri(url.serviceContext + serviceUrl + "?" + argreplace(urlargs, skipCount, cursor.lastPage, maxItems, cursor.pageSize)))?xml}"[#if type != ""] type="${type}"[/#if]/>
[/#if]  
[#if cursor.hasPrevPage]
<link rel="prev" href="${absurl(encodeuri(url.serviceContext + serviceUrl + "?" + argreplace(urlargs, skipCount, cursor.prevPage, maxItems, cursor.pageSize)))?xml}"[#if type != ""] type="${type}"[/#if]/>
[/#if]  
[#if cursor.hasNextPage]
<link rel="next" href="${absurl(encodeuri(url.serviceContext + serviceUrl + "?" + argreplace(urlargs, skipCount, cursor.nextPage, maxItems, cursor.pageSize)))?xml}"[#if type != ""] type="${type}"[/#if]/>
[/#if]  
[/#if]
[/#macro]

[#macro opensearch cursor]
[#-- NOTE: this macro requires the definition of xmlns:opensearch="http://a9.com/-/spec/opensearch/1.1/" in --]
[#--       the enclosing document                                                                           --]
<opensearch:totalResults>${cursor.totalRows?c}</opensearch:totalResults>
<opensearch:startIndex>${cursor.startRow?c}</opensearch:startIndex>
<opensearch:itemsPerPage>${cursor.pageSize?c}</opensearch:itemsPerPage>
[/#macro]

[#macro cmis cursor]
[#-- NOTE: this macro requires the definition of xmlns:cmisra="http://docs.oasis-open.org/ns/cmis/restatom/200908/" in --]
[#--       the enclosing document                                                                                      --]
<cmisra:numItems>${cursor.totalRows?c}</cmisra:numItems>
[/#macro]

[#macro changelinks changeLog] 
<link rel="first" href="${absurl(encodeuri(scripturl(argreplace(url.args, "changeLogToken", ""))))?xml}" type="${format.type}"/>
[#if changeLog.lastPageToken??]
<link rel="last" href="${absurl(encodeuri(scripturl(argreplace(url.args, "changeLogToken", changeLog.lastPageToken))))?xml}" type="${format.type}"/>
[/#if]  
[#if changeLog.previousPageToken??]
<link rel="prev" href="${absurl(encodeuri(scripturl(argreplace(url.args, "changeLogToken", changeLog.previousPageToken))))?xml}" type="${format.type}"/>
[/#if]  
[#if changeLog.nextChangeToken??]
<link rel="next" href="${absurl(encodeuri(scripturl(argreplace(url.args, "changeLogToken", changeLog.nextChangeToken))))?xml}" type="${format.type}"/>
[/#if]
[/#macro]

[#macro changeNumItems changeLog]
[#-- NOTE: this macro requires the definition of xmlns:cmisra="http://docs.oasis-open.org/ns/cmis/restatom/200908/" in --]
[#--       the enclosing document                                                                                      --]
<cmisra:numItems>${changeLog.eventCount?c}</cmisra:numItems>
[/#macro]
