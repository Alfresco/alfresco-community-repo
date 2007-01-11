dojo.require("dojo.widget.DebugConsole");
dojo.require("dojo.widget.DatePicker");
dojo.require("dojo.widget.Button");
dojo.require("dojo.lfx.html");
dojo.hostenv.writeIncludes();

alfresco_xforms_constants.XFORMS_ERROR_DIV_ID = "alfresco-xforms-error";
alfresco_xforms_constants.AJAX_LOADER_DIV_ID = "alfresco-ajax-loader";

alfresco_xforms_constants.EXPANDED_IMAGE = new Image();
alfresco_xforms_constants.EXPANDED_IMAGE.src = 
  alfresco_xforms_constants.WEBAPP_CONTEXT + "/images/icons/expanded.gif";

alfresco_xforms_constants.COLLAPSED_IMAGE = new Image();
alfresco_xforms_constants.COLLAPSED_IMAGE.src = 
  alfresco_xforms_constants.WEBAPP_CONTEXT + "/images/icons/collapsed.gif";

function _xforms_init()
{
  document.xform = new alfresco.xforms.XForm();
}

dojo.addOnLoad(_xforms_init);

tinyMCE.init({
  theme: "advanced",
  mode: "exact",
  width: -1,
  auto_resize: false,
  force_p_newlines: false,
  encoding: null,
  add_unload_trigger: false,
  add_form_submit_trigger: false,
  theme_advanced_toolbar_location: "top",
  theme_advanced_toolbar_align: "left",
  theme_advanced_buttons1: "bold,italic,underline,strikethrough,separator,fontselect,fontsizeselect",
  theme_advanced_buttons2: "link,unlink,image,separator,justifyleft,justifycenter,justifyright,justifyfull,separator,bullist,numlist,separator,undo,redo,separator,forecolor,backcolor",
  theme_advanced_buttons3: ""
});

