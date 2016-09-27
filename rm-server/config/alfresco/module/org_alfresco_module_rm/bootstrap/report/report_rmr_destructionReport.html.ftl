<html>
   <head>
      <style type="text/css"><!--
      body
      {
         font-family: Arial, sans-serif;
         font-size: 14px;
         color: #4c4c4c;
      }

      a, a:visited
      {
         color: #0072cf;
      }
      --></style>
   </head>
   <body bgcolor="#dddddd">
      <table width="100%" cellpadding="20" cellspacing="0" border="0" bgcolor="#dddddd">
         <tr>
            <td width="100%" align="center">
               <table width="70%" cellpadding="0" cellspacing="0" bgcolor="white" style="background-color: white; border: 1px solid #aaaaaa;">
                  <tr>
                     <td width="100%">
                        <table width="100%" cellpadding="0" cellspacing="0" border="0">
                           <tr>
                              <td style="padding: 10px 30px 0px;">
                                 <table width="100%" cellpadding="0" cellspacing="0" border="0">
                                    <tr>
                                       <td>
                                          <table cellpadding="0" cellspacing="0" border="0">
                                             <tr>
                                                <td>
                                                   <img src="${shareUrl}/res/components/images/task-64.png" alt="" width="64" height="64" border="0" style="padding-right: 20px;" />
                                                </td>
                                                <td>
                                                   <div style="font-size: 22px; padding-bottom: 4px;">
                                                      ${message("file.report.destruction.report")}
                                                   </div>
                                                </td>
                                             </tr>
                                          </table>
                                          <div style="font-size: 14px; margin: 12px 0px 24px 0px; padding-top: 10px; border-top: 1px solid #aaaaaa;">
                                          <table cellpadding="2" cellspacing="3" border="0">
                                             <tr>
                                                <td><i>${message("file.report.destroyed")} <#if node.hasAspect("rma:record")>${message("file.report.record")}<#else>${message("file.report.record.folder")}</#if>:</i></td>
                                                <td>${node.properties["rma:identifier"]} <b>${node.properties.name}</b></td>
                                             </tr>
                                             <tr>
                                                <td><i>${message("file.report.disposition.authority")}:</i></td>
                                                <td>
                                                   <#if node.properties["rma:recordSearchDispositionAuthority"]??>
                                                   ${node.properties["rma:recordSearchDispositionAuthority"]}
                                                   </#if>
                                                </td>
                                             </tr>
                                             <tr>
                                                <td><i>${message("file.report.disposition.instructions")}:</i></td>
                                                <td>
                                                   <#if node.properties["rma:recordSearchDispositionInstructions"]??>
                                                   ${node.properties["rma:recordSearchDispositionInstructions"]}
                                                   </#if>
                                                </td>
                                             </tr>
                                          </table>
                                          <#if  node.childAssociations["cm:contains"]??>
                                             <div style="font-size: 14px; margin: 12px 0px 24px 0px; padding-top: 10px; border-top: 1px solid #aaaaaa;">
                                             <table cellpadding="2" cellspacing="3" border="0">
                                                <tr>
                                                   <td><i>${message("file.report.destroyed.records")}:</i></td>
                                                   <td></td>
                                                </tr>
                                             </table>
                                             <table cellpadding="0" callspacing="0" border="0" bgcolor="#eeeeee" style="padding:10px; border: 1px solid #aaaaaa;">
                                                <tr>
                                                   <td>
                                                      <table cellpadding="0" cellspacing="0" border="0">
                                                         <#list node.childAssociations["cm:contains"] as child>
                                                            <tr>
                                                               <td valign="top">
                                                                  <img src="${url}/${child.icon32}" alt="" width="32" height="32" border="0" style="padding-right: 10px;" />
                                                               </td>
                                                               <td>
                                                                  <table cellpadding="2" cellspacing="0" border="0">
                                                                     <tr>
                                                                        <td>${child.properties["rma:identifier"]} <b>${child.properties.name}</b></td>
                                                                     </tr>
                                                                  </table>
                                                               </td>
                                                            </tr>
                                                         </#list>
                                                      </table>
                                                   </td>
                                                </tr>
                                             </table>
                                          </#if>
                                          <div style="font-size: 14px; margin: 12px 0px 24px 0px; padding-top: 10px; border-top: 1px solid #aaaaaa;">
                                          </div>
                                       </td>
                                    </tr>
                                 </table>
                              </td>
                           </tr>
                           <tr>
                              <td style="padding: 10px 30px;">
                                 <img src="${shareUrl}/themes/default/images/app-logo.png" alt="" width="117" height="48" border="0" />
                              </td>
                           </tr>
                        </table>
                     </td>
                  </tr>
               </table>
            </td>
         </tr>
      </table>
   </body>
</html>