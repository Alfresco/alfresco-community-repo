<#list template.parent.children as child>
   <#if child.name = "my_alfresco.ftl"><#assign office_home = child.id>
   <#elseif child.name = "navigation.ftl"><#assign office_browse = child.id>
   <#elseif child.name = "search.ftl"><#assign office_search = child.id>
   <#elseif child.name = "document_details.ftl"><#assign office_details = child.id>
   <#elseif child.name = "version_history.ftl"><#assign office_history = child.id>
   <#elseif child.name = "doc_actions.js"><#assign doc_actions = child.id>
   <#elseif child.name = "search.js"><#assign srch_script = child>
   </#if>
</#list>

<#if args.search?exists>
   <#assign searchString = args.search>
   <#if searchString != "">
      <#assign queryString = "TEXT:\"${searchString}\" @cm\\:title:${searchString}">
   </#if>
<#else>
   <#assign searchString = "">
   <#assign queryString = "">
</#if>

<#if searchString != "">
   <#if args.maxresults?exists>
      <#assign maxresults=args.maxresults?number>
   <#else>
      <#assign maxresults=10>
   </#if>

   <#assign rescount=1>


<!-- Start output -->
          <table>
                 <tbody style="font-family: tahoma, sans-serif; font-size: 11px;">
   <#assign results = companyhome.childrenByLuceneSearch[queryString] >
   <#if results?size = 0>
                 <tr><td>No results found.</td></tr>
   <#else>
      <#list results as child>
            <!-- lb: start repeat -->
         <#if child.isDocument>
            <#if child.name?ends_with(".pdf")>
               <#assign openURL = "/alfresco${child.url}">
               <#assign hrefExtra = " target=\"_blank\"">
            <#else>
               <#assign webdavPath = (child.displayPath?substring(13) + '/' + child.name)?url('ISO-8859-1')?replace('%2F', '/')?replace('\'', '\\\'') />
               <#assign openURL = "#">
               <#assign hrefExtra = " onClick=\"window.external.openDocument('${webdavPath}')\"">
            </#if>
         <#else>
            <#assign openURL = "/alfresco/template/workspace/SpacesStore/${child.id}/workspace/SpacesStore/${office_browse}?search=${searchString}&maxresults=${maxresults}">
            <#assign hrefExtra = "">
         </#if>
                 <tr>
                     <td>
                    <a href="${openURL}" ${hrefExtra}><img src="/alfresco${child.icon32}" border="0" alt="Open ${child.name}" /></a>
                     </td>
                     <td width="100%">
                     <a href="${openURL}" ${hrefExtra} title="Open ${child.name}">${child.name}</a><br/>
         <#if child.properties.description?exists>
                ${child.properties.description}<br/>
         </#if>
         <#if child.isDocument>
                Modified: ${child.properties.modified?datetime}, Size: ${child.size / 1024} Kb<br/>
         </#if>
                       </td>
                     </tr>
            <!-- lb: end repeat -->
         <#if rescount = maxresults>
            <#break>
         </#if>
         <#assign rescount=rescount + 1>
      </#list>
	</#if>
          </tbody>
          </table>
<!-- End output -->

</#if>
<!-- End of returning search results -->

<!-- Display Search UI -->
<#if !args.search?exists>

<!DOCTYPE html
PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
<title>Search</title>

<link rel="stylesheet" type="text/css"
href="/alfresco/css/taskpane.css" />

<script type="text/javascript" src="/alfresco${srch_script.url}" >
</script>

</head>
   <#if args.searchagain?exists>
      <#assign onLoad = "onLoad = \"doSearch('${args.searchagain}', '${args.maxresults}');\"">
   <#else>
      <#assign onLoad = "">
   </#if>
<body ${onLoad}>



<div id="tabBar">
    <ul>
      <li><a href="/alfresco/template/workspace/SpacesStore/${document.id}/workspace/SpacesStore/${office_home}"><img src="/alfresco/images/taskpane/my_alfresco.gif" border="0" alt="My Alfresco" /></a></li>
      <li><a href="/alfresco/template/workspace/SpacesStore/${document.id}/workspace/SpacesStore/${office_browse}"><img src="/alfresco/images/taskpane/navigator.gif" border="0" alt="Browse Spaces and Documents" /></a></li>
      <li id="current" style="padding-right:6px;"><a href="#"><img src="/alfresco/images/taskpane/search.gif" border="0" alt="Search Alfresco" /></a></li>
      <li><a href="/alfresco/template/workspace/SpacesStore/${document.id}/workspace/SpacesStore/${office_details}"><img src="/alfresco/images/taskpane/document_details.gif" border="0" alt="View Details" /></a></li>
      <li><a href="/alfresco/template/workspace/SpacesStore/${document.id}/workspace/SpacesStore/${office_history}"><img src="/alfresco/images/taskpane/version_history.gif" border="0" alt="View Version History" /></a></li>
    </ul>
</div>

<div id="search">
<table width="100%" border="0" style="font-family: tahoma, sans-serif; font-size: 11px;">
   <tr valign="top">
      <td align="left" valign="middle">
         Search for <input type="text" id="searchText" name="searchText" value="" maxlength='1024' style='width:140px;font-size:10px' onkeyup="return handleTextEnter(event);" /><input type="button" name="simpleSearchButton" id="simpleSearchButton" class="button" onClick="javascript:runSearch('${office_search}');" value="Search"/><br/>
<label><SELECT id="maxresults" NAME="maxresults" onchange="javascript:runSearch('${office_search}');">
        <option id="5" name="5" value=5>5</option>
        <option id="10" name="10" value=10>10</option>
        <option id="15" name="15" value=15>15</option>
        <option id="20" name="20" value=20>20</option>
        <option id="50" name="50" value=50>50</option>
        </select>&#160;Items</label><br/>
      </td>
   </tr>
</table>
</div>

<div id="searchResultsListHeader"><span style="font-weight:bold">Items Found</span></div>

<div id="searchResultsList">
   <table>
      <tbody>
      </tbody>
   </table>
</div>

<div id="bottomMargin" style="height:24px;"><span id="statusArea">&nbsp;</span>
</div>

</body>
</html>
</#if>
<!-- End of Search UI -->