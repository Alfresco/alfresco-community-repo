<#assign weekms=1000*60*60*24*7>
<#assign count=0>
<#-- get the filter mode from the passed in args -->
<#-- filters: 0=all, 1=word, 2=html, 3=pdf, 4=recent -->
<#if args.f?exists && args.f?length!=0><#assign filter=args.f?number><#else><#assign filter=0></#if>
<#if args.h?exists>
   <#assign docs=companyhome.nodeByReference[args.h].children?sort_by('name')>
<#else>
   <#assign docs=companyhome.childrenByLuceneSearch[args.q]?sort_by('name')>
</#if>
<#list docs as d>
   <#if d.isDocument>
      <#if (filter=0) ||
           (filter=1 && (d.mimetype="application/msword" || d.mimetype="application/vnd.openxmlformats-officedocument.wordprocessingml.document")) ||
           (filter=2 && d.mimetype="text/html") ||
           (filter=3 && d.mimetype="application/pdf") ||
           (filter=4 && (dateCompare(d.properties["cm:modified"],date,weekms) == 1 || dateCompare(d.properties["cm:created"], date, weekms) == 1))>
      <#assign count=count+1>
      <div class="docRow docRow${(count % 2 = 0)?string("Odd", "Even")}" id="${d.id}">
         <div class="docIcon">
            <a href="${url.context}${d.url}" target="new"><img class="docIconImage" alt="" width="16" height="16" src="${url.context}${d.icon16?replace(".gif",".png")}" border=0></a>
         </div>
         <div style="display:none"><img class="docIconImage64" alt="" width="64" height="64" src="${url.context}${d.icon64}"></div>
         <div>
            <a class="docItem" href="${url.context}${d.url}" target="new">${d.name?html}</a>
            <span class="docInfo" onclick="event.cancelBubble=true; AlfNodeInfoMgr.toggle('${d.nodeRef}',this);">
               <img src="${url.context}/images/icons/popup.gif" class="popupImage" width="16" height="16" />
            </span>
         </div>
         <div class="docDetail">
            <table cellpadding="2" cellspacing="0" border="0">
	            <tr>
	               <td>
	                  <span class="docMetaprop">${message("portlets.panel.description")}:</span>&nbsp;<span class="docMetadata"><#if d.properties.description?exists>${d.properties.description?html}<#else>&nbsp;</#if></span><br />
   	               <span class="docMetaprop">${message("portlets.panel.modified")}:</span>&nbsp;<span class="docMetadata">${d.properties.modified?datetime}</span><br />
   	               <span class="docMetaprop">${message("portlets.panel.modified_by")}:</span>&nbsp;<span class="docMetadata">${d.properties.modifier}</span>
	               </td>
	               <td width="24">&nbsp;</td>
	               <td>
	                  <span class="docMetaprop">${message("portlets.panel.created")}:</span>&nbsp;<span class="docMetadata">${d.properties.created?datetime}</span><br />
   	               <span class="docMetaprop">${message("portlets.panel.created_by")}:</span>&nbsp;<span class="docMetadata">${d.properties.creator}</span><br />
	                  <span class="docMetaprop">${message("portlets.panel.size")}:</span>&nbsp;<span class="docMetadata">${(d.size/1000)?string("0.##")} ${message("portlets.panel.kb")}</span>
	               </td>
	            </tr>
	         </table>
         </div>
         <div class="docResource doclistAjaxWait" id="${d.nodeRef}"></div>
      </div>
      </#if>
   </#if>
</#list>
<#-- hidden div with the count value for the page -->
<div id="docCountValue" style="display:none">${message("portlets.message.showing_items", count)}</div>
<#-- hidden div with the error message -->
<div id="displayTheError" style="display:none">${message("portlets.error.data_currently_unavailable")}</div>
<div id="previewCurrentlyUnavailable" style="display:none">${message("portlets.preview_currently_unavailable")}</div>
