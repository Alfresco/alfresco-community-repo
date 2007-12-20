<#assign doc_actions="${url.serviceContext}/office/docActions">
<#if args.e?exists><#assign extn=args.e><#else><#assign extn="doc"></#if><#assign extnx=extn+"x">
<#if args.n?exists><#assign nav=args.n><#else><#assign nav=""></#if>
<#assign chLen=companyhome.name?length>
<#if node.isDocument>
   <#assign thisSpace = node.parent>
<#else>
   <#assign thisSpace = node>
</#if>
<#assign defaultQuery="?p=" + path?url + "&e=" + extn + "&n=" + nav>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
	<title>Browse Spaces and Documents</title>
	<link rel="stylesheet" type="text/css" href="${url.context}/css/office.css" />
<!--[if IE 6]>
   <link rel="stylesheet" type="text/css" href="${url.context}/css/office_ie6.css" />
<![endif]-->
   <script type="text/javascript" src="${url.context}/scripts/ajax/mootools.v1.11.js"></script>
	<script type="text/javascript" src="${url.context}/scripts/office/office_addin.js"></script>
	<script type="text/javascript" src="${url.context}/scripts/office/navigation.js"></script>
   <script type="text/javascript">//<![CDATA[
      OfficeAddin.defaultQuery = '${defaultQuery}';
   //]]></script>
</head>
<body>
<div id="overlayPanel"></div>
<div class="tabBar">
   <ul>
      <li><a title="${message("office.title.my_alfresco")}" href="${url.serviceContext}/office/myAlfresco${defaultQuery?html}"><span><img src="${url.context}/images/office/my_alfresco.gif" alt="My Alfresco" /></span></a></li>
      <li id="current"><a title="${message("office.title.navigation")}" href="${url.serviceContext}/office/navigation${defaultQuery?html}"><span><img src="${url.context}/images/office/navigator.gif" alt="Browse Spaces and Documents" /></span></a></li>
      <li><a title="${message("office.title.search")}" href="${url.serviceContext}/office/search${defaultQuery?html}"><span><img src="${url.context}/images/office/search.gif" alt="Search Alfresco" /></span></a></li>
      <li><a title="${message("office.title.document_details")}" href="${url.serviceContext}/office/documentDetails${defaultQuery?html}"><span><img src="${url.context}/images/office/document_details.gif" alt="View Details" /></span></a></li>
      <li><a title="${message("office.title.my_tasks")}" href="${url.serviceContext}/office/myTasks${defaultQuery?html}"><span><img src="${url.context}/images/office/my_tasks.gif" alt="My Tasks" /></span></a></li>
      <li><a title="${message("office.title.document_tags")}" href="${url.serviceContext}/office/tags${defaultQuery?html}"><span><img src="${url.context}/images/office/tag.gif" alt="${message("office.title.document_tags")}" /></span></a></li>
   </ul>
</div>

<div class="header">Current Space</div>

<div id="currentSpaceInfo">
   <span style="float: left;">
      <span style="float: left;">
         <img src="${url.context}${thisSpace.icon16}" alt="${thisSpace.name}" />
      </span>
      <span style="float: left; padding-left: 6px;">
         <span class="bold">${thisSpace.name}</span><br />
<#if thisSpace.properties.description?exists>
         ${thisSpace.properties.description}
</#if>
      </span>
   </span>
<#if thisSpace=companyhome>
<#else>
   <span style="float: right;">
      <a title="To UserHome Space" href="${url.serviceContext}/office/navigation?p=${path?url}&amp;e=${extn}&amp;n=${userhome.id}">
         <img src="${url.context}/images/office/userhome.gif" alt="To UserHome Space" />
      </a>
      <a title="Up to Parent Space" href="${url.serviceContext}/office/navigation?p=${path?url}&amp;e=${extn}&amp;n=${thisSpace.parent.id}">
         <img src="${url.context}/images/office/go_up.gif" alt="Up to Parent Space" />
      </a>
   </span>
</#if>
</div>

<div class="header">Spaces in ${thisSpace.name}<span class="headerExtra"><span class="toggle">&nbsp;</span></span></div>

