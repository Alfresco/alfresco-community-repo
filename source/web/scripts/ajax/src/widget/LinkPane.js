//
// a div that loads from a URL.  (Similar to an iframe, but
// it's in the same environment as the main window)
//

dojo.provide("dojo.widget.LinkPane");

dojo.require("dojo.widget.*");
dojo.require("dojo.widget.ContentPane");
dojo.require("dojo.html.style");

dojo.widget.defineWidget(
	"dojo.widget.LinkPane",
	dojo.widget.ContentPane,
{
	// I'm using a template because the user may specify the input as
	// <a href="foo.html">label</a>, in which case we need to get rid of the
	// <a> because we don't want a link.
	templateString: '<div class="dojoLinkPane"></div>',

	fillInTemplate: function(args, frag){
		var source = this.getFragNodeRef(frag);

		// If user has specified node contents, they become the label
		// (the link must be plain text)
		this.label += source.innerHTML;

		var source = this.getFragNodeRef(frag);
		dojo.html.copyStyle(this.domNode, source);
	}
});
