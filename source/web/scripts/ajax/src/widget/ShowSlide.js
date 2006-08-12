dojo.provide("dojo.widget.ShowSlide");

dojo.require("dojo.widget.*");
dojo.require("dojo.lang.common");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.lfx.html");
dojo.require("dojo.html.display");
dojo.require("dojo.html.layout");
dojo.require("dojo.animation.Animation");
dojo.require("dojo.graphics.color");

dojo.widget.defineWidget(
	"dojo.widget.ShowSlide",
	dojo.widget.HtmlWidget,
{
	title: "",
	_action: -1,
	isContainer: true,
	_components: {},
	_actions: [],

	gotoAction: function(/*int*/ action){
		this._action = action;
	},
	_nextAction: function(/*Event?*/ event){
		if((this._action + 1) != this._actions.length){
			++this._action;
			return true; // boolean
		}
		return false; // boolean
	},
	_previousAction: function(/*Event?*/ event){
		if((this._action - 1) != -1){
			--this._action;
			return true; // boolean
		}
		return false; // boolean
	},

	htmlTitle: null,
	debug: false,
	noClick: false,
	templatePath: dojo.uri.dojoUri("src/widget/templates/ShowSlide.html"),
	templateCssPath: dojo.uri.dojoUri("src/widget/templates/ShowSlide.css"),
	fillInTemplate: function(){
		this.htmlTitle.innerHTML = this.title;

		this._components = {};
		var nodes = this.containerNode.all ? this.containerNode.all : this.containerNode.getElementsByTagName('*');
		for(var i = 0, node; node = nodes[i]; i++){
			var as = node.getAttribute("as");
			if(as){
				if(!this._components[as]){
					this._components[as] = [];
				}
				this._components[as].push(node);
			}
		}
	},
	postCreate: function(){
		this._actions = [];
		for(var i = 0, child; child = this.children[i]; i++){
			if(child.widgetType == "ShowAction"){
				this._actions.push(child);
				var components = this._components[child.on];
				for(var j = 0, component; component = components[j]; j++){
					if(child.action && child.action != "remove"){
						this.hideComponent(component);
					}
				}
			}
		}
	},
	previousAction: function(/*Event?*/ event){
		if(!this.parent.stopEvent(event)){
			return false;
		}

		var action = this._actions[this._action];
		if(!action){
			return false;
		}

		var on = action.on;
		while(action.on == on){
			var components = this._components[on];
			for(var i = 0, component; component = components[i]; i++){
				if(action.action == "remove"){
					if(component.style.display == "none"){
						component.style.display = "";
						component.style.visibility = "visible";
						var exits = true;
					}
				}else if(action.action){
					this.hideComponent(component);
				}
			}

			--this._action;

			if(exits){
				return true;
			}	

			if(action.auto == "true"){
				on = this._actions[this._action].on;
			}

			action = this._actions[this._action];
			if(!action){
				return false;
			}
		}
		return true;
	},
	hideComponent: function(/*Node*/ component){
		component.style.visibility = "hidden";
		component.style.backgroundColor = "transparent";
		var parent = component.parentNode;
		if((parent)&&(parent.tagName.toLowerCase() == "li")){
			parent.oldType = parent.style.listStyleType;
			parent.style.listStyleType = "none";
		}
	},
	nextAction: function(/*Event?*/ event){
		if(!this.parent.stopEvent(event)){
			return false;
		}

		if(!this._nextAction(this)){
			return false;
		}

		var action = this._actions[this._action];
		if(!action){
			return false;
		}
		
		var components = this._components[action.on];
		for(var i = 0, component; component = components[i]; i++){
			if(action.action){
				var duration = action.duration || 1000;
				if(action.action == "fade"){
					dojo.html.setOpacity(component, 0);
					dojo.lfx.html.fadeIn(component, duration).play(true);
				}else if(action.action == "fly"){
					var width = dojo.html.getMarginBox(component).width;
					var position = dojo.html.getAbsolutePosition(component);
					// alert(position);
					component.style.position = "relative";
					component.style.left = -(width + position.x) + "px";
					dojo.lfx.html.slideBy(component, { top: 0, left: (width + position.x)}, duration, -1, this.callWith).play(true);
				}else if(action.action == "wipe"){
					dojo.lfx.html.wipeIn(component, duration).play();
				}else if(action.action == "color"){
					var from = new dojo.graphics.color.Color(action.from).toRgb();
					var to = new dojo.graphics.color.Color(action.to).toRgb();
					var anim = new dojo.animation.Animation(new dojo.math.curves.Line(from, to), duration, 0);
					var node = component;
					dojo.event.connect(anim, "onAnimate", function(e) {
						node.style.color = "rgb(" + e.coordsAsInts().join(",") + ")";
					});
					anim.play(true);
				}else if(action.action == "bgcolor"){
					dojo.lfx.html.unhighlight(component, action.to, duration).play();
				}else if(action.action == "remove"){
					component.style.display = "none";
				}
				component.style.visibility = "visible";
			}
		}
		
		action = this._actions[this._action + 1];
		if(action && action.auto == "true"){
			this.nextAction();
		}

		return true;
	},
	callWith: function(/*Node*/ node){
		if(!node){ return; }
		if(dojo.lang.isArray(node)){
			dojo.lang.forEach(node, arguments.callee);
			return;
		}
		var parent = node.parentNode;
		if((parent)&&(parent.tagName.toLowerCase() == "li")){
			parent.style.listStyleType = parent.oldType;
		}
	}
});
