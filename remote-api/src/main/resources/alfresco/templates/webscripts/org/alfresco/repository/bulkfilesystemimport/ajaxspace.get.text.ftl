[#ftl]
[#-- TODO: Filter out Data Dictionary and all sub-folders --]
[#macro fullPath node][#if node == companyhome]/Company Home[#else][@fullPath node=node.parent/]/${node.properties.name}[/#if][/#macro]
[#assign luceneQuery = "@cm\\:name:\"" + args.query + "*\" AND TYPE:\\{http\\://www.alfresco.org/model/content/1.0\\}folder AND NOT TYPE:\\{http\\://www.alfresco.org/model/wcmappmodel/1.0\\}webfolder"]
[#assign matches     = companyhome.childrenByLuceneSearch[luceneQuery]]
[#list matches as match]
[@fullPath node=match/],${match.nodeRef}
[/#list]
