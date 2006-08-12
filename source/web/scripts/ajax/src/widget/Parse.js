dojo.provide("dojo.widget.Parse");

dojo.require("dojo.widget.Manager");
dojo.require("dojo.dom");
dojo.require("dojo.namespace");

dojo.widget.Parse = function(fragment){
	this.propertySetsList = [];
	this.fragment = fragment;
	
	this.createComponents = function(frag, parentComp){
		var comps = [];
		var built = false;
		// if we have items to parse/create at this level, do it!
		try{
			if((frag)&&(frag["tagName"])&&(frag!=frag["nodeRef"])){
				var djTags = dojo.widget.tags;
				// we split so that you can declare multiple
				// non-destructive widgets from the same ctor node
				var tna = String(frag["tagName"]).split(";");
				for(var x=0; x<tna.length; x++){
					var ltn = (tna[x].replace(/^\s+|\s+$/g, "")).toLowerCase();
					var pos = ltn.indexOf(":");
					var nsName = (pos > 0) ? ltn.substring(0,pos) : null;
					//if we don't already have a tag registered for this widget, try to load it's
					//namespace, if it has one
					if(!djTags[ltn] && dojo.getNamespace && dojo.lang.isString(ltn) && pos>0){				    					
						var ns = dojo.getNamespace(nsName);
						var tagName = ltn.substring(pos+1,ltn.length);
						var domain = null;
						var dojoDomain = frag[ltn]["dojoDomain"] || frag[ltn]["dojodomain"]; 
						if(dojoDomain){
							domain = dojoDomain[0].value;
						}
						if(ns){
						    ns.load(tagName, domain);
						}
					    
					}
					
					if(!djTags[ltn]){
						dojo.deprecated('dojo.widget.Parse.createComponents', 'Widget not defined for  namespace'+nsName+
							', so searching all namespaces. Developers should specify namespaces for all non-Dojo widgets', "0.5");						
	
						var newNs = dojo.findNamespaceForWidget(tagName);
						if(newNs){
							ltn = newNs.nsPrefix + ":" + (ltn.indexOf(":")> 0 ? ltn.substring(ltn.indexOf(":")+1) : ltn);
						}
					}
	
					if(djTags[ltn]){
						built = true;
						frag.tagName = ltn;
						var ret = djTags[ltn](frag, this, parentComp, frag["index"]);
						comps.push(ret);
					}else{
						if(dojo.lang.isString(ltn) && nsName && dojo._namespaces[nsName]){
							dojo.debug("no tag handler registered for type: ", ltn);
						} 
					}
				}
			}
		}catch(e){
			dojo.debug("dojo.widget.Parse: error:", e);
			// throw e;
			// IE is such a bitch sometimes
		}
		// if there's a sub-frag, build widgets from that too
		if(!built){
			comps = comps.concat(this.createSubComponents(frag, parentComp));
		}
		return comps;
	}

	/*	createSubComponents recurses over a raw JavaScript object structure,
			and calls the corresponding handler for its normalized tagName if it exists
	*/
	this.createSubComponents = function(fragment, parentComp){
		var frag, comps = [];
		for(var item in fragment){
			frag = fragment[item];
			if ((frag)&&(typeof frag == "object")&&(frag!=fragment.nodeRef)&&(frag!=fragment["tagName"])){
				comps = comps.concat(this.createComponents(frag, parentComp));
			}
		}
		return comps;
	}

	/*  parsePropertySets checks the top level of a raw JavaScript object
			structure for any propertySets.  It stores an array of references to 
			propertySets that it finds.
	*/
	this.parsePropertySets = function(fragment){
		return [];
		var propertySets = [];
		for(var item in fragment){
			if((fragment[item]["tagName"] == "dojo:propertyset")){
				propertySets.push(fragment[item]);
			}
		}
		// FIXME: should we store these propertySets somewhere for later retrieval
		this.propertySetsList.push(propertySets);
		return propertySets;
	}
	
	/*  parseProperties checks a raw JavaScript object structure for
			properties, and returns an array of properties that it finds.
	*/
	this.parseProperties = function(fragment){
		var properties = {};
		for(var item in fragment){
			// FIXME: need to check for undefined?
			// case: its a tagName or nodeRef
			if((fragment[item] == fragment["tagName"])||
				(fragment[item] == fragment.nodeRef)){
				// do nothing
			}else{
				if((fragment[item]["tagName"])&&
					(dojo.widget.tags[fragment[item].tagName.toLowerCase()])){
					// TODO: it isn't a property or property set, it's a fragment, 
					// so do something else
					// FIXME: needs to be a better/stricter check
					// TODO: handle xlink:href for external property sets
				}else if((fragment[item][0])&&(fragment[item][0].value!="")&&(fragment[item][0].value!=null)){
					try{
						// FIXME: need to allow more than one provider
						if(item.toLowerCase() == "dataprovider") {
							var _this = this;
							this.getDataProvider(_this, fragment[item][0].value);
							properties.dataProvider = this.dataProvider;
						}
						properties[item] = fragment[item][0].value;
						var nestedProperties = this.parseProperties(fragment[item]);
						// FIXME: this kind of copying is expensive and inefficient!
						for(var property in nestedProperties){
							properties[property] = nestedProperties[property];
						}
					}catch(e){ dojo.debug(e); }
				}
			}
		}
		return properties;
	}

	/* getPropertySetById returns the propertySet that matches the provided id
	*/
	
	this.getDataProvider = function(objRef, dataUrl){
		// FIXME: this is currently sync.  To make this async, we made need to move 
		//this step into the widget ctor, so that it is loaded when it is needed 
		// to populate the widget
		dojo.io.bind({
			url: dataUrl,
			load: function(type, evaldObj){
				if(type=="load"){
					objRef.dataProvider = evaldObj;
				}
			},
			mimetype: "text/javascript",
			sync: true
		});
	}

	
	this.getPropertySetById = function(propertySetId){
		for(var x = 0; x < this.propertySetsList.length; x++){
			if(propertySetId == this.propertySetsList[x]["id"][0].value){
				return this.propertySetsList[x];
			}
		}
		return "";
	}
	
	/* getPropertySetsByType returns the propertySet(s) that match(es) the
	 * provided componentClass
	 */
	this.getPropertySetsByType = function(componentType){
		var propertySets = [];
		for(var x=0; x < this.propertySetsList.length; x++){
			var cpl = this.propertySetsList[x];
			var cpcc = cpl["componentClass"]||cpl["componentType"]||null;
			var propertySetId = this.propertySetsList[x]["id"][0].value;
			if((cpcc)&&(propertySetId == cpcc[0].value)){
				propertySets.push(cpl);
			}
		}
		return propertySets;
	}
	
	/* getPropertySets returns the propertySet for a given component fragment
	*/
	this.getPropertySets = function(fragment){
		var ppl = "dojo:propertyproviderlist";
		var propertySets = [];
		var tagname = fragment["tagName"];
		if(fragment[ppl]){ 
			var propertyProviderIds = fragment[ppl].value.split(" ");
			// FIXME: should the propertyProviderList attribute contain #
			// 		  syntax for reference to ids or not?
			// FIXME: need a better test to see if this is local or external
			// FIXME: doesn't handle nested propertySets, or propertySets that
			// 		  just contain information about css documents, etc.
			for(var propertySetId in propertyProviderIds){
				if((propertySetId.indexOf("..")==-1)&&(propertySetId.indexOf("://")==-1)){
					// get a reference to a propertySet within the current parsed structure
					var propertySet = this.getPropertySetById(propertySetId);
					if(propertySet != ""){
						propertySets.push(propertySet);
					}
				}else{
					// FIXME: add code to parse and return a propertySet from
					// another document
					// alex: is this even necessaray? Do we care? If so, why?
				}
			}
		}
		// we put the typed ones first so that the parsed ones override when
		// iteration happens.
		return (this.getPropertySetsByType(tagname)).concat(propertySets);
	}
	
	/* 
		nodeRef is the node to be replaced... in the future, we might want to add 
		an alternative way to specify an insertion point

		componentName is the expected dojo widget name, i.e. Button of ContextMenu

		properties is an object of name value pairs
		namespace is the namespace of the widget.  Defaults to "dojo"
	*/
	this.createComponentFromScript = function(nodeRef, componentName, properties, namespace){
		if(!namespace){ namespace = "dojo"; }
		var ltn = namespace + ":" + componentName.toLowerCase();
		
		var djTags = dojo.widget.tags;
		//if we don't already have a tag registered for this widget, try to load it's
		//namespace, if it has one
		if(!djTags[ltn] && dojo.getNamespace && dojo.lang.isString(ltn)){		    
			var ns = dojo.getNamespace(namespace);
			if(ns){ns.load(componentName);}
		}
		
		if(!djTags[ltn]){
			dojo.deprecated('dojo.widget.Parse.createComponentFromScript', 'Widget not defined for namespace'+
				namespace +
				', so searching all namespaces. Developers should specify namespaces for all non-Dojo widgets', "0.5");						

			var newNs = dojo.findNamespaceForWidget(componentName.toLowerCase());
			if(newNs){
				var newLtn = newNs.nsPrefix + ":"+(ltn.indexOf(":")> 0 ? ltn.substring(ltn.indexOf(":")+1) : ltn);
				properties[newLtn]=properties[ltn];
				properties.namespace = newNs.nsPrefix;
				ltn = newLtn;
			}
		}
		
		if(djTags[ltn]){
			properties.fastMixIn = true;			
			//dojo.profile.start("dojo.widget.tags - "+ltn);
			//var ret = [dojo.widget.tags[ltn](properties, this, null, null, properties)];
			var ret = [dojo.widget.buildWidgetFromParseTree(ltn, properties, this, null, null, properties)];
			//dojo.profile.end("dojo.widget.tags - "+ltn);
			return ret;
		}else{
			dojo.debug("no tag handler registered for type: ", ltn);
		}

	}
}


