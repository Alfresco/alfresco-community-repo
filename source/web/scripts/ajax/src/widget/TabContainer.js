dojo.provide("dojo.widget.TabContainer");

dojo.require("dojo.lang.func");
dojo.require("dojo.widget.*");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.event.*");
dojo.require("dojo.html.selection");
dojo.require("dojo.widget.html.layout");

dojo.widget.defineWidget("dojo.widget.TabContainer", dojo.widget.HtmlWidget, {
	isContainer: true,

	// Constructor arguments
	labelPosition: "top",
	closeButton: "none",

	useVisibility: false,		// true-->use visibility:hidden instead of display:none
	
	// if false, TabContainers size changes according to size of currently selected tab
	doLayout: true,

	templatePath: dojo.uri.dojoUri("src/widget/templates/TabContainer.html"),
	templateCssPath: dojo.uri.dojoUri("src/widget/templates/TabContainer.css"),

	selectedTab: "",		// initially selected tab (widgetId)

	fillInTemplate: function(args, frag) {
		// Copy style info from input node to output node
		var source = this.getFragNodeRef(frag);
		dojo.html.copyStyle(this.domNode, source);

		dojo.widget.TabContainer.superclass.fillInTemplate.call(this, args, frag);
	},

	postCreate: function(args, frag) {
		// Load all the tabs, creating a label for each one
		for(var i=0; i<this.children.length; i++){
			this._setupTab(this.children[i]);
		}

		if (this.closeButton=="pane") {
			var div = document.createElement("div");
			dojo.html.addClass(div, "dojoTabPanePaneClose");
			dojo.event.connect(div, "onclick", dojo.lang.hitch(this, 
					function(){ this._runOnCloseTab(this.selectedTabWidget); }
				)
			);
			dojo.event.connect(div, "onmouseover", function(){ dojo.html.addClass(div, "dojoTabPanePaneCloseHover"); });
			dojo.event.connect(div, "onmouseout", function(){ dojo.html.removeClass(div, "dojoTabPanePaneCloseHover"); });
			this.dojoTabLabels.appendChild(div);
		}

		if(!this.doLayout){
			dojo.html.addClass(this.dojoTabLabels, "dojoTabNoLayout");
			if (this.labelPosition == 'bottom') {
				var p = this.dojoTabLabels.parentNode;
				p.removeChild(this.dojoTabLabels);
				p.appendChild(this.dojoTabLabels);
			}
		}
		dojo.html.addClass(this.dojoTabLabels, "dojoTabLabels-"+this.labelPosition);

		this._doSizing();

		// Display the selected tab
		if(this.selectedTabWidget){
			this.selectTab(this.selectedTabWidget, true);
		}
	},

	addChild: function(child, overrideContainerNode, pos, ref, insertIndex){
		// FIXME need connect to tab Destroy, so call removeChild properly.
		this._setupTab(child);
		dojo.widget.TabContainer.superclass.addChild.call(this,child, overrideContainerNode, pos, ref, insertIndex);

		// in case the tab labels have overflowed from one line to two lines
		this._doSizing();
	},

	_setupTab: function(tab){
		tab.domNode.style.display="none";

		// Create label
		tab.div = document.createElement("div");
		dojo.html.addClass(tab.div, "dojoTabPaneTab");
		var innerDiv = document.createElement("div");
		// need inner span so focus rectangle is drawn properly
		var titleSpan = document.createElement("span");
		titleSpan.innerHTML = tab.label;
		titleSpan.tabIndex="-1";
		// set role on tab title
		dojo.widget.wai.setAttr(titleSpan, "waiRole", "role", "tab");
		innerDiv.appendChild(titleSpan);
		dojo.html.disableSelection(titleSpan); 
		
		if(this.closeButton=="tab" || tab.tabCloseButton){
			var img = document.createElement("span");
			dojo.html.addClass(img, "dojoTabPaneTabClose");
			dojo.event.connect(img, "onclick", dojo.lang.hitch(this, 
					function(evt){ 
						this._runOnCloseTab(tab); dojo.event.browser.stopEvent(evt);
					}
				)
			);
			dojo.event.connect(img, "onmouseover", function(){ dojo.html.addClass(img,"dojoTabPaneTabCloseHover"); });
			dojo.event.connect(img, "onmouseout", function(){ dojo.html.removeClass(img,"dojoTabPaneTabCloseHover"); });
			innerDiv.appendChild(img);
		}
		tab.div.appendChild(innerDiv);
		tab.div.tabTitle=titleSpan;
		this.dojoTabLabels.appendChild(tab.div);

		dojo.event.connect(tab.div, "onclick", dojo.lang.hitch(this, 
				function(){ this.selectTab(tab); }
			)
		);
		dojo.event.connect(tab.div, "onkeydown", dojo.lang.hitch(this, 
				function(evt){ this.tabNavigation(evt, tab); } 
			)
		);

		if(!this.selectedTabWidget || this.selectedTab==tab.widgetId || tab.selected || (this.children.length==0)){
			// Deselect old tab and select new one
			// We do this instead of calling selectTab in this case, becuase other wise other widgets
			// listening for addChild and selectTab can run into a race condition
			if(this.selectedTabWidget){
				this._hideTab(this.selectedTabWidget);
			}
			this.selectedTabWidget = tab;
			this._showTab(tab);

		} else {
			this._hideTab(tab);
		}

		dojo.html.addClass(tab.domNode, "dojoTabPane");

		if(this.doLayout){
			with(tab.domNode.style){
				top = dojo.html.getPixelValue(this.containerNode, "padding-top", true);
				left = dojo.html.getPixelValue(this.containerNode, "padding-left", true);
			}
		}
	},

	// Configure the content pane to take up all the space except for where the tab labels are
	_doSizing: function(){
		if(!this.doLayout){ return; }

		// position the labels and the container node
		var labelAlign=this.labelPosition.replace(/-h/,"");
		var children = [
			{domNode: this.dojoTabLabels, layoutAlign: labelAlign},
			{domNode: this.containerNode, layoutAlign: "client"}
		];

		dojo.widget.html.layout(this.domNode, children);

		// size the current tab
		// TODO: should have ptr to current tab rather than searching
		var content = dojo.html.getContentBox(this.containerNode);
		dojo.lang.forEach(this.children, function(child){
			if(child.selected){
				child.resizeTo(content.width, content.height);
			}
		});
	},

	removeChild: function(tab){
		// remove tab event handlers
		dojo.event.disconnect(tab.div, "onclick", function(){ });
		if(this.closeButton == "tab"){
			var img = tab.div.lastChild.lastChild;
			if(img){
				dojo.html.removeClass(img, "dojoTabPaneTabClose");
			}
		}

		dojo.widget.TabContainer.superclass.removeChild.call(this, tab);

		dojo.html.removeClass(tab.domNode, "dojoTabPane");
		this.dojoTabLabels.removeChild(tab.div);
		delete(tab.div);

		if (this.selectedTabWidget === tab) {
			this.selectedTabWidget = undefined;
			if (this.children.length > 0) {
				this.selectTab(this.children[0], true);
			}
		}

		// in case the tab labels have overflowed from one line to two lines
		this._doSizing();
	},

	selectTab: function(tab, _noRefresh){
		// Deselect old tab and select new one
		if(this.selectedTabWidget){
			this._hideTab(this.selectedTabWidget);
		}
		this.selectedTabWidget = tab;
		this._showTab(tab, _noRefresh);
	},

	tabNavigation: function(evt, tab){
		if( (evt.keyCode == evt.KEY_RIGHT_ARROW)||
			(evt.keyCode == evt.KEY_LEFT_ARROW) ){
			var current = null;
			var next = null;
			for(var i=0; i < this.children.length; i++){
				if(this.children[i] == tab){
					current = i; 
					break;
				}
			}
			if(evt.keyCode == evt.KEY_RIGHT_ARROW){
				next = this.children[ (current+1) % this.children.length ]; 
			}else{ // is LEFT_ARROW
				next = this.children[ (current+ (this.children.length-1)) % this.children.length ];
			}
			this.selectTab(next);
			dojo.event.browser.stopEvent(evt);
			next.div.tabTitle.focus();
		} 
	
	},
	
	keyDown: function(e){ 
		if(e.keyCode == e.KEY_UP_ARROW && e.ctrlKey){
			// set focus to current tab
			this.selectTab(this.selectedTabWidget);
			dojo.event.browser.stopEvent(e);
			this.selectedTabWidget.div.tabTitle.focus();
		}
	},

	_showTab: function(tab, _noRefresh) {
		dojo.html.addClass(tab.div, "current");
		tab.selected=true;
		tab.div.tabTitle.setAttribute("tabIndex","0");
		if ( this.useVisibility && !dojo.render.html.ie){
			tab.domNode.style.visibility="visible";
		}else{
			// make sure we dont refresh onClose and on postCreate
			// speeds up things a bit when using refreshOnShow and fixes #646
			if(_noRefresh && tab.refreshOnShow){
				var tmp = tab.refreshOnShow;
				tab.refreshOnShow = false;
				tab.show();
				tab.refreshOnShow = tmp;
			}else{
				tab.show();
			}

			if(this.doLayout){
				var content = dojo.html.getContentBox(this.containerNode);
				tab.resizeTo(content.width, content.height);
			}
		}
	},

	_hideTab: function(tab) {
		dojo.html.removeClass(tab.div, "current");
		tab.div.tabTitle.setAttribute("tabIndex","-1");
		tab.selected=false;
		if( this.useVisibility ){
			tab.domNode.style.visibility="hidden";
		}else{
			tab.hide();
		}
	},

	_runOnCloseTab: function(tab) {
		var onc = tab.extraArgs.onClose || tab.extraArgs.onclose;
		var fcn = dojo.lang.isFunction(onc) ? onc : window[onc];
		var remove = dojo.lang.isFunction(fcn) ? fcn(this,tab) : true;
		if(remove) {
			this.removeChild(tab);
			// makes sure we can clean up executeScripts in ContentPane onUnLoad
			tab.destroy();
		}
	},

	onResized: function() {
		this._doSizing();
	}
});


// These arguments can be specified for the children of a TabContainer.
// Since any widget can be specified as a TabContainer child, mix them
// into the base widget class.  (This is a hack, but it's effective.)
dojo.lang.extend(dojo.widget.Widget, {
	label: "",
	selected: false,	// is this tab currently selected?
	tabCloseButton: false
});
