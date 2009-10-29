[#ftl]
[#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/]
<?xml version="1.0" encoding="utf-8"?> 
<service [@nsLib.serviceNS/]>
  <workspace>
    <atom:title>${server.name}</atom:title>

    <collection href="${absurl(url.serviceContext)}/api/path/${encodeuri(defaultRootFolderPath)}/children" cmisra:collectionType="root"> 
      <atom:title>root collection</atom:title> 
    </collection> 
    <collection href="${absurl(url.serviceContext)}/api/checkedout" cmisra:collectionType="checkedout"> 
      <atom:title>checkedout collection</atom:title> 
      <accept>application/atom+xml;type=entry</accept>
    </collection> 
    <collection href="${absurl(url.serviceContext)}/api/unfiled" cmisra:collectionType="unfiled"> 
      <atom:title>unfiled collection</atom:title> 
      <accept>application/atom+xml;type=entry</accept>
    </collection>
    <collection href="${absurl(url.serviceContext)}/api/types" cmisra:collectionType="types"> 
      <atom:title>type collection</atom:title> 
    </collection>
    <collection href="${absurl(url.serviceContext)}/api/query" cmisra:collectionType="query"> 
      <atom:title>query collection</atom:title> 
      <accept>application/cmisquery+xml</accept>
    </collection>

    <cmisra:repositoryInfo> 
      <cmis:repositoryId>${server.id}</cmis:repositoryId>
      <cmis:repositoryName>${server.name}</cmis:repositoryName>
      <cmis:repositoryRelationship>self</cmis:repositoryRelationship>
      <cmis:repositoryDescription></cmis:repositoryDescription>   [#-- TODO --]
      <cmis:vendorName>Alfresco</cmis:vendorName> 
      <cmis:productName>Alfresco Repository (${server.edition})</cmis:productName>
      <cmis:productVersion>${server.version}</cmis:productVersion>
      <cmis:rootFolderId>${absurl(url.serviceContext)}/api/path/${encodeuri(defaultRootFolderPath)}</cmis:rootFolderId>
      [#-- TODO: implement change log --]
      <cmis:latestChangeToken></cmis:latestChangeToken>
      <cmis:capabilities>
        <cmis:capabilityACL>[#-- TODO --]none</cmis:capabilityACL>
        <cmis:capabilityAllVersionsSearchable>${allVersionsSearchable?string}</cmis:capabilityAllVersionsSearchable>
        <cmis:capabilityChanges>[#-- TODO --]none</cmis:capabilityChanges>
        [#-- ISSUE CMIS-342 --]<cmis:capabilityChangesOnType>cmis:Document</cmis:capabilityChangesOnType>
        <cmis:capabilityContentStreamUpdates>[#-- TODO, ISSUE CMIS-342 --]anytime</cmis:capabilityContentStreamUpdates>
        <cmis:capabilityDescendantNavigation>[#-- TODO CMIS-342 --]true</cmis:capabilityDescendantNavigation>
        <cmis:capabilityMultifiling>true</cmis:capabilityMultifiling>
        <cmis:capabilityPWCSearchable>${pwcSearchable?string}</cmis:capabilityPWCSearchable>
        <cmis:capabilityPWCUpdateable>true</cmis:capabilityPWCUpdateable>
        <cmis:capabilityQuery>${querySupport}</cmis:capabilityQuery>
        <cmis:capabilityRenditions>[#-- TODO, ISSUE CMIS-342 --]false</cmis:capabilityRenditions>
        <cmis:capabilityUnfiling>false</cmis:capabilityUnfiling>
        <cmis:capabilityVersionSpecificFiling>false</cmis:capabilityVersionSpecificFiling>
        <cmis:capabilityJoin>${joinSupport}</cmis:capabilityJoin>
      </cmis:capabilities> 
      [#-- TODO: wait for ACL proposal before implementing --]
      [#-- <cmis:aclCapability></cmis:aclCapability> --]
      <cmis:cmisVersionSupported>${cmisVersion}</cmis:cmisVersionSupported>
    </cmisra:repositoryInfo>

  </workspace> 
</service> 
