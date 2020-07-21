[#ftl]
[#-- TODO: Filter out Data Dictionary and all sub-folders --]
[#macro fullPath node]
  [@compress single_line=true]
    [#if node == companyhome]
/Company Home
    [#else]
[@fullPath node=node.parent/]/${node.properties.name}
    [/#if]
  [/@compress]
[/#macro]
[#assign luceneQuery = "@cm\\:name:\"" + args.query + "*\" AND TYPE:\\{http\\://www.alfresco.org/model/content/1.0\\}folder AND NOT TYPE:\\{http\\://www.alfresco.org/model/wcmappmodel/1.0\\}webfolder AND PATH:\"app\\:company_home//*\""]
[#assign matches     = companyhome.childrenByLuceneSearch[luceneQuery]]
{
  "data" :
  [
[#list matches as match]
    {
      "path"    : "[@fullPath node=match/]",
      "nodeRef" : "${match.nodeRef}"
    }[#if match != matches?last],[/#if]
[/#list]
  ]
}