<div id="spaceList" class="containerMedium togglePanel">
   <div id="createSpaceContainer">
      <div id="createSpace" onclick="OfficeNavigation.showCreateSpace();">
         <img src="${url.context}/images/office/create_space.gif" alt="Create new space" />
         <span style="vertical-align: top;">Create New <#if args.cc?exists>Collaboration </#if>Space...</span>
      </div>
      <div id="createSpacePanel">
         <div id="createSpaceParameters">
            <div class="spaceParam">Name:</div>
            <div class="spaceValue">
               <input id="spaceName" type="text" value="" />
            </div>
            <div class="spaceParam">Title:</div>
            <div class="spaceValue">
               <input id="spaceTitle" type="text" value="" />
            </div>
            <div class="spaceParam">Description:</div>
            <div class="spaceValue">
               <input id="spaceDescription" type="text" value="" />
            </div>
<#assign xpath="app:dictionary/app:space_templates/*">
<#assign templates = companyhome.childrenByXPath[xpath]>
<#if (templates?size > 0)>
            <div class="spaceParam">Template:</div>
            <div class="spaceValue">
               <select id="spaceTemplate" style="width: 172px;">
                  <option selected="selected" value="">(None)</option>
   <#list templates as template>
                  <option value="${template.id}">${template.name}</option>
   </#list>
               </select>
            </div>
</#if>
            <div class="spaceParam">&nbsp;</div>
            <div class="spaceValue">
               <a class="spaceAction" href="#" onclick="OfficeNavigation.submitCreateSpace('${url.serviceContext}/office/docActions', '${thisSpace.id}');">
                  Submit
               </a>
               <a class="spaceAction" href="#" onclick="OfficeNavigation.hideCreateSpace();">
                  Cancel
               </a>
            </div>
         </div>
      </div>
   </div>
<#assign spacesFound = 0>
<#list thisSpace.children?sort_by('name') as child>
   <#if child.isContainer>
      <#assign spacesFound = spacesFound + 1>
      <div class="spaceItem ${(spacesFound % 2 = 0)?string("even", "odd")}">
         <span style="float: left; width: 36px;">
            <a href="${url.serviceContext}/office/navigation?p=${path?url}&amp;e=${extn}&amp;n=${child.id}"><img src="${url.context}${child.icon32}" alt="Open ${child.name}" /></a>
         </span>
         <span>
            <a href="${url.serviceContext}/office/navigation?p=${path?url}&amp;e=${extn}&amp;n=${child.id}" title="Open ${child.name}">
               <span class="bold">${child.name}</span>
            </a>
      <#if child.properties.description?exists>
      		<br />${child.properties.description}
      </#if>
         </span>
      </div>
   </#if>
</#list>
<#if spacesFound = 0>
      <div class="noItems">(No subspaces)</div>
</#if>
</div>

<div class="header">Documents in ${thisSpace.name}<span class="headerExtra"><span class="toggle">&nbsp;</span></span></div>

