dojo.provide("dojo.i18n.common");

/**
 * Gets a reference to a hash containing the localization for a given bundle in a package, matching the specified
 * locale.  Bundle must have already been loaded by dojo.requireLocalization() or by a build optimization step.
 *
 * @param modulename package in which the bundle is found
 * @param bundlename the filename in the directory structure without the ".js" suffix
 * @param locale the variant to load (optional).  By default, the locale defined by the
 *   host environment: dojo.locale
 * @return a hash containing name/value pairs.  Throws an exception if the bundle is not found.
 */
dojo.i18n.getLocalization = function(modulename, bundlename, locale /*optional*/){
	locale = dojo.normalizeLocale(locale);

	// look for nearest locale match
	var elements = locale.split('-');
	var module = [modulename,"_nls",bundlename].join('.');
	var bundle = dojo.hostenv.findModule(module, true);

	for(var i = elements.length; i > 0; i--){
		var loc = elements.slice(0, i).join('-');
		if(bundle[loc]){
			return bundle[loc];
		}
	}
	if(bundle.ROOT){
		return bundle.ROOT;
	}

	dojo.raise("Bundle not found: " + bundlename + " in " + modulename+" , locale=" + locale);
};

/**
 * Is the language read left-to-right?  Most exceptions are for middle eastern languages.
 *
 * @param locale a string representing the locale.  By default, the locale defined by the
 *   host environment: dojo.locale
 * @return true if language is read left to right; false otherwise
 */
dojo.i18n.isLTR = function(locale /*optional*/){
	var lang = dojo.normalizeLocale(locale).split('-')[0];
	var RTL = {ar:true,fa:true,he:true,ur:true,yi:true};
	return !RTL[lang];
};
