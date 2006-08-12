
dojo.provide("dojo.widget.TreeSelectorV3");

dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.widget.TreeCommon");


dojo.widget.tags.addParseTreeHandler("dojo:TreeSelectorV3");


dojo.widget.TreeSelectorV3 = function() {
	dojo.widget.HtmlWidget.call(this);

	this.eventNames = {};

	this.listenedTrees = {};
	this.selectedNodes = [];
		
}

dojo.inherits(dojo.widget.TreeSelectorV3, dojo.widget.HtmlWidget);

dojo.lang.extend(dojo.widget.TreeSelectorV3, dojo.widget.TreeCommon.prototype);

// TODO: add multiselect
dojo.lang.extend(dojo.widget.TreeSelectorV3, {
	widgetType: "TreeSelectorV3",

	listenTreeEvents: ["afterAddChild","afterCollapse","afterTreeChange", "afterDetach", "beforeTreeDestroy"],
	listenNodeFilter: function(elem) { return elem instanceof dojo.widget.Widget},	
	
	allowedMulti: true,
	
	eventNamesDefault: {
		select : "select",
		destroy : "destroy",
		deselect : "deselect",
		listenNode: "listenNode",
		unlistenNode: "unlistenNode",
		dblselect: "dblselect" // select already selected node.. Edit or whatever
	},

	initialize: function(args) {

		for(name in this.eventNamesDefault) {
			if (dojo.lang.isUndefined(this.eventNames[name])) {
				this.eventNames[name] = this.widgetId+"/"+this.eventNamesDefault[name];
			}
		}
		
		this.onLabelClickHandler =  dojo.lang.hitch(this, this.onLabelClick);
		this.onLabelDblClickHandler =  dojo.lang.hitch(this, this.onLabelDblClick);
		
	},


	listenNode: function(node) {
		//dojo.debug((new Error()).stack)
		//	dojo.debug("listen "+node);
		dojo.event.browser.addListener(node.labelNode, "onclick", this.onLabelClickHandler);
		if (dojo.render.html.ie) {
			dojo.event.browser.addListener(node.labelNode, "ondblclick", this.onLabelDblClickHandler);
		}
		dojo.event.topic.publish(this.eventNames.listenNode, { node: node });
			
	},
	
	unlistenNode: function(node) {
		//dojo.debug("unlisten "+node);
		
		dojo.event.browser.removeListener(node.labelNode, "onclick", this.onLabelClickHandler);
		if (dojo.render.html.ie) {
			dojo.event.browser.removeListener(node.labelNode, "ondblclick", this.onLabelDblClickHandler);
		}
		dojo.event.topic.publish(this.eventNames.unlistenNode, { node: node });
	},


	onAfterAddChild: function(message) {
		//dojo.debug("add child!");
		this.listenNode(message.child);
	},
	

	onBeforeTreeDestroy: function(message) {
		this.unlistenTree(message.source);
	},


	// deselect node if ancestor is collapsed
	onAfterCollapse: function(message) {		
		this.deselectIfAncestorMatch(message.source);		
	},

	// IE will throw select -> dblselect. Need to transform to select->select
	onLabelDblClick: function(event) {
		this.onLabelClick(event);			
	},		
		
	checkSpecialEvent: function(event) {		
		return event.shiftKey || event.ctrlKey;
	},
		
	/**
	 * press on selected with ctrl => deselect it
	 * press on selected w/o ctrl => dblselect it and deselect all other
	 *
	 * press on unselected with ctrl => add it to selection
	 */
	onLabelClick: function(event) {		
		var node = this.domElement2TreeNode(event.target);

		//dojo.debug("click "+node+ "special "+this.checkSpecialEvent(event));
		//dojo.html.setClass(event.target, "TreeLabel TreeNodeEmphased");
		
		if (dojo.lang.inArray(this.selectedNodes, node)) {			
			if(this.checkSpecialEvent(event)){				
				// If the node is currently selected, and they select it again while holding
				// down a meta key, it deselects it
				this.deselect(node);
				return;
			}
			
			var _this = this;
			dojo.lang.forEach(this.selectedNodes, function(selectedNode) {
				if (selectedNode !== node) {
					_this.deselect(selectedNode);
				}
			});
			dojo.event.topic.publish(this.eventNames.dblselect, { node: node });
			return;
		}
		
		// if unselected node..
		
		
		// deselect all if no meta key or disallowed
		if (!this.checkSpecialEvent(event) || !this.allowedMulti) {
			//dojo.debug("deselect All");
			this.deselectAll();
		}
		
		//dojo.debug("select");

		this.select(node);

	},
	

	deselectIfAncestorMatch: function(ancestor) {
		/* deselect all nodes with this ancestor */
		var _this = this;
		dojo.lang.forEach(this.selectedNodes, function(node) {
			var selectedNode = node;
			while (node && node.isTreeNode) {
				if (node === ancestor) {
					_this.deselect(selectedNode); 
					return;					
				}
				node = node.parent;
			}
		});
	},
	
			
	onAfterChangeTree: function(message) {
		
		
		if (!message.newTree || !this.listenedTrees[message.newTree.widgetId]) {
			// moving from our trfee to new one
			
			if (this.selectedNode && message.node.children) {
				this.deselectIfAncestorMatch(message.node);
			}
			
			this.processDescendants(message.node, this.listenNodeFilter, this.unlistenNode);					
			
		}
		if (!message.oldTree || !this.listenedTrees[message.oldTree.widgetId]) {
			//dojo.debugShallow("listen! "+this.listenedTrees);
			// moving from old tree to our tree
			this.processDescendants(message.node, this.listenNodeFilter, this.listenNode);			
		}
		
		
	},


	onAfterDetach: function(message) {
		this.deselectIfAncestorMatch(message.child);		
	},


	select: function(node) {

		var index = dojo.lang.find(this.selectedNodes, node, true);
		
		if (index >=0 ) {
			return; // already selected
		}
		
		//dojo.debug("select "+node);
		this.selectedNodes.push(node);
						
		dojo.event.topic.publish(this.eventNames.select, {node: node} );
	},


	deselect: function(node){
		var index = dojo.lang.find(this.selectedNodes, node, true);
		if (index < 0) {
			//dojo.debug("not selected");
			return; // not selected
		}
		
		//dojo.debug("deselect "+node);
		
		
		this.selectedNodes.splice(index, 1);
		dojo.event.topic.publish(this.eventNames.deselect, {node: node} );
		//dojo.debug("deselect");

	},
	
	deselectAll: function() {
		//dojo.debug("deselect all "+this.selectedNodes);
		while (this.selectedNodes.length) {
			this.deselect(this.selectedNodes[0]);
		}
	}

});



