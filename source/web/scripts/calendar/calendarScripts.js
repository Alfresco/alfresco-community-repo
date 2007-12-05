
var DaysArray = function()
{
    var _arr = new Array();
    _arr[0] = "Sunday";
    _arr[1] = "Monday";
    _arr[2] = "Tuesday";
    _arr[3] = "Wednesday";
    _arr[4] = "Thursday";
    _arr[5] = "Friday";
    _arr[6] = "Saturday";
    
    return _arr;
}

var HoursArray = function()
{
    var _arr = new Array();
    _arr[0] = "00:00";
    _arr[1] = "00:30";
    for (i=1; i<24; i++) {
        _arr[i*2] = i + ":00";
    	_arr[i*2+1] = i + ":30";
    }	
    return _arr;
}

var MonthsArray = function()
{
    var _arr = new Array();
    _arr[0] = "January";
    _arr[1] = "February";
    _arr[2] = "March";
    _arr[3] = "April";
    _arr[4] = "May";
    _arr[5] = "June";
    _arr[6] = "July";
    _arr[7] = "August";
    _arr[8] = "September";
    _arr[9] = "October";
    _arr[10] = "November";
    _arr[11] = "December";
    
    return _arr;
}

function refreshAllViews()
{
	callEventRetrieverWeekView();
	callEventRetrieverDayView();
	callEventRetrieverMonthView();
}

// Removes leading whitespaces
function lTrim( value )
{
	var re = /\s*((\S+\s*)*)/;
	return value.replace(re, "$1");
}

// Removes ending whitespaces
function rTrim( value )
{
	var re = /((\s*\S+)*)\s*/;
	return value.replace(re, "$1");
}

// Removes leading and ending whitespaces
function trim( value )
{
	return lTrim(rTrim(value));
}

String.prototype.pad = function(l, s, t)
{
	return s || (s = " "), (l -= this.length) > 0 ? (s = new Array(Math.ceil(l / s.length)
		+ 1).join(s)).substr(0, t = !t ? l : t == 1 ? 0 : Math.ceil(l / 2))
		+ this + s.substr(0, l - t) : this;
};
