<#assign null><span style="color:red">${msg("nodebrowser.null")?html}</span></#assign>
<#assign none><span style="color:red">${msg("nodebrowser.none")?html}</span></#assign>
<#assign collection>${msg("nodebrowser.collection")?html}</#assign>

<#macro dateFormat date>${date?string("dd MMM yyyy HH:mm:ss 'GMT'Z '('zzz')'")}</#macro>
<#macro propValue p>
<#if p.value??>
   <#if p.value?is_date>
      <@dateFormat p.value />
   <#elseif p.value?is_boolean>
      ${p.value?string}
   <#elseif p.value?is_number>
      ${p.value?c}
   <#elseif p.value?is_string>
      ${p.value?html}
   <#elseif p.value?is_hash>
      <#assign result = "{"/>
      <#assign first = true />
      <#list p.value?keys as key>
         <#if first = false>
            <#assign result = result + ", "/>
         </#if>
         <#assign result = result + "${key}=${p.value[key]?html}" />
         <#assign first = false/>
      </#list>
      <#assign result = result + "}"/>
      ${result}
   </#if>
<#else>
   ${null}
</#if>
</#macro>
<#macro contentUrl nodeRef prop>
${url.serviceContext}/api/node/${nodeRef?replace("://","/")}/content;${prop?url}
</#macro>

<#include "../admin-template.ftl" />

