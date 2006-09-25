dojo.require("dojo.widget.DebugConsole");
dojo.require("dojo.widget.DatePicker");
dojo.require("dojo.widget.Button");
dojo.require("dojo.widget.validate");
dojo.require("dojo.widget.ComboBox");
dojo.require("dojo.widget.Checkbox");
dojo.require("dojo.widget.Editor");
dojo.require("dojo.widget.Spinner");
dojo.require("dojo.lfx.html");
dojo.hostenv.writeIncludes();
dojo.addOnLoad(function()
	       {
		 document.xform = new alfresco.xforms.XForm();
	       });
tinyMCE.init({
      theme: "advanced",
      mode: "exact",
      encoding: null,
      save_callback: "document.xform.setXFormsValue",
      add_unload_trigger: false,
      add_form_submit_trigger: false,
      theme_advanced_toolbar_location: "top",
      theme_advanced_toolbar_align: "left",
      theme_advanced_buttons1: "fontselect,fontsizeselect",
      theme_advanced_buttons2: "separator,forecolor,backcolor"
});

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
	       var nsResolver = d.createNSResolver(d.documentElement);
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
						 ? "AdjustableRealNumberTextBox"
						 : "AdjustableIntegerTextBox"), 
						{ 
						widgetId: this.id + "-widget",
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
	     },
	     render: function(attach_point)
	     {
	       var initial_value = this.getInitialValue();

	       var dateTextBoxDiv = document.createElement("div");
	       attach_point.appendChild(dateTextBoxDiv);
	       this.dateTextBox = dojo.widget.createWidget("DateTextBox", 
							   {
							   widgetId: this.id + "-widget",
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

	       var dp_initial_value = (initial_value
				       ? initial_value
				       : dojo.widget.DatePicker.util.toRfcDate(new Date()));
	       this.dateTextBox.picker = dojo.widget.createWidget("DatePicker", 
								  { 
								  isHidden: true, 
								  storedDate: dp_initial_value
								  }, 
								  datePickerDiv);
	       this.dateTextBox.picker.hide();
	       dojo.event.connect(this.dateTextBox.picker,
				  "onSetDate", 
				  this,
				  this._datePicker_setDateHandler);
	     },
	     getValue: function()
	     {
	       return this.dateTextBox.getValue();
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
	       this.xform.setXFormsValue(this.id, this.getValue());
	     }
	     });

dojo.declare("alfresco.xforms.TextField",
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
	       var initial_value = this.getInitialValue() || "";

	       var w = dojo.widget.createWidget("ValidationTextBox", 
						{
						widgetId: this.id + "-widget",
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
	     },
	     render: function(attach_point)
	     {
	       this.domNode = document.createElement("div");
	       attach_point.appendChild(this.domNode);
	       this.domNode.setAttribute("style", "height: 200px; border: solid 1px black;");
	       this.domNode.setAttribute("id", this.id);
	       this.domNode.innerHTML = this.getInitialValue() || "";
	       dojo.event.connect(this.domNode, "onclick", this, this._clickHandler);
	       dojo.event.connect(this.domNode, "onblur", this, this._blurHandler);
	     },
             _clickHandler: function(event)
	     {
	       tinyMCE.addMCEControl(this.domNode, this.id);
	     },
	     _blurHandler: function(event)
	     {
	       alert('blurry');
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
					d.createNSResolver(d.documentElement),
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
		   radio.setAttribute("id", this.id + "-widget");
		   radio.setAttribute("name", this.id + "-widget");
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
		 combobox.setAttribute("id", this.id + "-widget");
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
	       this.xform.setXFormsValue(this.id,
					 event.target.options[event.target.selectedIndex].value);
	     },
	     _radio_clickHandler: function(event)
	     { 
	       this.xform.setXFormsValue(this.id,
					 event.target.value);
	     }
	     });

