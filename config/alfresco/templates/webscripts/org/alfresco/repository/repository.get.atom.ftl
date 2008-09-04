<#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/>
<?xml version="1.0" encoding='utf-8'?> 
<service <@nsLib.serviceNS/>>
  <workspace cmis:id="${server.id}"> 
    <atom:title>${server.name}</atom:title>

    <#-- TODO: cmis version -->

    <cmis:repositoryInfo> 
      <cmis:repositoryId>${server.id}</cmis:repositoryId>
      <cmis:repositoryName>${server.name}</cmis:repositoryName>
      <cmis:repositoryDescription></cmis:repositoryDescription>   <#-- TODO -->      
      <cmis:vendorName>Alfresco</cmis:vendorName> 
      <cmis:productName>Alfresco Repository (${server.edition})</cmis:productName>
      <cmis:productVersion>${server.version}</cmis:productVersion> 
      <cmis:capabilities>
        <cmis:capabilityMultifiling>true</cmis:capabilityMultifiling>
        <cmis:capabilityUnfiling>false</cmis:capabilityUnfiling>
        <cmis:capabilityVersionSpecificFiling>false</cmis:capabilityVersionSpecificFiling>        
        <cmis:capabilityAllVersionsSearchable>false</cmis:capabilityAllVersionsSearchable>
        <cmis:capabilityPWCUpdatable>true</cmis:capabilityPWCUpdatable>
        <cmis:capabilityInnerJoin>true</cmis:capabilityInnerJoin>
        <cmis:capabilityOuterJoin>true</cmis:capabilityOuterJoin>
      </cmis:capabilities> 
      <cmis:repositorySpecificInfo></cmis:repositorySpecificInfo>
    </cmis:repositoryInfo>

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
