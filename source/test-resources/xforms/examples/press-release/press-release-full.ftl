[#ftl ns_prefixes={"D", "http://www.alfresco.org/alfresco/press-release"}]
<html>
  <head>
    <title>
      ${PressRelease.Title}
    </title>
[#if PressRelease.Summary?exists]
    <meta name="description" content="${PressRelease.Summary}">
[/#if]
[#if PressRelease.Keyword?exists]
  [#if PressRelease.Keyword?size > 0]
    [#assign keywordString = ""]
    [#list PressRelease.Keyword as Keyword]
      [#assign keywordString = keywordString + "," + Keyword]
    [/#list]
    <meta name="keywords" content="${keywordString}">
  [/#if]
[/#if]
  </head>
  <body>
    <h1>${PressRelease.Title}</h1>
    <p>${PressRelease.Body}</p>
[#if PressRelease.Image?exists]
    <img src="${PressRelease.Image}">
[/#if]

<!-- TEST -->
<hr>
[#list avm.stores as store]
  ${store.name}<br>
[/#list]
<hr>
<!-- TEST END -->

  </body>
</html>
