<#assign isImage=node.isDocument && (node.mimetype = "image/gif" || node.mimetype = "image/jpeg" || node.mimetype = "image/png")>
<table width="692" cellpadding="2" cellspacing="0" border="0" onclick="event.cancelBubble=true;">
   <tr>
      <td>
         <div class="docPreview">
      	   <#if node.isDocument && !isImage>
      	      <#assign c=cropContent(node.properties.content, 2048)>
      	      <#if c?length != 0>
                  ${c?html?replace('$', '<br>', 'rm')}<#if (c?length >= 2048)>...</#if>
      	      </#if>
            <#elseif isImage>
	            <center><a href="${url.context}${node.url}" target="new"><img src="${url.context}${node.url}?${node.size}" height="140" border="0"></a></center>
      	   </#if>
         </div>
      </td>
      <td width="24" align="center">
         <table cellpadding="0" cellspacing="0" border="0" width="1">
         <tr>
            <td width="1" style="background-color: #75badd; height: 140px; width: 1px;"></td>
         </tr>
         </table>
      </td>
      <td width="300">
         <table width="100%" cellpadding="0" cellspacing="0">
            <tr>
<#assign navurl='/navigate/showDocDetails/' + node.nodeRef.storeRef.protocol + '/' + node.nodeRef.storeRef.identifier + '/' + node.nodeRef.id>
<#if node.isLocked>
               <td class="docAction docActionCheckout docActionLocked">(${msg("locked")})</td>
<#elseif hasAspect(node, "cm:workingcopy") == 1>
               <td class="docAction docActionCheckin" <#if node.hasPermission("CheckIn")>onclick='event.cancelBubble=true;MyDocs.checkinItem("${msg("portlets.checkin.item_working_copy_of_name_has_been_checked_in", node.name)}", "${node.nodeRef}");'</#if>>${msg("checkin")}</td>
<#else>
               <td class="docAction docActionCheckout" <#if node.hasPermission("CheckOut")>onclick='event.cancelBubble=true;MyDocs.checkoutItem("${msg("portlets.checkout.working_copy_for_the_checked_out", node.name)}", "${node.nodeRef}");'</#if>>${msg("checkout")}</td>
</#if>
<#if node.isLocked>
               <td class="docAction docActionEditDetails docActionLocked">${msg("edit_details")}</td>
<#else>
               <td class="docAction docActionEditDetails" onclick="openWindowCallback('${url.context}/command/ui/editcontentprops?container=plain&amp;noderef=${node.nodeRef}', MyDocs.editDetailsCallback);">${msg("edit_details")}</td>
</#if>
            </tr>
            <tr>
<#if node.isLocked>
               <td class="docAction docActionUpdate docActionLocked">${msg("update")}</td>
<#else>
               <td class="docAction docActionUpdate" onclick="event.cancelBubble=true;MyDocs.updateItem(this, '${node.nodeRef}');">${msg("update")}</td>
</#if>
               <td class="docAction docActionViewContent" onclick="window.open('${url.context}${node.downloadUrl}', '_blank');">${msg("download")}</td>
            </tr>
            <tr>
<#if node.isLocked>
               <td class="docAction docActionDelete docActionLocked">${msg("delete")}</td>
<#else>
               <td class="docAction docActionDelete" <#if node.hasPermission("Delete")>onclick='event.cancelBubble=true;MyDocs.deleteItem("${node.name}", "${node.nodeRef}");'</#if>>${msg("delete")}</td>
</#if>
               <td class="docAction docActionMoreActions" onclick="window.open('${url.context}${navurl}', '_blank');">${msg("more_actions")}...</td>
            </tr>
         </table>
      </td>
   </tr>
</table>
