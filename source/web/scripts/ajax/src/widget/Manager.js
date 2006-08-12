dojo.provide("dojo.widget.Manager");
dojo.require("dojo.lang.array");
dojo.require("dojo.lang.func");
dojo.require("dojo.event.*");

// Manager class
dojo.widget.manager = new function(){
	this.widgets = [];
	this.widgetIds = [];
	
	// map of widgetId-->widget for widgets without parents (top level widgets)
	this.topWidgets = {};

	var widgetTypeCtr = {};
	var renderPrefixCache = [];

	this.getUniqueId = function (widgetType) {
		return widgetType + "_" + (widgetTypeCtr[widgetType] != undefined ?
			++widgetTypeCtr[widgetType] : widgetTypeCtr[widgetType] = 0);
	}

	this.add = function(widget){
		//dojo.profile.start("dojo.widget.manager.add");
		this.widgets.push(widget);
		// Opera9 uses ID (caps)
		if(!widget.extraArgs["id"]){
			widget.extraArgs["id"] = widget.extraArgs["ID"];
		}
		// FIXME: the rest of this method is very slow!
		if(widget.widgetId == ""){
			if(widget["id"]){
				widget.widgetId = widget["id"];
			}else if(widget.extraArgs["id"]){
				widget.widgetId = widget.extraArgs["id"];
			}else{
				widget.widgetId = this.getUniqueId(widget.widgetType);
			}
		}
		if(this.widgetIds[widget.widgetId]){
			dojo.debug("widget ID collision on ID: "+widget.widgetId);
		}
		this.widgetIds[widget.widgetId] = widget;
		// Widget.destroy already calls removeById(), so we don't need to
		// connect() it here
		//dojo.profile.end("dojo.widget.manager.add");
	}

	this.destroyAll = function(){
		for(var x=this.widgets.length-1; x>=0; x--){
			try{
				// this.widgets[x].destroyChildren();
				this.widgets[x].destroy(true);
				delete this.widgets[x];
			}catch(e){ }
		}
	}

	// FIXME: we should never allow removal of the root widget until all others
	// are removed!
	this.remove = function(widgetIndex){
		if(dojo.lang.isNumber(widgetIndex)){
			var tw = this.widgets[widgetIndex].widgetId;
			delete this.widgetIds[tw];
			this.widgets.splice(widgetIndex, 1);
		}else{
			this.removeById(widgetIndex);
		}
	}
	
	// FIXME: suboptimal performance
	this.removeById = function(id) {
		if(!dojo.lang.isString(id)){
			id = id["widgetId"];
			if(!id){ dojo.debug("invalid widget or id passed to removeById"); return; }
		}
		for (var i=0; i<this.widgets.length; i++){
			if(this.widgets[i].widgetId == id){
				this.remove(i);
				break;
			}
		}
	}

	this.getWidgetById = function(id){
		if(dojo.lang.isString(id)){
			return this.widgetIds[id];
		}
		return id;
	}

	this.getWidgetsByType = function(type){
		var lt = type.toLowerCase();
		var ret = [];
		dojo.lang.forEach(this.widgets, function(x){
			if(x.widgetType.toLowerCase() == lt){
				ret.push(x);
			}
		});
		return ret;
	}

	this.getWidgetsByFilter = function(unaryFunc, onlyOne){
		var ret = [];
		dojo.lang.every(this.widgets, function(x){
			if(unaryFunc(x)){
				ret.push(x);
				if(onlyOne){return false;}
			}
			return true;
		});
		return (onlyOne ? ret[0] : ret);
	}

	this.getAllWidgets = function() {
		return this.widgets.concat();
	}

	//	added, trt 2006-01-20
	this.getWidgetByNode = function(/* DOMNode */ node){
		var w=this.getAllWidgets();
		node = dojo.byId(node);
		for(var i=0; i<w.length; i++){
			if(w[i].domNode==node){
				return w[i];
			}
		}
		return null;
	}

	// shortcuts, baby
	this.byId = this.getWidgetById;
	this.byType = this.getWidgetsByType;
	this.byFilter = this.getWidgetsByFilter;
	this.byNode = this.getWidgetByNode;

	// map of previousally discovered implementation names to constructors
	var knownWidgetImplementations = {};

	// support manually registered widget packages
	var widgetPackages = ["dojo.widget"];
	for (var i=0; i<widgetPackages.length; i++) {
		// convenience for checking if a package exists (reverse lookup)
		widgetPackages[widgetPackages[i]] = true;
	}

	this.registerWidgetPackage = function(pname) {
		if(!widgetPackages[pname]){
			widgetPackages[pname] = true;
			widgetPackages.push(pname);
		}
	}
	
	this.getWidgetPackageList = function() {
		return dojo.lang.map(widgetPackages, function(elt) { return(elt!==true ? elt : undefined); });
	}
	
	this.getImplementation = function(widgetName, ctorObject, mixins, namespace){
		// try and find a name for the widget
		var impl = this.getImplementationName(widgetName, namespace);
		if(impl){ 
			// var tic = new Date();
			var ret;
			if(ctorObject){ret = new impl(ctor);}
			else{ret = new impl();}
			// dojo.debug(new Date() - tic);
			return ret;
		}
	}

	this.getImplementationName = function(widgetName, namespace){
		/*
		 * This is the overly-simplistic implemention of getImplementation (har
		 * har). In the future, we are going to want something that allows more
		 * freedom of expression WRT to specifying different specializations of
		 * a widget.
		 *
		 * Additionally, this implementation treats widget names as case
		 * insensitive, which does not necessarialy mesh with the markup which
		 * can construct a widget.
		 */
		if(!namespace){namespace="dojo";}
		var lowerCaseWidgetName = widgetName.toLowerCase();

		if(!knownWidgetImplementations[namespace]){knownWidgetImplementations[namespace]={};}
		
		var impl = knownWidgetImplementations[namespace][lowerCaseWidgetName];
		if(impl){
			return impl;
		}
		var ns = dojo.getNamespace(namespace);
		if(ns){ns.load(widgetName);}

		// first store a list of the render prefixes we are capable of rendering
		if(!renderPrefixCache.length){
			for(var renderer in dojo.render){
				if(dojo.render[renderer]["capable"] === true){
					var prefixes = dojo.render[renderer].prefixes;
					for(var i = 0; i < prefixes.length; i++){
						renderPrefixCache.push(prefixes[i].toLowerCase());
					}
				}
			}
			// make sure we don't HAVE to prefix widget implementation names
			// with anything to get them to render
			renderPrefixCache.push("");
		}

		var nsSearchArr = null;
		
		var deprWarningLogged=false;
		for(var counter = 0; counter < 2; counter++){	
			// look for a rendering-context specific version of our widget name
			for(var i = 0; i < widgetPackages.length; i++){
				var widgetPackage = dojo.evalObjPath(widgetPackages[i]);
				if(!widgetPackage) { continue; }
				var pos = widgetPackages[i].indexOf(".");
				if(pos > -1){
					var n = widgetPackages[i].substring(0,pos);
					if(n != namespace){
						if(counter==0){continue;}
						if(!deprWarningLogged){
							deprWarningLogged = true;
							dojo.deprecated('dojo.widget.Manager.getImplementationName', 'Wrong namespace ('+namespace+
								') specified. Developers should specify correct namespaces for all non-Dojo widgets', "0.5");
						}
					}
				}

				for (var j = 0; j < renderPrefixCache.length; j++) {
					if (!widgetPackage[renderPrefixCache[j]]) { continue; }
					for (var widgetClass in widgetPackage[renderPrefixCache[j]]) {
						if (widgetClass.toLowerCase() != lowerCaseWidgetName) { continue; }
						knownWidgetImplementations[namespace][lowerCaseWidgetName] =
							widgetPackage[renderPrefixCache[j]][widgetClass];
						return knownWidgetImplementations[namespace][lowerCaseWidgetName];
					}
				}
	
				for (var j = 0; j < renderPrefixCache.length; j++) {
					for (var widgetClass in widgetPackage) {
						if (widgetClass.toLowerCase() != (renderPrefixCache[j] + lowerCaseWidgetName) &&
							widgetClass.toLowerCase() != lowerCaseWidgetName) { continue; }
		
						knownWidgetImplementations[namespace][lowerCaseWidgetName] =
							widgetPackage[widgetClass];
						return knownWidgetImplementations[namespace][lowerCaseWidgetName];
					}
				}
			}
			var newNs = dojo.findNamespaceForWidget(lowerCaseWidgetName);
			if(newNs){
				namespace = newNs.nsPrefix;	
			}
		}
		
		throw new Error('Could not locate "' + widgetName + '" class');
	}

	// FIXME: does it even belong in this name space?
	// NOTE: this method is implemented by DomWidget.js since not all
	// hostenv's would have an implementation.
	/*this.getWidgetFromPrimitive = function(baseRenderType){
		dojo.unimplemented("dojo.widget.manager.getWidgetFromPrimitive");
	}

	this.getWidgetFromEvent = function(nativeEvt){
		dojo.unimplemented("dojo.widget.manager.getWidgetFromEvent");
	}*/

	// Catch window resize events and notify top level widgets
	this.resizing=false;
	this.onWindowResized = function(){
		if(this.resizing){
			return;	// duplicate event
		}
		try{
			this.resizing=true;
			for(var id in this.topWidgets){
				var child = this.topWidgets[id];
				if(child.checkSize ){
					child.checkSize();
				}
			}
		}catch(e){
		}finally{
			this.resizing=false;
		}
	}
	if(typeof window != "undefined") {
		dojo.addOnLoad(this, 'onWindowResized');							// initial sizing
		dojo.event.connect(window, 'onresize', this, 'onWindowResized');	// window resize
	}

	// FIXME: what else?
};

(function(){
	var dw = dojo.widget;
	var dwm = dw.manager;
	var h = dojo.lang.curry(dojo.lang, "hitch", dwm);
	var g = function(oldName, newName){
		dw[(newName||oldName)] = h(oldName);
	}
	// copy the methods from the default manager (this) to the widget namespace
	g("add", "addWidget");
	g("destroyAll", "destroyAllWidgets");
	g("remove", "removeWidget");
	g("removeById", "removeWidgetById");
	g("getWidgetById");
	g("getWidgetById", "byId");
	g("getWidgetsByType");
	g("getWidgetsByFilter");
	g("getWidgetsByType", "byType");
	g("getWidgetsByFilter", "byFilter");
	g("getWidgetByNode", "byNode");
	dw.all = function(n){
		var widgets = dwm.getAllWidgets.apply(dwm, arguments);
		if(arguments.length > 0) {
			return widgets[n];
		}
		return widgets;
	}
	g("registerWidgetPackage");
	g("getImplementation", "getWidgetImplementation");
	g("getImplementationName", "getWidgetImplementationName");

	dw.widgets = dwm.widgets;
	dw.widgetIds = dwm.widgetIds;
	dw.root = dwm.root;
})();
