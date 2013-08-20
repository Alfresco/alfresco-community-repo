<!-- Calendar Code v2.1 -->

<link rel="stylesheet" type="text/css" href="${url.context}/yui/example.css">
<link rel="stylesheet" type="text/css" href="${url.context}/yui/assets/tabview.css">
<link rel="stylesheet" type="text/css" href="${url.context}/yui/assets/round_tabs.css">
<link type="text/css" rel="stylesheet" href="${url.context}/yui/fonts.css"/>
<link type="text/css" rel="stylesheet" href="${url.context}/yui/assets/calendar.css"/>
<link type="text/css" rel="stylesheet" href="${url.context}/yui/Custom.css"/>

<script type="text/javascript" src="${url.context}/yui/yahoo.js"></script>
<script type="text/javascript" src="${url.context}/yui/event.js"></script>
<script type="text/javascript" src="${url.context}/yui/dom.js"></script>
<script type="text/javascript" src="${url.context}/yui/element-beta.js"></script>
<script type="text/javascript" src="${url.context}/yui/tabview.js"></script>
<script type="text/javascript" src="${url.context}/yui/yahoo-min.js" ></script>
<script type="text/javascript" src="${url.context}/yui/event-min.js" ></script>
<script type="text/javascript" src="${url.context}/yui/connection-min.js" ></script>
<script type="text/javascript" src="${url.context}/yui/calendar.js"></script>

<script type="text/javascript">
var tabView;
var spaceRef = '${args["nodeRef"]}';

function showColorWindow(txtColor)
{
	win2 = window.open("${url.context}/images/calendar/color_picker.htm", "colorwind", "height=160,width=200,status=0");
	win2.colorControl = txtColor;
}
</script>

<script type="text/javascript" src="${url.context}/scripts/calendar/calendarScripts.js" ></script>
<script type="text/javascript" src="${url.context}/scripts/calendar/initializeEventDetails.js" ></script>
<script type="text/javascript" src="${url.context}/scripts/calendar/saveEventDetails.js" ></script>
<script type="text/javascript" src="${url.context}/scripts/calendar/eventRetrieversMonthView.js" ></script>
<script type="text/javascript" src="${url.context}/scripts/calendar/eventRetrieversWeekView.js" ></script>
<script type="text/javascript" src="${url.context}/scripts/calendar/captureWeekViewEvents.js" ></script>
<script type="text/javascript" src="${url.context}/scripts/calendar/eventRetrieversDayView.js" ></script>
<script type="text/javascript" src="${url.context}/scripts/calendar/captureDayViewEvents.js" ></script>
<script type="text/javascript" src="${url.context}/scripts/calendar/eventCaptureHandlers.js" ></script>
<script type="text/javascript" src="${url.context}/scripts/calendar/editEventHandlers.js" ></script>
<script type="text/javascript" src="${url.context}/scripts/calendar/calendarSearch.js" ></script>
<script type="text/javascript" src="${url.context}/scripts/calendar/calendarUtil.js" ></script>

<script type="text/javascript" src="${url.context}/scripts/calendar/ColorPicker2.js" ></script>

<link type="text/css" rel="stylesheet" href="${url.context}/css/calendar/calendarMain.css"/>

<style type="text/css">
	#demo .yui-content { padding:1em; } /* pad content container */
</style>