dojo.declare("alfresco.xforms.Widget",
             null,
             {
               initializer: function(xform, xformsNode) 
               {
                 this.xform = xform;
                 this.xformsNode = xformsNode;
                 this.id = this.xformsNode.getAttribute("id");
                 this.modified = false;
                 this.valid = true;
                 var b = this.xform.getBinding(this.xformsNode);
                 if (b)
                 {
                   dojo.debug("adding " + this.id + " to binding " + b.id);
                   b.widgets[this.id] = this;
                 }
                 else
                 {
                   dojo.debug("no binding found for " + this.id);
                 }
                 this.domNode = document.createElement("div");
                 this.domNode.setAttribute("id", this.id + "-domNode")
                 this.domNode.widget = this;
                 dojo.html.setClass(this.domNode, "xformsItem");
               },
               xformsNode: null,
               labelNode: null,
               parent: null,
               domContainer: null,
               setModified: function(b)
               {
                 this.modified = b;
                 this._updateDisplay();
                 this.hideAlert();
               },
               setValid: function(b)
               {
                 this.valid = b;
                 this._updateDisplay();
                 this.hideAlert();
               },
               isValidForSubmit: function()
               {
                 if (!this.valid)
                   return false;
                 if (!this.modified && this.isRequired() && this.getInitialValue() == null)
                   return false;
                 if (this.isRequired() && this.getValue() == null)
                   return false;
                 return true;
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
               setEnabled: function(enabled)
               {
               },
               setRequired: function(b)
               {
                 this.required = b;
                 this._updateDisplay();
               },
               isRequired: function()
               {
                 if (typeof this.required != "undefined")
                   return this.required;
                 var binding = this.xform.getBinding(this.xformsNode);
                 return binding && binding.required == "true()";
               },
               setReadonly: function(readonly)
               {
                 this.readonly = readonly;
               },
               isReadonly: function()
               {
                 if (typeof this.readonly != "undefined")
                   return this.readonly;
                 var binding = this.xform.getBinding(this.xformsNode);
                 return binding && binding.readonly == "true()";
               },
               setInitialValue: function(value)
               {
                 this.initialValue = value;
               },
               getInitialValue: function()
               {
                 if (typeof this.initialValue != "undefined")
                   return this.initialValue;

                 var xpath = this._getXPathInInstanceDocument();
                 var d = this.xformsNode.ownerDocument;
                 var contextNode = this.xform.getInstance();
                 dojo.debug("locating " + xpath + " in " + contextNode.nodeName);
                 var result = _evaluateXPath("/" + xpath, 
                                             this.xform.getInstance(), 
                                             XPathResult.FIRST_ORDERED_NODE_TYPE);
                 if (!result)
                 {
                   dojo.debug("unable to resolve xpath  /" + xpath + " for " + this.id);
                   return null;
                 }
                 result = (result.nodeType == dojo.dom.ELEMENT_NODE
                           ? dojo.dom.textContent(result)
                           : result.nodeValue);
                 dojo.debug("resolved xpath " + xpath + " to " + result);
                 return result;
               },
               _getXPathInInstanceDocument: function()
               {
                 var binding = this.xform.getBinding(this.xformsNode);
                 var xpath = '';
                 var repeatIndices = this.getRepeatIndices();
                 do
                 {
                   var s = binding.nodeset;
                   if (binding.nodeset == '.')
                   {
                     binding = binding.parent;
                   }
                   if (binding.nodeset.match(/.+\[.+\]/))
                   {
                     s = binding.nodeset.replace(/([^\[]+)\[.*/, "$1");
                     s += '[' + (repeatIndices.shift().index + 1) + ']';
                   }
                   xpath = s + (xpath.length != 0 ? '/' + xpath : "");
                   binding = binding.parent;
                 }
                 while (binding);
                 return xpath;
               },
               _getLabelNode: function()
               {
                 var labels = _getElementsByTagNameNS(this.xformsNode, 
                                                      alfresco_xforms_constants.XFORMS_NS,
                                                      alfresco_xforms_constants.XFORMS_PREFIX,
                                                      "label");
                 for (var i = 0; i < labels.length; i++)
                 {
                   if (labels[i].parentNode == this.xformsNode)
                     return labels[i];
                 }
                 return null;
               },
               _getAlertNode: function()
               {
                 var labels = _getElementsByTagNameNS(this.xformsNode, 
                                                      alfresco_xforms_constants.XFORMS_NS,
                                                      alfresco_xforms_constants.XFORMS_PREFIX,
                                                      "alert");
                 for (var i = 0; i < labels.length; i++)
                 {
                   if (labels[i].parentNode == this.xformsNode)
                     return labels[i];
                 }
                 return null;
               },
               getLabel: function()
               {
                 var node = this._getLabelNode();
                 var result = node ? dojo.dom.textContent(node) : "";
                 if (djConfig.isDebug)
                   result += " [" + this.id + "]";
                 return result;
               },
               getAlert: function()
               {
                 var node = this._getAlertNode();
                 return node ? dojo.dom.textContent(node) : "";
               },
               showAlert: function()
               {
                 if (this.labelNode._backupColor != "red")
                   this.labelNode._backupColor = this.labelNode.style.color;
                 this.labelNode.style.color = "red";
               },
               hideAlert: function()
               {
                 if (this.labelNode.style.color == "red")
                   this.labelNode.style.color = this.labelNode._backupColor;
               },
               _updateDisplay: function()
               {
//                 this.domContainer.style.backgroundColor =  
//                   (!this.valid ? "yellow" : this.modified ? "lightgreen" : "white");
               },
               _destroy: function()
               {
                 dojo.debug("destroying " + this.id);
               },
               getRepeatIndices: function()
               {
                 var result = [];
                 var w = this;
                 while (w.parent)
                 {
                   if (w.parent instanceof alfresco.xforms.Repeat)
                   {
                     result.push(new alfresco.xforms.RepeatIndexData(w.parent,
                                                                     w.parent.getChildIndex(w)));
                   }
                   w = w.parent;
                 }
                 return result;
               }
             });

dojo.declare("alfresco.xforms.FilePicker",
             alfresco.xforms.Widget,
             {
               initializer: function(xform, xformsNode)
               {
               },
               render: function(attach_point)
               {
                 dojo.html.prependClass(this.domNode, "xformsFilePicker");
                 attach_point.appendChild(this.domNode);
                 //XXXarielb support readonly and disabled
                 this.widget = new FilePickerWidget(this.domNode, 
                                                    this.getInitialValue(), 
                                                    false,
                                                    this._filePicker_changeHandler,
                                                    this._filePicker_resizeHandler);
                 this.widget.render();
               },
               getValue: function()
               {
                 return this.widget.getValue();
               },
               setValue: function(value)
               {
                 if (!this.widget)
                   this.setInitialValue(value);
                 else
                   this.widget.setValue(value);
               },
               _filePicker_changeHandler: function(fpw)
               {
                 var w = fpw.node.widget;
                 w.xform.setXFormsValue(w.id, w.getValue());
               },
               _filePicker_resizeHandler: function(fpw) 
               { 
                 var w = fpw.node.widget;
                 w.domContainer.style.height = fpw.node.offsetHeight + "px";
               }
             });

dojo.declare("alfresco.xforms.DatePicker",
             alfresco.xforms.Widget,
             {
               initializer: function(xform, xformsNode) 
               {
//           this.inherited("initializer", [ xform, xformsNode ]);
               },
               render: function(attach_point)
               {
                 var initial_value = this.getInitialValue() || "";
               
                 attach_point.appendChild(this.domNode);
                 this.widget = document.createElement("input");
                 this.widget.setAttribute("id", this.id + "-widget");
                 this.widget.setAttribute("type", "text");
                 this.widget.setAttribute("value", initial_value);
                 this.domNode.appendChild(this.widget);
                 dojo.event.connect(this.widget, "onfocus", this, this._dateTextBox_focusHandler);
               
                 var datePickerDiv = document.createElement("div");
                 attach_point.appendChild(datePickerDiv);
               
                 var dp_initial_value = (initial_value
                                         ? initial_value
                                         : dojo.widget.DatePicker.util.toRfcDate(new Date()));
                 this.widget.picker = dojo.widget.createWidget("DatePicker", 
                                                               { 
                                                                 isHidden: true, 
                                                                 storedDate: dp_initial_value
                                                               }, 
                                                               datePickerDiv);
                 this.widget.picker.hide();
                 dojo.event.connect(this.widget.picker,
                                    "onSetDate", 
                                    this,
                                    this._datePicker_setDateHandler);
               },
               setValue: function(value)
               {
                 throw new Error("setValue unimplemented for DatePicker");
               },
               getValue: function()
               {
                 return (this.widget.value == null || this.widget.value.length == 0
                         ? null
                         : this.widget.value);
               },
               _dateTextBox_focusHandler: function(event)
               {
                 dojo.style.hide(this.widget);
                 this.widget.picker.show();
                 this.domContainer.style.height = 
                   this.widget.picker.domNode.offsetHeight + "px";
               },
               _datePicker_setDateHandler: function(event)
               {
                 this.widget.picker.hide();
                 dojo.style.show(this.widget);
                 this.domContainer.style.height = 
                   Math.max(this.widget.offsetHeight, 20) + "px";
                 this.widget.value = dojo.widget.DatePicker.util.toRfcDate(this.widget.picker.date);
                 this.xform.setXFormsValue(this.id, this.getValue());
               }
             });

dojo.declare("alfresco.xforms.TextField",
             alfresco.xforms.Widget,
             {
               initializer: function(xform, xformsNode) 
               {
//           this.inherited("initializer", [ xform, xformsNode ]);
               },
               render: function(attach_point)
               {
                 var initial_value = this.getInitialValue() || "";
                 attach_point.appendChild(this.domNode);
               
                 this.widget = document.createElement("input");
                 this.widget.setAttribute("type", "text");
                 this.widget.setAttribute("id", this.id + "-widget");
                 this.widget.setAttribute("value", initial_value);
                 if (this.xform.getBinding(this.xformsNode).getType() == "string")
                 {
                   this.widget.style.width = "100%";
                 }

                 this.domNode.appendChild(this.widget);
                 if (this.isReadonly())
                 {
                   this.widget.setAttribute("readonly", this.isReadonly());
                   this.widget.setAttribute("disabled", this.isReadonly());
                 }
                 else
                 {
                   dojo.event.connect(this.widget, "onblur", this, this._widget_changeHandler);
                 }
               },
               setValue: function(value)
               {
                 if (!this.widget)
                   this.setInitialValue(value);
                 else
                   this.widget.value = value;
               },
               getValue: function()
               {
                 var result = this.widget.value;
                 if (result != null && result.length == 0)
                   result = null;
                 return result;
               },
               _widget_changeHandler: function(event)
               {
                 this.xform.setXFormsValue(this.id, this.getValue());
               }
             });

dojo.declare("alfresco.xforms.TextArea",
             alfresco.xforms.Widget,
             {
               initializer: function(xform, xformsNode) 
               {
                 this.focused = false;
               },
               render: function(attach_point)
               {
                 attach_point.appendChild(this.domNode);
                 dojo.html.prependClass(this.domNode, "xformsTextArea");
                 this.widget = document.createElement("div");
                 this.domNode.appendChild(this.widget);
                 dojo.html.prependClass(this.widget, "xformsTextArea");
                 this.widget.innerHTML = this.getInitialValue() || "";
                 if (!this.isReadonly())
                 {
                   this._createTinyMCE();
                 }
               },
               setValue: function(value)
               {
                 if (this.isReadonly())
                 {
                   this.widget.innerHTML = value;
                 }
                 else
                 {
                   tinyMCE.selectedInstance = tinyMCE.getInstanceById(this.id);
                   tinyMCE.setContent(value);
                 }
               },
               getValue: function()
               {
                 return this.isReadonly() ? this.widget.innerHTML : tinyMCE.getContent(this.id);
               },
               setReadonly: function(readonly)
               {
                 this.inherited("setReadonly", [ readonly ]);
                 var mce = tinyMCE.getInstanceById(this.id);
                 if (readonly && mce)
                 {
                   this._removeTinyMCE();
                 }
                 else if (!readonly && !mce && this.widget)
                 {
                   this._createTinyMCE();
                 }
               },
               _tinyMCE_blurHandler: function(event)
               {
                 var widget = event.target.widget;
                 widget.xform.setXFormsValue(widget.id, widget.getValue());
                 this.focused = false;
               },
               _tinyMCE_focusHandler: function(event)
               {
                 var widget = event.target.widget;
                 var repeatIndices = widget.getRepeatIndices();
                 if (repeatIndices.length != 0 && !this.focused)
                 {
                   var r = repeatIndices[repeatIndices.length - 1].repeat;
                   var p = widget;
                   while (p && p.parent != r)
                   {
                     if (p.parent instanceof alfresco.xforms.Repeat)
                     {
                       throw new Error("unexpected parent repeat " + p.parent.id);
                     }
                     p = p.parent;
                   }
                   if (!p)
                   {
                     throw new Error("unable to find parent repeat " + r.id +
                                     " of " + widget.id);
                   }
                   repeatIndices[repeatIndices.length - 1].repeat.setFocusedChild(p);
                 }
                 this.focused = true;
               },
               _destroy: function()
               {
                 this.inherited("_destroy", []);
                 if (!this.isReadonly())
                 {
                   dojo.debug("removing mce control " + this.id);
                   tinyMCE.removeMCEControl(this.id);
                 }
               },
               _removeTinyMCE: function()
               {
                 var value = tinyMCE.getContent(this.id);
                 tinyMCE.removeMCEControl(this.id);
               },
               _createTinyMCE:function()
               {
                 tinyMCE.addMCEControl(this.widget, this.id);
                 
                 var editorDocument = tinyMCE.getInstanceById(this.id).getDoc();
                 editorDocument.widget = this;
                 tinyMCE.addEvent(editorDocument, "blur", this._tinyMCE_blurHandler);
                 tinyMCE.addEvent(editorDocument, "focus", this._tinyMCE_focusHandler);
               }
             });

dojo.declare("alfresco.xforms.AbstractSelectWidget",
             alfresco.xforms.Widget,
             {
               initializer: function(xform, xformsNode) 
               {
               },
               getValues: function()
               {
                 var binding = this.xform.getBinding(this.xformsNode);
                 var values = _getElementsByTagNameNS(this.xformsNode, 
                                                      alfresco_xforms_constants.XFORMS_NS,
                                                      alfresco_xforms_constants.XFORMS_PREFIX, 
                                                      "item");
                 var result = [];
                 for (var v = 0; v < values.length; v++)
                 {
                   var label = _getElementsByTagNameNS(values[v], 
                                                       alfresco_xforms_constants.XFORMS_NS,
                                                       alfresco_xforms_constants.XFORMS_PREFIX,
                                                       "label")[0];
                   var value = _getElementsByTagNameNS(values[v], 
                                                       alfresco_xforms_constants.XFORMS_NS,
                                                       alfresco_xforms_constants.XFORMS_PREFIX, 
                                                       "value")[0];
                   var valid = true;
                   if (binding.constraint)
                   {
                     dojo.debug("testing " + binding.constraint + 
                                " on " + dojo.dom.textContent(value));
                     valid = _evaluateXPath(binding.constraint, value, XPathResult.BOOLEAN_TYPE);
                   }
                   dojo.debug("valid " + dojo.dom.textContent(value) + "? " + valid);
                   if (valid)
                   {
                     result.push({ 
                       id: value.getAttribute("id"), 
                       label: dojo.dom.textContent(label),
                       value: dojo.dom.textContent(value)
                     });
                   }
                 }
                 return result;
               }
             });

dojo.declare("alfresco.xforms.Select",
             alfresco.xforms.AbstractSelectWidget,
             {
               initializer: function(xform, xformsNode) 
               {
//           this.inherited("initializer", [ xform, xformsNode ]);
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
                 initial_value = initial_value ? initial_value.split(' ') : [];
                 this._selectedValues = [];
                 if (values.length <= 5)
                 {
                   this.widget = document.createElement("div");
                   this.widget.style.width = "100%";
                   attach_point.appendChild(this.widget);
                   for (var i = 0; i < values.length; i++)
                   {
                     var checkboxDiv = document.createElement("div");
                     checkboxDiv.style.lineHeight = "16px";
                     this.widget.appendChild(checkboxDiv);

                     var checkbox = document.createElement("input");
                     checkbox.setAttribute("id", this.id + "_" + i + "-widget");
                     checkbox.setAttribute("name", this.id + "_" + i + "-widget");
                     checkbox.setAttribute("type", "checkbox");
                     checkbox.setAttribute("value", values[i].value);
                     if (initial_value.indexOf(values[i].value) != -1)
                     {
                       this._selectedValues.push(values[i].value);
                       checkbox.checked = true;
                     }
                     checkboxDiv.appendChild(checkbox);
                     checkboxDiv.appendChild(document.createTextNode(values[i].label));
                     dojo.event.connect(checkbox, "onclick", this, this._checkbox_clickHandler);
                   }
                 }
                 else
                 {
                   this.widget = document.createElement("select");
                   this.widget.setAttribute("id", this.id + "-widget");
                   this.widget.setAttribute("multiple", true);
                   attach_point.appendChild(this.widget);
                   for (var i = 0; i < values.length; i++)
                   {
                     var option = document.createElement("option");
                     option.appendChild(document.createTextNode(values[i].label));
                     option.setAttribute("value", values[i].value);
                     if (initial_value.indexOf(values[i].value) != -1)
                     {
                       this._selectedValues.push(values[i].value);
                       option.selected = true;
                     }
                     this.widget.appendChild(option);
                   }
                   dojo.event.connect(this.widget, "onblur", this, this._list_changeHandler);
                 }
               },
               setValue: function(value)
               {
                 throw new Error("setValue unimplemented for Select");
               },
               getValue: function()
               {
                 return this._selectedValues.join(" ");
               },
               _list_changeHandler: function(event) 
               { 
                 this._selectedValues = [];
                 for (var i = 0; i < event.target.options.length; i++)
                 {
                   if (event.target.options[i].selected)
                     this._selectedValues.push(event.target.options[i].value);
                 }
                 this.xform.setXFormsValue(this.id, this._selectedValues.join(" "));
               },
               _checkbox_clickHandler: function(event)
               { 
                 this._selectedValues = [];
                 for (var i = 0; i < 5; i++)
                 {
                   var checkbox = document.getElementById(this.id + "_" + i + "-widget");
                   if (checkbox && checkbox.checked)
                     this._selectedValues.push(checkbox.value);
                 }
                 this.xform.setXFormsValue(this.id, this._selectedValues.join(" "));
               }
             });

dojo.declare("alfresco.xforms.Select1",
             alfresco.xforms.AbstractSelectWidget,
             {
               initializer: function(xform, xformsNode) 
               {
//           this.inherited("initializer", [ xform, xformsNode ]);
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
                   this.widget = document.createElement("div");
                   this.widget.style.width = "100%";
                   attach_point.appendChild(this.widget);
                   for (var i = 0; i < values.length; i++)
                   {
                     var radio_div = document.createElement("div");
                     radio_div.style.lineHeight = "16px";
                     this.widget.appendChild(radio_div);
                     var radio = document.createElement("input");
                     radio.setAttribute("id", this.id + "-widget");
                     radio.setAttribute("name", this.id + "-widget");
                     radio.setAttribute("type", "radio");
                     radio_div.appendChild(radio);
                     radio_div.appendChild(document.createTextNode(values[i].label));

                     radio.setAttribute("value", values[i].value);
                     if (values[i].value == initial_value)
                     {
                       this._selectedValue = initial_value;
                       radio.checked = true;
                     }
                     dojo.event.connect(radio, "onclick", this, this._radio_clickHandler);
                   }
                   this.widget.style.height = this.widget.offsetHeight + "px";
                 }
                 else
                 {
                   this.widget = document.createElement("select");
                   this.widget.setAttribute("id", this.id + "-widget");
                   attach_point.appendChild(this.widget);
                   for (var i = 0; i < values.length; i++)
                   {
                     var option = document.createElement("option");
                     this.widget.appendChild(option);
                     option.appendChild(document.createTextNode(values[i].label));
                     option.setAttribute("value", values[i].value);
                     if (values[i].value == initial_value)
                     {
                       this._selectedValue = initial_value;
                       option.selected = true;
                     }
                   }
                   dojo.event.connect(this.widget, "onchange", this, this._combobox_changeHandler);
                 }
               },
               setValue: function(value)
               {
                 throw new Error("setValue unimplemented for Select1");
               },
               getValue: function()
               {
                 return this._selectedValue;
               },
               _combobox_changeHandler: function(event) 
               { 
                 this._selectedValue = event.target.options[event.target.selectedIndex].value;
                 this.xform.setXFormsValue(this.id, this._selectedValue);
               },
               _radio_clickHandler: function(event)
               { 
                 if (!event.target.checked)
                 {
                   var all_radios = this.widget.getElementsByTagName("input");
                   for (var i = 0; i < all_radios.length; i++)
                   {
                     if (all_radios[i].name == event.target.name)
                       all_radios[i].checked = event.target == all_radios[i];
                   }
                 }
                 this._selectedValue = event.target.value;
                 this.xform.setXFormsValue(this.id, event.target.value);
               }
             });

dojo.declare("alfresco.xforms.Checkbox",
             alfresco.xforms.Widget,
             {
               initializer: function(xform, xformsNode) 
               {
//           this.inherited("initializer", [ xform, xformsNode ]);
               },
               render: function(attach_point)
               {
                 var initial_value = this.getInitialValue() == "true";
                 this.widget = document.createElement("input");
                 this.widget.setAttribute("type", "checkbox");
                 this.widget.setAttribute("id", this.id + "-widget");
                 attach_point.appendChild(this.widget);

                 if (initial_value)
                   this.widget.setAttribute("checked", true);
                 dojo.event.connect(this.widget, "onclick", this, this._checkbox_clickHandler);
               },
               setValue: function(value)
               {
                 this.widget.checked = value == "true";
               },
               getValue: function()
               {
                 return this.widget.checked;
               },
               _checkbox_clickHandler: function(event)
               {
                 this.xform.setXFormsValue(this.id, this.widget.checked);
               }
             });

dojo.declare("alfresco.xforms.Group",
             alfresco.xforms.Widget,
             {
               initializer: function(xform, xformsNode) 
               {
                 this.children = [];
                 dojo.html.removeClass(this.domNode, "xformsItem");
                 this.showHeader = false;
               },
               setShowHeader: function(showHeader)
               {
                 if (showHeader == this.showHeader)
                 {
                   return;
                 }

                 this.showHeader = showHeader;
                 if (this.showHeader && this.groupHeaderNode.style.display == "none")
                 {
                   this.groupHeaderNode.style.display = "block";
                 }
                 if (!this.showHeader && this.groupHeaderNode.style.display == "block")
                 {
                   this.groupHeaderNode.style.display = "none";
                 }
               },
               getWidgetsInvalidForSubmit: function()
               {
                 var result = [];
                 for (var i = 0; i < this.children.length; i++)
                 {
                   if (this.children[i] instanceof alfresco.xforms.Group)
                     result = result.concat(this.children[i].getWidgetsInvalidForSubmit());
                   else if (!this.children[i].isValidForSubmit())
                     result.push(this.children[i]);
                 }
                 return result;
               },
               getChildAt: function(index)
               {     
                 return index < this.children.length ? this.children[index] : null;
               },
               getChildIndex: function(child)
               {
                 for (var i = 0; i < this.children.length; i++)
                 {
                   dojo.debug(this.id + "[" + i + "]: " + 
                              " is " + this.children[i].id + 
                              " the same as " + child.id + "?");
                   if (this.children[i] == child)
                   {
                     return i;
                   }
                 }
                 return -1;
               },
               addChild: function(child)
               {
                 return this.insertChildAt(child, this.children.length);
               },
               insertChildAt: function(child, position)
               {
                 dojo.debug(this.id + ".insertChildAt(" + child.id + ", " + position + ")");
                 child.parent = this;
         
                 child.domContainer = document.createElement("div");
                 child.domContainer.setAttribute("id", child.id + "-domContainer");
                 child.domContainer.style.margin = "2px 0px 2px 0px";
                 child.domContainer.style.padding = "0px";
                 child.domContainer.style.position = "relative";
                 child.domContainer.style.left = "0px";
                 child.domContainer.style.top = "0px";

                 if (this.parent && this.parent.domNode)
                 {
                   child.domContainer.style.top = this.parent.domNode.style.bottom;
                 }
         
                 if (position == this.children.length)
                 {
                   this.domNode.childContainerNode.appendChild(child.domContainer);
                   this.children.push(child);
                 }
                 else
                 {
                   this.domNode.childContainerNode.insertBefore(child.domContainer, 
                                                                this.getChildAt(position).domContainer);
                   this.children.splice(position, 0, child);
                 }
         
                 if (!(child instanceof alfresco.xforms.Group))
                 {
                   var requiredImage = document.createElement("img");
                   requiredImage.setAttribute("src", 
                                              alfresco_xforms_constants.WEBAPP_CONTEXT + "/images/icons/required_field.gif");
                   requiredImage.style.marginLeft = "5px";
                   requiredImage.style.marginRight = "5px";
                   child.domContainer.appendChild(requiredImage);
            
                   if (!child.isRequired())
                     requiredImage.style.visibility = "hidden";
                   var label = child.getLabel();
                   if (label)
                   {
                     child.labelNode = document.createElement("span");
                     child.domContainer.appendChild(child.labelNode);
                     child.labelNode.appendChild(document.createTextNode(label));
                   }
                 }
                 var contentDiv = document.createElement("div");
                 contentDiv.setAttribute("id", child.id + "-content");
                 dojo.html.setClass(contentDiv, "xformsGroupItem");
                 child.domContainer.appendChild(contentDiv);


                 contentDiv.style.left = (child instanceof alfresco.xforms.Group 
                                          ? "0px" 
                                          : "30%");
                 if (!(child instanceof alfresco.xforms.Group))
                 {
                   contentDiv.style.width = (1 - (contentDiv.offsetLeft / 
                                                  child.domContainer.offsetWidth)) * 100 + "%";
                 }
                 child.render(contentDiv);
                 if (!(child instanceof alfresco.xforms.Group))
                 {
                   child.domContainer.style.height = Math.max(contentDiv.offsetHeight, 20) + "px";
                   child.domContainer.style.lineHeight = child.domContainer.style.height;
                 }

                 dojo.debug(contentDiv.getAttribute("id") + " offsetTop is " + contentDiv.offsetTop);
                 contentDiv.style.top = "-" + contentDiv.offsetTop + "px";
                 contentDiv.widget = child;
         
                 this._updateDisplay();

                 this._childAdded(child);

                 return child.domContainer;
               },
               removeChildAt: function(position)
               {
                 var child = this.getChildAt(position);
                 if (!child)
                   throw new Error("unable to find child at " + position);

                 this.children.splice(position, 1);
                 child.domContainer.group = this;
                 var anim = dojo.lfx.html.fadeOut(child.domContainer, 500);
                 anim.onEnd = function()
                   {
                     child.domContainer.style.display = "none";
                     child._destroy();

                     dojo.dom.removeChildren(child.domContainer);
                     dojo.dom.removeNode(child.domContainer);

                     child.domContainer.group._updateDisplay();
                   };
                 anim.play();

                 this._childRemoved(child);

                 return child;
               },
               _destroy: function()
               {
                 this.inherited("_destroy", []);
                 for (var i = 0; i < this.children.length; i++)
                 {
                   this.children[i]._destroy();
                 }
               },
               setReadonly: function(readonly)
               {
                 this.inherited("setReadonly", [ readonly ]);
                 for (var i = 0; i < this.children.length; i++)
                 {
                   this.children[i].setReadonly(readonly);
                 }
               },
               render: function(attach_point)
               {
                 this.domNode.widget = this;
                 attach_point.appendChild(this.domNode);
                 dojo.html.setClass(this.domNode, "xformsGroup");

                 if (false && djConfig.isDebug)
                 {
                   var idNode = document.createElement("div");
                   idNode.style.backgroundColor = "red";
                   idNode.appendChild(document.createTextNode(this.getLabel()));
                   this.domNode.appendChild(idNode);
                 }

                 this.groupHeaderNode = document.createElement("div");
                 this.groupHeaderNode.id = this.id + "-groupHeaderNode";
                 this.domNode.appendChild(this.groupHeaderNode);
                 dojo.html.setClass(this.groupHeaderNode, "xformsGroupHeader");
                 this.groupHeaderNode.style.display = "none";

                 this.toggleExpandedImage = document.createElement("img");
                 this.groupHeaderNode.appendChild(this.toggleExpandedImage);
                 this.toggleExpandedImage.setAttribute("src", alfresco_xforms_constants.EXPANDED_IMAGE.src);
                 this.toggleExpandedImage.align = "absmiddle";
                 this.toggleExpandedImage.style.marginLeft = "5px";
                 this.toggleExpandedImage.style.marginRight = "5px";
                 
                 dojo.event.connect(this.toggleExpandedImage, 
                                    "onclick", 
                                    this, 
                                    this._toggleExpanded_clickHandler);

                 this.groupHeaderNode.appendChild(document.createTextNode(this.getLabel()));

                 this.domNode.childContainerNode = document.createElement("div");
                 this.domNode.childContainerNode.setAttribute("id", this.id + "-childContainerNode");
                 this.domNode.appendChild(this.domNode.childContainerNode);
                 this.domNode.childContainerNode.style.width = "100%";
                 return this.domNode;
               },
               isExpanded: function()
               {
                 return this.toggleExpandedImage.getAttribute("src") == alfresco_xforms_constants.EXPANDED_IMAGE.src;
               },
               setExpanded: function(expanded)
               {
                 if (expanded != this.isExpanded())
                 {
                   this.toggleExpandedImage.src = 
                     (expanded 
                      ? alfresco_xforms_constants.EXPANDED_IMAGE.src 
                      : alfresco_xforms_constants.COLLAPSED_IMAGE.src);
                   this.domNode.childContainerNode.style.display = expanded ? "block" : "none";
                 }
               },
               _toggleExpanded_clickHandler: function(event)
               {
                 this.setExpanded(!this.isExpanded());
               },
               _updateDisplay: function()
               {
                 for (var i = 0; i < this.children.length; i++)
                 {
                   this.children[i]._updateDisplay();
                 }
               },
               showAlert: function()
               {
                 for (var i = 0; i < this.children.length; i++)
                 {
                   this.children[i].showAlert();
                 }
               },
               hideAlert: function()
               {
                 for (var i = 0; i < this.children.length; i++)
                 {
                   this.children[i].hideAlert();
                 }
               },
               _childAdded: function(child)
               {
                 var hasNonGroupChildren = false;
                 for (var i in this.children)
                 {
                   if (!(this.children[i] instanceof alfresco.xforms.Group))
                   {
                     hasNonGroupChildren = true;
                     break;
                   }
                 }
                 this.setShowHeader(hasNonGroupChildren &&
                                    this.children.length != 1 &&
                                    this.parent != null);
               },
               _childRemoved: function(child)
               {
               }
             });

alfresco.xforms.RepeatIndexData = function(repeat, index)
{
  this.repeat = repeat;
  this.index = index;
  this.toString = function()
  {
    return "{" + this.repeat.id + " = " + this.index + "}";
  };
}

dojo.declare("alfresco.xforms.Repeat",
             alfresco.xforms.Group,
             {
               initializer: function(xform, xformsNode) 
               {
                 this.showHeader = true;
                 this.repeatControls = [];
                 this._selectedIndex = -1;
               },
               getLabel: function()
               {
                 var label = this.parent.getLabel();
                 if (djConfig.isDebug)
                   label += " [" + this.id + "]";
                 return label;
               },
               isInsertRepeatItemEnabled: function()
               {
                 var maximum = this.xform.getBinding(this.xformsNode).maximum;
                 maximum = isNaN(maximum) ? Number.MAX_VALUE : maximum;
                 return this.children.length < maximum;
               },
               isRemoveRepeatItemEnabled: function()
               {
                 var minimum = this.xform.getBinding(this.xformsNode).minimum;
                 minimum = isNaN(minimum) ? this.isRequired() ? 1 : 0 : minimum;
                 return this.children.length > minimum;
               },
               insertChildAt: function(child, position)
               {
                 this.repeatControls.splice(position, 0, document.createElement("div"));
                 var images = 
                   [ 
                     { name: "insertRepeatItemImage", src: "plus", action: this._insertRepeatItemAfter_handler },
                     { name: "moveRepeatItemUpImage", src: "arrow_up", action: this._moveRepeatItemUp_handler },
                     { name: "moveRepeatItemDownImage", src: "arrow_down", action: this._moveRepeatItemDown_handler }, 
                     { name: "removeRepeatItemImage", src: "minus", action: this._removeRepeatItem_handler }
                   ];
                 var repeatControlsWidth = 0;
                 for (var i = 0; i < images.length; i++)
                 {
                   var img = document.createElement("img");
                   this.repeatControls[position][images[i].name] = img;
                   img.setAttribute("src", (alfresco_xforms_constants.WEBAPP_CONTEXT + "/images/icons/" + 
                                            images[i].src + ".gif"));
                   img.style.width = "16px";
                   img.style.height = "16px";
                   img.style.margin = "2px 5px 2px " + (i == 0 ? 5 : 0) + "px";
                   img.repeat = this;
                   repeatControlsWidth += (parseInt(img.style.width) + 
                                           parseInt(img.style.marginRight) +
                                           parseInt(img.style.marginLeft));
                   this.repeatControls[position].appendChild(img);
                   dojo.event.connect(img, "onclick", this, images[i].action);
                 }

                 var result = this.inherited("insertChildAt", [ child, position ]);
                 child.repeat = this;
                 dojo.event.connect(result, "onclick", function(event)
                                    {
                                      child.repeat.setFocusedChild(child);
                                    });

                 result.style.border = "1px solid black";
                 if (result.nextSibling)
                 {
                   result.parentNode.insertBefore(this.repeatControls[position], 
                                                  result.nextSibling);
                 }
                 else
                 {
                   result.parentNode.appendChild(this.repeatControls[position]);
                 }

                 dojo.html.setClass(this.repeatControls[position], "xformsRepeatControls");
                 this.repeatControls[position].style.width = repeatControlsWidth + "px";
                 this.repeatControls[position].style.backgroundColor = result.style.backgroundColor;

                 result.style.paddingBottom = (.5 * this.repeatControls[position].offsetHeight) + "px";

                 this.repeatControls[position].style.top = -(.5 * (this.repeatControls[position].offsetHeight ) +
                                                             parseInt(result.style.marginBottom) +
                                                             parseInt(result.style.borderBottomWidth)) + "px";
                 this.repeatControls[position].style.marginRight =
                   (.5 * result.offsetWidth - 
                    .5 * this.repeatControls[position].offsetWidth) + "px"; 

                 this.repeatControls[position].style.marginLeft = 
                   (.5 * result.offsetWidth - 
                    .5 * this.repeatControls[position].offsetWidth) + "px"; 
                 return result;
               },
               removeChildAt: function(position)
               {
                 this.repeatControls[position].style.display = "none";
                 dojo.dom.removeChildren(this.repeatControls[position]);
                 dojo.dom.removeNode(this.repeatControls[position]);
                 this.repeatControls.splice(position, 1);
                 return this.inherited("removeChildAt", [ position ]);
               },
               getSelectedIndex: function()
               {
                 this._selectedIndex = Math.min(this.children.length - 1, this._selectedIndex);
                 if (this.children.length == 0)
                 {
                   this._selectedIndex = -1;
                 }
                 return this._selectedIndex;
               },
               _updateDisplay: function()
               {
                 this.inherited("_updateDisplay", []);
                 for (var i = 0; i < this.children.length; i++)
                 {
                   if (dojo.html.hasClass(this.children[i].domContainer,
                                          "xformsRow" + (i % 2 ? "Odd" : "Even")))
                   {
                     dojo.html.removeClass(this.children[i].domContainer,
                                           "xformsRow" + (i % 2 ? "Odd" : "Even"));
                   }
                   dojo.html.addClass(this.children[i].domContainer, 
                                      "xformsRow" + (i % 2 ? "Even" : "Odd")); 
                 }
               },
               _getRepeatItemTrigger: function(type, properties)
               {
                 var bw = this.xform.getBinding(this.xformsNode).widgets;
                 for (var i in bw)
                 {
                   if (! (bw[i] instanceof alfresco.xforms.Trigger))
                   {
                     continue;
                   }

                   var action = bw[i].getAction();
                   if (action.getType() != type)
                   {
                     continue;
                   }

                   var propertiesEqual = true;
                   for (var p in properties)
                   {
                     if (!(p in action.properties) || 
                         action.properties[p] != properties[p])
                     {
                       propertiesEqual = false;
                       break;
                     }
                   }
                   if (propertiesEqual)
                   {
                     return bw[i];
                   }
                 }
                 throw new Error("unable to find trigger " + type + 
                                 ", properties " + properties +
                                 " for " + this.id);

               },
               _insertRepeatItemAfter_handler: function(event)
               {
                 dojo.event.browser.stopEvent(event);
                 var repeat = event.target.repeat;
                 if (repeat.isInsertRepeatItemEnabled())
                 {
                   var index = repeat.repeatControls.indexOf(event.target.parentNode);
                   var repeatItem = repeat.getChildAt(index);
                   this.setFocusedChild(repeatItem);
                   var trigger = this._getRepeatItemTrigger("insert", { position: "after" });
                   this.xform.fireAction(trigger.id);
                 }
               },
               _headerInsertRepeatItemBefore_handler: function(event)
               {
                 if (this.children.length == 0)
                 {
                   dojo.event.browser.stopEvent(event);
                   var repeat = event.target.repeat;
                   if (repeat.isInsertRepeatItemEnabled())
                   {
                     this.setFocusedChild(null);
                     var trigger = this._getRepeatItemTrigger("insert", { position: "before" });
                     this.xform.fireAction(trigger.id);
                   }
                 }
               },
               _removeRepeatItem_handler: function(event)
               {
                 dojo.event.browser.stopEvent(event);
                 var repeat = event.target.repeat;
                 if (!repeat.isRemoveRepeatItemEnabled())
                   return;

                 var index = repeat.repeatControls.indexOf(event.target.parentNode);
                 var repeatItem = repeat.getChildAt(index);
                 this.setFocusedChild(repeatItem);
                 var trigger = this._getRepeatItemTrigger("delete", {});
                 this.xform.fireAction(trigger.id);
               },
               _moveRepeatItemUp_handler: function(event)
               {
                 dojo.event.browser.stopEvent(event);
                 var repeat = event.target.repeat;
                 var index = repeat.repeatControls.indexOf(event.target.parentNode);
                 if (index == 0 || repeat.children.length == 1)
                   return;
                 repeat.swapChildren(index, index - 1);
               },
               _moveRepeatItemDown_handler: function(event)
               {
                 dojo.event.browser.stopEvent(event);
                 var repeat = event.target.repeat;
                 var index = repeat.repeatControls.indexOf(event.target.parentNode);
                 if (index == repeat.children.length - 1 || repeat.children.length == 1)
                   return;
                 repeat.swapChildren(index, index + 1);
               },
               swapChildren: function(fromIndex, toIndex)
               {
                 dojo.debug(this.id + ".swapChildren(" + fromIndex + 
                            ", " + toIndex + ")");
                 var fromChild = this.getChildAt(fromIndex);
                 var toChild = this.getChildAt(toIndex);
                 var req = create_ajax_request(this.xform,
                                               "swapRepeatItems",
                                               {
                                                 fromItemId: fromChild.xformsNode.getAttribute("id"),
                                                 toItemId: toChild.xformsNode.getAttribute("id"),
                                                 instanceId: this.xform.getInstance().getAttribute("id")
                                               },
                                               function(type, data, event)
                                               {
                                                 this.target._handleEventLog(data.documentElement)
                                               });
                 send_ajax_request(req);
                 var anim = dojo.lfx.html.fadeOut(fromChild.domContainer, 500);
                 anim.onEnd = function()
                   {
                     fromChild.domContainer.style.display = "none";
                   };
                 anim.play();

               },
               setFocusedChild: function(child)
               {
                 var repeatIndices = this.getRepeatIndices();
                 if (!child)
                 {
                   repeatIndices.push(new alfresco.xforms.RepeatIndexData(this, 0));
                   this.xform.setRepeatIndeces(repeatIndices);
                 }
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
                   {
                     repeatIndices.push(new alfresco.xforms.RepeatIndexData(this, index));
                     // xforms repeat indexes are 1-based
                     this.xform.setRepeatIndeces(repeatIndices);
                   }
                 }
               },
               render: function(attach_point)
               {
                 this.domNode = this.inherited("render", [ attach_point ]);
                 this.domNode.style.border = "1px solid black";

                 var parentRepeats = this.getRepeatIndices();
                 this.domNode.style.marginLeft = (parentRepeats.length * 10) + "px";
                 this.domNode.style.marginRight = (parseInt(this.domNode.style.marginLeft) / 2) + "px";
                 this.domNode.style.width = (1 - ((dojo.style.getBorderWidth(this.domNode) +
                                                   dojo.style.getMarginWidth(this.domNode)) /
                                                  this.domNode.offsetParent.offsetWidth)) * 100 + "%";

                 this.groupHeaderNode.style.display = "block";
                 this.groupHeaderNode.repeat = this;
                 dojo.event.connect(this.groupHeaderNode, "onclick", function(event)
                                    {
                                      if (event.target == event.currentTarget)
                                        event.currentTarget.repeat.setFocusedChild(null);
                                    });
               
                 this.headerInsertRepeatItemImage = document.createElement("img"); 
                 this.headerInsertRepeatItemImage.repeat = this;
                 this.groupHeaderNode.appendChild(this.headerInsertRepeatItemImage);
                 this.headerInsertRepeatItemImage.setAttribute("src", 
                                                               alfresco_xforms_constants.WEBAPP_CONTEXT + 
                                                               "/images/icons/plus.gif");
                 this.headerInsertRepeatItemImage.style.width = "16px";
                 this.headerInsertRepeatItemImage.style.height = "16px";
                 this.headerInsertRepeatItemImage.align = "absmiddle";
                 this.headerInsertRepeatItemImage.style.marginLeft = "5px";
           
                 dojo.event.connect(this.headerInsertRepeatItemImage, 
                                    "onclick", 
                                    this, 
                                    this._headerInsertRepeatItemBefore_handler);
           
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
                 var chibaData = _getElementsByTagNameNS(this.xformsNode, 
                                                         alfresco_xforms_constants.CHIBA_NS,
                                                         alfresco_xforms_constants.CHIBA_PREFIX,
                                                         "data");
                 chibaData = chibaData[chibaData.length - 1];
                 dojo.debug(alfresco_xforms_constants.CHIBA_PREFIX + ":data == " + dojo.dom.innerXML(chibaData));
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
                 var w = this.xform.createWidget(clonedPrototype);
                 this.insertChildAt(w, position);
                 this.xform.loadWidgets(w.xformsNode, w);
               },
               handleItemDeleted: function(position)
               {
                 dojo.debug(this.id + ".handleItemDeleted(" + position + ")");
                 this.removeChildAt(position);
               },
               _updateRepeatControls: function()
               {
                 var insertEnabled = this.isInsertRepeatItemEnabled();
                 var removeEnabled = this.isRemoveRepeatItemEnabled();
                 for (var i = 0; i < this.repeatControls.length; i++)
                 {
                   this.repeatControls[i].moveRepeatItemUpImage.style.opacity = i == 0 ? .3 : 1;
                   this.repeatControls[i].moveRepeatItemDownImage.style.opacity = 
                     (i == this.repeatControls.length - 1 ? .3 : 1);
                   this.repeatControls[i].insertRepeatItemImage.style.opacity = 
                     (insertEnabled ? 1 : .3);
                   this.repeatControls[i].removeRepeatItemImage.style.opacity = 
                     (removeEnabled ? 1 : .3);
                 }
               },
               _childAdded: function(child)
               {
                 this.headerInsertRepeatItemImage.style.opacity = .3;
                 this._updateRepeatControls();
               },
               _childRemoved: function(child)
               {
                 if (this.children.length == 0)
                 {
                   this.headerInsertRepeatItemImage.style.opacity = 1;
                 }
                 this._updateRepeatControls();
               }
             });

dojo.declare("alfresco.xforms.Trigger",
             alfresco.xforms.Widget,
             {
               initializer: function(xform, xformsNode) 
               {
               },
               isValidForSubmit: function()
               {
                 return true;
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
               getAction: function()
               {
                 var action = _getElementsByTagNameNS(this.xformsNode, 
                                                      alfresco_xforms_constants.XFORMS_NS,
                                                      alfresco_xforms_constants.XFORMS_PREFIX,
                                                      "action")[0];
                 return new alfresco.xforms.XFormsAction(this.xform, dojo.dom.firstElement(action));
               },
               _clickHandler: function(event)
               {
                 this.xform.fireAction(this.id);
               }
             });

dojo.declare("alfresco.xforms.Submit",
             alfresco.xforms.Trigger,
             {
               initializer: function(xform, xformsNode) 
               {
                 var submit_buttons = _xforms_getSubmitButtons();
                 for (var i = 0; i < submit_buttons.length; i++)
                 {
                   dojo.debug("adding submit handler for " + submit_buttons[i].getAttribute('id'));
                   submit_buttons[i].xform = this.xform;
                   dojo.event.browser.addListener(submit_buttons[i], 
                                                  "onclick", 
                                                  function(event)
                                                  {
                                                    var xform = event.target.xform;
                                                    if (!xform.submitWidget.done)
                                                    {
                                                      dojo.debug("triggering submit from handler " + event.target.id);
                                                      dojo.event.browser.stopEvent(event);
                                                      _hide_errors();
                                                      xform.submitWidget.currentButton = event.target;
                                                      xform.submitWidget.widget.buttonClick(); 
                                                      return false;
                                                    }
                                                    else
                                                    {
                                                      dojo.debug("done - doing base click on " + xform.submitWidget.currentButton.id);
                                                      xform.submitWidget.currentButton = null;
                                                      return true;
                                                    }
                                                  },
                                                  false);
                 }
               },
               render: function(attach_point)
               {
                 this.inherited("render", [ attach_point ]);
                 this.xform.submitWidget = this;
               },
               _clickHandler: function(event)
               {
                 this.done = false;
                 _hide_errors();
                 this.xform.fireAction(this.id);
               }
             });

dojo.declare("alfresco.xforms.XFormsAction",
             null,
             {
               initializer: function(xform, xformsNode)
               {
                 this.xform = xform;
                 this.xformsNode = xformsNode;
                 this.properties = [];
                 for (var i = 0; i < this.xformsNode.attributes.length; i++)
                 {
                   var attr = this.xformsNode.attributes[i];
                   if (attr.nodeName.match(new RegExp("^" + alfresco_xforms_constants.XFORMS_PREFIX + ":")))
                   {
                     this.properties[attr.nodeName.substring((alfresco_xforms_constants.XFORMS_PREFIX + ":").length)] = 
                       attr.nodeValue;
                   }
                 }
               },
               getType: function()
               {
                 return this.xformsNode.nodeName.substring((alfresco_xforms_constants.XFORMS_PREFIX + ":").length);
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
                 var targetDomNode = document.getElementById(this.targetId + "-content");
                 if (!targetDomNode)
                   throw new Error("unable to find node " + this.targetId + "-content");
                 return targetDomNode.widget;
               }
             });
dojo.declare("alfresco.xforms.Binding",
             null,
             {
               initializer: function(xformsNode, parent)
               {
                 this.xformsNode = xformsNode;
                 this.id = this.xformsNode.getAttribute("id");
                 this.readonly =  this.xformsNode.getAttribute(alfresco_xforms_constants.XFORMS_PREFIX + ":readonly");
                 this.required =  this.xformsNode.getAttribute(alfresco_xforms_constants.XFORMS_PREFIX + ":required");
                 this.nodeset =  this.xformsNode.getAttribute(alfresco_xforms_constants.XFORMS_PREFIX + ":nodeset");
                 this._type = (this.xformsNode.hasAttribute(alfresco_xforms_constants.XFORMS_PREFIX + ":type")
                               ? this.xformsNode.getAttribute(alfresco_xforms_constants.XFORMS_PREFIX + ":type")
                               : null);
                 this.constraint =  (this.xformsNode.hasAttribute(alfresco_xforms_constants.XFORMS_PREFIX + ":constraint")
                                     ? this.xformsNode.getAttribute(alfresco_xforms_constants.XFORMS_PREFIX + ":constraint")
                                     : null);
                 this.maximum = parseInt(this.xformsNode.getAttribute(alfresco_xforms_constants.ALFRESCO_PREFIX + ":maximum"));
                 this.minimum = parseInt(this.xformsNode.getAttribute(alfresco_xforms_constants.ALFRESCO_PREFIX + ":minimum"));
                 this.parent =  parent;
                 this.widgets =  {};
               },
               getType: function()
               {
                 return (this._type != null
                         ? this._type
                         : (this.parent != null ? this.parent.getType() : null));
               },
               toString: function()
               {
                 return ("{id:" + this.id + 
                         ",type:" + this.getType() + 
                         ",nodeset:" + this.nodeset + "}");
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
                                                         this.target._loadHandler(data);
                                                       }));
               },
               _loadHandler: function(xformDocument)
               {
                 this.xformDocument = xformDocument;
                 this.xformsNode = xformDocument.documentElement;
                 this._bindings = this._loadBindings(this.getModel());
               
                 var bindings = this.getBindings();
                 for (var i in bindings)
                 {
                   dojo.debug("bindings[" + i + "]=" + bindings[i].id + 
                              ", parent = " + (bindings[i].parent 
                                               ? bindings[i].parent.id
                                               : 'null'));
                 }
                 var alfUI = document.getElementById(alfresco_xforms_constants.XFORMS_UI_DIV_ID);
                 alfUI.style.width = "100%";
                 this.rootWidget = new alfresco.xforms.Group(this, alfUI);
                 this.rootWidget.render(alfUI);
                 this.loadWidgets(this.getBody(), this.rootWidget);
                 this.rootWidget._updateDisplay();
               },
               createWidget: function(node)
               {
                 dojo.debug("creating node for " + node.nodeName.toLowerCase());
                 switch (node.nodeName.toLowerCase())
                 {
                 case alfresco_xforms_constants.XFORMS_PREFIX + ":group":
                   return new alfresco.xforms.Group(this, node);
                 case alfresco_xforms_constants.XFORMS_PREFIX + ":repeat":
                   return new alfresco.xforms.Repeat(this, node);
                 case alfresco_xforms_constants.XFORMS_PREFIX + ":textarea":
                   return new alfresco.xforms.TextArea(this, node);
                 case alfresco_xforms_constants.XFORMS_PREFIX + ":upload":
                   return new alfresco.xforms.FilePicker(this, node);
                 case alfresco_xforms_constants.XFORMS_PREFIX + ":input":
                 {
                   var type = this.getBinding(node).getType();
                   switch (type)
                   {
                   case "date":
                     return new alfresco.xforms.DatePicker(this, node);
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
                   case "string":
                   default:
                     return new alfresco.xforms.TextField(this, node);
                   }
                 }
                 case alfresco_xforms_constants.XFORMS_PREFIX + ":select":
                   return new alfresco.xforms.Select(this, node);
                 case alfresco_xforms_constants.XFORMS_PREFIX + ":select1":
                   return (this.getBinding(node).getType() == "boolean"
                           ? new alfresco.xforms.Checkbox(this, node)
                           : new alfresco.xforms.Select1(this, node));
                 case alfresco_xforms_constants.XFORMS_PREFIX + ":submit":
                   return new alfresco.xforms.Submit(this, node);
                 case alfresco_xforms_constants.XFORMS_PREFIX + ":trigger":
                   return new alfresco.xforms.Trigger(this, node);
                 case alfresco_xforms_constants.CHIBA_PREFIX + ":data":
                 case alfresco_xforms_constants.XFORMS_PREFIX + ":label":
                 case alfresco_xforms_constants.XFORMS_PREFIX + ":alert":
                   dojo.debug("ignoring " + node.nodeName);
                 return null;
                 default:
                   throw new Error("unknown type " + node.nodeName);
                 }
               },
               loadWidgets: function(xformsNode, parentWidget)
               {
                 for (var i = 0; i < xformsNode.childNodes.length; i++)
                 {
                   if (xformsNode.childNodes[i].nodeType == dojo.dom.ELEMENT_NODE)
                   {
                     dojo.debug("loading " + xformsNode.childNodes[i].nodeName + 
                                " into " + parentWidget.id);
                     if (xformsNode.childNodes[i].getAttribute(alfresco_xforms_constants.ALFRESCO_PREFIX + ":prototype") == "true")
                     {
                       dojo.debug(xformsNode.childNodes[i].getAttribute("id") + 
                                  " is a prototype, ignoring");
                       continue;
                     }
                     var w = this.createWidget(xformsNode.childNodes[i]);
                     if (w != null)
                     {
                       dojo.debug("created " + w.id + " for " + xformsNode.childNodes[i].nodeName);
                       parentWidget.addChild(w);
                       if (w instanceof alfresco.xforms.Group)
                       {
                         this.loadWidgets(xformsNode.childNodes[i], w);
                       }
                     }
                   }
                 }
               },
               getModel: function()
               {
                 return _getElementsByTagNameNS(this.xformsNode, 
                                                alfresco_xforms_constants.XFORMS_NS, 
                                                alfresco_xforms_constants.XFORMS_PREFIX, 
                                                "model")[0];
               },
               getInstance: function()
               {
                 var model = this.getModel();
                 return _getElementsByTagNameNS(model,
                                                alfresco_xforms_constants.XFORMS_NS,
                                                alfresco_xforms_constants.XFORMS_PREFIX,
                                                "instance")[0];
               },
               getBody: function()
               {
                 var b = _getElementsByTagNameNS(this.xformsNode,
                                                 alfresco_xforms_constants.XHTML_NS,
                                                 alfresco_xforms_constants.XHTML_PREFIX,
                                                 "body");
                 return b[b.length - 1];
               },
               getBinding: function(node)
               {
                 return this._bindings[node.getAttribute(alfresco_xforms_constants.XFORMS_PREFIX + ":bind")];
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
                   if (bind.childNodes[i].nodeName.toLowerCase() == alfresco_xforms_constants.XFORMS_PREFIX + ":bind")
                   {
                     var b = new alfresco.xforms.Binding(bind.childNodes[i], parent);
                     result[b.id] = b;
                     dojo.debug("loaded binding " + b);
                     this._loadBindings(bind.childNodes[i], result[b.id], result);
                   }
                 }
                 return result;
               },
               setRepeatIndeces: function(repeatIndeces)
               {
                 dojo.debug("setting repeat indeces [" + repeatIndeces.join(", ") + "]");
                 var params = { };
                 params["repeatIds"] = [];
                 for (var i = 0; i < repeatIndeces.length; i++)
                 {
                   params.repeatIds.push(repeatIndeces[i].repeat.id);
                   params[repeatIndeces[i].repeat.id] = repeatIndeces[i].index + 1;
                 }
                 params.repeatIds = params.repeatIds.join(",");
                 var req = create_ajax_request(this,
                                               "setRepeatIndeces",
                                               params,
                                               function(type, data, evt)
                                               {
                                                 this.target._handleEventLog(data.documentElement);
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
                                                 this.target._handleEventLog(data.documentElement);
                                               });
                 send_ajax_request(req);
               },
               setXFormsValue: function(id, value)
               {
                 value = value == null ? "" : value;
                 dojo.debug("setting value " + id + " = " + value);
                 var req = create_ajax_request(this,
                                               "setXFormsValue",
                                               { id: id, value: value },
                                               function(type, data, evt)
                                               {
                                                 this.target._handleEventLog(data.documentElement);
                                               });
                 send_ajax_request(req);
               },
               _handleEventLog: function(events)
               {
                 var prototypeClone = null;
                 for (var i = 0; i < events.childNodes.length; i++)
                 {
                   if (events.childNodes[i].nodeType != dojo.dom.ELEMENT_NODE)
                   {
                     continue;
                   }
                   var xfe = new alfresco.xforms.XFormsEvent(events.childNodes[i]);
                   dojo.debug("parsing " + xfe.type +
                              "(" + xfe.targetId + ", " + xfe.targetName + ")");
                   switch (xfe.type)
                   {
                   case "chiba-index-changed":
                   {
                     var index = Number(xfe.properties["index"]) - 1;
                     try
                     {
                       xfe.getTarget().handleIndexChanged(index);
                     }
                     catch (e)
                     {
                       dojo.debug(e);
                     }
                     break;
                   }
                   case "chiba-state-changed":
                   {
                     dojo.debug("handleStateChanged(" + xfe.targetId + ")");
                     xfe.getTarget().setModified(true);
                     xfe.getTarget().setValid(xfe.properties["valid"] == "true");
                     xfe.getTarget().setRequired(xfe.properties["required"] == "true");
                     xfe.getTarget().setReadonly(xfe.properties["readonly"] == "true");
                     xfe.getTarget().setEnabled(xfe.properties["enabled"] == "true");
                     if ("value" in xfe.properties)
                     {
                       dojo.debug("setting " + xfe.getTarget().id + " = " + xfe.properties["value"]);
                       xfe.getTarget().setValue(xfe.properties["value"]);
                     }
                     break;
                   }
                   case "chiba-prototype-cloned":
                   {
                     var prototypeId = xfe.properties["prototypeId"];
                     var originalId = xfe.properties["originalId"];
                     dojo.debug("handlePrototypeCloned(" + xfe.targetId + 
                                ", " + originalId + 
                                ", " + prototypeId + ")");
                     var clone = null;
                     var prototypeNode = _findElementById(this.xformsNode, prototypeId);
                     if (prototypeNode)
                     {
                       dojo.debug("cloning prototype " + prototypeNode.getAttribute("id"));
                       clone = prototypeNode.cloneNode(true);
                     }
                     else
                     {
                       dojo.debug("cloning prototype " + originalId);
                       var prototypeNode = _findElementById(this.xformsNode, originalId);
                       clone = prototypeNode.cloneNode(true);
                       var clone = prototypeNode.ownerDocument.createElement(alfresco_xforms_constants.XFORMS_PREFIX + ":group");
                       clone.setAttribute(alfresco_xforms_constants.XFORMS_PREFIX + ":appearance", "repeated");
                       for (var j = 0; j < prototypeNode.childNodes.length; j++)
                       {
                         clone.appendChild(prototypeNode.childNodes[j].cloneNode(true));
                       }
                       clone.setAttribute("id", prototypeId);
                     }
                     clone.parentClone = prototypeClone;
                     prototypeClone = clone;
                     break;
                   }
                   case "chiba-id-generated":
                   {
                     var originalId = xfe.properties["originalId"];
               
                     dojo.debug("handleIdGenerated(" + xfe.targetId + ", " + originalId + ")");
                     var node = _findElementById(prototypeClone, originalId);
                     if (!node)
                       throw new Error("unable to find " + originalId + 
                                       " in clone " + dojo.dom.innerXML(clone));
                     dojo.debug("applying id " + xfe.targetId + 
                                " to " + node.nodeName + "(" + originalId + ")");
                     node.setAttribute("id", xfe.targetId);
                     if (prototypeClone.parentClone)
                     {
                       var e = _findElementById(prototypeClone.parentClone, originalId);
                       if (e)
                       {
                         e.setAttribute(alfresco_xforms_constants.ALFRESCO_PREFIX + ":prototype", "true");
                       }
                     }
                     break;
                   }
                   case "chiba-item-inserted":
                   {
                     var position = Number(xfe.properties["position"]) - 1;
                     var originalId = xfe.properties["originalId"];
                     var clone = prototypeClone;
                     prototypeClone = clone.parentClone;
                     if (prototypeClone)
                     {
                       var parentRepeat = _findElementById(prototypeClone, xfe.targetId);
                       parentRepeat.appendChild(clone);
                     }
                     else
                     {
                       xfe.getTarget().handleItemInserted(clone, position);
                     }
                     break;
                   }
                   case "chiba-item-deleted":
                   {
                     var position = Number(xfe.properties["position"]) - 1;
                     xfe.getTarget().handleItemDeleted(position);
                     break;
                   }
                   case "chiba-replace-all":
                   {
                     if (this.submitWidget)
                     {
                       this.submitWidget.done = true;
                       this.submitWidget.currentButton.click();
                     }
                     break;
                   }
                   case "xforms-valid":
                   {
                     xfe.getTarget().setValid(true);
                     xfe.getTarget().setModified(true);
                     break;
                   }
                   case "xforms-invalid":
                   {
                     xfe.getTarget().setValid(false);
                     xfe.getTarget().setModified(true);
                     break;
                   }
                   case "xforms-required":
                   {
                     xfe.getTarget().setRequired(true);
                     break;
                   }
                   case "xforms-optional":
                   {
                     xfe.getTarget().setRequired(false);
                     break;
                   }
                   case "xforms-submit-error":
                   {
                     var invalid = this.rootWidget.getWidgetsInvalidForSubmit();
                     _show_error(document.createTextNode("Please provide values for all required fields."));
                     var error_list = document.createElement("ul");
                     for (var j = 0; j < invalid.length; j++)
                     {
                       var error_item = document.createElement("li");
                       error_item.appendChild(document.createTextNode(invalid[j].getAlert()));
                       error_list.appendChild(error_item);
                       invalid[j].showAlert();
                     }
                     _show_error(error_list);
                     break;
                   }
                   default:
                   {
                     dojo.debug("unhandled event " + events.childNodes[i].nodeName);
                   }
                   }
                 }
               }
             });

