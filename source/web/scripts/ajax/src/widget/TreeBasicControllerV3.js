
dojo.provide("dojo.widget.TreeBasicControllerV3");

dojo.require("dojo.event.*");
dojo.require("dojo.json")
dojo.require("dojo.io.*");
dojo.require("dojo.widget.TreeCommon");
dojo.require("dojo.widget.TreeNodeV3");
dojo.require("dojo.widget.TreeV3");


dojo.widget.tags.addParseTreeHandler("dojo:TreeBasicControllerV3");


dojo.widget.TreeBasicControllerV3 = function() {
	dojo.widget.HtmlWidget.call(this);
	
	this.listenedTrees = {};
}

dojo.inherits(dojo.widget.TreeBasicControllerV3, dojo.widget.HtmlWidget);


dojo.lang.extend(dojo.widget.TreeBasicControllerV3, dojo.widget.TreeCommon.prototype);

// TODO: do something with addChild / setChild, so that RpcController become able
// to hook on this and report to server
dojo.lang.extend(dojo.widget.TreeBasicControllerV3, {
	widgetType: "TreeBasicControllerV3",

	listenTreeEvents: ["afterChangeTree","afterTreeCreate", "beforeTreeDestroy","afterSetFolder","afterUnsetFolder"],
	listenNodeFilter: function(elem) { return elem.isFolder && elem instanceof dojo.widget.Widget},

	editor: null,

	listenNode: function(node) {
		//dojo.debug("listen "+node);
		dojo.event.browser.addListener(node.expandNode, "onclick", this.onExpandClickHandler);
		//dojo.event.connect(node.expandNode, "onclick", this, "onExpandClick");
	},

	unlistenNode: function(node) {
		dojo.event.browser.removeListener(node.expandNode, "onclick", this.onExpandClickHandler);
		//dojo.event.disconnect(node.expandNode, "onclick", this, "onExpandClick");
	},
	
	
	onAfterSetFolder: function(message) {
		//dojo.debug("setFolder "+message.source)
		this.listenNode(message.source);
	},
	
	
	onAfterUnsetFolder: function(message) {
		this.unlistenNode(message.source);
	},

	initialize: function(args) {
		if (args.editor) {
			this.editor = dojo.widget.byId(args.editor);
			this.editor.controller = this;
		}
		
		this.onExpandClickHandler =  dojo.lang.hitch(this, this.onExpandClick)
	},
		
	
	getInfo: function(elem) {
		return elem.getInfo();
	},

	onAfterChangeTree: function(message) {
		//dojo.debugShallow(message);
		
		
		//dojo.profile.start("onTreeChange");
		
		// we listen/unlisten only if tree changed, not when its assigned first time
		if (!message.oldTree) {
			if (message.node.expandLevel > 0) {
				this.expandToLevel(message.node, message.node.expandLevel);				
			}
			if (message.node.loadLevel > 0) {
				this.loadToLevel(message.node, message.node.loadLevel);				
			}
			
			//dojo.profile.end("onTreeChange");
			return; 
		}
		            
		if (!message.newTree || !this.listenedTrees[message.newTree.widgetId]) {
			// I got this message because node leaves me (oldtree)
			/**
			 * clean all folders that I listen. I don't listen to non-folders.
			 */
			this.processDescendants(message.node, this.listenNodeFilter, this.unlistenNode);
		}		
		
		if (!message.oldTree || !this.listenedTrees[message.oldTree.widgetId]) {
			// we have new node
			this.processDescendants(message.node, this.listenNodeFilter, this.listenNode);
		}
		
		//dojo.profile.end("onTreeChange");
	},
	

	// perform actions-initializers for tree
	onAfterTreeCreate: function(message) {
		var tree = message.source;
		if (tree.expandLevel) {								
			this.expandToLevel(tree, tree.expandLevel)
		}
		if (tree.loadLevel) {
			this.loadToLevel(tree, tree.loadLevel);
		}
	},

	/**
	 * time between expand calls for batch operations
	 * @see expandToLevel
	 */
	batchExpandTimeout: 20,
	
	
	expandAll: function(nodeOrTree) {		
		return this.expandToLevel(nodeOrTree, Number.POSITIVE_INFINITY);
		
	},
	
	
	collapseAll: function(nodeOrTree) {
		var _this = this;
		
		var filter = function(elem) {
			return (elem instanceof dojo.widget.Widget) && elem.isFolder && elem.isExpanded;
		}
		
		if (nodeOrTree.isTreeNode) {		
			this.processDescendants(nodeOrTree, filter, this.collapse);
		} else if (nodeOrTree.isTree) {
			dojo.lang.forEach(nodeOrTree.children,function(c) { _this.processDescendants(c, filter, _this.collapse) });
		}
	},
	
	/**
	 * walk a node in time, forward order, with pauses between expansions
	 */
	expandToLevel: function(nodeOrTree, level) {
		dojo.require("dojo.widget.TreeTimeoutIterator");
		
		var filterFunc = function(elem) {
			var res = elem.isFolder || elem.children && elem.children.length;
			//dojo.debug("Filter "+elem+ " result:"+res);
			return res;
		};
		var callFunc = function(node, iterator) {
			 this.expand(node, true);
			 iterator.forward();
		}
			
		var iterator = new dojo.widget.TreeTimeoutIterator(nodeOrTree, callFunc, this);
		iterator.setFilter(filterFunc);
		
		
		iterator.timeout = this.batchExpandTimeout;
		
		//dojo.debug("here "+nodeOrTree+" level "+level);
		
		iterator.setMaxLevel(nodeOrTree.isTreeNode ? level-1 : level);
		
		
		return iterator.start(nodeOrTree.isTreeNode);
	},
	
	

	getWidgetByNode: function(node) {
		var widgetId;
		while (! (widgetId = node.widgetId) ) {
			node = node.parentNode;
		}
		return dojo.widget.byId(widgetId);
	},

	onExpandClick: function(e){
		//dojo.debugShallow(e)
		var node = this.getWidgetByNode(e.target);
		
		if (node.isExpanded){
			this.collapse(node);
		} else {
			this.expand(node);
		}
	},

	/**
	 * callout activated even if node is expanded already
	 */
	expand: function(node) {
		//dojo.debug("Expand "+node);
		
		if (node.isFolder) {
			node.expand(); // skip trees or non-folders
		}
				
	},

	collapse: function(node) {
		node.collapse();	
	},
	
	
	// -------------------------- TODO: Inline edit node ---------------------
	canEditLabel: function(node) {
		if (node.actionIsDisabledNow(node.actions.EDIT)) return false;

		return true;
	},
	
		
	editLabelStart: function(node) {		
		if (!this.canEditLabel(node)) {
			return false;
		}
		
		if (!this.editor.isClosed()) {
			//dojo.debug("editLabelStart editor open");
			this.editLabelFinish(this.editor.saveOnBlur);			
		}
				
		this.doEditLabelStart(node);
		
	
	},
	
	
	editLabelFinish: function(save) {
		this.doEditLabelFinish(save);		
	},
	
	
	doEditLabelStart: function(node) {
		if (!this.editor) {
			dojo.raise(this.widgetType+": no editor specified");
		}
		
		//dojo.debug("editLabelStart editor open "+node);
		
		this.editor.open(node);
	},
	
	doEditLabelFinish: function(save) {
		if (!this.editor) {
			dojo.raise(this.widgetType+": no editor specified");
		}
		var node = this.editor.node;
		
		this.editor.close(save);
		//dojo.debug("Finish edit "+node+" "+node.isPhantom+" save:"+save);
		
		if (node.isPhantom) {
			if (save) {
				node.isPhantom = false;
			} else {
				node.destroy();
			}
		}
	},
	
	
		
	
	/**
	 * check that something is possible
	 * run maker to do it
	 * run exposer to expose result to visitor immediatelly
	 *   exposer does not affect result
	 */
	runStages: function(check, prepare, make, finalize, expose, args) {
		
		if (check && !check.apply(this, args)) {
			return false;
		}
		
		if (prepare && !prepare.apply(this, args)) {
			return false;
		}
		
		var result = make.apply(this, args);
		
		
		if (finalize) {
			finalize.apply(this,args);			
		}
			
		if (!result) {
			return result;
		}
		
			
		if (expose) {
			expose.apply(this, args);
		}
		
		return result;
	}
});


