dojo.provide("dojo.widget.InlineEditBox");

dojo.require("dojo.widget.*");
dojo.require("dojo.lfx.*");
dojo.require("dojo.graphics.color");
dojo.require("dojo.string");
dojo.require("dojo.html.*");
dojo.require("dojo.html.layout");

dojo.widget.defineWidget(
	"dojo.widget.InlineEditBox",
	dojo.widget.HtmlWidget,
	function(){
		// mutable objects need to be in constructor to give each instance its own copy
		this.history = [];
	},
{
	templatePath: dojo.uri.dojoUri("src/widget/templates/InlineEditBox.html"),
	templateCssPath: dojo.uri.dojoUri("src/widget/templates/InlineEditBox.css"),

	form: null,
	editBox: null,
	edit: null,
	text: null,
	textarea: null,
	submitButton: null,
	cancelButton: null,
	mode: "text",

	minWidth: 100, //px. minimum width of edit box
	minHeight: 200, //px. minimum width of edit box, if it's a TA

	editing: false,
	textValue: "",
	defaultText: "",
	doFade: false,
	
	onSave: function(newValue, oldValue){},
	onUndo: function(value){},

	postCreate: function(args, frag){
		// put original node back in the document, and attach handlers
		// which hide it and display the editor
		this.editable = this.getFragNodeRef(frag);
		dojo.html.insertAfter(this.editable, this.form);
		dojo.event.connect(this.editable, "onmouseover", this, "mouseover");
		dojo.event.connect(this.editable, "onmouseout", this, "mouseout");
		dojo.event.connect(this.editable, "onclick", this, "beginEdit");

		this.textValue = dojo.string.trim(this.editable.innerHTML);
		if(dojo.string.trim(this.textValue).length == 0){
			this.editable.innerHTML = this.defaultText;
		}		
	},

	mouseover: function(e){
		if(!this.editing){
			dojo.html.addClass(this.editable, "editableRegion");
			if(this.mode == "textarea"){
				dojo.html.addClass(this.editable, "editableTextareaRegion");
			}
		}
	},

	mouseout: function(e){
		if(!this.editing){
			dojo.html.removeClass(this.editable, "editableRegion");
			dojo.html.removeClass(this.editable, "editableTextareaRegion");
		}
	},

	// When user clicks the text, then start editing.
	// Hide the text and display the form instead.
	beginEdit: function(e){
		if(this.editing){ return; }
		this.mouseout();
		this.editing = true;

		// setup the form's <input> or <textarea> field, as specified by mode
		var ee = this[this.mode.toLowerCase()];
		ee.value = dojo.string.trim(this.textValue);
		ee.style.fontSize = dojo.html.getStyle(this.editable, "font-size");
		ee.style.fontWeight = dojo.html.getStyle(this.editable, "font-weight");
		ee.style.fontStyle = dojo.html.getStyle(this.editable, "font-style");
		var bb = dojo.html.getBorderBox(this.editable);
		ee.style.width = Math.max(bb.width, this.minWidth) + "px";
		if(this.mode.toLowerCase()=="textarea"){
			ee.style.display = "block";
			ee.style.height = Math.max(bb.height, this.minHeight) + "px";
		} else {
			ee.style.display = "";
		}

		// show the edit form and hide the read only version of the text
		this.form.style.display = "";
		this.editable.style.display = "none";

		ee.focus();
		ee.select();
		this.submitButton.disabled = true;
	},

	saveEdit: function(e){
		e.preventDefault();
		e.stopPropagation();
		var ee = this[this.mode.toLowerCase()];
		if((this.textValue != ee.value)&&
			(dojo.string.trim(ee.value) != "")){
			this.doFade = true;
			this.history.push(this.textValue);
			this.onSave(ee.value, this.textValue);
			this.textValue = ee.value;
			this.editable.innerHTML = "";
			var textNode = document.createTextNode( this.textValue );
			this.editable.appendChild( textNode );
		}else{
			this.doFade = false;
		}
		this.finishEdit(e);
	},

	cancelEdit: function(e){
		if(!this.editing){ return false; }
		this.editing = false;
		this.form.style.display="none";
		this.editable.style.display = "";
		return true;
	},

	finishEdit: function(e){
		if(!this.cancelEdit(e)){ return; }
		if(this.doFade) {
			dojo.lfx.highlight(this.editable, dojo.graphics.color.hex2rgb("#ffc"), 700).play(300);
		}
		this.doFade = false;
	},

	setText: function(txt){
		// sets the text without informing the server
		var tt = dojo.string.trim(txt);
		this.textValue = tt
		this.editable.innerHTML = tt;
	},

	undo: function(){
		if(this.history.length > 0){
			var value = this.history.pop();
			this.editable.innerHTML = value;
			this.textValue = value;
			this.onUndo(value);
		}
	},

	checkForValueChange: function(){
		var ee = this[this.mode.toLowerCase()];
		if((this.textValue != ee.value)&&
			(dojo.string.trim(ee.value) != "")){
			this.submitButton.disabled = false;
		}
	}
});
