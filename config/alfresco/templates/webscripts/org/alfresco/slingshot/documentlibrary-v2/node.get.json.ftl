<#import "item.lib.ftl" as itemLib />
<#assign workingCopyLabel = " " + message("coci_service.working_copy_label")>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "metadata":
   {
      "repositoryId": "${server.id}",
      <#if doclist.container??>"container": "${doclist.container.nodeRef}",</#if>
      "onlineEditing": ${doclist.onlineEditing?string},
      "workingCopyLabel": "${workingCopyLabel}"
   },
   "item":
   {
   <#if doclist.item??>
      <#assign item = doclist.item>
      "node": <#noescape>${item.nodeJSON}</#noescape>,
      <#if item.parent??>"parent": <#noescape>${item.parent.nodeJSON},</#noescape></#if>
      <@itemLib.itemJSON item=item />
   </#if>
   }
}
</#escape>