<div id="divCalendarMainContainer" class="yui-navset">
	<ul class="yui-nav">
		<li class="selected" id="lnkShowMonthEvents"><a href="#showMonthEvents"><em>Month</em></a></li>
		<li id="lnkShowWeekEvents"><a href="#showWeekEvents"><em>Week</em></a></li>
		<li id="lnkShowDayEvents"><a href="#showDayEvents"><em>Day</em></a></li>
		<li id="lnkEventCapture"><a href="#eventCapture"><em>Create New Event</em></a></li>
                <li id="lnkCalendarSubscribe"><a href="#calendarSubscribe"><em>Calendar Admin</em></a></li>
	</ul>
	<div class="yui-content">

		<!-- Start Show and Capture Month View Events -->
		<div id="showMonthEvents">
			<BR>
			<div style="text-align:center">
				<img src="${url.context}/yui/img/prev.gif" title="Previous Year" onclick="addYearsMonthView(-1)" style="cursor:pointer" />
				<img src="${url.context}/yui/img/prevMinor.gif" title="Previous Month" onclick="addMonthsMonthView(-1)" style="cursor:pointer" />
				<img src="${url.context}/yui/img/now.gif" title="This Month" onclick="setCurrentMonthView(event)" style="cursor:pointer" />
				<img src="${url.context}/yui/img/nextMinor.gif" title="Next Month" onclick="addMonthsMonthView(1)"  style="cursor:pointer"/>
				<img src="${url.context}/yui/img/next.gif" title="Next Year" onclick="addYearsMonthView(1)" style="cursor:pointer" />
				<span id="spnCurrentDisplayMonth"></span>
			</div>
			
			<table id="tabMonthViewMain" border="0" cellpadding="2" cellspacing="2" width="100%">
				<tr>
					<th>
						<span id="spnCurrentDisplayMonthMonthView"></span>
					</th>
				</tr>
				<tr>
					<td>
						<div id="divMonthView"></div>
					</td>
				</tr>
			</table>
			
		</div>
		<!-- Start Show and Capture Month View Events -->

		<!-- Start Show and Capture Week View Events -->
		<div id="showWeekEvents">
			<BR>
			<div style="text-align:center">
				<img src="${url.context}/yui/img/prevMinor.gif" title="Previous Week" onclick="callLoadersWeekView(-7)" style="cursor:pointer" />
				<img src="${url.context}/yui/img/now.gif" title="This Week" onclick="callLoadersWeekView(0)" style="cursor:pointer" />
				<img src="${url.context}/yui/img/nextMinor.gif" title="Next Week" onclick="callLoadersWeekView(7)" style="cursor:pointer" />
				<span id="spnCurrentDisplayWeek"></span>
			</div>
			<table id="tabWeekViewMain" border="0" cellpadding="2" cellspacing="2" width="100%">
				<tr>
					<th>
						<span id="spnCurrentDisplayWeekWeekView"></span>
					</th>
				</tr>
				<tr>
					<td>
						<div id="divWeekView">
						</div>
					</td>
				</tr>
			</table>
		</div>
		<!-- End Show and Capture Week View Events -->

		<!-- Start Show and Capture Day View Events -->
		<div id="showDayEvents">
			<BR>
			<div style="text-align:center">
				<img src="${url.context}/yui/img/prevMinor.gif" title="Previous Day" onclick="callLoadersDayView(-1)" style="cursor:pointer" />
				<img src="${url.context}/yui/img/now.gif" title="Today" onclick="callLoadersDayView(0)" style="cursor:pointer" />
				<img src="${url.context}/yui/img/nextMinor.gif" title="Next Day" onclick="callLoadersDayView(1)" style="cursor:pointer" />
				<span id="spnCurrentDisplayDay"></span>
			</div>
			<table id="tabDayViewMain" border="0" cellpadding="2" cellspacing="2" width="100%">
				<tr>
					<th>
						<span id="spnCurrentDisplayDayDayView"></span>
					</th>
				</tr>
				<tr>
					<td>
						<div id="divDayView">
						</div>
					</td>
				</tr>
			</table>
		</div>
		<!-- End Show and Capture Day View Events -->

		<!-- Start Create/Edit Events -->
		<div id="eventCapture">
			<BR>
			<div id="cal1Container" style="display:none; position:absolute"></div>
			<input type="hidden" id="hidFromDate"/>
			<input type="hidden" id="hidToDate"/>
			<table id="tabEventCapture" border="0" cellpadding="4" cellspacing="1" width="100%">
				<thead>
					<tr>
						<th colspan="2">Enter Event Information<span id="spnEventCapture"></span></th>
					</tr>
				</thead>
				<tr class='alternateRow'>
					<td>What *</td>
					<td><input type="text" id="txtWhatEvent" /></td>
				</tr>
				<tr>
					<td>When *</td>
					<td>
						<input type="text" id="txtFromDate" readonly="readonly" onfocus="ToggleCalendar(1, this)" />
						<select id="lstFromTime"></select>
						to
						<input type="text" id="txtToDate" readonly="readonly" onfocus="ToggleCalendar(1, this)" />
						<select id="lstToTime"></select>
					</td>
				</tr>
				<tr class='alternateRow'>
					<td>Where</td>
					<td><input type="text" id="txtWhereEvent" /></td>
				</tr>
				<tr>
					<td>Description</td>
					<td><textarea id="txtDescriptionEvent" rows="4" cols="50"></textarea></td>
				</tr>
				<tr style="display: none;">
					<td>Color</td>
					<td><input type="text" size="10" maxlength="7" id="txtColor" readonly="readonly"><input type="button" class="buttonStyle" value="Color picker" onclick="showColorWindow('txtColor')"></td>
				</tr>
				<tr class='alternateRow'>
					<td></td>
					<td><input class="buttonStyle" type="button" id="btnSubmit" value="Submit" onclick="saveCalendarEvent(false)" /><input class="buttonStyle" type="button" id="btnReset" value="New" onclick="resetEventDetails()" /><input class="buttonStyle" type="button" id="btnDelete" value="Delete" onclick="saveCalendarEvent(true)" /></td>
				</tr>
			</table>
		</div>
		<!-- End Create/Edit Events -->
                <!-- Start calendar subscription -->
                <div id="calendarSubscribe">

<script language="JavaScript">

function colorHandler(o) {
	// Success
};

function colorFailureHandler(o) {
	alert("Failed to set color");
};

// Color picker functions
function pickColor(color) {
     document.getElementById("color2").style.background = color;
     // Update the calendar color property
     var params = new Array();
     params[0] = "ref=" + spaceRef;
     params[1] = "color=" + escape(color);
     var url = getContextPath() + "/wcservice/camden/setColor";
     var colorCallback = {
	success:colorHandler,
	failure:colorFailureHandler,
	argument: { foo:"foo", bar:"bar" }
     };
     var request = YAHOO.util.Connect.asyncRequest("POST", url + "?" + params.join("&"), colorCallback, null); 	   
};

var picker = new ColorPicker();
</script>
                 </div> <!-- end of calendar subscription -->
	</div>
</div>
