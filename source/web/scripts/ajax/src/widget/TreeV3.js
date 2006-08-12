/**
 * Tree model does all the drawing, visual node management etc.
 * Throws events about clicks on it, so someone may catch them and process
 * Tree knows nothing about DnD stuff, covered in TreeDragAndDrop and (if enabled) attached by controller
*/

/**
 * TODO: use domNode.cloneNode instead of createElement for grid
 * Should be faster (lyxsus)
 */
dojo.provide("dojo.widget.TreeV3");


dojo.require("dojo.widget.TreeWithNode");
dojo.require("dojo.widget.*");
dojo.require("dojo.event.*");
dojo.require("dojo.io.*");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.widget.TreeNodeV3");



// make it a tag
dojo.widget.tags.addParseTreeHandler("dojo:TreeV3");


dojo.widget.TreeV3 = function() {
	dojo.widget.HtmlWidget.call(this);

	this.eventNames = {};
	
	this.DndAcceptTypes = [];
	this.actionsDisabled = [];
	
	this.listeners = [];
	
	this.tree = this;

}
dojo.inherits(dojo.widget.TreeV3, dojo.widget.HtmlWidget);

dojo.lang.extend(dojo.widget.TreeV3, dojo.widget.TreeWithNode);

dojo.lang.extend(dojo.widget.TreeV3, {
	widgetType: "TreeV3",

	DndMode: "",

	/**
	 * factory to generate default widgets
	 */
	defaultChildWidget: null,
	
	defaultChildTitle: "New Node", // for editing
	
	
	eagerWidgetInstantiation: false,
	
	eventNamesDefault: {

		// tree created.. Perform tree-wide actions if needed
		afterTreeCreate: "afterTreeCreate",
		beforeTreeDestroy: "beforeTreeDestroy",
		beforeDestroy: "beforeDestroy",
		afterChangeTree: "afterChangeTree",

		afterSetFolder: "afterSetFolder",
		afterUnsetFolder: "afterUnsetFolder",		
		beforeMoveFrom: "beforeMoveFrom",
		beforeMoveTo: "beforeMoveTo",
		afterMoveFrom: "afterMoveFrom",
		afterMoveTo: "afterMoveTo",
		afterAddChild: "afterAddChild",
		afterDetach: "afterDetach",
		afterExpand: "afterExpand",
		afterSetTitle: "afterSetTitle",		
		afterCollapse: "afterCollapse"
	},

	classPrefix: "Tree",
	
	/**
	 * is it possible to add a new child to leaf ?
	 */	
	allowAddChildToLeaf: true,
	
	/**
	 * when last children is removed from node should it stop being a "folder" ?
	 */
	unsetFolderOnEmpty: true,


	DndModes: {
		BETWEEN: 1,
		ONTO: 2
	},

	DndAcceptTypes: "",

    // will have cssRoot before it 
	templateCssPath: dojo.uri.dojoUri("src/widget/templates/TreeV3.css"),

	templateString: '<div>\n</div>',

	isExpanded: true, // consider this "root node" to be always expanded

	isTree: true,
	
	objectId: "",

	createNode: function(data) {
			
		data.tree = this.widgetId;		
		
		if (data.widgetName) {
			// TODO: check if such widget has createSimple
			return dojo.widget.createWidget(data.widgetName, data);		
		} else if (this.defaultChildWidget.prototype.createSimple) {			
			return this.defaultChildWidget.prototype.createSimple(data);					
		} else {
			var ns = this.defaultChildWidget.prototype.namespace; 
			var wt = this.defaultChildWidget.prototype.widgetType; 

			return dojo.widget.createWidget(ns + ":" + wt, data); 
		}
 	    	
	},
				

	// expandNode has +- CSS background. Not img.src for performance, background src string resides in single place.
	// selection in KHTML/Mozilla disabled treewide, IE requires unselectable for every node
	// you can add unselectable if you want both in postCreate of tree and in this template

	// create new template and put into prototype
	makeNodeTemplate: function() {
		
		var domNode = document.createElement("div");
		dojo.html.setClass(domNode, this.classPrefix+"Node "+this.classPrefix+"ExpandLeaf "+this.classPrefix+"ChildrenNo");		
		this.nodeTemplate = domNode;
		
		var expandNode = document.createElement("div");
		var clazz = this.classPrefix+"Expand";
		if (dojo.render.html.ie) {
			clazz = clazz + ' ' + this.classPrefix+"IEExpand";
		}
		dojo.html.setClass(expandNode, clazz);
		
		this.expandNodeTemplate = expandNode;

		// need <span> inside <div>
		// div for multiline support, span for styling exactly the text, not whole line
		var labelNode = document.createElement("span");
		dojo.html.setClass(labelNode, this.classPrefix+"Label");
		this.labelNodeTemplate = labelNode;
		
		var contentNode = document.createElement("div");
		var clazz = this.classPrefix+"Content";
		
		/**
		 * IE does not support min-height properly so I have to rely
		 * on this hack
		 * FIXME: do it in CSS only, remove iconHeight from code
		 */
		if (dojo.render.html.ie) {
			clazz = clazz + ' ' + this.classPrefix+"IEContent";
		}	
		
				
		dojo.html.setClass(contentNode, clazz);
		
		this.contentNodeTemplate = contentNode;
		
		domNode.appendChild(expandNode);
		domNode.appendChild(contentNode);
		contentNode.appendChild(labelNode);
		
		
	},

	makeContainerNodeTemplate: function() {
		
		var div = document.createElement('div');
		div.style.display = 'none';			
		dojo.html.setClass(div, this.classPrefix+"Container");
		
		this.containerNodeTemplate = div;
		
	},


	//
	// these icons control the grid and expando buttons for the whole tree
	//

	icons: {
		expandLoading: dojo.uri.dojoUri("src/widget/templates/images/TreeV3/loading.gif")		
	},
		

	
	actions: {
    	ADDCHILD: "ADDCHILD"
	},


	getInfo: function() {
		var info = {
			widgetId: this.widgetId,
			objectId: this.objectId
		}

		return info;
	},

	adjustEventNames: function() {
		
		for(var name in this.eventNamesDefault) {
			if (dojo.lang.isUndefined(this.eventNames[name])) {
				this.eventNames[name] = this.widgetId+"/"+this.eventNamesDefault[name];
			}
		}
	},

	
	adjustDndMode: function() {
		var _this = this;
		
		
		var DndMode = 0;
		dojo.lang.forEach(this.DndMode.split(';'),
			function(elem) {
				var mode = _this.DndModes[dojo.string.trim(elem).toUpperCase()];
				if (mode) DndMode = DndMode | mode;
			}
		 );
	
		
		this.DndMode = DndMode;

	},
	
	/**
	 * publish destruction event so that any listeners should stop listening
	 */
	destroy: function() {
		dojo.event.topic.publish(this.tree.eventNames.beforeTreeDestroy, { source: this } );

		return dojo.widget.HtmlWidget.prototype.destroy.apply(this, arguments);
	},

	initialize: function(args){
		
		//dojo.debug(args.defaultChildWidget ? true : false)
		
		if (!args.defaultChildWidget) {
			this.defaultChildWidget = dojo.widget.TreeNodeV3;
		} else {
			this.defaultChildWidget = dojo.lang.getObjPathValue(args.defaultChildWidget);
		}
		
		this.adjustEventNames();
		this.adjustDndMode();

		this.makeNodeTemplate();
		this.makeContainerNodeTemplate();
		
		//this.initializeSelector();
		//this.initializeController();
		//this.initializeMenu();

		this.containerNode = this.domNode;
		
		dojo.html.setClass(this.domNode, this.classPrefix+"Container");
		
		var _this = this;
		
		//dojo.debug(this.listeners[1]);
						
		
		dojo.lang.forEach(this.listeners,
			function(elem) {
				var t = dojo.widget.manager.getWidgetById(elem);
				if (! (t instanceof dojo.widget.Widget)) {
					dojo.raise("No listener found by widgetId for "+elem);
				}
				//dojo.debug(t)
				t.listenTree(_this)
				
			}
		);
		
		

	},

	
	postCreate: function() {						
		dojo.event.topic.publish(this.eventNames.afterTreeCreate, { source: this } );
	},
	
	
	/**
	 * Move child to newParent as last child
	 * redraw tree and update icons.
	 *
	 * Called by target, saves source in event.
	 * events are published for BOTH trees AFTER update.
	*/
	move: function(child, newParent, index) {
		
		if (!child.parent) {
			dojo.raise(this.widgetType+": child can be moved only while it's attached");
		}
		
		var oldParent = child.parent;
		var oldTree = child.tree;
		var oldIndex = child.getParentIndex();
		var newTree = newParent.tree;
		var newParent = newParent;
		var newIndex = index;

		var message = {
				oldParent: oldParent, oldTree: oldTree, oldIndex: oldIndex,
				newParent: newParent, newTree: newTree, newIndex: newIndex,
				child: child
		};

		dojo.event.topic.publish(oldTree.eventNames.beforeMoveFrom, message);
		dojo.event.topic.publish(newTree.eventNames.beforeMoveTo, message);
		
		this.doMove.apply(this, arguments);

		
		/* publish events here about structural changes for both source and target trees */
		dojo.event.topic.publish(oldTree.eventNames.afterMoveFrom, message);
		dojo.event.topic.publish(newTree.eventNames.afterMoveTo, message);

	},


	/* do actual parent change here. Write remove child first */
	doMove: function(child, newParent, index) {
		//dojo.debug("MOVE "+child+" to "+newParent+" at "+index);

		//var parent = child.parent;
		child.doDetach();

		//dojo.debug("addChild "+child+" to "+newParent+" at "+index);

		newParent.doAddChild(child, index);
	},

	toString: function() {
		return "["+this.widgetType+" ID:"+this.widgetId	+"]"
	}



});



