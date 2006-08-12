dojo.provide("dojo.data.Type");
dojo.require("dojo.data.Item");

// -------------------------------------------------------------------
// Constructor
// -------------------------------------------------------------------
dojo.data.Type = function(/* dojo.data.provider.Base */ dataProvider) {
	/**
	 * summary:
	 * A Type represents a type of value, like Text, Number, Picture,
	 * or Varchar.
	 */
	dojo.data.Item.call(this, dataProvider);
};
dojo.inherits(dojo.data.Type, dojo.data.Item);
