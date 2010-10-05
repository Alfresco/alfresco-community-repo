<#import "office.inc.ftl" as office />
<#--
   Template specific
-->
<#assign doc_actions="${url.serviceContext}/office/docActions">
<#assign path=args.p!"">
<#assign extn=args.e!"doc"><#assign extnx=extn+"x">
<#if args.e??><#assign extList=[]><#else><#assign extList=[".odt", ".sxw", ".doc", ".rtf", ".ods", ".sxc", ".xls", ".odp", ".sxi", ".ppt", ".odg", ".sxd", ".odb", ".odf", ".sxm"]></#if>
<#assign nav=args.n!"">
<#assign chLen=companyhome.name?length>
<#-- resolve the path (from Company Home) into a node -->
<#if companyhome.childByNamePath[path]??>
   <#assign d=companyhome.childByNamePath[path]>
<#else>
   <#assign d=companyhome>
</#if>
<#assign defaultQuery="?p=" + path?url + "&e=" + extn + "&n=" + nav>
<#--
   /Template specific
-->

<@office.header "my_alfresco" defaultQuery>
   <script type="text/javascript" src="${url.context}/scripts/office/my_alfresco.js"></script>
</@>

<div class="headerRow">
   <div class="headerWrapper"><div class="header">${message("office.header.my_checked_out_documents")}</div></div>
   <div class="headerExtra"><div class="toggle">&nbsp;</div></div>
</div>

<div id="checkedoutList" class="containerMedium togglePanel">
<#assign rowNum=0>
<#assign query="@cm\\:workingCopyOwner:${person.properties.userName}">
   <#list companyhome.childrenByLuceneSearch[query] as child>
      <#if child.isDocument>
         <#assign rowNum=rowNum+1>
         <#assign relativePath = child.displayPath?substring(chLen + 1) + '/' + child.name />
         <#assign isSupportedExtn = false>
         <#list extList as ext>
            <#if child.name?ends_with(ext)>
               <#assign isSupportedExtn = true>
               <#break>
            </#if>
         </#list>
   <div class="documentItem ${(rowNum % 2 = 0)?string("odd", "even")}">
         <span class="documentItemIcon">
            <img src="${url.context}${child.icon32}" alt="${child.name?html}" />
         </span>
         <span class="documentItemDetails">
         <#if child.name?ends_with(extn) || child.name?ends_with(extnx) || isSupportedExtn>
            <a href="#" onclick="ExternalComponent.openDocument('${relativePath?js_string}')" title="${message("office.action.open", child.name?html)}" style="font-weight: bold;">${child.name?html}</a><br />
         <#else>
            <a href="${url.context}${child.url}" target="_blank" title="${message("office.action.open", child.name?html)}" style="font-weight: bold;">${child.name?html}</a><br />
         </#if>
         <#if child.properties.description??>
            <#if (child.properties.description?length > 0)>
               ${child.properties.description?html}<br />
            </#if>
         </#if>
            ${message("office.property.modified")}: ${child.properties.modified?datetime} (${(child.size / 1024)?int}${message("office.unit.kb")})<br />
            <a href="#" onclick="OfficeAddin.getAction('${doc_actions}','checkin','${child.id}', '');"><img src="${url.context}/images/office/checkin.gif" style="padding:3px 6px 2px 0px;" alt="${message("office.action.checkin")}" title="${message("office.action.checkin")}" /></a>
            <a href="${url.serviceContext}/office/myTasks${defaultQuery?html}&amp;w=new&amp;wd=${child.id}"><img src="${url.context}/images/office/new_workflow.gif" style="padding:3px 6px 2px 0px;" alt="${message("office.action.start_workflow")}..." title="${message("office.action.start_workflow")}..." /></a>
            <a href="#" onclick="ExternalComponent.insertDocument('${relativePath?js_string}','${child.nodeRef}')"><img src="${url.context}/images/office/insert_document.gif" style="padding:3px 6px 2px 0px;" alt="${message("office.action.insert")}" title="${message("office.action.insert")}" /></a>
         <#if !child.name?ends_with(".pdf")>
            <a href="#" onclick="OfficeAddin.getAction('${doc_actions}','makepdf','${child.id}', '');"><img src="${url.context}/images/office/makepdf.gif" style="padding:3px 6px 2px 0px;" alt="${message("office.action.transform_pdf")}" title="${message("office.action.transform_pdf")}" /></a>
         </#if>
         </span>
      </div>
      </#if>
   </#list>
   <#if rowNum = 0>
      <div>
         <div class="noItems">(${message("office.message.no_documents")})</div>
      </div>
   </#if>
