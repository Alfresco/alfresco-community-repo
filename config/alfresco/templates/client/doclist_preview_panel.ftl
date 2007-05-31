<#assign isImage=node.isDocument && (node.mimetype = "image/gif" || node.mimetype = "image/jpeg" || node.mimetype = "image/png")>
<table width="100%" cellpadding="2" cellspacing="0" border="0" onclick="event.cancelBubble=true;">
   <tr>
      <td>
         <div class="docPreview">
      	   <#if node.isDocument && !isImage>
      	      <#assign c=cropContent(node.properties.content, 2048)>
      	      <#if c?length != 0>
                  ${c?html?replace('$', '<br>', 'rm')}<#if (c?length >= 2048)>...</#if>
      	      </#if>
            <#elseif isImage>
	            <center><a href="${url.context}${node.url}" target="new"><img src="${url.context}${node.url}" height=140 border=0></a></center>
      	   </#if>
         </div>
      </td>
      <td width="24"></td>
      <td width="300">
         <table width="100%" cellpadding="0" cellspacing="0">
            <tr>
               <td class="docAction docActionCheckout">Checkout</td>
               <td class="docAction docActionEditDetails">Edit Details</td>
            </tr>
            <tr>
               <td class="docAction docActionUpdate">Update</td>
               <td class="docAction docActionViewContent">View Content</td>
            </tr>
            <tr>
               <td class="docAction docActionDelete">Delete</td>
               <td class="docAction docActionMoreActions">More Actions...</td>
            </tr>
         </table>
      </td>
   </tr>
</table>
