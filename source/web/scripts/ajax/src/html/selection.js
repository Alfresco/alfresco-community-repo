dojo.require("dojo.html.common");
dojo.provide("dojo.html.selection");

dojo.require("dojo.dom");
dojo.require("dojo.lang.common");

/**
 * type of selection
**/
dojo.html.selectionType = {
	NONE : 0,
	TEXT : 1,
	CONTROL : 2
};

dojo.html.clearSelection = function(){
	var _window = dojo.global();
	var _document = dojo.doc();
	try{
		if(_window["getSelection"]){ 
			if(dojo.render.html.safari){
				// pulled from WebCore/ecma/kjs_window.cpp, line 2536
				_window.getSelection().collapse();
			}else{
				_window.getSelection().removeAllRanges();
			}
		}else if(_document.selection){
			if(_document.selection.empty){
				_document.selection.empty();
			}else if(_document.selection.clear){
				_document.selection.clear();
			}
		}
		return true;
	}catch(e){
		dojo.debug(e);
		return false;
	}
}

dojo.html.disableSelection = function(element){
	element = dojo.byId(element)||dojo.body();
	var h = dojo.render.html;
	
	if(h.mozilla){
		element.style.MozUserSelect = "none";
	}else if(h.safari){
		element.style.KhtmlUserSelect = "none"; 
	}else if(h.ie){
		element.unselectable = "on";
	}else{
		return false;
	}
	return true;
}

dojo.html.enableSelection = function(element){
	element = dojo.byId(element)||dojo.body();
	
	var h = dojo.render.html;
	if(h.mozilla){ 
		element.style.MozUserSelect = ""; 
	}else if(h.safari){
		element.style.KhtmlUserSelect = "";
	}else if(h.ie){
		element.unselectable = "off";
	}else{
		return false;
	}
	return true;
}

dojo.html.selectElement = function(element){
	var _window = dojo.global();
	var _document = dojo.doc();
	element = dojo.byId(element);
	if(_document.selection && dojo.body().createTextRange){ // IE
		var range = dojo.body().createTextRange();
		range.moveToElementText(element);
		range.select();
	}else if(_window["getSelection"]){
		var selection = _window.getSelection();
		// FIXME: does this work on Safari?
		if(selection["selectAllChildren"]){ // Mozilla
			selection.selectAllChildren(element);
		}
	}
}

dojo.html.selectInputText = function(element){
	var _window = dojo.global();
	var _document = dojo.doc();
	element = dojo.byId(element);
	if(_document.selection && dojo.body().createTextRange){ // IE
		var range = element.createTextRange();
		range.moveStart("character", 0);
		range.moveEnd("character", element.value.length);
		range.select();
	}else if(_window["getSelection"]){
		var selection = _window.getSelection();
		// FIXME: does this work on Safari?
		element.setSelectionRange(0, element.value.length);
	}
	element.focus();
}


dojo.html.isSelectionCollapsed = function(){
	var _window = dojo.global();
	var _document = dojo.doc();
	if(_document["selection"]){ // IE
		return _document.selection.createRange().text == "";
	}else if(_window["getSelection"]){
		var selection = _window.getSelection();
		if(dojo.lang.isString(selection)){ // Safari
			return selection == "";
		}else{ // Mozilla/W3
			return selection.isCollapsed;
		}
	}
}

