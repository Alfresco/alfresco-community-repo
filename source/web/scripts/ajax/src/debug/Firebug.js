dojo.provide("dojo.debug.Firebug");

if (dojo.render.html.moz) {
	if (console && console.log) {
		dojo.hostenv.println=console.log;
	} else {
		dojo.debug("dojo.debug.Firebug requires Firebug > 0.4");
	}
}