function _findElementById(node, id)
{
//  dojo.debug("looking for " + id + 
//             " in " + (node ? node.nodeName : null) + 
//             "(" + (node ? node.getAttribute("id") : null) + ")");
  if (node.getAttribute("id") == id)
  {
    return node;
  }
  for (var i = 0; i < node.childNodes.length; i++)
  {
    if (node.childNodes[i].nodeType == dojo.dom.ELEMENT_NODE)
    {
      var n = _findElementById(node.childNodes[i], id);
      if (n)
      {
        return n;
      }
    }
  }
  return null;
}

function create_ajax_request(target, serverMethod, methodArgs, load, error)
{
  var result = new dojo.io.Request(alfresco_xforms_constants.WEBAPP_CONTEXT + 
                                   "/ajax/invoke/XFormsBean." + serverMethod, 
                                   "text/xml");
  result.target = target;
  result.content = methodArgs;

  result.load = load;
  dojo.event.connect(result, "load", function(type, data, evt)
                     {
                       ajax_request_load_handler(result);
                     });
  result.error = error || function(type, e)
    {
      dojo.debug("error [" + type + "] " + e.message);
      _show_error(document.createTextNode(e.message));
      ajax_request_load_handler(this);
    };
  return result;
}

function _hide_errors()
{
  var errorDiv = document.getElementById(alfresco_xforms_constants.XFORMS_ERROR_DIV_ID);
  if (errorDiv)
  {
    dojo.dom.removeChildren(errorDiv);
    errorDiv.style.display = "none";
  }
}

