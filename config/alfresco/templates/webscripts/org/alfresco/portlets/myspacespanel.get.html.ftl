<#assign user=person.properties.userName>
<#assign count=0>
<#assign weekms=1000*60*60*24*7>
<#function encodepath node>
<#if node.parent?exists><#return encodepath(node.parent) + "/" + node.name?url><#else><#return ""></#if>
</#function>
<#list companyhome.nodeByReference[args.h].children?sort_by('name') as d>
   <#if (d.isDocument || 
           (d.type != "{http://www.alfresco.org/model/forum/1.0}forums" &&
   		   d.type != "{http://www.alfresco.org/model/wcmappmodel/1.0}webfolder" &&
   		   d.type != "{http://www.alfresco.org/model/content/1.0}systemfolder")) &&
         ((args.f="0") ||
          (args.f="1" && !d.isDocument) ||
          (args.f="2" && d.isDocument) ||
          (args.f="3" && (d.properties.creator == user || d.properties.modifier == user)) ||
          (args.f="4" && (dateCompare(d.properties["cm:modified"],date,weekms) == 1 || dateCompare(d.properties["cm:created"], date, weekms) == 1)))>
   <#assign count=count+1>
   <div class="spaceRow spaceRow${(count % 2 = 0)?string("Odd", "Even")}" id="${d.id}">
      <div class="spaceIcon">
         <#if d.isDocument>
            <a href="${url.context}${d.url}" target="new" onclick="event.cancelBubble=true"><img class="spaceIconImage" alt="" width="16" height="16" src="${url.context}${d.icon16?replace(".gif",".png")}" border=0></a>
         <#elseif d.type="{http://www.alfresco.org/model/application/1.0}folderlink"> 
            <#-- the component parts need to build up an encoded url to the outer webscript -->
            <#-- the client-side url encoder method of the outer webscript runtime will be used -->
            <span class="spaceNavLinkUrl">${url.serviceContext}/ui/myspaces?f=${args.f}&amp;p=${encodepath(d.properties.destination)}</span>
            <span class="spaceNavLinkImg" style="display:none"><img class="spaceIconImage" alt="" width="16" height="16" src="${url.context}${d.icon16?replace(".gif",".png")}" border="0"></span> 
         <#else>
            <span class="spaceNavLinkUrl">${url.serviceContext}/ui/myspaces?f=${args.f}&amp;p=${args.p?url}%2F${d.name?url}</span>
            <span class="spaceNavLinkImg" style="display:none"><img class="spaceIconImage" alt="" width="16" height="16" src="${url.context}${d.icon16?replace(".gif",".png")}" border="0"></span>
         </#if>
         <div style="display:none"><img class="spaceIconImage64" alt="" width="64" height="64" src="${url.context}${d.icon64}"></div>
      </div>
      <div class="spaceItem">
         ${d.name?html}
         <span class="spaceInfo" onclick="event.cancelBubble=true; AlfNodeInfoMgr.toggle('${d.nodeRef}',this);">
            <img src="${url.context}/images/icons/popup.gif" class="popupImage" width="16" height="16" />
         </span>
      </div>
      <div class="spaceDetail">
         <table cellpadding="2" cellspacing="0" border="0">
            <tr>
               <td>
                  <span class="spaceMetaprop">${message("portlets.panel.description")}:</span>&nbsp;<span class="spaceMetadata"><#if d.properties.description?exists>${d.properties.description?html}<#else>&nbsp;</#if></span><br />
	               <span class="spaceMetaprop">${message("portlets.panel.modified")}:</span>&nbsp;<span class="spaceMetadata">${d.properties.modified?datetime}</span><br />
	               <span class="spaceMetaprop">${message("portlets.panel.modified_by")}:</span>&nbsp;<span class="spaceMetadata">${d.properties.modifier}</span>
               </td>
               <td width="24">&nbsp;</td>
               <td>
                  <span class="spaceMetaprop">${message("portlets.panel.created")}:</span>&nbsp;<span class="spaceMetadata">${d.properties.created?datetime}</span><br />
	               <span class="spaceMetaprop">${message("portlets.panel.created_by")}:</span>&nbsp;<span class="spaceMetadata">${d.properties.creator}</span><br />
                  <span class="spaceMetaprop">${message("portlets.panel.size")}:</span>&nbsp;<span class="spaceMetadata">${(d.size/1000)?string("0.##")} ${message("portlets.panel.kb")}</span>
               </td>
            </tr>
         </table>
      </div>
      <div class="spaceResource spacesAjaxWait" id="${d.nodeRef}"></div>
   </div>
   </#if>
</#list>
<#-- hidden div with the count value for the page -->
<div id="spaceCountValue" style="display:none">${message("portlets.message.showing_items", count)}</div>
<#-- hidden div with the error message -->
<div id="displayTheError" style="display:none">${message("portlets.error.data_currently_unavailable")}</div>
<div id="previewCurrentlyUnavailable" style="display:none">${message("portlets.preview_currently_unavailable")}</div>
