dojo.provide("dojo.experimental");

/**
 * Convenience for informing of experimental code.
 */
dojo.experimental = function(packageName, extra){
	var mess = "EXPERIMENTAL: " + packageName;
	mess += " -- Not yet ready for use.  APIs subject to change without notice.";
	if(extra){ mess += " " + extra; }
	dojo.debug(mess);
}
