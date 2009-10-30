[#ftl]
[#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/]
[#import "/org/alfresco/cmis/links.lib.atom.ftl" as linksLib/]
<?xml version="1.0" encoding="utf-8"?> 
<service [@nsLib.serviceNS/]>
  <workspace>
    <atom:title>${server.name}</atom:title>

    <collection href="${absurl(url.serviceContext)}[@linksLib.nodeuri defaultRootFolder/]/children"> 
      <atom:title>root collection</atom:title> 
      <cmisra:collectionType>root</cmisra:collectionType>
    </collection> 
    <collection href="${absurl(url.serviceContext)}/cmis/types"> 
      <atom:title>type collection</atom:title> 
      <cmisra:collectionType>types</cmisra:collectionType>
    </collection>
    <collection href="${absurl(url.serviceContext)}/cmis/checkedout"> 
      <atom:title>checkedout collection</atom:title> 
      <accept>${cmisconstants.MIMETYPE_ENTRY}</accept>
      <cmisra:collectionType>checkedout</cmisra:collectionType>
    </collection> 
    <collection href="${absurl(url.serviceContext)}/cmis/unfiled"> 
      <atom:title>unfiled collection</atom:title> 
      <accept>${cmisconstants.MIMETYPE_ENTRY}</accept>
      <cmisra:collectionType>unfiled</cmisra:collectionType>
    </collection>
    <collection href="${absurl(url.serviceContext)}/cmis/queries"> 
      <atom:title>query collection</atom:title> 
      <accept>${cmisconstants.MIMETYPE_CMIS_QUERY}</accept>
      <cmisra:collectionType>query</cmisra:collectionType>
    </collection>

    <atom:link title="root folder tree" type="${cmisconstants.MIMETYPE_CMISTREE}" rel="${cmisconstants.REL_FOLDER_TREE}" href="${absurl(url.serviceContext)}[@linksLib.nodeuri defaultRootFolder/]/tree"/>
    <atom:link title="root descendants" type="${cmisconstants.MIMETYPE_CMISTREE}" rel="${cmisconstants.REL_ROOT_DESCENDANTS}" href="${absurl(url.serviceContext)}[@linksLib.nodeuri defaultRootFolder/]/descendants"/>
    <atom:link title="type descendants" type="${cmisconstants.MIMETYPE_CMISTREE}" rel="${cmisconstants.REL_TYPES_DESCENDANTS}" href="${absurl(url.serviceContext)}/cmis/types/descendants"/>

    <cmisra:repositoryInfo>
      <cmis:repositoryId>${server.id}</cmis:repositoryId>
      <cmis:repositoryName>${server.name}</cmis:repositoryName>
      <cmis:repositoryDescription></cmis:repositoryDescription>   [#-- TODO --]
      <cmis:vendorName>Alfresco</cmis:vendorName> 
      <cmis:productName>Alfresco Repository (${server.edition})</cmis:productName>
      <cmis:productVersion>${server.version}</cmis:productVersion>
      <cmis:rootFolderId>[@linksLib.noderef defaultRootFolder/]</cmis:rootFolderId>
      [#-- TODO: implement change log --]
      [#-- TODO: <cmis:latestChangeLogToken></cmis:latestChangeLogToken> --]
      <cmis:capabilities>
        <cmis:capabilityACL>[#-- TODO --]none</cmis:capabilityACL>
        <cmis:capabilityAllVersionsSearchable>${allVersionsSearchable?string}</cmis:capabilityAllVersionsSearchable>
        <cmis:capabilityChanges>[#-- TODO --]none</cmis:capabilityChanges>
        <cmis:capabilityContentStreamUpdatability>anytime</cmis:capabilityContentStreamUpdatability>
        <cmis:capabilityGetDescendants>true</cmis:capabilityGetDescendants>
        <cmis:capabilityGetFolderTree>true</cmis:capabilityGetFolderTree>
        <cmis:capabilityMultifiling>true</cmis:capabilityMultifiling>
        <cmis:capabilityPWCSearchable>${pwcSearchable?string}</cmis:capabilityPWCSearchable>
        <cmis:capabilityPWCUpdatable>true</cmis:capabilityPWCUpdatable>
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
      [#-- TODO: implement change log --]
      [#-- TODO: <cmis:changesIncomplete></cmis:changesIncomplete> --]
      [#-- TODO: <cmis:changesOnType></cmis:changesOnType> --]
    </cmisra:repositoryInfo>

    <cmisra:uritemplate>
        <cmisra:template>${absurl(url.serviceContext)}/cmis/arg/n?noderef={id}&amp;filter={filter}&amp;includeAllowableActions={includeAllowableActions}&amp;includePolicyIds={includePolicyIds}&amp;includeRelationships={includeRelationships}&amp;includeACL={includeACL}&amp;renditionFilter={renditionFilter}</cmisra:template>
        <cmisra:type>${cmisconstants.URI_OBJECT_BY_ID}</cmisra:type>
        <cmisra:mediatype>${cmisconstants.MIMETYPE_ENTRY}</cmisra:mediatype>
    </cmisra:uritemplate>
    <cmisra:uritemplate>
        [#-- NOTE: path provided as URL argument for safe handling by URI template generators --]
        <cmisra:template>${absurl(url.serviceContext)}/cmis/s/${defaultRootFolder.storeType}:${defaultRootFolder.storeId}/arg/p?path={path}&amp;filter={filter}&amp;includeAllowableActions={includeAllowableActions}&amp;includePolicyIds={includePolicyIds}&amp;includeRelationships={includeRelationships}&amp;includeACL={includeACL}&amp;renditionFilter={renditionFilter}</cmisra:template>
        <cmisra:type>${cmisconstants.URI_OBJECT_BY_PATH}</cmisra:type>
        <cmisra:mediatype>${cmisconstants.MIMETYPE_ENTRY}</cmisra:mediatype>
    </cmisra:uritemplate>
    <cmisra:uritemplate>
        <cmisra:template>${absurl(url.serviceContext)}/cmis/type/{id}</cmisra:template>
        <cmisra:type>${cmisconstants.URI_TYPE_BY_ID}</cmisra:type>
        <cmisra:mediatype>${cmisconstants.MIMETYPE_ENTRY}</cmisra:mediatype>
    </cmisra:uritemplate>
    <cmisra:uritemplate>
        <cmisra:template>${absurl(url.serviceContext)}/cmis/query?q={q}&amp;searchAllVersions={searchAllVersions}&amp;maxItems={maxItems}&amp;skipCount={skipCount}&amp;includeAllowableActions={includeAllowableActions}&amp;includeRelationships={includeRelationships}</cmisra:template>
        <cmisra:type>${cmisconstants.URI_QUERY}</cmisra:type>
        <cmisra:mediatype>${cmisconstants.MIMETYPE_FEED}</cmisra:mediatype>
    </cmisra:uritemplate>

  </workspace> 
</service> 
