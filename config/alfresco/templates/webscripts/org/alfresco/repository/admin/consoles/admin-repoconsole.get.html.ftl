<#include "../admin-template.ftl" />

<@page title=msg("repoconsole.title") controller="/admin/admin-repoconsole" readonly=true>
   
   <div class="column-full">
      <@section label=msg("repoconsole.command") />
      <@text id="cmd" name="repo-cmd" label="" description="${cmd.description}" value="" controlStyle="width:55em" escape=false />
      <@button label=msg("repoconsole.execute") onclick="AdminConsole_execute()" style="" />
   </div>
   
   <div class="column-full">
      <@section label=msg("repoconsole.result") />
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