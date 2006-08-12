dojo.provide("dojo.widget.Dialog");

dojo.require("dojo.widget.*");
dojo.require("dojo.widget.ContentPane");
dojo.require("dojo.event.*");
dojo.require("dojo.graphics.color");
dojo.require("dojo.html.layout");
dojo.require("dojo.html.display");
dojo.require("dojo.html.iframe");

dojo.declare(
	"dojo.widget.ModalDialogBase", 
	null,
	{
		isContainer: true,
		_scrollConnected: false,

		// provide a focusable element or element id if you need to
		// work around FF's tendency to send focus into outer space on hide
		focusElement: "",

		shared: {bg: null, bgIframe: null},
		bgColor: "black",
		bgOpacity: 0.4,
		followScroll: true,
		_fromTrap: false,

		trapTabs: function(e){
			if(e.target == this.tabStart) {
				if(this._fromTrap) {
					this._fromTrap = false;
				} else {
					this._fromTrap = true;
					this.tabEnd.focus();
				}
			} else if(e.target == this.tabEnd) {
				if(this._fromTrap) {
					this._fromTrap = false;
				} else {
					this._fromTrap = true;
					this.tabStart.focus();
				}
			}
		},

		clearTrap: function(e) {
			var _this = this;
			setTimeout(function() {
				_this._fromTrap = false;
			}, 100);
		},

		//if the target mixin class already defined postCreate,
		//dojo.widget.ModalDialogBase.prototype.postCreate.call(this)
		//should be called in its postCreate()
		postCreate: function() {
			with(this.domNode.style) {
				position = "absolute";
				zIndex = 999;
				display = "none";
				overflow = "visible";
			}
			var b = dojo.body();
			b.appendChild(this.domNode);

			if(!this.shared.bg){
				this.shared.bg = document.createElement("div");
				this.shared.bg.className = "dialogUnderlay";
				with(this.shared.bg.style) {
					position = "absolute";
					left = top = "0px";
					zIndex = 998;
					display = "none";
				}
				this.setBackgroundColor(this.bgColor);
				b.appendChild(this.shared.bg);
	
				this.shared.bgIframe = new dojo.html.BackgroundIframe(this.shared.bg);
			}
		},

		setBackgroundColor: function(color) {
			if(arguments.length >= 3) {
				color = new dojo.graphics.color.Color(arguments[0], arguments[1], arguments[2]);
			} else {
				color = new dojo.graphics.color.Color(color);
			}
			this.shared.bg.style.backgroundColor = color.toString();
			return this.bgColor = color;
		},

		setBackgroundOpacity: function(op) {
			if(arguments.length == 0) { op = this.bgOpacity; }
			dojo.html.setOpacity(this.shared.bg, op);
			try {
				this.bgOpacity = dojo.html.getOpacity(this.shared.bg);
			} catch (e) {
				this.bgOpacity = op;
			}
			return this.bgOpacity;
		},

		sizeBackground: function() {
			if(this.bgOpacity > 0) {
				var viewport = dojo.html.getViewport();
				var h = Math.max(
					dojo.doc().documentElement.scrollHeight || dojo.body().scrollHeight,
					viewport.height);
				var w = viewport.width;
				this.shared.bg.style.width = w + "px";
				this.shared.bg.style.height = h + "px";
			}
			this.shared.bgIframe.onResized();
		},

		showBackground: function() {
			this.sizeBackground();
			if(this.bgOpacity > 0) {
				this.shared.bg.style.display = "block";
			}
		},

		placeModalDialog: function() {
			var scroll_offset = dojo.html.getScroll().offset;
			var viewport_size = dojo.html.getViewport();

			// find the size of the dialog
			var mb = dojo.html.getMarginBox(this.containerNode);

			var x = scroll_offset.x + (viewport_size.width - mb.width)/2;
			var y = scroll_offset.y + (viewport_size.height - mb.height)/2;

			with(this.domNode.style) {
				left = x + "px";
				top = y + "px";
			}
		},

		//if the target mixin class already defined show,
		//dojo.widget.ModalDialogBase.prototype.show.call(this)
		//should be called in its show()
		show: function() {
			this.setBackgroundOpacity();
			this.showBackground();

			this.inherited("show");
		},

		//if the target mixin class already defined hide,
		//dojo.widget.ModalDialogBase.prototype.hide.call(this)
		//should be called in its hide()
		hide: function(){
			// workaround for FF focus going into outer space
			if (this.focusElement) { 
				dojo.byId(this.focusElement).focus(); 
				dojo.byId(this.focusElement).blur();
			}

			this.shared.bg.style.display = "none";
			this.shared.bg.style.width = this.shared.bg.style.height = "1px";
			
			this.inherited("hide");
		}
	});