dojo.declare("alfresco.xforms.Checkbox",
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
	       var initial_value = this.getInitialValue() == "true";
	       this.widget = dojo.widget.createWidget("Checkbox", 
						      { 
						      widgetId: this.id + "-widget",
						      checked: initial_value 
						      },
						      nodeRef);

	       dojo.event.connect(this.widget, 
				  "onMouseUp", 
				  this, 
				  this._checkBox_mouseUpHandler);
	     },
             _checkBox_mouseUpHandler: function(event)
	     {
	       this.xform.setXFormsValue(this.id, 
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

	       child.domContainer = document.createElement("div");
	       child.domContainer.setAttribute("style", "position: relative; border: 0px solid green; margin-top: 2px; margin-bottom: 2px;");
	       child.domContainer.style.width = "100%";
	       if (this.parent && this.parent.domNode)
		 child.domContainer.style.top = this.parent.domNode.style.bottom;

	       if (position == this.children.length)
	       {
		 this.domNode.appendChild(child.domContainer);
		 this.children.push(child);
	       }
	       else
	       {
		 this.domNode.insertBefore(child.domContainer, 
					   this.getChildAt(position).domContainer);
		 this.children.splice(position, 0, child);
	       }

	       if (!(child instanceof alfresco.xforms.Group))
	       {
		 var requiredImage = document.createElement("img");
		 requiredImage.setAttribute("src", WEBAPP_CONTEXT + "/images/icons/required_field.gif");
		 requiredImage.setAttribute("style", "margin: 0px 5px 0px 5px;");
		 child.domContainer.appendChild(requiredImage);
		 
		 if (!child.isRequired())
		   requiredImage.style.visibility = "hidden";
		 var label = child._getLabelNode();
		 if (label)
		 {
		   var labelNode = document.createTextNode(dojo.dom.textContent(label));
		   child.domContainer.appendChild(labelNode);
		 }
	       }
	       var contentDiv = document.createElement("div");
	       contentDiv.setAttribute("id", child.id + "-content");
	       child.domContainer.appendChild(contentDiv);
	       contentDiv.style.position = "relative";
//	       contentDiv.style.width = (d.offsetWidth - contentDiv.offsetLeft) + "px";
	       child.render(contentDiv);
	       if (!(child instanceof alfresco.xforms.Group))
	       {
		 contentDiv.style.width = (child.domContainer.offsetWidth * .7) + "px";
		 child.domContainer.style.height = contentDiv.offsetHeight + "px";
		 child.domContainer.style.lineHeight = child.domContainer.style.height;
	       }

//	       contentDiv.appendChild(document.createTextNode("ot " + contentDiv.offsetTop + 
//							      "st " + contentDiv.style.top));

	       contentDiv.style.top = "-" + contentDiv.offsetTop + "px";
	       contentDiv.style.left = (child instanceof alfresco.xforms.Group 
					? "0px" 
					: "30%");


	       child.domContainer.style.borderColor = "pink";
	       child.domContainer.style.borderWidth = "0px";
	       this._updateDisplay();
	       return child.domContainer;
	     },
	     removeChildAt: function(position)
	     {
	       var child = this.getChildAt(position);
	       if (!child)
		 throw new Error("unabled to find child at " + position);
	       this.children.splice(position, 1);
	       child.domContainer.group = this;
	       var anim = dojo.lfx.html.fadeOut(child.domContainer, 500);
	       anim.onEnd = function()
	       {
		 child.domContainer.style.display = "none";
		 dojo.dom.removeChildren(child.domContainer);
		 dojo.dom.removeNode(child.domContainer);
		 child.domContainer.group._updateDisplay();
	       };
	       anim.play();
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
		 attach_point.appendChild(this.domNode);

		 this.domNode.setAttribute("style", "border: 0px solid blue;");
		 this.domNode.style.width = "100%";
		 if (this.isIndented())
		   this.domNode.style.marginLeft = "10px";
		 return this.domNode;
             },
	     _updateDisplay: function()
	     {
	     }
	     });

