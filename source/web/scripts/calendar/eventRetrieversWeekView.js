var _currentDateForWeekView = new Date();

function setCurrentDateForWeekView(change)
{
	if (isNaN(change))      change = 0;
	if (change == 0)        _currentDateForWeekView = new Date();
	_currentDateForWeekView.setDate(_currentDateForWeekView.getDate() + change);
}
	
var handleSuccessWeekView = function(o)
{
	var response = o.responseText;
	var _divWeekView = document.getElementById("divWeekView");
	if (_divWeekView != null)
		_divWeekView.innerHTML = response;
	//document.getElementById("spnCurrentDisplayWeekWeekView").innerHTML = response[1];
}

var handleFailureWeekView = function(o)
{
	alert("Unable to retrieve, " + o.statusText);
}

var callbackWeekView =
{
	success:handleSuccessWeekView,
	failure:handleFailureWeekView,
	argument: { foo:"foo", bar:"bar" }
};

function callEventRetrieverWeekView()
{
	var month = _currentDateForWeekView.getMonth() + 1;

	var sUrl = getContextPath() + '/wcservice/calendar/RetrieveWeekEvents?';
	var postData = "s=" + spaceRef + "&d=" + _currentDateForWeekView .getFullYear()  + "/" + month + "/" + _currentDateForWeekView .getDate(); 
	sUrl += postData;
	var request = YAHOO.util.Connect.asyncRequest('GET', sUrl, callbackWeekView, null);
}


function callLoadersWeekView(change)
{
	if (isNaN(change))      change = 0;
	setCurrentDateForWeekView(change);
	callEventRetrieverWeekView();
}

YAHOO.util.Event.addListener(window, "load", callLoadersWeekView);
