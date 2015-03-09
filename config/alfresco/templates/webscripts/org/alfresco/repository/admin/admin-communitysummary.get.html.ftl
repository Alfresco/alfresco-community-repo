<#include "admin-template.ftl" />

<@page title=msg("admin-console.tool.admin-communitysummary.label") readonly=true>
   
   <@section label=msg("communitysummary.system-information") />
   <div class="column-left">
      <@field value=sysPropsAttributes["java.home"] label=msg("communitysummary.system-information.java-home") />
      <@field value=sysPropsAttributes["java.version"] label=msg("communitysummary.system-information.java-version") />
      <@field value=sysPropsAttributes["java.vm.vendor"] label=msg("communitysummary.system-information.java-vm-vendor") />
      <@field value=sysPropsAttributes["os.name"] label=msg("communitysummary.system-information.operating-system") />
      <@field value=sysPropsAttributes["os.version"] label=msg("communitysummary.system-information.version") />
      <@field value=sysPropsAttributes["os.arch"] label=msg("communitysummary.system-information.architecture") />
   </div>
   
   <div class="column-right">
      <@field value=memoryAttributes["FreeMemory"] label=msg("communitysummary.system-information.free-memory") />
      <@field value=memoryAttributes["MaxMemory"] label=msg("communitysummary.system-information.maximum-memory") />
      <@field value=memoryAttributes["TotalMemory"] label=msg("communitysummary.system-information.total-memory") />
      <@field value=memoryAttributes["CPUs"] label=msg("communitysummary.system-information.cpus") />
   </div>
   
   <div class="column-full">
      <@section label=msg("communitysummary.repository") />
      <@field value=alfrescoAttributes["edition"] label=msg("communitysummary.system-information.alfresco-edition") />
      <@field value=alfrescoAttributes["version"] label=msg("communitysummary.system-information.alfresco-version") />
      <@field value=alfrescoAttributes["versionLabel"] label=msg("communitysummary.system-information.version-label") description=msg("communitysummary.system-information.version-label.description") />
      <@field value=alfrescoAttributes["schema"] label=msg("communitysummary.system-information.schema") description=msg("communitysummary.system-information.schema.description") />
      <@field value=alfrescoAttributes["id"] label=msg("communitysummary.system-information.id") description=msg("communitysummary.system-information.id.description") />
   </div>
   
</@page>