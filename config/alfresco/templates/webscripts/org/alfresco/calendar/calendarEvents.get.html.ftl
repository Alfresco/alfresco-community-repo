<script type="text/javascript" src="/alfresco/yui/build/yahoo/yahoo.js"></script> 
<script type="text/javascript" src="/alfresco/yui/build/event/event.js" ></script> 
<script type="text/javascript" src="/alfresco/yui/build/dom/dom.js" ></script> 	 
<script type="text/javascript" src="/alfresco/yui/build/calendar/calendar.js"></script> 
<link type="text/css" rel="stylesheet" href="/alfresco/yui/build/calendar/assets/calendar.css"> 

<script type="text/javascript">
YAHOO.namespace("example.calendar"); 
YAHOO.example.calendar.init = function() {
  YAHOO.example.calendar.cal1 = new YAHOO.widget.Calendar("cal1","cal1Container", { pagedate:"${month}/${year}" } );
<#list dates as d>
  YAHOO.example.calendar.cal1.addRenderer("${d}", YAHOO.example.calendar.cal1.renderCellStyleSelected);
</#list>
  YAHOO.example.calendar.cal1.render(); 
 } 	 

YAHOO.util.Event.onDOMReady(YAHOO.example.calendar.init); 
</script>

<div id="cal1Container"></div> 
