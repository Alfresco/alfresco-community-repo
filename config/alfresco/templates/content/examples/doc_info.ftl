<#-- Shows some general info about the current document, including NodeRef and aspects applied -->
<#if document?exists>
   <h4>Current Document Info:</h4>
   <b>Name:</b> ${document.name}<br>
   <b>Ref:</b> ${document.nodeRef}<br>
   <b>Type:</b> ${document.type}<br>
   <b>DBID:</b> ${document.properties["sys:node-dbid"]}<br>
   <b>Content URL:</b> <a href="/alfresco${document.url}">/alfresco${document.url}</a><br>
   <b>Locked:</b> <#if document.isLocked>Yes<#else>No</#if><br>
   <#if hasAspect(document, "cm:countable") == 1 && document.properties['cm:counter']?exists>
   <b>Counter:</b> ${document.properties['cm:counter']}<br>
   </#if>
   <b>Aspects:</b>
   <table>
      <#list document.aspects as aspect>
         <tr><td>${aspect}</td></tr>
      </#list>
   </table>
   <b>Assocs:</b>
   <table>
      <#list document.assocs?keys as key>
         <tr><td>${key}</td><td>
         <#list document.assocs[key] as t>
            ${t.displayPath}/${t.name}<br>
         </#list>
         </td></tr>
      </#list>
   </table>
   <b>Properties:</b>
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
   No document found!
</#if>
