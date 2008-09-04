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
        <cmis:capabilityPWCUpdateable>true</cmis:capabilityPWCUpdateable>
        <cmis:capabilityAllVersionsSearchable>false</cmis:capabilityAllVersionsSearchable>
        <cmis:capabilityInnerJoin>true</cmis:capabilityInnerJoin>
        <cmis:capabilityOuterJoin>true</cmis:capabilityOuterJoin>
        <cmis:capabilityFullText>fulltextwithmetadata</cmis:capabilityFullText>
      </cmis:capabilities> 
      <cmis:repositorySpecificInformation></cmis:repositorySpecificInformation>
    </cmis:repositoryInfo>

    <collection href="${absurl(url.serviceContext)}/api/path/${encodeuri(defaultRootFolderPath)}/children" cmis:collectionType="root"> 
      <atom:title>root collection</atom:title> 
    </collection> 
    <collection href="${absurl(url.serviceContext)}/api/checkedout" cmis:collectionType="checkedout"> 
      <atom:title>checkedout collection</atom:title> 
    </collection> 
    <collection href="${absurl(url.serviceContext)}/api/unfiled" cmis:collectionType="unfiled"> 
      <atom:title>unfiled collection</atom:title> 
    </collection>
    
    <#-- TODO: collection resources -->
    <collection href="http://example.org/cmis/main?types" cmis:collectionType="types"> 
      <atom:title>CMIS Types</atom:title> 
    </collection> 

  </workspace> 
</service> 
