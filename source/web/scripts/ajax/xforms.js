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
      save_callback : "document.xform.setXFormsValue",
      add_unload_trigger: false,
      add_form_submit_trigger: false,
      theme_advanced_toolbar_location : "top",
      theme_advanced_toolbar_align : "left",
      theme_advanced_buttons1_add : "fontselect,fontsizeselect",
      theme_advanced_buttons2_add : "separator,forecolor,backcolor"
});

var control_images = [ "plus", "minus", "arrow_up", "arrow_down" ];
for (var i in control_images)
{
  var s = control_images[i];
  control_images[i] = new Image();
  control_images[i].src = s;
}

dojo.declare("alfresco.xforms.Widget",
	     null,
	     {
	     initializer: function(xform, node) 
	       {
		 this.xform = xform;
		 this.node = node;
		 this.node.widget = this;
		 this.id = this.node.getAttribute("id");
	       },
             parent: null,
             domContainer: null,
	     _getBinding: function()
	     {
	       return this.xform.getBinding(this.node);
	     },
	     getDepth: function()
	     {
	       var result = 1;
	       var p = this.parent;
	       while (p)
	       {
		 result++;
		 p = p.parent;
	       }
	       return result;
	     },
	     isRequired: function()
	     {
	       var binding = this._getBinding();
	       var required = binding && binding.required == "true()";
	       return required;
	     },
	     getInitialValue: function()
	     {
	       var chibaData = this.node.getElementsByTagName("data");
	       if (chibaData.length == 0)
		 return null;

	       chibaData = chibaData[chibaData.length - 1];
	       var xpath = "/" + chibaData.getAttribute("chiba:xpath");
	       var d = this.node.ownerDocument;
	       var nsResolver = d.createNSResolver(d);
	       var contextNode = this.xform.getInstance();
	       dojo.debug("locating " + xpath + 
			  " from " + chibaData.nodeName + 
			  " in " + contextNode.nodeName);
	       var result = d.evaluate(xpath, 
				       this.xform.getInstance(), 
				       nsResolver, 
				       XPathResult.STRING_TYPE, 
				       null);
	       return result.stringValue;
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
	     },
	     getLabel: function()
	     {
	       var node = this._getLabelNode();
	       return node ? dojo.dom.textContent(node) : "";
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
	     render: function(attach_point)
	     {
	       var nodeRef = document.createElement("div");
	       attach_point.appendChild(nodeRef);
	       var initial_value = this.getInitialValue() || "";
	       var w = dojo.widget.createWidget((this.stepper_type == "double"
						 ? "SpinnerRealNumberTextBox"
						 : "SpinnerIntegerTextBox"), 
						{ 
						widgetId: this.id,
						required: this.isRequired(), 
						value: initial_value 
						}, 
						nodeRef);
	       w.widget = this;
	       this.widget = w;
	       dojo.event.connect(w, "adjustValue", this, this._widget_changeHandler);
	       dojo.event.connect(w, "onkeyup", this, this._widget_changeHandler);
	     },
	     getValue: function()
	     {
	       return this.widget.getValue();
	     },
             _widget_changeHandler: function(event)
	     {
	       this.xform.setXFormsValue(this.id, this.getValue());
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
	     render: function(attach_point)
	       {
		 var initial_value = this.getInitialValue() || dojo.widget.DatePicker.util.toRfcDate();
		 var dateTextBoxDiv = document.createElement("div");
		 attach_point.appendChild(dateTextBoxDiv);
		 this.dateTextBox = dojo.widget.createWidget("DateTextBox", 
							    {
							    widgetId: this.id,
							    required: this.isRequired(), 
							    format: "YYYY-MM-DD", 
							    value: initial_value 
							    }, 
							    dateTextBoxDiv);
		 dojo.event.connect(this.dateTextBox, 
				    "onfocus", 
				    this, 
				    this._dateTextBox_focusHandler);
		 var datePickerDiv = document.createElement("div");
		 attach_point.appendChild(datePickerDiv);
		 this.dateTextBox.picker = dojo.widget.createWidget("DatePicker", 
								    { 
								    isHidden: true, 
								    value : initial_value 
								    }, 
								    datePickerDiv);
		 this.dateTextBox.picker.hide();
		 dojo.event.connect(this.dateTextBox.picker,
				    "onSetDate", 
				    this,
				    this._datePicker_setDateHandler);
	       },
	     _dateTextBox_focusHandler: function(event)
             {
		 this.dateTextBox.hide(); 
		 this.dateTextBox.picker.show();
		 this.domContainer.style.height = 
		   this.dateTextBox.picker.domNode.offsetHeight + "px";
             },
	     _datePicker_setDateHandler: function(event)
	     {
	       this.dateTextBox.picker.hide();
	       this.dateTextBox.show();
	       this.domContainer.style.height = 
		 this.dateTextBox.domNode.offsetHeight + "px";
	       this.dateTextBox.setValue(dojo.widget.DatePicker.util.toRfcDate(this.dateTextBox.picker.date));
	       this.xform.setXFormsValue(this.dateTextBox.widgetId, 
					 this.dateTextBox.getValue());
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
	     render: function(attach_point)
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
		 w.widget = this;
		 this.widget = w;
		 dojo.event.connect(w, "onkeyup", this, this._widget_keyUpHandler);
	       },
	     getValue: function()
	       {
		 return this.widget.getValue();
	       },
	     _widget_keyUpHandler: function(event)
	       {
		 this.xform.setXFormsValue(this.id, this.getValue());
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
	     render: function(attach_point)
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
		 var binding = this._getBinding();
		 var values = this.node.getElementsByTagName("item");
		 var result = [];
		 for (var v in values)
		 {
		   if (values[v].getElementsByTagName)
		   {
		     var label = values[v].getElementsByTagName("label")[0];
		     var value = values[v].getElementsByTagName("value")[0];
		     var valid = true;
		     if (binding.constraint)
		     {
		       dojo.debug("testing " + binding.constraint + 
				  " on " + dojo.dom.textContent(value));
		       var d = this.node.ownerDocument;
		       valid = d.evaluate(binding.constraint,
					  value,
					  d.createNSResolver(d),
					  XPathResult.ANY_TYPE,
					  null);
		       dojo.debug("valid " + dojo.dom.textContent(value) + "? " + valid);
		       valid = valid.booleanValue;
		     }
		     if (valid)
		     {
		       result.push({ 
			 id: value.getAttribute("id"), 
			 label: dojo.dom.textContent(label),
			 value: dojo.dom.textContent(value)
		       });
		     }
		   }
		 }
		 return result;
	       },
	     render: function(attach_point)
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
		   dojo.event.connect(radio, "onclick", this, this._radio_clickHandler);
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
		 dojo.event.connect(combobox, "onchange", this, this._combobox_changeHandler);
	       }
	     },
             _combobox_changeHandler: function(event) 
	     { 
	       this.xform.setXFormsValue(event.target.getAttribute("id"),
					 event.target.options[event.target.selectedIndex].value);
	     },
	     _radio_clickHandler: function(event)
	     { 
	       this.xform.setXFormsValue(event.target.getAttribute("id"),
					 event.target.value);
	     }
	     });

