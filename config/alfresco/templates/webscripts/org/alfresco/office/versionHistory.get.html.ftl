<#list template.parent.children as child>
   <#if child.name = "my_alfresco.ftl"><#assign office_home = child.id>
   <#elseif child.name = "navigation.ftl"><#assign office_browse = child.id>
   <#elseif child.name = "search.ftl"><#assign office_search = child.id>
   <#elseif child.name = "document_details.ftl"><#assign office_details = child.id>
   <#elseif child.name = "version_history.ftl"><#assign office_history = child.id>
   <#elseif child.name = "doc_actions.js"><#assign doc_actions = child.id>
   <#elseif child.name = "version.js"><#assign ver_script = child>
   </#if>
</#list>
<!DOCTYPE html
PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
<title>Version History</title>

<link rel="stylesheet" type="text/css"
href="/alfresco/css/taskpane.css" />


<script type="text/javascript" src="/alfresco${ver_script.url}">
</script>


</head>
<body>

<div id="tabBar">
    <ul>
      <li><a href="/alfresco/template/workspace/SpacesStore/${document.id}/workspace/SpacesStore/${office_home}"><img src="/alfresco/images/taskpane/my_alfresco.gif" border="0" alt="My Alfresco" /></a></li>
      <li><a href="/alfresco/template/workspace/SpacesStore/${document.id}/workspace/SpacesStore/${office_browse}"><img src="/alfresco/images/taskpane/navigator.gif" border="0" alt="Browse Spaces and Documents" /></a></li>
      <li style="padding-right:6px;"><a href="/alfresco/template/workspace/SpacesStore/${document.id}/workspace/SpacesStore/${office_search}"><img src="/alfresco/images/taskpane/search.gif" border="0" alt="Search Alfresco" /></a></li>
      <li><a href="/alfresco/template/workspace/SpacesStore/${document.id}/workspace/SpacesStore/${office_details}"><img src="/alfresco/images/taskpane/document_details.gif" border="0" alt="View Details" /></a></li>
      <li id="current"><a href="#"><img src="/alfresco/images/taskpane/version_history.gif" border="0" alt="View Version History" /></a></li>
    </ul>
  </div>

<div id="versionListHeader"><span style="font-weight:bold">Version History for ${document.name}</span></div>

<div id="versionList">
          <table>
                 <tbody>
<#if document.isDocument >
   <#if hasAspect(document, "cm:versionable") == 1 >
                 <!-- lb: start repeat row -->
      <#list document.versionHistory?sort_by("versionLabel")?reverse as record>
                   <tr>
                       <td valign="top">
                       <a href="/alfresco${document.url}"><img src="/alfresco/images/taskpane/document.gif" border="0" alt="Open ${record.versionLabel}"/></a>
                       </td>
                       <td style="line-height:16px;" width="100%">
                       <a href="#" title="Open ${record.versionLabel}"><span style="font-weight:bold;">${record.versionLabel}</span></a><br/>
                       Author: ${record.creator}<br/>
                       Date: ${record.createdDate?datetime}<br/>
<#if record.description?exists>
                       Notes: ${record.description}<br/>
</#if>
<!--                       <a href="#" onClick="window.external.compareDocument('/alfresco${document.url}')" title="Compare with current">Compare with current</a><br/> -->
                       </td>
                   </tr>
      </#list>
   <#else>
                   <tr>
                       <td valign="top">
The current document is not versioned.<br>
<a href="#" onClick="javascript:runAction('${doc_actions}','makeversion','${document.id}', '');">Make Versionable</a>
                       </td>
                   </tr>
   </#if>
                   <!-- lb: end repeat row -->
<#else>
                   <tr>
                       <td valign="top">
The current document is not managed by Alfresco.
                       </td>
                   </tr>
</#if>
                 </tbody>
          </table>
</div>

<div id="bottomMargin" style="height:24px; padding-left:6px;"><span id="statusArea">&nbsp;</span>
</div>


</body>
</html>