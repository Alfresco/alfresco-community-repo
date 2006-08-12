dojo.provide("dojo.widget.DomWidget");

dojo.require("dojo.event.*");
dojo.require("dojo.widget.Widget");
dojo.require("dojo.dom");
dojo.require("dojo.html.style");
dojo.require("dojo.xml.Parse");
dojo.require("dojo.uri.*");
dojo.require("dojo.lang.func");
dojo.require("dojo.lang.extras");

dojo.widget._cssFiles = {};
dojo.widget._cssStrings = {};
dojo.widget._templateCache = {};

dojo.widget.defaultStrings = {
	dojoRoot: dojo.hostenv.getBaseScriptUri(),
	baseScriptUri: dojo.hostenv.getBaseScriptUri()
};

dojo.widget.buildFromTemplate = function() {
	dojo.lang.forward("fillFromTemplateCache");
}

// static method to build from a template w/ or w/o a real widget in place
dojo.widget.fillFromTemplateCache = function(obj, templatePath, templateString, avoidCache){
	// dojo.debug("avoidCache:", avoidCache);
	var tpath = templatePath || obj.templatePath;

	// DEPRECATED: use Uri objects, not strings
	if (tpath && !(tpath instanceof dojo.uri.Uri)) {
		tpath = dojo.uri.dojoUri(tpath);
		dojo.deprecated("templatePath should be of type dojo.uri.Uri", null, "0.4");
	}

	var tmplts = dojo.widget._templateCache;
	if(!obj["widgetType"]) { // don't have a real template here
		do {
			var dummyName = "__dummyTemplate__" + dojo.widget._templateCache.dummyCount++;
		} while(tmplts[dummyName]);
		obj.widgetType = dummyName;
	}
	var wt = obj.widgetType;

	var ts = tmplts[wt];
	if(!ts){
		tmplts[wt] = { "string": null, "node": null };
		if(avoidCache){
			ts = {};
		}else{
			ts = tmplts[wt];
		}
	}
	if((!obj.templateString)&&(!avoidCache)){
		obj.templateString = templateString || ts["string"];
	}
	if((!obj.templateNode)&&(!avoidCache)){
		obj.templateNode = ts["node"];
	}
	if((!obj.templateNode)&&(!obj.templateString)&&(tpath)){
		// fetch a text fragment and assign it to templateString
		// NOTE: we rely on blocking IO here!
		var tstring = dojo.hostenv.getText(tpath);
		if(tstring){
			// strip <?xml ...?> declarations so that external SVG and XML
			// documents can be added to a document without worry
			tstring = tstring.replace(/^\s*<\?xml(\s)+version=[\'\"](\d)*.(\d)*[\'\"](\s)*\?>/im, "");
			var matches = tstring.match(/<body[^>]*>\s*([\s\S]+)\s*<\/body>/im);
			if(matches){
				tstring = matches[1];
			}
		}else{
			tstring = "";
		}
		obj.templateString = tstring;
		if(!avoidCache){
			tmplts[wt]["string"] = tstring;
		}
	}
	if((!ts["string"])&&(!avoidCache)){
		ts.string = obj.templateString;
	}
}
dojo.widget._templateCache.dummyCount = 0;

dojo.widget.attachProperties = ["dojoAttachPoint", "id"];
dojo.widget.eventAttachProperty = "dojoAttachEvent";
dojo.widget.onBuildProperty = "dojoOnBuild";
dojo.widget.waiNames  = ["waiRole", "waiState"];
dojo.widget.wai = {
	waiRole: { 	name: "waiRole", 
				namespace: "http://www.w3.org/TR/xhtml2", 
				alias: "x2",
				prefix: "wairole:"
	},
	waiState: { name: "waiState", 
				namespace: "http://www.w3.org/2005/07/aaa" , 
				alias: "aaa",
				prefix: ""
	},
	setAttr: function(node, ns, attr, value){
		if(dojo.render.html.ie){
			node.setAttribute(this[ns].alias+":"+ attr, this[ns].prefix+value);
		}else{
			node.setAttributeNS(this[ns].namespace, attr, this[ns].prefix+value);
		}
	},

	getAttr: function(node, ns, attr){
		if(dojo.render.html.ie){
			 return node.getAttribute(this[ns].alias+":"+attr);
		}else{
			return node.getAttributeNS(this[ns].namespace, attr);
		}
	}
};

dojo.widget.attachTemplateNodes = function(rootNode, targetObj, events){
	// FIXME: this method is still taking WAAAY too long. We need ways of optimizing:
	//	a.) what we are looking for on each node
	//	b.) the nodes that are subject to interrogation (use xpath instead?)
	//	c.) how expensive event assignment is (less eval(), more connect())
	// var start = new Date();
	var elementNodeType = dojo.dom.ELEMENT_NODE;

	function trim(str){
		return str.replace(/^\s+|\s+$/g, "");
	}

	if(!rootNode){ 
		rootNode = targetObj.domNode;
	}

	if(rootNode.nodeType != elementNodeType){
		return;
	}
	// alert(events.length);

	var nodes = rootNode.all || rootNode.getElementsByTagName("*");
	var _this = targetObj;
	for(var x=-1; x<nodes.length; x++){
		var baseNode = (x == -1) ? rootNode : nodes[x];
		// FIXME: is this going to have capitalization problems?  Could use getAttribute(name, 0); to get attributes case-insensitve
		var attachPoint = [];
		for(var y=0; y<this.attachProperties.length; y++){
			var tmpAttachPoint = baseNode.getAttribute(this.attachProperties[y]);
			if(tmpAttachPoint){
				attachPoint = tmpAttachPoint.split(";");
				for(var z=0; z<attachPoint.length; z++){
					if(dojo.lang.isArray(targetObj[attachPoint[z]])){
						targetObj[attachPoint[z]].push(baseNode);
					}else{
						targetObj[attachPoint[z]]=baseNode;
					}
				}
				break;
			}
		}
		// continue;

		// FIXME: we need to put this into some kind of lookup structure
		// instead of direct assignment
		var tmpltPoint = baseNode.getAttribute(this.templateProperty);
		if(tmpltPoint){
			targetObj[tmpltPoint]=baseNode;
		}

		dojo.lang.forEach(dojo.widget.waiNames, function(name){
			var wai = dojo.widget.wai[name];
			var val = baseNode.getAttribute(wai.name);
			if(val){
				if(val.indexOf('-') == -1){ 
					dojo.widget.wai.setAttr(baseNode, wai.name, "role", val);
				}else{
					// this is a state-value pair
					var statePair = val.split('-');
					dojo.widget.wai.setAttr(baseNode, wai.name, statePair[0], statePair[1]);
				}
			}
		}, this);

		var attachEvent = baseNode.getAttribute(this.eventAttachProperty);
		if(attachEvent){
			// NOTE: we want to support attributes that have the form
			// "domEvent: nativeEvent; ..."
			var evts = attachEvent.split(";");
			for(var y=0; y<evts.length; y++){
				if((!evts[y])||(!evts[y].length)){ continue; }
				var thisFunc = null;
				var tevt = trim(evts[y]);
				if(evts[y].indexOf(":") >= 0){
					// oh, if only JS had tuple assignment
					var funcNameArr = tevt.split(":");
					tevt = trim(funcNameArr[0]);
					thisFunc = trim(funcNameArr[1]);
				}
				if(!thisFunc){
					thisFunc = tevt;
				}

				var tf = function(){ 
					var ntf = new String(thisFunc);
					return function(evt){
						if(_this[ntf]){
							_this[ntf](dojo.event.browser.fixEvent(evt, this));
						}
					};
				}();
				dojo.event.browser.addListener(baseNode, tevt, tf, false, true);
				// dojo.event.browser.addListener(baseNode, tevt, dojo.lang.hitch(_this, thisFunc));
			}
		}

		for(var y=0; y<events.length; y++){
			//alert(events[x]);
			var evtVal = baseNode.getAttribute(events[y]);
			if((evtVal)&&(evtVal.length)){
				var thisFunc = null;
				var domEvt = events[y].substr(4); // clober the "dojo" prefix
				thisFunc = trim(evtVal);
				var funcs = [thisFunc];
				if(thisFunc.indexOf(";")>=0){
					funcs = dojo.lang.map(thisFunc.split(";"), trim);
				}
				for(var z=0; z<funcs.length; z++){
					if(!funcs[z].length){ continue; }
					var tf = function(){ 
						var ntf = new String(funcs[z]);
						return function(evt){
							if(_this[ntf]){
								_this[ntf](dojo.event.browser.fixEvent(evt, this));
							}
						}
					}();
					dojo.event.browser.addListener(baseNode, domEvt, tf, false, true);
					// dojo.event.browser.addListener(baseNode, domEvt, dojo.lang.hitch(_this, funcs[z]));
				}
			}
		}

		var onBuild = baseNode.getAttribute(this.onBuildProperty);
		if(onBuild){
			eval("var node = baseNode; var widget = targetObj; "+onBuild);
		}
	}

}

dojo.widget.getDojoEventsFromStr = function(str){
	// var lstr = str.toLowerCase();
	var re = /(dojoOn([a-z]+)(\s?))=/gi;
	var evts = str ? str.match(re)||[] : [];
	var ret = [];
	var lem = {};
	for(var x=0; x<evts.length; x++){
		if(evts[x].legth < 1){ continue; }
		var cm = evts[x].replace(/\s/, "");
		cm = (cm.slice(0, cm.length-1));
		if(!lem[cm]){
			lem[cm] = true;
			ret.push(cm);
		}
	}
	return ret;
}

/*
dojo.widget.buildAndAttachTemplate = function(obj, templatePath, templateCssPath, templateString, targetObj) {
	this.buildFromTemplate(obj, templatePath, templateCssPath, templateString);
	var node = dojo.dom.createNodesFromText(obj.templateString, true)[0];
	this.attachTemplateNodes(node, targetObj||obj, dojo.widget.getDojoEventsFromStr(templateString));
	return node;
}
*/

dojo.declare("dojo.widget.DomWidget", dojo.widget.Widget,
	function() {
		if((arguments.length>0)&&(typeof arguments[0] == "object")){
			this.create(arguments[0]);
		}
	},
{							 
	templateNode: null,
	templateString: null,
	templateCssString: null,
	preventClobber: false,
	domNode: null, // this is our visible representation of the widget!
	containerNode: null, // holds child elements

	// Process the given child widget, inserting it's dom node as a child of our dom node
	// FIXME: should we support addition at an index in the children arr and
	// order the display accordingly? Right now we always append.
	addChild: function(widget, overrideContainerNode, pos, ref, insertIndex){
		if(!this.isContainer){ // we aren't allowed to contain other widgets, it seems
			dojo.debug("dojo.widget.DomWidget.addChild() attempted on non-container widget");
			return null;
		}else{
			if(insertIndex == undefined){
				insertIndex = this.children.length;
			}
			this.addWidgetAsDirectChild(widget, overrideContainerNode, pos, ref, insertIndex);
			this.registerChild(widget, insertIndex);
		}
		return widget;
	},
	
	addWidgetAsDirectChild: function(widget, overrideContainerNode, pos, ref, insertIndex){
		if((!this.containerNode)&&(!overrideContainerNode)){
			this.containerNode = this.domNode;
		}
		var cn = (overrideContainerNode) ? overrideContainerNode : this.containerNode;
		if(!pos){ pos = "after"; }
		if(!ref){ 
			if(!cn){ cn = dojo.body(); }
			ref = cn.lastChild; 
		}
		if(!insertIndex) { insertIndex = 0; }
		widget.domNode.setAttribute("dojoinsertionindex", insertIndex);

		// insert the child widget domNode directly underneath my domNode, in the
		// specified position (by default, append to end)
		if(!ref){
			cn.appendChild(widget.domNode);
		}else{
			// FIXME: was this meant to be the (ugly hack) way to support insert @ index?
			//dojo.dom[pos](widget.domNode, ref, insertIndex);

			// CAL: this appears to be the intended way to insert a node at a given position...
			if (pos == 'insertAtIndex'){
				// dojo.debug("idx:", insertIndex, "isLast:", ref === cn.lastChild);
				dojo.dom.insertAtIndex(widget.domNode, ref.parentNode, insertIndex);
			}else{
				// dojo.debug("pos:", pos, "isLast:", ref === cn.lastChild);
				if((pos == "after")&&(ref === cn.lastChild)){
					cn.appendChild(widget.domNode);
				}else{
					dojo.dom.insertAtPosition(widget.domNode, cn, pos);
				}
			}
		}
	},

	// Record that given widget descends from me
	registerChild: function(widget, insertionIndex){

		// we need to insert the child at the right point in the parent's 
		// 'children' array, based on the insertionIndex

		widget.dojoInsertionIndex = insertionIndex;

		var idx = -1;
		for(var i=0; i<this.children.length; i++){
			if (this.children[i].dojoInsertionIndex < insertionIndex){
				idx = i;
			}
		}

		this.children.splice(idx+1, 0, widget);

		widget.parent = this;
		widget.addedTo(this, idx+1);
		
		// If this widget was created programatically, then it was erroneously added
		// to dojo.widget.manager.topWidgets.  Fix that here.
		delete dojo.widget.manager.topWidgets[widget.widgetId];
	},

	removeChild: function(widget){
		// detach child domNode from parent domNode
		dojo.dom.removeNode(widget.domNode);

		// remove child widget from parent widget
		return dojo.widget.DomWidget.superclass.removeChild.call(this, widget);
	},

	getFragNodeRef: function(frag){
		if(!frag || !frag[this.namespace+":"+this.widgetType.toLowerCase()]){
			dojo.raise("Error: no frag for widget type " + this.widgetType
				+ " with namespace "+this.namespace + ", id " + this.widgetId
				+ " (maybe a widget has set it's type incorrectly)");
		}
		return frag ? frag[this.namespace+":"+this.widgetType.toLowerCase()]["nodeRef"] : null;
	},
	
	// Replace source domNode with generated dom structure, and register
	// widget with parent.
	postInitialize: function(args, frag, parentComp){
		
		//dojo.profile.start(this.widgetType + " postInitialize");
		
		var sourceNodeRef = this.getFragNodeRef(frag);
		// Stick my generated dom into the output tree
		//alert(this.widgetId + ": replacing " + sourceNodeRef + " with " + this.domNode.innerHTML);
		if (parentComp && (parentComp.snarfChildDomOutput || !sourceNodeRef)){
			// Add my generated dom as a direct child of my parent widget
			// This is important for generated widgets, and also cases where I am generating an
			// <li> node that can't be inserted back into the original DOM tree
			parentComp.addWidgetAsDirectChild(this, "", "insertAtIndex", "",  args["dojoinsertionindex"], sourceNodeRef);
		} else if (sourceNodeRef){
			// Do in-place replacement of the my source node with my generated dom
			if(this.domNode && (this.domNode !== sourceNodeRef)){
				var oldNode = sourceNodeRef.parentNode.replaceChild(this.domNode, sourceNodeRef);
			}
		}

		// Register myself with my parent, or with the widget manager if
		// I have no parent
		// TODO: the code below erroneously adds all programatically generated widgets
		// to topWidgets (since we don't know who the parent is until after creation finishes)
		if ( parentComp ) {
			parentComp.registerChild(this, args.dojoinsertionindex);
		} else {
			dojo.widget.manager.topWidgets[this.widgetId]=this;
		}


		//dojo.profile.end(this.widgetType + " postInitialize");
		
		// Expand my children widgets
		/* dojoDontFollow is important for a very special case
		 * basically if you have a widget that you instantiate from script
		 * and that widget is a container, and it contains a reference to a parent
		 * instance, the parser will start recursively parsing until the browser
		 * complains.  So the solution is to set an initialization property of 
		 * dojoDontFollow: true and then it won't recurse where it shouldn't
		 */
		if(this.isContainer && !frag["dojoDontFollow"]){
			//alert("recurse from " + this.widgetId);
			// build any sub-components with us as the parent
			var fragParser = dojo.widget.getParser();
			fragParser.createSubComponents(frag, this);
		}
	},

	// method over-ride
	buildRendering: function(args, frag){
		// DOM widgets construct themselves from a template
		var ts = dojo.widget._templateCache[this.widgetType];
		
		// Handle style for this widget here, as even if templatePath
		// is not set, style specified by templateCssString or templateCssPath
		// should be applied. templateCssString has higher priority
		// than templateCssPath
		if(args["templatecsspath"]){
			args["templateCssPath"] = args["templatecsspath"];
		}
		var cpath = args["templateCssPath"] || this.templateCssPath;
		// DEPRECATED: use Uri objects, not strings
		if (cpath && !(cpath instanceof dojo.uri.Uri)) {
			cpath = dojo.uri.dojoUri(cpath);
			dojo.deprecated("templateCssPath should be of type dojo.uri.Uri", null, "0.4");
		}
		if(cpath && !dojo.widget._cssFiles[cpath.toString()]){
			if((!this.templateCssString)&&(cpath)){
				this.templateCssString = dojo.hostenv.getText(cpath);
				this.templateCssPath = null;
			}
			dojo.widget._cssFiles[cpath.toString()] = true;
		}
	
		if((this["templateCssString"])&&(!this.templateCssString["loaded"])){
			dojo.html.insertCssText(this.templateCssString, null, cpath);
			if(!this.templateCssString){ this.templateCssString = ""; }
			this.templateCssString.loaded = true;
		}
		if(	
			(!this.preventClobber)&&(
				(this.templatePath)||
				(this.templateNode)||
				(
					(this["templateString"])&&(this.templateString.length) 
				)||
				(
					(typeof ts != "undefined")&&( (ts["string"])||(ts["node"]) )
				)
			)
		){
			// if it looks like we can build the thing from a template, do it!
			this.buildFromTemplate(args, frag);
		}else{
			// otherwise, assign the DOM node that was the source of the widget
			// parsing to be the root node
			this.domNode = this.getFragNodeRef(frag);
		}
		this.fillInTemplate(args, frag); 	// this is where individual widgets
											// will handle population of data
											// from properties, remote data
											// sets, etc.
	},

	buildFromTemplate: function(args, frag){
		// var start = new Date();
		// copy template properties if they're already set in the templates object
		// dojo.debug("buildFromTemplate:", this);
		var avoidCache = false;
		if(args["templatepath"]){
			avoidCache = true;
			args["templatePath"] = args["templatepath"];
		}
		dojo.widget.fillFromTemplateCache(	this, 
											args["templatePath"], 
											null,
											avoidCache);
		var ts = dojo.widget._templateCache[this.widgetType];
		if((ts)&&(!avoidCache)){
			if(!this.templateString.length){
				this.templateString = ts["string"];
			}
			if(!this.templateNode){
				this.templateNode = ts["node"];
			}
		}
		var matches = false;
		var node = null;
		// var tstr = new String(this.templateString); 
		var tstr = this.templateString; 
		// attempt to clone a template node, if there is one
		if((!this.templateNode)&&(this.templateString)){
			matches = this.templateString.match(/\$\{([^\}]+)\}/g);
			if(matches) {
				// if we do property replacement, don't create a templateNode
				// to clone from.
				var hash = this.strings || {};
				// FIXME: should this hash of default replacements be cached in
				// templateString?
				for(var key in dojo.widget.defaultStrings) {
					if(dojo.lang.isUndefined(hash[key])) {
						hash[key] = dojo.widget.defaultStrings[key];
					}
				}
				// FIXME: this is a lot of string munging. Can we make it faster?
				for(var i = 0; i < matches.length; i++) {
					var key = matches[i];
					key = key.substring(2, key.length-1);
					var kval = (key.substring(0, 5) == "this.") ? dojo.lang.getObjPathValue(key.substring(5), this) : hash[key];
					var value;
					if((kval)||(dojo.lang.isString(kval))){
						value = (dojo.lang.isFunction(kval)) ? kval.call(this, key, this.templateString) : kval;
						tstr = tstr.replace(matches[i], value);
					}
				}
			}else{
				// otherwise, we are required to instantiate a copy of the template
				// string if one is provided.
				
				// FIXME: need to be able to distinguish here what should be done
				// or provide a generic interface across all DOM implementations
				// FIMXE: this breaks if the template has whitespace as its first 
				// characters
				// node = this.createNodesFromText(this.templateString, true);
				// this.templateNode = node[0].cloneNode(true); // we're optimistic here
				this.templateNode = this.createNodesFromText(this.templateString, true)[0];
				if(!avoidCache){
					ts.node = this.templateNode;
				}
			}
		}
		if((!this.templateNode)&&(!matches)){ 
			dojo.debug("DomWidget.buildFromTemplate: could not create template");
			return false;
		}else if(!matches){
			node = this.templateNode.cloneNode(true);
			if(!node){ return false; }
		}else{
			node = this.createNodesFromText(tstr, true)[0];
		}

		// recurse through the node, looking for, and attaching to, our
		// attachment points which should be defined on the template node.

		this.domNode = node;
		// dojo.profile.start("attachTemplateNodes");
		this.attachTemplateNodes(this.domNode, this);
		// dojo.profile.end("attachTemplateNodes");
		
		// relocate source contents to templated container node
		// this.containerNode must be able to receive children, or exceptions will be thrown
		if (this.isContainer && this.containerNode){
			var src = this.getFragNodeRef(frag);
			if (src){
				dojo.dom.moveChildren(src, this.containerNode);
			}
		}
	},

	attachTemplateNodes: function(baseNode, targetObj){
		if(!targetObj){ targetObj = this; }
		return dojo.widget.attachTemplateNodes(baseNode, targetObj, 
					dojo.widget.getDojoEventsFromStr(this.templateString));
	},

	fillInTemplate: function(){
		// dojo.unimplemented("dojo.widget.DomWidget.fillInTemplate");
	},
	
	// method over-ride
	destroyRendering: function(){
		try{
			delete this.domNode;
		}catch(e){ /* squelch! */ }
	},

	// FIXME: method over-ride
	cleanUp: function(){},
	
	getContainerHeight: function(){
		dojo.unimplemented("dojo.widget.DomWidget.getContainerHeight");
	},

	getContainerWidth: function(){
		dojo.unimplemented("dojo.widget.DomWidget.getContainerWidth");
	},

	createNodesFromText: function(){
		dojo.unimplemented("dojo.widget.DomWidget.createNodesFromText");
	}
});
