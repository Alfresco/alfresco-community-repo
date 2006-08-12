
dojo.provide("dojo.widget.TreeExtension");

dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.widget.TreeCommon");



// abstract class
dojo.widget.TreeExtension = function() {
	dojo.widget.HtmlWidget.call(this);
	
	this.listenedTrees = {}
}

dojo.inherits(dojo.widget.TreeExtension, dojo.widget.HtmlWidget);


dojo.lang.extend(dojo.widget.TreeExtension, dojo.widget.TreeCommon.prototype);

