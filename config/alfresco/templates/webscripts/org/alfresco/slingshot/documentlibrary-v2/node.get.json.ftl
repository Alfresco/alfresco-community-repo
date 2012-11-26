<#import "item.lib.ftl" as itemLib />
<#assign workingCopyLabel = " " + message("coci_service.working_copy_label")>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "metadata":
   {
      "repositoryId": "${server.id}",
      <#if doclist.parent?? && doclist.parent.nodeJSON??>"parent": <#noescape>${doclist.parent.nodeJSON},</#noescape></#if>
      <#if doclist.customJSON??>"custom": <#noescape>${doclist.customJSON},</#noescape></#if>
      "onlineEditing": ${doclist.onlineEditing?string},
      "workingCopyLabel": "${workingCopyLabel}",
      "shareURL": "${site.getShareUrl()}",
      "serverURL": "${url.server}"
   },
   "item":
   {
   <#if doclist.item??>
      <#assign item = doclist.item>
      "node": <#noescape>${item.nodeJSON}</#noescape>,
      <#if item.parent?? && item.parent.nodeJSON??>"parent": <#noescape>${item.parent.nodeJSON},</#noescape></#if>
      <@itemLib.itemJSON item=item />
   </#if>
   }
}
</#escape>