function _show_error(msg)
{
  var errorDiv = document.getElementById(alfresco_xforms_constants.XFORMS_ERROR_DIV_ID);
  if (!errorDiv)
  {
    errorDiv = document.createElement("div");
    errorDiv.setAttribute("id", alfresco_xforms_constants.XFORMS_ERROR_DIV_ID);
    dojo.html.setClass(errorDiv, "infoText statusErrorText xformsError");
    var alfUI = document.getElementById(alfresco_xforms_constants.XFORMS_UI_DIV_ID);
    dojo.dom.prependChild(errorDiv, alfUI);
  }

  if (errorDiv.style.display == "block")
  {
    errorDiv.appendChild(document.createElement("br"));
  }
  else
  {
    errorDiv.style.display = "block";
  }
  errorDiv.appendChild(msg);
}

function send_ajax_request(req)
{
  ajax_request_send_handler(req);
  dojo.io.queueBind(req);
}

function _get_ajax_loader_element()
{
  var result = document.getElementById(alfresco_xforms_constants.AJAX_LOADER_DIV_ID);
  if (result)
    return result;
  result = document.createElement("div");
  result.setAttribute("id", alfresco_xforms_constants.AJAX_LOADER_DIV_ID);
  dojo.html.setClass(result, "xformsAjaxLoader");
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
  if (/* djConfig.isDebug && */ _ajax_requests.length != 0)
  {
    dojo.style.show(ajaxLoader);
  }
  else
  {
    dojo.style.hide(ajaxLoader);
  }
}