// create and edit
dojo.lang.extend(dojo.widget.TreeBasicControllerV3, {
		
	createAndEdit: function(parent, index) {
		var data = {title:parent.tree.defaultChildTitle};
		
		if (!this.canCreateChild(parent, index, data)) {
			return false;
		}
		
		var child = this.doCreateChild(parent, index, data);
		if (!child) return false;
		this.exposeCreateChild(parent, index, data);
		
		child.isPhantom = true;
		
		if (!this.editor.isClosed()) {
			//dojo.debug("editLabelStart editor open");
			this.editLabelFinish(this.editor.saveOnBlur);			
		}
		
		
				
		this.doEditLabelStart(child);		
	
	}
	
});


// =============================== clone ============================
dojo.lang.extend(dojo.widget.TreeBasicControllerV3, {
	
	canClone: function(child, newParent, index, deep){
		return true;
	},
	
	
	clone: function(child, newParent, index, deep) {
		return this.runStages(
			this.canClone, this.prepareClone, this.doClone, this.finalizeClone, this.exposeClone, arguments
		);			
	},

	exposeClone: function(child, newParent) {
		if (newParent.isTreeNode) {
			this.expand(newParent);
		}
	},

	doClone: function(child, newParent, index, deep) {
		//dojo.debug("Clone "+child);
		var cloned = child.clone(deep);
		newParent.addChild(cloned, index);
				
		return cloned;
	}
	

});

