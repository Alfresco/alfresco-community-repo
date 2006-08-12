dojo.provide("dojo.xml.Parse");

dojo.require("dojo.dom");

//TODO: determine dependencies
// currently has dependency on dojo.xml.DomUtil nodeTypes constants...

/* generic method for taking a node and parsing it into an object

TODO: WARNING: This comment is wrong!

For example, the following xml fragment

<foo bar="bar">
	<baz xyzzy="xyzzy"/>
</foo>

can be described as:

dojo.???.foo = {}
dojo.???.foo.bar = {}
dojo.???.foo.bar.value = "bar";
dojo.???.foo.baz = {}
dojo.???.foo.baz.xyzzy = {}
dojo.???.foo.baz.xyzzy.value = "xyzzy"

*/
// using documentFragment nomenclature to generalize in case we don't want to require passing a collection of nodes with a single parent
dojo.xml.Parse = function(){

	function getDojoTagName(node){
		var tagName = node.tagName;
		if(dojo.render.html.capable && dojo.render.html.ie && node.scopeName != 'HTML'){
			tagName = node.scopeName + ':' + tagName;
		}
		if(tagName.substr(0,5).toLowerCase() == "dojo:"){
			return tagName.toLowerCase();
		}

		if(tagName.substr(0,4).toLowerCase() == "dojo"){
			// FIXME: this assumes tag names are always lower case
			return "dojo:" + tagName.substring(4).toLowerCase();
		}

		// allow lower-casing
		var djt = node.getAttribute("dojoType") || node.getAttribute("dojotype");
		if(djt){
			if(djt.indexOf(":")<0){
				djt = "dojo:"+djt;
			}
			return djt.toLowerCase();
		}
		
		if(node.getAttributeNS && node.getAttributeNS(dojo.dom.dojoml,"type")){
			return "dojo:" + node.getAttributeNS(dojo.dom.dojoml,"type").toLowerCase();
		}
		try{
			// FIXME: IE really really doesn't like this, so we squelch errors for it
			djt = node.getAttribute("dojo:type");
		}catch(e){ /* FIXME: log? */ }

		if(djt){ return "dojo:"+djt.toLowerCase(); }
	
		if(!dj_global["djConfig"] || !djConfig["ignoreClassNames"]){ 
			// FIXME: should we make this optionally enabled via djConfig?
			var classes = node.className||node.getAttribute("class");
			// FIXME: following line, without check for existence of classes.indexOf
			// breaks firefox 1.5's svg widgets
			if(classes && classes.indexOf && classes.indexOf("dojo-") != -1){
			    var aclasses = classes.split (" ");
			    for(var x=0; x<aclasses.length; x++){
			        if(aclasses[x].length > 5 && aclasses[x].indexOf("dojo-") >= 0){
			            return "dojo:"+aclasses[x].substr(5).toLowerCase(); 
					}
				}
			}
		}

		return tagName.toLowerCase();
	}

	this.parseElement = function(node, hasParentNodeSet, optimizeForDojoML, thisIdx){

		var parsedNodeSet = {};

		//There's a weird bug in IE where it counts end tags, e.g. </dojo:button> as nodes that should be parsed.  Ignore these
		if(node.tagName && node.tagName.indexOf("/") == 0){
			return null;
		}

		var tagName = getDojoTagName(node);
		parsedNodeSet[tagName] = [];
		if(tagName.substr(0,4).toLowerCase()=="dojo"){
			parsedNodeSet.namespace = "dojo";
		}else{
			var pos = tagName.indexOf(":");
			if(pos > 0){
			    parsedNodeSet.namespace = tagName.substring(0,pos);
			}
		}

		var process = false;
		if(!optimizeForDojoML){process=true;}
		else if(parsedNodeSet.namespace&&dojo.getNamespace(parsedNodeSet.namespace)){process=true;}
		else if(dojo.widget.tags[tagName]){
			dojo.deprecated('dojo.xml.Parse.parseElement', 'Widgets should be placed in a defined namespace', "0.5");
			process = true;	
		}
		
		if(process){
			var attributeSet = this.parseAttributes(node);
			for(var attr in attributeSet){
				if((!parsedNodeSet[tagName][attr])||(typeof parsedNodeSet[tagName][attr] != "array")){
					parsedNodeSet[tagName][attr] = [];
				}
				parsedNodeSet[tagName][attr].push(attributeSet[attr]);
			}	
			// FIXME: we might want to make this optional or provide cloning instead of
			// referencing, but for now, we include a node reference to allow
			// instantiated components to figure out their "roots"
			parsedNodeSet[tagName].nodeRef = node;
			parsedNodeSet.tagName = tagName;
			parsedNodeSet.index = thisIdx||0;
			//    dojo.debug("parseElement: set the element tagName = "+parsedNodeSet.tagName+" and namespace to "+parsedNodeSet.namespace);
		}

		var count = 0;
		for(var i = 0; i < node.childNodes.length; i++){
			var tcn = node.childNodes.item(i);
			switch(tcn.nodeType){
				case  dojo.dom.ELEMENT_NODE: // element nodes, call this function recursively
					count++;
					var ctn = getDojoTagName(tcn);
					if(!parsedNodeSet[ctn]){
						parsedNodeSet[ctn] = [];
					}
					parsedNodeSet[ctn].push(this.parseElement(tcn, true, optimizeForDojoML, count));
					if(	(tcn.childNodes.length == 1)&&
						(tcn.childNodes.item(0).nodeType == dojo.dom.TEXT_NODE)){
						parsedNodeSet[ctn][parsedNodeSet[ctn].length-1].value = tcn.childNodes.item(0).nodeValue;
					}
					break;
				case  dojo.dom.TEXT_NODE: // if a single text node is the child, treat it as an attribute
					if(node.childNodes.length == 1){
						parsedNodeSet[tagName].push({ value: node.childNodes.item(0).nodeValue });
					}
					break;
				default: break;
				/*
				case  dojo.dom.ATTRIBUTE_NODE: // attribute node... not meaningful here
					break;
				case  dojo.dom.CDATA_SECTION_NODE: // cdata section... not sure if this would ever be meaningful... might be...
					break;
				case  dojo.dom.ENTITY_REFERENCE_NODE: // entity reference node... not meaningful here
					break;
				case  dojo.dom.ENTITY_NODE: // entity node... not sure if this would ever be meaningful
					break;
				case  dojo.dom.PROCESSING_INSTRUCTION_NODE: // processing instruction node... not meaningful here
					break;
				case  dojo.dom.COMMENT_NODE: // comment node... not not sure if this would ever be meaningful 
					break;
				case  dojo.dom.DOCUMENT_NODE: // document node... not sure if this would ever be meaningful
					break;
				case  dojo.dom.DOCUMENT_TYPE_NODE: // document type node... not meaningful here
					break;
				case  dojo.dom.DOCUMENT_FRAGMENT_NODE: // document fragment node... not meaningful here
					break;
				case  dojo.dom.NOTATION_NODE:// notation node... not meaningful here
					break;
				*/
			}
		}
		//return (hasParentNodeSet) ? parsedNodeSet[node.tagName] : parsedNodeSet;
		//if(parsedNodeSet.tagName)dojo.debug("parseElement: RETURNING NODE WITH TAGNAME "+parsedNodeSet.tagName);
		return parsedNodeSet;
	};

	/* parses a set of attributes on a node into an object tree */
	this.parseAttributes = function(node){
		var parsedAttributeSet = {};
		var atts = node.attributes;
		// TODO: should we allow for duplicate attributes at this point...
		// would any of the relevant dom implementations even allow this?
		var attnode, i=0;
		while((attnode=atts[i++])){
			if((dojo.render.html.capable)&&(dojo.render.html.ie)){
				if(!attnode){ continue; }
				if(	(typeof attnode == "object")&&
					(typeof attnode.nodeValue == 'undefined')||
					(attnode.nodeValue == null)||
					(attnode.nodeValue == '')){ 
					continue; 
				}
			}

			var nn = attnode.nodeName.split(":");
			nn = (nn.length == 2) ? nn[1] : attnode.nodeName;
						
			parsedAttributeSet[nn] = { 
				value: attnode.nodeValue 
			};
		}
		return parsedAttributeSet;
	};
};
