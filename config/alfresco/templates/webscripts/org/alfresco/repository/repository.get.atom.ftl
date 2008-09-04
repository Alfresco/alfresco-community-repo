[#ftl]
[#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/]
<?xml version="1.0" encoding="utf-8"?> 
<service [@nsLib.serviceNS/]>
  <workspace cmis:repositoryRelationship="self">
    <atom:title>${server.name}</atom:title>

    <cmis:repositoryInfo> 
      <cmis:repositoryId>${server.id}</cmis:repositoryId>
      <cmis:repositoryName>${server.name}</cmis:repositoryName>
      <cmis:repositoryRelationship>self</cmis:repositoryRelationship>
      <cmis:repositoryDescription></cmis:repositoryDescription>   [#-- TODO --]      
      <cmis:vendorName>Alfresco</cmis:vendorName> 
      <cmis:productName>Alfresco Repository (${server.edition})</cmis:productName>
      <cmis:productVersion>${server.version}</cmis:productVersion>
      <cmis:rootFolderId>${absurl(url.serviceContext)}/api/path/${encodeuri(defaultRootFolderPath)}/children</cmis:rootFolderId> 
      <cmis:capabilities>
        <cmis:capabilityMultifiling>true</cmis:capabilityMultifiling>
        <cmis:capabilityUnfiling>false</cmis:capabilityUnfiling>
        <cmis:capabilityVersionSpecificFiling>false</cmis:capabilityVersionSpecificFiling>        
        <cmis:capabilityPWCUpdateable>true</cmis:capabilityPWCUpdateable>
        <cmis:capabilityPWCSearchable>${pwcSearchable?string}</cmis:capabilityPWCSearchable>
        <cmis:capabilityAllVersionsSearchable>${allVersionsSearchable?string}</cmis:capabilityAllVersionsSearchable>
        <cmis:capabilityQuery>${querySupport}</cmis:capabilityQuery>
        <cmis:capabilityJoin>${joinSupport}</cmis:capabilityJoin>
        <cmis:capabilityFullText>${fullTextSupport}</cmis:capabilityFullText>
      </cmis:capabilities> 
      <cmis:cmisVersionsSupported>${cmisVersion}</cmis:cmisVersionsSupported>
      <cmis:repositorySpecificInformation></cmis:repositorySpecificInformation>
    </cmis:repositoryInfo>

    <collection href="${absurl(url.serviceContext)}/api/path/${encodeuri(defaultRootFolderPath)}/children" cmis:collectionType="root-children"> 
      <atom:title>root collection</atom:title> 
    </collection> 
    <collection href="${absurl(url.serviceContext)}/api/path/${encodeuri(defaultRootFolderPath)}/descendants" cmis:collectionType="root-descendants"> 
      <atom:title>root collection</atom:title> 
    </collection> 
    <collection href="${absurl(url.serviceContext)}/api/checkedout" cmis:collectionType="checkedout"> 
      <atom:title>checkedout collection</atom:title> 
    </collection> 
    <collection href="${absurl(url.serviceContext)}/api/unfiled" cmis:collectionType="unfiled"> 
      <atom:title>unfiled collection</atom:title> 
    </collection>
    <collection href="${absurl(url.serviceContext)}/api/types" cmis:collectionType="types-children"> 
      <atom:title>type collection</atom:title> 
    </collection>
    [#-- TODO: wait for 0.5 spec --] 
    <collection href="${absurl(url.serviceContext)}/api/types" cmis:collectionType="types-descendants"> 
      <atom:title>type collection</atom:title> 
    </collection> 
    [#-- TODO: --]
    <collection href="${absurl(url.serviceContext)}/api/query" cmis:collectionType="query"> 
      <atom:title>query collection</atom:title> 
    </collection>
     
  </workspace> 
</service> 
