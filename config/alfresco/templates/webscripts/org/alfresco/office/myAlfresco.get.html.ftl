<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
	<title>My Alfresco</title>
	<link rel="stylesheet" type="text/css" href="/alfresco/css/taskpane.css" />
	<script type="text/javascript" src="/alfresco/scripts/office/my_alfresco.js"></script>
</head>
<body>

<div id="tabBar">
   <ul>
      <li id="current"><a href="#"><img src="/alfresco/images/office/my_alfresco.gif" border="0" alt="My Alfresco" /></a></li>
      <li><a href="/alfresco/template/workspace/SpacesStore/${thisContext.id}/workspace/SpacesStore/${office_browse}"><img src="/alfresco/images/office/navigator.gif" border="0" alt="Browse Spaces and Documents" /></a></li>
      <li style="padding-right:6px;"><a href="/alfresco/template/workspace/SpacesStore/${thisContext.id}/workspace/SpacesStore/${office_search}"><img src="/alfresco/images/office/search.gif" border="0" alt="Search Alfresco" /></a></li>
      <li><a href="/alfresco/template/workspace/SpacesStore/${thisContext.id}/workspace/SpacesStore/${office_details}"><img src="/alfresco/images/office/document_details.gif" border="0" alt="View Details" /></a></li>
      <li><a href="/alfresco/template/workspace/SpacesStore/${thisContext.id}/workspace/SpacesStore/${office_history}"><img src="/alfresco/images/office/version_history.gif" border="0" alt="View Version History" /></a></li>
   </ul>
</div>

<div id="mycheckedoutdocsListHeader"><span style="font-weight:bold">My checked out documents</span></div>

<div id="mycheckedoutdocsList">
          <table>
                 <tbody>
<#assign query="@cm\\:workingCopyOwner:${person.properties.userName}">
   <#list companyhome.childrenByLuceneSearch[query] as child>
      <#if child.isDocument>
                   <!-- lb: start repeat -->
                   <tr>
                       <td>
                       <a href="#"><img src="/alfresco/images/office/document.gif" border="0" alt="Open ${child.name}" /></a>
                       </td>
                       <td style="line-height:16px;" width="100%">
                       <a href="#" title="Open ${child.name}">${child.name}</a><br/>
<#if child.properties.description?exists>
		${child.properties.description}<br/>
</#if>
                Modified: ${child.properties.modified?datetime}, Size: ${child.size / 1024} Kb<br/>
                <a href="#" onClick="javascript:runAction('checkin','${child.id}', '');"><img src="/alfresco/images/office/checkin.gif" border="0" style="padding:3px 6px 2px 0px;" alt="Check In" title="Check In"></a>
<a href="#" onClick="javascript:runAction('makepdf','${child.id}', '');"><img src="/alfresco/images/office/makepdf.gif" border="0" style="padding:3px 6px 2px 0px;" alt="Make PDF..." title="Make PDF"></a>
                </td>
            </tr>
            <!-- lb: end repeat -->
      </#if>
   </#list>

                 </tbody>
          </table>
</div>
<div id="mytodoListHeader"><span style="font-weight:bold;">My Communities</span>
</div>

<div id="mytodoList">
          <table>
          <tbody>
<#list companyhome.childrenByXPath["*[@cm:name='Communities']/*"] as child>
            <!-- lb: start repeat -->
            <tr>
                <td>
                <a href="#"><img src="/alfresco${child.icon32}" border="0" alt="Open ${child.name}" /></a>
                </td>
                <td width="100%">
                <a href="/alfresco/template/workspace/SpacesStore/${child.id}/workspace/SpacesStore/${office_browse}" title="Open ${child.name}">${child.name}</a><br/>
<#if child.properties.description?exists>
		${child.properties.description}
</#if>
                </td>
            </tr>
            <!-- lb: end repeat -->
</#list>
          </table>
</div>

<div id="documentActions">
<span style="font-weight:bold;">Other Actions</span><br/>
<div id="yellowbox" style="background:#ffffcc;border: 1px solid #cccccc; margin-top:6px; padding-bottom:6px;">
<ul>
    <li style="padding-bottom:4px;"><a href="/alfresco/template/workspace/SpacesStore/${thisContext.id}/workspace/SpacesStore/${office_browse}"><img src="/alfresco/images/office/save_to_alfresco.gif" border="0" style="padding-right:6px;" alt="Save to Alfresco"><b>Save to Alfresco</b></a><br> Allows you to place the current document under Alfresco management.</li>
    <li style="padding-bottom:4px;"><a href="/alfresco/template/workspace/SpacesStore/${thisContext.id}/workspace/SpacesStore/${office_browse}"><img src="/alfresco/images/office/navigator.gif" border="0" style="padding-right:6px;" alt="Browse"><b>Browse Alfresco</b></a><br> Navigate around the Alfresco repository for documents.</li>
    <li style="padding-bottom:4px;"><a href="/alfresco/template/workspace/SpacesStore/${thisContext.id}/workspace/SpacesStore/${office_search}"><img src="/alfresco/images/office/search.gif" border="0" style="padding-right:6px;" alt="Search"><b>Find Documents</b></a><br> Search Alfresco for documents by name and content.</li>
    <li style="padding-bottom:4px;"><a href="/alfresco/navigate/browse?ticket=${session.ticket}" target="_blank"><img src="/alfresco/images/logo/AlfrescoLogo16.gif" border="0" style="padding-right:6px;" alt="Save to Alfresco"><b>Launch Alfresco</b></a><br> Start the Alfresco Web Client.</li>
</ul>
</div>
</div>

<div id="bottomMargin" style="height:24px;"><span id="statusArea">&nbsp;</span>
</div>


</body>
</html>