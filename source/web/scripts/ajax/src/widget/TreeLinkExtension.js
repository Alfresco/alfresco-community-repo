
dojo.provide("dojo.widget.TreeLinkExtension");

dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.widget.TreeExtension");

dojo.widget.tags.addParseTreeHandler("dojo:TreeLinkExtension");


dojo.widget.TreeLinkExtension = function() {
	dojo.widget.TreeExtension.call(this);
	
	this.params = {}
}

dojo.inherits(dojo.widget.TreeLinkExtension, dojo.widget.TreeExtension);


/**
 * can only listen, no unlisten
 */
dojo.lang.extend(dojo.widget.TreeLinkExtension, {
	widgetType: "TreeLinkExtension",
	
	listenTreeEvents: ["afterChangeTree"],
	

	listenTree: function(tree) {
		
		dojo.widget.TreeCommon.prototype.listenTree.call(this,tree);
		
		var labelNode = tree.labelNodeTemplate;
		var newLabel = this.makeALabel();
		dojo.html.setClass(newLabel, dojo.html.getClass(labelNode));
		labelNode.parentNode.replaceChild(newLabel, labelNode);		
	},
	
		
	
	makeALabel: function() {		
		var newLabel = document.createElement("a");
		
		for(key in this.params) {
			if (key in {}) continue;
			newLabel.setAttribute(key, this.params[key]);
		}
		
		return newLabel;
	},
		
	
	onAfterChangeTree: function(message) {
		var _this = this;
		
		
		// only for new nodes
		if (!message.oldTree) {
			this.listenNode(message.node);
		}
		
	},
	
	listenNode: function(node) {
		for(key in node.object) {
			if (key in {}) continue;
			node.labelNode.setAttribute(key, node.object[key]);
		}
	}
	


});
