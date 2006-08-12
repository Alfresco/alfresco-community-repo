//
// this widget provides Delphi-style panel layout semantics
//

dojo.provide("dojo.widget.LayoutContainer");

dojo.require("dojo.widget.*");
dojo.require("dojo.widget.html.layout");

dojo.widget.defineWidget(
	"dojo.widget.LayoutContainer",
	dojo.widget.HtmlWidget,
{
	isContainer: true,

	layoutChildPriority: 'top-bottom',

	postCreate: function(){
		dojo.widget.html.layout(this.domNode, this.children, this.layoutChildPriority);
	},

	addChild: function(child, overrideContainerNode, pos, ref, insertIndex){
		dojo.widget.LayoutContainer.superclass.addChild.call(this, child, overrideContainerNode, pos, ref, insertIndex);
		dojo.widget.html.layout(this.domNode, this.children, this.layoutChildPriority);
	},

	removeChild: function(pane){
		dojo.widget.LayoutContainer.superclass.removeChild.call(this,pane);
		dojo.widget.html.layout(this.domNode, this.children, this.layoutChildPriority);
	},

	onResized: function(){
		dojo.widget.html.layout(this.domNode, this.children, this.layoutChildPriority);
	},

	show: function(){
		// If this node was created while display=="none" then it
		// hasn't been laid out yet.  Do that now.
		this.domNode.style.display="";
		this.checkSize();
		this.domNode.style.display="none";
		this.domNode.style.visibility="";

		dojo.widget.LayoutContainer.superclass.show.call(this);
	}
});

// This argument can be specified for the children of a LayoutContainer.
// Since any widget can be specified as a LayoutContainer child, mix it
// into the base widget class.  (This is a hack, but it's effective.)
dojo.lang.extend(dojo.widget.Widget, {
	layoutAlign: 'none'
});
