<!DOCTYPE html
PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<#list template.parent.children as child>
   <#if child.name = "my_alfresco.ftl"><#assign office_home = child.id>
   <#elseif child.name = "navigation.ftl"><#assign office_browse = child.id>
   <#elseif child.name = "search.ftl"><#assign office_search = child.id>
   <#elseif child.name = "document_details.ftl"><#assign office_details = child.id>
   <#elseif child.name = "version_history.ftl"><#assign office_history = child.id>
   <#elseif child.name = "doc_actions.js"><#assign doc_actions = child.id>
   <#elseif child.name = "navigation.js"><#assign nav_script = child>
   </#if>
</#list>
<#if document.isDocument>
   <#assign thisSpace = document.parent>
<#else>
   <#assign thisSpace = document>
</#if>
<html>
<head>
<title>Basic Navigation</title>
 
<link rel="stylesheet" type="text/css"
href="/alfresco/css/taskpane.css" />

<script type="text/javascript" src="/alfresco${nav_script.url}" >
</script>

</head>
<body>

<div id="tabBar">
    <ul>
      <li><a href="/alfresco/template/workspace/SpacesStore/${document.id}/workspace/SpacesStore/${office_home}"><img src="/alfresco/images/taskpane/my_alfresco.gif" border="0" alt="My Alfresco" /></a></li>
      <li id="current"><a href="#"><img src="/alfresco/images/taskpane/navigator.gif" border="0" alt="Browse Spaces and Documents" /></a></li>
      <li style="padding-right:6px;"><a href="/alfresco/template/workspace/SpacesStore/${document.id}/workspace/SpacesStore/${office_search}"><img src="/alfresco/images/taskpane/search.gif" border="0" alt="Search Alfresco" /></a></li>
      <li><a href="/alfresco/template/workspace/SpacesStore/${document.id}/workspace/SpacesStore/${office_details}"><img src="/alfresco/images/taskpane/document_details.gif" border="0" alt="View Details" /></a></li>
      <li><a href="/alfresco/template/workspace/SpacesStore/${document.id}/workspace/SpacesStore/${office_history}"><img src="/alfresco/images/taskpane/version_history.gif" border="0" alt="View Version History" /></a></li>
    </ul>
  </div>


<div id="currentSpaceInfo">
<table>
            <tr>
                <td rowspan=2 class="valign">
                In:<img src="/alfresco${thisSpace.icon32}" border="0"/>
                </td>
                <td>
                <span style="font-weight:bold;">${thisSpace.name}</span>
                </td>
                </tr>
                <tr>
                <td>
<#if thisSpace.properties.description?exists>
		${thisSpace.properties.description}
</#if>
                </td>
                <td>
                &nbsp;&nbsp;&nbsp;&nbsp;
<#if thisSpace = companyhome>
<#else>
<img src="/alfresco/images/taskpane/go_up.gif" border="0" width="16" height="16"  alt="go up to parent space"/><a href="/alfresco/template/workspace/SpacesStore/${thisSpace.parent.id}/workspace/SpacesStore/${template.id}"><span title="Go up to parent space">Up</span></a>
</#if>
                </td>
            </tr>
          </table>
</div>

<div id="spaceListHeader"><span style="font-weight:bold">Spaces in ${thisSpace.name}</span></div>

<div id="spaceList">
          <table>
          <tbody>
<#list thisSpace.children as child>
   <#if child.isContainer>
            <!-- lb: start repeat -->
            <tr>
                <td>
                <a href="#"><img src="/alfresco${child.icon32}" border="0" alt="Open ${child.name}" /></a>
                </td>
                <td width="100%">
                <a href="/alfresco/template/workspace/SpacesStore/${child.id}/workspace/SpacesStore/${template.id}" title="Open ${child.name}">${child.name}</a><br/>
<#if child.properties.description?exists>
		${child.properties.description}
</#if>
                </td>
            </tr>
   </#if>
</#list>
            <!-- lb: end repeat -->
            </tbody>
          </table>
</div>

<div id="contentListHeader"><span style="font-weight:bold;">Documents in ${thisSpace.name}</span>
</div>

