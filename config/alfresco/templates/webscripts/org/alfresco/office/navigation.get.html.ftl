<#assign doc_actions="${url.serviceContext}/office/docActions">
<#assign path=args.p!"">
<#assign extn=args.e!"doc"><#assign extnx=extn+"x">
<#if args.e??><#assign extList=[]><#else><#assign extList=[".odt", ".sxw", ".doc", ".rtf", ".ods", ".sxc", ".xls", ".odp", ".sxi", ".ppt", ".odg", ".sxd", ".odb", ".odf", ".sxm"]></#if>
<#assign nav=args.n!"">
<#assign chLen=companyhome.name?length>
<#if node.isDocument>
   <#assign thisSpace = node.parent>
<#else>
   <#assign thisSpace = node>
</#if>
<#assign defaultQuery="?p=" + path?url + "&e=" + extn + "&n=" + nav>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
   <meta http-equiv="content-type" content="text/html; charset=UTF-8" />
   <title>${message("office.title.navigation")}</title>
   <link rel="stylesheet" type="text/css" href="${url.context}/css/office.css" />
<!--[if IE 6]>
   <link rel="stylesheet" type="text/css" href="${url.context}/css/office_ie6.css" />
<![endif]-->
   <script type="text/javascript" src="${url.context}/scripts/ajax/mootools.v1.11.js"></script>
   <script type="text/javascript" src="${url.context}/scripts/office/office_addin.js"></script>
   <script type="text/javascript" src="${url.context}/scripts/office/navigation.js"></script>
   <script type="text/javascript" src="${url.context}/scripts/office/external_component.js"></script>
   <script type="text/javascript">//<![CDATA[
      OfficeAddin.defaultQuery = "${defaultQuery}";
      ExternalComponent.init(
      {
         folderPath: "${url.serviceContext}/office/",
         ticket: "${session.ticket}"
      }<#if args.env??>, "${args.env}")</#if>);
   //]]></script>
</head>
<body>
<div id="overlayPanel"></div>
<div class="tabBar">
   <ul>
      <li><a title="${message("office.title.my_alfresco")}" href="${url.serviceContext}/office/myAlfresco${defaultQuery?html}"><span><img src="${url.context}/images/office/my_alfresco.gif" alt="${message("office.title.my_alfresco")}" /></span></a></li>
      <li id="current"><a title="${message("office.title.navigation")}" href="${url.serviceContext}/office/navigation${defaultQuery?html}"><span><img src="${url.context}/images/office/navigator.gif" alt="${message("office.title.navigation")}" /></span></a></li>
      <li><a title="${message("office.title.search")}" href="${url.serviceContext}/office/search${defaultQuery?html}"><span><img src="${url.context}/images/office/search.gif" alt="${message("office.title.search")}" /></span></a></li>
      <li><a title="${message("office.title.document_details")}" href="${url.serviceContext}/office/documentDetails${defaultQuery?html}"><span><img src="${url.context}/images/office/document_details.gif" alt="${message("office.title.document_details")}" /></span></a></li>
      <li><a title="${message("office.title.my_tasks")}" href="${url.serviceContext}/office/myTasks${defaultQuery?html}"><span><img src="${url.context}/images/office/my_tasks.gif" alt="${message("office.title.my_tasks")}" /></span></a></li>
      <li><a title="${message("office.title.document_tags")}" href="${url.serviceContext}/office/tags${defaultQuery?html}"><span><img src="${url.context}/images/office/tag.gif" alt="${message("office.title.document_tags")}" /></span></a></li>
   </ul>
   <span class="help">
      <a title="${message("office.help.title")}" href="${message("office.help.url")}" target="alfrescoHelp"><img src="${url.context}/images/office/help.gif" alt="${message("office.help.title")}" /></a>
   </span>
</div>

<div class="headerRow">
   <div class="headerWrapper"><div class="header">${message("office.header.current_space")}</div></div>
</div>

<div id="currentSpaceInfo">
   <span style="float: left;">
      <span style="float: left;">
         <img src="${url.context}${thisSpace.icon16}" alt="${thisSpace.name?html}" />
      </span>
      <span style="float: left; padding-left: 6px;">
         <p class="ellipsis bold" style="width: 200px" title="${thisSpace.name?html}">${thisSpace.name?html}</p><br />
<#if thisSpace.properties.description??>
         ${thisSpace.properties.description?html}
</#if>
      </span>
   </span>
<#if thisSpace=companyhome>
<#else>
   <span style="float: right;">
      <a title="${message("office.action.userhome")}" href="${url.serviceContext}/office/navigation?p=${path?url}&amp;e=${extn}&amp;n=${userhome.id}">
         <img src="${url.context}/images/office/userhome.gif" alt="${message("office.action.userhome")}" />
      </a>
      <a title="${message("office.action.parent_space")}" href="${url.serviceContext}/office/navigation?p=${path?url}&amp;e=${extn}&amp;n=${thisSpace.parent.id}">
         <img src="${url.context}/images/office/go_up.gif" alt="${message("office.action.parent_space")}" />
      </a>
   </span>
</#if>
</div>

<div class="headerRow">
   <#assign title = message("office.header.spaces_in", thisSpace.name?html)>
   <div class="headerWrapper"><div class="header" title="${title}">${title}</div></div>
   <div class="headerExtra"><div class="toggle">&nbsp;</div></div>
</div>

<div id="spaceList" class="containerMedium togglePanel">
   <div id="createSpaceContainer">
      <div id="createSpace" onclick="OfficeNavigation.showCreateSpace();">
         <img src="${url.context}/images/office/create_space.gif" alt="${message("office.action.create_space")}" />
         <span style="vertical-align: top;"><#if args.cc??>${message("office.action.create_collaboration_space")}<#else>${message("office.action.create_space")}</#if>...</span>
      </div>
      <div id="createSpacePanel">
         <div id="createSpaceParameters">
            <div class="spaceParam">${message("office.property.name")}:</div>
            <div class="spaceValue">
               <input id="spaceName" type="text" value="" />
            </div>
            <div class="spaceParam">${message("office.property.title")}:</div>
            <div class="spaceValue">
               <input id="spaceTitle" type="text" value="" />
            </div>
            <div class="spaceParam">${message("office.property.description")}:</div>
            <div class="spaceValue">
               <input id="spaceDescription" type="text" value="" />
            </div>
<#assign xpath="app:dictionary/app:space_templates/*">
<#assign templates = companyhome.childrenByXPath[xpath]>
<#if (templates?size > 0)>
            <div class="spaceParam">${message("office.property.template")}:</div>
            <div class="spaceValue">
               <select id="spaceTemplate" style="width: 172px;">
                  <option selected="selected" value="">(${message("office.message.none")})</option>
   <#list templates as template>
                  <option value="${template.id}">${template.name?html}</option>
   </#list>
               </select>
            </div>
</#if>
            <div class="spaceParam">&nbsp;</div>
            <div class="spaceValue">
               <a class="spaceAction" href="#" onclick="OfficeNavigation.submitCreateSpace('${url.serviceContext}/office/docActions', '${thisSpace.id}');">
                  ${message("office.button.submit")}
               </a>
               <a class="spaceAction" href="#" onclick="OfficeNavigation.hideCreateSpace();">
                  ${message("office.button.cancel")}
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
            <a href="${url.serviceContext}/office/navigation?p=${path?url}&amp;e=${extn}&amp;n=${child.id}"><img src="${url.context}${child.icon32}" alt="${message("office.action.open", child.name?html)}" /></a>
         </span>
         <span>
            <a href="${url.serviceContext}/office/navigation?p=${path?url}&amp;e=${extn}&amp;n=${child.id}" title="${message("office.action.open", child.name?html)}">
               <span class="bold">${child.name?html}</span>
            </a>
      <#if child.properties.description??>
      		<br />${child.properties.description?html}
      </#if>
         </span>
      </div>
   </#if>
</#list>
<#if spacesFound = 0>
      <div class="noItems">(${message("office.message.no_subspaces")})</div>
</#if>
</div>

<div class="headerRow">
   <div class="headerWrapper"><div class="header">${message("office.header.documents_in", thisSpace.name?html)}</div></div>
   <div class="headerExtra"><div class="toggle">&nbsp;</div></div>
</div>

<div id="documentList" class="containerMedium togglePanel">
<#assign documentsFound = 0>
<#list thisSpace.children?sort_by('name') as child>
   <#if child.isDocument>
      <#assign isVersionable = child.hasAspect("cm:versionable")>
      <#assign documentsFound = documentsFound + 1>
      <#assign relativePath = child.displayPath?substring(chLen + 1) + '/' + child.name />
      <#assign isSupportedExtn = false>
      <#list extList as ext>
         <#if child.name?ends_with(ext)>
            <#assign isSupportedExtn = true>
            <#break>
         </#if>
      </#list>
      <div class="documentItem ${(documentsFound % 2 = 0)?string("even", "odd")}">
         <span class="documentItemIcon">
      <#if child.name?ends_with(extn) || child.name?ends_with(extnx) || isSupportedExtn>
            <a href="#" onclick="ExternalComponent.openDocument('${relativePath?js_string}')"><img src="${url.context}${child.icon32}" alt="Open ${child.name?html}" /></a>
      <#else>
            <a href="${url.context}${child.url}" rel="_blank"><img src="${url.context}${child.icon32}" alt="Open ${child.name?html}" /></a>
      </#if>
         </span>
         <span class="documentItemDetails">
      <#if child.name?ends_with(extn) || child.name?ends_with(extnx) || isSupportedExtn>
            <a href="#" onclick="ExternalComponent.openDocument('${relativePath}')" title="${child.name?html}"><span class="bold ${isVersionable?string("versionable", "notVersionable")}">${child.name?html}</span></a>
      <#else>
            <a href="${url.context}${child.url}" rel="_blank" title="${child.name?html}"><span class="bold">${child.name?html}</span></a>
      </#if>
            <br />
      <#if child.properties.description??>
         <#if (child.properties.description?length > 0)>
            ${child.properties.description?html}<br />
         </#if>
      </#if>
            ${message("office.property.modified")}: ${child.properties.modified?datetime}, ${message("office.property.size")}: ${(child.size / 1024)?int}${message("office.unit.kb")}<br />
      <#if child.isLocked >
            <img src="${url.context}/images/office/lock.gif" style="padding:3px 6px 2px 0px;" alt="Locked" />
      <#elseif child.hasAspect("cm:workingcopy")>
            <a href="#" onclick="OfficeAddin.getAction('${doc_actions}','checkin','${child.id}', '');"><img src="${url.context}/images/office/checkin.gif" style="padding:3px 6px 2px 0px;" alt="${message("office.action.checkin")}" title="${message("office.action.checkin")}" /></a>
      <#else>
            <a href="#" onclick="OfficeAddin.getAction('${doc_actions}','checkout','${child.id}', '');"><img src="${url.context}/images/office/checkout.gif" style="padding:3px 6px 2px 0px;" alt="${message("office.action.checkout")}" title="${message("office.action.checkout")}" /></a>
      </#if>
            <a href="${url.serviceContext}/office/myTasks${defaultQuery?html}&amp;w=new&amp;wd=${child.id}"><img src="${url.context}/images/office/new_workflow.gif" style="padding:3px 6px 2px 0px;" alt="${message("office.action.start_workflow")}..." title="${message("office.action.start_workflow")}..." /></a>
            <a href="#" onclick="ExternalComponent.insertDocument('${relativePath?js_string}', '${child.nodeRef}')"><img src="${url.context}/images/office/insert_document.gif" style="padding:3px 6px 2px 0px;" alt="${message("office.action.insert")}" title="${message("office.action.insert")}" /></a>
      <#if !child.name?ends_with(".pdf")>
            <a href="#" onclick="OfficeAddin.getAction('${doc_actions}','makepdf','${child.id}', '');"><img src="${url.context}/images/office/makepdf.gif" style="padding:3px 6px 2px 0px;" alt="${message("office.action.transform_pdf")}" title="${message("office.action.transform_pdf")}" /></a>
      </#if>
      <#if !child.isLocked>
            <a href="#" onclick="OfficeAddin.getAction('${doc_actions}','delete','${child.id}', '${message("office.message.confirm_delete")}');"><img src="${url.context}/images/office/delete.gif" style="padding:3px 6px 2px 0px;" alt="${message("office.action.delete")}..." title="${message("office.action.delete")}..." /></a>
      </#if>
         </span>
      </div>
   </#if>
</#list>
<#if documentsFound = 0>
      <div class="noItems">(${message("office.message.no_documents")})</div>
</#if>
</div>

<div class="headerRow">
   <div class="headerWrapper"><div class="header">${message("office.header.actions")}</div></div>
</div>

<#assign currentPath = thisSpace.displayPath  + '/' + thisSpace.name />
<#assign currentPath = currentPath?substring(chLen + 1) />
<div id="navigationActions" class="actionsPanel">
   <div id="saveDetailsPanel">
      ${message("office.property.filename")}:<br />
      <input class="saveDetailsItem" type="text" id="saveFilename" style="height: 18px; width: 168px;" />
      <a id="saveFilenameOK" class="spaceAction" href="#" onclick="OfficeNavigation.saveOK(this);">${message("office.button.ok")}</a>
      <a class="spaceAction" href="#" onclick="OfficeNavigation.saveCancel();">${message("office.button.cancel")}</a>
   </div>
   <div id="nonStatusText">
      <ul>
         <li>
            <a href="#" onclick="OfficeNavigation.saveToAlfresco('${currentPath?js_string}')">
               <img src="${url.context}/images/office/save_to_alfresco.gif" alt="${message("office.action.save_to_alfresco")}" />
               ${message("office.action.save_to_alfresco")}
            </a>
            <br />${message("office.action.save_to_alfresco.description")}
         </li>
<#if args.search??>
         <li>
            <a href="${url.serviceContext}/office/search${defaultQuery?html}&amp;searchagain=${args.search?url}&amp;maxresults=${args.maxresults}">
               <img src="${url.context}/images/office/search_again.gif" alt="${message("office.action.return_search")}" />
               ${message("office.action.return_search")}
            </a>
            <br />${message("office.action.return_search.description")}
         </li>
</#if>
      </ul>
   </div>
   
   <div id="statusText"></div>
</div>

<div style="position: absolute; top: 0px; left: 0px; z-index: 100; display: none">
   <iframe id="if_externalComponenetMethodCall" name="if_externalComponenetMethodCall" src="" style="visibility: hidden;" width="0" height="0"></iframe>
</div>

</body>
</html>