dojo.declare("alfresco.xforms.Repeat",
	     alfresco.xforms.Group,
	     {
	     initializer: function(xform, node) 
             {
	       this.inherited("initializer", [ xform, node ]);
	     },
             _selectedIndex: -1,
             insertChildAt: function(child, position)
	     {
	       var result = this.inherited("insertChildAt", [ child, position ]);
	       child.repeat = this;

	       dojo.event.connect(result, "onclick", function(event)
				  {
				    child.repeat.setFocusedChild(child);
				  });
	       
	       var controls = document.createElement("div");
	       result.appendChild(controls);
	       controls.style.position = "absolute";
	       controls.style.left = "80%";
	       controls.style.bottom = "0px";

	       var images = [ 
		 { src: "plus", action: this._insertRepeatItemAfter_handler },
		 { src: "arrow_up", action: this._moveRepeatItemUp_handler },
		 { src: "arrow_down", action: this._moveRepeatItemDown_handler }, 
		 { src: "minus", action: this._removeRepeatItem_handler }
	       ];
	       for (var i in images)
	       {
		 var img = document.createElement("img");
		 img.setAttribute("src", (WEBAPP_CONTEXT + "/images/icons/" + 
					  images[i].src + ".gif"));
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
	     getSelectedIndex: function()
	     {
	       this._selectedIndex = Math.min(this.children.length - 1, this._selectedIndex);
	       if (this.children.length == 0)
		 this._selectedIndex = -1;
	       return this._selectedIndex;
	     },
	     _updateDisplay: function()
	     {
	       for (var i = 0; i < this.children.length; i++)
	       {
		 this.children[i].domContainer.style.backgroundColor = 
		   i % 2 ? "#cccc99" : "#ffffff"; 
		 if (i == this.getSelectedIndex())
		   this.children[i].domContainer.style.backgroundColor = "orange";
//		   dojo.lfx.html.highlight(this.children[i].domContainer, 
//					   "orange", 
//					   200,
//					   0,
//					   function(node)
//					   {
//					     node.style.backgroundColor = "orange";
//					   }).play();
	       }
	     },
	     _insertRepeatItemAfter_handler: function(event)
	     {
	       dojo.event.browser.stopEvent(event);
	       this.setFocusedChild(event.target.repeatItem);
	       if (!this.insertRepeatItemAfterTrigger)
		 this.insertRepeatItemAfterTrigger = 
		   _findElementById(this.node.parentNode, this.id + "-insert_after");
	       this.xform.fireAction(this.insertRepeatItemAfterTrigger.getAttribute("id"));
	     },
	     _insertRepeatItemBefore_handler: function(event)
	     {
	       dojo.event.browser.stopEvent(event);
	       this.setFocusedChild(event.target.repeatItem);
	       if (!this.insertRepeatItemBeforeTrigger)
		 this.insertRepeatItemBeforeTrigger = 
		   _findElementById(this.node.parentNode, this.id + "-insert_before");
	       this.xform.fireAction(this.insertRepeatItemBeforeTrigger.getAttribute("id"));
	     },
	     _removeRepeatItem_handler: function(event)
	     {
	       dojo.event.browser.stopEvent(event);
	       this.setFocusedChild(event.target.repeatItem);
	       if (!this.removeRepeatItemTrigger)
		 this.removeRepeatItemTrigger = _findElementById(this.node.parentNode, 
								 this.id + "-delete");
	       this.xform.fireAction(this.removeRepeatItemTrigger.getAttribute("id"));
	     },
	     _moveRepeatItemUp_handler: function(event)
	     {
	       dojo.event.browser.stopEvent(event);
	       var r = event.target.repeat;
	       var index = r.getChildIndex(event.target.repeatItem);
	       if (index == 0 || r.children.length == 1)
		 return;
	       event.target.repeat.swapChildren(index, index - 1);
	     },
	     _moveRepeatItemDown_handler: function(event)
	     {
	       dojo.event.browser.stopEvent(event);
	       var r = event.target.repeat;
	       var index = r.getChildIndex(event.target.repeatItem);
	       if (index == r.children.length - 1 || r.children.length == 1)
		 return;
	       event.target.repeat.swapChildren(index, index + 1);
	     },
	     swapChildren: function(fromIndex, toIndex)
	     {
	       dojo.debug(this.id + ".swapChildren(" + fromIndex + 
			  ", " + toIndex + ")");
	       var fromChild = this.getChildAt(fromIndex);
	       var toChild = this.getChildAt(toIndex);
//	       var toChildCoords = dojo.style.getAbsolutePosition(toChild.domContainer);
//	       toChildCoords = [ toChildCoords.x, toChildCoords.y ];
//	       alert("to coords [ " + toChildCoords[0] + ", " + toChildCoords[0] + "]");
//	       var fromChildCoords = dojo.style.getAbsolutePosition(fromChild.domContainer);
//	       fromChildCoords = [ fromChildCoords.x, fromChildCoords.y ];
//	       alert("from coords [ " + fromChildCoords[0] + ", " + fromChildCoords[0] + "]");
//	       dojo.lfx.html.slideTo(fromChild.domContainer, 5000, toChildCoords);
//	       dojo.lfx.html.slideTo(toChild.domContainer, 5000, fromChildCoords);


	       var swapNode = document.createElement("div");
//	       dojo.dom.removeNode(toChild.domContainer);
//	       dojo.dom.removeNode(fromChild.domContainer);
	       this.domNode.replaceChild(swapNode, fromChild.domContainer);
	       this.domNode.replaceChild(fromChild.domContainer, toChild.domContainer);
	       this.domNode.replaceChild(toChild.domContainer, swapNode);

	       this.children[fromIndex] = toChild;
	       this.children[toIndex] = fromChild;
	       this._selectedIndex = toIndex;
	       this._updateDisplay();
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
		 if (this.getSelectedIndex() == -1 && index == 0)
		   this.handleIndexChanged(0);
		 else
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
	       d.setAttribute("style", "position: relative; height: 20px; line-height: 20px; background-color: #cddbe8; font-weight: bold;");
	       d.style.width = "100%";
	       dojo.event.connect(d, "onclick", function(event)
				  {
				    if (event.target == event.currentTarget)
				      event.currentTarget.repeat.setFocusedChild(null);
				  });

	       //used only for positioning the label accurately
	       var requiredImage = document.createElement("img");
	       requiredImage.setAttribute("src", WEBAPP_CONTEXT + "/images/icons/required_field.gif");
	       requiredImage.setAttribute("style", "margin: 0px 5px 0px 5px;");
	       d.appendChild(requiredImage);
//	       requiredImage.style.position = "relative";
//	       requiredImage.style.top = "0px";
//	       requiredImage.style.left = "0px";
	       requiredImage.style.visibility = "hidden";

	       var labelElement = document.createTextNode(this.parent.getLabel());//document.createElement("span");
	       d.appendChild(labelElement);
	       
	       var addElement = document.createElement("img");
	       d.appendChild(addElement);
	       addElement.setAttribute("src", WEBAPP_CONTEXT + "/images/icons/plus.gif");
	       addElement.style.width = "16px";
	       addElement.style.height = "16px";
	       addElement.style.position = "absolute";
	       addElement.style.top = "0px";
	       addElement.style.left = "80%";
	       
	       dojo.event.connect(addElement, "onclick", this, this._insertRepeatItemBefore_handler);
	       
	       return this.domNode;
	     },
	     handleIndexChanged: function(index)
	     {
	       dojo.debug(this.id + ".handleIndexChanged(" + index + ")");
	       this._selectedIndex = index;
	       this._updateDisplay();
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
	       this.widget = dojo.widget.createWidget("Button", 
						      {
						      widgetId: this.id + "-widget",
						      caption: this.getLabel() + " " + this.id
						      }, 
						      nodeRef);
	       dojo.event.connect(this.widget, "onClick", this, this._clickHandler);
	       this.domContainer.style.display = "none";
	     },
	     _clickHandler: function(event)
	     {
	       this.xform.fireAction(this.id);
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
	       this.inherited("render", [ attach_point ]);
	       document.submitWidget = this;
	     },
	     _clickHandler: function(event)
	     {
	       document.submitWidget.done = false;
	       this.xform.fireAction(this.id);
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
		 if (node.childNodes[i].nodeType == dojo.dom.ELEMENT_NODE)
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
	     initializer: function()
             {
	       send_ajax_request(create_ajax_request(this,
						     "getXForm",
						     {},
						     function(type, data, evt) 
						     {
						       this.xform._loadHandler(data);
						     }));
	     },
	     _loadHandler: function(xformDocument)
	     {
	       this.xformDocument = xformDocument;
	       this.node = xformDocument.documentElement;
	       this._bindings = this._loadBindings(this.getModel());

	       var bindings = this.getBindings();
	       for (var i in bindings)
	       {
		 dojo.debug("bindings[" + i + "]=" + bindings[i].id + 
			    ", parent = " + (bindings[i].parent 
					     ? bindings[i].parent.id
					     : 'null'));
	       }
	       var alfUI = document.getElementById("alf-ui");
	       alfUI.style.width = "100%";
	       
	       var root = new alfresco.xforms.Group(this, alfUI);
	       root.render(alfUI);
	       load_body(this, this.getBody(), root);
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
	       var req = create_ajax_request(this,
					     "setRepeatIndex",
					     { id: id, index: index },
					     function(type, data, evt)
					     {
					       this.xform._handleEventLog(data.documentElement);
					     });
	       send_ajax_request(req);
	     },
             fireAction: function(id)
	     {
	       var req = create_ajax_request(this,
					     "fireAction",
					     { id: id },
					     function(type, data, evt)
					     {
					       dojo.debug("fireAction." + type);
					       this.xform._handleEventLog(data.documentElement);
					     });
	       send_ajax_request(req);
	     },
	     setXFormsValue: function(id, value)
             {
	       dojo.debug("setting value " + id + " = " + value);
	       var req = create_ajax_request(this,
					     "setXFormsValue",
					     { id: id, value: value },
					     function(type, data, evt)
					     {
					       this.xform._handleEventLog(data.documentElement);
					     });
	       send_ajax_request(req);
	     },
	     _handleEventLog: function(events)
             {
	       var prototypeClones = [];
	       for (var i = 0; i < events.childNodes.length; i++)
	       {
		 if (events.childNodes[i].nodeType == dojo.dom.ELEMENT_NODE)
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
		     _show_error("Please provide values for all required fields.");
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
    case "byte":
    case "double":
    case "float":
    case "int":
    case "integer":
    case "long":
    case "negativeInteger":
    case "nonNegativeInteger":
    case "nonPositiveInteger":
    case "short":
    case "unsignedByte":
    case "unsignedInt":
    case "unsignedLong":
    case "unsignedShort":
    case "positiveInteger":
      return new alfresco.xforms.NumericStepper(xform, node, type);
      case "string":
    default:
      return new alfresco.xforms.TextField(xform, node);
    }
  case "xforms:select1":
    return (xform.getType(node) == "boolean"
	    ? new alfresco.xforms.Checkbox(xform, node)
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
    if (o.nodeType == dojo.dom.ELEMENT_NODE)
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
    if (!document.submitWidget.done)
    {
      dojo.debug("not done, resubmitting");
      tinyMCE.triggerSave();
      document.submitWidget.currentButton = this;
      document.submitWidget.widget.buttonClick(); 
      return false;
    }
    else
    {
      dojo.debug("done - doing base click");
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
    if (node.childNodes[i].nodeType == dojo.dom.ELEMENT_NODE)
    {
      var n = _findElementById(node.childNodes[i], id);
      if (n)
	return n;
    }
  }
  return null;
}

function create_ajax_request(xform, serverMethod, methodArgs, load, error)
{
  var result = {};
  result.xform = xform;
  result.url = WEBAPP_CONTEXT + "/ajax/invoke/XFormsBean." + serverMethod;
  result.content = methodArgs;
  result.load = load;
  dojo.event.connect(result, "load", function(type, data, evt)
		     {
//		       _hide_errors();
		       ajax_request_load_handler(this);
		     });
  result.mimetype = "text/xml";
  result.error = error || function(type, e)
  {
    dojo.debug("error [" + type + "] " + e.message);
    _show_error(e.message);
    ajax_request_load_handler(this);
  };
  return result;
}

function _hide_errors()
{
  var errorDiv = document.getElementById("alf-xforms-error");
  if (errorDiv)
    errorDiv.style.display = "none";
}

function _show_error(msg)
{
    var errorDiv = document.getElementById("alf-xforms-error");
    if (!errorDiv)
    {
      errorDiv = document.createElement("div");
      errorDiv.setAttribute("id", "alf-xforms-error");
      errorDiv.setAttribute("class", "infoText statusErrorText");
      errorDiv.setAttribute("style", "padding: 2px; border: 1px solid #003366");
      var alfUI = document.getElementById("alf-ui");
      dojo.dom.prependChild(errorDiv, alfUI);
    }
    if (errorDiv.style.display == "block")
      errorDiv.innerHTML = errorDiv.innerHTML + "<br/>" + e.message;
    else
    {
      errorDiv.innerHTML = msg;
      errorDiv.style.display = "block";
    }
}

function send_ajax_request(req)
{
  ajax_request_send_handler(req);
  dojo.io.queueBind(req);
}

function _get_ajax_loader_element()
{
  var result = document.getElementById("alf-ajax-loader");
  if (result)
    return result;
  result = document.createElement("div");
  result.setAttribute("id", "alf-ajax-loader");
  result.setAttribute("style", "position: absolute; background-color: red; color: white; top: 0px; right: 0px;");
  dojo.style.hide(result);
  document.body.appendChild(result);
  return result;
}

var _ajax_requests = [];

function ajax_request_send_handler(req)
{
  _ajax_requests.push(req);
  ajax_loader_update_display();
}

function ajax_loader_update_display()
{
  var ajaxLoader = _get_ajax_loader_element();
  ajaxLoader.innerHTML = (_ajax_requests.length == 0
			  ? "Idle"
			  : "Loading" + (_ajax_requests.length > 1
					 ? " (" + _ajax_requests.length + ")"
					 : "..."));
  dojo.debug(ajaxLoader.innerHTML);
  if (/*dojo.style.isVisible(ajaxLoader) && */ _ajax_requests.length == 0)
  {
//    dojo.fx.html.fadeOut(ajaxLoader,
//			 200,
//			 function(node)
//			 {
			   dojo.style.hide(ajaxLoader);
//			 });
  }
  else if (/*!dojo.style.isVisible(ajaxLoader) && */ _ajax_requests.length != 0)
  {
//    dojo.fx.html.fadeIn(ajaxLoader,
//			100,
//			function(node)
//			{
			  dojo.style.show(ajaxLoader);
//			});
  }
  else
  {
    alert("v " + dojo.style.isVisible(ajaxLoader) + " l " + _ajax_requests.length);
  }
}

function ajax_request_load_handler(req)
{
  var ajaxLoader = _get_ajax_loader_element();
  var index = -1;
  for (var i = 0; i < _ajax_requests.length; i++)
  {
    if (_ajax_requests[i] == req)
    {
      index = i;
      break;
    }
  }
  if (index == -1)
    _ajax_requests.splice(index, 1);
  else
    throw new Error("unable to find " + req.url);
  ajax_loader_update_display();
}