<@page title=msg("nodebrowser.title") controller="/admin/admin-nodebrowser" readonly=true>
   
   <div class="column-full">
      <@section label=msg("nodebrowser.store") />
      <@options name="nodebrowser-store" style="display:inline" valueStyle="display:inline" onchange="AdminConsole_action('root')" value="${args.store!'workspace://SpacesStore'}">
         <#list stores as s>
            <@option label=s value=s />
         </#list>
      </@options>
      <@button label=msg("nodebrowser.root") onclick="AdminConsole_action('root')" />
      <#if action??><@button label=msg("nodebrowser.refresh") onclick="AdminConsole_action('${action?html}')" class="input" style="position:absolute;top:60px;left:1042px" /></#if>
      
      <@section label=msg("nodebrowser.query") />
      <@options name="nodebrowser-search" style="display:inline" valueStyle="display:inline" value="${args.searcher!''}">
         <@option label="noderef" value="noderef" />
         <@option label="fts-alfresco" value="fts-alfresco" />
         <@option label="lucene" value="lucene" />
         <@option label="xpath" value="xpath" />
         <@option label="selectnodes" value="selectnodes" />
         <@option label="cmis-strict" value="cmis-strict" />
         <@option label="cmis-alfresco" value="cmis-alfresco" />
         <@option label="db-afts" value="db-afts" />
         <@option label="db-cmis" value="db-cmis" />
      </@options>
      <@text id="query" name="nodebrowser-query" label="" value="${query!''}" style="display:inline" valueStyle="display:inline" controlStyle="width:50em" />
      <@button label=msg("nodebrowser.execute") onclick="AdminConsole_action('search')" />
      <@tsection label=msg("nodebrowser.search-settings")>
         <div class="column-left">
            <@text name="nodebrowser-query-maxresults" label=msg("nodebrowser.maxresults") value="${args.maxResults!''}" />
         </div>
         <div class="column-right">
            <@text name="nodebrowser-query-skipcount" label=msg("nodebrowser.skipcount") value="${args.skipCount!''}" />
         </div>
      </@tsection>
      
      <!-- hidden values set by button and key events to provide action ID to the Form POST -->
      <@hidden name="nodebrowser-action" id="action" />
      <@hidden name="nodebrowser-action-value" id="action-value" value="${actionValue!''}" />
      <@hidden name="nodebrowser-execute" id="execute" />
      <@hidden name="nodebrowser-execute-value" id="execute-value" />
   </div>
   
   <#if result??>
   
   <#if result.info??>
   <div class="column-full">
      <a name="parent"></a><a name="children"></a>
      <@section label=msg("nodebrowser.node-info") />
      <table id="info-table" class="node">
         <tr><td class="node-info">${msg("nodebrowser.reference")}</td><td>${result.info.nodeRef}</td></tr>
         <tr><td class="node-info">${msg("nodebrowser.path")}</td><td style="word-break:break-all">${result.info.path!""}</td></tr>
         <tr><td class="node-info">${msg("nodebrowser.type")}</td><td>${result.info.type}</td></tr>
         <tr><td class="node-info">${msg("nodebrowser.parent")}</td><td><a href="#" onclick="AdminConsole_parentClick('${result.info.parent}');return false;">${result.info.parent}</a></td></tr>
      </table>
   </div>
   </#if>
   
   <#if result.properties??>
   <div class="column-full">
      <#assign propsMsg>${msg("nodebrowser.properties")} (<#if result.properties??>${result.properties?size?c}<#else>0</#if>)</#assign>
      <@section label=propsMsg />
      <table id="properties-table" class="node grid">
         <tr>
            <th>${msg("nodebrowser.name")}</th>
            <th>${msg("nodebrowser.type")}</th>
            <th>${msg("nodebrowser.value")}</th>
            <th>${msg("nodebrowser.residual")}</th>
            <th>${msg("nodebrowser.actions")}</th>
         </tr>
         <#list result.properties as p>
         <tr>
            <td>${p.name.prefixedName}</td>
            <td><#if p.typeName??>${p.typeName.prefixedName}<#else>${none}</#if></td>
            <td><#if (p.values?size > 1)><span style="color:red">${collection} (${p.values?size?c})</span><br></#if><#list p.values as v><#if v.content><a target="new" href="<@contentUrl result.info.nodeRef p.name.prefixedName/>"></#if><@propValue v/><#if v.content></a></#if><#if v_has_next><br></#if></#list></td>
            <td>${p.residual?string}</td>
            <td>
               <b><a href="#" onclick="AdminConsole_confirmExecute('${result.info.nodeRef}|${p.name}','delete-property');return false;" title="${msg("nodebrowser.delete-property.tip")}">${msg("nodebrowser.delete")}</a></b>
            </td>
         </tr>
         </#list>
      </table>
   </div>
   </#if>
   
   <#if result.aspects??>
   <div class="column-full">
      <#assign aspectMsg>${msg("nodebrowser.aspects")} (<#if result.aspects??>${result.aspects?size?c}<#else>0</#if>)</#assign>
      <@section label=aspectMsg />
      <table id="aspects-table" class="node">
         <#list result.aspects as a>
         <tr><td>${a.prefixedName}</td></tr>
         </#list>
      </table>
   </div>
   </#if>
   
   <div class="column-full">
      <#assign childMsg><#if action?? && action="search">${msg("nodebrowser.results")}<#else>${msg("nodebrowser.children")}</#if> (${result.children?size?c})</#assign>
      <@section label=childMsg />
      <table id="child-table" class="node grid">
         <tr>
            <th>${msg("nodebrowser.name")}</th>
            <th>${msg("nodebrowser.child-node")}</th>
            <th>${msg("nodebrowser.primary")}</th>
            <th>${msg("nodebrowser.association-type")}</th>
            <th>${msg("nodebrowser.index")}</th>
            <th>${msg("nodebrowser.actions")}</th>
         </tr>
         <#list result.children as n>
         <tr>
            <td><a href="#" onclick="AdminConsole_childClick('${n.childRef}');return false;">${n.QName}</a></td>
            <td><a href="#" onclick="AdminConsole_childClick('${n.childRef}');return false;">${n.childRef}</a></td>
            <td>${n.primary?string}</td>
            <td>${n.typeQName}</td>
            <td>${n_index}</td>
            <#assign isarchive=(args.store!"")?starts_with("archive://")>
            <td>
               <b><a href="#" onclick="AdminConsole_execute('${n.childRef}','delete');return false;" title="${msg("nodebrowser.delete.tip")}">${msg("nodebrowser.delete")}</a></b>
               <#if isarchive>| <b><a href="#" onclick="AdminConsole_execute('${n.childRef}','restore');return false;" title="${msg("nodebrowser.restore.tip")}">${msg("nodebrowser.restore")}</a></b>
               <#else>| <b><a href="#" onclick="AdminConsole_confirmExecute('${n.childRef}','fdelete');return false;" title="${msg("nodebrowser.force-delete.tip")}">${msg("nodebrowser.force-delete")}</a></b>
               | <b><a href="#" onclick="AdminConsole_execute('${n.childRef}','take-ownership');return false;" title="${msg("nodebrowser.take-ownership.tip")}">${msg("nodebrowser.take-ownership")}</a></b>
               | <b><a href="#" onclick="AdminConsole_execute('${n.childRef}','delete-permissions');return false;" title="${msg("nodebrowser.delete-permissions.tip")}">${msg("nodebrowser.delete-permissions")}</a></b>
               <#if n.childLocked>| <b><a href="#" onclick="AdminConsole_execute('${n.childRef}','unlock');return false;" title="${msg("nodebrowser.unlock.tip")}">${msg("nodebrowser.unlock")}</a></b></#if>
               </#if>
            </td>
         </tr>
         </#list>
      </table>
   </div>
   
   <#if result.parents??>
   <div class="column-full">
      <#assign parentMsg>${msg("nodebrowser.parents")} (<#if result.parents??>${result.parents?size?c}<#else>0</#if>)</#assign>
      <@section label=parentMsg />
      <table id="parents-table" class="node grid">
         <tr>
            <th>${msg("nodebrowser.name")}</th>
            <th>${msg("nodebrowser.parent-type")}</th>
            <th>${msg("nodebrowser.parent-reference")}</th>
            <th>${msg("nodebrowser.primary")}</th>
            <th>${msg("nodebrowser.association-type")}</th>
         </tr>
         <#list result.parents as p>
         <tr>
            <td>${p.name.prefixedName}</td>
            <td>${p.parentTypeName.prefixedName}</td>
            <td><a href="#" onclick="AdminConsole_parentClick('${p.parentRef}');return false;">${p.parentRef}</a></td></td>
            <td>${p.primary?string}</td>
            <td><#if p.typeName??>${p.typeName.prefixedName}<#else>${none}</#if></td>
         </tr>
         </#list>
      </table>
   </div>
   </#if>
   
   <#if result.assocs??>
   <div class="column-full">
      <#assign assocsMsg>${msg("nodebrowser.associations")} (<#if result.assocs??>${result.assocs?size?c}<#else>0</#if>)</#assign>
      <@section label=assocsMsg />
      <table id="assocs-table" class="node grid">
         <tr>
            <th>${msg("nodebrowser.type")}</th>
            <th>${msg("nodebrowser.target-reference")}</th>
            <th>${msg("nodebrowser.association-type")}</th>
         </tr>
         <#list result.assocs as a>
         <tr>
            <td>${a.targetTypeName.prefixedName}</td>
            <td><a href="#" onclick="AdminConsole_parentClick('${a.targetRef}');return false;">${a.targetRef}</a></td></td>
            <td><#if a.typeName??>${a.typeName.prefixedName}<#else>${none}</#if></td>
         </tr>
         </#list>
      </table>
   </div>
   </#if>
   
   <#if result.sourceAssocs??>
   <div class="column-full">
      <#assign sassocsMsg>${msg("nodebrowser.source-assocs")} (<#if result.sourceAssocs??>${result.sourceAssocs?size?c}<#else>0</#if>)</#assign>
      <@section label=sassocsMsg />
      <table id="sourceassocs-table" class="node grid">
         <tr>
            <th>${msg("nodebrowser.type")}</th>
            <th>${msg("nodebrowser.source-reference")}</th>
            <th>${msg("nodebrowser.association-type")}</th>
         </tr>
         <#list result.sourceAssocs as a>
         <tr>
            <td>${a.sourceTypeName.prefixedName}</td>
            <td><a href="#" onclick="AdminConsole_parentClick('${a.sourceRef}');return false;">${a.sourceRef}</a></td></td>
            <td><#if a.typeName??>${a.typeName.prefixedName}<#else>${none}</#if></td>
         </tr>
         </#list>
      </table>
   </div>
   </#if>
   
   <#if result.permissions??>
   <div class="column-full">
      <@section label=msg("nodebrowser.permissions") />
      <table id="perminfo-table" class="node">
         <tr><td>${msg("nodebrowser.inherits")}: ${result.permissions.inherit?string}</td></tr>
         <tr><td>${msg("nodebrowser.owner")}: ${result.permissions.owner!""}</td></tr>
      </table>
      <table id="permissions-table" class="node grid">
         <tr>
            <th>${msg("nodebrowser.permission")}</th>
            <th>${msg("nodebrowser.authority")}</th>
            <th>${msg("nodebrowser.access")}</th>
         </tr>
         <#list result.permissions.entries as p>
         <tr>
            <td>${p.permission}</td>
            <td>${p.authority}</td>
            <td>${p.accessStatus}</td>
         </tr>
         </#list>
      </table>
   </div>
   </#if>
   
   </#if>
   
   <#if args.in??><br><p>Processed in ${args.in?html}ms</p></#if>
   
   <script type="text/javascript">//<![CDATA[

/* Page load handler */
Admin.addEventListener(window, 'load', function() {
   // bind Enter key press to call the Execute search button event handler
   Admin.addEventListener(el("query"), 'keypress', function(e) {
      if (e.keyCode === 13) AdminConsole_action('search');
      return true;
   });
   
   <#if args.nodeRef??>
      AdminConsole_action('search');
   </#if>
});

function AdminConsole_action(action)
{
   el("action").value = action;
   el("${FORM_ID}").submit();
   return false;
}

function AdminConsole_childClick(ref)
{
   el("action-value").value = ref;
   AdminConsole_action("children");
}

function AdminConsole_parentClick(ref)
{
   el("action-value").value = ref;
   AdminConsole_action("parent");
}

function AdminConsole_execute(value, execute)
{
   el("execute-value").value = value;
   el("execute").value = execute;
   AdminConsole_action('${(action!"")?html}');
}

function AdminConsole_confirmExecute(value, execute)
{
   if (confirm("${msg("nodebrowser.confirm")}"))
   {
      AdminConsole_execute(value, execute);
   }
}

//]]></script>

</@page>