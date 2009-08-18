<#-- Shows some general info about the current document, including NodeRef and aspects applied -->
<#if document?exists>
   <h4>${message("templates.doc_info.current_document_info")}</h4>
   <b>${message("templates.doc_info.name")}</b> ${document.name}<br>
   <b>${message("templates.doc_info.ref")}</b> ${document.nodeRef}<br>
   <b>${message("templates.doc_info.type")}</b> ${document.type}<br>
   <b>${message("templates.doc_info.dbid")}</b> ${document.properties["sys:node-dbid"]}<br>
   <b>${message("templates.doc_info.content_url")}</b> <a href="${url.context}${document.url}">${url.context}${document.url}</a><br>
   <b>${message("templates.doc_info.locked")}</b> <#if document.isLocked>Yes<#else>No</#if><br>
   <#if hasAspect(document, "cm:countable") == 1 && document.properties['cm:counter']?exists>
   <b>${message("templates.doc_info.counter")}</b> ${document.properties['cm:counter']}<br>
   </#if>
   <b>${message("templates.doc_info.aspects")}</b>
   <table>
      <#list document.aspects as aspect>
         <tr><td>${aspect}</td></tr>
      </#list>
   </table>
   <b>${message("templates.doc_info.assocs")}</b>
   <table>
      <#list document.assocs?keys as key>
         <tr><td>${key}</td><td>
         <#list document.assocs[key] as t>
            ${t.displayPath}/${t.name}<br>
         </#list>
         </td></tr>
      </#list>
   </table>
   <b>${message("templates.doc_info.properties")}</b>
   <table>
      <#-- Get a list of all the property names for the document -->
      <#assign props = document.properties?keys>
      <#list props as t>
         <#-- If the property exists -->
         <#if document.properties[t]?exists>
            <#-- If it is a date, format it accordingly -->
            <#if document.properties[t]?is_date>
               <tr><td>${t} = ${document.properties[t]?datetime}</td></tr>
            
            <#-- If it is a boolean, format it accordingly -->
            <#elseif document.properties[t]?is_boolean>
               <tr><td>${t} = ${document.properties[t]?string("yes", "no")}</td></tr>
            
            <#-- If it is a collection, enumerate it -->
            <#elseif document.properties[t]?is_enumerable>
               <tr><td>${t} = <#list document.properties[t] as i>${i} </#list></td></tr>
            
            <#-- Otherwise treat it as a string -->
            <#else>
               <tr><td>${t} = ${document.properties[t]}</td></tr>
            </#if>
         </#if>
      </#list>
   </table>
<#else>
   ${message("templates.doc_info.no_document_found")}
</#if>