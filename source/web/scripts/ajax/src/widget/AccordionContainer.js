dojo.provide("dojo.widget.AccordionContainer");

dojo.require("dojo.widget.*");
dojo.require("dojo.html.*");
dojo.require("dojo.widget.AccordionPane");

dojo.widget.defineWidget(
	"dojo.widget.AccordionContainer",
	dojo.widget.HtmlWidget,
	{
		isContainer: true,
		labelNodeClass: "",
		containerNodeClass: "",
		allowCollapse: false,

		addChild: function(widget, overrideContainerNode, pos, ref, insertIndex){
			if (widget.widgetType != "AccordionPane") {
				var wrapper=dojo.widget.createWidget("AccordionPane",{label: widget.label, open: widget.open, labelNodeClass: this.labelNodeClass, containerNodeClass: this.containerNodeClass, allowCollapse: this.allowCollapse });
				wrapper.addChild(widget);
				this.addWidgetAsDirectChild(wrapper);
				this.registerChild(wrapper);
				wrapper.setSizes();
				return wrapper;
			} else {
				dojo.html.addClass(widget.containerNode, this.containerNodeClass);
				dojo.html.addClass(widget.labelNode, this.labelNodeClass);
				this.addWidgetAsDirectChild(widget);
				this.registerChild(widget);	
				widget.setSizes();
				return widget;
			}
	        },
	
		postCreate: function() {
			var tmpChildren = this.children;
			this.children=[];
			dojo.html.removeChildren(this.domNode);
			dojo.lang.forEach(tmpChildren, dojo.lang.hitch(this,"addChild"));
		},
	
		removeChild: function(widget) {
			dojo.widget.AccordionContainer.superclass.removeChild.call(this, widget);
			if(this.children[0]){
				this.children[0].setSizes();
			}
		},
		
		onResized: function(){
			this.children[0].setSizes();
		}
	}
);

// These arguments can be specified for the children of a Accordion
// Since any widget can be specified as a child, mix them
// into the base widget class.  (This is a hack, but it's effective.)
dojo.lang.extend(dojo.widget.Widget, {
	label: "",
	open: false
});
