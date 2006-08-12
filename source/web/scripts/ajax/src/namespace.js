dojo.provide("dojo.namespace");

//Every namespace that is defined using the dojo.defineNamespace method has one of these Namespace objects created.
//It stores the fully qualified namespace name, it's location relative to the dojo root, the short namespace name, and a 
//resolver function that maps a widget's short name to it's fully qualified name
dojo.Namespace = function(objRoot, location, nsPrefix, resolver){
	this.root = objRoot;
	this.location = location;
	this.nsPrefix = nsPrefix;
	this.resolver = resolver;
	
	dojo.setModulePrefix(nsPrefix, location);
};

dojo.Namespace.prototype._loaded = {};
dojo.Namespace.prototype.load = function(name, domain){
	if(this.resolver){
		var fullName = this.resolver(name,domain);
		//only load a widget once. This is a quicker check than dojo.require does
		if(fullName && !this._loaded[fullName]){
			//workaround so we don't break the build system
			var req = dojo.require;
			req(fullName);

			this._loaded[fullName] = true;
 		}
		if(this._loaded[fullName]){
			return true;
		}
	}
	return false;
};

//This function is used to define a new namespace. 
//objRoot is the fully qualified namespace name
//location is the file system location relative to the dojo root, e.g. "../myNewNamespace"
//nsPrefix is the short name of the namespace. e.g. for the namespace " my.new.namespace", the nsPrefix could be "mnn"
//resolver is a function that takes two parameters:
//    1. a short name of a widget and returns it's fully qualified name. For example if passed "checkbox", it could return " dojo.widget.CheckBox"
//    2. the widget domain, e.g. "html", "svg", "vml" etc.  This is optional, and depends on what the particular widget set supports. Dojo defaults to "html" 
//   resolver is optional, as it only applies to widgets, and a namespace may have no widgets
//widgetPackage the name of a widget package.  e.g. if you had a namespace with nsPrefix = "mnn", and your widgets were in a 
//   "widget" folder in that namespace, your widget package would be "mnn.widget".  This is optional, like the resolver
dojo.defineNamespace = function(objRoot, location, nsPrefix, resolver /*optional*/, widgetPackage /*optional*/){
//    dojo.debug("dojo.defineNamespace('"+objRoot+"','"+location+"','"+nsPrefix+"',resolver) called");
	if(dojo._namespaces[objRoot]){
		return;
	}
	var ns = new dojo.Namespace(objRoot, location, nsPrefix, resolver);
	dojo._namespaces[objRoot] = ns;
	if(nsPrefix){
		dojo._namespaces[nsPrefix] = ns;
	}
	if(widgetPackage){
		dojo.widget.manager.registerWidgetPackage(widgetPackage);
	}
};

dojo.findNamespaceForWidget = function(widgetName){
	dojo.deprecated('dojo.findNamespaceForWidget', 'Widget [' + widgetName + '] not defined for a namespace'+
		', so searching all namespaces. Developers should specify namespaces for all non-Dojo widgets', "0.5");						
	widgetName = widgetName.toLowerCase();
	for(x in dojo._namespaces){
		if(dojo._namespaces[x].load(widgetName)){
			return dojo._namespaces[x];
		}
	}
};
