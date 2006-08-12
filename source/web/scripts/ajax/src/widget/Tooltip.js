dojo.provide("dojo.widget.Tooltip");
dojo.require("dojo.widget.ContentPane");
dojo.require("dojo.widget.Menu2");
dojo.require("dojo.uri.Uri");
dojo.require("dojo.widget.*");
dojo.require("dojo.event");
dojo.require("dojo.html.style");
dojo.require("dojo.html.util");
dojo.require("dojo.html.iframe");

dojo.widget.defineWidget(
	"dojo.widget.Tooltip",
	[dojo.widget.ContentPane, dojo.widget.PopupContainerBase],
	{
		isContainer: true,

		// Constructor arguments
		caption: "",
		showDelay: 500,
		hideDelay: 100,
		connectId: "",

		templateCssPath: dojo.uri.dojoUri("src/widget/templates/TooltipTemplate.css"),

		connectNode: null,

		fillInTemplate: function(args, frag){
			if(this.caption != ""){
				this.domNode.appendChild(document.createTextNode(this.caption));
			}
			this.connectNode = dojo.byId(this.connectId);
			dojo.widget.Tooltip.superclass.fillInTemplate.call(this, args, frag);

			this.addOnLoad(this, "_LoadedContent");
			dojo.html.addClass(this.domNode, "dojoTooltip");

			//copy style from input node to output node
			var source = this.getFragNodeRef(frag);
			dojo.html.copyStyle(this.domNode, source);

			//apply the necessary css rules to the node so that it can popup
			this.applyPopupBasicStyle();
		},

		postCreate: function(args, frag){
			dojo.event.connect(this.connectNode, "onmouseover", this, "onMouseOver");
			dojo.widget.Tooltip.superclass.postCreate.call(this, args, frag);
		},

		onMouseOver: function(e) {
			this.mouse = {x: e.pageX, y: e.pageY};

			if(!this.showTimer){
				this.showTimer = setTimeout(dojo.lang.hitch(this, "open"), this.showDelay);
				dojo.event.connect(document.documentElement, "onmousemove", this, "onMouseMove");
			}
		},

		onMouseMove: function(e) {
			this.mouse = {x: e.pageX, y: e.pageY};

			if(dojo.html.overElement(this.connectNode, e) || dojo.html.overElement(this.domNode, e)){
				// If the tooltip has been scheduled to be erased, cancel that timer
				// since we are hovering over element/tooltip again
				if(this.hideTimer) {
					clearTimeout(this.hideTimer);
					delete this.hideTimer;
				}
			} else {
				// mouse has been moved off the element/tooltip
				// note: can't use onMouseOut to detect this because the "explode" effect causes
				// spurious onMouseOut/onMouseOver events (due to interference from outline)
				if(this.showTimer){
					clearTimeout(this.showTimer);
					delete this.showTimer;
				}
				if(this.isShowingNow && !this.hideTimer){
					this.hideTimer = setTimeout(dojo.lang.hitch(this, "close"), this.hideDelay);
				}
			}
		},

		open: function() {
			if (this.isShowingNow) { return; }

			dojo.widget.PopupContainerBase.prototype.open.call(this, this.mouse.x, this.mouse.y, null, [this.mouse.x, this.mouse.y], "TL,TR,BL,BR", [10,15]);
		},

		close: function() {
			if (this.isShowingNow) {
				if ( this.showTimer ) {
					clearTimeout(this.showTimer);
					delete this.showTimer;
				}
				if ( this.hideTimer ) {
					clearTimeout(this.hideTimer);
					delete this.hideTimer;
				}
				dojo.event.disconnect(document.documentElement, "onmousemove", this, "onMouseMove");
				dojo.widget.PopupContainerBase.prototype.close.call(this);
			}
		},

		position: function(){
			this.move(this.mouse.x, this.mouse.y, [10,15], "TL,TR,BL,BR");
		},

		_LoadedContent: function(){
			if(this.isShowingNow){
				// the tooltip has changed size due to downloaded contents, so reposition it
				this.position();
			}
		},

		checkSize: function() {
			// checkSize() is called when the user has resized the browser window,
			// but that doesn't affect this widget (or this widget's children)
			// so it can be safely ignored
		}
	}
);
