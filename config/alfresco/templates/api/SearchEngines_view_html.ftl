<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head profile="http://a9.com/-/spec/opensearch/1.1/"> 
    <title>Search Engines registered with Alfresco</title>
<#list engines as engine>
<#if engine.urlType == "description">
    <link rel="search" type="${engine.type}" href="${absurl(engine.url)}" title="${engine.label}">
</#if>
</#list>
  </head>
  <body>
    <h2>Search Engines registered with Alfresco</h2>
    <ul>
<#list engines as engine>
      <li>
        <a href="${absurl(engine.url)}">${engine.label}</a> (<#if engine.urlType == "description">OpenSearch Description<#else>Template URL</#if> - ${engine.type})
      </li>
</#list>
    </ul>
  </body>
</html>