function ajax_request_load_handler(req)
{
  var index = _ajax_requests.indexOf(req);
  if (index != -1)
    _ajax_requests.splice(index, 1);
  else
  {
    var urls = [];
    for (var i = 0; i < _ajax_requests.length; i++)
    {
      urls.push(_ajax_requests[i].url);
    }
    throw new Error("unable to find " + req.url + 
                    " in [" + urls.join(", ") + "]");
  }
  ajax_loader_update_display();
}

function _getElementsByTagNameNS(parentNode, ns, nsPrefix, tagName)
{
  return (parentNode.getElementsByTagNameNS
          ? parentNode.getElementsByTagNameNS(ns, tagName)
          : parentNode.getElementsByTagName(nsPrefix + ":" + tagName));
}

function _evaluateXPath(xpath, contextNode, result_type)
{
  var xmlDocument = contextNode.ownerDocument;
  dojo.debug("evaluating xpath " + xpath +
             " on node " + contextNode.nodeName +
             " in document " + xmlDocument);
  var result = null;
  if (xmlDocument.evaluate)
  {
    var nsResolver = (xmlDocument.createNSResolver 
                      ? xmlDocument.createNSResolver(xmlDocument.documentElement) :
                      null);
    result = xmlDocument.evaluate(xpath, 
                                  contextNode, 
                                  nsResolver, 
                                  result_type,
                                  null);
    if (result)
    {
      switch (result_type)
      {
      case XPathResult.FIRST_ORDERED_NODE_TYPE:
        result = result.singleNodeValue;
        break;
      case XPathResult.BOOLEAN_TYPE:
        result = result.booleanValue;
        break;
      case XPathResult.STRING_TYPE:
        result = result.stringValue;
        break;
      }
    }
  }
  else
  {
    xmlDocument.setProperty("SelectionLanguage", "XPath");
    var namespaces = [];
    for (var i = 0; i < xmlDocument.documentElement.attributes.length; i++)
    {
      var attr = xmlDocument.documentElement.attributes[i];
      if (attr.nodeName.match(/^xmlns:/))
        namespaces.push(attr.nodeName + "=\'" + attr.nodeValue + "\'");
    }
    dojo.debug("using namespaces " + namespaces.join(","));
    xmlDocument.setProperty("SelectionNamespaces", namespaces.join(' '));
    if (result_type == XPathResult.FIRST_ORDERED_NODE_TYPE)
      result = xmlDocument.selectSingleNode(xpath);
    else if (result_type == XPathResult.BOOLEAN_TYPE)
      result = true;
  }
  dojo.debug("resolved xpath " + xpath + " to " + result);
  return result;
}

