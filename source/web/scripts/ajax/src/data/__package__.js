dojo.require("dojo.experimental");

dojo.experimental("dojo.data.*");
dojo.kwCompoundRequire({
	common: [
		"dojo.data.Item",
		"dojo.data.ResultSet",
		"dojo.data.provider.FlatFile"
	]
});
dojo.provide("dojo.data.*");

