<#macro document node>
  <#-- TODO: link elements (both APP and CMIS) -->
  <entry>
    <title>${node.name}</title>
    <link rel="alternate" href="${absurl(url.serviceContext)}${node.url}"/>
    <id>urn:uuid:${node.id}</id>
    <updated>${xmldate(node.properties.modified)}</updated>
    <published>${xmldate(node.properties.created)}</published>
    <summary>${node.properties.description!""}</summary>
    <author><name>${node.properties.creator}</name></author> 
    <alf:noderef>${node.nodeRef}</alf:noderef>
    <alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
    <#-- TODO: full cmis schema -->
    <cmis:object>
      <cmis:object_id>${node.id}</cmis:object_id>    
      <cmis:baseType>document</cmis:baseType>    
    </cmis:object>
  </entry>
</#macro>

<#macro folder node>
  <#-- TODO: link elements (both APP and CMIS) -->
  <entry>
    <title>${node.name}</title>
    <link rel="alternate" href="${absurl(url.serviceContext)}${node.url}"/>
    <id>urn:uuid:${node.id}</id>
    <updated>${xmldate(node.properties.modified)}</updated>
    <published>${xmldate(node.properties.created)}</published>
    <summary>${node.properties.description!""}</summary>
    <author><name>${node.properties.creator!""}</name></author> 
    <alf:noderef>${node.nodeRef}</alf:noderef>
    <alf:icon>${absurl(url.context)}${node.icon16}</alf:icon>
    <#-- TODO: full cmis schema -->
    <cmis:object>
      <cmis:object_id>${node.id}</cmis:object_id>    
      <cmis:baseType>folder</cmis:baseType>    
    </cmis:object>
  </entry>
</#macro>