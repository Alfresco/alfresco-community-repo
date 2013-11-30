<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
   <#assign isAccession=node.properties["rma:transferAccessionIndicator"]>
   <head>
      <#if isAccession>
         <title>${message("file.report.acession.report")}</title>
      <#else>
         <title>${message("file.report.transfer.report")}</title>
      </#if>
      <style>
         body { font-family: arial,verdana; font-size: 81%; color: #333; }
         .records { margin-left: 20px; margin-top: 10px; }
         .record { padding: 5px; }
         .label { color: #111; }
         .nodeName { font-weight: bold; }
         .transferred-item { background-color: #eee; padding: 10px; margin-bottom: 15px; }
      </style>
   </head>
   <body>
      <#if isAccession>
         <h1>${message("file.report.acession.report")}</h1>
      <#else>
         <h1>${message("file.report.transfer.report")}</h1>
      </#if>
      <table cellpadding="3" cellspacing="3">
         <tr>
            <td class="label">${message("file.report.transfer.date")}:</td>
            <td>${node.properties["cm:created"]?string(message("file.report.date.format"))?html}</td>
         </tr>
         <tr>
            <td class="label">${message("file.report.transfer.location")}:</td>
            <td>
               <#if isAccession>
               ${message("file.report.nara")}
               <#else>
               ${node.properties["rma:transferLocation"]?html}
               </#if>
            </td>
         </tr>
         <tr>
            <td class="label">${message("file.report.performed.by")}:</td>
            <td>${node.properties["cm:creator"]?html}</td>
         </tr>
         <tr>
            <td class="label">${message("file.report.disposition.authority")}:</td>
            <td>${properties["dispositionAuthority"]?html}</td>
         </tr>
      </table>
      <h2>${message("file.report.transferred.items")}</h2>
      <div class="transferred-item">
         <#-- FIXME
         <span class="nodeName">folder</span>&nbsp;(Unique Folder Identifier:&nbsp;2013-1385393610833)
         <div class="records">
         </div>
         -->
      </div>
   </body>
</html>