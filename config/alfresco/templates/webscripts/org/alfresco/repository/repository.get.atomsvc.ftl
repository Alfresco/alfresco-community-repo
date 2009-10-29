[#ftl]
[#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/]
[#import "/org/alfresco/cmis/links.lib.atom.ftl" as linksLib/]
<?xml version="1.0" encoding="utf-8"?> 
<service [@nsLib.serviceNS/]>
  <workspace cmis:id="${server.id}" cmis:repositoryRelationship="self">
    <atom:title>${server.name}</atom:title>

    <collection href="${absurl(url.serviceContext)}[@linksLib.nodeuri defaultRootFolder/]/children" cmisra:collectionType="root"> 
      <atom:title>root collection</atom:title> 
    </collection> 
    <collection href="${absurl(url.serviceContext)}/api/types" cmisra:collectionType="types"> 
      <atom:title>type collection</atom:title> 
    </collection>
    <collection href="${absurl(url.serviceContext)}/api/checkedout" cmisra:collectionType="checkedout"> 
      <atom:title>checkedout collection</atom:title> 
      <accept>${cmisconstants.MIMETYPE_ENTRY}</accept>
    </collection> 
    <collection href="${absurl(url.serviceContext)}/api/unfiled" cmisra:collectionType="unfiled"> 
      <atom:title>unfiled collection</atom:title> 
      <accept>${cmisconstants.MIMETYPE_ENTRY}</accept>
    </collection>
    <collection href="${absurl(url.serviceContext)}/api/queries" cmisra:collectionType="query"> 
      <atom:title>query collection</atom:title> 
      <accept>${cmisconstants.MIMETYPE_CMIS_QUERY}</accept>
    </collection>

    <atom:link title="root folder tree" type="${cmisconstants.MIMETYPE_CMISTREE}" rel="http://docs.oasis-open.org/ns/cmis/link/200901/foldertree" href="${absurl(url.serviceContext)}[@linksLib.nodeuri defaultRootFolder/]/tree"/>
    <atom:link title="root descendants" type="${cmisconstants.MIMETYPE_CMISTREE}" rel="http://docs.oasis-open.org/ns/cmis/link/200901/rootdescendants" href="${absurl(url.serviceContext)}[@linksLib.nodeuri defaultRootFolder/]/descendants"/>
    <atom:link title="type descendants" type="${cmisconstants.MIMETYPE_CMISTREE}" rel="http://docs.oasis-open.org/ns/cmis/link/200901/typesdescendants" href="${absurl(url.serviceContext)}/api/types/descendants"/>

    <cmisra:repositoryInfo>
      <cmis:repositoryId>${server.id}</cmis:repositoryId>
      <cmis:repositoryName>${server.name}</cmis:repositoryName>
      <cmis:repositoryRelationship>self</cmis:repositoryRelationship>
      <cmis:repositoryDescription></cmis:repositoryDescription>   [#-- TODO --]
      <cmis:vendorName>Alfresco</cmis:vendorName> 
      <cmis:productName>Alfresco Repository (${server.edition})</cmis:productName>
      <cmis:productVersion>${server.version}</cmis:productVersion>
      <cmis:rootFolderId>[@linksLib.noderef defaultRootFolder/]</cmis:rootFolderId>
      [#-- TODO: implement change log --]
      <cmis:latestChangeToken></cmis:latestChangeToken>
      <cmis:capabilities>
        <cmis:capabilityACL>[#-- TODO --]none</cmis:capabilityACL>
        <cmis:capabilityAllVersionsSearchable>${allVersionsSearchable?string}</cmis:capabilityAllVersionsSearchable>
        <cmis:capabilityChanges>[#-- TODO --]none</cmis:capabilityChanges>
        <cmis:capabilityChangesOnType>cmis:document</cmis:capabilityChangesOnType>
        <cmis:capabilityContentStreamUpdatability>anytime</cmis:capabilityContentStreamUpdatability>
        <cmis:capabilityGetDescendants>true</cmis:capabilityGetDescendants>
        <cmis:capabilityMultifiling>true</cmis:capabilityMultifiling>
        <cmis:capabilityPWCSearchable>${pwcSearchable?string}</cmis:capabilityPWCSearchable>
        <cmis:capabilityPWCUpdateable>true</cmis:capabilityPWCUpdateable>
        <cmis:capabilityQuery>${querySupport}</cmis:capabilityQuery>
        [#-- TODO: implement rendition spec --]
        <cmis:capabilityRenditions>none</cmis:capabilityRenditions>
        <cmis:capabilityUnfiling>false</cmis:capabilityUnfiling>
        <cmis:capabilityVersionSpecificFiling>false</cmis:capabilityVersionSpecificFiling>
        <cmis:capabilityJoin>${joinSupport}</cmis:capabilityJoin>
      </cmis:capabilities> 
      [#-- TODO: implement ACL spec --]
      [#-- <cmis:aclCapability></cmis:aclCapability> --]
      <cmis:cmisVersionSupported>${cmisVersion}</cmis:cmisVersionSupported>
    </cmisra:repositoryInfo>

    <cmisra:uritemplate>
        <cmisra:template>${absurl(url.serviceContext)}/api/node/{id}?filter={filter}&amp;includeAllowableActions={includeAllowableActions}&amp;includeRelationships={includeRelationships}</cmisra:template>
        <cmisra:type>entrybyid</cmisra:type>
        <cmisra:mediatype>${cmisconstants.MIMETYPE_ENTRY}</cmisra:mediatype>
    </cmisra:uritemplate>
    <cmisra:uritemplate>
        <cmisra:template>${absurl(url.serviceContext)}/api/query?q={q}&amp;includeAllowableActions={includeAllowableActions?}&amp;searchAllVersions={searchAllVersions?}&amp;skipCount={skipCount?}&amp;maxItems={maxItems?}</cmisra:template>
        <cmisra:type>query</cmisra:type>
        <cmisra:mediatype>${cmisconstants.MIMETYPE_FEED}</cmisra:mediatype>
    </cmisra:uritemplate>

  </workspace> 
</service> 
