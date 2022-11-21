<#import "/org/springframework/extensions/webscripts/webscripts.lib.html.ftl" as wsLib/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<@wsLib.head>${msg("alfresco.index.title")}</@wsLib.head>
<body>
<div>
    <@wsLib.indexheader>Web Scripts Home</@wsLib.indexheader>
    <#if failures?size &gt; 0>
        <br/>
        <table>
            <tr><td><a href="${url.serviceContext}/index/failures">(+${failures?size} failed)</td></tr>
        </table>
    </#if>
    <br>
    <@wsLib.onlinedoc/>
    <br/>
    <span class="mainSubTitle">Index</span>
    <#if rootfamily.children?size &gt; 0>
        <table>
            <#list rootfamily.children as childpath>
                <tr><td><a href="${url.serviceContext}/index/family${childpath.path}">Browse '${childpath.name}' Web Scripts</a></td></tr>
            </#list>
        </table>
        <br/>
    </#if>
    <table>
        <tr><td><a href="${url.serviceContext}/index/all">Browse all Web Scripts</a></td></tr>
        <tr><td><a href="${url.serviceContext}/index/uri/">Browse by Web Script URI</a></td></tr>
        <tr><td><a href="${url.serviceContext}/index/package/">Browse by Web Script Package</a></td></tr>
        <tr><td><a href="${url.serviceContext}/index/lifecycle/">Browse by Web Script Lifecycle</a></td></tr>
    </table>
    <br/>
    <br/>
    <span class="mainSubTitle">Maintenance</span>
    <form id="refresh" action="${url.serviceContext}${url.match}" method="post">
        <input type="hidden" name="reset" value="on"/>
        <table>
            <#if failures?size &gt; 0>
                <tr><td><a href="${url.serviceContext}/index/failures">Browse failed Web Scripts</a></td></tr>
            </#if>
            <tr><td><a href="${url.serviceContext}/api/javascript/debugger">Alfresco Javascript Debugger</a></td></tr>
        </table>
        <br/>
        <table>
            <tr><td><input type="submit" name="submit" value="Refresh Web Scripts"/></td></tr>
        </table>
    </form>
</div>
<#assign CSRF=(config.scoped["CSRFPolicy"]["filter"].getChildren("rule")?size != 0)!false>
<#if CSRF>
<script type="text/javascript">
function addCsrfTokenToRefreshForm() {
   const properties = {};
    <#if config.scoped["CSRFPolicy"]["properties"]??>
        <#assign csrfProperties = (config.scoped["CSRFPolicy"]["properties"].children)![]>
        <#list csrfProperties as p>
   properties["${p.name?js_string}"] = "${(p.value!"")?js_string}";
        </#list>
   </#if>

   function substitute(str) {
      for (const prop in properties) {
         str = str.replace("{" + prop + "}", properties[prop]);
      }
      return str;
   }

   const csrfCookieName = substitute("${config.scoped["CSRFPolicy"]["client"].getChildValue("cookie")!""}");
   const csrfParamName = substitute("${config.scoped["CSRFPolicy"]["client"].getChildValue("parameter")!""}");
   const matchingCookies = document.cookie.match(new RegExp("(?:^|; )" + csrfCookieName + "=([^;]*)"));
   if (matchingCookies) {
      const csrfToken = decodeURIComponent(matchingCookies[1]).replace(/"/g, '');
      const form = document.getElementById('refresh');
      const originalAction = form.attributes.action.value;
      form.attributes.action.value = originalAction + (originalAction.lastIndexOf('?') === -1 ? "?" : "&") + csrfParamName + "=" + encodeURIComponent(csrfToken);
   }
}
window.addEventListener('load', addCsrfTokenToRefreshForm, false);
</script>
</#if>
</body>
</html>