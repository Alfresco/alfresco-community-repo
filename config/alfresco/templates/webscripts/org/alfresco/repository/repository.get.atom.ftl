[#ftl]
[#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/]
<?xml version="1.0" encoding="utf-8"?> 
<service [@nsLib.serviceNS/]>
  <workspace cmis:id="${server.id}">
    <atom:title>${server.name}</atom:title>

    <cmis:repositoryInfo> 
      <cmis:repositoryId>${server.id}</cmis:repositoryId>
      <cmis:repositoryName>${server.name}</cmis:repositoryName>
      <cmis:repositoryDescription></cmis:repositoryDescription>   [#-- TODO --]      
      <cmis:vendorName>Alfresco</cmis:vendorName> 
      <cmis:productName>Alfresco Repository (${server.edition})</cmis:productName>
      <cmis:productVersion>${server.version}</cmis:productVersion> 
      <cmis:capabilities>
        <cmis:capabilityMultifiling>true</cmis:capabilityMultifiling>
        <cmis:capabilityUnfiling>false</cmis:capabilityUnfiling>
        <cmis:capabilityVersionSpecificFiling>false</cmis:capabilityVersionSpecificFiling>        
        <cmis:capabilityPWCUpdateable>true</cmis:capabilityPWCUpdateable>
        <cmis:capabilityAllVersionsSearchable>${allVersionsSearchable?string}</cmis:capabilityAllVersionsSearchable>
        <cmis:capabilityJoin>[@capabilityJoin joinSupport/]</cmis:capabilityJoin>
        <cmis:capabilityFullText>[@capabilityFullText fullTextSupport/]</cmis:capabilityFullText>
      </cmis:capabilities> 
      <cmis:cmisVersionsSupported>${cmisVersion}</cmis:cmisVersionsSupported>
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
    <collection href="${absurl(url.serviceContext)}/api/types" cmis:collectionType="types"> 
      <atom:title>type collection</atom:title> 
    </collection> 
    <collection href="${absurl(url.serviceContext)}/api/query" cmis:collectionType="query"> 
      <atom:title>query collection</atom:title> 
    </collection>
     
  </workspace> 
</service> 


[#macro capabilityJoin join]
[#if join = "NO_JOIN_SUPPORT"]noJoin[#elseif join = "INNER_JOIN_SUPPORT"]innerOnly[#elseif join = "INNER_AND_OUTER_JOIN_SUPPORT"]innerAndOuter[/#if][/#macro]

[#macro capabilityFullText fulltext]
[#if fulltext = "NO_FULL_TEXT"]none[#elseif fulltext = "FULL_TEXT_ONLY"]fulltextonly[#elseif fulltext = "FULL_TEXT_AND_STRUCTURED"]fulltextandstructured[/#if][/#macro]
