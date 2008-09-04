<?xml version="1.0" encoding="UTF-8"?>
<feed xmlns="http://www.w3.org/2005/Atom" xmlns:cmis="http://www.cmis.org/CMIS/1.0" xmlns:alf="http://www.alfresco.org" xmlns:opensearch="http://a9.com/-/spec/opensearch/1.1/">
  <generator version="${server.version}">Alfresco (${server.edition})</generator>
  <title>Unfiled Documents</title> 
  <updated>${xmldate(date)}</updated>
  <icon>${absurl(url.context)}/images/logo/AlfrescoLogo16.ico</icon>
  <author> 
    <name>System</name>
  </author> 
  <id>urn:uuid:unfiled</id>
  <link rel="self" href="${absurl(url.full)?xml}" type="${format.type}"/>
  <#-- NOTE: Alfresco does not yet support unfiled documents -->
</feed>
