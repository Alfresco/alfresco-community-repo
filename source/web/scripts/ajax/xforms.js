dojo.require("dojo.widget.DebugConsole");
dojo.require("dojo.widget.DatePicker");
dojo.require("dojo.widget.Button");
dojo.require("dojo.widget.validate");
dojo.require("dojo.widget.ComboBox");
dojo.require("dojo.widget.Checkbox");
dojo.require("dojo.widget.Editor");
dojo.require("dojo.widget.Spinner");
dojo.require("dojo.html.style");
dojo.hostenv.writeIncludes();
dojo.addOnLoad(xforms_init);

tinyMCE.init({
      theme: "advanced",
      mode: "exact",
      encoding: null,
      save_callback : "setXFormsValue",
      add_unload_trigger: false,
      add_form_submit_trigger: false,
      theme_advanced_toolbar_location : "top",
      theme_advanced_toolbar_align : "left",
      theme_advanced_buttons1_add : "fontselect,fontsizeselect",
      theme_advanced_buttons2_add : "separator,forecolor,backcolor"
});

dojo.declare("alfresco.xforms.Widget",
	     null,
	     {
	     initializer: function(xform, node) 
	       {
		 this.xform = xform;
		 this.node = node;
		 this.id = this.node.getAttribute("id");
	       },
	     _getBinding: function()
	       {
		 return this.xform.getBinding(this.node);
	       },
	     isRequired: function()
	       {
		 var binding = this._getBinding();
		 var required = binding.required == "true()";
		 return required;
	       },
	     getInitialValue: function()
	       {
		 var b = this._getBinding();
		 var a = [];
		 do
		 {
		   a.push(b);
		   b = b.parent;
		 }
		 while (b);
		 var node = this.xform.getInstance();
		 for (var i = a.length - 1; i >= 0; i--)
		 {
		   var element_name = (a[i].nodeset.match(/^\//)
				       ? a[i].nodeset.replace(/^\/(.+)/, "$1")
				       : a[i].nodeset);
		   dojo.debug("locating " + a[i].nodeset + "(" + element_name + ")" +
			      " in " + node.nodeName);
		   if (element_name.match(/^@/))
		     return node.getAttribute(a[i].nodeset.replace(/^@(.+)/, "$1"));
		   else if (element_name == '.')
		     break;
		   node = node.getElementsByTagName(element_name)[0];
		   if (node)
		     dojo.debug("got node " + node.nodeName);
		   else
		     return null;
		 }
		 return dojo.dom.textContent(node);
	       },
	     appendHTML: function(attach_point)
	       {
		 var row = document.createElement("tr");
		 attach_point.appendChild(row);
		 var cell = document.createElement("td");
		 row.appendChild(cell);
		 if (this.isRequired())
		 {
		   var req = document.createElement("img");
		   req.setAttribute("src", WEBAPP_CONTEXT + "/images/icons/required_field.gif");
		   req.setAttribute("style", "margin:5px");
		   cell.appendChild(req);
		 }

		 var cell = document.createElement("td");
		 row.appendChild(cell);

		 var label = this._getLabelNode();
		 if (label)
		   cell.appendChild(document.createTextNode(dojo.dom.textContent(label)));
		 cell = document.createElement("td");
		 row.appendChild(cell);
		 this._appendHTMLForWidgetCell(cell);
	       },
	     _getLabelNode: function()
	       {
		 var labels = this.node.getElementsByTagName("label");
		 for (var i = 0; i < labels.length; i++)
		 {
		   dojo.debug("parent " + labels[i].parentNode.nodeName + 
			      " o " + this.node.nodeName);
		   if (labels[i].parentNode == this.node)
		     return labels[i];
		 }
		 return null;
	       }
	     });

dojo.declare("alfresco.xforms.NumericStepper",
	     alfresco.xforms.Widget,
	     {
	     initializer: function(xform, node, stepper_type) 
	       {
		 this.inherited("initializer", [ xform, node ]);
		 this.stepper_type = stepper_type;
	       },
	     _appendHTMLForWidgetCell: function(attach_point)
	       {
		 var nodeRef = document.createElement("div");
		 attach_point.appendChild(nodeRef);
		 var initial_value = this.getInitialValue() || "";
		 var w = dojo.widget.createWidget(this.stepper_type == "double"
						  ? "SpinnerRealNumberTextBox"
						  : "SpinnerIntegerTextBox", 
						  { 
						  widgetId: this.id,
						  required: this.isRequired(), 
						  value: initial_value 
						  }, 
						  nodeRef);
		 var handler = function(event)
		   {
		     dojo.debug("value changed " + w.widgetId + 
				" value " + w.getValue() + 
				" t " + event.target + 
				" w " + w + " this " + this);
		     setXFormsValue(w.widgetId, w.getValue());
		   }
		 dojo.event.connect(w, "adjustValue", handler);
		 dojo.event.connect(w, "onkeyup", handler);
	       }
	     });

dojo.declare("alfresco.xforms.DatePicker",
	     alfresco.xforms.Widget,
	     {
	     initializer: function(xform, node) 
	       {
		 this.inherited("initializer", [ xform, node ]);
		 dojo.debug("created a TextField");
	       },
	     _appendHTMLForWidgetCell: function(attach_point)
	       {
		 var initial_value = this.getInitialValue() || dojo.widget.DatePicker.util.toRfcDate();
		 var dateTextBoxDiv = document.createElement("div");
		 attach_point.appendChild(dateTextBoxDiv);
		 var dateTextBox = dojo.widget.createWidget("DateTextBox", 
							    {
							    widgetId: this.id,
							    required: this.isRequired(), 
							    format: "YYYY-MM-DD", 
							    value: initial_value 
							    }, 
							    dateTextBoxDiv);
		 dateTextBox.onfocus = function(o) 
		 { 
		   dateTextBox.hide(); dojo.debug("hiding " + o); 
		   dateTextBox.picker.show();
		 };
		 var datePickerDiv = document.createElement("div");
		 attach_point.appendChild(datePickerDiv);
		 dateTextBox.picker = dojo.widget.createWidget("DatePicker", 
							       { 
							       isHidden: true, 
							       value : initial_value 
							       }, 
							       datePickerDiv);
		 dateTextBox.picker.hide();
		 dojo.event.connect(dateTextBox.picker,
				    "onSetDate", 
				    function(event)
				    {
				      dateTextBox.picker.hide();
				      dateTextBox.show();
				      dateTextBox.setValue(dojo.widget.DatePicker.util.toRfcDate(dateTextBox.picker.date));
				      setXFormsValue(dateTextBox.widgetId, 
						     dateTextBox.getValue());
				    });
	       }
	     });

dojo.declare("alfresco.xforms.TextField",
	     alfresco.xforms.Widget,
	     {
	     initializer: function(xform, node) 
	       {
		 this.inherited("initializer", [ xform, node ]);
		 dojo.debug("created a TextField");
	       },
	     _appendHTMLForWidgetCell: function(attach_point)
	       {
		 var nodeRef = document.createElement("div");
		 attach_point.appendChild(nodeRef);
		 var initial_value = this.getInitialValue() || "";
		 var w = dojo.widget.createWidget("ValidationTextBox", 
						  {
						  widgetId: this.id,
						  required: this.isRequired(), 
						  value: initial_value 
						  }, 
						  nodeRef);
		 dojo.event.connect(w,
				    "onkeyup", 
				    function(event)
				    {
				      dojo.debug("value changed " + w.widgetId + 
						 " value " + w.getValue() + 
						 " t " + event.target + 
						 " w " + w + " this " + this);
				      setXFormsValue(w.widgetId, w.getValue());
				    });
	       }
	     });

dojo.declare("alfresco.xforms.TextArea",
	     alfresco.xforms.Widget,
	     {
	     initializer: function(xform, node) 
	       {
		 this.inherited("initializer", [ xform, node ]);
		 dojo.debug("created a TextArea");
	       },
	     _appendHTMLForWidgetCell: function(attach_point)
	       {
		 dojo.debug("xxx " + this.id);
		 var nodeRef = document.createElement("div");
		 attach_point.appendChild(nodeRef);
		 nodeRef.setAttribute("style", "height: 200px; width: 100%; border: solid 1px black;");
		 nodeRef.setAttribute("id", this.id);

		 nodeRef.innerHTML = this.getInitialValue() || "";
		 tinyMCE.addMCEControl(nodeRef, this.id);
	       }
	     });

dojo.declare("alfresco.xforms.Select1",
	     alfresco.xforms.Widget,
	     {
	     initializer: function(xform, node) 
	       {
		 this.inherited("initializer", [ xform, node ]);
	       },
	     getValues: function()
	       {
		 var values = this.node.getElementsByTagName("item");
		 var result = [];
		 for (var v in values)
		 {
		   if (values[v].getElementsByTagName)
		   {
		     var label = values[v].getElementsByTagName("label")[0];
		     var value = values[v].getElementsByTagName("value")[0];
		     result.push({ 
		       id: value.getAttribute("id"), 
		       label: dojo.dom.textContent(label),
                       value: dojo.dom.textContent(value)
		     });
		   }
		 }
		 return result;
	       },
	     _appendHTMLForWidgetCell: function(attach_point)
	       {
		 var values = this.getValues();
		 for (var i in values)
		 {
		   dojo.debug("values["+ i + "] = " + values[i].id + 
			      ", " + values[i].label + ", " + values[i].value);
		 }
		 var initial_value = this.getInitialValue();
		 if (values.length <= 5)
		 {
		   for (var i in values)
		   {
		     var radio = document.createElement("input");
		     radio.setAttribute("id", this.id);
		     radio.setAttribute("name", this.id);
		     radio.setAttribute("type", "radio");
		     radio.setAttribute("value", values[i].value);
		     if (values[i].value == initial_value)
		       radio.setAttribute("checked", "true");
		     radio.onclick = function(event) 
		       { 
			 setXFormsValue(this.getAttribute("id"),
					this.value);
		       }
		     attach_point.appendChild(radio);
		     attach_point.appendChild(document.createTextNode(values[i].label));
		   }
		 }
		 else
		 {
		   var combobox = document.createElement("select");
		   combobox.setAttribute("id", this.id);
		   attach_point.appendChild(combobox);
		   for (var i in values)
		   {
		     var option = document.createElement("option");
		     option.appendChild(document.createTextNode(values[i].label));
		     option.setAttribute("value", values[i].value);
		     if (values[i].value == initial_value)
		       option.setAttribute("selected", "true");
		     combobox.appendChild(option);
		   }
		   combobox.onchange = function(event) 
		     { 
		       setXFormsValue(this.getAttribute("id"),
				      this.options[this.selectedIndex].value);
		     }
		 }
	       }
	     });

dojo.declare("alfresco.xforms.CheckBox",
	     alfresco.xforms.Widget,
	     {
	     initializer: function(xform, node) 
	       {
		 this.inherited("initializer", [ xform, node ]);
	       },
	     _appendHTMLForWidgetCell: function(attach_point)
	       {
		 var nodeRef = document.createElement("div");
		 attach_point.appendChild(nodeRef);
		 var initial_value = this.getInitialValue() || false;
		 var w = dojo.widget.createWidget("CheckBox", 
						  { 
						  widgetId: this.id,
						  checked: initial_value 
						  },
						  nodeRef);
		 dojo.event.connect(w,
				    "onClick",
				    function(event)
				    {
				      setXFormsValue(w.widgetId, w.checked);
				    });
	       }
	     });

dojo.declare("alfresco.xforms.Group",
	     alfresco.xforms.Widget,
	     {
	     initializer: function(xform, node) 
	       {
		 this.inherited("initializer", [ xform, node ]);
	       },
	     appendHTML: function(attach_point)
	       {
		 var n = attach_point;
		 if (attach_point.nodeName.toLowerCase() == "table")
		 {
		   var tr = document.createElement("tr");
		   var td = document.createElement("td");
		   td.setAttribute("colspan", "3");
		   tr.appendChild(td);
		   attach_point.appendChild(tr);
		   n = td;
		 }
		 var table = document.createElement("table");
		 table.setAttribute("style", "width:100%; border: 0px solid blue;");
		 n.appendChild(table);
		 return table;
	       }
	     });

dojo.declare("alfresco.xforms.Submit",
	     alfresco.xforms.Widget,
	     {
	     initializer: function(xform, node) 
	       {
		 this.inherited("initializer", [ xform, node ]);
	       },
	     appendHTML: function(attach_point)
	       {
		 var row = document.createElement("tr");
		 attach_point.appendChild(row);
		 var cell = document.createElement("td");
		 cell.setAttribute("colspan", "3");
		 row.appendChild(cell);
		 var nodeRef = document.createElement("div");
		 cell.appendChild(nodeRef);
		 var w = dojo.widget.createWidget("Button", 
						  {
						  widgetId: this.id,
						  caption: "submit" 
						  }, 
						  nodeRef);
		 w.hide();
		 document.submitTrigger = w;
		 document.submitTrigger.done = false;
		 w.onClick = function()
		   {
		     fireAction(w.widgetId);
		   };
	       }
	     });

dojo.declare("alfresco.xforms.XForm",
	     null,
	     {
	     initializer: function(node)
	       {
		 this.node = node;
		 this._bindings = this._loadBindings(this.getModel());
	       },
	     getModel: function()
	       {
		 return this.node.getElementsByTagName("model")[0];
	       },
	     getInstance: function()
	       {
		 var model = this.getModel();
		 return model.getElementsByTagName("instance")[0];
	       },
	     getBody: function()
	       {
		 var b = this.node.getElementsByTagName("body");
		 return b[b.length - 1];
	       },
	     getType: function(node)
	       {
		 return this.getBinding(node).type;
	       },
	     getBinding: function(node)
	       {
		 return this._bindings[node.getAttribute("xforms:bind")];
	       },
	     getBindings: function()
	       {
		 return this._bindings;
	       },
		 _loadBindings: function(bind, parent, result)
	       {
		 result = result || [];
		 dojo.debug("loading bindings for " + bind.nodeName);
		 for (var i = 0; i < bind.childNodes.length; i++)
		 {
		   if (bind.childNodes[i].nodeName.toLowerCase() == "xforms:bind")
		   {
		     var id = bind.childNodes[i].getAttribute("id");
		     dojo.debug("loading binding " + id);
		     result[id] = {
                       id: bind.childNodes[i].getAttribute("id"),
		       required: bind.childNodes[i].getAttribute("xforms:required"),
		       nodeset: bind.childNodes[i].getAttribute("xforms:nodeset"),
		       type: bind.childNodes[i].getAttribute("xforms:type"),
		       parent: parent
		     };
		     this._loadBindings(bind.childNodes[i], result[id], result);
		   }
		 }
		 return result;
	       }
	     });

function xforms_init()
{
  var req = {
  url: WEBAPP_CONTEXT + "/ajax/invoke/XFormsBean.getXForm",
  content: { },
  mimetype: "text/xml",
  load: function(type, data, evt)
  {
    var xform = new alfresco.xforms.XForm(data.documentElement);
    var bindings = xform.getBindings();
    for (var i in bindings)
    {
      dojo.debug("bindings[" + i + "]=" + bindings[i].id + 
		 ", parent = " + (bindings[i].parent 
				  ? bindings[i].parent.id
				  : 'null'));
    }
    
    load_body(xform, xform.getBody(), document.getElementById("alf-ui"));
  },
  error: function(type, e)
  {
    alert("error!! " + type + " e = " + e.message);
  }
  };
  dojo.io.bind(req);
}

function load_body(xform, currentNode, domNode)
{
  dojo.lang.forEach(currentNode.childNodes, function(o)
  {
    dojo.debug("loading " + o + " NN " + o.nodeName);
    switch (o.nodeName.toLowerCase())
    {
    case "xforms:group":
      var w = new alfresco.xforms.Group(xform, o);
      load_body(xform, o, w.appendHTML(domNode));
      break;
    case "xforms:textarea":
      var w = new alfresco.xforms.TextArea(xform, o);
      w.appendHTML(domNode);
      break;
    case "xforms:input":
      var type = xform.getType(o);
      switch (type)
      {
      case "date":
	var w = new alfresco.xforms.DatePicker(xform, o);
	break;
      case "integer":
      case "positiveInteger":
      case "negativeInteger":
      case "double":
	var w = new alfresco.xforms.NumericStepper(xform, o, type);
	break;
      case "string":
      default:
	var w = new alfresco.xforms.TextField(xform, o);
      }
      w.appendHTML(domNode);
      break;
    case "xforms:select1":
      var w = (xform.getType(o) == "boolean"
	       ? new alfresco.xforms.CheckBox(xform, o)
	       : new alfresco.xforms.Select1(xform, o));
      w.appendHTML(domNode);
      break;
    case "xforms:submit":
      var w = new alfresco.xforms.Submit(xform, o);
      w.appendHTML(domNode);
      break;
    case "xforms:repeat":
      var w = new alfresco.xforms.Group(xform, o);
      load_body(xform, o, w.appendHTML(domNode));
      break;
    default:
      load_body(xform, o, domNode);
    }
  });
}

function fireAction(id)
{
  var req = {
  url: WEBAPP_CONTEXT + "/ajax/invoke/XFormsBean.fireAction",
  content: { id: id },
  mimetype: "text/xml",
  load: function(type, data, evt)
  {
      document.submitTrigger.done = true;
      document.submitTrigger.currentButton.click();
      document.submitTrigger.currentButton = null;
  },
  error: function(type, e)
  {
    alert("error!! " + type + " e = " + e.message);
  }
  };
  dojo.io.bind(req);
}

function setXFormsValue(id, value)
{
  var req = {
  url: WEBAPP_CONTEXT + "/ajax/invoke/XFormsBean.setXFormsValue",
  content: { id: id, value: value },
  mimetype: "text/xml",
  load: function(type, data, evt)
  {
  },
  error: function(type, e)
  {
    alert("error!! " + type + " e = " + e.message);
  }
  };
  dojo.io.bind(req);
}

function addSubmitHandlerToButton(b)
{
  var baseOnClick = b.onclick;
  b.onclick = function(event)
  {
//    alert("submitting xform from " + b.getAttribute("id") + 
//	  " b " + b +
//	  " this " + this );
    if (!document.submitTrigger.done)
    {
      //      alert("not done, resubmitting");
      tinyMCE.triggerSave();
      document.submitTrigger.currentButton = this;
      document.submitTrigger.buttonClick(); 
      return false;
    }
    else
    {
      //     alert("done - doing base click");
      return baseOnClick(event);
    }
  }
}