dojo.widget.defineWidget(
	"dojo.widget.Dialog",
	[dojo.widget.ContentPane, dojo.widget.ModalDialogBase],
	{
		templatePath: dojo.uri.dojoUri("src/widget/templates/Dialog.html"),

		anim: null,
		blockDuration: 0,
		lifetime: 0,

		show: function() {
			dojo.widget.ModalDialogBase.prototype.show.call(this);

			if (this.followScroll && !this._scrollConnected){
				this._scrollConnected = true;
				dojo.event.connect(window, "onscroll", this, "onScroll");
			}
			
			if(this.lifetime){
				this.timeRemaining = this.lifetime;
				if(!this.blockDuration){
					dojo.event.connect(this.shared.bg, "onclick", this, "hide");
				}else{
					dojo.event.disconnect(this.shared.bg, "onclick", this, "hide");
				}
				if(this.timerNode){
					this.timerNode.innerHTML = Math.ceil(this.timeRemaining/1000);
				}
				if(this.blockDuration && this.closeNode){
					if(this.lifetime > this.blockDuration){
						this.closeNode.style.visibility = "hidden";
					}else{
						this.closeNode.style.display = "none";
					}
				}
				this.timer = setInterval(dojo.lang.hitch(this, "onTick"), 100);
			}

			this.checkSize();
		},

		onLoad: function(){
			// when href is specified we need to reposition
			// the dialog after the data is loaded
			this.placeModalDialog();
		},
		
		fillInTemplate: function(){
			// dojo.event.connect(this.domNode, "onclick", this, "killEvent");
		},

		hide: function(){
			dojo.widget.ModalDialogBase.prototype.hide.call(this)

			if(this.timer){
				clearInterval(this.timer);
			}

			if (this._scrollConnected){
				this._scrollConnected = false;
				dojo.event.disconnect(window, "onscroll", this, "onScroll");
			}
		},
		
		setTimerNode: function(node){
			this.timerNode = node;
		},

		setCloseControl: function(node) {
			this.closeNode = node;
			dojo.event.connect(node, "onclick", this, "hide");
		},

		setShowControl: function(node) {
			dojo.event.connect(node, "onclick", this, "show");
		},
		
		onTick: function(){
			if(this.timer){
				this.timeRemaining -= 100;
				if(this.lifetime - this.timeRemaining >= this.blockDuration){
					dojo.event.connect(this.shared.bg, "onclick", this, "hide");
					if(this.closeNode){
						this.closeNode.style.visibility = "visible";
					}
				}
				if(!this.timeRemaining){
					clearInterval(this.timer);
					this.hide();
				}else if(this.timerNode){
					this.timerNode.innerHTML = Math.ceil(this.timeRemaining/1000);
				}
			}
		},

		onScroll: function(){
			this.placeModalDialog();
			this.domNode.style.display = "block";
		},

		// Called when the browser window's size is changed
		checkSize: function() {
			if(this.isShowing()){
				this.sizeBackground();
				this.placeModalDialog();
				this.domNode.style.display="block";
				this.onResized();
			}
		},
		
		killEvent: function(evt){
			evt.preventDefault();
			evt.stopPropagation();
		}

	}
);
