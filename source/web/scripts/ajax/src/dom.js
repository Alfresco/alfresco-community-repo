dojo.provide("dojo.dom");

dojo.dom.ELEMENT_NODE                  = 1;
dojo.dom.ATTRIBUTE_NODE                = 2;
dojo.dom.TEXT_NODE                     = 3;
dojo.dom.CDATA_SECTION_NODE            = 4;
dojo.dom.ENTITY_REFERENCE_NODE         = 5;
dojo.dom.ENTITY_NODE                   = 6;
dojo.dom.PROCESSING_INSTRUCTION_NODE   = 7;
dojo.dom.COMMENT_NODE                  = 8;
dojo.dom.DOCUMENT_NODE                 = 9;
dojo.dom.DOCUMENT_TYPE_NODE            = 10;
dojo.dom.DOCUMENT_FRAGMENT_NODE        = 11;
dojo.dom.NOTATION_NODE                 = 12;
	
dojo.dom.dojoml = "http://www.dojotoolkit.org/2004/dojoml";

/**
 *	comprehensive list of XML namespaces
**/
dojo.dom.xmlns = {
	svg : "http://www.w3.org/2000/svg",
	smil : "http://www.w3.org/2001/SMIL20/",
	mml : "http://www.w3.org/1998/Math/MathML",
	cml : "http://www.xml-cml.org",
	xlink : "http://www.w3.org/1999/xlink",
	xhtml : "http://www.w3.org/1999/xhtml",
	xul : "http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul",
	xbl : "http://www.mozilla.org/xbl",
	fo : "http://www.w3.org/1999/XSL/Format",
	xsl : "http://www.w3.org/1999/XSL/Transform",
	xslt : "http://www.w3.org/1999/XSL/Transform",
	xi : "http://www.w3.org/2001/XInclude",
	xforms : "http://www.w3.org/2002/01/xforms",
	saxon : "http://icl.com/saxon",
	xalan : "http://xml.apache.org/xslt",
	xsd : "http://www.w3.org/2001/XMLSchema",
	dt: "http://www.w3.org/2001/XMLSchema-datatypes",
	xsi : "http://www.w3.org/2001/XMLSchema-instance",
	rdf : "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
	rdfs : "http://www.w3.org/2000/01/rdf-schema#",
	dc : "http://purl.org/dc/elements/1.1/",
	dcq: "http://purl.org/dc/qualifiers/1.0",
	"soap-env" : "http://schemas.xmlsoap.org/soap/envelope/",
	wsdl : "http://schemas.xmlsoap.org/wsdl/",
	AdobeExtensions : "http://ns.adobe.com/AdobeSVGViewerExtensions/3.0/"
};

dojo.dom.isNode = function(wh){
	if(typeof Element == "function") {
		try {
			return wh instanceof Element;
		} catch(E) {}
	} else {
		// best-guess
		return wh && !isNaN(wh.nodeType);
	}
}

dojo.dom.getUniqueId = function(){
	var _document = dojo.doc();
	do {
		var id = "dj_unique_" + (++arguments.callee._idIncrement);
	}while(_document.getElementById(id));
	return id;
}
dojo.dom.getUniqueId._idIncrement = 0;

dojo.dom.firstElement = dojo.dom.getFirstChildElement = function(parentNode, tagName){
	var node = parentNode.firstChild;
	while(node && node.nodeType != dojo.dom.ELEMENT_NODE){
		node = node.nextSibling;
	}
	if(tagName && node && node.tagName && node.tagName.toLowerCase() != tagName.toLowerCase()) {
		node = dojo.dom.nextElement(node, tagName);
	}
	return node;
}

dojo.dom.lastElement = dojo.dom.getLastChildElement = function(parentNode, tagName){
	var node = parentNode.lastChild;
	while(node && node.nodeType != dojo.dom.ELEMENT_NODE) {
		node = node.previousSibling;
	}
	if(tagName && node && node.tagName && node.tagName.toLowerCase() != tagName.toLowerCase()) {
		node = dojo.dom.prevElement(node, tagName);
	}
	return node;
}

