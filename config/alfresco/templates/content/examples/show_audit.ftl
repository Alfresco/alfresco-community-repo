 <#-- Shows some general audit info about the current document -->
 <#if document?exists>
   <h4>${message("templates.show_audit.current_document_audit_info")}</h4>
   <b>${message("templates.show_audit.name")}</b> ${document.name}<br>
   <table border="1" cellspacing="0" cellpadding="4">
   <tr>
          <th>${message("templates.show_audit.user_name")}</th>
          <th>${message("templates.show_audit.application")}</th>
          <th>${message("templates.show_audit.service")}</th>
          <th>${message("templates.show_audit.method")}</th>
          <th>${message("templates.show_audit.timestamp")}</th>
          <th>${message("templates.show_audit.failed")}</th>
          <th>${message("templates.show_audit.message")}</th>
          <th>${message("templates.show_audit.arg_1")}</th>
          <th>${message("templates.show_audit.arg_2")}</th>
          <th>${message("templates.show_audit.arg_3")}</th>
          <th>${message("templates.show_audit.arg_4")}</th>
          <th>${message("templates.show_audit.arg_5")}</th>
          <th>${message("templates.show_audit.return")}</th>
          <th>${message("templates.show_audit.thowable")}</th> 
          <th>${message("templates.show_audit.tx")}</th>
   </tr>
   <#list document.auditTrail as t>
           <tr>
          <td>${t.userIdentifier}</td>
          <td>${t.auditApplication}</td>
          <#if t.auditService?exists>
             <td>${t.auditService}</td>
          <#else>
             <td>&nbsp;</td>
          </#if>
          <#if t.auditMethod?exists>
             <td>${t.auditMethod}</td>
          <#else>
             <td>&nbsp;</td>
          </#if>
          <td>${t.date}</td>
          <#if t.fail?exists>
             <td>${t.fail?string("FAILED", "OK")}</td>
          <#else>
             <td>&nbsp;</td>
          </#if>       
          <#if t.message?exists>
             <td>${t.message}</td>
          <#else>
             <td>&nbsp;</td>
          </#if>          
          <#if t.methodArgumentsAsStrings[0]?exists>
             <td>${t.methodArgumentsAsStrings[0]}</td>
          <#else>
             <td>&nbsp;</td>
          </#if>          
          <#if t.methodArgumentsAsStrings[1]?exists>
             <td>${t.methodArgumentsAsStrings[1]}</td>
          <#else>
             <td>&nbsp;</td>
          </#if>          
          <#if t.methodArgumentsAsStrings[2]?exists>
             <td>${t.methodArgumentsAsStrings[2]}</td>
          <#else>
             <td>&nbsp;</td>
          </#if>          
          <#if t.methodArgumentsAsStrings[3]?exists>
             <td>${t.methodArgumentsAsStrings[3]}</td>
          <#else>
             <td>&nbsp;</td>
          </#if>          
          <#if t.methodArgumentsAsStrings[4]?exists>
             <td>${t.methodArgumentsAsStrings[4]}</td>
          <#else>
             <td>&nbsp;</td>
          </#if>          
          <#if t.returnObjectAsString?exists>
             <td>${t.returnObjectAsString}</td>
          <#else>
             <td>&nbsp;</td>
          </#if>
          <#if t.throwableAsString?exists>
             <td>${t.throwableAsString}</td>
          <#else>
             <td>&nbsp;</td>
          </#if>          
          <td>${t.txId}</td>
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
          <th>${message("templates.show_audit.service")}</th>
          <th>${message("templates.show_audit.method")}</th>
          <th>${message("templates.show_audit.timestamp")}</th>
          <th>${message("templates.show_audit.failed")}</th>
          <th>${message("templates.show_audit.message")}</th>
          <th>${message("templates.show_audit.arg_1")}</th>
          <th>${message("templates.show_audit.arg_2")}</th>
          <th>${message("templates.show_audit.arg_3")}</th>
          <th>${message("templates.show_audit.arg_4")}</th>
          <th>${message("templates.show_audit.arg_5")}</th>
          <th>${message("templates.show_audit.return")}</th>
          <th>${message("templates.show_audit.thowable")}</th> 
          <th>${message("templates.show_audit.tx")}</th>
   </tr>
 
   <#list space.auditTrail as t>
           <tr>
          <td>${t.userIdentifier}</td>
          <td>${t.auditApplication}</td>
          <#if t.auditService?exists>
             <td>${t.auditService}</td>
          <#else>
             <td>&nbsp;</td>
          </#if>
          <#if t.auditMethod?exists>
             <td>${t.auditMethod}</td>
          <#else>
             <td>&nbsp;</td>
          </#if>
          <td>${t.date}</td>
          <#if t.fail?exists>
             <td>${t.fail?string("FAILED", "OK")}</td>
          <#else>
             <td>&nbsp;</td>
          </#if>       
          <#if t.message?exists>
             <td>${t.message}</td>
          <#else>
             <td>&nbsp;</td>
          </#if>          
          <#if t.methodArgumentsAsStrings[0]?exists>
             <td>${t.methodArgumentsAsStrings[0]}</td>
          <#else>
             <td>&nbsp;</td>
          </#if>          
          <#if t.methodArgumentsAsStrings[1]?exists>
             <td>${t.methodArgumentsAsStrings[1]}</td>
          <#else>
             <td>&nbsp;</td>
          </#if>          
          <#if t.methodArgumentsAsStrings[2]?exists>
             <td>${t.methodArgumentsAsStrings[2]}</td>
          <#else>
             <td>&nbsp;</td>
          </#if>          
          <#if t.methodArgumentsAsStrings[3]?exists>
             <td>${t.methodArgumentsAsStrings[3]}</td>
          <#else>
             <td>&nbsp;</td>
          </#if>          
          <#if t.methodArgumentsAsStrings[4]?exists>
             <td>${t.methodArgumentsAsStrings[4]}</td>
          <#else>
             <td>&nbsp;</td>
          </#if>          
          <#if t.returnObjectAsString?exists>
             <td>${t.returnObjectAsString}</td>
          <#else>
             <td>&nbsp;</td>
          </#if>
          <#if t.throwableAsString?exists>
             <td>${t.throwableAsString}</td>
          <#else>
             <td>&nbsp;</td>
          </#if>          
          <td>${t.txId}</td>
      </tr>
   </#list>
   </table>
 </#if>