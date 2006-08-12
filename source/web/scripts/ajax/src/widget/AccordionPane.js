dojo.provide("dojo.widget.AccordionPane");

dojo.require("dojo.widget.*");
dojo.require("dojo.widget.TitlePane");
dojo.require("dojo.html.selection");

dojo.widget.defineWidget(
	"dojo.widget.AccordionPane",
	dojo.widget.TitlePane,
{
	// parameters
	open: false,
	allowCollapse: true,
	label: "",
	labelNodeClass: "",
	containerNodeClass: "",
	
	// methods
    postCreate: function() {
		dojo.widget.AccordionPane.superclass.postCreate.call(this);
		this.domNode.widgetType=this.widgetType;
		this.setSizes();
		dojo.html.addClass(this.labelNode, this.labelNodeClass);
		dojo.html.disableSelection(this.labelNode);
		dojo.html.addClass(this.containerNode, this.containerNodeClass);
    },

	collapse: function() {
		//dojo.fx.html.wipeOut(this.containerNode,250);
		//var anim = dojo.fx.html.wipe(this.containerNode, 1000, this.containerNode.offsetHeight, 0, null, true);
		this.containerNode.style.display="none";
		this.open=false;
	},

	expand: function() {
		//dojo.fx.html.wipeIn(this.containerNode,250);
		this.containerNode.style.display="block";
		//var anim = dojo.fx.html.wipe(this.containerNode, 1000, 0, this.containerNode.scrollHeight, null, true);
		this.open=true;
	},

	getCollapsedHeight: function() {
		return dojo.html.getMarginBox(this.labelNode).height+1;
	},

	setSizes: function() {
		var siblings = this.domNode.parentNode.childNodes;
		var height=dojo.html.getBorderBox(this.domNode.parentNode).height-this.getCollapsedHeight();

		this.siblingWidgets = [];
	
		for (var x=0; x<siblings.length; x++) {
			if (siblings[x].widgetType==this.widgetType) {
				if (this.domNode != siblings[x]) {
					var ap = dojo.widget.byNode(siblings[x]);
					this.siblingWidgets.push(ap);
					height -= ap.getCollapsedHeight();
				}
			}
		}
	
		for (var x=0; x<this.siblingWidgets.length; x++) {
			dojo.html.setMarginBox(this.siblingWidgets[x].containerNode,{ height: height });
		}

		dojo.html.setMarginBox(this.containerNode,{ height: height});
	},

	onLabelClick: function() {
		this.setSizes();
		if (!this.open) { 
			for (var x=0; x<this.siblingWidgets.length;x++) {
				if (this.siblingWidgets[x].open) {
					this.siblingWidgets[x].collapse();
				}
			}
			this.expand();
		} else {
			if (this.allowCollapse) {
				this.collapse();
			}
		}
	}
});
