<#if args.p?exists><#assign path=args.p><#else><#assign path=""></#if>
<#if args.n?exists><#assign node=args.n><#else><#assign node=companyhome></#if>
<#assign searchCommand="OfficeAddin.runSearch('${url.context}/service/office/searchResults', '${path}')" >
<#if (args.searchagain?exists)><#assign searchText=args.searchagain><#else><#assign searchText=""></#if>
<#if (args.maxresults?exists)><#assign maxResults=args.maxresults><#else><#assign maxResults="5"></#if>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
	<title>Browse Spaces and Documents</title>
	<link rel="stylesheet" type="text/css" href="${url.context}/css/office.css" />
   <script type="text/javascript" src="${url.context}/scripts/ajax/mootools.v1.1.js"></script>
	<script type="text/javascript" src="${url.context}/scripts/office/office_addin.js"></script>
</head>
<body>

<div id="tabBar">
   <ul>
      <li><a title="My Alfresco" href="${url.context}/service/office/myAlfresco?p=${path}"><span><img src="${url.context}/images/office/my_alfresco.gif" alt="My Alfresco" /></span></a></li>
      <li><a title="Browse Spaces and Documents" href="${url.context}/service/office/navigation?p=${path}&n=${node.id}"><span><img src="${url.context}/images/office/navigator.gif" alt="Browse Spaces and Documents" /></span></a></li>
      <li id="current"><a title="Search Alfresco" href="${url.context}/service/office/search?p=${path}"><span><img src="${url.context}/images/office/search.gif" alt="Search Alfresco" /></span></a></li>
      <li><a title="View Details" href="${url.context}/service/office/documentDetails?p=${path}"><span><img src="${url.context}/images/office/document_details.gif" alt="View Details" /></span></a></li>
      <li><a title="My Tasks" href="${url.context}/service/office/myTasks?p=${path}"><span><img src="${url.context}/images/office/my_tasks.gif" alt="My Tasks" /></span></a></li>
   </ul>
</div>

<div class="header">Search</div>

<div id="search">
   <table width="100%" border="0" style="font-family: tahoma, sans-serif; font-size: 11px;">
      <tr valign="top">
         <td align="left" valign="middle">
            Search for
            <input type="text" id="searchText" name="searchText" value="${searchText}" maxlength="1024" style="width:140px; font-size:10px" />
            <input type="button" name="simpleSearchButton" id="simpleSearchButton" class="button" onClick="${searchCommand}" value="Search" /><br />
            <br />
            Return a maximum of <select id="maxResults" name="maxResults" onChange="${searchCommand}">
               <option <#if maxResults="5">selected</#if> value="5">5</option>
               <option <#if maxResults="10">selected</#if> value="10">10</option>
               <option <#if maxResults="15">selected</#if>value="15">15</option>
               <option <#if maxResults="20">selected</#if>value="20">20</option>
               <option <#if maxResults="50">selected</#if>value="50">50</option>
               </select>&nbsp;items
            <br/>
         </td>
      </tr>
   </table>
</div>

<div class="header">Items Found</div>

<div class="listBig">
   <div id="searchResultsList"></div>
   <div id="statusText"></div>
</div>

<#if (args.searchagain?exists)>
<script type="text/javascript">
   window.addEvent('domready', function(){${searchCommand}});
</script>
</#if>
</body>
</html>
