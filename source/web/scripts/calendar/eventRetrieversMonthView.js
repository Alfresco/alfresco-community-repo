var _currentDateForMonthView = new Date();

function setCurrentMonthView()
{
    _currentDateForMonthView = new Date();
    callEventRetrieverMonthView();
}

function addMonthsMonthView(n)
{
    _currentDateForMonthView.setMonth(_currentDateForMonthView.getMonth() + n);
    callEventRetrieverMonthView();
}

function addYearsMonthView(n)
{
    _currentDateForMonthView.setFullYear(_currentDateForMonthView.getFullYear() + n);
    callEventRetrieverMonthView();
}

var handleSuccessMonthView = function(o)
{
	var response = o.responseText;
	var _divMonthView = document.getElementById("divMonthView");
	if (_divMonthView != null)
		_divMonthView.innerHTML = response;
}

var handleFailureMonthView = function(o)
{
	alert("Unable to retrieve, " + o.statusText);
}

var callbackMonthView =
{
	success:handleSuccessMonthView,
	failure:handleFailureMonthView,
	argument: { foo:"foo", bar:"bar" }
};

function callEventRetrieverMonthView()
{
	var _arrMonths = MonthsArray();
	var month = _currentDateForMonthView.getMonth() + 1;
    document.getElementById("spnCurrentDisplayMonthMonthView").innerHTML = _arrMonths[month-1] + ", " + _currentDateForMonthView.getFullYear();

	var sUrl = getContextPath() + '/wcservice/calendar/RetrieveMonthEvents?';
	var postData = "s=" + spaceRef + "&d=" + _currentDateForMonthView.getFullYear()  + "/" + month + "/" + _currentDateForMonthView.getDate();
	sUrl += postData;
	var request = YAHOO.util.Connect.asyncRequest('GET', sUrl, callbackMonthView, null);
}


YAHOO.util.Event.addListener(window, "load", callEventRetrieverMonthView);