if (!XPathResult)
{
  var XPathResult = {};
  XPathResult.ANY_TYPE = 0;
  XPathResult.NUMBER_TYPE = 1;
  XPathResult.STRING_TYPE = 2;
  XPathResult.BOOEAN_TYPE = 3;
  XPathResult.FIRST_ORDERED_NODE_TYPE = 9;
}

if (!Array.prototype.indexOf)
{
  Array.prototype.indexOf = function(o)
  {
    for (var i = 0; i < this.length; i++)
    {
      if (this[i] == o)
        return i;
    }
    return -1;
  }
}

function FilePickerWidget(node, value, readonly, change_callback, resize_callback)
{
  this.node = node;
  this.value = value == null || value.length == 0 ? null : value;
  this.readonly =  readonly || false;
  this.change_callback = change_callback;
  this.resize_callback = resize_callback;
}

FilePickerWidget._uploads = [];
FilePickerWidget._handleUpload = function(id, fileInput, webappRelativePath, widget)
{
  id = id.substring(0, id.indexOf("-widget"));
  var d = fileInput.ownerDocument;
  var iframe = d.createElement("iframe");
  iframe.style.display = "none";
  iframe.name = id + "_upload_frame";
  iframe.id = iframe.name;
  document.body.appendChild(iframe);
  // makes it possible to target the frame properly in ie.
  window.frames[id + "_upload_frame"].name = iframe.name;

  FilePickerWidget._uploads[id] = 
  {
    widget:widget, 
    path: fileInput.value, 
    webappRelativePath: webappRelativePath,
    fileName: fileInput.value.substring(fileInput.value.lastIndexOf("/") + 1)
  };

  var form = document.createElement("form");
  form.style.display = "none";
  d.body.appendChild(form);
  form.id = id + "_upload_form";
  form.name = form.id;
  form.method = "post";
  form.encoding = "multipart/form-data";
  form.enctype = "multipart/form-data";
  form.target = iframe.name;
  form.action = alfresco_xforms_constants.WEBAPP_CONTEXT + "/ajax/invoke/XFormsBean.uploadFile";
  form.appendChild(fileInput.cloneNode(true));

  var rp = d.createElement("input");
  rp.type = "hidden";
  rp.name = "id";
  rp.value = id;
  form.appendChild(rp);

  var rp = d.createElement("input");
  rp.name = "currentPath";
  rp.value = webappRelativePath;
  rp.type = "hidden";
  form.appendChild(rp);

  form.submit();
}

