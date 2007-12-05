var CalendarUtil = {
	DAYTAB: 2,
	setDayView: function(day, tabs) {
		var thedate = new Date(_currentDateForMonthView);
		thedate.setDate(day);

		_currentDateForDayView = thedate; // global refers to the current day
		callEventRetrieverDayView(); // load the events for the given date
		tabs.set('activeIndex', CalendarUtil.DAYTAB);
	}
};
