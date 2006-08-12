dojo.provide("dojo.i18n.calendar.GregorianNames");

dojo.require("dojo.i18n.common");
dojo.requireLocalization("dojo.i18n.calendar", "gregorian");

dojo.i18n.calendar.GregorianNames.getNames = function(item, type, use, locale){
// item = 'months' || 'days'
// type = 'wide' || 'narrow' || 'abbr'
// use = 'standAlone' || 'format' (default)
// locale (optional)
// returns an array
	var label;
	var lookup = dojo.i18n.getLocalization("dojo.i18n.calendar", "gregorian", locale);
	var props = [item, use, type];
	if (use == 'standAlone') {
		label = lookup[props.join('-')];
	}
	props[1] = 'format';
	return label || lookup[props.join('-')];	
};