FilePickerWidget._upload_completeHandler = function(id)
{
  var upload = FilePickerWidget._uploads[id];
  upload.widget._upload_completeHandler(upload.fileName, 
                                        upload.webappRelativePath);
}

FilePickerWidget.prototype = {
getValue: function()
{
  return this.value;
},
setValue: function(v)
{
  this.value = (v == null || v.length == 0 ? null : v);
  if (this.selectedPathInput)
  {
    this.selectedPathInput.value = v;
  }

  this.change_callback(this);
},
setReadonly: function(r)
{
  this.readonly = r;
  if (this.selectButton)
  {
    this.selectButton.disabled = this.readonly;
  }
  else if (this.readonly)
  {
    this._showSelectedValue();
  }
},
render: function()
{
  this._showSelectedValue();
},
_showStatus: function(text)
{
  var d = this.node.ownerDocument;
  if (!this.statusDiv || !this.statusDiv.parentNode)
  {
    this.statusDiv = d.createElement("div");
    this.node.insertBefore(this.statusDiv, this.node.firstChild);
    dojo.html.setClass(this.statusDiv, "infoText xformsFilePickerStatus");
    this.statusDiv.appendChild(d.createTextNode(text));
    this.node.style.height = (parseInt(this.node.style.height) +
                              dojo.style.getMarginHeight(this.statusDiv) +
                              this.statusDiv.offsetHeight) + "px";
    this.resize_callback(this);
  }
  else
  {
    this.statusDiv.firstChild.nodeValue = text;
  }
},
_hideStatus: function()
{
  if (this.statusDiv)
  {
    this.node.style.height = (parseInt(this.node.style.height) -
                              this.statusDiv.offsetHeight) + "px";
    dojo.dom.removeChildren(this.statusDiv);
    dojo.dom.removeNode(this.statusDiv);
    this.resize_callback(this);
  }
},
_showSelectedValue: function()
{
  var d = this.node.ownerDocument;
  dojo.dom.removeChildren(this.node);

  this.node.style.height = "20px";
  this.node.style.lineHeight = this.node.style.height;
  this.node.style.position = "relative";
  this.node.style.whiteSpace = "nowrap";

  this.resize_callback(this);

  this.selectedPathInput = d.createElement("input");
  this.selectedPathInput.type = "text";
  this.selectedPathInput.value = this.value == null ? "" : this.value;
  this.node.appendChild(this.selectedPathInput);

  dojo.event.connect(this.selectedPathInput, "onblur", this, this._selectPathInput_changeHandler);

  this.selectButton = d.createElement("input");
  this.selectButton.filePickerWidget = this;
  this.selectButton.type = "button";
  this.selectButton.value = this.value == null ? "Select" : "Change";
  this.selectButton.disabled = this.readonly;
  this.selectButton.style.margin = "0px 10px 0px 10px";
  this.node.appendChild(this.selectButton);

  this.selectedPathInput.style.width = (1 -
                                        ((this.selectButton.offsetWidth +
                                          dojo.style.getMarginWidth(this.selectButton)) /
                                         dojo.style.getContentBoxWidth(this.node))) * 100 + "%";
  dojo.event.browser.addListener(this.selectButton, 
                                 "click", 
                                 function(event)
                                 {
                                   var w = event.target.filePickerWidget;
                                   w._navigateToNode(w.getValue() || "");
                                 });

},
_selectPathInput_changeHandler: function(event)
{
  this.setValue(event.target.value);
},
_navigateToNode: function(path)
{
  var req = create_ajax_request(this,
                                "getFilePickerData",
                                {},
                                function(type, data, evt)
                                {
                                  this.target._showPicker(data.documentElement);
                                });
  req.content.currentPath = path;
  send_ajax_request(req);
},
_showPicker: function(data)
{
  while (this.node.hasChildNodes() &&
         this.node.lastChild != this.statusDiv)
  {
    this.node.removeChild(this.node.lastChild);
  }

  var d = this.node.ownerDocument;
  this.node.style.height = (200 +
                            (this.statusDiv 
                             ? (parseInt(this.statusDiv.style.height) +
                                parseInt(this.statusDiv.style.marginTop) +
                                parseInt(this.statusDiv.style.marginBottom))
                             : 0) + "px");
  this.resize_callback(this);

  var currentPath = data.getElementsByTagName("current-node")[0];
  currentPath = currentPath.getAttribute("webappRelativePath");
  var currentPathName = currentPath.replace(/.*\/([^/]+)/, "$1")

  var headerDiv = d.createElement("div");
  dojo.html.setClass(headerDiv, "xformsFilePickerHeader");
  this.node.appendChild(headerDiv);
  headerDiv.appendChild(d.createTextNode("In: "));
  this.headerMenuTriggerLink = d.createElement("a");
  this.headerMenuTriggerLink.filePickerWidget = this;
  this.headerMenuTriggerLink.style.textDecoration = "none";
  this.headerMenuTriggerLink.setAttribute("href", "javascript:void(0)");
  this.headerMenuTriggerLink.setAttribute("webappRelativePath", currentPath);
  dojo.html.setClass(this.headerMenuTriggerLink, "xformsFilePickerHeaderMenuTrigger");
  headerDiv.appendChild(this.headerMenuTriggerLink);

  dojo.event.connect(this.headerMenuTriggerLink,
                     "onmouseover",
                     function(event)
                     {
                       event.currentTarget.style.backgroundColor = "#fefefe";
                       event.currentTarget.style.borderStyle = "inset";
                     });
  dojo.event.connect(this.headerMenuTriggerLink,
                     "onmouseout",
                     function(event)
                     {
                       var w = event.currentTarget.filePickerWidget;
                       if (!w.parentPathMenu)
                       {
                         event.currentTarget.style.backgroundColor = 
                           event.currentTarget.parentNode.style.backgroundColor;
                         event.currentTarget.style.borderStyle = "solid";
                       }
                     });
  dojo.event.connect(this.headerMenuTriggerLink,
                     "onclick",
                     function(event)
                     {
                       var t = event.currentTarget;
                       var w = t.filePickerWidget;
                       if (w.parentPathMenu)
                       {
                         w._closeParentPathMenu();
                       }
                       else
                       {
                         w._openParentPathMenu(t, t.getAttribute("webappRelativePath"));
                       }
                     });

  this.headerMenuTriggerLink.appendChild(d.createTextNode(currentPathName));

  headerMenuTriggerImage = d.createElement("img");
  this.headerMenuTriggerLink.appendChild(headerMenuTriggerImage);
  this.headerMenuTriggerLink.image = headerMenuTriggerImage;
  headerMenuTriggerImage.setAttribute("src", alfresco_xforms_constants.WEBAPP_CONTEXT + "/images/icons/menu.gif");
  headerMenuTriggerImage.style.borderWidth = "0px";
  headerMenuTriggerImage.style.marginLeft = "4px";
  headerMenuTriggerImage.align = "absmiddle";

  var headerRightDiv = d.createElement("div");

  var addContentLink = d.createElement("a");
  headerRightDiv.appendChild(addContentLink);
  addContentLink.setAttribute("webappRelativePath", currentPath);
  addContentLink.filePickerWidget = this;
  addContentLink.setAttribute("href", "javascript:void(0)");
  dojo.event.connect(addContentLink, 
                     "onclick", 
                     function(event)
                     {
                       var t = event.target;
                       t.filePickerWidget._showAddContentPanel(t, t.getAttribute("webappRelativePath"));
                     });

  var addContentImage = d.createElement("img");
  addContentImage.style.borderWidth = "0px";
  addContentImage.style.margin = "0px 2px 0px 2px";
  addContentImage.align = "absmiddle";
  addContentImage.setAttribute("src", alfresco_xforms_constants.WEBAPP_CONTEXT + "/images/icons/add.gif");
  addContentLink.appendChild(addContentImage);
  addContentLink.appendChild(d.createTextNode("Add Content"));

  var navigateToParentLink = d.createElement("a");
  headerRightDiv.appendChild(navigateToParentLink);
  navigateToParentLink.setAttribute("webappRelativePath", currentPath);
  navigateToParentLink.filePickerWidget = this;
  navigateToParentLink.setAttribute("href", "javascript:void(0)");
  if (currentPathName != "/")
  {
    dojo.event.connect(navigateToParentLink, 
                       "onclick", 
                       function(event)
                       {
                         var w = event.target.filePickerWidget;
                         var parentPath = event.target.getAttribute("webappRelativePath");
                         parentPath = (parentPath.lastIndexOf("/") == 0 
                                       ? "/" 
                                       : parentPath.substring(0, parentPath.lastIndexOf("/")));
                         w._navigateToNode(parentPath);
                       });
  }

  var navigateToParentNodeImage = d.createElement("img");
  navigateToParentNodeImage.style.borderWidth = "0px";
  navigateToParentNodeImage.style.opacity =  (currentPathName == "/" ? .3 : 1);
  navigateToParentNodeImage.style.margin = "0px 2px 0px 2px";
  navigateToParentNodeImage.align = "absmiddle";
  navigateToParentNodeImage.setAttribute("src", alfresco_xforms_constants.WEBAPP_CONTEXT + "/images/icons/up.gif");
  navigateToParentLink.appendChild(navigateToParentNodeImage);
  navigateToParentLink.appendChild(d.createTextNode("Go up"));

  headerRightDiv.style.position = "absolute";
  headerRightDiv.style.height = headerDiv.style.height;
  headerRightDiv.style.lineHeight = headerRightDiv.style.height;
  headerRightDiv.style.top = "0px";
  headerRightDiv.style.right = "0px";
  headerRightDiv.style.paddingRight = "2px";
  headerDiv.appendChild(headerRightDiv);

  this.contentDiv = d.createElement("div");
  dojo.html.setClass(this.contentDiv, "xformsFilePickerFileList");
  this.node.appendChild(this.contentDiv);

  var footerDiv = d.createElement("div");
  dojo.html.setClass(footerDiv, "xformsFilePickerFooter");
  this.node.appendChild(footerDiv);

  var cancelButton = d.createElement("input");
  cancelButton.type = "button";
  cancelButton.filePickerWidget = this;
  cancelButton.value = "Cancel";
  footerDiv.appendChild(cancelButton);

  cancelButton.style.margin = ((.5 * footerDiv.offsetHeight) - 
                               (.5 * cancelButton.offsetHeight)) + "px 0px";
  dojo.event.connect(cancelButton, "onclick", function(event)
                     {
                       var w = event.target.filePickerWidget;
                       w._showSelectedValue();
                     });

  this.contentDiv.style.height = (this.node.offsetHeight -
                                  (this.statusDiv ? this.statusDiv.offsetHeight : 0) -
                                  footerDiv.offsetHeight -
                                  headerDiv.offsetHeight - 10) + "px";
//  this.contentDiv.style.overflowY = "auto";
  var childNodes = data.getElementsByTagName("child-node");
  for (var i = 0; i < childNodes.length; i++)
  {
    if (childNodes[i].nodeType != dojo.dom.ELEMENT_NODE)
    {
      continue;
    }
    var path = childNodes[i].getAttribute("webappRelativePath");
    var name = path.replace(/.*\/([^/]+)/, "$1");

    var row = d.createElement("div");
    row.setAttribute("id", name + "-row");
    this.contentDiv.appendChild(row);
    row.rowIndex = i;
    dojo.html.setClass(row, "xformsFilePickerRow xformsRow" + (row.rowIndex % 2 ? "Even" : "Odd"));
    dojo.event.browser.addListener(row,
                                   "mouseover", 
                                   function(event)
                                   {
                                     var prevHover = event.currentTarget.parentNode.hoverNode;
                                     if (prevHover)
                                     {
                                       dojo.html.removeClass(prevHover, "xformsRowHover");
                                     }
                                     event.currentTarget.parentNode.hoverNode = event.currentTarget;
                                     dojo.html.addClass(event.currentTarget, "xformsRowHover")
                                   },
                                   true);
    dojo.event.browser.addListener(row,
                                   "mouseout", 
                                   function(event)
                                   {
                                     if (event.relatedTarget &&
                                         event.relatedTarget.parentNode == event.currentTarget)
                                     {
                                       return true;
                                     }
                                     dojo.html.removeClass(event.currentTarget, "xformsRowHover");
                                   },
                                   true);
    var e = d.createElement("img");
    e.align = "absmiddle";
    e.style.margin = "0px 4px 0px 4px";
    e.setAttribute("src", alfresco_xforms_constants.WEBAPP_CONTEXT + childNodes[i].getAttribute("image"));
    row.appendChild(e);

    if (childNodes[i].getAttribute("type") == "directory")
    {
      e = d.createElement("a");
      e.filePickerWidget = this;
      e.setAttribute("href", "javascript:void(0)");
      e.setAttribute("webappRelativePath", path); 
      dojo.event.connect(e, "onclick", function(event)
                         {
                           var w = event.target.filePickerWidget;
                           w._navigateToNode(event.target.getAttribute("webappRelativePath"));
                           return true;
                         });
      e.appendChild(d.createTextNode(name));
      row.appendChild(e);
    }
    else
    {
      row.appendChild(d.createTextNode(name));
    }

    e = d.createElement("input");
    e.filePickerWidget = this;
    e.type = "button";
    e.name = path;
    e.value = "Select";
    row.appendChild(e);
    
    e.style.position = "absolute";
    e.style.right = "10px";
    e.style.top = (.5 * row.offsetHeight) - (.5 * e.offsetHeight) + "px";
    dojo.event.connect(e, "onclick", function(event)
                       {
                         var w = event.target.filePickerWidget;
                         w.setValue(event.target.name);
                         w._showSelectedValue();
                       });
  }
},
_showAddContentPanel: function(addContentLink, currentPath)
{
  var d = this.node.ownerDocument;
  this.addContentDiv = d.createElement("div");
  dojo.html.setClass(this.addContentDiv, "xformsFilePickerAddContent");

  if (this.contentDiv.firstChild)
  {
    this.contentDiv.insertBefore(this.addContentDiv, this.contentDiv.firstChild);
  }
  else
  {
    this.contentDiv.appendChild(this.addContentDiv);
  }
  var e = d.createElement("div");
  e.style.marginLeft = "4px";
  this.addContentDiv.appendChild(e);
  e.appendChild(d.createTextNode("Upload: "));

  var fileInputDiv = d.createElement("div");
  this.addContentDiv.appendChild(fileInputDiv);
  var fileInput = d.createElement("input");
  fileInput.type = "file";
  fileInput.widget = this;
  fileInput.name = this.node.getAttribute("id") + "_file_input";
  fileInput.size = "35";
  fileInput.setAttribute("webappRelativePath", currentPath);
  fileInputDiv.appendChild(fileInput);
  fileInputDiv.style.position = "absolute";
  fileInputDiv.style.right = "10px";
  fileInputDiv.style.top = (.5 * this.addContentDiv.offsetHeight) - (.5 * fileInputDiv.offsetHeight) + "px";

  dojo.event.connect(fileInput, 
                     "onchange", 
                     function(event)
                     {
                       var w = event.target.widget;
                       FilePickerWidget._handleUpload(w.node.getAttribute("id"), 
                                                      event.target,
                                                      event.target.getAttribute("webappRelativePath"),
                                                      w);
                       if (w.addContentDiv)
                       {
                         dojo.dom.removeChildren(w.addContentDiv);
                         dojo.dom.removeNode(w.addContentDiv);
                         w.addContentDiv = null;
                       }
                     });
},
_upload_completeHandler: function(fileName, webappRelativePath)
{
  this._showStatus("Successfully uploaded " + fileName + "."); // " into " + webappRelativePath);
  this._navigateToNode(webappRelativePath);
},
_closeParentPathMenu: function()
{
  if (this.parentPathMenu)
  {
    dojo.dom.removeChildren(this.parentPathMenu);
    dojo.dom.removeNode(this.parentPathMenu);
    this.parentPathMenu = null;
  }
  this.headerMenuTriggerLink.style.borderStyle = "solid";
},
_openParentPathMenu: function(target, path)
{
  var d = target.ownerDocument;
  this.parentPathMenu = d.createElement("div");
  this.parentPathMenu.filePickerWidget = this;
  d.currentParentPathMenu = this.parentPathMenu;

  // handler for closing the menu if the mouse is clicked
  // outside of the menu
  var parentPathMenu_documentClickHandler = function(event)
  {
    var t = event.target;
    var d = event.target.ownerDocument;

    // always remove - this handler only ever needs to handle a single click
    d.removeEventListener("click", parentPathMenu_documentClickHandler, true);
    while (t && t != d)
    {
      if (t == d.currentParentPathMenu ||
          t == d.currentParentPathMenu.filePickerWidget.headerMenuTriggerLink)
      {
        // the click is coming from within the component - ignore it
        return true;
      }
      t = t.parentNode;
    }
    d.currentParentPathMenu.filePickerWidget._closeParentPathMenu();
  };
  d.addEventListener("click", parentPathMenu_documentClickHandler, true);

  dojo.html.setClass(this.parentPathMenu, "xformsFilePickerParentPathMenu");

  var left = 0;
  var top = 0;
  var n = target;
  do
  {
    left += n.offsetLeft;// + parseInt(n.style.marginLeft) + parseInt(n.style.borderLeft);
    top += n.offsetTop;// + parseInt(n.style.marginTop) + parseInt(n.style.borderTop);
    n = n.parentNode;
  }
  while (n != this.node);
  this.parentPathMenu.style.top = top + target.offsetHeight + "px";
  this.parentPathMenu.style.left = left + "px";
  var parentNodes = null;
  if (path == "/")
  {
    parentNodes = [ "/" ];
  }
  else
  {
    parentNodes = path.split("/");
    parentNodes[0] = "/";
  }
  
  var pathTextDiv = d.createElement("div");
  pathTextDiv.style.fontWeight = "bold";
  pathTextDiv.style.paddingLeft = "5px";
  pathTextDiv.appendChild(d.createTextNode("Path"));
  this.parentPathMenu.appendChild(pathTextDiv);
  var currentPathNodes = [];
  for (var i = 0; i < parentNodes.length; i++)
  {
    if (i != 0)
    {
      currentPathNodes.push(parentNodes[i]);
    }
    var path = i == 0 ? "/" : "/" + currentPathNodes.join("/");
    var parentNodeDiv = d.createElement("div");
    parentNodeDiv.setAttribute("webappRelativePath", path);
    this.parentPathMenu.appendChild(parentNodeDiv);
    parentNodeDiv.style.display = "block";
    parentNodeDiv.style.paddingLeft = (i * 16) + parseInt(pathTextDiv.style.paddingLeft) + "px";
    parentNodeDiv.style.paddingRight = parseInt(pathTextDiv.style.paddingLeft) + "px";
    parentNodeDiv.style.whiteSpace = "nowrap";

    var parentNodeImage = d.createElement("img");
    parentNodeImage.align = "absmiddle";
    parentNodeImage.style.marginRight = "4px";
    parentNodeDiv.appendChild(parentNodeImage);
    parentNodeImage.setAttribute("src", alfresco_xforms_constants.WEBAPP_CONTEXT + "/images/icons/space_small.gif");
    parentNodeDiv.appendChild(parentNodeImage);
    parentNodeDiv.appendChild(d.createTextNode(path));
    dojo.event.connect(parentNodeDiv,
                       "onclick",
                       function(event)
                       {
                         var w = event.currentTarget;
                         var path = w.getAttribute("webappRelativePath");
                         w = w.parentNode;
                         w.filePickerWidget._closeParentPathMenu();
                         w.filePickerWidget._navigateToNode(path);
                       });
  }
  this.node.appendChild(this.parentPathMenu);
}
};