dojo.declare("alfresco.xforms.CheckBox",
	     alfresco.xforms.Widget,
	     {
	     initializer: function(xform, node) 
	     {
	       this.inherited("initializer", [ xform, node ]);
	     },
	     render: function(attach_point)
	     {
	       var nodeRef = document.createElement("div");
	       attach_point.appendChild(nodeRef);
	       var initial_value = this.getInitialValue() || false;
	       this.widget = dojo.widget.createWidget("CheckBox", 
						      { 
						      widgetId: this.id,
						      checked: initial_value 
						      },
						      nodeRef);

	       dojo.event.connect(this.widget, "onClick", this, this._checkBox_clickHandler);
	     },
             _checkBox_clickHandler: function(event)
	     {
	       this.xform.setXFormsValue(this.widget.widgetId, 
					 this.widget.checked);
	     }
	     });

dojo.declare("alfresco.xforms.Group",
	     alfresco.xforms.Widget,
	     {
	     initializer: function(xform, node) 
	       {
		 this.inherited("initializer", [ xform, node ]);
		 this.children = [];
	       },
	     children: null,
	     getChildAt: function(index)
	     {		 
	       return index < this.children.length ? this.children[index] : null;
	     },
	     getChildIndex: function(child)
	     {
	       for (var i = 0; i < this.children.length; i++)
	       {
		 dojo.debug(this.id + "[" + i + "]: " + 
			    " is " + this.children[i].id + " the same as " + child.id + "?");
		 if (this.children[i] == child)
		   return i;
	       }
	       return -1;
	     },
             domNode: null,
	     addChild: function(child)
	     {
	       return this.insertChildAt(child, this.children.length);
	     },
             insertChildAt: function(child, position)
	     {
	       dojo.debug(this.id + ".insertChildAt(" + child.id + ", " + position + ")");
	       child.parent = this;

	       var d = document.createElement("div");
	       child.domContainer = d;
	       d.setAttribute("style", "position: relative; border: 0px solid green; margin-top: 2px; margin-bottom: 2px;");
	       if (this.parent && this.parent.domNode)
		 d.style.top = this.parent.domNode.style.bottom;

	       if (position == this.children.length)
	       {
		 this.domNode.appendChild(d);
		 this.children.push(child);
	       }
	       else
	       {
		 this.domNode.insertBefore(d, this.getChildAt(position).domContainer);
		 this.children.splice(position, 0, child);
	       }

	       if (child.isRequired() && !(child instanceof alfresco.xforms.Group))
	       {
		 var requiredImage = document.createElement("img");
		 requiredImage.setAttribute("src", WEBAPP_CONTEXT + "/images/icons/required_field.gif");
		 requiredImage.setAttribute("style", "margin:5px;");
		 d.appendChild(requiredImage);
		 requiredImage.style.position = "relative";
		 requiredImage.style.top = "0px";
		 requiredImage.style.left = "0px";
		 requiredImage.style.lineHeight = d.style.height;
	       }
	       var label = child._getLabelNode();
	       if (label && !(child instanceof alfresco.xforms.Group))
	       {
		 var labelDiv = document.createElement("div");
		 labelDiv.appendChild(document.createTextNode(dojo.dom.textContent(label)));
		 d.appendChild(labelDiv);
		 labelDiv.style.position = "relative";
		 labelDiv.style.top = "-" + labelDiv.offsetTop + "px";
		 labelDiv.style.left = "5%";
	       }
	       var contentDiv = document.createElement("div");
	       d.appendChild(contentDiv);
	       child.render(contentDiv);
	       contentDiv.style.position = "relative";
	       contentDiv.style.top = "-" + contentDiv.offsetTop + "px";
	       contentDiv.style.left = (child instanceof alfresco.xforms.Group 
					? "0px" 
					: "40%");
	       d.style.borderColor = "pink";
	       d.style.borderWidth = "0px";
	       if (!(child instanceof alfresco.xforms.Group))
		 d.style.height = contentDiv.offsetHeight;
	       return d;
	     },
	     removeChildAt: function(position)
	     {
	       var child = this.getChildAt(position);
	       if (!child)
		 throw new Error("unabled to find child at " + position);
	       this.children.splice(position, 1);
	       dojo.dom.removeChildren(child.domContainer);
	       this.domNode.removeChild(child.domContainer);
	     },
	     isIndented: function()
             {
		 return false && this.parent != null;
             },
	     render: function(attach_point)
             {
		 this.domNode = document.createElement("div");
		 this.domNode.setAttribute("id", this.id + "-domNode");
		 this.domNode.widget = this;
		 this.domNode.setAttribute("style", "border: 0px solid blue;");
		 if (this.isIndented())
		   this.domNode.style.marginLeft = "10px";
		 attach_point.appendChild(this.domNode);
		 return this.domNode;
             }
	     });