</div>

<div class="headerRow">
   <div class="headerWrapper"><div class="header">${message("office.header.my_tasks")}<span class="taskKey"><img src="${url.context}/images/office/task_overdue.gif" alt="${message("office.task.overdue")}" />=${message("office.task.overdue")}, <img src="${url.context}/images/office/task_today.gif" alt="${message("office.task.due_today")}" />=${message("office.task.due_today")}</span></div></div>
   <div class="headerExtra"><span class="toggle">&nbsp;</span></div>
</div>

<div id="taskList" class="containerMedium togglePanel">
<#assign taskNum=0>
<#list workflow.assignedTasks as t>
   <#assign taskNum=taskNum+1>
   <#assign hasDue=t.properties["bpm:dueDate"]??>
   <#if hasDue>
      <#assign due=t.properties["bpm:dueDate"]>
   </#if>
   <div id="${t.id?replace("$", ".")}" class="taskItem" rel="<#if hasDue>${due?date?string("yyyyMMddHHmmss")}<#else>99999999999999</#if>">
      <span class="taskIndicator">
   <#if hasDue>
      <#-- items due today? -->
      <#if (dateCompare(date?date, due?date, 0, "==") == 1)>
         <img src="${url.context}/images/office/task_today.gif" alt="${message("office.task.due_today")}" />
      <#-- items overdue? -->
      <#elseif (dateCompare(date?date, due?date) == 1)>
         <img src="${url.context}/images/office/task_overdue.gif" alt="${message("office.task.overdue")}" />
      </#if>
   <#else>
         &nbsp;
   </#if>
      </span>
      <span class="taskItemDetails">
         <span style="font-weight: bold;">${(t.description!"")?html}</span> (${t.type?html})
   <#if hasDue>
            <br />${message("office.property.due_date")}: ${due?date}
   <#else>
            <br />(${message("office.message.no_due_date")})
   </#if>
      </span>
   </div>
</#list>
<#if taskNum = 0>
   <div>
      <div class="noItems">(${message("office.message.no_tasks")})</div>
   </div>
</#if>
</div>

<div class="headerRow">
   <div class="headerWrapper"><div class="header">${message("office.header.actions")}</div></div>
</div>

<div id="myAlfrescoActions" class="actionsPanel">
   <div id="nonStatusText">
      <ul>
<#if d.isDocument>
         <li>
            <a title="${message("office.action.save_to_alfresco")}" href="${url.serviceContext}/office/navigation${defaultQuery?html}">
               <img src="${url.context}/images/office/save_to_alfresco.gif" alt="${message("office.action.save_to_alfresco")}" />
               ${message("office.action.save_to_alfresco")}
            </a>
            <br />${message("office.action.save_to_alfresco.description")}
         </li>
</#if>
         <li>
            <a title="${message("office.action.create_collaboration_space")}" href="${url.serviceContext}/office/navigation${defaultQuery?html}&amp;cc=true">
               <img src="${url.context}/images/office/create_space.gif" alt="${message("office.action.create_collaboration_space")}" />
               ${message("office.action.create_collaboration_space")}
            </a>
            <br />${message("office.action.create_collaboration_space.description")}
         </li>
         <li>
            <a title="${message("office.action.launch_alfresco")}" href="${url.context}/navigate/browse" rel="_blank">
               <img src="${url.context}/images/logo/AlfrescoLogo16.gif" alt="${message("office.action.launch_alfresco")}" />
               ${message("office.action.launch_alfresco")}
            </a>
            <br />${message("office.action.launch_alfresco.description")}
         </li>
      </ul>
   </div>

   <div id="statusText"></div>

</div>

<div style="position: absolute; top: 0px; left: 0px; z-index: 100; display: none">
   <iframe id="if_externalComponenetMethodCall" name="if_externalComponenetMethodCall" src="" style="visibility: hidden;" width="0" height="0"></iframe>
</div>

<script type="text/javascript">//<![CDATA[
   OfficeMyAlfresco.taskCount = ${taskNum};
//]]></script>

</body>
</html>