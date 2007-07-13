<#assign isImage=node.isDocument && (node.mimetype = "image/gif" || node.mimetype = "image/jpeg" || node.mimetype = "image/png")>
<table width="690" cellpadding="2" cellspacing="0" border="0" onclick="event.cancelBubble=true;">
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
      <td width="24"></td>
      <td width="300">
         <table width="100%" cellpadding="0" cellspacing="0">
            <tr>
<#assign navurl='/navigate/showDocDetails/' + node.nodeRef.storeRef.protocol + '/' + node.nodeRef.storeRef.identifier + '/' + node.nodeRef.id>
<#if node.isLocked >
               <td class="docAction docActionCheckout docActionLocked">(Locked)</td>
<#elseif hasAspect(node, "cm:workingcopy") == 1>
               <td class="docAction docActionCheckin" <#if node.hasPermission("CheckIn")>onclick='event.cancelBubble=true;MyDocs.checkinItem("${node.name}", "${node.nodeRef}");'</#if>>Check In</td>
<#else>
               <td class="docAction docActionCheckout" <#if node.hasPermission("CheckOut")>onclick='event.cancelBubble=true;MyDocs.checkoutItem("${node.name}", "${node.nodeRef}");'</#if>>Check Out</td>
</#if>
<#if node.isLocked >
               <td class="docAction docActionEditDetails docActionLocked">Edit Details</td>
<#else>
               <td class="docAction docActionEditDetails" onclick="openWindowCallback('${url.context}/command/ui/editcontentprops?container=plain&amp;noderef=${node.nodeRef}', MyDocs.editDetailsCallback);">Edit Details</td>
</#if>
            </tr>
            <tr>
<#if node.isLocked >
               <td class="docAction docActionUpdate docActionLocked">Update</td>
<#else>
               <td class="docAction docActionUpdate" onclick="event.cancelBubble=true;MyDocs.updateItem(this, '${node.nodeRef}');">Update</td>
</#if>
               <td class="docAction docActionViewContent" onclick="window.open('${url.context}${node.downloadUrl}', '_blank');">View Content</td>
            </tr>
            <tr>
               <td class="docAction docActionDelete" <#if node.hasPermission("Delete")>onclick='event.cancelBubble=true;MyDocs.deleteItem("${node.name}", "${node.nodeRef}");'</#if>>Delete</td>
               <td class="docAction docActionMoreActions" onclick="window.open('${url.context}${navurl}', '_blank');">More Actions...</td>
            </tr>
         </table>
      </td>
   </tr>
</table>
