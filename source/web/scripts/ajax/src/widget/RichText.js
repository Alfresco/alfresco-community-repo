 /* -*- tab-width: 4 -*- */
dojo.provide("dojo.widget.RichText");

dojo.require("dojo.widget.*");
dojo.require("dojo.html.*");
dojo.require("dojo.html.layout");
dojo.require("dojo.event.*");
dojo.require("dojo.string.extras");

// used to save content
if(dojo.hostenv.post_load_){
	(function(){
		var savetextarea = dojo.doc().createElement('textarea');
		savetextarea.id = "dojo.widget.RichText.savedContent";
		savetextarea.style = "display:none;position:absolute;top:-100px;left:-100px;height:3px;width:3px;overflow:hidden;";
		dojo.body().appendChild(savetextarea);
	})();
}else{
	//dojo.body() is not available before onLoad is fired
	try {
		dojo.doc().write('<textarea id="dojo.widget.RichText.savedContent" ' +
			'style="display:none;position:absolute;top:-100px;left:-100px;height:3px;width:3px;overflow:hidden;"></textarea>');
	}catch(e){ }
}

dojo.widget.defineWidget(
	"dojo.widget.RichText",
	dojo.widget.HtmlWidget,
	{
		/** whether to inherit the parent's width or simply use 100% */
		inheritWidth: false,
		focusOnLoad: true,
		
		/**
		 * If a save name is specified the content is saved and restored if the
		 * editor is not properly closed after editing has started.
		 */
		saveName: "",
		_content: "",
		
		/* set height to fix the editor at a specific height, with scrolling */
		height: null,

		/** The minimum height that the editor should have */
		minHeight: "1em",
		
		isClosed: true,
		isLoaded: false,
		
		/** whether to use the active-x object in IE */
		useActiveX: false,

		/*whether to use relative URLs for images - if this is enabled
		images will be given absolute URLs when inside the editor but
		will be changed to use relative URLs (to the current page) on save
		*/
		relativeImageUrls: false,
		
		_SEPARATOR: "@@**%%__RICHTEXTBOUNDRY__%%**@@",

		/*
		defaultContentCleaner: function(content){
			if(!dojo.render.html.ie){
				return content;
			}

			content = content.replace(/\x20/g, " ");
			// alert(content);
			return content;
		},
		*/

	/* Init
	 *******/

		fillInTemplate: function(){
			this.open();

			// add the formatting functions
			var funcs = ["queryCommandEnabled", "queryCommandState",
				"queryCommandValue", "execCommand"];
			for(var i = 0; i < funcs.length; i++){
				dojo.event.connect("around", this, funcs[i], this, "_normalizeCommand");
			}
			
			// backwards compatibility, needs to be removed
			dojo.event.connect(this, "onKeyPressed", this, "afterKeyPress");
			dojo.event.connect(this, "onKeyPress", this, "keyPress");
			dojo.event.connect(this, "onKeyDown", this, "keyDown");
			dojo.event.connect(this, "onKeyUp", this, "keyUp");

			// add default some key handlers		
			var ctrl = this.KEY_CTRL;
			var exec = function (cmd, arg) {
				return arguments.length == 1 ? function () { this.execCommand(cmd); } :
					function () { this.execCommand(cmd, arg); }
			}
				
			this.addKeyHandler("b", ctrl, exec("bold"));
			this.addKeyHandler("i", ctrl, exec("italic"));
			this.addKeyHandler("u", ctrl, exec("underline"));
			this.addKeyHandler("a", ctrl, exec("selectall"));
			//this.addKeyHandler("k", ctrl, exec("createlink", ""));
			//this.addKeyHandler("K", ctrl, exec("unlink"));
			this.addKeyHandler("s", ctrl, function () { this.save(true); });
			
			this.addKeyHandler("1", ctrl, exec("formatblock", "h1"));
			this.addKeyHandler("2", ctrl, exec("formatblock", "h2"));
			this.addKeyHandler("3", ctrl, exec("formatblock", "h3"));
			this.addKeyHandler("4", ctrl, exec("formatblock", "h4"));
					
			this.addKeyHandler("\\", ctrl, exec("insertunorderedlist"));
			if(!dojo.render.html.ie){
				this.addKeyHandler("Z", ctrl, exec("redo"));
			}
		},


		events: ["onBlur", "onFocus", "onKeyPress", "onKeyDown", "onKeyUp", "onClick"],

		/**
		 * Transforms the node referenced in this.domNode into a rich text editing
		 * node. This can result in the creation and replacement with an <iframe> if
		 * designMode is used, an <object> and active-x component if inside of IE or
		 * a reguler element if contentEditable is available.
		 */
		open: function (element) {
			var h = dojo.render.html;
			dojo.event.topic.publish("dojo.widget.RichText::open", this);

			if (!this.isClosed) { this.close(); }
			this._content = "";
			if((arguments.length == 1)&&(element["nodeName"])){ this.domNode = element; } // else unchanged

			if(	(this.domNode["nodeName"])&&
				(this.domNode.nodeName.toLowerCase() == "textarea")){
				this.textarea = this.domNode;
				var html = dojo.string.trim(this.textarea.value);
				if(html == ""){ html = "&nbsp;"; }
				this.domNode = document.createElement("div");
				var tmpFunc = dojo.lang.hitch(this, function(){
					with(this.textarea.style){
						display = "block";
						position = "absolute";
						width = "1px";
						height = "1px";
						border = margin = padding = "0px";
						// border = "3px solid black";
						left = top = "-50px";
						// visiblity = "hidden";
						if(h.ie){
							overflow = "hidden";
						}
					}
				});
				if(h.ie){ 
					setTimeout(tmpFunc, 10); 
				}else{
					tmpFunc();
				}
				if(!h.safari){
					// FIXME: VERY STRANGE safari 2.0.4 behavior here caused by
					// moving the textarea. Often crashed the browser!!! Seems
					// fixed on webkit nightlies.
					dojo.html.insertBefore(this.domNode, this.textarea);
				}
				// this.domNode.innerHTML = html;
				
				if(this.textarea.form){
					dojo.event.connect(this.textarea.form, "onsubmit", 
						// FIXME: should we be calling close() here instead?
						dojo.lang.hitch(this, function(){
							this.textarea.value = this.getEditorContent();
						})
					);
				}
				
				// dojo plucks our original domNode from the document so we need
				// to go back and put ourselves back in
				var editor = this;
				dojo.event.connect(this, "postCreate", function (){
					dojo.html.insertAfter(editor.textarea, editor.domNode);
				});
			}else{
				var html = this._preFilterContent(dojo.string.trim(this.domNode.innerHTML));
				if(html == ""){ html = "&nbsp;"; }
			}
			
			var content = dojo.html.getContentBox(this.domNode);
			this._oldHeight = content.height;
			this._oldWidth = content.width;

			this._firstChildContributingMargin = this._getContributingMargin(this.domNode, "top");
			this._lastChildContributingMargin = this._getContributingMargin(this.domNode, "bottom");

			this.savedContent = document.createElement("div");
			while (this.domNode.hasChildNodes()) {
				this.savedContent.appendChild(this.domNode.firstChild);
			}
			
			// If we're a list item we have to put in a blank line to force the
			// bullet to nicely align at the top of text
			if(	(this.domNode["nodeName"])&&
				(this.domNode.nodeName == "LI")){
				this.domNode.innerHTML = " <br>";
			}
					
			if(this.saveName != ""){
				var saveTextarea = document.getElementById("dojo.widget.RichText.savedContent");
				if (saveTextarea.value != "") {
					var datas = saveTextarea.value.split(this._SEPARATOR);
					for (var i = 0; i < datas.length; i++) {
						var data = datas[i].split(":");
						if (data[0] == this.saveName) {
							html = data[1];
							datas.splice(i, 1);
							break;
						}
					}				
				}
				dojo.event.connect("before", window, "onunload", this, "_saveContent");
				// dojo.event.connect(window, "onunload", this, "_saveContent");
			}

			if(h.ie70 && this.useActiveX){
				dojo.debug("activeX in ie70 is not currently supported, useActiveX is ignored for now.");
				this.useActiveX = false;
			}
			// Safari's selections go all out of whack if we do it inline,
			// so for now IE is our only hero
			//if (typeof document.body.contentEditable != "undefined") {
			if (this.useActiveX && h.ie) { // active-x
				this._drawObject(html);
				// dojo.debug(this.object.document);
			} else if (h.ie) { // contentEditable, easy
				this.editNode = document.createElement("div");
				with (this.editNode) {
					contentEditable = true;
					if(h.ie70){
						if(this.height){
							style.height = this.height;
						}
						if(this.minHeight){
							style.minHeight = this.minHeight;
						}
					}else{
						style["height"] = this.height ? this.height : this.minHeight;
					}
				}
				this.window = window;
				this.document = document;

				// FIXME: setting contentEditable on switches this element to
				// IE's hasLayout mode, triggering weird margin collapsing
				// behavior. It's particularly bad if the element you're editing
				// contains childnodes that don't have margin: defined in local
				// css rules. It would be nice if it was possible to hack around
				// this. Sadly _firstChildContributingMargin and 
				// _lastChildContributingMargin don't work on IE unless all
				// elements have margins set in CSS :-(

				this.domNode.appendChild(this.editNode);

				//if the normal way fails, we try the hard way to get the list
//				this.editNode.style.display = "none";
				if(!this._cacheLocalBlockFormatNames()){
					//in the array below, ul can not come directly after ol, otherwise the queryCommandValue returns Normal for it
					var formats = ['p', 'pre', 'address', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'ol', 'div', 'ul'];
					var localhtml = "";
					for(var i in formats){
						if(formats[i].charAt(1) != 'l'){
							localhtml += "<"+formats[i]+"><span>content</span></"+formats[i]+">";
						}else{
							localhtml += "<"+formats[i]+"><li>content</li></"+formats[i]+">";
						}
					}
					//queryCommandValue returns empty if we hide editNode, so move it out of screen temporary
					with(this.editNode.style){
						position = "absolute";
						left = "-2000px";
						top = "-2000px";
					}
					this.editNode.innerHTML = localhtml;
					var node = this.editNode.firstChild;
					while(node){
						dojo.html.selection.selectElement(node.firstChild);
						var nativename = node.tagName.toLowerCase();
						this._local2NativeFormatNames[nativename] = this.queryCommandValue("formatblock");
						dojo.debug([nativename,this._local2NativeFormatNames[nativename]]);
						this._native2LocalFormatNames[this._local2NativeFormatNames[nativename]] = nativename;
						node = node.nextSibling;
					}
					with(this.editNode.style){
						position = "";
						left = "";
						top = "";
					}
				}

				this.editNode.innerHTML = html;
				if(this.height){ this.editNode.style.overflowY="scroll"; }

				dojo.lang.forEach(this.events, function(e){
					dojo.event.connect(this.editNode, e.toLowerCase(), this, e);
				}, this);
				
				this.onLoad();
			} else { // designMode in iframe
				this._drawIframe(html);
			}

			// TODO: this is a guess at the default line-height, kinda works
			if (this.domNode.nodeName == "LI") { this.domNode.lastChild.style.marginTop = "-1.2em"; }
			dojo.html.addClass(this.domNode, "RichTextEditable");
			
			this.isClosed = false;
		},

		_hasCollapseableMargin: function(element, side) {
			// check if an element has padding or borders on the given side
			// which would prevent it from collapsing margins
			if (dojo.html.getPixelValue(element, 
										 'border-'+side+'-width', 
										 false)) {
				return false;
			} else if (dojo.html.getPixelValue(element, 
												'padding-'+side,
												false)) {
				return false;
			} else {
				return true;
			}
		},

		_getContributingMargin:	function(element, topOrBottom) {
			// calculate how much margin this element and its first or last
			// child are contributing to the total margin between this element
			// and the adjacent node. CSS border collapsing makes this
			// necessary.

			if (topOrBottom == "top") {
				var siblingAttr = "previousSibling";
				var childSiblingAttr = "nextSibling";
				var childAttr = "firstChild";
				var marginProp = "margin-top";
				var siblingMarginProp = "margin-bottom";
			} else {
				var siblingAttr = "nextSibling";
				var childSiblingAttr = "previousSibling";
				var childAttr = "lastChild";
				var marginProp = "margin-bottom";
				var siblingMarginProp = "margin-top";
			}

			var elementMargin = dojo.html.getPixelValue(element, marginProp, false);

			function isSignificantNode(element) {
				// see if an node is significant in the current context
				// for calulating margins
				return !(element.nodeType==3 && dojo.string.isBlank(element.data)) 
					&& dojo.html.getStyle(element, "display") != "none" 
					&& !dojo.html.isPositionAbsolute(element);
			}

			// walk throuh first/last children to find total collapsed margin size
			var childMargin = 0;
			var child = element[childAttr];
			while (child) {
				// skip over insignificant elements (whitespace, etc)
				while ((!isSignificantNode(child)) && child[childSiblingAttr]) {
					child = child[childSiblingAttr];
				}
						  
				childMargin = Math.max(childMargin, dojo.html.getPixelValue(child, marginProp, false));
				// stop if we hit a bordered/padded element
				if (!this._hasCollapseableMargin(child, topOrBottom)) break;
				child = child[childAttr];								   
			}

			// if this element has a border, return full child margin immediately
			// as there won't be any margin collapsing
			if (!this._hasCollapseableMargin(element, topOrBottom)){ return parseInt(childMargin); }

			// find margin supplied by nearest sibling
			var contextMargin = 0;
			var sibling = element[siblingAttr];
			while (sibling) {
				if (isSignificantNode(sibling)) {
					contextMargin = dojo.html.getPixelValue(sibling, 
															 siblingMarginProp, 
															 false);
					break;
				}
				sibling = sibling[siblingAttr];
			}
			if (!sibling) { // no sibling, look at parent's margin instead
				contextMargin = dojo.html.getPixelValue(element.parentNode, 
												marginProp, false);
			}

			if (childMargin > elementMargin) {
				return parseInt(Math.max((childMargin-elementMargin)-contextMargin, 0));
			} else {
				return 0;
			}
			
		},
		
		/** Draws an iFrame using the existing one if one exists. 
			Used by Mozilla, Safari, and Opera */
		_drawIframe: function (html) {

			// detect firefox < 1.5, which has some iframe loading issues
			var oldMoz = Boolean(dojo.render.html.moz && (
									typeof window.XML == 'undefined'))

			if (!this.iframe) {
				var currentDomain = (new dojo.uri.Uri(document.location)).host;
				this.iframe = document.createElement("iframe");
				with (this.iframe) {
					scrolling = this.height ? "auto" : "no";
					style.border = "none";
					style.lineHeight = "0"; // squash line height
					style.verticalAlign = "bottom";
				}
			}
			// opera likes this to be outside the with block
			this.iframe.src = dojo.uri.dojoUri("src/widget/templates/richtextframe.html") + "#" + ((document.domain != currentDomain) ? document.domain : "");
			this.iframe.width = this.inheritWidth ? this._oldWidth : "100%";
			if (this.height) {
				this.iframe.style.height = this.height;
			} else {
				var height = this._oldHeight;
				if (this._hasCollapseableMargin(this.domNode, 'top')) {
					height += this._firstChildContributingMargin;
				}
				if (this._hasCollapseableMargin(this.domNode, 'bottom')) {
					height += this._lastChildContributingMargin;
				}
				this.iframe.height = height;
			}

			var tmpContent = document.createElement('div');
			tmpContent.innerHTML = html;

			// make relative image urls absolute
			if (this.relativeImageUrls) {
				var imgs = tmpContent.getElementsByTagName('img');
				for (var i=0; i<imgs.length; i++) {
					imgs[i].src = (new dojo.uri.Uri(window.location, imgs[i].src)).toString();
				}
				html = tmpContent.innerHTML;
			}

			// fix margins on tmpContent
			var firstChild = dojo.html.firstElement(tmpContent);
			var lastChild = dojo.html.lastElement(tmpContent);
			if(firstChild){
				firstChild.style.marginTop = this._firstChildContributingMargin+"px";
			}
			if(lastChild){
				lastChild.style.marginBottom = this._lastChildContributingMargin+"px";
			}

			// show existing content behind iframe for now
			tmpContent.style.position = "absolute";
			this.domNode.appendChild(tmpContent);
			this.domNode.appendChild(this.iframe);

			var _iframeInitialized = false;

			// now we wait for onload. Janky hack!
			var ifrFunc = dojo.lang.hitch(this, function(){
				if(!_iframeInitialized){
					_iframeInitialized = true;
				}else{ return; }
				if(!this.editNode){
					if(this.iframe.contentWindow){
						this.window = this.iframe.contentWindow;
					}else{
						// for opera
						this.window = this.iframe.contentDocument.window;
					}
					if(dojo.render.html.moz){
						this.document = this.iframe.contentWindow.document
					}else{
						this.document = this.iframe.contentDocument;
					}

					// curry the getStyle function
					var getStyle = (function (domNode) { return function (style) {
						return dojo.html.getStyle(domNode, style);
					}; })(this.domNode);

					var font =
						getStyle('font-weight') + " " +
						getStyle('font-size') + " " +
						getStyle('font-family');
					
					// line height is tricky - applying a units value will mess things up.
					// if we can't get a non-units value, bail out.
					var lineHeight = "1.0";
					var lineHeightStyle = dojo.html.getUnitValue(this.domNode, 'line-height');
					if (lineHeightStyle.value && lineHeightStyle.units=="") {
						lineHeight = lineHeightStyle.value;
					}

					dojo.html.insertCssText(
						'    body,html { background: transparent; padding: 0; margin: 0; }\n' +
						// TODO: left positioning will case contents to disappear out of view
						//       if it gets too wide for the visible area
						'    body { top: 0; left: 0; right: 0;' +
						(((this.height)||(dojo.render.html.opera)) ? '' : ' position: fixed; ') + 
						'        font: ' + font + ';\n' + 
						'        min-height: ' + this.minHeight + '; \n' +
						'        line-height: ' + lineHeight + '} \n' +
						'    p { margin: 1em 0 !important; }\n' +
						'    body > *:first-child { padding-top: 0 !important; margin-top: ' + this._firstChildContributingMargin + 'px !important; }\n' + // FIXME: test firstChild nodeType
						'    body > *:last-child { padding-bottom: 0 !important; margin-bottom: ' + this._lastChildContributingMargin + 'px !important; }\n' +
						'    li > ul:-moz-first-node, li > ol:-moz-first-node { padding-top: 1.2em; }\n' +
						'    li { min-height: 1.2em; }\n' +
						//'    p,ul,li { padding-top: 0; padding-bottom: 0; margin-top:0; margin-bottom: 0; }\n' + 
						'', this.document);

					tmpContent.parentNode.removeChild(tmpContent);
					this.document.body.innerHTML = html;
					if(oldMoz){
						this.document.designMode = "on";
					}
					this.onLoad();
				}else{
					tmpContent.parentNode.removeChild(tmpContent);
					this.editNode.innerHTML = html;
					this.onDisplayChanged();
				}
			});

			if(this.editNode){
				ifrFunc(); // iframe already exists, just set content
			}else if(dojo.render.html.moz){
				// FIXME: if we put this on a delay, we get a height of 20px.
				// Otherwise we get the correctly specified minHeight value.
				this.iframe.onload = function(){
					setTimeout(ifrFunc, 250);
				}
			}else{ // new mozillas, opera, safari
				this.iframe.onload = ifrFunc;
			}
		},

		/** Draws an active x object, used by IE */
		_drawObject: function (html) {
			this.object = dojo.html.createExternalElement(dojo.doc(), "object");

			with (this.object) {
				classid = "clsid:2D360201-FFF5-11D1-8D03-00A0C959BC0A";
				width = this.inheritWidth ? this._oldWidth : "100%";
				style.height = this.height ? this.height : (this._oldHeight+"px");
				Scrollbars = this.height ? true : false;
				Appearance = this._activeX.appearance.flat;
			}
			this.domNode.appendChild(this.object);

			this.object.attachEvent("DocumentComplete", dojo.lang.hitch(this, "onLoad"));
			//DisplayChanged is fired too often even no change is made, so we ignore it
			//and call onDisplayChanged manually in execCommand instead
//			this.object.attachEvent("DisplayChanged", dojo.lang.hitch(this, "onDisplayChanged"));

			dojo.lang.forEach(this.events, function(e){
				this.object.attachEvent(e.toLowerCase(), dojo.lang.hitch(this, e));
			}, this);

			this.object.DocumentHTML = '<!doctype HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">' +
				'<html><title></title>' +
				'<style type="text/css">' +
				'    body,html { padding: 0; margin: 0; }' + //font: ' + font + '; }' +
				(this.height ? '' : '    body,  { overflow: hidden; }') +
				//'    #bodywrapper {  }' +
				'</style>' +
				//'<base href="' + window.location + '">' +
				'<body><div id="bodywrapper">' + html + '</div></body></html>';

			this._cacheLocalBlockFormatNames();
		},

		//static cache variables shared between all instance of this class
		_local2NativeFormatNames: {},
		_native2LocalFormatNames: {},
		//in IE, names for blockformat is locale dependent, so we cache the values here
		//we use activeX to obtain the list, if success or the names are already cached, 
		//return true
		_cacheLocalBlockFormatNames: function(){
			if(!this._native2LocalFormatNames['p']){
				var obj = this.object;
				if(!obj){
					//create obj temporarily
					try{
						obj = dojo.html.createExternalElement(dojo.doc(), "object");
						obj.classid = "clsid:2D360201-FFF5-11D1-8D03-00A0C959BC0A";
						dojo.body().appendChild(obj);
						obj.DocumentHTML = "<html><head></head><body></body></html>";
					}catch(e){
						return false;
					}
				}
				var oNamesParm = new ActiveXObject("DEGetBlockFmtNamesParam.DEGetBlockFmtNamesParam"); 
				obj.ExecCommand(this._activeX.command['getblockformatnames'], 0, oNamesParm); 
				var vbNamesArray = new VBArray(oNamesParm.Names);
				var localFormats = vbNamesArray.toArray();
				var nativeFormats = ['p', 'pre', 'address', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'ol', 'ul', '', '', '','','div'];
				for(var i=0;i<nativeFormats.length;++i){
					if(nativeFormats[i].length>0){
						this._local2NativeFormatNames[localFormats[i]] = nativeFormats[i];
						this._native2LocalFormatNames[nativeFormats[i]] = localFormats[i];
					}
				}
				if(!this.object){
					//delete the temporary obj
					dojo.body().removeChild(obj);
				}
			}
			return true;
		},
	/* Event handlers
	 *****************/

		_isResized: function(){ return false; },

		onLoad: function(e){
			this.isLoaded = true;
			if (this.object){
				this.document = this.object.DOM;
				this.window = this.document.parentWindow;
				this.editNode = this.document.body.firstChild;
				this.domNode.style.height = this.height ? this.height : this.minHeight;
				this.connect(this, "onDisplayChanged", "_updateHeight");
			}else if (this.iframe){
				this.editNode = this.document.body;
				this.connect(this, "onDisplayChanged", "_updateHeight");
		
				try { // sanity check for Mozilla
					this.document.execCommand("useCSS", false, true); // old moz call
					this.document.execCommand("styleWithCSS", false, false); // new moz call
					//this.document.execCommand("insertBrOnReturn", false, false); // new moz call
				}catch(e2){ }
				
				if (dojo.render.html.safari) {
					/*
					this.iframe.style.visiblity = "visible";
					this.iframe.style.border = "1px solid black";
					this.editNode.style.visiblity = "visible";
					this.editNode.style.border = "1px solid black";
					*/
					// this.onDisplayChanged();
					this.connect(this.editNode, "onblur", "onBlur");
					this.connect(this.editNode, "onfocus", "onFocus");
				
					this.interval = setInterval(dojo.lang.hitch(this, "onDisplayChanged"), 750);
					// dojo.raise("onload");
					// dojo.debug(this.editNode.parentNode.parentNode.parentNode.nodeName);
				} else if (dojo.render.html.mozilla || dojo.render.html.opera) {

					// We need to unhook the blur event listener on close as we
					// can encounter a garunteed crash in FF if another event is
					// also fired
					var doc = this.document;
					var blurfp = dojo.event.browser.addListener(this.document, "blur", dojo.lang.hitch(this, "onBlur"));
					var unBlur = { unBlur: function(e){
							dojo.event.browser.removeListener(doc, "blur", blurfp);
					} };
					dojo.event.connect("before", this, "close", unBlur, "unBlur");
					dojo.event.browser.addListener(this.document, "focus", dojo.lang.hitch(this, "onFocus"));
				
					// safari can't handle key listeners, it kills the speed
					var addListener = dojo.event.browser.addListener;
					addListener(this.document, "keypress", dojo.lang.hitch(this, "onKeyPress"));
					addListener(this.document, "keydown", dojo.lang.hitch(this, "onKeyDown"));
					addListener(this.document, "keyup", dojo.lang.hitch(this, "onKeyUp"));
					addListener(this.document, "click", dojo.lang.hitch(this, "onClick"));
				}

				// FIXME: when scrollbars appear/disappear this needs to be fired						
			}else if(dojo.render.html.ie){
				// IE contentEditable
				this.editNode.style.zoom = 1.0;
			}
			
			if(this.focusOnLoad){
				this.focus();
			}
			this.onDisplayChanged(e);
		},

		/** Fired on keydown */
		onKeyDown: function(e){
			if((!e)&&(this.object)){
				e = dojo.event.browser.fixEvent(this.window.event);
			}
			// dojo.debug("onkeydown:", e.keyCode);
			// we need this event at the moment to get the events from control keys
			// such as the backspace. It might be possible to add this to Dojo, so that
			// keyPress events can be emulated by the keyDown and keyUp detection.
			if((dojo.render.html.ie)&&(e.keyCode == e.KEY_TAB)){
				e.preventDefault();
				e.stopPropagation();
				// FIXME: this is a poor-man's indent/outdent. It would be
				// better if it added 4 "&nbsp;" chars in an undoable way.
				// Unfortuantly pasteHTML does not prove to be undoable 
				this.execCommand((e.shiftKey ? "outdent" : "indent"));
			}else if(dojo.render.html.ie){
				if((65 <= e.keyCode)&&(e.keyCode <= 90)){
					e.charCode = e.keyCode;
					this.onKeyPress(e);
				}
				// dojo.debug(e.ctrlKey);
				// dojo.debug(e.keyCode);
				// dojo.debug(e.charCode);
				// this.onKeyPress(e);
			}
		},
		
		/** Fired on keyup */
		onKeyUp: function(e){
			return;
		},
		
		KEY_CTRL: 1,
		
		/** Fired on keypress. */
		onKeyPress: function(e){
			if((!e)&&(this.object)){
				e = dojo.event.browser.fixEvent(this.window.event);
			}
			// handle the various key events

			var character = e.charCode > 0 ? String.fromCharCode(e.charCode) : null;
			var code = e.keyCode;

			var modifiers = e.ctrlKey ? this.KEY_CTRL : 0;

			if (this._keyHandlers[character]) {
				// dojo.debug("char:", character);
				var handlers = this._keyHandlers[character], i = 0, handler;
				while (handler = handlers[i++]) {
					if (modifiers == handler.modifiers) {
						handler.handler.call(this);
						e.preventDefault();
						break;
					}
				}
			}
			
			/*
			// define some key combos
			if (e.ctrlKey || e.metaKey) { // modifier pressed
				switch (character) {
					case "b": this.execCommand("bold"); break;
					case "i": this.execCommand("italic"); break;
					case "u": this.execCommand("underline"); break;
					//case "a": this.execCommand("selectall"); break;
					//case "k": this.execCommand("createlink", ""); break;
					//case "K": this.execCommand("unlink"); break;
					case "Z": this.execCommand("redo"); break;
					case "s": this.close(true); break; // saves
					
					case "1": this.execCommand("formatblock", "h1"); break;
					case "2": this.execCommand("formatblock", "h2"); break;
					case "3": this.execCommand("formatblock", "h3"); break;
					case "4": this.execCommand("formatblock", "h4"); break;
					
					case "\\": this.execCommand("insertunorderedlist"); break;
					
					default: switch (code) {
						case e.KEY_LEFT_ARROW:
						case e.KEY_RIGHT_ARROW:
							//break; // preventDefault stops the browser
								   // going through its history
						default:
							preventDefault = false; break; // didn't handle here
					}
				}
			} else {
				switch (code) {
					case e.KEY_TAB:
					  // commenting out bcs it's crashing FF
						// this.execCommand(e.shiftKey ? "unindent" : "indent");
						// break;
					default:
						preventDefault = false; break; // didn't handle here
				}
			}
			
			if (preventDefault) { e.preventDefault(); }
			*/

			// function call after the character has been inserted
			dojo.lang.setTimeout(this, this.onKeyPressed, 1, e);
		},
		
		addKeyHandler: function (key, modifiers, handler) {
			if (!(this._keyHandlers[key] instanceof Array)) { this._keyHandlers[key] = []; }
			this._keyHandlers[key].push({
				modifiers: modifiers || 0,
				handler: handler
			});
		},
		
		
		
		/**
		 * Fired after a keypress event has occured and it's action taken. This
		 * is useful if action needs to be taken after text operations have
		 * finished
		 */
		onKeyPressed: function (e) {
			// Mozilla adds a single <p> with an embedded <br> when you hit enter once:
			//   <p><br>\n</p>
			// when you hit enter again it adds another <br> inside your enter
			//   <p><br>\n<br>\n</p>
			// and if you hit enter again it splits the <br>s over 2 <p>s
			//   <p><br>\n</p>\n<p><br>\n</p>
			// now this assumes that <p>s have double the line-height of <br>s to work
			// and so we need to remove the <p>s to ensure the position of the cursor
			// changes from the users perspective when they hit enter, as the second two
			// html snippets render the same when margins are set to 0.
			
			// TODO: doesn't really work; is this really needed?
			//if (dojo.render.html.moz) {
			//	for (var i = 0; i < this.document.getElementsByTagName("p").length; i++) {
			//		var p = this.document.getElementsByTagName("p")[i];
			//		if (p.innerHTML.match(/^<br>\s$/m)) {
			//			while (p.hasChildNodes()) { p.parentNode.insertBefore(p.firstChild, p); }
			//			p.parentNode.removeChild(p);
			//		}
			//	}
			//}
			this.onDisplayChanged(/*e*/); // can't pass in e
		},
		
		onClick: function(e){ this.onDisplayChanged(e); },
		onBlur: function(e){ },
//		_initialFocus: true,
		onFocus: function(e){ 
			//what's this for?
//			if( (dojo.render.html.mozilla)&&(this._initialFocus) ){
//				this._initialFocus = false;
//				if(dojo.string.trim(this.editNode.innerHTML) == "&nbsp;"){
//					this.execCommand("selectall");
//					this.window.getSelection().collapseToStart();
//				}
//			}
		},

		blur: function () {
			if(this.iframe) { this.window.blur(); }
			else if(this.object) { this.document.body.blur(); }
			else if(this.editNode) { this.editNode.blur(); }
		},
		
		focus: function () {
			if(this.iframe) { this.window.focus(); }
			else if(this.object) { this.document.focus(); }
			// editNode may be hidden in display:none div, lets just punt in this case
			else if(this.editNode && dojo.lang.isFunction(this.editNode.focus)) { this.editNode.focus(); }
		},
		
		/** this event will be fired everytime the display context changes and the
		 result needs to be reflected in the UI */
		onDisplayChanged: function (e){ },
		

	/* Formatting commands
	 **********************/
		
		/** IE's Active X codes: see http://www.computerbytesman.com/js/activex/dhtmledit.htm */
		_activeX: {
			command: {
				bold: 5000,
				italic: 5023,
				underline: 5048,

				justifycenter: 5024,
				justifyleft: 5025,
				justifyright: 5026,

				cut: 5003,
				copy: 5002,
				paste: 5032,
				"delete": 5004,

				undo: 5049,
				redo: 5033,

				removeformat: 5034,
				selectall: 5035,
				unlink: 5050,

				indent: 5018,
				outdent: 5031,

				insertorderedlist: 5030,
				insertunorderedlist: 5051,

				// table commands
				inserttable: 5022,
				insertcell: 5019,
				insertcol: 5020,
				insertrow: 5021,
				deletecells: 5005,
				deletecols: 5006,
				deleterows: 5007,
				mergecells: 5029,
				splitcell: 5047,
				
				// the command need mapping, they don't translate directly
				// to the contentEditable commands
				setblockformat: 5043,
				getblockformat: 5011,
				getblockformatnames: 5012,
				setfontname: 5044,
				getfontname: 5013,
				setfontsize: 5045,
				getfontsize: 5014,
				setbackcolor: 5042,
				getbackcolor: 5010,
				setforecolor: 5046,
				getforecolor: 5015,
				
				findtext: 5008,
				font: 5009,
				hyperlink: 5016,
				image: 5017,
				
				lockelement: 5027,
				makeabsolute: 5028,
				sendbackward: 5036,
				bringforward: 5037,
				sendbelowtext: 5038,
				bringabovetext: 5039,
				sendtoback: 5040,
				bringtofront: 5041,
				
				properties: 5052
			},
			
			ui: {
				"default": 0,
				prompt: 1,
				noprompt: 2
			},
			
			status: {
				notsupported: 0,
				disabled: 1,
				enabled: 3,
				latched: 7,
				ninched: 11
			},
			
			appearance: {
				flat: 0,
				inset: 1
			},
			
			state: {
				unchecked: 0,
				checked: 1,
				gray: 2
			}
		},
		
		/**
		 * Used as the advice function by dojo.event.connect to map our
		 * normalized set of commands to those supported by the target
		 * browser
		 *
		 * @param arugments The arguments Array, containing at least one
		 *                  item, the command and an optional second item,
		 *                  an argument.
		 */
		_normalizeCommand: function (joinObject){
			var drh = dojo.render.html;
			
			var command = joinObject.args[0].toLowerCase();
			if(command == "formatblock"){
				if(drh.safari){ command = "heading"; }
				if(this.object){ //IE activeX mode
					joinObject.args[1] = this._native2LocalFormatNames[joinObject.args[1]];
				}
				else if(drh.ie){ joinObject.args[1] = "<"+joinObject.args[1]+">"; }
			}else if(command == "createlink" && this.object){
				command = "hyperlink";
			}else if(command == "hilitecolor" && !drh.mozilla) { command = "backcolor"; }

			joinObject.args[0] = command;
			
			if (joinObject.args.length > 1) { // a command was specified
				var argument = joinObject.args[1];
				if (command == "heading") { throw new Error("unimplemented"); }
				joinObject.args[1] = argument;
			}
			
			return joinObject.proceed();
		},
		
		/**
		 * Tests whether a command is supported by the host. Clients SHOULD check
		 * whether a command is supported before attempting to use it, behaviour
		 * for unsupported commands is undefined.
		 *
		 * @param command The command to test for
		 * @return true if the command is supported, false otherwise
		 */
		queryCommandAvailable: function (command) {
			var ie = 1;
			var mozilla = 1 << 1;
			var safari = 1 << 2;
			var opera = 1 << 3;
			function isSupportedBy (browsers) {
				return {
					ie: Boolean(browsers & ie),
					mozilla: Boolean(browsers & mozilla),
					safari: Boolean(browsers & safari),
					opera: Boolean(browsers & opera)
				}
			}

			var supportedBy = null;
			
			switch (command.toLowerCase()) {
				case "bold": case "italic": case "underline":
				case "subscript": case "superscript":
				case "fontname": case "fontsize":
				case "forecolor": case "hilitecolor":
				case "justifycenter": case "justifyfull": case "justifyleft": 
				case "justifyright": case "delete": case "selectall":
					supportedBy = isSupportedBy(mozilla | ie | safari | opera);
					break;
					
				case "createlink": case "unlink": case "removeformat":
				case "inserthorizontalrule": case "insertimage":
				case "insertorderedlist": case "insertunorderedlist":
				case "indent": case "outdent": case "formatblock": 
				case "inserthtml": case "undo": case "redo": 
					supportedBy = isSupportedBy(mozilla | ie | opera);
					break;
					
				case "strikethrough": 
					supportedBy = isSupportedBy(mozilla |  opera | (this.object ? 0 : ie));
					break;

				case "blockdirltr": case "blockdirrtl":
				case "dirltr": case "dirrtl":
				case "inlinedirltr": case "inlinedirrtl":
					supportedBy = isSupportedBy(ie);
					break;
				case "cut": case "copy": case "paste": 
					supportedBy = isSupportedBy(ie|mozilla);
					break;
				
				case "inserttable":
					supportedBy = isSupportedBy(mozilla | (this.object ? ie : 0));
					break;
				
				case "insertcell": case "insertcol": case "insertrow":
				case "deletecells": case "deletecols": case "deleterows":
				case "mergecells": case "splitcell":
					supportedBy = isSupportedBy(this.object ? ie : 0);
					break;
				
				default: return false;
			}
			
			return (dojo.render.html.ie && supportedBy.ie) ||
				(dojo.render.html.mozilla && supportedBy.mozilla) ||
				(dojo.render.html.safari && supportedBy.safari) ||
				(dojo.render.html.opera && supportedBy.opera);
		},

		/**
		 * Executes a command in the Rich Text area
		 *
		 * @param command The command to execute
		 * @param argument An optional argument to the command
		 */
		execCommand: function (command, argument){
			var returnValue;

			//focus() is required for IE (none-activeX mode) to work
			//In addition, focus() makes sure after the execution of
			//the command, the editor receives the focus as expected
			this.focus();

			if(this.object){
				switch (command) {
					case "forecolor":
					case "backcolor":
					case "fontsize":
					case "fontname":
						command = "set" + command;
						break;
					case "formatblock":
						command = "setblockformat";
				}
			
				//if (typeof this._activeX.command[command] == "undefined") { return null; }
			
				if(command == "inserttable"){
					var tableInfo = this.constructor._tableInfo;
					if(!tableInfo){
						tableInfo = document.createElement("object");
						tableInfo.classid = "clsid:47B0DFC7-B7A3-11D1-ADC5-006008A5848C";
						dojo.body().appendChild(tableInfo);
						this.constructor._table = tableInfo;
					}
					
					tableInfo.NumRows = argument["rows"];
					tableInfo.NumCols = argument["cols"];
					tableInfo.TableAttrs = argument["TableAttrs"];
					tableInfo.CellAttrs = argument["CellAttrs"];
					tableInfo.Caption = argument["Caption"];
				}
			
				if(command == "inserthtml"){
					var insertRange = this.document.selection.createRange();
					insertRange.select();
					insertRange.pasteHTML(argument);
					insertRange.collapse(true);
					returnValue = true;
				}else if(arguments.length == 1){
					returnValue = this.object.ExecCommand(this._activeX.command[command],
						this._activeX.ui.noprompt);
				}else{
					returnValue = this.object.ExecCommand(this._activeX.command[command],
						this._activeX.ui.noprompt, argument);
				}
		
			/* */
			}else if(command == "inserthtml"){
				// on IE, we can use the pasteHTML method of the textRange object
				// to get an undo-able innerHTML modification
				if(dojo.render.html.ie){
					dojo.debug("inserthtml breaks the undo stack when not using the ActiveX version of the control!");
					var insertRange = this.document.selection.createRange();
					insertRange.select();
					insertRange.pasteHTML(argument);
					insertRange.collapse(true);
					return true;
				}else{
					return this.document.execCommand(command, false, argument);			
				}
			/* */
			// fix up unlink in Mozilla to unlink the link and not just the selection
			}else if((command == "unlink")&&
				(this.queryCommandEnabled("unlink"))&&
				(dojo.render.html.mozilla)){
				// grab selection
				// Mozilla gets upset if we just store the range so we have to
				// get the basic properties and recreate to save the selection
				var selection = this.window.getSelection();
				var selectionRange = selection.getRangeAt(0);
				var selectionStartContainer = selectionRange.startContainer;
				var selectionStartOffset = selectionRange.startOffset;
				var selectionEndContainer = selectionRange.endContainer;
				var selectionEndOffset = selectionRange.endOffset;
				
				// select our link and unlink
				var a = dojo.withGlobal(this.window, "getAncestorElement", dojo.html.selection, 'a');
				dojo.withGlobal(this.window, "selectElement", dojo.html.selection, a);
				
				returnValue = this.document.execCommand("unlink", false, null);
				
				// restore original selection
				var selectionRange = document.createRange();
				selectionRange.setStart(selectionStartContainer, selectionStartOffset);
				selectionRange.setEnd(selectionEndContainer, selectionEndOffset);
				selection.removeAllRanges();
				selection.addRange(selectionRange);
				
				return returnValue;
			}else if((command == "inserttable")&&(dojo.render.html.mozilla)){

				var cols = "<tr>";
				for (var i = 0; i < argument.cols; i++) { cols += "<td></td>"; }
				cols += "</tr>";
			
				var table = "<table><tbody>";
				for (var i = 0; i < argument.rows; i++) { table += cols; }
				table += "</tbody></table>";
				returnValue = this.document.execCommand("inserthtml", false, table);

			}else if((command == "hilitecolor")&&(dojo.render.html.mozilla)){
				// mozilla doesn't support hilitecolor properly when useCSS is
				// set to false (bugzilla #279330)
				
				this.document.execCommand("useCSS", false, false);
				returnValue = this.document.execCommand(command, false, argument);			
				this.document.execCommand("useCSS", false, true);
			
			}else if((dojo.render.html.ie)&&( (command == "backcolor")||(command == "forecolor") )){
				// Tested under IE 6 XP2, no problem here, comment out
				// IE weirdly collapses ranges when we exec these commands, so prevent it	
//				var tr = this.document.selection.createRange();
				argument = arguments.length > 1 ? argument : null;
				returnValue = this.document.execCommand(command, false, argument);

				// timeout is workaround for weird IE behavior were the text
				// selection gets correctly re-created, but subsequent input
				// apparently isn't bound to it
//				setTimeout(function(){tr.select();}, 1);
			}else{
				// dojo.debug("command:", command, "arg:", argument);

				argument = arguments.length > 1 ? argument : null;
				if(dojo.render.html.moz){
					this.document = this.iframe.contentWindow.document
				}

				if(argument || command!="createlink") {
					returnValue = this.document.execCommand(command, false, argument);
				}
			}
			
			this.onDisplayChanged();
			return returnValue;
		},

		queryCommandEnabled: function(command, argument){
			if(this.object){
				switch (command) {
					case "forecolor":
					case "backcolor":
					case "fontsize":
					case "fontname":
						command = "set" + command;
						break;
					case "formatblock":
						command = "setblockformat";
				}

				if(typeof this._activeX.command[command] == "undefined"){ return false; }
				var status = this.object.QueryStatus(this._activeX.command[command]);
				return ((status != this._activeX.status.notsupported)&& 
					(status != this._activeX.status.disabled));
			}else{
				// mozilla returns true always
				if(command == "unlink" && dojo.render.html.mozilla){
					return dojo.withGlobal(this.window, "hasAncestorElement", dojo.html.selection, 'a');
				} else if (command == "inserttable" && dojo.render.html.mozilla) {
					return true;
				}

				// return this.document.queryCommandEnabled(command);
				var elem = (dojo.render.html.ie) ? this.document.selection.createRange() : this.document;
				return elem.queryCommandEnabled(command);
			}
		},

		queryCommandState: function(command, argument){
			if(this.object){
				if(command == "forecolor"){
					command = "setforecolor";
				}else if(command == "backcolor"){
					command = "setbackcolor";
				}

				if(typeof this._activeX.command[command] == "undefined"){ return null; }
				var status = this.object.QueryStatus(this._activeX.command[command]);
				return ((status == this._activeX.status.latched)||
					(status == this._activeX.status.ninched));
			}else{
				return this.document.queryCommandState(command);
			}
		},

		queryCommandValue: function (command, argument) {
			if (this.object) {
				switch (command) {
					case "forecolor":
					case "backcolor":
					case "fontsize":
					case "fontname":
						command = "get" + command;
						return this.object.execCommand(
							this._activeX.command[command],
							this._activeX.ui.noprompt);
					case "formatblock":
						var retvalue = this.object.execCommand(
							this._activeX.command["getblockformat"],
							this._activeX.ui.noprompt);
						if(retvalue){
							return this._local2NativeFormatNames[retvalue];
						}
				}
			} else {
				if(dojo.render.html.ie && command == "formatblock"){
					return this._local2NativeFormatNames[this.document.queryCommandValue(command)] || this.document.queryCommandValue(command);
				}
				return this.document.queryCommandValue(command);
			}
		},
		
		
	/* Misc.
	 ********/
		
		placeCursorAtStart: function(){
			this.focus();
			dojo.withGlobal(this.window, "selectElementChildren", dojo.html.selection, this.editNode);
			dojo.withGlobal(this.window, "collapse", dojo.html.selection, true);
		},

		placeCursorAtEnd: function(){
			this.focus();
			//In mozilla, if last child is not a text node, we have to use selectElementChildren on this.editNode.lastChild
			//otherwise the cursor would be placed at the end of the closing tag of this.editNode.lastChild
			if(dojo.render.html.moz && this.editNode.lastChild && 
				this.editNode.lastChild.nodeType != dojo.dom.TEXT_NODE){
				dojo.withGlobal(this.window, "selectElementChildren", dojo.html.selection, this.editNode.lastChild);
			}else{
				dojo.withGlobal(this.window, "selectElementChildren", dojo.html.selection, this.editNode);
			}
			dojo.withGlobal(this.window, "collapse", dojo.html.selection, false);
		},

		//this function set the content while trying to maintain the undo stack
		replaceEditorContent: function(html){
			html = this._preFilterContent(html);
			if(this.isClosed){
				this.domNode.innerHTML = html;
			}else if(this.window.getSelection && !dojo.render.html.moz){ // Safari
				// look ma! it's a totally f'd browser!
				this.editNode.innerHTML = html;
			}else if(this.window.getSelection || this.document.selection){ // Moz/IE
				this.execCommand("selectall");
				this.execCommand("inserthtml", html);
			}
		},

		_preFilterContent: function(html){
			var ec = html;
			dojo.lang.forEach(this.contentPreFilters, function(ef){
				ec = ef(ec);
			});
			return ec;
		},

		_lastHeight: 0,

		/** Updates the height of the editor area to fit the contents. */
		_updateHeight: function(){
			if(!this.isLoaded){ return; }
			if(this.height){ return; }
			if(this.iframe){
				/*
				if(!this.dojo.body()["offsetHeight"]){
					return;
				}
				*/
				// The height includes the padding, borders and margins so these
				// need to be added on
				var heights = ["margin-top", "margin-bottom",
					"padding-bottom", "padding-top",
					"border-width-bottom", "border-width-top"];
				for(var i = 0, chromeheight = 0; i < heights.length; i++){
					var height = dojo.html.getStyle(this.iframe, heights[i]);
					// Safari doesn't have all the heights so we have to test
					if(height){
						chromeheight += Number(height.replace(/[^0-9]/g, ""));
					}
				}

				if(this.document.body["offsetHeight"]){
					this._lastHeight = Math.max(this.document.body.scrollHeight, this.document.body.offsetHeight) + chromeheight;
					this.iframe.height = this._lastHeight + "px";
					this.window.scrollTo(0, 0);
				}
				// dojo.debug(this.iframe.height);
			}else if(this.object){
				var height = dojo.html.getBorderBox(this.editNode).height;
				//height maybe zero in some cases even though the content is not empty,
				//we try the height of body instead
				if(!height){
					height = dojo.html.getBorderBox(this.document.body).height;
				}
				if(height == 0){
					dojo.debug("Can not figure out the height of the editing area!");
					return; //prevent setting height to 0
				}
				this.object.style.height = height+"px";
			}
		},
		
		/**
		 * Saves the content in an onunload event if the editor has not been closed
		 */
		_saveContent: function(e){
			var saveTextarea = document.getElementById("dojo.widget.RichText.savedContent");
			saveTextarea.value += this._SEPARATOR + this.saveName + ":" + this.getEditorContent();
		},

		getEditorContent: function(){
			var ec = "";
			try{
				ec = (this._content.length > 0) ? this._content : this.editNode.innerHTML;
				if(dojo.string.trim(ec) == "&nbsp;"){ ec = ""; }
			}catch(e){ /* squelch */ }

			dojo.lang.forEach(this.contentPostFilters, function(ef){
				ec = ef(ec);
			});

			if (this.relativeImageUrls) {
				// why use a regexp instead of dom? because IE is stupid 
				// and won't let us set img.src to a relative URL
				// this comes after contentPostFilters because once content
				// gets innerHTML'd img urls will be fully qualified
				var siteBase = window.location.protocol + "//" + window.location.host;
				var pathBase = window.location.pathname;
				if (pathBase.match(/\/$/)) {
					// ends with slash, match full path
				} else {
					// match parent path to find siblings
					var pathParts = pathBase.split("/");
					if (pathParts.length) {
						pathParts.pop();
					}
					pathBase = pathParts.join("/") + "/";

				}
				
				var sameSite = new RegExp("(<img[^>]*\ src=[\"'])("+siteBase+"("+pathBase+")?)", "ig");
				ec = ec.replace(sameSite, "$1");
			}
			return ec;
		},
		
		/**
		 * Kills the editor and optionally writes back the modified contents to the 
		 * element from which it originated.
		 *
		 * @param save Whether or not to save the changes. If false, the changes are
		 *             discarded.
		 * @return true if the contents has been modified, false otherwise
		 */
		close: function(save, force){
			if(this.isClosed){return false; }

			if (arguments.length == 0) { save = true; }
			this._content = this.editNode.innerHTML;
			var changed = (this.savedContent.innerHTML != this._content);
			
			// line height is squashed for iframes
			// FIXME: why was this here? if (this.iframe){ this.domNode.style.lineHeight = null; }

			if(this.interval){ clearInterval(this.interval); }
			
			if(dojo.render.html.ie && !this.object){
				dojo.event.browser.clean(this.editNode);
			}
			
			if (this.iframe) {
				// FIXME: should keep iframe around for later re-use
				delete this.iframe;
			}
			this.domNode.innerHTML = "";

			if(save){
				// kill listeners on the saved content
				dojo.event.browser.clean(this.savedContent);
				if(dojo.render.html.moz){
					var nc = document.createElement("span");
					this.domNode.appendChild(nc);
					nc.innerHTML = this.editNode.innerHTML;
				}else{
					this.domNode.innerHTML = this._content;
				}
			} else {
				while (this.savedContent.hasChildNodes()) {
					this.domNode.appendChild(this.savedContent.firstChild);
				}
			}
			delete this.savedContent;
			
			dojo.html.removeClass(this.domNode, "RichTextEditable");
			this.isClosed = true;
			this.isLoaded = false;
			// FIXME: is this always the right thing to do?
			delete this.editNode;

			return changed;
		},

		destroyRendering: function(){}, // stub!
		
		destroy: function (){
			this.destroyRendering();
			if(!this.isClosed){ this.close(false); }
		
			// disconnect those listeners.
			while(this._connected.length){
				this.disconnect(this._connected[0],
					this._connected[1], this._connected[2]);
			}
			this.window = null;
			this.document = null;
			this.editNode = null;
			this.object = null;
			dojo.widget.RichText.superclass.destroy.call(this);
		},

		_connected: [],
		connect: function (targetObj, targetFunc, thisFunc) {
			dojo.event.connect(targetObj, targetFunc, this, thisFunc);
			// this._connected.push([targetObj, targetFunc, thisFunc]);	
		},
		
		// FIXME: below two functions do not work with the above line commented out
		disconnect: function (targetObj, targetFunc, thisFunc) {
			for (var i = 0; i < this._connected.length; i++) {
				if (this._connected[0] == targetObj &&
					this._connected[1] == targetFunc &&
					this._connected[2] == thisFunc) {
					dojo.event.disconnect(targetObj, targetFunc, this, thisFunc);
					this._connected.splice(i, 1);
					break;
				}
			}
		},
		
		disconnectAllWithRoot: function (targetObj) {
			for (var i = 0; i < this._connected.length; i++) {
				if (this._connected[0] == targetObj) {
					dojo.event.disconnect(targetObj,
						this._connected[1], this, this._connected[2]);
					this._connected.splice(i, 1);
				}
			}	
		},

		fixContentForMoz: function(html){
			//Moz can not handle strong/em tags correctly, so we change them here
			html = html.replace(/<strong([ \>])/gi, '<b$1' );
			html = html.replace(/<\/strong>/gi, '<\/b>' );
			html = html.replace(/<em([ \>])/gi, '<i$1' );
			html = html.replace(/<\/em>/gi, '<\/i>' );
			return html;
		}
	},
	"html",
	function(){
		this.contentPreFilters = [];
		this.contentPostFilters = [];
		if(dojo.render.html.moz){
			this.contentPreFilters.push(this.fixContentForMoz);
		}
		
		this._keyHandlers = {};
	}
);
