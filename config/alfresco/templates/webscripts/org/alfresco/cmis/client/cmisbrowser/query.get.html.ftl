<#import "cmis.lib.html.ftl" as cmisLib/>

<html>
  <@cmisLib.head>CMIS Query Tool</@cmisLib.head>
  <body>
  <div id="container">
    <@cmisLib.connectionNavigation conn>Query Tool</@cmisLib.connectionNavigation>
    <div id="content">

    <form id="query" name="query" action="<@cmisLib.cmisContextUrl conn.id/>/query">
    <table class="form">
        <tr><td valign="top">Statement</td><td><textarea name="statement" cols=80 rows=5>${formattedStatement!defaultStatement}</textarea></td></tr>
        <tr><td>Page size</td><td><input class="field" type="text" name="maxItems" value="${maxItems}"/></td></tr>
        <tr><td></td><td><@cmisLib.button "Execute Query"/></td></tr>
    </table>
    </form>

    <#if rows??>
    <hr>
    <table>
        <tr>
        <#if skipCount gt 0>
           <td><a class="pag-enabled" href="<@cmisLib.cmisContextUrl conn.id/>/query?statement=${encodeuri(formattedStatement)}&skipCount=${skipCount - maxItems}&maxItems=${maxItems}">&larr; Prev</a></td>
        <#else>
           <td><a class="pag-disabled" href="#">&larr; Prev</td>
        </#if>
        <td><@cmisLib.resultsHeader skipCount pageNumItems/></td>
        <#if hasMoreItems>
           <td><a class="pag-enabled" href="<@cmisLib.cmisContextUrl conn.id/>/query?statement=${encodeuri(formattedStatement)}&skipCount=${skipCount + maxItems}&maxItems=${maxItems}">Next &rarr;</a></td>
        <#else>
           <td><a class="pag-disabled" href="#">Next &rarr;</td>
        </#if>
        </tr>
    </table>
    
    <br>
    <table class="details">
        <#list rows as row>
        <#assign properties = row.properties>
        <#if row_index == 0>
        <tr>
        <#list properties as property>
           <th><b>${property.queryName!property.displayName!property.id?html}</b></th>
        </#list>
        </tr>
        </#if>
        
        <tr>
        <#list properties as property>
           <td><@cmisLib.propertyvalue property conn.id>,<br></@cmisLib.propertyvalue></td>
        </#list>
        </tr>
        </#list>
    </table>
    </#if>

    </div>
  </div>
  </body>
</html>
