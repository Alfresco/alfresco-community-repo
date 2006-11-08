 <#-- Shows some general audit info about the current document -->
 <#if document?exists>
   <h4>Current Docuement Audit Info</h4>
   <b>Name:</b> ${document.name}<br>
   <table border="1" cellspacing="0" cellpadding="4">
   <tr>
          <th>User Name</th>
          <th>Application</th>
          <th>Service</th>
          <th>Method</th>
          <th>Timestamp</th>
          <th>Failed</th>
          <th>Message</th>
          <th>Arg 1</th>
          <th>Arg 2</th>
          <th>Arg 3</th>
          <th>Arg 4</th>
          <th>Arg 5</th>
          <th>Return</th>
          <th>Thowable</th> 
          <th>TX</th>
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
   <h4>Current Space Audit Info:</h4>
   <b>Name:</b> ${space.name}<br>
   <table border="1" cellspacing="0" cellpadding="4">
   <tr>
          <th>User Name</th>
          <th>Application</th>
          <th>Service</th>
          <th>Method</th>
          <th>Timestamp</th>
          <th>Failed</th>
          <th>Message</th>
          <th>Arg 1</th>
          <th>Arg 2</th>
          <th>Arg 3</th>
          <th>Arg 4</th>
          <th>Arg 5</th>
          <th>Return</th>
          <th>Thowable</th> 
          <th>TX</th>
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