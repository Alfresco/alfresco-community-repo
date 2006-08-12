dojo.provide("dojo.widget.Checkbox");

dojo.require("dojo.widget.*");
dojo.require("dojo.event");
dojo.require("dojo.html.style");

dojo.widget.defineWidget(
	"dojo.widget.Checkbox",
	dojo.widget.HtmlWidget,
	{
		templatePath: dojo.uri.dojoUri('src/widget/templates/Checkbox.html'),
		templateCssPath: dojo.uri.dojoUri('src/widget/templates/Checkbox.css'),

		// parameters
		disabled: "enabled",
		name: "",
		checked: false,
		tabIndex: "0",
		id: "",
		inputNode: null,

		postMixInProperties: function(){
			// set the variables referenced by the template
			this.disabledStr = this.disabled=="enabled" ? "" : "disabled";
		},

		postCreate: function(args, frag){
			// find any associated label and create a labelled-by relationship
			// assumes <label for="inputId">label text </label> rather than
			// <label><input type="xyzzy">label text</label> 
			this.inputNode.id = this.widgetId;  
			var inputId = this.inputNode.id;
			if(inputId != null){
				var labels = document.getElementsByTagName("label");
				if (labels != null && labels.length > 0){
					for(var i=0; i<labels.length; i++){
						if (labels[i].htmlFor == inputId){
							labels[i].id = (labels[i].htmlFor + "label"); 
							dojo.widget.wai.setAttr(this.domNode, "waiState", "labelledby", labels[i].id);
							break;
						}
					}
				}
			}
		},

		fillInTemplate: function(){
			this._setInfo();
		},

		onClick: function(e){
			if(this.disabled == "enabled"){
				this.checked = !this.checked;
				this._setInfo();
			}
			e.preventDefault();
		},

		keyPress: function(e){
			var k = dojo.event.browser.keys;
			if(e.keyCode==k.KEY_SPACE || e.charCode==k.KEY_SPACE){
	 			this.onClick(e);
	 		}
		},

		// set CSS class string according to checked/unchecked and disabled/enabled state
		_setInfo: function(){
			var prefix = (this.disabled == "enabled" ? "dojoHtmlCheckbox" : "dojoHtmlCheckboxDisabled");
			var state = prefix + (this.checked ? "On" : "Off");
			dojo.html.setClass(this.domNode, "dojoHtmlCheckbox " + state);
			this.inputNode.checked = this.checked;
			dojo.widget.wai.setAttr(this.domNode, "waiState", "checked", this.checked);
		}
	}
);

