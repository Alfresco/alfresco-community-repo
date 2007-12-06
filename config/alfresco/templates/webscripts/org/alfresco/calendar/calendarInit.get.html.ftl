<div>
<table>
<tr>
<td>
<div id="color2" style="height: 30px; width: 30px; background: ${color}; border: 1px solid black;">&nbsp;</div>
</td>
<td>
<A HREF="#" onClick="picker.show('pick2');return false;" NAME="pick2" ID="pick2">Select a color</A>
</td>
</tr>
</table>
</div>
<#-- This MUST be present -->
<DIV ID="colorPickerDiv" STYLE=\"position: absolute; left: 0px; top: 0px; z-index: 1; display:none;"> </DIV>

<table>
<tr>
<td>Available Calendars</td>
<td>&nbsp;</td>
<td>Subscribed Calendars</td>
<tr>
<td>
<table>
<tr>
<td id="availableCalendars">
<#-- Display the list of available calendars-->
<select id="availcals" style="width:300px" size="8">
<#list available as node>
<option value="${node.nodeRef}">${node.parent.name}</option>
</#list>
 </select>
</td>
</tr>
</table>
<td>
<!-- middle column -->
<input class="alignMiddle" type="button" onclick="setupCalendarSubscription()" value="&gt;&gt;"/>
</td>
<td id="subscribedCalendars">
<#-- Display the list of subscribed calendars-->
<select id="subscriptions" style="width:300px" size="8">
<#list subscriptions as node>
<option value="${node.nodeRef}">${node.parent.name}</option>
</#list>
 </select>
</td>
</tr>
<tr>
<td><input type="text" id="filter" value="" size="25"/><input type="button" onclick="applyFilter(document.getElementById('filter').value);" value="Filter"/></td>
<td>&nbsp;</td>
<td><input type="button" onclick="removeCalendarSubscription();" value="Remove"/></td>
</tr>
</table>
  