dojo.provide("dojo.widget.RemoteTabController");

//Summary
//Remote Tab Controller widget.  Can be located independently of a tab
//container and control the selection of its tabs
dojo.require("dojo.widget.*");
dojo.require("dojo.event.*");

dojo.widget.defineWidget(
    "dojo.widget.RemoteTabController",
    dojo.widget.HtmlWidget,
	function() {
		//summary
		//Initialize Remote Tab Controller
		// for passing in as a parameter
		this.tabContainer = "";

		// the reference to the tab container
		this._tabContainer={};

		//hash of tabs
		this.tabs = {}; 

		this.selectedTab="";

		//override these classes to change the style
		this["class"]="dojoRemoteTabController"; // alt syntax: "class" is a reserved word in JS
		this.labelClass="dojoRemoteTab";
		this.imageClass="dojoRemoteTabClose";
		this.imageHoverClass="dojoRemoteTabCloseHover";
	},
	{
        templateCssPath: dojo.uri.dojoUri("src/widget/templates/RemoteTabControl.css"),
		templateString: '<div dojoAttachPoint="domNode" wairole="tablist"></div>',

		postCreate: function() {
			dojo.html.addClass(this.domNode, this["class"]);  // "class" is a reserved word in JS
			dojo.widget.wai.setAttr(this.domNode, "waiRole", "role", "tablist");

			if (this.tabContainer) {
				dojo.addOnLoad(dojo.lang.hitch(this, function() {
					this.setTabContainer(dojo.widget.byId(this.tabContainer));
				}));
			}
		},

		setTabContainer: function(/* dojo.widget.TabContainer */ tabContainer) {
			//summary
			//Connect this Remote Tab Controller to an existing TabContainer
			this._tabContainer = tabContainer;
			this.setupTabs();

			dojo.event.connect(this._tabContainer, "addChild", this, "setupTabs");
			dojo.event.connect(this._tabContainer, "selectTab", this, "onTabSelected");
		},

		setupTabs: function() {
			//summary
			//Setup tab buttons for each of the TabContainers tabs

			dojo.html.removeChildren(this.domNode);
			dojo.lang.forEach(this._tabContainer.children, this.addTab, this);
		},

		onTabSelected: function(/* dojo.widget.TabPane */tab) {
			//summary
			//Do this when a tab gets selected
			if (this.selectedTab.tab != tab.widgetId) {
				dojo.html.removeClass(this.selectedTab.button, "current");
			}

			this.selectedTab = this.tabs[tab.widgetId];
			dojo.html.addClass(this.selectedTab.button,"current");

		},

		addTab: function(/* dojo.widget.TabPane */tab) {
			//summary
			//Add a new button 

			// Create label
			div = document.createElement("div");
			dojo.html.addClass(div, this.labelClass);
			var innerDiv = document.createElement("div");

			// need inner span so focus rectangle is drawn properly
			var titleSpan = document.createElement("span");
			titleSpan.innerHTML = tab.label;
			titleSpan.tabIndex="-1";

			// set role on tab title
			dojo.widget.wai.setAttr(titleSpan, "waiRole", "role", "tab");
			innerDiv.appendChild(titleSpan);
			dojo.html.disableSelection(titleSpan);

			if(this._tabContainer.closeButton=="tab" || tab.tabCloseButton){
				var img = document.createElement("span");
				dojo.html.addClass(img, this.imageClass);
				dojo.event.connect(img, "onclick", dojo.lang.hitch(this, function(evt){
					this._runOnCloseTab(tab); dojo.event.browser.stopEvent(evt);
				}));
				dojo.event.connect(img, "onmouseover", dojo.lang.hitch(this, function(){
					dojo.html.addClass(img, this.imageHoverClass); 
				}));
				dojo.event.connect(img, "onmouseout", dojo.lang.hitch(this, function(){
					dojo.html.removeClass(img, this.imageHoverClass);
				}));
				innerDiv.appendChild(img);
			}

			// connect to _runOnCloseTab in case the tab pane or
			// another remote controller closes a tab
			if(this._tabContainer.closeButton=="tab" || this._tabContainer.closeButton=="pane"){
				dojo.event.connect(this._tabContainer, "_runOnCloseTab", dojo.lang.hitch(this, function(t){
					this._runOnRemoteClose(t);
				}));
			}

			div.appendChild(innerDiv);
			div.tabTitle=titleSpan;
			this.domNode.appendChild(div);

			var tabObj = {"tab": tab, "button": div};
			
			if (this._tabContainer.selectedTab == tab.widgetId || tab.selected) {
				this.selectedTab = tabObj;
				dojo.html.addClass(div, "current");
			}

			this.tabs[tab.widgetId] = tabObj;

			dojo.event.connect(div, "onclick", dojo.lang.hitch(this._tabContainer, function() {
				this.selectTab(tab); 
			}));

			dojo.event.connect(div, "onkeydown", dojo.lang.hitch(this._tabContainer, function(evt) {
				this.tabNavigation(evt, tab); 
			}));
		},

		_runOnCloseTab: function(tab){
			var tabObj = this.tabs[tab.widgetId];
			this.removeChild(tabObj);
			this._tabContainer._runOnCloseTab(tab);
		},

		_runOnRemoteClose: function(tab){
			var tabObj = this.tabs[tab.widgetId];
			if(!tabObj.button){ return; }
			this.removeChild(tabObj);
		},

		removeChild: function(tab){
			// remove tab event handlers
			dojo.event.disconnect(tab.button, "onclick", function(){ });
			if(this._tabContainer.closeButton == "tab"){
				var img = tab.button.lastChild.lastChild;
				if(img){
					dojo.html.removeClass(img, this.imageClass);
				}
			}

			dojo.widget.RemoteTabController.superclass.removeChild.call(this, tab);

			if(tab.domNode) { dojo.html.removeClass(tab.domNode, this.labelClass); }
			this.domNode.removeChild(tab.button);
			delete(tab.button);
		}
	},
	"html"
);
