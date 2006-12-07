dojo.require("dojo.widget.DebugConsole");
dojo.require("dojo.widget.DatePicker");
dojo.require("dojo.widget.Button");
dojo.require("dojo.lfx.html");
dojo.hostenv.writeIncludes();

var XFORMS_NS = "http://www.w3.org/2002/xforms";
var XFORMS_NS_PREFIX = "xforms";
var XHTML_NS = "http://www.w3.org/1999/xhtml";
var XHTML_NS_PREFIX = "xhtml";
var CHIBA_NS = "http://chiba.sourceforge.net/xforms";
var CHIBA_NS_PREFIX = "chiba";
var ALFRESCO_NS = "http://www.alfresco.org/alfresco";
var ALFRESCO_NS_PREFIX = "alfresco";

var EXPANDED_IMAGE = new Image();
EXPANDED_IMAGE.src = WEBAPP_CONTEXT + "/images/icons/expanded.gif";
var COLLAPSED_IMAGE = new Image();
COLLAPSED_IMAGE.src = WEBAPP_CONTEXT + "/images/icons/collapsed.gif";

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
                 //XXXarielb this has to come back
                 //     this.node.widget = this;
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
               },
               xformsNode: null,
               labelNode: null,
               parent: null,
               domNode: null,
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
                 var chibaData = _getElementsByTagNameNS(this.xformsNode, 
                                                         CHIBA_NS, 
                                                         CHIBA_NS_PREFIX, 
                                                         "data");
                 if (chibaData.length == 0)
                   return null;
             
                 chibaData = chibaData[chibaData.length - 1];
                 var xpath = chibaData.getAttribute(CHIBA_NS_PREFIX + ":xpath");
                 var d = this.xformsNode.ownerDocument;
                 var contextNode = this.xform.getInstance();
                 dojo.debug("locating " + xpath + 
                            " from " + chibaData.nodeName + 
                            " in " + contextNode.nodeName);
                 var result = _evaluateXPath("/" + xpath, 
                                             this.xform.getInstance(), 
                                             XPathResult.FIRST_ORDERED_NODE_TYPE);
                 if (!result)
                   throw new Error("unable to resolve xpath  /" + xpath + " for " + this.id);
                 result = (result.nodeType == dojo.dom.ELEMENT_NODE
                           ? dojo.dom.textContent(result)
                           : result.nodeValue);
                 dojo.debug("resolved xpath " + xpath + " to " + result);
                 return result;
               },
               _getLabelNode: function()
               {
                 var labels = _getElementsByTagNameNS(this.xformsNode, XFORMS_NS, XFORMS_NS_PREFIX, "label");
                 for (var i = 0; i < labels.length; i++)
                 {
                   if (labels[i].parentNode == this.xformsNode)
                     return labels[i];
                 }
                 return null;
               },
               _getAlertNode: function()
               {
                 var labels = _getElementsByTagNameNS(this.xformsNode, XFORMS_NS, XFORMS_NS_PREFIX, "alert");
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
               getParentRepeats: function()
               {
                 var result = [];
                 var p = this.parent;
                 while (p)
                 {
                   if (p instanceof alfresco.xforms.Repeat)
                   {
                     result.push(p);
                   }
                   p = p.parent;
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
                 this.domNode = document.createElement("div");
                 this.domNode.setAttribute("id", this.id + "-widget");
                 this.domNode.style.width = "100%";
                 this.domNode.widget = this;
                 this.domNode.addEventListener("heightChanged", 
                                               function(event) 
                                               { 
                                                 this.widget.domContainer.style.height = 
                                                   event.target.offsetHeight + "px";
                                               }, 
                                               false);
                 attach_point.appendChild(this.domNode);
                 //XXXarielb support readonly and disabled
                 this.widget = new FilePickerWidget(this.domNode, this.getInitialValue(), false);
                 this.widget.render();
                 this.domNode.addEventListener("valueChanged",
                                               function(event)
                                               {
                                                 var w = event.target.widget;
                                                 w.xform.setXFormsValue(w.id, w.getValue());
                                               },
                                               false);
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
               _filePicker_changeHandler: function(event)
               {
                 this.xform.setXFormsValue(this.id, this.getValue());
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
               
                 this.domNode = document.createElement("div");
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
                 this.domNode = document.createElement("div");
                 attach_point.appendChild(this.domNode);
               
                 this.widget = document.createElement("input");
                 this.widget.setAttribute("type", "text");
                 this.widget.setAttribute("id", this.id + "-widget");
                 this.widget.setAttribute("value", initial_value);
                 if (this.xform.getType(this.xformsNode) == "string")
                   this.widget.style.width = "100%";

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
                 this.widget.setAttribute("value", value);
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
//           this.inherited("initializer", [ xform, xformsNode ]);
               },
               render: function(attach_point)
               {
                 this.domNode = document.createElement("div");
                 attach_point.appendChild(this.domNode);
                 this.domNode.setAttribute("id", this.id);
                 this.domNode.style.height = "200px";
                 this.domNode.innerHTML = this.getInitialValue() || "";
                 tinyMCE.addMCEControl(this.domNode, this.id);

                 var editorDocument = tinyMCE.getInstanceById(this.id).getDoc();
                 editorDocument.widget = this;
                 tinyMCE.addEvent(editorDocument, "blur", this._tinyMCE_blurHandler);
                 this.widget = this.domNode;
               },
               setValue: function(value)
               {
                 tinyMCE.selectedInstance = tinyMCE.getInstanceById(this.id);
                 tinyMCE.setContent(value);
               },
               getValue: function()
               {
                 return tinyMCE.getContent(this.id);
               },
               _tinyMCE_blurHandler: function(event)
               {
                 var widget = event.target.widget;
                 widget.xform.setXFormsValue(widget.id, widget.getValue());
               },
               _destroy: function()
               {
                 this.inherited("_destroy", []);
                 dojo.debug("removing mce control " + this.id);
                 tinyMCE.removeMCEControl(this.id);
               }
             });

dojo.declare("alfresco.xforms.AbstractSelectWidget",
             alfresco.xforms.Widget,
             {
               initializer: function(xform, xformsNode) 
               {
//           this.inherited("initializer", [ xform, xformsNode ]);
               },
               getValues: function()
               {
                 var binding = this.xform.getBinding(this.xformsNode);
                 var values = _getElementsByTagNameNS(this.xformsNode, XFORMS_NS, XFORMS_NS_PREFIX, "item");
                 var result = [];
                 for (var v = 0; v < values.length; v++)
                 {
                   var label = _getElementsByTagNameNS(values[v], XFORMS_NS, XFORMS_NS_PREFIX, "label")[0];
                   var value = _getElementsByTagNameNS(values[v], XFORMS_NS, XFORMS_NS_PREFIX, "value")[0];
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
                   for (var i = 0; i < values.length; i++)
                   {
                     this.widget = document.createElement("span");
                     attach_point.appendChild(this.widget);
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
                     dojo.event.connect(checkbox, "onclick", this, this._checkbox_clickHandler);
                     this.widget.appendChild(checkbox);
                     this.widget.appendChild(document.createTextNode(values[i].label));
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
//           this.inherited("initializer", [ xform, xformsNode ]);
                 this.children = [];
                 this.domNode = document.createElement("div");
                 this.domNode.setAttribute("id", this.id + "-domNode");
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
                     return i;
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
//           child.domContainer.style.width = "100%";
                 if (this.parent && this.parent.domNode)
                   child.domContainer.style.top = this.parent.domNode.style.bottom;
         
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
                   requiredImage.setAttribute("src", WEBAPP_CONTEXT + "/images/icons/required_field.gif");
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
                 child.domContainer.appendChild(contentDiv);
                 contentDiv.style.position = "relative";
                 contentDiv.style.marginRight = "2px";
                 contentDiv.style.left = (child instanceof alfresco.xforms.Group 
                                          ? "0px" 
                                          : "30%");
                 if (!(child instanceof alfresco.xforms.Group))
                 {
                   contentDiv.style.width = (child.domContainer.offsetWidth -
                                             contentDiv.offsetLeft) + "px";
                   //     contentDiv.style.width = ((child.domContainer.offsetWidth - contentDiv.offsetLeft) - 10) + "px";
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

                 var event = document.createEvent("UIEvents");
                 event.initUIEvent("childAdded", false, true, window, 0);
                 event.relatedNode = child;
                 this.domNode.dispatchEvent(event);

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

                 var event = document.createEvent("UIEvents");
                 event.initUIEvent("childRemoved", false, true, window, 0);
                 event.relatedNode = child;
                 this.domNode.dispatchEvent(event);

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
               render: function(attach_point)
               {
                 this.domNode.widget = this;
                 attach_point.appendChild(this.domNode);
         
                 this.domNode.style.position = "relative";
                 this.domNode.style.top = "0px";
                 this.domNode.style.left = "0px";
                 this.domNode.style.width = "100%";
                 if (djConfig.isDebug)
                 {
                   var idNode = document.createElement("div");
                   idNode.style.backgroundColor = "red";
                   idNode.appendChild(document.createTextNode(this.id));
                   this.domNode.appendChild(idNode);
                 }

                 this.groupHeaderNode = document.createElement("div");
                 this.groupHeaderNode.id = this.id + "-groupHeaderNode";
                 this.domNode.appendChild(this.groupHeaderNode);

                 this.domNode.childContainerNode = document.createElement("div");
                 this.domNode.childContainerNode.setAttribute("id", this.id + "-childContainerNode");
                 this.domNode.appendChild(this.domNode.childContainerNode);
                 this.domNode.childContainerNode.style.width = "100%";
                 return this.domNode;
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
               }
             });

dojo.declare("alfresco.xforms.Repeat",
             alfresco.xforms.Group,
             {
               initializer: function(xform, xformsNode) 
               {
                 this.domNode.addEventListener("childAdded", this._childAddedListener, false);
                 this.domNode.addEventListener("childRemoved", this._childRemovedListener, false);
                 this.repeatControls = [];
                 this._selectedIndex = -1;
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
                 var images = [ 
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
                   img.setAttribute("src", (WEBAPP_CONTEXT + "/images/icons/" + 
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
                   result.parentNode.insertBefore(this.repeatControls[position], result.nextSibling);
                 else
                   result.parentNode.appendChild(this.repeatControls[position]);

                 this.repeatControls[position].style.position = "relative";
                 this.repeatControls[position].style.width = repeatControlsWidth + "px";
                 this.repeatControls[position].style.whiteSpace = "nowrap";
                 this.repeatControls[position].style.border = "1px solid black";
                 this.repeatControls[position].style.height = "20px";
                 this.repeatControls[position].style.lineHeight = "20px";
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
                   this._selectedIndex = -1;
                 return this._selectedIndex;
               },
               _updateDisplay: function()
               {
                 this.inherited("_updateDisplay", []);
                 for (var i = 0; i < this.children.length; i++)
                 {
                   this.children[i].domContainer.style.backgroundColor = 
                     i % 2 ? "#f0f0ee" : "#ffffff"; 
                 }
               },
               _getRepeatItemTrigger: function(type, properties)
               {
                 var bw = this.xform.getBinding(this.xformsNode).widgets;
                 for (var i in bw)
                 {
                   if (! (bw[i] instanceof alfresco.xforms.Trigger))
                     continue;
                   var action = bw[i].getAction();
                   if (action.getType() != type)
                     continue;
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
                     return bw[i];
                 }
                 throw new Error("unable to find trigger " + type + 
                                 ", properties " + properties +
                                 " for " + this.id);

               },
               _insertRepeatItemAfter_handler: function(event)
               {
                 dojo.event.browser.stopEvent(event);
                 var repeat = event.target.repeat;
                 if (!repeat.isInsertRepeatItemEnabled())
                   return;

                 var index = repeat.repeatControls.indexOf(event.target.parentNode);
                 var repeatItem = repeat.getChildAt(index);
                 this.setFocusedChild(repeatItem);
                 var trigger = this._getRepeatItemTrigger("insert", { position: "after" });
                 this.xform.fireAction(trigger.id);
               },
               _headerInsertRepeatItemBefore_handler: function(event)
               {
                 if (this.children.length == 0)
                 {
                   dojo.event.browser.stopEvent(event);
                   var repeat = event.target.repeat;
                   if (!repeat.isInsertRepeatItemEnabled())
                     return;
                   this.setFocusedChild(null);
                   var trigger = this._getRepeatItemTrigger("insert", { position: "before" });
                   this.xform.fireAction(trigger.id);
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
               
                 var swapNode = document.createElement("div");
                 this.domNode.childContainerNode.replaceChild(swapNode, fromChild.domContainer);
                 this.domNode.childContainerNode.replaceChild(fromChild.domContainer, toChild.domContainer);
                 this.domNode.childContainerNode.replaceChild(toChild.domContainer, swapNode);
                 
                 this.children[fromIndex] = toChild;
                 this.children[toIndex] = fromChild;
                 this._selectedIndex = toIndex;
                 this._updateDisplay();

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
               render: function(attach_point)
               {
                 this.domNode = this.inherited("render", [ attach_point ]);
                 this.domNode.style.border = "1px solid black";

                 var parentRepeats = this.getParentRepeats();
                 this.domNode.style.marginLeft = (parentRepeats.length * 10) + "px";
                 this.domNode.style.marginRight = (parseInt(this.domNode.style.marginLeft) / 2) + "px";
                 this.domNode.style.width = (this.domNode.offsetParent.offsetWidth - 
                                             parseInt(this.domNode.style.borderWidth) -
                                             parseInt(this.domNode.style.marginLeft) -
                                             parseInt(this.domNode.style.marginRight)) + "px";

                 this.groupHeaderNode.repeat = this;
                 this.groupHeaderNode.style.position = "relative";
                 this.groupHeaderNode.style.top = "0px";
                 this.groupHeaderNode.style.left = "0px";
                 this.groupHeaderNode.style.height = "20px";
                 this.groupHeaderNode.style.lineHeight = "20px";
                 this.groupHeaderNode.style.backgroundColor = "#cddbe8";
                 this.groupHeaderNode.style.fontWeight = "bold";
                 this.groupHeaderNode.style.width = "100%";
                 dojo.event.connect(this.groupHeaderNode, "onclick", function(event)
                                    {
                                      if (event.target == event.currentTarget)
                                        event.currentTarget.repeat.setFocusedChild(null);
                                    });
               
                 //used only for positioning the label accurately
                 this.toggleExpandedImage = document.createElement("img");
                 this.groupHeaderNode.appendChild(this.toggleExpandedImage);
                 this.toggleExpandedImage.setAttribute("src", EXPANDED_IMAGE.src);
                 this.toggleExpandedImage.align = "absmiddle";
                 this.toggleExpandedImage.style.marginLeft = "5px";
                 this.toggleExpandedImage.style.marginRight = "5px";
                 
                 dojo.event.connect(this.toggleExpandedImage, "onclick", this, this._toggleExpanded_clickHandler);
               
                 var label = this.parent.getLabel()
                   if (djConfig.isDebug)
                     label += " [" + this.id + "]";
                   
                 this.groupHeaderNode.appendChild(document.createTextNode(label));
           
                 this.headerInsertRepeatItemImage = document.createElement("img"); 
                 this.headerInsertRepeatItemImage.repeat = this;
                 this.groupHeaderNode.appendChild(this.headerInsertRepeatItemImage);
                 this.headerInsertRepeatItemImage.setAttribute("src", WEBAPP_CONTEXT + "/images/icons/plus.gif");
                 this.headerInsertRepeatItemImage.style.width = "16px";
                 this.headerInsertRepeatItemImage.style.height = "16px";
                 this.headerInsertRepeatItemImage.align = "absmiddle";
                 this.headerInsertRepeatItemImage.style.marginLeft = "5px";
//                 addElement.style.opacity = .2;
           
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
                 var chibaData = _getElementsByTagNameNS(this.xformsNode, CHIBA_NS, CHIBA_NS_PREFIX, "data");
                 chibaData = chibaData[chibaData.length - 1];
                 dojo.debug(CHIBA_NS_PREFIX + ":data == " + dojo.dom.innerXML(chibaData));
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
               isExpanded: function()
               {
                 return this.toggleExpandedImage.getAttribute("src") == EXPANDED_IMAGE.src;
               },
               setExpanded: function(expanded)
               {
                 if (expanded == this.isExpanded())
                   return;
                 this.toggleExpandedImage.src = expanded ? EXPANDED_IMAGE.src : COLLAPSED_IMAGE.src;
                 this.domNode.childContainerNode.style.display = expanded ? "block" : "none";
               },
               _toggleExpanded_clickHandler: function(event)
               {
                 this.setExpanded(!this.isExpanded());
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
               _childAddedListener: function(event)
               {
                 this.widget.headerInsertRepeatItemImage.style.opacity = .3;
                 this.widget._updateRepeatControls();
               },
               _childRemovedListener: function(event)
               {
                 if (this.widget.children.length == 0)
                 {
                   this.widget.headerInsertRepeatItemImage.style.opacity = 1;
                 }
                 this.widget._updateRepeatControls();
               }
             });

dojo.declare("alfresco.xforms.Trigger",
             alfresco.xforms.Widget,
             {
             initializer: function(xform, xformsNode) 
               {
//           this.inherited("initializer", [ xform, xformsNode ]);
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
                 for (var i = 0; i < this.xformsNode.childNodes.length; i++)
                 {
                   var c = this.xformsNode.childNodes[i];
                   if (c.nodeType != dojo.dom.ELEMENT_NODE)
                     continue;
                   if (c.nodeName == XFORMS_NS_PREFIX + ":label" ||
                       c.nodeName == XFORMS_NS_PREFIX + ":alert")
                     continue;
                   return new alfresco.xforms.XFormsAction(this.xform, c);
                 }
                 throw new Error("unable to find action node for " + this.id);
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
//           this.inherited("initializer", [ xform, xformsNode ]);
                 var submit_buttons = _xforms_getSubmitButtons();
                 for (var i = 0; i < submit_buttons.length; i++)
                 {
                   dojo.debug("adding submit handler for " + submit_buttons[i].getAttribute('id'));
                   submit_buttons[i].xform = this.xform;
                   dojo.event.browser.addListener(submit_buttons[i], "onclick", function(event)
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
                                                  });
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
                   if (attr.nodeName.match(/^xforms:/))
                   {
                     this.properties[attr.nodeName.substring((XFORMS_NS_PREFIX + ":").length)] = 
                       attr.nodeValue;
                   }
                 }
               },
               getType: function()
               {
                 return this.xformsNode.nodeName.substring((XFORMS_NS_PREFIX + ":").length);
               },
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
                 var alfUI = document.getElementById("alfresco-xforms-ui");
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
                 case XFORMS_NS_PREFIX + ":group":
                   return new alfresco.xforms.Group(this, node);
                 case XFORMS_NS_PREFIX + ":repeat":
                   return new alfresco.xforms.Repeat(this, node);
                 case XFORMS_NS_PREFIX + ":textarea":
                   return new alfresco.xforms.TextArea(this, node);
                 case XFORMS_NS_PREFIX + ":upload":
                   return new alfresco.xforms.FilePicker(this, node);
                 case XFORMS_NS_PREFIX + ":input":
                   var type = this.getType(node);
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
                 case XFORMS_NS_PREFIX + ":select":
                   return new alfresco.xforms.Select(this, node);
                 case XFORMS_NS_PREFIX + ":select1":
                   return (this.getType(node) == "boolean"
                           ? new alfresco.xforms.Checkbox(this, node)
                           : new alfresco.xforms.Select1(this, node));
                 case XFORMS_NS_PREFIX + ":submit":
                   return new alfresco.xforms.Submit(this, node);
                 case XFORMS_NS_PREFIX + ":trigger":
                   return new alfresco.xforms.Trigger(this, node);
                 case CHIBA_NS_PREFIX + ":data":
                 case XFORMS_NS_PREFIX + ":label":
                 case XFORMS_NS_PREFIX + ":alert":
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
                     var w = this.createWidget(xformsNode.childNodes[i]);
                     if (w != null)
                     {
                       dojo.debug("created " + w.id + " for " + xformsNode.childNodes[i].nodeName);
                       parentWidget.addChild(w);
                       if (w instanceof alfresco.xforms.Group)
                         this.loadWidgets(xformsNode.childNodes[i], w);
                     }
                   }
                 }
               },
               getModel: function()
               {
                 return _getElementsByTagNameNS(this.xformsNode, 
                                                XFORMS_NS, 
                                                XFORMS_NS_PREFIX, 
                                                "model")[0];
               },
               getInstance: function()
               {
                 var model = this.getModel();
                 return _getElementsByTagNameNS(model,
                                                XFORMS_NS,
                                                XFORMS_NS_PREFIX,
                                                "instance")[0];
               },
               getBody: function()
               {
                 var b = _getElementsByTagNameNS(this.xformsNode,
                                                 XHTML_NS,
                                                 XHTML_NS_PREFIX,
                                                 "body");
                 return b[b.length - 1];
               },
               getType: function(node)
               {
                 return this.getBinding(node).type;
               },
               getBinding: function(node)
               {
                 return this._bindings[node.getAttribute(XFORMS_NS_PREFIX + ":bind")];
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
                   if (bind.childNodes[i].nodeName.toLowerCase() == XFORMS_NS_PREFIX + ":bind")
                   {
                     var id = bind.childNodes[i].getAttribute("id");
                     dojo.debug("loading binding " + id);
                     result[id] = 
                       {
                       id: bind.childNodes[i].getAttribute("id"),
                       readonly: bind.childNodes[i].getAttribute(XFORMS_NS_PREFIX + ":readonly"),
                       required: bind.childNodes[i].getAttribute(XFORMS_NS_PREFIX + ":required"),
                       nodeset: bind.childNodes[i].getAttribute(XFORMS_NS_PREFIX + ":nodeset"),
                       type: bind.childNodes[i].getAttribute(XFORMS_NS_PREFIX + ":type"),
                       constraint: bind.childNodes[i].getAttribute(XFORMS_NS_PREFIX + ":constraint"),
                       maximum: parseInt(bind.childNodes[i].getAttribute(ALFRESCO_NS_PREFIX + ":maximum")),
                       minimum: parseInt(bind.childNodes[i].getAttribute(ALFRESCO_NS_PREFIX + ":minimum")),
                       parent: parent,
                       widgets: {}
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
                 var prototypeClones = [];
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
                     xfe.getTarget().setModified(true);
                     xfe.getTarget().setValid(xfe.properties["valid"] == "true");
                     xfe.getTarget().setRequired(xfe.properties["required"] == "true");
                     xfe.getTarget().setReadonly(xfe.properties["readonly"] == "true");
                     xfe.getTarget().setEnabled(xfe.properties["enabled"] == "true");
                     if ("value" in xfe.properties)
                     {
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

                     var prototypeNode = _findElementById(this.xformsNode, originalId);
                     var binding = this.getBinding(prototypeNode);
                     var prototypeToClone = null;
                     for (var w in binding.widgets)
                     {
                       if (binding.widgets[w] instanceof alfresco.xforms.Repeat)
                       {
                         var chibaData = _getElementsByTagNameNS(binding.widgets[w].xformsNode, 
                                                                 CHIBA_NS, 
                                                                 CHIBA_NS_PREFIX, "data");
                         if (chibaData.length == 0)
                           continue;
                         prototypeToClone = dojo.dom.firstElement(chibaData[chibaData.length - 1]);
                       }
                     }
                     if (!prototypeToClone)
                       throw new Error("unable to find prototype for " + originalId);
                     dojo.debug("cloning prototype " + prototypeToClone.getAttribute("id"));
                     var clone = prototypeToClone.cloneNode(true);
                     clone.setAttribute("id", prototypeId);
//                       if (true || originalId  == xfe.targetId)
//                         var clone = xfe.getTarget().handlePrototypeCloned(prototypeId);
//                       else
//                       {
//                         var parentClone = prototypeClones[prototypeClones.length - 1];
                           
//                         var clone = originalWidget.widget.handlePrototypeCloned(prototypeId);
//                       }
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
                     var originalId = xfe.properties["originalId"];
                     var clone = prototypeClones.pop();
                     if (prototypeClones.length == 0)
                       xfe.getTarget().handleItemInserted(clone, position);
                     else
                     {
                       var parentClone = prototypeClones[prototypeClones.length - 1];
                       var parentRepeat = _findElementById(parentClone, xfe.targetId);
                       parentRepeat.appendChild(clone);
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

function create_ajax_request(target, serverMethod, methodArgs, load, error)
{
  var result = new dojo.io.Request(WEBAPP_CONTEXT + "/ajax/invoke/XFormsBean." + serverMethod, "text/xml");
  result.target = target;
  result.content = methodArgs;

  result.load = load;
  dojo.event.connect(result, "load", function(type, data, evt)
                     {
//           _hide_errors();
                       ajax_request_load_handler(this);
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
  var errorDiv = document.getElementById("alfresco-xforms-error");
  if (errorDiv)
  {
    dojo.dom.removeChildren(errorDiv);
    errorDiv.style.display = "none";
  }
}

function _show_error(msg)
{
  var errorDiv = document.getElementById("alfresco-xforms-error");
  if (!errorDiv)
  {
    errorDiv = document.createElement("div");
    errorDiv.setAttribute("id", "alfresco-xforms-error");
    dojo.html.setClass(errorDiv, "infoText statusErrorText");
    errorDiv.style.padding = "2px";
    errorDiv.style.borderColor = "#003366";
    errorDiv.style.borderWidth = "1px";
    errorDiv.style.borderStyle = "solid";
    var alfUI = document.getElementById("alfresco-xforms-ui");
    dojo.dom.prependChild(errorDiv, alfUI);
  }
  if (errorDiv.style.display == "block")
    errorDiv.appendChild(document.createElement("br"));
  else
    errorDiv.style.display = "block";
  errorDiv.appendChild(msg);
}

function send_ajax_request(req)
{
  ajax_request_send_handler(req);
  dojo.io.queueBind(req);
}

function _get_ajax_loader_element()
{
  var result = document.getElementById("alfresco-ajax-loader");
  if (result)
    return result;
  result = document.createElement("div");
  result.setAttribute("id", "alfresco-ajax-loader");
  result.style.position = "absolute";
  result.style.right = "0px";
  result.style.top = "0px";
  result.style.color = "white";
  result.style.backgroundColor = "red";
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

function _getElementsByTagNameNS(parentNode, ns, nsPrefix, tagName)
{
  return (parentNode.getElementsByTagNameNS
          ? parentNode.getElementsByTagNameNS(ns, tagName)
          : parentNode.getElementsByTagName(nsPrefix + ":" + tagName));
}

function _evaluateXPath(xpath, contextNode, result_type)
{
  var xmlDocument = contextNode.ownerDocument;
  dojo.debug("evaluating xpath " + xpath + " on node " + contextNode.nodeName +
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

function FilePickerWidget(node, value, readonly)
{
  this.node = node;
  this.value = value == null || value.length == 0 ? null : value;
  this.readonly =  readonly || false;
}

FilePickerWidget.prototype = {
getValue: function()
{
  return this.value;
},
setValue: function(v)
{
  this.value = (v == null || v.length == 0 ? null : v);
  var event = document.createEvent("UIEvents");
  event.initUIEvent("valueChanged", true, true, window, 0);
  this.node.dispatchEvent(event);
},
setReadonly: function(r)
{
  this.readonly = r;
},
render: function()
{
  this._showSelectedValue();
},
_showSelectedValue: function()
{
  var d = this.node.ownerDocument;
  dojo.dom.removeChildren(this.node);
  dojo.html.setClass(this.node, "selector");

  this.node.style.height = "20px";
  this.node.style.lineHeight = this.node.style.height;
  var event = d.createEvent("UIEvents");
  event.initUIEvent("heightChanged", true, true, window, 0);
  this.node.dispatchEvent(event);

  this.node.appendChild(d.createTextNode(this.value == null 
                                         ? "<none selected>" 
                                         : this.value));
  var selectButton = d.createElement("input");
  this.node.appendChild(selectButton);
  selectButton.filePickerWidget = this;
  selectButton.type = "button";
  selectButton.value = this.value == null ? "Select" : "Change";
  selectButton.enabled = this.readonly;
  selectButton.style.marginLeft = "10px";
  selectButton.style.position = "absolute";
  selectButton.style.right = "10px";
  selectButton.style.top = (.5 * this.node.offsetHeight) - (.5 * selectButton.offsetHeight) + "px";
  dojo.event.connect(selectButton, 
                     "onclick", 
                     function(event)
                     {
                       var w = event.target.filePickerWidget;
                       w._navigateToNode(w.getValue() || "");
                     });
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
  dojo.dom.removeChildren(this.node);
  var d = this.node.ownerDocument;
  this.node.style.height = "200px";
  var event = d.createEvent("UIEvents");
  event.initUIEvent("heightChanged", true, true, window, 0);
  this.node.dispatchEvent(event);

  var currentPath = data.getElementsByTagName("current-node")[0];
  currentPath = currentPath.getAttribute("webappRelativePath");
  var currentPathName = currentPath.replace(/.*\/([^/]+)/, "$1")

  var headerDiv = d.createElement("div");
  headerDiv.style.position = "relative";
  this.node.appendChild(headerDiv);
  headerDiv.style.width = "100%";
  headerDiv.style.height = "30px";
  headerDiv.style.lineHeight = "30px";
  headerDiv.style.backgroundColor = "lightgrey";
  headerDiv.style.paddingLeft = "2px";
  headerDiv.appendChild(d.createTextNode("In: "));

  this.headerMenuTriggerLink = d.createElement("a");
  this.headerMenuTriggerLink.filePickerWidget = this;
  this.headerMenuTriggerLink.setAttribute("webappRelativePath", currentPath);
  headerDiv.appendChild(this.headerMenuTriggerLink);
  this.headerMenuTriggerLink.style.padding = "2px";
  this.headerMenuTriggerLink.style.textDecoration = "none";
  this.headerMenuTriggerLink.style.border = "solid 1px lightgrey";
  dojo.event.connect(this.headerMenuTriggerLink,
                     "onmouseover",
                     function(event)
                     {
                       event.currentTarget.style.borderStyle = "inset";
                     });
  dojo.event.connect(this.headerMenuTriggerLink,
                     "onmouseout",
                     function(event)
                     {
                       var w = event.currentTarget.filePickerWidget;
                       if (!w.parentPathMenu)
                         event.currentTarget.style.borderStyle = "solid";
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
  headerMenuTriggerImage.setAttribute("src", WEBAPP_CONTEXT + "/images/icons/menu.gif");
  headerMenuTriggerImage.style.borderWidth = "0px";
  headerMenuTriggerImage.style.marginLeft = "4px";
  headerMenuTriggerImage.align = "absmiddle";

  var headerRightLink = d.createElement("a");
  headerRightLink.setAttribute("webappRelativePath", currentPath);
  headerRightLink.filePickerWidget = this;
  headerRightLink.setAttribute("href", "javascript:void(0)");
  if (currentPathName != "/")
  {
    dojo.event.connect(headerRightLink, 
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
  navigateToParentNodeImage.style.marginRight = "2px";
  navigateToParentNodeImage.setAttribute("src", WEBAPP_CONTEXT + "/images/icons/up.gif");
  headerRightLink.appendChild(navigateToParentNodeImage);
  headerRightLink.appendChild(d.createTextNode("Go up"));

  headerRightLink.style.position = "absolute";
  headerRightLink.style.height = headerDiv.style.height;
  headerRightLink.style.lineHeight = headerRightLink.style.height;
  headerRightLink.style.top = "0px";
  headerRightLink.style.right = "0px";
  headerRightLink.style.paddingRight = "2px";
  headerDiv.appendChild(headerRightLink);

  var contentDiv = d.createElement("div");
  this.node.appendChild(contentDiv);

  var footerDiv = d.createElement("div");
  footerDiv.style.backgroundColor = "lightgrey";
  var cancelButton = d.createElement("input");
  cancelButton.filePickerWidget = this;
  cancelButton.type = "button";
  cancelButton.value = "Cancel";
  cancelButton.style.margin = "2px 0px 2px 0px";
  dojo.event.connect(cancelButton, "onclick", function(event)
                     {
                       var w = event.target.filePickerWidget;
                       w._showSelectedValue();
                     });

  footerDiv.style.textAlign = "center";
  footerDiv.style.height = headerDiv.style.height;
  footerDiv.appendChild(cancelButton);
  this.node.appendChild(footerDiv);

  contentDiv.style.height = (this.node.offsetHeight - 
                             footerDiv.offsetHeight -
                             headerDiv.offsetHeight - 10) + "px";
  contentDiv.style.overflowY = "auto";
  var childNodes = data.getElementsByTagName("child-node");
  for (var i = 0; i < childNodes.length; i++)
  {
    if (childNodes[i].nodeType != dojo.dom.ELEMENT_NODE)
    {
      continue;
    }
    var row = d.createElement("div");
    contentDiv.appendChild(row);
    row.rowIndex = i;
    row.style.position = "relative";
    row.style.backgroundColor = row.rowIndex % 2 ? "#f0f0ee" : "#ffffff";
    dojo.event.connect(row, "onmouseover", function(event)
                       {
                         event.currentTarget.style.backgroundColor = "orange";
                         var prevHover = event.currentTarget.parentNode.hoverNode;
                         if (prevHover)
                         {
                           prevHover.style.backgroundColor = 
                             prevHover.rowIndex %2 ? "#f0f0ee" :"#ffffff";
                         }
                         event.currentTarget.parentNode.hoverNode = event.currentTarget;
                       });
    dojo.event.connect(row, "onmouseout", function(event)
                       {
                         event.currentTarget.style.backgroundColor = 
                           event.currentTarget.rowIndex %2 ? "#f0f0ee" :"#ffffff";
                       });
    var e = d.createElement("img");
    e.align = "absmiddle";
    e.style.margin = "0px 4px 0px 4px";
    e.setAttribute("src", WEBAPP_CONTEXT + childNodes[i].getAttribute("image"));
    row.appendChild(e);

    var path = childNodes[i].getAttribute("webappRelativePath");
    var name = path.replace(/.*\/([^/]+)/, "$1");

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
    row.appendChild(e);
    e.type = "button";
    e.name = path;
    e.value = "Select";
    
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
_closeParentPathMenu: function()
{
  if (this.parentPathMenu)
  {
    dojo.dom.removeChildren(this.parentPathMenu);
    dojo.dom.removeNode(this.parentPathMenu);
    this.parentPathMenu = null;
  }
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

  this.parentPathMenu.style.position = "absolute";
  this.parentPathMenu.style.backgroundColor = "lightgrey";
  this.parentPathMenu.style.borderStyle = "outset";
  this.parentPathMenu.style.borderWidth = "1px";
  this.parentPathMenu.style.lineHeight = "20px";
  this.parentPathMenu.style.minWidth = "100px";

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
    parentNodeDiv.style.paddingLeft = i * 16 + "px";
    parentNodeDiv.style.border = "1px solid lightgrey";
    parentNodeDiv.style.whiteSpace = "nowrap";

    var parentNodeImage = d.createElement("img");
    parentNodeImage.align = "absmiddle";
    parentNodeImage.style.marginRight = "4px";
    parentNodeDiv.appendChild(parentNodeImage);
    parentNodeImage.setAttribute("src", WEBAPP_CONTEXT + "/images/icons/space_small.gif");
    parentNodeDiv.appendChild(parentNodeImage);
    parentNodeDiv.appendChild(d.createTextNode(path));
    dojo.event.connect(parentNodeDiv,
                       "onmouseover",
                       function(event)
                       {
                         event.currentTarget.style.borderStyle = "inset";
                       });
    dojo.event.connect(parentNodeDiv,
                       "onmouseout",
                       function(event)
                       {
                         event.currentTarget.style.borderStyle = "solid";
                       });
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
