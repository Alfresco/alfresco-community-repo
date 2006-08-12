dojo.provide("dojo.i18n.datetime");

dojo.require("dojo.experimental");
dojo.experimental("dojo.i18n.datetime");

dojo.require("dojo.string.common");
dojo.require("dojo.i18n.common");
dojo.requireLocalization("dojo.i18n.calendar", "gregorian");

// Everything here assumes Gregorian calendars.  Other calendars will be implemented in separate modules.

//Q: Do we need to support passing in custom formats?  maybe we can just use dojo.date.strftime for that purpose?
//Q: Do we have to handle dates and times combined, or can we leave that to the caller?
//Q: How do we pass in just a time to format?
//Q: What's the right way to define whether we're formatting/parsing dates vs times? separate methods? option arg?


//Q: how do we resolve format vs strftime? reuse?

/**
* Method to Format and validate a given Date object
*
* @param Date value
*	The Date object to be formatted and validated.
* @param String formatLength choice of long, short, medium or full
* @param String locale the locale to determine formatting used.  By default, the locale defined by the
*   host environment: dojo.locale
* @return String
* 	the formatted date of type String if successful; ?? if an
* 	invalid currency is provided or null if an unsupported locale value was provided.
**/
dojo.i18n.datetime.format = function(value, formatLength, options, locale /*optional*/){
	locale = dojo.normalizeLocale(locale);
	var info = dojo.i18n.getLocalization("dojo.i18n.calendar", "gregorian", locale);
	var pattern = info["dateFormat-"+formatLength];

	function formatPattern(value, pattern) {
		return pattern.replace(/[a-zA-Z]+/g, function(match){
			var s;
			var c = match.charAt(0);
			var l = match.length;
			var pad;
			switch(c){
				case 'G':
					if(l>3){dojo.unimplemented("Era format not implemented");}
					s = info.eras[value.getFullYear() < 0 ? 1 : 0];
					break;
				case 'y':
					s = value.getFullYear();
					switch(l){
						case 1:
							break;
						case 2:
							s = String(s).substr(-2);
							break;
						default:
							pad = true;
					}
					break;
				case 'Q':
				case 'q':
					var s = Math.ceil((value.getMonth()+1)/3);
					switch(l){
						case 1: case 2:
							pad = true;
							break;
						case 3:
						case 4:
							dojo.unimplemented("Quarter format not implemented");
					}
					break;
				case 'M':
				case 'L':
					var m = value.getMonth();
					var width;
					switch(l){
						case 1: case 2:
							s = m+1; pad = true;
							break;
						case 3:
							width = "abbr";
							break;							
						case 4:
							width = "wide";
							break;
						case 5:
							width = "narrow";
							break;
					}
					if(width){
						var type = (c == "L") ? "standalone" : "format";
						var prop = ["months",type,width].join("-");
						s = info[prop][m];
					}
					break;
				case 'd':
					s = value.getDate(); pad = true;
					break;
				case 'E':
				case 'e':
				case 'c':
					var d = value.getDay();
					var width;
					switch(l){
						case 1: case 2:
							if(c != 'E'){
								s = d+1; pad = true; //TODO: depends on starting day of week
								break;
							}
							//else fallthrough
						case 3:
							width = "abbr";
							break;							
						case 4:
							width = "wide";
							break;
						case 5:
							width = "narrow";
							break;
					}
					if(width){
						var type = (c == "c") ? "standalone" : "format";
						var prop = ["days",type,width].join("-");
						s = info[prop][d];
					}
					break;
				case 'a':
					var timePeriod = (value.getHours() < 12) ? 'am' : 'pm';
					s = info[timePeriod];
					break;
				case 'h':
				case 'H':
				case 'K':
				case 'k':
					var h = value.getHours();
					if((h>11)&&(c=='h' || c=='K')){h-=12;}
					if(c=='h' || c=='k'){h++;}
					s = h; pad = true;
					break;
				case 'm':
					s = value.getMinutes(); pad = true;
					break;
				case 's':
					s = value.getSeconds(); pad = true;
					break;
				case 'S':
					s = Math.round(value.getMilliseconds() * Math.pow(10, l));
					break;
				case 'Z':
					var tz = value.getTimezoneOffset().split('.');
					var sign = "-"; //TODO: how do you derive the sign? tz is positive for EDT?
					tz.splice(0, 0, sign);
					tz[1] = dojo.string.pad(Math.abs(tz[0]), 2);
					tz[2] = dojo.string.pad((tz[1] || 0), 2, 0, -1);
					if(l==4){
						tz.splice(0, 0, "GMT");
						tz.splice(3, 0, ":");
					}
					s = s.join("");
					break;
				case 'Y':
				case 'u':
				case 'w':
				case 'W':
				case 'D':
				case 'F':
				case 'g':
				case 'A':
				case 'z':
				case 'v':
					dojo.unimplemented("date format not implemented, pattern="+match);
					s = "?";
					break;
				default:
					dojo.raise("invalid format: "+pattern);
			}
			if(pad){ s = dojo.string.pad(s, l); }
			return s;
		});
	}

	// Break up on single quotes, treat every other one as a literal, except '' which becomes '
	var chunks = pattern.split('\'');
	var format = true;
	for (var i=0; i<chunks.length; i++){
		if(!chunks[i]){chunks[i]='\'';}
		else{
			if(format){chunks[i]=formatPattern(value, chunks[i]);}
			format = !format;
		}
	}
	return chunks.join("");
};

/**
* Method to convert a properly formatted date to a primative Date object.
*
* @param String value
*	The int string to be convertted
* @param String formatLength choice of long, short, medium or full
* @param String locale the locale to determine formatting used.  By default, the locale defined by the
*   host environment: dojo.locale
* @return Date
* 	Returns a primative Date object, ?? if unable to convert to a number, or null if an unsupported locale is provided.
**/
dojo.i18n.datetime.parse = function(value, formatLength, locale /*optional*/){
	locale = dojo.normalizeLocale(locale);
	dojo.unimplemented("dojo.i18n.datetime.parse");
};

/**
  Validates whether a string represents a valid date. 

  @param value  A string
  @param formatLength choice of long, short, medium or full
  @param locale the locale to determine formatting used.  By default, the locale defined by the
    host environment: dojo.locale
  @return true or false.
*/
dojo.i18n.datetime.isDate = function(value, formatLength, locale /*optional*/){
	locale = dojo.normalizeLocale(locale);
	dojo.unimplemented("dojo.i18n.datetime.isDate");
};

/**
  Validates whether a string represents a valid time. 

  @param value  A string
  @param formatLength choice of long, short, medium or full
  @param locale the locale to determine formatting used.  By default, the locale defined by the
    host environment: dojo.locale
  @return true or false.
*/
dojo.i18n.datetime.isTime = function(value, formatLength, locale /*optional*/){
	locale = dojo.normalizeLocale(locale);
	dojo.unimplemented("dojo.i18n.datetime.isTime");
};

/**
  Validates whether a string represents a valid date and time. 

  @param value  A string
  @param formatLength choice of long, short, medium or full
  @param locale the locale to determine formatting used.  By default, the locale defined by the
    host environment: dojo.locale
  @return true or false.
*/
dojo.i18n.datetime.isDateTime = function(value, formatLength, locale /*optional*/){
	locale = dojo.normalizeLocale(locale);
	dojo.unimplemented("dojo.i18n.datetime.isDateTime");
};
