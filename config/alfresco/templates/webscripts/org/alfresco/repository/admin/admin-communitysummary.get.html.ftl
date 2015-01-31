<#include "admin-template.ftl" />

<@page title=msg("admin-console.tool.admin-communitysummary.label") readonly=true>
   
   <@section label=msg("communitysummary.system-information") />
   <div class="column-left">
      <#--<@attrfield attribute=sysPropsAttributes["alfresco.home"] label=msg("communitysummary.system-information.alfresco-home") />
      <@attrfield attribute=alfrescoAttributes["Edition"] label=msg("communitysummary.system-information.alfresco-edition") />
      <@attrfield attribute=alfrescoAttributes["VersionNumber"] label=msg("communitysummary.system-information.alfresco-version") />
      <@attrfield attribute=sysPropsAttributes["java.home"] label=msg("communitysummary.system-information.java-home") />
      <@attrfield attribute=sysPropsAttributes["java.version"] label=msg("communitysummary.system-information.java-version") />
      <@attrfield attribute=sysPropsAttributes["java.vm.vendor"] label=msg("communitysummary.system-information.java-vm-vendor") />-->
   </div>

   <div class="column-right">
      <#--<@attrfield attribute=sysPropsAttributes["os.name"] label=msg("communitysummary.system-information.operating-system") />
      <@attrfield attribute=sysPropsAttributes["os.version"] label=msg("communitysummary.system-information.version") />
      <@attrfield attribute=sysPropsAttributes["os.arch"] label=msg("communitysummary.system-information.architecture") />
      <@attrfield attribute=memoryAttributes["FreeMemory"] label=msg("communitysummary.system-information.free-memory") />
      <@attrfield attribute=memoryAttributes["MaxMemory"] label=msg("communitysummary.system-information.maximum-memory") />
      <@attrfield attribute=memoryAttributes["TotalMemory"] label=msg("communitysummary.system-information.total-memory") />-->
   </div>
   
</@page>