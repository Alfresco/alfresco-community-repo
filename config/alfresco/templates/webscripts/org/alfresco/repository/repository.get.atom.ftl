<?xml version="1.0" encoding='utf-8'?> 
<service xmlns="http://www.w3.org/2007/app" xmlns:atom="http://www.w3.org/2005/Atom" xmlns:cmis="http://www.cmis.org/CMIS/1.0">
  <workspace cmis:id="${server.id}"> 
    <atom:title>${server.name}</atom:title>
     
    <cmis:repository_info> 
      <cmis:vendorName>Alfresco</cmis:vendorName> 
      <cmis:productName>Alfresco Repository (${server.edition})</cmis:productName>
      <cmis:productVersion>${server.version}</cmis:productVersion> 
      <cmis:capabilities>
        <cmis:capabilityMultifiling>true</cmis:capabilityMultifiling>
        <cmis:capabilityAllVersionsSearchable>false</cmis:capabilityAllVersionsSearchable>
        <cmis:capabilityPWCUpdatable>true</cmis:capabilityPWCUpdatable>
      </cmis:capabilities> 
      <cmis:description></cmis:description>
      <cmis:repositoryInfo></cmis:repositoryInfo>
    </cmis:repository_info>

    <#-- TODO: cmis:id on collection elements - are they required by cmis? -->
    <#-- TODO: collection resources -->
     
    <collection href="${absurl(url.serviceContext)}/api/path/workspace/SpacesStore//children" cmis:collectionType="root"> 
      <atom:title>CMIS root folder</atom:title> 
    </collection> 
    <collection href="http://example.org/cmis/main?checkedout" cmis:collectionType="checkedout"> 
      <atom:title>CMIS checked-out documents</atom:title> 
    </collection> 
    <collection href="http://example.org/cmis/main?types" cmis:collectionType="types"> 
      <atom:title>CMIS Types</atom:title> 
    </collection> 

    <#-- NOTE: alfresco does not support notion of unfiled...
    <collection href="http://example.org/cmis/main?unfiled" cmis:collectionType="unfiled"> 
      <atom:title>CMIS unfiled documents</atom:title> 
    </collection>
    -->
         
  </workspace> 
</service> 
