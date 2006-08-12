dojo.provide("dojo.widget.DropdownContainer");
dojo.require("dojo.widget.*");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.widget.Menu2");		// for PopupContainer
dojo.require("dojo.event.*");
dojo.require("dojo.html.layout");
dojo.require("dojo.html.display");
dojo.require("dojo.html.iframe");
dojo.require("dojo.html.util");

dojo.widget.defineWidget(
	"dojo.widget.DropdownContainer",
	dojo.widget.HtmlWidget,
	{
		inputWidth: "7em",
		inputId: "",
		inputName: "",
		iconURL: dojo.uri.dojoUri("src/widget/templates/images/combo_box_arrow.png"),
		iconAlt: "",

		inputNode: null,
		buttonNode: null,
		containerNode: null,

		containerToggle: "plain",
		containerToggleDuration: 150,
		containerAnimInProgress: false,

		templateString: '<span style="white-space:nowrap"><input type="text" value="" style="vertical-align:middle;" dojoAttachPoint="inputNode" autocomplete="off" /> <img src="${this.iconURL}" alt="${this.iconAlt}" dojoAttachEvent="onclick: onIconClick;" dojoAttachPoint="buttonNode" style="vertical-align:middle; cursor:pointer; cursor:hand;" /></span>',
		templateCssPath: "",

		fillInTemplate: function(args, frag){
			var source = this.getFragNodeRef(frag);

			this.popup = dojo.widget.createWidget("PopupContainer", {toggle: this.containerToggle});

			this.containerNode = this.popup.domNode;

			this.domNode.appendChild(this.popup.domNode);

			if(this.inputId){ this.inputNode.id = this.inputId; }
			if(this.inputName){ this.inputNode.name = this.inputName; }
			this.inputNode.style.width = this.inputWidth;

			dojo.event.connect(this.inputNode, "onchange", this, "onInputChange");
		},

		onIconClick: function(evt){
			if(!this.popup.isShowingNow){
				this.popup.open(this.inputNode, this, this.buttonNode);
			}else{
				this.popup.close();
			}
		},

		hideContainer: function(){
			this.popup.close();
		},

		onInputChange: function(){}
	}
);
