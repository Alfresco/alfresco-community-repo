<#assign path=args.p!"">
<#if args.n??><#assign node=args.n><#else><#assign node=companyhome></#if>
<#assign extn=args.e!"doc"><#assign extnx=extn+"x">
<#assign nav=args.n!"">
<#if (args.searchagain??)><#assign searchText=args.searchagain><#else><#assign searchText=""></#if>
<#if (args.maxresults??)><#assign maxResults=args.maxresults><#else><#assign maxResults="5"></#if>
<#assign defaultQuery="?p=" + path?url + "&e=" + extn + "&n=" + nav>
<#assign searchCommand="OfficeSearch.runSearch('${url.serviceContext}/office/searchResults', '${defaultQuery?js_string}')" >
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
   <meta http-equiv="content-type" content="text/html; charset=UTF-8" />
   <title>${message("office.title.search")}</title>
   <link rel="stylesheet" type="text/css" href="${url.context}/css/office.css" />
<!--[if IE 6]>
   <link rel="stylesheet" type="text/css" href="${url.context}/css/office_ie6.css" />
<![endif]-->
   <script type="text/javascript" src="${url.context}/scripts/ajax/mootools.v1.11.js"></script>
   <script type="text/javascript" src="${url.context}/scripts/office/office_addin.js"></script>
   <script type="text/javascript" src="${url.context}/scripts/office/search.js"></script>
   <script type="text/javascript" src="${url.context}/scripts/office/external_component.js"></script>
   <script type="text/javascript">//<![CDATA[
      OfficeAddin.defaultQuery = "${defaultQuery}";
      ExternalComponent.init(
      {
         folderPath: "${url.serviceContext}/office/",
         ticket: "${session.ticket}"
      });
   //]]></script>
</head>
<body>
<div class="tabBar">
   <ul>
      <li><a title="${message("office.title.my_alfresco")}" href="${url.serviceContext}/office/myAlfresco${defaultQuery?html}"><span><img src="${url.context}/images/office/my_alfresco.gif" alt="${message("office.title.my_alfresco")}" /></span></a></li>
      <li><a title="${message("office.title.navigation")}" href="${url.serviceContext}/office/navigation${defaultQuery?html}"><span><img src="${url.context}/images/office/navigator.gif" alt="${message("office.title.navigation")}" /></span></a></li>
      <li id="current"><a title="${message("office.title.search")}" href="${url.serviceContext}/office/search${defaultQuery?html}"><span><img src="${url.context}/images/office/search.gif" alt="${message("office.title.search")}" /></span></a></li>
      <li><a title="${message("office.title.document_details")}" href="${url.serviceContext}/office/documentDetails${defaultQuery?html}"><span><img src="${url.context}/images/office/document_details.gif" alt="${message("office.title.document_details")}" /></span></a></li>
      <li><a title="${message("office.title.my_tasks")}" href="${url.serviceContext}/office/myTasks${defaultQuery?html}"><span><img src="${url.context}/images/office/my_tasks.gif" alt="${message("office.title.my_tasks")}" /></span></a></li>
      <li><a title="${message("office.title.document_tags")}" href="${url.serviceContext}/office/tags${defaultQuery?html}"><span><img src="${url.context}/images/office/tag.gif" alt="${message("office.title.document_tags")}" /></span></a></li>
   </ul>
   <span class="help">
      <a title="${message("office.help.title")}" href="${message("office.help.url")}" target="alfrescoHelp"><img src="${url.context}/images/office/help.gif" alt="${message("office.help.title")}" /></a>
   </span>
</div>

<div class="headerRow">
   <div class="headerWrapper"><div class="header">${message("office.header.search")}</div></div>
</div>

<div class="containerSearchTerms">
   <div id="nonStatusText">
      <div class="searchBox">
         <span class="searchParam">
            ${message("office.message.search_for")}
            <input type="text" id="searchText" value="${searchText}" maxlength="512" />
         </span>
         <span>
            <a id="simpleSearchButton" class="searchButton" href="#" onclick="${searchCommand}">${message("office.button.search")}</a>
         </span>
         <span class="searchParam">
            ${message("office.message.results_max.before")}
            <select id="maxResults" name="maxResults" onchange="${searchCommand}">
               <option <#if maxResults="5">selected="selected" </#if>value="5">5</option>
               <option <#if maxResults="10">selected="selected" </#if>value="10">10</option>
               <option <#if maxResults="15">selected="selected" </#if>value="15">15</option>
               <option <#if maxResults="20">selected="selected" </#if>value="20">20</option>
               <option <#if maxResults="50">selected="selected" </#if>value="50">50</option>
            </select>&nbsp;${message("office.message.results_max.after")}
         </span>
      </div>
   </div>
   
   <div id="statusText"></div>
   
</div>

<div class="headerRow">
   <div class="headerWrapper"><div class="header"><span id="itemsFound">&nbsp;</span></div></div>
</div>

<div id="resultsList" class="containerSearchResults">
   <div id="searchResultsList"></div>
</div>

<#if (args.searchagain??)>
<script type="text/javascript">
   window.addEvent('domready', function(){${searchCommand}});
</script>
</#if>

<div style="position: absolute; top: 0px; left: 0px; z-index: 100; display: none">
   <iframe id="if_externalComponenetMethodCall" name="if_externalComponenetMethodCall" src="" style="visibility: hidden;" width="0" height="0"></iframe>
</div>

</body>
</html>