dojo.declare("alfresco.xforms.Repeat",
	     alfresco.xforms.Group,
	     {
	     initializer: function(xform, node) 
             {
	       this.inherited("initializer", [ xform, node ]);
	     },
             selectedIndex: null,
             insertChildAt: function(child, position)
	     {
	       var result = this.inherited("insertChildAt", [ child, position ]);
	       result.style.borderColor = "green";
	       result.style.borderWidth = "0px";
	       child.repeat = this;

	       dojo.event.browser.addListener(result, "onclick", function(event)
					      {
						child.repeat.setFocusedChild(child);
					      });
	       
	       var controls = document.createElement("div");
	       result.appendChild(controls);
	       controls.style.position = "absolute";
	       controls.style.left = "80%";
	       controls.style.bottom = "0px";

	       var images = [ 
		 { src: "plus", action: this._insertRepeatItem_handler },
		 { src: "arrow_up", action: this._moveRepeatItemUp_handler },
		 { src: "arrow_down", action: this._moveRepeatItemDown_handler }, 
		 { src: "minus", action: this._removeRepeatItem_handler }
	       ];
	       for (var i in images)
	       {
		 var img = document.createElement("img");
		 img.setAttribute("src", 
				  WEBAPP_CONTEXT + "/images/icons/" + images[i].src + ".gif");
		 img.style.width = "16px";
		 img.style.height = "16px";
		 img.style.marginRight = "4px";
		 img.repeatItem = child;
		 img.repeat = this;
		 controls.appendChild(img);
		 dojo.event.connect(img, "onclick", this, images[i].action);
	       }

	       return result;
	     },
	     removeChildAt: function(position)
	     {
	       this.inherited("removeChildAt", [ position ]);
	       if (this.selectedIndex == position)
		 this.handleIndexChanged(position);
	     },
	     _insertRepeatItem_handler: function(event)
	     {
	       var trigger = _findElementById(this.node.parentNode, 
					      this.id + "-insert_after");
	       this.xform.fireAction(trigger.getAttribute("id"));
	     },
	     _removeRepeatItem_handler: function(event)
	     {
	       var trigger = _findElementById(this.node.parentNode, 
					      this.id + "-delete");
	       this.xform.fireAction(trigger.getAttribute("id"));
	     },
	     _moveRepeatItemUp_handler: function(event)
	     {
	       alert("moveUp " + event);
	     },
	     _moveRepeatItemDown_handler: function(event)
	     {
	       alert("moveDown " + event);
	     },
	     setFocusedChild: function(child)
	     {
	       if (!child)
		 this.xform.setRepeatIndex(this.id, 0);
	       else
	       {
		 var index = this.getChildIndex(child);
		 if (index < 0)
		   throw new Error("unable to find child " + child.id + " in " + this.id);

		 // chiba thinks indexes are initialized to 1 so just
		 // highlight the thing
		 if (this.selectedIndex == null && index == 0)
		   this.handleIndexChanged(0);
		 // xforms repeat indexes are 1-based
		 this.xform.setRepeatIndex(this.id, index + 1);
	       }
	     },
	     isIndented: function()
	     {
	       return false;
	     },
	     render: function(attach_point)
	     {
	       this.domNode = this.inherited("render", [ attach_point ]);
	       this.domNode.style.borderColor = "black";
	       this.domNode.style.borderWidth = "1px";
	       
	       var d = document.createElement("div");
	       d.repeat = this;
	       this.domNode.appendChild(d);
	       d.setAttribute("style", "position: relative; line-height: 16px; background-color: #cddbe8; font-weight: bold;");
	       dojo.event.browser.addListener(d, 
					      "onclick", 
					      function(event)
					      {
						event.currentTarget.repeat.setFocusedChild(null);
					      });
	       
	       var labelElement = document.createElement("div");
	       d.appendChild(labelElement);
	       labelElement.appendChild(document.createTextNode(this.parent.getLabel()));
	       labelElement.setAttribute("style", "position: relative; left: 5%; top: 0px;");
	       
	       var addElement = document.createElement("img");
	       d.appendChild(addElement);
	       addElement.setAttribute("src", WEBAPP_CONTEXT + "/images/icons/plus.gif");
	       addElement.style.width = "16px";
	       addElement.style.height = "16px";
	       addElement.style.position = "absolute";
	       addElement.style.top = "0px";
	       addElement.style.left = "80%";
	       
	       dojo.event.browser.addListener(addElement, 
					      "onclick", 
					      function(event)
					      {
						var repeat = event.currentTarget.parentNode.repeat;
						var trigger = _findElementById(repeat.node.parentNode, 
									       repeat.id + "-insert_before");
						repeat.xform.fireAction(trigger.getAttribute("id"));
					      });
	       
	       return this.domNode;
	     },
	     handleIndexChanged: function(index)
	     {
	       dojo.debug(this.id + ".handleIndexChanged(" + index + ")");
	       if (this.selectedIndex != null && this.selectedIndex >= 0)
	       {
		 var child = this.getChildAt(this.selectedIndex);
		 child.domContainer.style.backgroundColor = "white";
	       }
	       
	       if (index >= 0)
	       {
		 var child = this.getChildAt(index);
		 child.domContainer.style.backgroundColor = "orange";
	       }
	       this.selectedIndex = index;
	     },
	     handlePrototypeCloned: function(prototypeId)
	       {
		 dojo.debug(this.id + ".handlePrototypeCloned("+ prototypeId +")");
		 var chibaData = this.node.getElementsByTagName("data");
		 dojo.debug("repeat node == " +dojo.dom.innerXML(this.node));
		 dojo.debug(chibaData + " l = " + chibaData.length);
		 chibaData = chibaData[chibaData.length - 1];
		 dojo.debug("chiba:data == " + dojo.dom.innerXML(chibaData));
		 var prototypeToClone = dojo.dom.firstElement(chibaData);
		 if (prototypeToClone.getAttribute("id") != prototypeId)
		   throw new Error("unable to locate " + prototypeId +
				   " in " + this.id);
		 return prototypeToClone.cloneNode(true);
	       },
	     handleItemInserted: function(clonedPrototype, position)
	       {
		 dojo.debug(this.id + ".handleItemInserted(" + clonedPrototype.nodeName +
			    ", " + position + ")");
		 var w = create_widget(this.xform, clonedPrototype);
		 this.insertChildAt(w, position);
		 load_body(this.xform, w.node, w);
	       },
	     handleItemDeleted: function(position)
	       {
		 dojo.debug(this.id + ".handleItemDeleted(" + position + ")");
		 this.removeChildAt(position);
	       }
	     });