<div id="documentList" class="containerMedium togglePanel">
<#assign documentsFound = 0>
<#list thisSpace.children?sort_by('name') as child>
   <#if child.isDocument>
      <#assign documentsFound = documentsFound + 1>
      <#assign relativePath = (child.displayPath?substring(chLen+1) + '/' + child.name)?url?replace('%2F', '/')?replace('\'', '\\\'') />
      <div class="documentItem ${(documentsFound % 2 = 0)?string("even", "odd")}">
         <span class="documentItemIcon">
      <#if child.name?ends_with(extn) || child.name?ends_with(extnx)>
            <a href="#" onclick="window.external.openDocument('${relativePath}')"><img src="${url.context}${child.icon32}" alt="Open ${child.name}" /></a>
      <#else>
            <a href="${url.context}${child.url}?ticket=${session.ticket}" rel="_blank"><img src="${url.context}${child.icon32}" alt="Open ${child.name}" /></a>
      </#if>
         </span>
         <span class="documentItemDetails">
      <#if child.name?ends_with(extn) || child.name?ends_with(extnx)>
            <a href="#" onclick="window.external.openDocument('${relativePath}')"><span class="bold">${child.name}</span></a>
      <#else>
            <a href="${url.context}${child.url}?ticket=${session.ticket}" rel="_blank"><span class="bold">${child.name}</span></a>
      </#if>
            <br />
      <#if child.properties.description?exists>
         <#if (child.properties.description?length > 0)>
		      ${child.properties.description}<br />
		   </#if>
      </#if>
            Modified: ${child.properties.modified?datetime}, Size: ${(child.size / 1024)?int}Kb<br />
      <#if child.isLocked >
            <img src="${url.context}/images/office/lock.gif" style="padding:3px 6px 2px 0px;" alt="Locked" />
      <#elseif hasAspect(child, "cm:workingcopy") == 1>
            <a href="#" onclick="OfficeAddin.getAction('${doc_actions}','checkin','${child.id}', '');"><img src="${url.context}/images/office/checkin.gif" style="padding:3px 6px 2px 0px;" alt="Check In" title="Check In" /></a>
      <#else>
            <a href="#" onclick="OfficeAddin.getAction('${doc_actions}','checkout','${child.id}', '');"><img src="${url.context}/images/office/checkout.gif" style="padding:3px 6px 2px 0px;" alt="Check Out" title="Check Out" /></a>
      </#if>
            <a href="${url.serviceContext}/office/myTasks${defaultQuery?html}&amp;w=new&amp;wd=${child.id}"><img src="${url.context}/images/office/new_workflow.gif" style="padding:3px 6px 2px 0px;" alt="Create Workflow..." title="Create Workflow..." /></a>
            <a href="#" onclick="window.external.insertDocument('${relativePath}')"><img src="${url.context}/images/office/insert_document.gif" style="padding:3px 6px 2px 0px;" alt="Insert File into Current Document" title="Insert File into Current Document" /></a>
      <#if !child.name?ends_with(".pdf")>
            <a href="#" onclick="OfficeAddin.getAction('${doc_actions}','makepdf','${child.id}', '');"><img src="${url.context}/images/office/makepdf.gif" style="padding:3px 6px 2px 0px;" alt="Make PDF..." title="Make PDF" /></a>
      </#if>
      <#if !child.isLocked>
            <a href="#" onclick="OfficeAddin.getAction('${doc_actions}','delete','${child.id}', 'Are you sure you want to delete this document?');"><img src="${url.context}/images/office/delete.gif" style="padding:3px 6px 2px 0px;" alt="Delete..." title="Delete" /></a>
      </#if>
         </span>
      </div>
   </#if>
</#list>
<#if documentsFound = 0>
      <div class="noItems">(No documents)</div>
</#if>
</div>

<div class="header">Actions</div>

<#assign currentPath = thisSpace.displayPath  + '/' + thisSpace.name />
<#assign currentPath = currentPath?substring(chLen+1)?url?replace('%2F', '/')?replace('\'', '\\\'') />
<div id="navigationActions" class="actionsPanel">
   <div id="saveDetailsPanel">
      Document filename:<br />
      <input class="saveDetailsItem" type="text" id="saveFilename" style="height: 18px; width: 168px;" />
      <a class="spaceAction" href="#" onclick="OfficeNavigation.saveOK(this);">OK</a>
      <a class="spaceAction" href="#" onclick="OfficeNavigation.saveCancel();">Cancel</a>
   </div>
   <div id="nonStatusText">
      <ul>
<#if (path?length != 0)>
         <li>
            <a href="#" onclick="OfficeNavigation.saveToAlfresco('${currentPath}')">
               <img src="${url.context}/images/office/save_to_alfresco.gif" alt="Save to Alfresco" />
               Save to Alfresco
            </a>
            <br />Save the document to the current Space.
         </li>
</#if>
<#if args.search?exists>
         <li>
            <a href="${url.serviceContext}/office/search${defaultQuery?html}&amp;searchagain=${args.search?url}&amp;maxresults=${args.maxresults}">
               <img src="${url.context}/images/office/search_again.gif" alt="Back to results" />
               Back to search results
            </a>
            <br />Return to the search tab.
         </li>
</#if>
      </ul>
   </div>
   
   <div id="statusText"></div>
</div>

</body>
</html>