<#assign doc_actions="${url.context}/scripts/office/docActions.js">
<#if args.p?exists><#assign path=args.p><#else><#assign path=""></#if>
<#-- resolve the path (from Company Home) into a node -->
<#if path?starts_with("/Company Home")>
   <#if companyhome.childByNamePath[args.p[14..]]?exists>
      <#assign d=companyhome.childByNamePath[args.p[14..]]>
   <#else>
      <#assign d=companyhome>
   </#if>
<#else>
   <#assign d=companyhome>
</#if>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!-- ${args.p} -->
<!-- ${path} -->
<!-- ${d.id} -->
<html>
<head>
   <title>Document Details</title>
   <link rel="stylesheet" type="text/css" href="${url.context}/css/taskpane.css" />
   <script type="text/javascript" src="${url.context}/scripts/office/doc_details.js"></script>
</head>
<body>

<div id="tabBar">
   <ul>
      <li><a href="${url.context}/scripts/office/myAlfresco?p=${path}"><img src="${url.context}/images/taskpane/my_alfresco.gif" border="0" alt="My Alfresco" /></a></li>
      <li><a href="${url.context}/scripts/office/navigation?p=${path}"><img src="${url.context}/images/taskpane/navigator.gif" border="0" alt="Browse Spaces and Documents" /></a></li>
      <li style="padding-right:6px;"><a href="${url.context}/scripts/office/search?p=${path}"><img src="${url.context}/images/taskpane/search.gif" border="0" alt="Search Alfresco" /></a></li>
      <li id="current"><a href="#"><img src="${url.context}/images/taskpane/document_details.gif" border="0" alt="View Details" /></a></li>
      <li><a href="${url.context}/scripts/office/versionHistory?p=${path}"><img src="${url.context}/images/taskpane/version_history.gif" border="0" alt="View Version History" /></a></li>
   </ul>
</div>

<div id="detailsListHeader"><span style="font-weight:bold">Details</span></div>

<div id="detailsList">
   <table>
      <tbody>
         <tr>
            <td valign="top">
<#if d.isDocument>
               <img src="${url.context}${d.icon32}" border="0" alt="${d.name}" />
            </td>
            <td style="line-height:16px;" width="100%">
               <span style="font-weight:bold;">${d.name}
   <#if d.isLocked >
                  <img src="${url.context}/images/taskpane/lock.gif" border="0" style="padding:3px 6px 2px 0px;" alt="Locked">
   </#if>
               </span><br/>
               <table>
   <#if d.properties.title?exists>
                  <tr><td>Title:</td><td>${d.properties.title}</td></tr>
   <#else>
                  <tr><td>Title:</td><td></td></tr>
   </#if>
   <#if d.properties.description?exists>
		            <tr><td>Description:</td><td>${d.properties.description}</td></tr>
   <#else>
                  <tr><td valign="top">Description:</td><td></td></tr>
   </#if>
                  <tr><td>Creator:</td><td>${d.properties.creator}</td></tr>
                  <tr><td>Created:</td><td>${d.properties.created?datetime}</td></tr>
                  <tr><td>Modifier:</td><td>${d.properties.modifier}</td></tr>
                  <tr><td>Modified:</td><td>${d.properties.modified?datetime}</td></tr>
                  <tr><td>Size:</td><td>${d.size / 1024} Kb</td></tr>
                  <tr><td valign="top">Categories:</td>
                     <td>
   <#if d.hasAspect("cm:generalclassifiable")>
      <#list d.properties.categories as category>
                       ${companyhome.nodeByReference[category].name};
      </#list>
   <#else>
                       None.
   </#if>
                     </td>
                  </tr>
               </table>
<#else>
                       The current document is not managed by Alfresco.
</#if>
            </td>
         </tr>

      </tbody>
   </table>
</div>

<div id="documentActions">
<span style="font-weight:bold;">Document Actions</span><br/>
<#if d.isDocument>
<ul>
   <#if d.isLocked >
   <#elseif hasAspect(d, "cm:workingcopy") == 1>
    <li><a href="#" onClick="javascript:runAction('${doc_actions}','checkin','${d.id}', '');"><img src="${url.context}/images/taskpane/checkin.gif" border="0" style="padding-right:6px;" alt="Check In">Check In</a></li>
   <#else>
    <li><a href="#" onClick="javascript:runAction('${doc_actions}','checkout','${d.id}', '');"><img src="${url.context}/images/taskpane/checkout.gif" border="0" style="padding-right:6px;" alt="Check Out">Check Out</a></li>
   </#if>
    <li><a href="#" onClick="javascript:runAction('${doc_actions}','makepdf','${d.id}', '');"><img src="${url.context}/images/taskpane/makepdf.gif" border="0" style="padding-right:6px;" alt="Transform to PDF">Transform to PDF</a></li>
    <li><a href="${url.context}/navigate/showDocDetails/workspace/SpacesStore/${d.id}?ticket=${session.ticket}" target="_blank"><img src="${url.context}/images/taskpane/document_details.gif" border="0" style="padding-right:6px;" alt="Open Full Details">Open Full Details</a></li>
</ul>

<#else>
                       No actions available.
</#if>
</div>

<div id="bottomMargin"><span id="statusArea">&nbsp;</span>
</div>


</body>
</html>