dojo.declare("alfresco.xforms.Trigger",
	     alfresco.xforms.Widget,
	     {
	     initializer: function(xform, node) 
	       {
		 this.inherited("initializer", [ xform, node ]);
	       },
	     
	     render: function(attach_point)
	       {
		 var nodeRef = document.createElement("div");
		 attach_point.appendChild(nodeRef);
		 var w = dojo.widget.createWidget("Button", 
						  {
						  widgetId: this.id,
						  caption: this.getLabel() + " " + this.id
						  }, 
						  nodeRef);
		 w.onClick = function()
		   {
		     fireAction(w.widgetId);
		   };
		 this.domContainer.style.display = "none";
	       }
	     });

dojo.declare("alfresco.xforms.Submit",
	     alfresco.xforms.Trigger,
	     {
	     initializer: function(xform, node) 
	       {
		 this.inherited("initializer", [ xform, node ]);
	       },
	     render: function(attach_point)
	       {
		 var nodeRef = document.createElement("div");
		 attach_point.appendChild(nodeRef);
		 var w = dojo.widget.createWidget("Button", 
						  {
						  widgetId: this.id,
						  caption: "submit" 
						  }, 
						  nodeRef);
		 w.widget = this;
		 this.widget = w;
		 document.submitWidget = this;
		 w.onClick = function()
		   {
		     document.submitWidget.done = false;
		     w.widget.xform.fireAction(w.widgetId);
		   };
		 this.domContainer.style.display = "none";
	       }
	     });

