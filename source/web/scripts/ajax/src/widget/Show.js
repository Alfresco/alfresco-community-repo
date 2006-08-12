dojo.provide("dojo.widget.Show");

dojo.require("dojo.widget.*");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.uri.Uri");
dojo.require("dojo.event");
dojo.require("dojo.animation.Animation");
dojo.require("dojo.math.curves");
dojo.require("dojo.lang.common");
dojo.require("dojo.lang.func");

dojo.widget.defineWidget(
	"dojo.widget.Show",
	dojo.widget.HtmlWidget,
	function(){
		this._slides=[];
	},
{
	isContainer: true,
	_slide: -1,

	body: null,
	nav: null,
	hider: null,
	select: null,
	option: null,
	inNav: false,
	debugPane: null,
	noClick: false,
	templatePath: dojo.uri.dojoUri("src/widget/templates/Show.html"),
	templateCssPath: dojo.uri.dojoUri("src/widget/templates/Show.css"),
	fillInTemplate: function(args, frag){
		if (args.debugPane) {
			var dp = this.debugPane = dojo.widget.byId(args.debugPane);
			dp.hide();
			dojo.event.connect(dp, "closeWindow", dojo.lang.hitch(this, function(){ this.debugPane = false; }));
		}
		var source = this.getFragNodeRef(frag);
		this.sourceNode = dojo.body().appendChild(source.cloneNode(true));
		for(var i = 0, child; child = this.sourceNode.childNodes[i]; i++){
			if(child.tagName && child.getAttribute("dojotype").toLowerCase() == "showslide"){
				child.className = "dojoShowPrintSlide";
				child.innerHTML = "<h1>" + child.title + "</h1>" + child.innerHTML;
			}
		}
		this.sourceNode.className = "dojoShowPrint";
		this.sourceNode.style.display = "none";
		
		dojo.event.connect(document, "onclick", this, "gotoSlideByEvent");
		if(dojo.render.html.ie) {
			dojo.event.connect(document,"onkeydown",this, "gotoSlideByEvent");
		} else {
			// while keydown works, keypress allows rapid successive key presses
			// to be handled correctly
			dojo.event.connect(document,"onkeypress",this, "gotoSlideByEvent");
		}
		dojo.event.connect(window, "onresize", this, "resizeWindow");
		dojo.event.connect(this.nav, "onmousemove", this, "popUpNav");
	},
	postCreate: function(){		
		this._slides = [];
		for(var i = 0, child; child = this.children[i]; i++){
			if(child.widgetType == "ShowSlide"){
				this._slides.push(child);
				this.option.text = child.title;
				this.option.parentNode.insertBefore(this.option.cloneNode(true), this.option);
			}
		}
		this.option.parentNode.removeChild(this.option);

		dojo.body().style.display = "block";
		this.resizeWindow();
		this.gotoSlide(0);
	},
	gotoSlide: function(/*int*/ slide){
		if(slide == this._slide){
			return;
		}

		if(!this._slides[slide]){
			// slide: string
			for(var i = 0, child; child = this._slides[i]; i++){
				if(child.title == slide){
					slide = i;
					break;
				}
			}
		}
		
		if(!this._slides[slide]){
			return;
		}

		if(this.debugPane){
			if(this._slides[slide].debug){
				this.debugPane.show();
			}else{
				this.debugPane.hide();
			}
		}
		
		if(this._slide != -1){
			while(this._slides[this._slide].previousAction()){}
		}
		
		this._slide = slide;
		this.select.selectedIndex = slide;
		while(this.contentNode.hasChildNodes()){ this.contentNode.removeChild(this.contentNode.firstChild); }
		this.contentNode.appendChild(this._slides[slide].domNode);
	},
	gotoSlideByEvent: function(/*Event*/ event){
		var node = event.target;
		var type = event.type;
		if(type == "click"){
			if(node.tagName == "OPTION" && node.parentNode == this.select){
				this.gotoSlide(node.index);
			}else if(node == this.select){
				this.gotoSlide(node.selectedIndex);
			}else{
				this.nextSlide(event);
			}
		}else if (type=="keydown" || type=="keypress") {
			var key = event.keyCode;
			var ch = event.charCode;
			if(key == 63234 || key == 37){
				this.previousSlide(event);
			}else if(key == 63235 || key == 39 || ch == 32){
				this.nextSlide(event);
			}
		}
	},
	nextSlide: function(/*Event?*/ event){
		if(!this.stopEvent(event)){
			return false;
		}
		if(!this._slides[this._slide].nextAction(event)){
			if((this._slide + 1) != this._slides.length){
				this.gotoSlide(this._slide + 1);
				return true; // boolean
			}
			return false; // boolean
		}
	},
	previousSlide: function(/*Event?*/ event){
		if(!this.stopEvent(event)){
			return false;
		}
		if(!this._slides[this._slide].previousAction(event)){
			if((this._slide - 1) != -1){
				this.gotoSlide(this._slide - 1);
				return true; // boolean
			}
			return false; // boolean
		}
	},

	stopEvent: function(/*Event*/ ev){
		if(!ev){
			return true;
		}
	
		if (ev.type == "click" && (this._slides[this._slide].noClick || this.noClick)) {
			return false;
		}	
		var target = ev.target;
		// Check to see if the target is below the show domNode
		while(target != null){
			if(target == this.domNode){
				target = ev.target;
				break;
			}
			target = target.parentNode;
		}
		// Now that we know it's below this widget's domNode, we bubble up until we get to our domNode
		while(target && target != this.domNode){
			if(target.tagName == "A" || target.tagName == "INPUT" || target.tagName == "TEXTAREA" || target.tagName == "SELECT"){
				return false;
			}
			if(typeof target.onclick == "function" || typeof target.onkeypress == "function"){
				return false;
			}
			target = target.parentNode;
		}
		
		if(window.event){
			ev.returnValue = false;
			ev.cancelBubble = true;
		}else{
			ev.preventDefault();
			ev.stopPropagation();
		}
		
		return true;
	},
	popUpNav: function(){
		if(!this.inNav){
			dojo.widget.Show.node = this.nav;
			var anim = new dojo.animation.Animation(new dojo.math.curves.Line([5], [30]), 250, -1);
			dojo.event.connect(anim, "onAnimate", function(e) {
				dojo.widget.Show.node.style.height = e.x + "px";
			});
			dojo.event.connect(anim, "onEnd", function(e) {
				dojo.widget.Show.node.style.height = e.x + "px";
			});
			anim.play(true);
		}
		clearTimeout(this.inNav);
		this.inNav = setTimeout(dojo.lang.hitch(this, "hideNav"), 2000);
	},
	hideNav: function(){
		clearTimeout(this.inNav);
		this.inNav = false;

		dojo.widget.Show.node = this.nav;
		var anim = new dojo.animation.Animation(new dojo.math.curves.Line([30], [5]), 250, 1);
		dojo.event.connect(anim, "onAnimate", function(e) {
			dojo.widget.Show.node.style.height = e.x + "px";
		});
		dojo.event.connect(anim, "onEnd", function(e) {
			dojo.widget.Show.node.style.height = e.x + "px";
		});
		anim.play(true);
	},
	resizeWindow: function(/*Event*/ ev){
		dojo.body().style.height = "auto";
		var h = Math.max(
			document.documentElement.scrollHeight || dojo.body().scrollHeight,
			dojo.html.getViewport().height);
		dojo.body().style.height = h + "px";
	}
});
