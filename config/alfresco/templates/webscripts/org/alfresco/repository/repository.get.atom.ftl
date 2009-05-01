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
      <cmis:rootFolderId>${absurl(url.serviceContext)}/api/path/${encodeuri(defaultRootFolderPath)}</cmis:rootFolderId> 
      <cmis:capabilities>
        <cmis:capabilityMultifiling>true</cmis:capabilityMultifiling>
        <cmis:capabilityUnfiling>false</cmis:capabilityUnfiling>
        <cmis:capabilityVersionSpecificFiling>false</cmis:capabilityVersionSpecificFiling>        
        <cmis:capabilityPWCUpdateable>true</cmis:capabilityPWCUpdateable>
        <cmis:capabilityPWCSearchable>${pwcSearchable?string}</cmis:capabilityPWCSearchable>
        <cmis:capabilityAllVersionsSearchable>${allVersionsSearchable?string}</cmis:capabilityAllVersionsSearchable>
        <cmis:capabilityQuery>${querySupport}</cmis:capabilityQuery>
        <cmis:capabilityJoin>${joinSupport}</cmis:capabilityJoin>
        [#-- TODO: wait for unified search proposal before implementing --] 
        <cmis:capabilityChanges>none</cmis:capabilityChanges>
        <cmis:changesIncomplete>false</cmis:changesIncomplete>
        [#-- TODO: --] 
      </cmis:capabilities> 
      <cmis:cmisVersionSupported>${cmisVersion}</cmis:cmisVersionSupported>
      <cmis:repositorySpecificInformation></cmis:repositorySpecificInformation>
    </cmis:repositoryInfo>

    <collection href="${absurl(url.serviceContext)}/api/path/${encodeuri(defaultRootFolderPath)}/children" cmis:collectionType="rootchildren"> 
      <atom:title>root collection</atom:title> 
    </collection> 
    <collection href="${absurl(url.serviceContext)}/api/path/${encodeuri(defaultRootFolderPath)}/descendants" cmis:collectionType="rootdescendants"> 
      <atom:title>root collection</atom:title> 
    </collection> 
    <collection href="${absurl(url.serviceContext)}/api/checkedout" cmis:collectionType="checkedout"> 
      <atom:title>checkedout collection</atom:title> 
      <atom:accept>application/atom+xml;type=entry</atom:accept>
    </collection> 
    <collection href="${absurl(url.serviceContext)}/api/unfiled" cmis:collectionType="unfiled"> 
      <atom:title>unfiled collection</atom:title> 
      <atom:accept>application/atom+xml;type=entry</atom:accept>
    </collection>
    <collection href="${absurl(url.serviceContext)}/api/types" cmis:collectionType="typeschildren"> 
      <atom:title>type collection</atom:title> 
    </collection>
    <collection href="${absurl(url.serviceContext)}/api/types" cmis:collectionType="typesdescendants"> 
      <atom:title>type collection</atom:title> 
    </collection> 
    <collection href="${absurl(url.serviceContext)}/api/query" cmis:collectionType="query"> 
      <atom:title>query collection</atom:title> 
      <atom:accept>application/cmisquery+xml</atom:accept>
    </collection>
     
  </workspace> 
</service> 