dojo.declare("alfresco.xforms.XFormsEvent",
	     null,
	     {
	     initializer: function(node)
             {
	       this.type = node.nodeName;
	       this.targetId = node.getAttribute("targetId");
	       this.targetName = node.getAttribute("targetName");
	       this.properties = {};
	       for (var i = 0; i < node.childNodes.length; i++)
	       {
		 if (dojo.dom.isNode(node.childNodes[i]))
		   this.properties[node.childNodes[i].getAttribute("name")] =
		     node.childNodes[i].getAttribute("value");
	       }
	     },
	     getTarget: function()
	     {
	       var targetDomNode = document.getElementById(this.targetId + "-domNode");
	       if (!targetDomNode)
		 throw new Error("unable to find node " + this.targetId + "-domNode");
	       return targetDomNode.widget;
	     }
	     });

dojo.declare("alfresco.xforms.XForm",
	     null,
	     {
	     initializer: function(document)
	       {
		 this.document = document;
		 this.node = document.documentElement;
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
		       constraint: bind.childNodes[i].getAttribute("xforms:constraint"),
		       parent: parent
		     };
		     this._loadBindings(bind.childNodes[i], result[id], result);
		   }
		 }
		 return result;
	       },
	     setRepeatIndex: function(id, index)
	       {
		 dojo.debug("setting repeat index " + index + " on " + id);
		 var req = {
		 xform: this,
		 url: WEBAPP_CONTEXT + "/ajax/invoke/XFormsBean.setRepeatIndex",
		 content: { id: id, index: index },
		 mimetype: "text/xml",
		 load: function(type, data, evt)
		 {
		   this.xform._handleEventLog(data.documentElement);
		 },
		 error: function(type, e)
		 {
		   alert("error!! " + type + " e = " + e.message);
		 }
		 };
		 dojo.io.bind(req);
	       },
             fireAction: function(id)
	       {
		 var req = {
		 xform: this,
		 url: WEBAPP_CONTEXT + "/ajax/invoke/XFormsBean.fireAction",
		 content: { id: id },
		 mimetype: "text/xml",
		 load: function(type, data, evt)
		 {
		   dojo.debug("fireAction." + type);
		   this.xform._handleEventLog(data.documentElement);
		 },
		 error: function(type, e)
		 {
		   alert("error!! " + type + " e = " + e.message);
		 }
		 };
		 dojo.io.bind(req);
	       },
	     setXFormsValue: function(id, value)
	       {
		 dojo.debug("setting value " + id + " = " + value);
		 var req = {
		 xform: this,
		 url: WEBAPP_CONTEXT + "/ajax/invoke/XFormsBean.setXFormsValue",
		 content: { id: id, value: value },
		 mimetype: "text/xml",
		 load: function(type, data, evt)
		 {
		   this.xform._handleEventLog(data.documentElement);
		 },
		 error: function(type, e)
		 {
		   alert("error!! " + type + " e = " + e.message);
		 }
		 };
		 dojo.io.bind(req);
	       },
	     _handleEventLog: function(events)
	       {
		 var prototypeClones = [];
		 for (var i = 0; i < events.childNodes.length; i++)
		 {
		   if (dojo.dom.isNode(events.childNodes[i]))
		   {
		     var xfe = new alfresco.xforms.XFormsEvent(events.childNodes[i]);
		     dojo.debug("parsing " + xfe.type +
				"(" + xfe.targetId + ", " + xfe.targetName + ")");
		     switch (xfe.type)
		     {
		     case "chiba-index-changed":
		     {
		       var index = Number(xfe.properties["index"]) - 1;
		       xfe.getTarget().handleIndexChanged(index);
		       break;
		     }
		     case "chiba-prototype-cloned":
		     {
		       var prototypeId = xfe.properties["prototypeId"];
		       var clone = xfe.getTarget().handlePrototypeCloned(prototypeId);
		       prototypeClones.push(clone);
		       break;
		     }
		     case "chiba-id-generated":
		     {
		       var originalId = xfe.properties["originalId"];

		       dojo.debug("handleIdGenerated(" + xfe.targetId + ", " + originalId + ")");
		       var clone = prototypeClones[prototypeClones.length - 1];
		       var node = _findElementById(clone, originalId);
		       if (node)
		       {
			 dojo.debug("applying id " + xfe.targetId + 
				    " to " + node.nodeName + "(" + originalId + ")");
			 node.setAttribute("id", xfe.targetId);
		       }
		       else
			 throw new Error("unable to find " + originalId + 
					 " in clone " + dojo.dom.innerXML(clone));
		       break;
		     }
		     case "chiba-item-inserted":
		     {
		       var position = Number(xfe.properties["position"]) - 1;
		     
		       var clone = prototypeClones.pop();
		       xfe.getTarget().handleItemInserted(clone, position);
		       break;
		     }
		     case "chiba-item-deleted":
		     {
		       var position = Number(xfe.properties["position"]) - 1;
		       xfe.getTarget().handleItemDeleted(position);
		       break;
		     }
		     case "chiba-replace-all":
		       if (document.submitWidget)
		       {
			 document.submitWidget.done = true;
			 document.submitWidget.currentButton.click();
			 document.submitWidget.currentButton = null;
		       }
		       break;
		     case "xforms-submit-error":
		       alert("you gotta fill out the form first!");
		       break;
		     default:
		     {
		       dojo.debug("unhandled event " + events.childNodes[i].nodeName);
		     }
		     }
		   }
		 }
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
    var xform = new alfresco.xforms.XForm(data);
    var bindings = xform.getBindings();
    for (var i in bindings)
    {
      dojo.debug("bindings[" + i + "]=" + bindings[i].id + 
		 ", parent = " + (bindings[i].parent 
				  ? bindings[i].parent.id
				  : 'null'));
    }
    var alfUI = document.getElementById("alf-ui");
    var root = new alfresco.xforms.Group(xform, document.getElementById("alf-ui"));
    root.render(alfUI);
    load_body(xform, xform.getBody(), root);
    document.xform = xform;
  },
  error: function(type, e)
  {
    alert("error!! " + type + " e = " + e.message);
  }
  };
  dojo.io.bind(req);
}

