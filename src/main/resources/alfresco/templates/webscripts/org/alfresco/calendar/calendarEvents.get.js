function getGUIDFromNodeRef(nodeRef) {
	var str = "" + nodeRef;
	if (str.length > 24) {
		return str.substring(24);
	} else {
		return "";
	}
}

function findNodeByNodeRef(nodeRef) {
	var resultsArray = search.luceneSearch("ID:workspace\\://SpacesStore/" + getGUIDFromNodeRef(nodeRef));
	if (resultsArray != null && resultsArray.length > 0) {
		return resultsArray[0];
	} else {
		return null;
	}
}

var spaceRef = args.ref;
var calendar = findNodeByNodeRef(spaceRef);

function formatANSI(datestr) {
  var tmp = datestr.split("/");
  var d = new Date();
  d.setFullYear(tmp[0]);
  d.setMonth(tmp[1]-1);
  d.setDate(tmp[2]);
  return d;
}

function Interval(start, end) {
  this.start = start;
  this.end = end;
};

Interval.prototype.overlaps = function(interval) {
  // take the interval with the early start time as the one to compare against
  var x, y;
  if (this.start.getTime() < interval.start.getTime()) {
         x = this; y = interval;
  } else {
         x = interval; y = this;
  } 
  var time = y.start.getTime();
  return (x.start.getTime() <= time) && (time < this.end.getTime());
};

function isLeap(year) {
  return (year % 4 == 0) && ((year % 100 != 0) || (year % 400 == 0));
}

var monthToDays = [31,28,31,30,31,30,31,31,30,31,30,31];

if (isLeap(args.year)) {
  monthToDays[1] = 29; // one extra day in February
}

var fromDate = args.year + "/0" + args.month + "/01";
var from = formatANSI(fromDate);
var toDate = args.year + "/0" + args.month + "/" + monthToDays[args.month - 1];
var to = formatANSI(toDate);
var time = new Interval(from, to);

var selectedDates = new Array();
var events = calendar.children;

for(var i=0; i < events.length; i++) {
  var event = events[i];
  var startdate = new Date(event.properties["ia:fromDate"]);
  var enddate = new Date(event.properties["ia:toDate"]);
  var interval = new Interval(startdate, enddate);
  if (interval.overlaps(time)) {
      var key = (interval.start.getMonth()+1) + "/" + interval.start.getDate();
      if (selectedDates.indexOf(key) < 0) {
        selectedDates.push(key);
      }
  }
}

model.dates = selectedDates;

// These are only used for the html view
model.month = args.month;
model.year = args.year;



