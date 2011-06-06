<#import "cmis.lib.html.ftl" as cmisLib/>

<html>
  <@cmisLib.head>CMIS Federated Query Tool</@cmisLib.head>
  <body>
  <div id="container">
    <@cmisLib.navigation>Federated Query Tool: ${connectionCount} Connection<#if connectionCount != 1>s</#if></@cmisLib.navigation>
    <div id="content">

    <form id="query" name="query" action="<@cmisLib.cmisContextUrl/>/federatedquery">
    <table class="form">
        <tr><td valign="top">Statement</td><td><textarea name="statement" cols=80 rows=5>${formattedStatement!defaultStatement}</textarea></td></tr>
        <tr><td>Page size</td><td><input class="field" type="text" name="maxItems" value="${maxItems}"/></td></tr>
        <tr><td></td><td><@cmisLib.button "Execute Query"/></td></tr>
    </table>
    </form>

    <#if results??>
    <#list results as result>
    <hr>
    <table>
        <tr>
        <#if result.skipCount gt 0>
           <td><a class="pag-enabled" href="<@cmisLib.cmisContextUrl/>/federatedquery?statement=${encodeuri(formattedStatement)}&<@decSkipCounts results result.conn/>&maxItems=${maxItems}">&larr; Prev</a></td>
        <#else>
           <td><a class="pag-disabled" href="#">&larr; Prev</td>
        </#if>
        <td><@cmisLib.resultsHeader result.skipCount result.pageNumItems/> for ${result.conn.server.name?html} (${result.conn.server.description!""}) (${result.conn.userName!"<i>anonymous</i>"?html})</td>
        <#if result.hasMoreItems>
           <td><a class="pag-enabled" href="<@cmisLib.cmisContextUrl/>/federatedquery?statement=${encodeuri(formattedStatement)}&<@incSkipCounts results result.conn/>&maxItems=${maxItems}">Next &rarr;</a></td>
        <#else>
           <td><a class="pag-disabled" href="#">Next &rarr;</td>
        </#if>
        </tr>
    </table>
    
    <br>
    <table class="details">
        <#list result.rows as row>
        <#assign properties = row.properties>
        <#if row_index == 0>
        <tr>
        <#list properties as property>
           <th><b>${property.queryName!property.displayName!property.id}</b></th>
        </#list>
        </tr>
        </#if>
        
        <tr>
        <#list properties as property>
           <td><@cmisLib.propertyvalue property result.conn.id>,<br></@cmisLib.propertyvalue></td>
        </#list>
        </tr>
        </#list>
    </table>
    </#list>
    </#if>
    
    </div>
  </div>
  </body>
</html>

<#macro incSkipCounts results conn>
<#list results as result>${result.conn.id}_skipCount=<#if result.conn == conn>${result.skipCount + maxItems}<#else>${result.skipCount}</#if><#if result_has_next>&</#if></#list>
</#macro>

<#macro decSkipCounts results conn>
<#list results as result>${result.conn.id}_skipCount=<#if result.conn == conn>${result.skipCount - maxItems}<#else>${result.skipCount}</#if><#if result_has_next>&</#if></#list>
</#macro>