dojo.lang.mixin(dojo.html.selection, {
	getType: function() {
		// summary: Get the selection type (like document.select.type in IE).
		if(dojo.doc().selection){ //IE
		
			return dojo.html.selectionType[dojo.doc().selection.type.toUpperCase()];
		}else{
			var stype = dojo.html.selectionType.TEXT;
	
			// Check if the actual selection is a CONTROL (IMG, TABLE, HR, etc...).
			var oSel;
			try {oSel = dojo.global().getSelection();}
			catch (e) {}
			
			if(oSel && oSel.rangeCount==1){
				var oRange = oSel.getRangeAt(0);
				if (oRange.startContainer == oRange.endContainer && (oRange.endOffset - oRange.startOffset) == 1
					&& oRange.startContainer.nodeType != dojo.dom.TEXT_NODE) {
					stype = dojo.html.selectionType.CONTROL;
				}
			}
			return stype;
		}
	},
	getSelectedElement: function() {
		// summary: 
		//		Retrieves the selected element (if any), just in the case that a single
		//		element (object like and image or a table) is selected.
		if ( dojo.html.selection.getType() == dojo.html.selectionType.CONTROL ){
			if(dojo.doc().selection){ //IE
				var range = dojo.doc().selection.createRange();
		
				if ( range && range.item ){
					return dojo.doc().selection.createRange().item(0);
				}
			}else{
				var selection = dojo.global().getSelection();
				return selection.anchorNode.childNodes[ selection.anchorOffset ];
			}
		}
	},
	getParentElement: function() {
		// summary: 
		//		Get the parent element of the current selection
		if(dojo.html.selection.getType() == dojo.html.selectionType.CONTROL){
			var p = dojo.html.selection.getSelectedElement();
			if(p){ return p.parentNode; }
		}else{
			if(dojo.doc().selection){ //IE
				return dojo.doc().selection.createRange().parentElement();
			}else{
				var selection = dojo.global().getSelection();
				if(selection){
					var node = selection.anchorNode;
		
					while ( node && node.nodeType != dojo.dom.ELEMENT_NODE ){
						node = node.parentNode;
					}
		
					return node;
				}
			}
		}
	},
	hasAncestorElement: function(tagName /* ... */){
		return (dojo.html.selection.getAncestorElement.apply(this, arguments) != null);
	},
	getAncestorElement: function(tagName /* ... */){
		var node = dojo.html.selection.getSelectedElement() || dojo.html.selection.getParentElement();
		while(node /*&& node.tagName.toLowerCase() != 'body'*/){
			if(dojo.html.selection.isTag(node, arguments).length>0){
				return node;
			}
			node = node.parentNode;
		}
		return null;
	},
	//modified from dojo.html.isTag to take an array as second parameter
	isTag: function(node, tags) {
		if(node && node.tagName) {
			for (var i=0; i<tags.length; i++){
				if (node.tagName.toLowerCase()==String(tags[i]).toLowerCase()){
					return String(tags[i]).toLowerCase();
				}
			}
		}
		return "";
	},
	selectElement: function(element) {
		// summary: clear previous selection and select element
		var _window = dojo.global();
		var _document = dojo.doc();
		element = dojo.byId(element);
		if(_document.selection && dojo.body().createTextRange){ // IE
			try{
				var range = dojo.body().createControlRange();
				range.addElement(element);
				range.select();
			}catch(e){
				dojo.html.selection.selectElementChildren(element);
			}
		}else if(_window["getSelection"]){
			var selection = _window.getSelection();
			// FIXME: does this work on Safari?
			if(selection["removeAllRanges"]){ // Mozilla
				var range = _document.createRange() ;
				range.selectNode(element) ;
				selection.removeAllRanges() ;
				selection.addRange(range) ;
			}
		}
	},
	selectElementChildren: function(element){
		// summary: clear previous selection and select the content of the node
		var _window = dojo.global();
		var _document = dojo.doc();
		element = dojo.byId(element);
		if(_document.selection && dojo.body().createTextRange){ // IE
			var range = dojo.body().createTextRange();
			range.moveToElementText(element);
			range.select();
		}else if(_window["getSelection"]){
			var selection = _window.getSelection();
			// FIXME: does this work on Safari?
			if(selection["selectAllChildren"]){ // Mozilla
				selection.selectAllChildren(element);
			}
		}
	},
	collapse: function(beginning) {
		// summary: clear selection
		if(dojo.global().getSelection){
			var selection = dojo.global().getSelection();
			if(selection.removeAllRanges){ // Mozilla
				if(beginning){
					selection.collapseToStart();
				}else{
					selection.collapseToEnd();
				}
			}else{ // Safari
				// not a great deal we can do
			}
		}else if(dojo.doc().selection){ // IE
			var range = dojo.doc().selection.createRange();
			range.collapse(beginning);
			range.select();
		}
	},

	remove: function() {
		// summary: delete selection
		if(dojo.doc().selection) { //IE
			var oSel = dojo.doc().selection;

			if ( oSel.type.toUpperCase() != "NONE" ){
				oSel.clear();
			}
		
			return oSel;
		}else{
			var oSel = dojo.global().getSelection();

			for ( var i = 0; i < oSel.rangeCount; i++ ){
				oSel.getRangeAt(i).deleteContents();
			}
		
			return oSel;
		}
	}
});