<div id="contentList">
          <table>
          <tbody>
<#list thisSpace.children as child>
   <#if child.isDocument>
            <!-- lb: start repeat -->
<#assign webdavPath = (child.displayPath?substring(13) + '/' + child.name)?url('ISO-8859-1')?replace('%2F', '/')?replace('\'', '\\\'') />
            <tr>
                <td>
<#if child.name?ends_with(".doc")>
                <a href="#" onClick="window.external.openDocument('${webdavPath}')"><img src="/alfresco${child.icon32}" border="0" alt="Open ${child.name}" /></a>
<#else>
                <a href="/alfresco${child.url}?ticket=${session.ticket}" target="_blank"><img src="/alfresco${child.icon32}" border="0" alt="Open ${child.name}" /></a>
</#if>
                </td>
                <td style="line-height:16px;" width="100%">
<#if child.name?ends_with(".doc")>
                <a href="#" onClick="window.external.openDocument('${webdavPath}')" title="Open ${child.name}">${child.name}</a><br/>
<#else>
                <a href="/alfresco${child.url}?ticket=${session.ticket}" target="_blank" title="Open ${child.name}">${child.name}</a><br/>
</#if>
<#if child.properties.description?exists>
		${child.properties.description}<br/>
</#if>
                Modified: ${child.properties.modified?datetime}, Size: ${child.size / 1024} Kb<br/>
<#if child.isLocked >
                <img src="/alfresco/images/taskpane/lock.gif" border="0" style="padding:3px 6px 2px 0px;" alt="Locked">
<#elseif hasAspect(child, "cm:workingcopy") == 1>
                <a href="#" onClick="javascript:runAction('${doc_actions}','checkin','${child.id}', '');"><img src="/alfresco/images/taskpane/checkin.gif" border="0" style="padding:3px 6px 2px 0px;" alt="Check In" title="Check In"></a>
<#else>
                <a href="#" onClick="javascript:runAction('${doc_actions}','checkout','${child.id}', '');"><img src="/alfresco/images/taskpane/checkout.gif" border="0" style="padding:3px 6px 2px 0px;" alt="Check Out" title="Check Out"></a>
</#if>
<a href="#" onClick="javascript:runAction('${doc_actions}','makepdf','${child.id}', '');"><img src="/alfresco/images/taskpane/makepdf.gif" border="0" style="padding:3px 6px 2px 0px;" alt="Make PDF..." title="Make PDF"></a>
<#if !child.isLocked >
<a href="#" onClick="javascript:runAction('${doc_actions}','delete','${child.id}', 'Are you sure you want to delete this document?');"><img src="/alfresco/images/taskpane/delete.gif" border="0" style="padding:3px 6px 2px 0px;" alt="Delete..." title="Delete"></a>
</#if>
                </td>
            </tr>
            <!-- lb: end repeat -->
   </#if>
</#list>
           </tbody>
          </table>
</div>

<div id="documentActions">
<span style="font-weight:bold;">Document Actions</span><br/>
<ul>
<#assign currentPath = thisSpace.displayPath  + '/' + thisSpace.name />
<#assign webdavPath = currentPath?substring(13)?url('ISO-8859-1')?replace('%2F', '/')?replace('\'', '\\\'') />
    <li><a href="#" onClick="window.external.saveToAlfresco('${webdavPath}')"><img src="/alfresco/images/taskpane/save_to_alfresco.gif" border="0" style="padding-right:6px;" alt="Save to Alfresco">Save to Alfresco</a></li>
    <#if args.search?exists>
    <li><a href="/alfresco/template/workspace/SpacesStore/${document.id}/workspace/SpacesStore/${office_search}?searchagain=${args.search}&maxresults=${args.maxresults}"><img src="/alfresco/images/taskpane/placeholder.gif" border="0" style="padding-right:6px;" alt="Back to results">Back to search results</a></li>
    </#if>
</ul>
</div>

<div id="bottomMargin" style="height:24px; padding-left:6px;"><span id="statusArea">&nbsp;</span>
</div>


</body>
</html>