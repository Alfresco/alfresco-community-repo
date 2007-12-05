//
// TODO: package up in an calendar object
//       instead of having functions floating
//       around in the global namespace.
//
// Author: simon@webteq.eu
//
function applyFilter(query) {
  if (query == "") {
    query = "\S"; // match everything
  }
  var re = new RegExp(query,"i");
  var select = document.getElementById("availcals");
  for (i=0; i<select.length;i++) {
     var display;
     if (!re.test(select.options[i].text)) {
       display = "none";
     } else {
       display = "block";
     }
     select.options[i].style.display = display;
  }
};

var addSuccessHandler = function(o) {
    var result = o.responseText;
    var _div = document.getElementById("subscribedCalendars");
    if (_div != null) {
	_div.innerHTML = result;
    }
    refreshAllViews();
};

var addFailureHandler = function(o) {
     alert("Couldn't get calendar subscriptions");
};

var addCallback = {
    success:addSuccessHandler,
    failure:addFailureHandler,
    argument: { foo:"foo", bar:"bar" }	  	
};

function setupCalendarSubscription() {
    // Determine which option is selected
    var selectList = document.getElementById("availcals");
    var idx = selectList.options.selectedIndex;
    if (idx > -1) {
	addCalendarSubscription(selectList.options[idx].value);
    }
}

function addCalendarSubscription(calendarRef) {
    var url = getContextPath() + "/wcservice/calendar/calendarSubscriptions";
    var params = new Array();
    params[0] = "ref=" + spaceRef;
    params[1] = "calendar=" + calendarRef;	
    var request = YAHOO.util.Connect.asyncRequest("POST", url + "?" + params.join("&"), addCallback, null);
}

//
// Functions for removing subscriptions
//

var removeFailureHandler = function(o) {
     alert(o.responseText);
};

var removeCallback = {
    success:addSuccessHandler,
    failure:removeFailureHandler,
    argument: { foo:"foo", bar:"bar" }	  	
};

function removeCalendarSubscription() {
    // Determine which option is selected
    var selectList = document.getElementById("subscriptions");
    var idx = selectList.options.selectedIndex;
    if (idx > -1) {
	var url = getContextPath() + "/wcservice/calendar/calendarRemove";
    	var params = new Array();
    	params[0] = "ref=" + spaceRef;
    	params[1] = "calendar=" + selectList.options[idx].value;	
    	var request = YAHOO.util.Connect.asyncRequest("POST", url + "?" + params.join("&"), removeCallback, null);
    }
}

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
     params[1] = "color=" + color;
     var url = getContextPath() + "/wcservice/calendar/setColor";
     var colorCallback = {
	success:colorHandler,
	failure:colorFailureHandler,
	argument: { ref:spaceRef, color:color }
     };
     var request = YAHOO.util.Connect.asyncRequest("POST", url, colorCallback, null); 	   
};


//YAHOO.util.Event.addListener("window", "load", retrieveAvailableCalendars);