function create_widget(xform, node)
{
  switch (node.nodeName.toLowerCase())
    {
    case "xforms:group":
      return new alfresco.xforms.Group(xform, node);
    case "xforms:repeat":
      return new alfresco.xforms.Repeat(xform, node);
    case "xforms:textarea":
      return new alfresco.xforms.TextArea(xform, node);
    case "xforms:input":
      var type = xform.getType(node);
      switch (type)
      {
      case "date":
	return new alfresco.xforms.DatePicker(xform, node);
      case "integer":
      case "positiveInteger":
      case "negativeInteger":
      case "double":
	return new alfresco.xforms.NumericStepper(xform, node, type);
      case "string":
      default:
	return new alfresco.xforms.TextField(xform, node);
      }
    case "xforms:select1":
      return (xform.getType(node) == "boolean"
	      ? new alfresco.xforms.CheckBox(xform, node)
	      : new alfresco.xforms.Select1(xform, node));
    case "xforms:submit":
      return  new alfresco.xforms.Submit(xform, node);
    case "xforms:trigger":
      return new alfresco.xforms.Trigger(xform, node);
    case "chiba:data":
    case "xforms:label":
    case "xforms:alert":
      return null;
    default:
      throw new Error("unknown type " + node.nodeName);
    }
}

function load_body(xform, currentNode, parentWidget)
{
  dojo.lang.forEach(currentNode.childNodes, function(o)
  {
    if (dojo.dom.isNode(o))
    {
      dojo.debug("loading " + o + " NN " + o.nodeName + " into " + parentWidget);
      var w = create_widget(xform, o);
      if (w != null)
      {
	parentWidget.addChild(w);
	if (w instanceof alfresco.xforms.Group)
	  load_body(xform, o, w);
      }
    }
  });
}

function addSubmitHandlerToButton(b)
{
  var baseOnClick = b.onclick;
  b.onclick = function(event)
  {
//    alert("submitting xform from " + b.getAttribute("id") + 
//	  " b " + b +
//	  " this " + this );
    if (!document.submitWidget.done)
    {
      //      alert("not done, resubmitting");
      tinyMCE.triggerSave();
      document.submitWidget.currentButton = this;
      document.submitWidget.widget.buttonClick(); 
      return false;
    }
    else
    {
      //     alert("done - doing base click");
      return baseOnClick(event);
    }
  }
}

function _findElementById(node, id)
{
  dojo.debug("looking for " + id + 
	     " in " + node.nodeName + 
	     "(" + node.getAttribute("id") + ")");
  if (node.getAttribute("id") == id)
    return node;
  for (var i = 0; i < node.childNodes.length; i++)
  {
    if (dojo.dom.isNode(node.childNodes[i]))
    {
      var n = _findElementById(node.childNodes[i], id);
      if (n)
	return n;
    }
  }
  return null;
}