dojo.dom.nextElement = dojo.dom.getNextSiblingElement = function(node, tagName){
	if(!node) { return null; }
	do {
		node = node.nextSibling;
	} while(node && node.nodeType != dojo.dom.ELEMENT_NODE);

	if(node && tagName && tagName.toLowerCase() != node.tagName.toLowerCase()) {
		return dojo.dom.nextElement(node, tagName);
	}
	return node;
}

dojo.dom.prevElement = dojo.dom.getPreviousSiblingElement = function(node, tagName){
	if(!node) { return null; }
	if(tagName) { tagName = tagName.toLowerCase(); }
	do {
		node = node.previousSibling;
	} while(node && node.nodeType != dojo.dom.ELEMENT_NODE);

	if(node && tagName && tagName.toLowerCase() != node.tagName.toLowerCase()) {
		return dojo.dom.prevElement(node, tagName);
	}
	return node;
}

// TODO: hmph
/*this.forEachChildTag = function(node, unaryFunc) {
	var child = this.getFirstChildTag(node);
	while(child) {
		if(unaryFunc(child) == "break") { break; }
		child = this.getNextSiblingTag(child);
	}
}*/

dojo.dom.moveChildren = function(srcNode, destNode, trim){
	var count = 0;
	if(trim) {
		while(srcNode.hasChildNodes() &&
			srcNode.firstChild.nodeType == dojo.dom.TEXT_NODE) {
			srcNode.removeChild(srcNode.firstChild);
		}
		while(srcNode.hasChildNodes() &&
			srcNode.lastChild.nodeType == dojo.dom.TEXT_NODE) {
			srcNode.removeChild(srcNode.lastChild);
		}
	}
	while(srcNode.hasChildNodes()){
		destNode.appendChild(srcNode.firstChild);
		count++;
	}
	return count;
}

dojo.dom.copyChildren = function(srcNode, destNode, trim){
	var clonedNode = srcNode.cloneNode(true);
	return this.moveChildren(clonedNode, destNode, trim);
}

dojo.dom.removeChildren = function(node){
	var count = node.childNodes.length;
	while(node.hasChildNodes()){ node.removeChild(node.firstChild); }
	return count;
}

dojo.dom.replaceChildren = function(node, newChild){
	// FIXME: what if newChild is an array-like object?
	dojo.dom.removeChildren(node);
	node.appendChild(newChild);
}

dojo.dom.removeNode = function(node){
	if(node && node.parentNode){
		// return a ref to the removed child
		return node.parentNode.removeChild(node);
	}
}

dojo.dom.getAncestors = function(node, filterFunction, returnFirstHit) {
	var ancestors = [];
	var isFunction = (filterFunction && (filterFunction instanceof Function || typeof filterFunction == "function"));
	while(node) {
		if (!isFunction || filterFunction(node)) {
			ancestors.push(node);
		}
		if (returnFirstHit && ancestors.length > 0) { return ancestors[0]; }
		
		node = node.parentNode;
	}
	if (returnFirstHit) { return null; }
	return ancestors;
}

dojo.dom.getAncestorsByTag = function(node, tag, returnFirstHit) {
	tag = tag.toLowerCase();
	return dojo.dom.getAncestors(node, function(el){
		return ((el.tagName)&&(el.tagName.toLowerCase() == tag));
	}, returnFirstHit);
}

dojo.dom.getFirstAncestorByTag = function(node, tag) {
	return dojo.dom.getAncestorsByTag(node, tag, true);
}

dojo.dom.isDescendantOf = function(node, ancestor, guaranteeDescendant){
	// guaranteeDescendant allows us to be a "true" isDescendantOf function
	if(guaranteeDescendant && node) { node = node.parentNode; }
	while(node) {
		if(node == ancestor){ return true; }
		node = node.parentNode;
	}
	return false;
}

