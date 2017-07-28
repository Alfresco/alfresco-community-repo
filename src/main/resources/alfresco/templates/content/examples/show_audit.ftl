 <#-- Shows some general audit info about the current document -->
 <#if document?exists>
   <h4>${message("templates.show_audit.current_document_audit_info")}</h4>
   <b>${message("templates.show_audit.name")}</b> ${document.name}<br>
   <table border="1" cellspacing="0" cellpadding="4">
   <tr>
          <th>${message("templates.show_audit.user_name")}</th>
          <th>${message("templates.show_audit.application")}</th>
          <th>${message("templates.show_audit.method")}</th>
          <th>${message("templates.show_audit.timestamp")}</th>
          <th>${message("templates.show_audit.values")}</th>
   </tr>
   <#list document.auditTrail as t>
           <tr>
          <td>${t.userIdentifier}</td>
          <td>${t.auditApplication}</td>
          <#if t.auditMethod?exists>
             <td>${t.auditMethod}</td>
          <#else>
             <td>&nbsp;</td>
          </#if>
          <td>${t.date?datetime}</td>
          <#if t.values?exists>
          <td>
          <@hashMap map=t.values />
          </td>
          <#else>
             <td>&nbsp;</td>
          </#if>       
      </tr>
   </#list>
   </table>
 <#elseif space?exists>
   <h4>${message("templates.show_audit.current_space_audit_info")}</h4>
   <b>${message("templates.show_audit.name")}</b> ${space.name}<br>
   <table border="1" cellspacing="0" cellpadding="4">
   <tr>
          <th>${message("templates.show_audit.user_name")}</th>
          <th>${message("templates.show_audit.application")}</th>
          <th>${message("templates.show_audit.method")}</th>
          <th>${message("templates.show_audit.timestamp")}</th>
          <th>${message("templates.show_audit.values")}</th>
   </tr>
 
   <#list space.auditTrail as t>
           <tr>
          <td>${t.userIdentifier}</td>
          <td>${t.auditApplication}</td>
          <#if t.auditMethod?exists>
             <td>${t.auditMethod}</td>
          <#else>
             <td>&nbsp;</td>
          </#if>
          <td>${t.date?datetime}</td>
          <#if t.values?exists>
          <td>
          <@hashMap map=t.values />
          </td>
          <#else>
             <td>&nbsp;</td>
          </#if>       
      </tr>
   </#list>
   </table>
          </#if>          
 
<#-- renders an audit entry values -->
<#macro hashMap map simpleMode=false>
    <ul>
    <#assign index = 0 />
    <#list map?keys as key>
    <#if simpleMode>
    <li><@parseValue value=key />=<@parseValue value=map?values[index] /></li>
    <#else>
    <#assign value = map[key] />
    <#if value?is_sequence>
    <li><@parseValue value=key />=
    <ul>
    <#list value as element>
    <li><@parseValue value=element /></li>
    </#list>
    </ul>
    </li>
    <#elseif value?is_hash>
    <li><@parseValue value=key />=
    <@hashMap map=value simpleMode=true />
    </li>
    <#else>
    <li><@parseValue value=key />=<@parseValue value=value /></li>
    </#if>          
    </#if>          
    <#assign index = index + 1 />
   </#list>
    </ul>
</#macro>

<#-- renders an audit entry value -->
<#macro parseValue value="null">
    <#if value?is_number>
    ${value?c}
    <#elseif value?is_boolean>
    ${value?string}
    <#elseif value?is_date>
    ${value?datetime}
    <#elseif value?is_string && value != "null">
    ${shortQName(value?string)}
    <#elseif value?is_hash && value?values[0]?exists>
    ${value?values[0]}
    </#if>
</#macro>