dojo.widget._parser_collection = {"dojo": new dojo.widget.Parse() };
dojo.widget.getParser = function(name){
	if(!name){ name = "dojo"; }
	if(!this._parser_collection[name]){
		this._parser_collection[name] = new dojo.widget.Parse();
	}
	return this._parser_collection[name];
}

/**
 * Creates widget.
 *
 * @param name     The name of the widget to create with optional namespace prefix,
 *                 e.g."ns:widget", namespace defaults to "dojo".
 * @param props    Key-Value pairs of properties of the widget
 * @param refNode  If the position argument is specified, this node is used as
 *                 a reference for inserting this node into a DOM tree; else
 *                 the widget becomes the domNode
 * @param position The position to insert this widget's node relative to the
 *                 refNode argument
 * @return The new Widget object
 */

dojo.widget.createWidget = function(name, props, refNode, position){

	var isNode = false;
	var isNameStr = (typeof name == "string");
	if(isNameStr){
		var pos = name.indexOf(":");
		var namespace = (pos > -1) ? name.substring(0,pos) : "dojo";
		if(pos > -1){ name = name.substring(pos+1); }
		var lowerCaseName = name.toLowerCase();
		var namespacedName = namespace+":" + lowerCaseName;
		isNode = ( dojo.byId(name) && (!dojo.widget.tags[namespacedName]) ); 
	}

	if( (arguments.length == 1) && ((isNode)||(!isNameStr)) ){
		// we got a DOM node 
		var xp = new dojo.xml.Parse(); 
		// FIXME: we should try to find the parent! 
		var tn = (isNode) ? dojo.byId(name) : name; 
		return dojo.widget.getParser().createComponents(xp.parseElement(tn, null, true))[0]; 
	}

	function fromScript(placeKeeperNode, name, props, namespace){
		props[namespacedName] = { 
			dojotype: [{value: lowerCaseName}],
			nodeRef: placeKeeperNode,
			fastMixIn: true
		};
		props.namespace = namespace;
		return dojo.widget.getParser().createComponentFromScript(
			placeKeeperNode, name, props, namespace);
	}

	props = props||{};
	var notRef = false;
	var tn = null;
	var h = dojo.render.html.capable;
	if(h){
		tn = document.createElement("span");
	}
	if(!refNode){
		notRef = true;
		refNode = tn;
		if(h){
			dojo.body().appendChild(refNode);
		}
	}else if(position){
		dojo.dom.insertAtPosition(tn, refNode, position);
	}else{ // otherwise don't replace, but build in-place
		tn = refNode;
	}
	var widgetArray = fromScript(tn, name.toLowerCase(), props, namespace);
	if (!widgetArray || !widgetArray[0] || typeof widgetArray[0].widgetType == "undefined") {
		throw new Error("createWidget: Creation of \"" + name + "\" widget failed.");
	}
	if (notRef) {
		if (widgetArray[0].domNode.parentNode) {
			widgetArray[0].domNode.parentNode.removeChild(widgetArray[0].domNode);
		}
	}
	return widgetArray[0]; // just return the widget
}