dojo.dom.innerXML = function(node){
	if(node.innerXML){
		return node.innerXML;
	}else if (node.xml){
		return node.xml;
	}else if(typeof XMLSerializer != "undefined"){
		return (new XMLSerializer()).serializeToString(node);
	}
}

dojo.dom.createDocument = function(){
	var doc = null;
	var _document = dojo.doc();

	if(!dj_undef("ActiveXObject")){
		var prefixes = [ "MSXML2", "Microsoft", "MSXML", "MSXML3" ];
		for(var i = 0; i<prefixes.length; i++){
			try{
				doc = new ActiveXObject(prefixes[i]+".XMLDOM");
			}catch(e){ /* squelch */ };

			if(doc){ break; }
		}
	}else if((_document.implementation)&&
		(_document.implementation.createDocument)){
		doc = _document.implementation.createDocument("", "", null);
	}
	
	return doc;
}

dojo.dom.createDocumentFromText = function(str, mimetype){
	if(!mimetype){ mimetype = "text/xml"; }
	if(!dj_undef("DOMParser")){
		var parser = new DOMParser();
		return parser.parseFromString(str, mimetype);
	}else if(!dj_undef("ActiveXObject")){
		var domDoc = dojo.dom.createDocument();
		if(domDoc){
			domDoc.async = false;
			domDoc.loadXML(str);
			return domDoc;
		}else{
			dojo.debug("toXml didn't work?");
		}
	/*
	}else if((dojo.render.html.capable)&&(dojo.render.html.safari)){
		// FIXME: this doesn't appear to work!
		// from: http://web-graphics.com/mtarchive/001606.php
		// var xml = '<?xml version="1.0"?>'+str;
		var mtype = "text/xml";
		var xml = '<?xml version="1.0"?>'+str;
		var url = "data:"+mtype+";charset=utf-8,"+encodeURIComponent(xml);
		var req = new XMLHttpRequest();
		req.open("GET", url, false);
		req.overrideMimeType(mtype);
		req.send(null);
		return req.responseXML;
	*/
	}else{
		var _document = dojo.doc();
		if(_document.createElement){
			// FIXME: this may change all tags to uppercase!
			var tmp = _document.createElement("xml");
			tmp.innerHTML = str;
			if(_document.implementation && _document.implementation.createDocument) {
				var xmlDoc = _document.implementation.createDocument("foo", "", null);
				for(var i = 0; i < tmp.childNodes.length; i++) {
					xmlDoc.importNode(tmp.childNodes.item(i), true);
				}
				return xmlDoc;
			}
			// FIXME: probably not a good idea to have to return an HTML fragment
			// FIXME: the tmp.doc.firstChild is as tested from IE, so it may not
			// work that way across the board
			return ((tmp.document)&&
				(tmp.document.firstChild ?  tmp.document.firstChild : tmp));
		}
	}
	return null;
}

dojo.dom.prependChild = function(node, parent) {
	if(parent.firstChild) {
		parent.insertBefore(node, parent.firstChild);
	} else {
		parent.appendChild(node);
	}
	return true;
}

dojo.dom.insertBefore = function(node, ref, force){
	if (force != true &&
		(node === ref || node.nextSibling === ref)){ return false; }
	var parent = ref.parentNode;
	parent.insertBefore(node, ref);
	return true;
}

dojo.dom.insertAfter = function(node, ref, force){
	var pn = ref.parentNode;
	if(ref == pn.lastChild){
		if((force != true)&&(node === ref)){
			return false;
		}
		pn.appendChild(node);
	}else{
		return this.insertBefore(node, ref.nextSibling, force);
	}
	return true;
}

dojo.dom.insertAtPosition = function(node, ref, position){
	if((!node)||(!ref)||(!position)){ return false; }
	switch(position.toLowerCase()){
		case "before":
			return dojo.dom.insertBefore(node, ref);
		case "after":
			return dojo.dom.insertAfter(node, ref);
		case "first":
			if(ref.firstChild){
				return dojo.dom.insertBefore(node, ref.firstChild);
			}else{
				ref.appendChild(node);
				return true;
			}
			break;
		default: // aka: last
			ref.appendChild(node);
			return true;
	}
}

