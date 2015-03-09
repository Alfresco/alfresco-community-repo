<#include "../admin-template.ftl" />

<@page title=msg("tenantconsole.title") controller="/admin/admin-tenantconsole" readonly=true>
   
   <div class="column-full">
      <@section label=msg("tenantconsole.command") />
      <@text id="cmd" name="tenant-cmd" label="" description="${cmd.description}" value="" controlStyle="width:55em" escape=false />
      <@button label=msg("tenantconsole.execute") onclick="AdminConsole_execute()" style="" />
   </div>
   
   <div class="column-full">
      <@section label=msg("tenantconsole.result") />
      <pre>${cmd.output}</pre>
   </div>
   
   <script type="text/javascript">//<![CDATA[

/* Page load handler */
Admin.addEventListener(window, 'load', function() {
   // bind Enter key press to call the Add button event handler
   Admin.addEventListener(el("cmd"), 'keypress', function(e) {
      if (e.keyCode === 13) AdminConsole_execute();
      return true;
   });
});

function AdminConsole_execute()
{
   el("${FORM_ID}").submit();
   return false;
}

//]]></script>

</@page>