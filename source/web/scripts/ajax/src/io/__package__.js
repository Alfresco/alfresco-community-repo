dojo.kwCompoundRequire({
	common: ["dojo.io"],
	rhino: ["dojo.io.RhinoIO"],
	browser: ["dojo.io.BrowserIO", "dojo.io.cookie"],
	dashboard: ["dojo.io.BrowserIO", "dojo.io.cookie"]
});
dojo.provide("dojo.io.*");
