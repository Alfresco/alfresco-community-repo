<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
   <#assign isAccession=node.property["rma:transferAccessionIndicator"]>
   <head>
      <#if isAccession>
         <#-- FIXME: Label -->
         <title>Transfer Report</title>
      <#else>
         <#-- FIXME: Label -->
         <title>Transfer Report</title>
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
         <#-- FIXME: Label -->
         <h1>Accession Report</h1>
      <#else>
         <#-- FIXME: Label -->
         <h1>Transfer Report</h1>
      </#if>
      <table cellpadding="3" cellspacing="3">
         <tr>
            <#-- FIXME: Label -->
            <td class="label">Transfer Date:</td>
            <#-- FIXME: Escape, toString -->
            <td>${node.property["cm:created"]}</td>
         </tr>
         <tr>
            <#-- FIXME: Label -->
            <td class="label">Transfer Location:</td>
            <td>
               <#if isAccession>
               <#-- FIXME: Label -->
               NARA
               <#else>
               <#-- FIXME: String, Escape -->
               ${node.property["rma:transferLocation"]}
               </#if>
            </td>
         </tr>
         <tr>
            <#-- FIXME: Label -->
            <td class="label">Performed By:</td>
            <#-- FIXME: String, Escape -->
            <td> ${node.property["cm:creator"]}</td>
         </tr>
         <tr>
            <#-- FIXME: Label -->
            <td class="label">Disposition Authority:</td>
            <#-- FIXME: Disposition Authority - Check, escape -->
            <td></td>
         </tr>
      </table>
      <#-- FIXME: Label -->
      <h2>Transferred Items</h2>
      <div class="transferred-item">
         <#--
         <span class="nodeName">folder</span>&nbsp;(Unique Folder Identifier:&nbsp;2013-1385393610833)
         <div class="records">
         </div>
         -->
      </div>
   </body>
</html>