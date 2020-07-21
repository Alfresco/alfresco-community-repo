<select id="subscriptions" style="width:300px" size="8" multiple="multiple">
<#list resultset as node>
<option value="${node.nodeRef}">${node.parent.name}</option>
</#list>
 </select>
  