// =============================== detach ============================

dojo.lang.extend(dojo.widget.TreeBasicControllerV3, {
	canDetach: function(child) {
		if (child.actionIsDisabledNow(child.actions.DETACH)) {
			return false;
		}

		return true;
	},


	detach: function(node) {
		return this.runStages(
			this.canDetach, this.prepareDetach, this.doDetach, this.finalizeDetach, this.exposeDetach, arguments
		);			
	},


	doDetach: function(node, callObj, callFunc) {
		node.detach();
	}
	
});


// =============================== destroy ============================
dojo.lang.extend(dojo.widget.TreeBasicControllerV3, {

	canDestroy: function(child) {
		
		if (child.parent && !this.canDetach(child)) {
			return false;
		}
		return true;
	},


	destroy: function(node) {
		return this.runStages(
			this.canDestroy, this.prepareDestroy, this.doDestroy, this.finalizeDestroy, this.exposeDestroy, arguments
		);			
	},


	doDestroy: function(node) {
		node.destroy();
	}
	
});



// =============================== move ============================

dojo.lang.extend(dojo.widget.TreeBasicControllerV3, {

	/**
	 * check for non-treenodes
	 */
	canMoveNotANode: function(child, parent) {
		if (child.treeCanMove) {
			return child.treeCanMove(parent);
		}
		
		return true;
	},

	/**
	 * Checks whether it is ok to change parent of child to newParent
	 * May incur type checks etc
	 *
	 * It should check only hierarchical possibility w/o index, etc
	 * because in onDragOver event for Between Dnd mode we can't calculate index at once on onDragOVer.
	 * index changes as client moves mouse up-down over the node
	 */
	canMove: function(child, newParent){
		if (!child.isTreeNode) {
			return this.canMoveNotANode(child, newParent);
		}
						
		if (child.actionIsDisabledNow(child.actions.MOVE)) {
			return false;
		}

		// if we move under same parent then no matter if ADDCHILD disabled for him
		// but if we move to NEW parent then check if action is disabled for him
		// also covers case for newParent being a non-folder in strict mode etc
		if (child.parent !== newParent && newParent.actionIsDisabledNow(newParent.actions.ADDCHILD)) {
			return false;
		}

		// Can't move parent under child. check whether new parent is child of "child".
		var node = newParent;
		while(node.isTreeNode) {
			//dojo.debugShallow(node.title)
			if (node === child) {
				// parent of newParent is child
				return false;
			}
			node = node.parent;
		}

		return true;
	},


	move: function(child, newParent, index/*,...*/) {
		return this.runStages(this.canMove, this.prepareMove, this.doMove, this.finalizeMove, this.exposeMove, arguments);			
	},

	doMove: function(child, newParent, index) {
		//dojo.debug("MOVE "+child);
		child.tree.move(child, newParent, index);

		return true;
	},
	
	exposeMove: function(child, newParent) {		
		if (newParent.isTreeNode) {
			this.expand(newParent);
		}
	}
		

});

dojo.lang.extend(dojo.widget.TreeBasicControllerV3, {

	// -----------------------------------------------------------------------------
	//                             Create node stuff
	// -----------------------------------------------------------------------------


	canCreateChild: function(parent, index, data) {
		if (parent.actionIsDisabledNow(parent.actions.ADDCHILD)) {
			return false;
		}

		return true;
	},


	/* send data to server and add child from server */
	/* data may contain an almost ready child, or anything else, suggested to server */
	/*in Rpc controllers server responds with child data to be inserted */
	createChild: function(parent, index, data) {
		return this.runStages(this.canCreateChild, this.prepareCreateChild, this.doCreateChild, this.finalizeCreateChild, this.exposeCreateChild, arguments);		
	},


	doCreateChild: function(parent, index, data) {

		var newChild = parent.tree.createNode(data); 
		//var newChild = dojo.widget.createWidget(widgetType, data);

		parent.addChild(newChild, index);

		return newChild;
	},
	
	exposeCreateChild: function(parent) {
		return this.expand(parent);
	}


});
