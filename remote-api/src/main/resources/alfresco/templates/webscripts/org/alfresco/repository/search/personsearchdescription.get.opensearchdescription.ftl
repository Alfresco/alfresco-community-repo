<?xml version="1.0" encoding="UTF-8"?>
<OpenSearchDescription xmlns="http://a9.com/-/spec/opensearch/1.1/" xmlns:alf="http://www.alfresco.org">
  <ShortName>Alfresco Person Search</ShortName>
  <LongName>Alfresco ${server.edition} Person Search ${server.version}</LongName>
  <Description>Search Alfresco People using keywords</Description>
  <#comment>IE takes first template from list, thus html response is listed first</#comment>
  <Url type="text/html" template="${absurl(url.serviceContext)}/api/search/person?q={searchTerms}&amp;p={startPage?}&amp;c={count?}&amp;l={language?}&amp;guest={alf:guest?}"/>
  <Url type="application/atom+xml" template="${absurl(url.serviceContext)}/api/search/person.atom?q={searchTerms}&amp;p={startPage?}&amp;c={count?}&amp;l={language?}&amp;guest={alf:guest?}"/>
  <Url type="application/rss+xml" template="${absurl(url.serviceContext)}/api/search/person.rss?q={searchTerms}&amp;p={startPage?}&amp;c={count?}&amp;l={language?}&amp;guest={alf:guest?}"/>
  <Image height="16" width="16" type="image/x-icon">${absurl(url.context)}/images/logo/AlfrescoLogo16.ico</Image>
</OpenSearchDescription>