dojo.dom.insertAtIndex = function(node, containingNode, insertionIndex){
	var siblingNodes = containingNode.childNodes;

	// if there aren't any kids yet, just add it to the beginning

	if (!siblingNodes.length){
		containingNode.appendChild(node);
		return true;
	}

	// otherwise we need to walk the childNodes
	// and find our spot

	var after = null;

	for(var i=0; i<siblingNodes.length; i++){

		var sibling_index = siblingNodes.item(i)["getAttribute"] ? parseInt(siblingNodes.item(i).getAttribute("dojoinsertionindex")) : -1;

		if (sibling_index < insertionIndex){
			after = siblingNodes.item(i);
		}
	}

	if (after){
		// add it after the node in {after}

		return dojo.dom.insertAfter(node, after);
	}else{
		// add it to the start

		return dojo.dom.insertBefore(node, siblingNodes.item(0));
	}
}
	
/**
 * implementation of the DOM Level 3 attribute.
 * 
 * @param node The node to scan for text
 * @param text Optional, set the text to this value.
 */
dojo.dom.textContent = function(node, text){
	if (arguments.length>1) {
		var _document = dojo.doc();
		dojo.dom.replaceChildren(node, _document.createTextNode(text));
		return text;
	} else {
		if(node.textContent != undefined){ //FF 1.5
			return node.textContent;
		}
		var _result = "";
		if (node == null) { return _result; }
		for (var i = 0; i < node.childNodes.length; i++) {
			switch (node.childNodes[i].nodeType) {
				case 1: // ELEMENT_NODE
				case 5: // ENTITY_REFERENCE_NODE
					_result += dojo.dom.textContent(node.childNodes[i]);
					break;
				case 3: // TEXT_NODE
				case 2: // ATTRIBUTE_NODE
				case 4: // CDATA_SECTION_NODE
					_result += node.childNodes[i].nodeValue;
					break;
				default:
					break;
			}
		}
		return _result;
	}
}

dojo.dom.hasParent = function (node) {
	return node && node.parentNode && dojo.dom.isNode(node.parentNode);
}

/**
 * Determines if node has any of the provided tag names and
 * returns the tag name that matches, empty string otherwise.
 *
 * Examples:
 *
 * myFooNode = <foo />
 * isTag(myFooNode, "foo"); // returns "foo"
 * isTag(myFooNode, "bar"); // returns ""
 * isTag(myFooNode, "FOO"); // returns ""
 * isTag(myFooNode, "hey", "foo", "bar"); // returns "foo"
**/
dojo.dom.isTag = function(node /* ... */) {
	if(node && node.tagName) {
		for(var i=1; i<arguments.length; i++){
			if(node.tagName==String(arguments[i])){
				return String(arguments[i]);
			}
		}
	}
	return "";
}

/** 
 * Implements DOM Level 2 setAttributeNS so it works cross browser.
 *
 * Example:
 * dojo.dom.setAttributeNS(domElem, "http://foobar.com/2006/someSpec", 
 * 							"hs:level", 3);
 */
dojo.dom.setAttributeNS = function(elem, namespaceURI, attrName, attrValue){
	if(elem == null || ((elem == undefined)&&(typeof elem == "undefined"))){
		dojo.raise("No element given to dojo.dom.setAttributeNS");
	}
	
	if(!((elem.setAttributeNS == undefined)&&(typeof elem.setAttributeNS == "undefined"))){ // w3c
		elem.setAttributeNS(namespaceURI, attrName, attrValue);
	}else{ // IE
		// get a root XML document
		var ownerDoc = elem.ownerDocument;
		var attribute = ownerDoc.createNode(
			2, // node type
			attrName,
			namespaceURI
		);
		
		// set value
		attribute.nodeValue = attrValue;
		
		// attach to element
		elem.setAttributeNode(attribute);
	}
}
