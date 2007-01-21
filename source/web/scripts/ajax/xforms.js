/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
////////////////////////////////////////////////////////////////////////////////
// XForms user interface
//
// This script communicates with the XFormBean to produce and manage an xform.
//
// This script requires dojo.js, tiny_mce.js, and upload_helper.js to be
// loaded in advance.
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// initialization
//
// Initiliaze dojo requirements, tinymce, and add a hook to load the xform.
////////////////////////////////////////////////////////////////////////////////
dojo.require("dojo.date");
dojo.require("dojo.widget.DebugConsole");
dojo.require("dojo.widget.DatePicker");
dojo.require("dojo.widget.TimePicker");
dojo.require("dojo.widget.Button");
dojo.require("dojo.lfx.html");
dojo.hostenv.writeIncludes();

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

////////////////////////////////////////////////////////////////////////////////
// constants
//
// These are the client side declared constants.  Others relating to namespaces
// and the webapp context path are expected to be provided by the jsp including
// this script.
////////////////////////////////////////////////////////////////////////////////
alfresco_xforms_constants.XFORMS_ERROR_DIV_ID = "alfresco-xforms-error";
alfresco_xforms_constants.AJAX_LOADER_DIV_ID = "alfresco-ajax-loader";

alfresco_xforms_constants.EXPANDED_IMAGE = new Image();
alfresco_xforms_constants.EXPANDED_IMAGE.src = 
  alfresco_xforms_constants.WEBAPP_CONTEXT + "/images/icons/expanded.gif";

alfresco_xforms_constants.COLLAPSED_IMAGE = new Image();
alfresco_xforms_constants.COLLAPSED_IMAGE.src = 
  alfresco_xforms_constants.WEBAPP_CONTEXT + "/images/icons/collapsed.gif";

////////////////////////////////////////////////////////////////////////////////
// widgets
////////////////////////////////////////////////////////////////////////////////

/**
 * Base class for all xforms widgets.  Each widget has a set of common properties,
 * particularly a corresponding xforms node, a node within the browser DOM,
 * a parent widget, and state variables.
 */
dojo.declare("alfresco.xforms.Widget",
             null,
             {
               initializer: function(xform, xformsNode) 
               {
                 this.xform = xform;
                 this.xformsNode = xformsNode;
                 this.id = this.xformsNode.getAttribute("id");
                 this._modified = false;
                 this._valid = true;
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

               /////////////////////////////////////////////////////////////////
               // properties
               /////////////////////////////////////////////////////////////////

               /** A reference to the xform. */
               xform: null,

               /** The xformsNode managed by this widget. */
               xformsNode: null,

               /** The dom node containing the label for this widget. */
               labelNode: null,
               
               /** The parent widget, or null if this is the root widget. */
               parent: null,
                 
               /** The dom node for this widget. */
               domNode: null,
                 
               /** The dom node containing this widget. */
               domContainer: null,

               /** The parent widget which is using this as a composite. */
               _compositeParent: null,

               /////////////////////////////////////////////////////////////////
               // methods
               /////////////////////////////////////////////////////////////////

               /** Sets the widget's modified state, as indicated by an XFormsEvent. */
               setModified: function(b)
               {
                 this._modified = b;
                 this._updateDisplay();
                 this.hideAlert();
               },

               /** Sets the widget's valid state, as indicated by an XFormsEvent */
               setValid: function(b)
               {
                 this._valid = b;
                 this._updateDisplay();
                 this.hideAlert();
               },

               /** 
                * Heuristic approach to determine if the widget is valid for submit or
                * if it's causing an xforms-error.
                */
               isValidForSubmit: function()
               {
                 if (!this._valid)
                 {
                   dojo.debug(this.id + " is invalid");
                   return false;
                 }
                 if (!this._modified && 
                     this.isRequired() && 
                     this.getInitialValue() == null)
                 {
                   dojo.debug(this.id + " is unmodified and required and empty");
                   return false;
                 }
                 if (this.isRequired() && this.getValue() == null)
                 {
                   dojo.debug(this.id + " is required and empty");
                   return false;
                 }
                 dojo.debug(this.id + " is valid: {" +
                            "modified: " + this._modified + 
                            ", required: " + this.isRequired() +
                            ", initial_value: " + this.getInitialValue() +
                            ", value: " + this.getValue() + "}");
                 return true;
               },

               /** Returns the depth of the widget within the widget heirarchy. */
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

               /** Returns the root group element */
               getViewRoot: function()
               {
                 var p = this;
                 while (p.parent)
                 {
                   p = p.parent;
                 }
                 if (! (p instanceof alfresco.xforms.ViewRoot))
                 {
                   throw new Error("expected root widget " + p + " to be a view root");
                 }
                 return p;
               },

               /** Returns true if the parent is an ancestor of the given parent */
               isAncestorOf: function(parent)
               {
                 var p = this;
                 while (p.parent)
                 {
                   if (p.parent == parent)
                   {
                     return true;
                   }
                   p = p.parent;
                 }
                 return false;
               },

               /** Sets the widget's enabled state, as indicated by an XFormsEvent */
               setEnabled: function(enabled)
               {
               },

               /** Returns the widget's enabled state */
               isEnabled: function()
               {
                 return true;
               },

               /** Sets the widget's required state, as indicated by an XFormsEvent */
               setRequired: function(b)
               {
                 this._required = b;
                 this._updateDisplay();
               },

               /** Indicates if a value is required for the widget. */
               isRequired: function()
               {
                 if (typeof this._required != "undefined")
                 {
                   return this._required;
                 }
                 var binding = this.xform.getBinding(this.xformsNode);
                 return binding && binding.isRequired();
               },

               /** Sets the widget's readonly state, as indicated by an XFormsEvent */
               setReadonly: function(readonly)
               {
                 this._readonly = readonly;
               },

               /** Indicates if the widget's value is readonly. */
               isReadonly: function()
               {
                 if (typeof this._readonly != "undefined")
                 {
                   return this._readonly;
                 }
                 var binding = this.xform.getBinding(this.xformsNode);
                 return binding && binding.isReadonly();
               },

               isVisible: function()
               {
                 return true;
               },

               /** Commits the changed value to the server */
               _commitValueChange: function()
               {
                 if (this._compositeParent)
                 {
                   this._compositeParent._commitValueChange();
                 }
                 else
                 {
                   this.xform.setXFormsValue(this.id, this.getValue());
                 }
               },

               /** Sets the value contained by the widget */
               setValue: function(value, forceCommit)
               {
                 if (forceCommit)
                 {
                   this.xform.setXFormsValue(this.id, value);
                 }
               },
               
               /** Returns the value contained by the widget, or null if none is set */
               getValue: function()
               {
                 return null;
               },

               /** Sets the widget's initial value. */
               setInitialValue: function(value, forceCommit)
               {
                 this.initialValue = 
                   (typeof value == "string" && value.length == 0 ? null : value);
                 if (forceCommit)
                 {
                   this.xform.setXFormsValue(this.id, value);
                 }
               },

               /** 
                * Returns the widget's local value, either with a local variable, or by 
                * looking it up within the model section. 
                */
               getInitialValue: function()
               {
                 if (typeof this.initialValue != "undefined")
                 {
                   return this.initialValue;
                 }

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
                 if (typeof result == "string" && result.length == 0)
                 {
                   result = null;
                 }
                 dojo.debug("resolved xpath " + xpath + " to " + result);
                 return result;
               },

               /** Produces an xpath to the model node within the instance data document. */
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
                     s += '[' + (repeatIndices.shift().index) + ']';
                   }
                   xpath = s + (xpath.length != 0 ? '/' + xpath : "");
                   binding = binding.parent;
                 }
                 while (binding);
                 return xpath;
               },

               /** Returns the label node for this widget from the xforms document. */
               _getLabelNode: function()
               {
                 return this._getChildXFormsNode("label");
               },

               /** Returns the alert node for this widget from the xforms document. */
               _getAlertNode: function()
               {
                 return this._getChildXFormsNode("alert");
               },

               /** Returns a child node by name within the xform. */
               _getChildXFormsNode: function(nodeName)
               {
                 var x = _getElementsByTagNameNS(this.xformsNode, 
                                                 alfresco_xforms_constants.XFORMS_NS,
                                                 alfresco_xforms_constants.XFORMS_PREFIX,
                                                 nodeName);
                 for (var i = 0; i < x.length; i++)
                 {
                   if (x[i].parentNode == this.xformsNode)
                   {
                     return x[i];
                   }
                 }
                 return null;
               },

               /** Returns the widget's label. */
               getLabel: function()
               {
                 var node = this._getLabelNode();
                 var result = node ? dojo.dom.textContent(node) : "";
                 if (djConfig.isDebug)
                 {
                   result += " [" + this.id + "]";
                 }
                 return result;
               },

               /** Returns the widget's alert text. */
               getAlert: function()
               {
                 var node = this._getAlertNode();
                 return node ? dojo.dom.textContent(node) : "";
               },

               /** Makes the label red. */
               showAlert: function()
               {
                 if (!dojo.html.hasClass(this.labelNode, "xformsItemLabelSubmitError"))
                 {
                   dojo.html.addClass(this.labelNode,  "xformsItemLabelSubmitError");
                 }
               },

               /** Restores the label to its original color. */
               hideAlert: function()
               {
                 if (dojo.html.hasClass(this.labelNode, "xformsItemLabelSubmitError"))
                 {
                   dojo.html.removeClass(this.labelNode,  "xformsItemLabelSubmitError");
                 }
               },

               /** Updates the display of the widget.  This is intended to be overridden. */
               _updateDisplay: function()
               {
//                 this.domContainer.style.backgroundColor =  
//                   (!this._valid ? "yellow" : this._modified ? "lightgreen" : "white");
               },

               /** Destroy the widget and any resources no longer needed. */
               _destroy: function()
               {
                 dojo.debug("destroying " + this.id);
               },

               /** 
                * Returns an array of RepeatIndexDatas corresponding to all enclosing repeats.
                * The closest repeat will be at index 0.
                */
               getRepeatIndices: function()
               {
                 var result = [];
                 var w = this;
                 while (w.parent)
                 {
                   if (w.parent instanceof alfresco.xforms.Repeat)
                   {
                     result.push(new alfresco.xforms.RepeatIndexData(w.parent,
                                                                     w.parent.getChildIndex(w) + 1));
                   }
                   w = w.parent;
                 }
                 return result;
               },

               /** 
                * Returns an array of RepeatIndexDatas corresponding to all enclosing repeats.
                * The closest repeat will be at index 0.
                */
               getParentGroups: function(appearance)
               {
                 var result = [];
                 var w = this;
                 while (w.parent)
                 {
                   if (w.parent instanceof alfresco.xforms.Group)
                   {
                     if (appearance && w.parent.getAppearance() == appearance)
                     {
                       result.push(w.parent);
                     }
                   }
                   w = w.parent;
                 }
                 return result;
               }
             });

////////////////////////////////////////////////////////////////////////////////
// widgets for atomic types
////////////////////////////////////////////////////////////////////////////////

/** The file picker widget which handles xforms widget xf:upload. */
dojo.declare("alfresco.xforms.FilePicker",
             alfresco.xforms.Widget,
             {
               initializer: function(xform, xformsNode)
               {
               },

               /////////////////////////////////////////////////////////////////
               // overridden methods
               /////////////////////////////////////////////////////////////////

               render: function(attach_point)
               {
                 dojo.html.prependClass(this.domNode, "xformsFilePicker");
                 attach_point.appendChild(this.domNode);
                 //XXXarielb support readonly and disabled
                 this.widget = new FilePickerWidget(this.id,
                                                    this.domNode, 
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

               setValue: function(value, forceCommit)
               {
                 if (!this.widget)
                 {
                   this.setInitialValue(value, forceCommit);
                 }
                 else
                 {
                   this.inherited("setValue", [ value, forceCommit ]);
                   this.widget.setValue(value);
                 }
               },

               /////////////////////////////////////////////////////////////////
               // DOM event handlers
               /////////////////////////////////////////////////////////////////

               _filePicker_changeHandler: function(fpw)
               {
                 fpw.node.widget._commitValueChange();
               },

               _filePicker_resizeHandler: function(fpw) 
               { 
                 var w = fpw.node.widget;
                 w.domContainer.style.height = 
                   Math.max(fpw.node.offsetHeight + 
                            dojo.style.getMarginHeight(w.domNode.parentNode),
                            20) + "px";
               }
             });

/** The textfield widget which handle xforms widget xf:input with any string or numerical type */
dojo.declare("alfresco.xforms.TextField",
             alfresco.xforms.Widget,
             {
               initializer: function(xform, xformsNode) 
               {
               },

               /////////////////////////////////////////////////////////////////
               // overridden methods
               /////////////////////////////////////////////////////////////////

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

               setValue: function(value, forceCommit)
               {
                 if (!this.widget)
                 {
                   this.setInitialValue(value, forceCommit);
                 }
                 else
                 {
                   this.inherited("setValue", [ value, forceCommit ]);
                   this.widget.value = value;
                 }
               },

               getValue: function()
               {
                 return (this.widget.value != null && this.widget.value.length == 0 
                         ? null 
                         : this.widget.value);
               },

               /////////////////////////////////////////////////////////////////
               // DOM event handlers
               /////////////////////////////////////////////////////////////////

               _widget_changeHandler: function(event)
               {
                 this._commitValueChange();
               }
             });

/** The textfield widget which handle xforms widget xf:textarea. */
dojo.declare("alfresco.xforms.TextArea",
             alfresco.xforms.Widget,
             {
               initializer: function(xform, xformsNode) 
               {
                 this.focused = false;
               },

               /////////////////////////////////////////////////////////////////
               // methods
               /////////////////////////////////////////////////////////////////

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

                 tinyMCE.addEvent(editorDocument, 
                                  dojo.render.html.ie ? "beforedeactivate" : "blur", 
                                  this._tinyMCE_blurHandler);
                 tinyMCE.addEvent(editorDocument, "focus", this._tinyMCE_focusHandler);
               },

               /////////////////////////////////////////////////////////////////
               // overridden methods
               /////////////////////////////////////////////////////////////////

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
                   try
                   {
                     tinyMCE.setContent(value);
                   }
                   catch (e)
                   {
                     //XXXarielb figure this out - getting intermittent errors in IE.
                     dojo.debug(e);
                   }
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

               _destroy: function()
               {
                 this.inherited("_destroy", []);
                 if (!this.isReadonly())
                 {
                   dojo.debug("removing mce control " + this.id);
                   tinyMCE.removeMCEControl(this.id);
                 }
               },

               /////////////////////////////////////////////////////////////////
               // DOM event handlers
               /////////////////////////////////////////////////////////////////

               _tinyMCE_blurHandler: function(event)
               {
                 if (event.type == "beforedeactivate")
                 {
                   event.target = event.srcElement.ownerDocument;
                 }
                 var widget = event.target.widget;
                 widget._commitValueChange();
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
               }
             });

/** Base class for all select widgets. */
dojo.declare("alfresco.xforms.AbstractSelectWidget",
             alfresco.xforms.Widget,
             {
               initializer: function(xform, xformsNode) 
               {
               },

               /////////////////////////////////////////////////////////////////
               // methods
               /////////////////////////////////////////////////////////////////

               /**
                * Returns the possible item values for the select control as an array
                * of anonymous objects with properties id, label, value, and valid.
                */
               _getItemValues: function()
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
                   result.push({ 
                     id: value.getAttribute("id"), 
                     label: valid ? dojo.dom.textContent(label) : "",
                     value: valid ? dojo.dom.textContent(value) : "_invalid_value_",
                     valid: valid
                   });
                 }
                 return result;
               }
             });

/** 
 * Handles xforms widget xf:select.  Produces either a multiselect list or a set of
 * checkboxes depending on the number of inputs.
 */
dojo.declare("alfresco.xforms.Select",
             alfresco.xforms.AbstractSelectWidget,
             {
               initializer: function(xform, xformsNode) 
               {
               },

               /////////////////////////////////////////////////////////////////
               // overridden methods
               /////////////////////////////////////////////////////////////////

               render: function(attach_point)
               {
                 var values = this._getItemValues();
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

               setValue: function(value, forceCommit)
               {
                 if (!this.widget)
                 {
                   this.setInitialValue(value, forceCommit);
                 }
                 else
                 {
                   this.inherited("setValue", [ value, forceCommit ]);
                   this._selectedValues = value.split(' ');
                   if (this.widget.nodeName.toLowerCase() == "div")
                   {
                     var checkboxes = this.widgets.getElementsByTagName("input");
                     for (var i = 0; i < checkboxes.length; i++)
                     {
                       checkboxes[i].checked = 
                         this._selectedValues.indexOf(checkboxes[i].getAttribute("value")) != -1;
                     }
                   }
                   else if (this.widget.nodeName.toLowerCase() == "select")
                   {
                     var options = this.widgets.getElementsByTagName("option");
                     for (var i = 0; i < options.length; i++)
                     {
                       options[i].selected = 
                         this._selectedValues.indexOf(options[i].getAttribute("value")) != -1;
                     }
                   }
                   else
                   {
                     throw new Error("unexpected nodeName for Select widget: " + this.widget.nodeName);
                   }
                 }
               },

               getValue: function()
               {
                 return this._selectedValues.length == 0 ? null : this._selectedValues.join(" ");
               },

               /////////////////////////////////////////////////////////////////
               // DOM event handlers
               /////////////////////////////////////////////////////////////////

               _list_changeHandler: function(event) 
               { 
                 this._selectedValues = [];
                 for (var i = 0; i < event.target.options.length; i++)
                 {
                   if (event.target.options[i].selected)
                   {
                     this._selectedValues.push(event.target.options[i].value);
                   }
                 }
                 this._commitValueChange();
               },

               _checkbox_clickHandler: function(event)
               { 
                 this._selectedValues = [];
                 for (var i = 0; i < 5; i++)
                 {
                   var checkbox = document.getElementById(this.id + "_" + i + "-widget");
                   if (checkbox && checkbox.checked)
                   {
                     this._selectedValues.push(checkbox.value);
                   }
                 }
                 this._commitValueChange();
               }
             });

/** 
 * Handles xforms widget xf:select1.  Produces either a combobox or a set of
 * radios depending on the number of inputs.
 */
dojo.declare("alfresco.xforms.Select1",
             alfresco.xforms.AbstractSelectWidget,
             {
               initializer: function(xform, xformsNode) 
               {
               },

               /////////////////////////////////////////////////////////////////
               // overridden methods
               /////////////////////////////////////////////////////////////////

               render: function(attach_point)
               {
                 var values = this._getItemValues();
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
                     if (initial_value && !values[i].valid)
                     {
                       // skip the invalid value if we have a default value
                       continue;
                     }
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

               /** */
               setValue: function(value, forceCommit)
               {
                 if (!this.widget)
                 {
                   this.setInitialValue(value, forceCommit);
                 }
                 else
                 {
                   this.inherited("setValue", [ value, forceCommit ]);
                   this._selectedValue = value;
                   if (this.widget.nodeName.toLowerCase() == "div")
                   {
                     var radios = this.widget.getElementsByTagName("input");
                     for (var i = 0; i < radios.length; i++)
                     {
                       radios[i].checked = radios[i].getAttribute("value") == this._selectedValue;
                     }
                   }
                   else if (this.widget.nodeName.toLowerCase() == "select")
                   {
                     var options = this.widget.getElementsByTagName("option");
                     for (var i = 0; i < options.length; i++)
                     {
                       options[i].selected = options[i].getAttribute("value") == this._selectedValue;
                     }
                   }
                   else
                   {
                     throw new Error("unexpected nodeName for Select1 widget: " + this.widget.nodeName);
                   }
                 }
               },

               getValue: function()
               {
                 return this._selectedValue;
               },

               /////////////////////////////////////////////////////////////////
               // DOM event handlers
               /////////////////////////////////////////////////////////////////

               _combobox_changeHandler: function(event) 
               { 
                 this._selectedValue = event.target.options[event.target.selectedIndex].value;
                 this._commitValueChange();
               },

               _radio_clickHandler: function(event)
               { 
                 if (!event.target.checked)
                 {
                   var all_radios = this.widget.getElementsByTagName("input");
                   for (var i = 0; i < all_radios.length; i++)
                   {
                     if (all_radios[i].name == event.target.name)
                     {
                       all_radios[i].checked = event.target == all_radios[i];
                     }
                   }
                 }
                 this._selectedValue = event.target.value;
                 this._commitValueChange();
               }
             });

/** 
 * Handles xforms widget xf:select1 with a type of boolean.
 */
dojo.declare("alfresco.xforms.Checkbox",
             alfresco.xforms.Widget,
             {
               initializer: function(xform, xformsNode) 
               {
               },

               /////////////////////////////////////////////////////////////////
               // overridden methods
               /////////////////////////////////////////////////////////////////

               render: function(attach_point)
               {
                 var initial_value = this.getInitialValue() == "true";
                 this.widget = document.createElement("input");
                 this.widget.setAttribute("type", "checkbox");
                 this.widget.setAttribute("id", this.id + "-widget");
                 attach_point.appendChild(this.widget);

                 if (initial_value)
                 {
                   this.widget.setAttribute("checked", true);
                 }
                 dojo.event.connect(this.widget, "onclick", this, this._checkbox_clickHandler);
               },

               setValue: function(value, forceCommit)
               {
                 if (!this.widget)
                 {
                   this.setInitialValue(value, forceCommit);
                 }
                 else
                 {
                   this.inherited("setValue", [ value, forceCommit ]);
                   this.widget.checked = value == "true";
                 }
               },

               getValue: function()
               {
                 return this.widget.checked;
               },

               /////////////////////////////////////////////////////////////////
               // DOM event handlers
               /////////////////////////////////////////////////////////////////

               _checkbox_clickHandler: function(event)
               {
                 this._commitValueChange();
               }
             });

////////////////////////////////////////////////////////////////////////////////
// widgets for date types
////////////////////////////////////////////////////////////////////////////////

/** The date picker widget which handles xforms widget xf:input with type xf:date */
dojo.declare("alfresco.xforms.DatePicker",
             alfresco.xforms.Widget,
             {
               initializer: function(xform, xformsNode) 
               {
               },

               /////////////////////////////////////////////////////////////////
               // overridden methods
               /////////////////////////////////////////////////////////////////

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

               setValue: function(value, forceCommit)
               {
                 if (!this.widget)
                 {
                   this.setInitialValue(value, forceCommit);
                 }
                 else
                 {
                   this.inherited("setValue", [ value, forceCommit ]);
                   this.widget.setAttribute("value", value);
                   this.widget.picker.setDate(value);
                 }
               },

               getValue: function()
               {
                 return (this.widget.value == null || this.widget.value.length == 0
                         ? null
                         : this.widget.value);
               },

               /////////////////////////////////////////////////////////////////
               // DOM event handlers
               /////////////////////////////////////////////////////////////////

               _dateTextBox_focusHandler: function(event)
               {
                 dojo.style.hide(this.widget);
                 this.widget.picker.show();
                 this.domContainer.style.height = 
                   Math.max(this.widget.picker.domNode.offsetHeight +
                            dojo.style.getMarginHeight(this.domNode.parentNode),
                            20) + "px";
               },

               _datePicker_setDateHandler: function(event)
               {
                 this.widget.picker.hide();
                 dojo.style.show(this.widget);
                 this.domContainer.style.height = 
                   Math.max(this.domNode.parentNode.offsetHeight + 
                            dojo.style.getMarginHeight(this.domNode.parentNode), 
                            20) + "px";
                 this.widget.value = dojo.widget.DatePicker.util.toRfcDate(this.widget.picker.date);
                 this._commitValueChange();
               }
             });

/** The date picker widget which handles xforms widget xf:input with type xf:date */
dojo.declare("alfresco.xforms.TimePicker",
             alfresco.xforms.Widget,
             {
               initializer: function(xform, xformsNode) 
               {
               },

               /////////////////////////////////////////////////////////////////
               // overridden methods
               /////////////////////////////////////////////////////////////////

               render: function(attach_point)
               {
                 var initial_value = this.getInitialValue() || "";
               
                 attach_point.appendChild(this.domNode);
                 this.widget = document.createElement("div");
//                 this.widget.setAttribute("id", this.id + "-widget");
                 this.domNode.appendChild(this.widget);

                 if (initial_value)
                 {
                   initial_value = initial_value.split(":");
                   var date = new Date();
                   date.setHours(initial_value[0]);
                   date.setMinutes(initial_value[1]);
                   initial_value = dojo.widget.TimePicker.util.toRfcDateTime(date);
                 }

                 this.widget.picker = dojo.widget.createWidget("TimePicker", 
                                                               { 
                                                                 widgetId: this.id + "-widget",
                                                                 storedTime: initial_value
                                                               }, 
                                                               this.widget);
                 this.widget.picker.anyTimeContainerNode.innerHTML = "";

                 // don't let it float - it screws up layout somehow
                 this.widget.picker.domNode.style.cssFloat = "none";
                 this.domNode.style.height = dojo.style.getMarginBoxHeight(this.widget.picker.domNode) + "px";
                 dojo.event.connect(this.widget.picker,
                                    "onSetTime", 
                                    this,
                                    this._timePicker_setTimeHandler);
               },

               setValue: function(value, forceCommit)
               {
                 if (!this.widget)
                 {
                   this.setInitialValue(value, forceCommit);
                 }
                 else
                 {
                   this.inherited("setValue", [ value, forceCommit ]);
                   this.widget.picker.setDateTime(value);
                 }
               },

               getValue: function()
               {
                 return dojo.date.format(this.widget.picker.time, "%H:%M:00");
               },

               /////////////////////////////////////////////////////////////////
               // DOM event handlers
               /////////////////////////////////////////////////////////////////

               _timePicker_setTimeHandler: function(event)
               {
                 this._commitValueChange();
               }
             });

/** The year picker handles xforms widget xf:input with a gYear type */
dojo.declare("alfresco.xforms.YearPicker",
             alfresco.xforms.TextField,
             {
               initializer: function(xform, xformsNode) 
               {
               },

               /////////////////////////////////////////////////////////////////
               // overridden methods
               /////////////////////////////////////////////////////////////////

               render: function(attach_point)
               {
                 this.inherited("render", [ attach_point ]);
                 this.widget.size = "4";
                 this.widget.setAttribute("maxlength", "4");
               },

               getInitialValue: function()
               {
                 var result = this.inherited("getInitialValue", []);
                 return result ? result.replace(/^0*([^0]+)$/, "$1") : result;
               },

               setValue: function(value, forceCommit)
               {
                 this.inherited("setValue", 
                                [ value ? value.replace(/^0*([^0]+)$/, "$1") : null, forceCommit ]);
               },

               getValue: function()
               {
                 var result = this.inherited("getValue", []);
                 return result ? dojo.string.padLeft(result, 4, "0") : null;
               }
             });

/** The day picker widget which handles xforms widget xf:input with type xf:gDay */
dojo.declare("alfresco.xforms.DayPicker",
             alfresco.xforms.Select1,
             {
               initializer: function(xform, xformsNode)
               {
               },

               /////////////////////////////////////////////////////////////////
               // overridden methods
               /////////////////////////////////////////////////////////////////
               _getItemValues: function()
               {
                 var result = [];
                 result.push({id: "day_empty", label: "", value: "", valid: false});
                 for (var i = 1; i <= 31; i++)
                 {
                   result.push({
                         id: "day_" + i, 
                         label: i, 
                         value: "---" + (i < 10 ? "0" + i : i),
                         valid: true});
                 }
                 return result;
               }
             });

/** The month picker widget which handles xforms widget xf:input with type xf:gMonth */
dojo.declare("alfresco.xforms.MonthPicker",
             alfresco.xforms.Select1,
             {
               initializer: function(xform, xformsNode)
               {
               },

               /////////////////////////////////////////////////////////////////
               // overridden methods
               /////////////////////////////////////////////////////////////////
               _getItemValues: function()
               {
                 var result = [];
                 result.push({id: "month_empty", label: "", value: "", valid: false});
                 for (var i = 0; i <= dojo.date.months.length; i++)
                 {
                   if (typeof dojo.date.months[i] != "string")
                   {
                     continue;
                   }
                   result.push({
                         id: "month_" + i, 
                         label: dojo.date.months[i], 
                         value: "--" + (i + 1 < 10 ? "0" + (i + 1) : i + 1),
                         valid: true});
                 }
                 return result;
               }
             });

/** The month day picker widget which handles xforms widget xf:input with type xf:gMonthDay */
dojo.declare("alfresco.xforms.MonthDayPicker",
             alfresco.xforms.Widget,
             {
               initializer: function(xform, xformsNode)
               {
                 this.monthPicker = new alfresco.xforms.MonthPicker(xform, xformsNode);
                 this.monthPicker._compositeParent = this;

                 this.dayPicker = new alfresco.xforms.DayPicker(xform, xformsNode);
                 this.dayPicker._compositeParent = this;
               },

               /////////////////////////////////////////////////////////////////
               // overridden methods
               /////////////////////////////////////////////////////////////////
               render: function(attach_point)
               {
                 this.setValue(this.getInitialValue());
                 attach_point.appendChild(this.domNode);
                 this.dayPicker.render(this.domNode); 
                 this.dayPicker.widget.style.marginRight = "10px";
                 this.monthPicker.render(this.domNode);
               },
                 
               setValue: function(value)
               {
                 this.monthPicker.setValue(value ? value.match(/^--[^-]+/)[0] : null);
                 this.dayPicker.setValue(value ? "---" + value.replace(/^--[^-]+-/, "") : null);
               },
                 
               getValue: function()
               {
                 // format is --MM-DD
                 var day = this.dayPicker.getValue();
                 var month = this.monthPicker.getValue();
                 return month && day ? day.replace(/^--/, month) : null;
               }
             });

/** The year month picker widget which handles xforms widget xf:input with type xf:gYearMonth */
dojo.declare("alfresco.xforms.YearMonthPicker",
             alfresco.xforms.Widget,
             {
               initializer: function(xform, xformsNode)
               {
                 this.yearPicker = new alfresco.xforms.YearPicker(xform, xformsNode);
                 this.yearPicker._compositeParent = this;

                 this.monthPicker = new alfresco.xforms.MonthPicker(xform, xformsNode);
                 this.monthPicker._compositeParent = this;
               },

               /////////////////////////////////////////////////////////////////
               // overridden methods
               /////////////////////////////////////////////////////////////////
               render: function(attach_point)
               {
                 this.setValue(this.getInitialValue());
                 attach_point.appendChild(this.domNode);
                 this.monthPicker.render(this.domNode);
                 this.monthPicker.widget.style.marginRight = "10px";
                 this.yearPicker.domNode.style.display = "inline";
                 this.yearPicker.render(this.domNode);
               },

               setValue: function(value)
               {
                 this.monthPicker.setValue(value ? value.replace(/^[^-]+-/, "--") : null);
                 this.yearPicker.setValue(value ? value.match(/^[^-]+/)[0] : null);
               },

               getValue: function()
               {
                 // format is CCYY-MM
                 var year = this.yearPicker.getValue();
                 var month = this.monthPicker.getValue();
                 return year && month ? month.replace(/^-/, year) : null;
               }
             });

////////////////////////////////////////////////////////////////////////////////
// widgets for group types
////////////////////////////////////////////////////////////////////////////////

/** 
 * Handles xforms widget xf:group.  A group renders and manages a set of children
 * and provides a header for expanding and collapsing the group.  A group header
 * is shown for all group that don't have xf:appearance set to 'repeated' and 
 * that are not the root group.
 */
dojo.declare("alfresco.xforms.Group",
             alfresco.xforms.Widget,
             {
               initializer: function(xform, xformsNode) 
               {
                 this._children = [];
                 dojo.html.removeClass(this.domNode, "xformsItem");
               },

               /////////////////////////////////////////////////////////////////
               // methods & properties
               /////////////////////////////////////////////////////////////////

               /** Returns the value of the appearance attribute for widget */
               getAppearance: function()
               {
                 return (this.xformsNode.getAttribute("appearance") ||
                         this.xformsNode.getAttribute(alfresco_xforms_constants.XFORMS_PREFIX + ":appearance"));
               },

               /** Returns the child at the specified index or null if the index is out of range. */
               getChildAt: function(index)
               {     
                 return index < this._children.length ? this._children[index] : null;
               },

               /** Returns the index of a particular child or -1 if the child was not found. */
               getChildIndex: function(child)
               {
                 for (var i = 0; i < this._children.length; i++)
                 {
                   dojo.debug(this.id + "[" + i + "]: " + 
                              " is " + this._children[i].id + 
                              " the same as " + child.id + "?");
                   if (this._children[i] == child)
                   {
                     return i;
                   }
                 }
                 return -1;
               },

               /** Adds the child to end of the list of children. */
               addChild: function(child)
               {
                 return this._insertChildAt(child, this._children.length);
               },

               /** Inserts a child at the specified position. */
               _insertChildAt: function(child, position)
               {
                 dojo.debug(this.id + "._insertChildAt(" + child.id + ", " + position + ")");
                 child.parent = this;
         
                 child.domContainer = document.createElement("div");
                 child.domContainer.setAttribute("id", child.id + "-domContainer");
                 dojo.html.addClass(child.domContainer, "xformsItemDOMContainer");

                 if (this.parent && this.parent.domNode)
                 {
                   child.domContainer.style.top = this.parent.domNode.style.bottom;
                 }
         
                 if (!this.domNode.childContainerNode.parentNode)
                 {
                   // only add this to the dom once we're adding a child
                   this.domNode.appendChild(this.domNode.childContainerNode);
                 }

                 if (position == this._children.length)
                 {
                   this.domNode.childContainerNode.appendChild(child.domContainer);
                   this._children.push(child);
                 }
                 else
                 {
                   this.domNode.childContainerNode.insertBefore(child.domContainer, 
                                                                this.getChildAt(position).domContainer);
                   this._children.splice(position, 0, child);
                 }

                 if (this.getAppearance() == "full" && 
                     !(this instanceof alfresco.xforms.Repeat) &&
                     child.isVisible() &&
                     ((child instanceof alfresco.xforms.Group && position != 0) ||
                      this._children[position - 1] instanceof alfresco.xforms.Group))
                 {
                   var divider = document.createElement("div");
                   dojo.html.setClass(divider, "xformsGroupDivider");
                   this.domNode.childContainerNode.insertBefore(divider,
                                                                child.domContainer);
                 }

                 var labelDiv = null;
                 if (!(child instanceof alfresco.xforms.Group))
                 {
                   var labelDiv = document.createElement("div");
                   labelDiv.setAttribute("id", child.id + "-label");
                   labelDiv.style.position = "relative";
                   labelDiv.style.left = "0px";
                   child.domContainer.appendChild(labelDiv);

                   var requiredImage = document.createElement("img");
                   requiredImage.setAttribute("src", 
                                              alfresco_xforms_constants.WEBAPP_CONTEXT + "/images/icons/required_field.gif");
                   requiredImage.style.verticalAlign = "middle";
                   requiredImage.style.marginLeft = "5px";
                   requiredImage.style.marginRight = "5px";
                   requiredImage.style.left = "0px";
                   requiredImage.style.position = "relative";
                   requiredImage.style.top = "0px";
                   labelDiv.appendChild(requiredImage);
            
                   if (!child.isRequired())
                   {
                     requiredImage.style.visibility = "hidden";
                   }
                   var label = child.getLabel();
                   if (label)
                   {
                     child.labelNode = document.createElement("span");
                     child.labelNode.style.verticalAlign = "middle";
                     labelDiv.appendChild(child.labelNode);
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

                 contentDiv.style.width = (child instanceof alfresco.xforms.Group
                                           ? "100%"
                                           : (1 - (contentDiv.offsetLeft / 
                                                   child.domContainer.offsetWidth)) * 100 + "%");
                 child.render(contentDiv);
                 if (!(child instanceof alfresco.xforms.Group))
                 {
                   child.domContainer.style.height = 
                     Math.max(contentDiv.offsetHeight + 
                              dojo.style.getMarginHeight(contentDiv), 20) + "px";
//                   child.domContainer.style.lineHeight = child.domContainer.style.height;
                 }

                 dojo.debug(contentDiv.getAttribute("id") + " offsetTop is " + contentDiv.offsetTop);
//                 alert(contentDiv.offsetTop - dojo.style.getPixelValue(contentDiv, "margin-top"));
                 contentDiv.style.top = "-" + Math.max(0, contentDiv.offsetTop - 
                                                       dojo.style.getPixelValue(contentDiv, "margin-top")) + "px";
                 if (labelDiv)
                 {
                   labelDiv.style.top = (contentDiv.offsetTop + ((.5 * contentDiv.offsetHeight) -
                                                                 (.5 * labelDiv.offsetHeight))) + "px";
                 }
                 contentDiv.widget = child;

                 this._updateDisplay();
                 this._childAdded(child);
                 return child.domContainer;
               },

               /** Removes the child at the specified position. */
               _removeChildAt: function(position)
               {
                 var child = this.getChildAt(position);
                 if (!child)
                 {
                   throw new Error("unable to find child at " + position);
                 }

                 this._children.splice(position, 1);
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

               /** Event handler for when a child has been added. */
               _childAdded: function(child) { },

               /** Event handler for when a child has been removed. */
               _childRemoved: function(child) { },

               /////////////////////////////////////////////////////////////////
               // overridden methods & properties
               /////////////////////////////////////////////////////////////////

               /** Iterates all children a produces an array of widgets which are invalid for submit. */
               getWidgetsInvalidForSubmit: function()
               {
                 var result = [];
                 for (var i = 0; i < this._children.length; i++)
                 {
                   if (this._children[i] instanceof alfresco.xforms.Group)
                   {
                     result = result.concat(this._children[i].getWidgetsInvalidForSubmit());
                   }
                   else if (!this._children[i].isValidForSubmit())
                   {
                     result.push(this._children[i]);
                   }
                 }
                 return result;
               },

               /** Recusively destroys all children. */
               _destroy: function()
               {
                 this.inherited("_destroy", []);
                 for (var i = 0; i < this._children.length; i++)
                 {
                   this._children[i]._destroy();
                 }
               },

               setReadonly: function(readonly)
               {
                 this.inherited("setReadonly", [ readonly ]);
                 for (var i = 0; i < this._children.length; i++)
                 {
                   this._children[i].setReadonly(readonly);
                 }
               },

               render: function(attach_point)
               {
                 this.domNode.widget = this;
                 if (false && djConfig.isDebug)
                 {
                   var idNode = document.createElement("div");
                   idNode.style.backgroundColor = "red";
                   idNode.appendChild(document.createTextNode(this.getLabel()));
                   this.domNode.appendChild(idNode);
                 }

                 if (this.getAppearance() == "full")
                 {
                   dojo.html.setClass(this.domNode, "xformsGroup");
                   this.domNode.style.position = "relative";
                   this.domNode.style.marginLeft = 10 + "px";
                   this.domNode.style.marginRight = (parseInt(this.domNode.style.marginLeft) / 3) + "px";
                   if (dojo.render.html.ie)
                   {
                     this.domNode.style.width = "100%";
                   }
                   else
                   {
                     this.domNode.style.width = (1 - ((dojo.style.getBorderWidth(this.domNode) +
                                                       dojo.style.getPaddingWidth(this.domNode) +
                                                       dojo.style.getMarginWidth(this.domNode)) /
                                                      attach_point.offsetWidth)) * 100 + "%";
                   }

                   this.groupHeaderNode = document.createElement("div");
                   this.groupHeaderNode.id = this.id + "-groupHeaderNode";
                   dojo.html.setClass(this.groupHeaderNode, "xformsGroupHeader");
                   this.domNode.appendChild(this.groupHeaderNode);

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
                 }
                 attach_point.appendChild(this.domNode);

                 this.domNode.childContainerNode = document.createElement("div");
                 this.domNode.childContainerNode.setAttribute("id", this.id + "-childContainerNode");
                 this.domNode.childContainerNode.style.position = "relative";
                 this.domNode.childContainerNode.style.width = "100%";

                 return this.domNode;
               },

               /** Indicates if the group is expanded. */
               isExpanded: function()
               {
                 return (this.toggleExpandedImage.getAttribute("src") == 
                         alfresco_xforms_constants.EXPANDED_IMAGE.src);
               },

               /** 
                * Sets the expanded state of the widget.  If collapsed, everything but the header 
                * will be hidden.
                */
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

               _updateDisplay: function()
               {
                 if (dojo.render.html.ie)
                 {
                   this.domNode.style.width = "100%";
                 }
                 else
                 {
                   this.domNode.style.width = (1 - ((dojo.style.getBorderWidth(this.domNode) +
                                                     dojo.style.getPaddingWidth(this.domNode) +
                                                     dojo.style.getMarginWidth(this.domNode)) /
                                                    this.domNode.parentNode.offsetWidth)) * 100 + "%";
                 }

                 for (var i = 0; i < this._children.length; i++)
                 {
                   var contentDiv = document.getElementById(this._children[i].id + "-content");

                   contentDiv.style.position = "static";
                   contentDiv.style.top = "0px";
                   contentDiv.style.left = "0px";

                   contentDiv.style.position = "relative";
                   contentDiv.style.left = (this._children[i] instanceof alfresco.xforms.Group
                                            ? "0px"
                                            : "30%");
                   contentDiv.style.width = (this._children[i] instanceof alfresco.xforms.Group
                                             ? "100%"
                                             : (1 - (contentDiv.offsetLeft / 
                                                     this._children[i].domContainer.parentNode.offsetWidth)) * 100 + "%");

                   this._children[i]._updateDisplay();

                   if (!(this._children[i] instanceof alfresco.xforms.Group))
                   {
                     this._children[i].domContainer.style.height =
                       Math.max(contentDiv.offsetHeight +
                                dojo.style.getMarginHeight(contentDiv), 20) + "px";
                   }

                   contentDiv.style.top = "-" + Math.max(0, contentDiv.offsetTop -
                                                         dojo.style.getPixelValue(contentDiv, "margin-top")) + "px";

                   var labelDiv = document.getElementById(this._children[i].id + "-label");
                   if (labelDiv)
                   {
                     labelDiv.style.position = "static";
                     labelDiv.style.top = "0px";
                     labelDiv.style.left = "0px";
                     labelDiv.style.position = "relative";

                     labelDiv.style.top = (contentDiv.offsetTop + ((.5 * contentDiv.offsetHeight) -
                                                                   (.5 * labelDiv.offsetHeight))) + "px";
                   }
                 }
               },

               showAlert: function()
               {
                 for (var i = 0; i < this._children.length; i++)
                 {
                   this._children[i].showAlert();
                 }
               },

               hideAlert: function()
               {
                 for (var i = 0; i < this._children.length; i++)
                 {
                   this._children[i].hideAlert();
                 }
               },
               
               /////////////////////////////////////////////////////////////////
               // DOM event handlers
               /////////////////////////////////////////////////////////////////
               _toggleExpanded_clickHandler: function(event)
               {
                 this.setExpanded(!this.isExpanded());
               }
             });

dojo.declare("alfresco.xforms.SwitchGroup",
             alfresco.xforms.Group,
             {
               initializer: function(xform, xformsNode)
               {
                 this.selectedCaseId = null;
                 var widgets = this.xform.getBinding(this.xformsNode).widgets;
                 for (var i in widgets)
                 {
                   if (widgets[i] instanceof alfresco.xforms.Select1)
                   {
                     widgets[i].setValue(this.getInitialValue(), "true");
                   }
                 }
               },

               /////////////////////////////////////////////////////////////////
               // overridden methods & properties
               /////////////////////////////////////////////////////////////////

               /** */
               _insertChildAt: function(child, position)
               {
                 var childDomContainer = this.inherited("_insertChildAt", [child, position]);
                 this.selectedCaseId = this.selectedCaseId || child.id;
                 if (this.selectedCaseId != child.id)
                 {
                   childDomContainer.style.display = "none";
                 }
                 return childDomContainer;
               },

                
               /////////////////////////////////////////////////////////////////
               // XForms event handlers
               /////////////////////////////////////////////////////////////////

               /** */
               handleSwitchToggled: function(selectedCaseId, deselectedCaseId)
               {
                 dojo.debug(this.id + ".handleSwitchToggled(" + selectedCaseId + 
                            ", " + deselectedCaseId + ")");
                 this.selectedCaseId = selectedCaseId;
                 for (var i = 0; i < this._children.length; i++)
                 {
                   if (this._children[i].id == selectedCaseId)
                   {
                     this._children[i].domContainer.style.display = "block";
                   }
                   else if (this._children[i].id == deselectedCaseId)
                   {
                     this._children[i].domContainer.style.display = "none";
                   }
                 }
                 this._updateDisplay();
               }
             });

/** 
 * Handles xforms widget xf:group for the root group.  Does some special rendering
 * to present a title rather than a group header.
 */
dojo.declare("alfresco.xforms.ViewRoot",
             alfresco.xforms.Group,
             {
               initializer: function(xform, xformsNode) 
               {
                 this.focusedRepeat = null;
               },
               render: function(attach_point)
               {
                 this.domNode.widget = this;
                 this.domNode.style.position = "relative";
                 this.domNode.style.width = "100%";
                 dojo.html.setClass(this.domNode, "xformsViewRoot");

                 this.groupHeaderNode = document.createElement("div");
                 this.groupHeaderNode.id = this.id + "-groupHeaderNode";
                 dojo.html.setClass(this.groupHeaderNode, "xformsViewRootHeader");
                 this.domNode.appendChild(this.groupHeaderNode);

                 var icon = document.createElement("img");
                 this.groupHeaderNode.appendChild(icon);
                 icon.setAttribute("src", alfresco_xforms_constants.WEBAPP_CONTEXT + "/images/icons/file_large.gif");
                 icon.align = "absmiddle";
                 icon.style.marginLeft = "5px";
                 icon.style.marginRight = "5px";
                 this.groupHeaderNode.appendChild(document.createTextNode(this.getLabel()));
                 attach_point.appendChild(this.domNode);

                 this.domNode.childContainerNode = document.createElement("div");
                 this.domNode.childContainerNode.setAttribute("id", this.id + "-childContainerNode");
                 this.domNode.childContainerNode.style.position = "relative";
                 this.domNode.childContainerNode.style.width = "100%";

                 return this.domNode;
               }
             });

/** A struct for providing repeat index data. */
alfresco.xforms.RepeatIndexData = function(repeat, index)
{
  this.repeat = repeat;
  this.index = index;
  this.toString = function()
  {
    return "{" + this.repeat.id + " = " + this.index + "}";
  };
}

/** 
 * Handles xforms widget xf:repeat.
 */
dojo.declare("alfresco.xforms.Repeat",
             alfresco.xforms.Group,
             {
               initializer: function(xform, xformsNode) 
               {
                 this.repeatControls = [];
                 this._selectedIndex = -1;
               },

               /////////////////////////////////////////////////////////////////
               // methods & properties
               /////////////////////////////////////////////////////////////////

               /** 
                * Indicates whether or not this repeat can insert more children based
                * on the alf:maximum restriction.
                */
               isInsertRepeatItemEnabled: function()
               {
                 var maximum = this.xform.getBinding(this.xformsNode).maximum;
                 maximum = isNaN(maximum) ? Number.MAX_VALUE : maximum;
                 return this._children.length < maximum;
               },

               /** 
                * Indicates whether or not this repeat can removed children based
                * on the alf:minimum restriction.
                */
               isRemoveRepeatItemEnabled: function()
               {
                 var minimum = this.xform.getBinding(this.xformsNode).minimum;
                 minimum = isNaN(minimum) ? this.isRequired() ? 1 : 0 : minimum;
                 return this._children.length > minimum;
               },

               /** 
                * Returns the currently selected index or -1 if this repeat has no repeat items.
                */
               getSelectedIndex: function()
               {
                 this._selectedIndex = Math.min(this._children.length, this._selectedIndex);
                 if (this._children.length == 0)
                 {
                   this._selectedIndex = -1;
                 }
                 return this._selectedIndex;
               },

               /** 
                * Helper function to locate the appropriate repeat item trigger for this repeat.
                * This is done by locating all related widgets via binding, and selecting the
                * Trigger who's action type is the type provided and where the properties
                * provided are the same for that action.  This approach is used rather than simply
                * looking up the trigger by id since the id isn't known for nested repeats as 
                * chiba modifies them.
                */
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

               /** 
                * Sets the currently selected child by calliing XFormsBean.setRepeatIndeces.
                * If the child provided is null, the index is set to 0.
                */
               setFocusedChild: function(child)
               {
                 var oldFocusedRepeat = this.getViewRoot().focusedRepeat;
                 this.getViewRoot().focusedRepeat = this;
                 if (oldFocusedRepeat != null && oldFocusedRepeat != this)
                 {
                   if (!oldFocusedRepeat.isAncestorOf(this))
                   {
                     oldFocusedRepeat._selectedIndex = -1;
                   }
                   oldFocusedRepeat._updateDisplay();
                 }

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
                   {
                     throw new Error("unable to find child " + child.id + " in " + this.id);
                   }
               
                   repeatIndices.push(new alfresco.xforms.RepeatIndexData(this, index + 1));
                   // xforms repeat indexes are 1-based
                   this.xform.setRepeatIndeces(repeatIndices);
                 }
               },

               /** 
                * Calls swapRepeatItems on the XFormsBean which will produce the event log
                * to insert and remove the appropriate repeat items.
                */
               _swapChildren: function(fromIndex, toIndex)
               {
                 dojo.debug(this.id + ".swapChildren(" + fromIndex + ", " + toIndex + ")");
                 var fromChild = this.getChildAt(fromIndex);
                 var toChild = this.getChildAt(toIndex);
                 this.xform.swapRepeatItems(fromChild, toChild);
                 var anim = dojo.lfx.html.fadeOut(fromChild.domContainer, 500);
                 anim.onEnd = function()
                   {
                     fromChild.domContainer.style.display = "none";
                   };
                 anim.play();
               },

               /** 
                * Updates the repeat controls by changing the opacity on the image based on 
                * whether or not the action is enabled.
                */
               _updateRepeatControls: function()
               {
                 var insertEnabled = this.isInsertRepeatItemEnabled();
                 var removeEnabled = this.isRemoveRepeatItemEnabled();
                 for (var i = 0; i < this.repeatControls.length; i++)
                 {
                   dojo.style.setOpacity(this.repeatControls[i].moveRepeatItemUpImage,
                                         i == 0 ? .3 : 1);
                   dojo.style.setOpacity(this.repeatControls[i].moveRepeatItemDownImage, 
                                         i == this.repeatControls.length - 1 ? .3 : 1);
                   dojo.style.setOpacity(this.repeatControls[i].insertRepeatItemImage,
                                         insertEnabled ? 1 : .3);
                   dojo.style.setOpacity(this.repeatControls[i].removeRepeatItemImage,
                                         removeEnabled ? 1 : .3);
                 }
               },

               /////////////////////////////////////////////////////////////////
               // overridden methods & properties
               /////////////////////////////////////////////////////////////////

               /** When debugging, insert the id into the label. */
               getLabel: function()
               {
                 var label = this.parent.getLabel();
                 if (djConfig.isDebug)
                 {
                   label += " [" + this.id + "]";
                 }
                 return label;
               },

               /** Overrides _insertChildAt in Group to provide repeater controls. */
               _insertChildAt: function(child, position)
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
                                           dojo.style.getMarginWidth(img));
                   this.repeatControls[position].appendChild(img);
                   dojo.event.connect(img, "onclick", this, images[i].action);
                 }

                 var result = this.inherited("_insertChildAt", [ child, position ]);
                 child.repeat = this;
                 dojo.event.connect(result, "onclick", function(event)
                                    {
                                      child.repeat.setFocusedChild(child);
                                      event.stopPropagation();
                                    });
                 dojo.html.addClass(result, "xformsRepeatItem");
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
                 this.repeatControls[position].style.backgroundColor = 
                   dojo.html.getStyle(result, "background-color");

                 result.style.paddingBottom = (.5 * this.repeatControls[position].offsetHeight) + "px";

                 this.repeatControls[position].style.top = -(.5 * (this.repeatControls[position].offsetHeight ) +
                                                             dojo.style.getPixelValue(result, "margin-bottom") +
                                                             dojo.style.getBorderExtent(result, "bottom")) + "px";
                 this.repeatControls[position].style.marginRight =
                   (.5 * result.offsetWidth - 
                    .5 * this.repeatControls[position].offsetWidth) + "px"; 

                 this.repeatControls[position].style.marginLeft = 
                   (.5 * result.offsetWidth - 
                    .5 * this.repeatControls[position].offsetWidth) + "px"; 
                 return result;
               },

               /** 
                * Overrides _removeChildAt in Group to remove the repeat controls associated with
                * the repeat item.
                */
               _removeChildAt: function(position)
               {
                 this.repeatControls[position].style.display = "none";
                 dojo.dom.removeChildren(this.repeatControls[position]);
                 dojo.dom.removeNode(this.repeatControls[position]);
                 this.repeatControls.splice(position, 1);
                 return this.inherited("_removeChildAt", [ position ]);
               },

               /** Disables insert before. */
               _childAdded: function(child)
               {
                 dojo.style.setOpacity(this.headerInsertRepeatItemImage, .3);
                 this._updateRepeatControls();
               },

               /** Reenables insert before if there are no children left. */
               _childRemoved: function(child)
               {
                 if (this._children.length == 0)
                 {
                   dojo.style.setOpacity(this.headerInsertRepeatItemImage, 1);
                 }
                 this._updateRepeatControls();
               },

               render: function(attach_point)
               {
                 this.domNode = this.inherited("render", [ attach_point ]);
                 dojo.html.addClass(this.domNode, "xformsRepeat");

                 // clear the border bottom for the group header since we'll be getting it
                 // from the repeat item border
                 this.groupHeaderNode.style.borderBottomWidth = "0px";

                 this.groupHeaderNode.repeat = this;
                 dojo.event.connect(this.groupHeaderNode, "onclick", function(event)
                                    {
                                      if (event.target == event.currentTarget)
                                      {
                                        event.currentTarget.repeat.setFocusedChild(null);
                                      }
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

               _updateDisplay: function()
               {
                 this.inherited("_updateDisplay", []);
                 if (this.getViewRoot().focusedRepeat != null &&
                     (this.getViewRoot().focusedRepeat == this ||
                      this.getViewRoot().focusedRepeat.isAncestorOf(this)))
                 {
                   if (!dojo.html.hasClass(this.groupHeaderNode, "xformsRepeatFocusedHeader"))
                   {
                     dojo.html.addClass(this.groupHeaderNode, "xformsRepeatFocusedHeader");
                   }
                 }
                 else if (dojo.html.hasClass(this.groupHeaderNode, "xformsRepeatFocusedHeader"))
                 {
                   dojo.html.removeClass(this.groupHeaderNode, "xformsRepeatFocusedHeader");
                 }

                 for (var i = 0; i < this._children.length; i++)
                 {
                   if (i + 1 == this.getSelectedIndex() && this.getViewRoot().focusedRepeat == this)
                   {
                     if (dojo.html.hasClass(this._children[i].domContainer, "xformsRowOdd"))
                     {
                       dojo.html.removeClass(this._children[i].domContainer, "xformsRowOdd");
                     }
                     if (dojo.html.hasClass(this._children[i].domContainer, "xformsRowEven"))
                     {
                       dojo.html.removeClass(this._children[i].domContainer, "xformsRowEven");
                     }
                     if (!dojo.html.hasClass(this._children[i].domContainer,"xformsRepeatItemSelected"))
                     {
                       dojo.html.addClass(this._children[i].domContainer, "xformsRepeatItemSelected");
                     }
                   }
                   else
                   {
                     if (dojo.html.hasClass(this._children[i].domContainer, "xformsRepeatItemSelected"))
                     {
                       dojo.html.removeClass(this._children[i].domContainer, "xformsRepeatItemSelected");
                     }
                     if (dojo.html.hasClass(this._children[i].domContainer, "xformsRow" + (i % 2 ? "Odd" : "Even")))
                     {
                       dojo.html.removeClass(this._children[i].domContainer, "xformsRow" + (i % 2 ? "Odd" : "Even"));
                     }
                     dojo.html.addClass(this._children[i].domContainer, 
                                        "xformsRow" + (i % 2 ? "Even" : "Odd")); 
                   }

                   this.repeatControls[i].style.backgroundColor = 
                     dojo.html.getStyle(this._children[i].domContainer, "background-color");
                 }
               },

               /////////////////////////////////////////////////////////////////
               // DOM event handlers
               /////////////////////////////////////////////////////////////////

               /** 
                * Event handler for insert after.  If insert is enabled, causes a setRepeatIndeces
                * and an insert.
                */
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

               /** 
                * Event handler for insert before.  If insert is enabled, causes a setRepeatIndeces
                * and an insert.
                */
               _headerInsertRepeatItemBefore_handler: function(event)
               {
                 if (this._children.length == 0)
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

               /** 
                * Event handler for remove.  If remove is enabled, causes a setRepeatIndeces
                * and an delete.
                */
               _removeRepeatItem_handler: function(event)
               {
                 dojo.event.browser.stopEvent(event);
                 var repeat = event.target.repeat;
                 if (repeat.isRemoveRepeatItemEnabled())
                 {
                   var index = repeat.repeatControls.indexOf(event.target.parentNode);
                   var repeatItem = repeat.getChildAt(index);
                   this.setFocusedChild(repeatItem);
                   var trigger = this._getRepeatItemTrigger("delete", {});
                   this.xform.fireAction(trigger.id);
                 }
               },

               /** 
                * Event handler for move up.  Calls swap children with the child before
                * if the current select child is not the first child.
                */
               _moveRepeatItemUp_handler: function(event)
               {
                 dojo.event.browser.stopEvent(event);
                 var repeat = event.target.repeat;
                 var index = repeat.repeatControls.indexOf(event.target.parentNode);
                 if (index != 0 && repeat._children.length != 1)
                 {
                   var repeatItem = repeat.getChildAt(index);
                   this.setFocusedChild(repeatItem);
                   repeat._swapChildren(index, index - 1);
                 }
               },

               /** 
                * Event handler for move down.  Calls swap children with the child after
                * if the current select child is not the last child.
                */
               _moveRepeatItemDown_handler: function(event)
               {
                 dojo.event.browser.stopEvent(event);
                 var repeat = event.target.repeat;
                 var index = repeat.repeatControls.indexOf(event.target.parentNode);
                 if (index != repeat._children.length - 1 && repeat._children.length != 1)
                 {
                   var repeatItem = repeat.getChildAt(index);
                   this.setFocusedChild(repeatItem);
                   repeat._swapChildren(index, index + 1);
                 }
               },

               /////////////////////////////////////////////////////////////////
               // XForms event handlers
               /////////////////////////////////////////////////////////////////

               /** Sets the selected index. */
               handleIndexChanged: function(index)
               {
                 dojo.debug(this.id + ".handleIndexChanged(" + index + ")");
                 this._selectedIndex = index;
                 this._updateDisplay();
               },

               /** Returns a clone of the specified prototype id. */
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
                 {
                   throw new Error("unable to locate " + prototypeId +
                                   " in " + this.id);
                 }
                 return prototypeToClone.cloneNode(true);
               },

               /** Inserts the clonedPrototype at the specified position. */
               handleItemInserted: function(clonedPrototype, position)
               {
                 dojo.debug(this.id + ".handleItemInserted(" + clonedPrototype.nodeName +
                            ", " + position + ")");
                 var w = this.xform.createWidget(clonedPrototype);
                 this._insertChildAt(w, position);
                 this.xform.loadWidgets(w.xformsNode, w);
               },

               /** Deletes the item at the specified position. */
               handleItemDeleted: function(position)
               {
                 dojo.debug(this.id + ".handleItemDeleted(" + position + ")");
                 this._removeChildAt(position);
               }
             });

////////////////////////////////////////////////////////////////////////////////
// trigger widgets
////////////////////////////////////////////////////////////////////////////////

/** 
 * Handles xforms widget xf:trigger.
 */
dojo.declare("alfresco.xforms.Trigger",
             alfresco.xforms.Widget,
             {
               initializer: function(xform, xformsNode) 
               {
               },
               /////////////////////////////////////////////////////////////////
               // methods & properties
               /////////////////////////////////////////////////////////////////

               /** TODO: DOCUMENT */
               getAction: function()
               {
                 var action = _getElementsByTagNameNS(this.xformsNode, 
                                                      alfresco_xforms_constants.XFORMS_NS,
                                                      alfresco_xforms_constants.XFORMS_PREFIX,
                                                      "action")[0];
                 return new alfresco.xforms.XFormsAction(this.xform, dojo.dom.firstElement(action));
               },

               /////////////////////////////////////////////////////////////////
               // overridden methods
               /////////////////////////////////////////////////////////////////

               isValidForSubmit: function()
               {
                 return true;
               },

               isVisible: function()
               {
                 return false;
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

               /////////////////////////////////////////////////////////////////
               // DOM event handlers
               /////////////////////////////////////////////////////////////////
               _clickHandler: function(event)
               {
                 this.xform.fireAction(this.id);
               }
             });

/** 
 * Handles xforms widget xf:submit.
 */
dojo.declare("alfresco.xforms.Submit",
             alfresco.xforms.Trigger,
             {
               initializer: function(xform, xformsNode) 
               {
                 var submit_buttons = (this.id == "submit" 
                                       ? _xforms_getSubmitButtons()
                                       : (this.id == "save-draft"
                                          ? _xforms_getSaveDraftButtons()
                                          : null));
                 if (submit_buttons == null)
                 {
                   throw new Error("unknown submit button " + this.id);
                 }
                 for (var i = 0; i < submit_buttons.length; i++)
                 {
                   dojo.debug("adding submit handler for " + submit_buttons[i].getAttribute('id'));
                   submit_buttons[i].widget = this;
                   dojo.event.browser.addListener(submit_buttons[i], 
                                                  "onclick", 
                                                  function(event)
                                                  {
                                                    if (!event.target.widget)
                                                    {
                                                      return true;
                                                    }

                                                    var xform = event.target.widget.xform;
                                                    if (xform.submitWidget && xform.submitWidget.done)
                                                    {
                                                      dojo.debug("done - doing base click on " + xform.submitWidget.currentButton.id);
                                                      xform.submitWidget.currentButton = null;
                                                      xform.submitWidget = null;
                                                      return true;
                                                    }
                                                    else
                                                    {
                                                      dojo.debug("triggering submit from handler " + event.target.id);
                                                      dojo.event.browser.stopEvent(event);
                                                      _hide_errors();
                                                      xform.submitWidget = event.target.widget;
                                                      xform.submitWidget.currentButton = event.target;
                                                      xform.submitWidget.widget.buttonClick(); 
                                                      return false;
                                                    }
                                                  },
                                                  false);
                 }
               },
               render: function(attach_point)
               {
                 this.inherited("render", [ attach_point ]);
               },

               /////////////////////////////////////////////////////////////////
               // DOM event handlers
               /////////////////////////////////////////////////////////////////

               _clickHandler: function(event)
               {
                 this.done = false;
                 _hide_errors();
                 this.xform.fireAction(this.id);
               }
             });

/**
 * A struct describing an xforms action block.
 */
dojo.declare("alfresco.xforms.XFormsAction",
             null,
             {
               initializer: function(xform, xformsNode)
               {
                 this.xform = xform;
                 this.xformsNode = xformsNode;
                 /** All properties of the action as map of key value pairs */
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

               /** Returns the action type. */
               getType: function()
               {
                 return this.xformsNode.nodeName.substring((alfresco_xforms_constants.XFORMS_PREFIX + ":").length);
               }
             });

////////////////////////////////////////////////////////////////////////////////
// xforms data model
////////////////////////////////////////////////////////////////////////////////

/** 
 * An xforms event.  A log of events is returned by any xforms action and 
 * is used to update the UI appropriately.
 */
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
               /** Returns the widget managing the specified target id. */
               getTarget: function()
               {
                 var targetDomNode = document.getElementById(this.targetId + "-content");
                 if (!targetDomNode)
                 {
                   throw new Error("unable to find node " + this.targetId + "-content");
                 }
                 return targetDomNode.widget;
               }
             });

/**
 * A parsed xf:bind.
 */
dojo.declare("alfresco.xforms.Binding",
             null,
             {
               initializer: function(xformsNode, parent)
               {
                 this.xformsNode = xformsNode;
                 this.id = this.xformsNode.getAttribute("id");
                 this.nodeset =  this.xformsNode.getAttribute(alfresco_xforms_constants.XFORMS_PREFIX + ":nodeset");
                 this._readonly =
                   (_hasAttribute(this.xformsNode, alfresco_xforms_constants.XFORMS_PREFIX + ":readonly")
                    ? this.xformsNode.getAttribute(alfresco_xforms_constants.XFORMS_PREFIX + ":readonly") == "true()"
                    : null);
                 this._required =
                   (_hasAttribute(this.xformsNode, alfresco_xforms_constants.XFORMS_PREFIX + ":required")
                    ? this.xformsNode.getAttribute(alfresco_xforms_constants.XFORMS_PREFIX + ":required") == "true()"
                    : null);
                 
                 this._type =
                   (_hasAttribute(this.xformsNode, alfresco_xforms_constants.XFORMS_PREFIX + ":type")
                    ? this.xformsNode.getAttribute(alfresco_xforms_constants.XFORMS_PREFIX + ":type")
                    : null);
                 this.constraint = 
                   (_hasAttribute(this.xformsNode, alfresco_xforms_constants.XFORMS_PREFIX + ":constraint")
                    ? this.xformsNode.getAttribute(alfresco_xforms_constants.XFORMS_PREFIX + ":constraint")
                    : null);
                 this.maximum = parseInt(this.xformsNode.getAttribute(alfresco_xforms_constants.ALFRESCO_PREFIX + ":maximum"));
                 this.minimum = parseInt(this.xformsNode.getAttribute(alfresco_xforms_constants.ALFRESCO_PREFIX + ":minimum"));
                 this.parent = parent;
                 this.widgets = {};
               },

               /** Returns the expected schema type for this binding. */
               getType: function()
               {
                 return (this._type != null
                         ? this._type
                         : (this.parent != null ? this.parent.getType() : null));
               },

               /** Returns true if a node bound by this binding has a readonly value */
               isReadonly: function()
               {
                 return (this._readonly != null ? this._readonly : 
                         (this.parent != null ? this.parent.isReadonly() : false));
               },
                 
               /** Returns true if a node bound by this binding has a required value */
               isRequired: function()
               {
                 return (this._required != null ? this._required :
                         (this.parent != null ? this.parent.isRequired() : false));
               },
               
               toString: function()
               {
                 return ("{id: " + this.id + 
                         ",type: " + this.getType() + 
                         ",required: " + this.isRequired() +
                         ",readonly: " + this.isReadonly() +
                         ",nodeset: " + this.nodeset + "}");
               }
             });

/**
 * Manages the xforms document.
 */
dojo.declare("alfresco.xforms.XForm",
             null,
             {
               /////////////////////////////////////////////////////////////////
               // Initialization
               /////////////////////////////////////////////////////////////////

               /** Makes a request to the XFormsBean to load the xforms document. */
               initializer: function()
               {
                 var req = AjaxHelper.createRequest(this,
                                                    "getXForm",
                                                    {},
                                                    function(type, data, evt) 
                                                    {
                                                      this.target._loadHandler(data);
                                                    });
                 AjaxHelper.sendRequest(req);
               },

               /** Parses the xforms document and produces the widget tree. */
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
                 var rootGroup = _getElementsByTagNameNS(this.getBody(),
                                                         alfresco_xforms_constants.XFORMS_NS,
                                                         alfresco_xforms_constants.XFORMS_PREFIX,
                                                         "group")[0];
                 this.rootWidget = new alfresco.xforms.ViewRoot(this, rootGroup);
                 this.rootWidget.render(alfUI);
                 this.loadWidgets(rootGroup, this.rootWidget);
//                 this.rootWidget._updateDisplay();
               },

               /** Creates the widget for the provided xforms node. */
               createWidget: function(xformsNode)
               {
                 dojo.debug("creating widget for " + xformsNode.nodeName.toLowerCase());
                 switch (xformsNode.nodeName.toLowerCase())
                 {
                 case alfresco_xforms_constants.XFORMS_PREFIX + ":group":
                   return new alfresco.xforms.Group(this, xformsNode);
                 case alfresco_xforms_constants.XFORMS_PREFIX + ":repeat":
                   return new alfresco.xforms.Repeat(this, xformsNode);
                 case alfresco_xforms_constants.XFORMS_PREFIX + ":textarea":
                   return new alfresco.xforms.TextArea(this, xformsNode);
                 case alfresco_xforms_constants.XFORMS_PREFIX + ":upload":
                   return new alfresco.xforms.FilePicker(this, xformsNode);
                 case alfresco_xforms_constants.XFORMS_PREFIX + ":input":
                 {
                   var type = this.getBinding(xformsNode).getType();
                   switch (type)
                   {
                   // date types
                   case "date":
                     return new alfresco.xforms.DatePicker(this, xformsNode);
                   case "time":
                     return new alfresco.xforms.TimePicker(this, xformsNode);
                   case "gMonth":
                     return new alfresco.xforms.MonthPicker(this, xformsNode);
                   case "gDay":
                     return new alfresco.xforms.DayPicker(this, xformsNode);
                   case "gYear":
                     return new alfresco.xforms.YearPicker(this, xformsNode);
                   case "gYearMonth":
                     return new alfresco.xforms.YearMonthPicker(this, xformsNode);
                   case "gMonthDay":
                     return new alfresco.xforms.MonthDayPicker(this, xformsNode);
                   case "dateTime":
                   case "yearMonthDuration":
                   case "dayTimeDuration":
                   // number types
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
                   // string types
                   case "string":
                   case "normalizedString":
                   default:
                     return new alfresco.xforms.TextField(this, xformsNode);
                   }
                 }
                 case alfresco_xforms_constants.XFORMS_PREFIX + ":select":
                   return new alfresco.xforms.Select(this, xformsNode);
                 case alfresco_xforms_constants.XFORMS_PREFIX + ":select1":
                   return (this.getBinding(xformsNode).getType() == "boolean"
                           ? new alfresco.xforms.Checkbox(this, xformsNode)
                           : new alfresco.xforms.Select1(this, xformsNode));
                 case alfresco_xforms_constants.XFORMS_PREFIX + ":submit":
                   return new alfresco.xforms.Submit(this, xformsNode);
                 case alfresco_xforms_constants.XFORMS_PREFIX + ":trigger":
                   return new alfresco.xforms.Trigger(this, xformsNode);
                 case alfresco_xforms_constants.CHIBA_PREFIX + ":data":
                 case alfresco_xforms_constants.XFORMS_PREFIX + ":label":
                 case alfresco_xforms_constants.XFORMS_PREFIX + ":alert":
                 {
                   dojo.debug("ignoring " + xformsNode.nodeName);
                   return null;
                 }
                 case alfresco_xforms_constants.XFORMS_PREFIX + ":switch":
                   return new alfresco.xforms.SwitchGroup(this, xformsNode);
                 case alfresco_xforms_constants.XFORMS_PREFIX + ":case":
                   return new alfresco.xforms.Group(this, xformsNode);

                 default:
                   throw new Error("unknown type " + xformsNode.nodeName);
                 }
               },

               /** Loads all widgets for the provided xforms node's children. */
               loadWidgets: function(xformsNode, parentWidget)
               {
                 for (var i = 0; i < xformsNode.childNodes.length; i++)
                 {
                   if (xformsNode.childNodes[i].nodeType == dojo.dom.ELEMENT_NODE)
                   {
                     dojo.debug("loading " + xformsNode.childNodes[i].nodeName + 
                                " into " + parentWidget.id);
                     if (xformsNode.childNodes[i].getAttribute(alfresco_xforms_constants.ALFRESCO_PREFIX +
                                                               ":prototype") == "true")
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

               /** Loads all bindings from the xforms document. */
               _loadBindings: function(bind, parent, result)
               {
                 result = result || [];
                 dojo.debug("loading bindings for " + bind.nodeName);
                 for (var i = 0; i < bind.childNodes.length; i++)
                 {
                   if (bind.childNodes[i].nodeName.toLowerCase() == 
                       alfresco_xforms_constants.XFORMS_PREFIX + ":bind")
                   {
                     var b = new alfresco.xforms.Binding(bind.childNodes[i], parent);
                     result[b.id] = b;
                     dojo.debug("loaded binding " + b);
                     this._loadBindings(bind.childNodes[i], result[b.id], result);
                   }
                 }
                 return result;
               },

               /////////////////////////////////////////////////////////////////
               // XForms model properties & methods
               /////////////////////////////////////////////////////////////////

               /** Returns the model section of the xforms document. */
               getModel: function()
               {
                 return _getElementsByTagNameNS(this.xformsNode, 
                                                alfresco_xforms_constants.XFORMS_NS, 
                                                alfresco_xforms_constants.XFORMS_PREFIX, 
                                                "model")[0];
               },

               /** Returns the instance section of the xforms document. */
               getInstance: function()
               {
                 var model = this.getModel();
                 return _getElementsByTagNameNS(model,
                                                alfresco_xforms_constants.XFORMS_NS,
                                                alfresco_xforms_constants.XFORMS_PREFIX,
                                                "instance")[0];
               },

               /** Returns the body section of the xforms document. */
               getBody: function()
               {
                 var b = _getElementsByTagNameNS(this.xformsNode,
                                                 alfresco_xforms_constants.XHTML_NS,
                                                 alfresco_xforms_constants.XHTML_PREFIX,
                                                 "body");
                 return b[b.length - 1];
               },

               /** Returns the binding corresponding to the provided xforms node. */
               getBinding: function(xformsNode)
               {
                 return this._bindings[xformsNode.getAttribute(alfresco_xforms_constants.XFORMS_PREFIX + ":bind")];
               },

               /** Returns all parsed bindings. */
               getBindings: function()
               {
                 return this._bindings;
               },

               /////////////////////////////////////////////////////////////////
               // XFormsBean interaction
               /////////////////////////////////////////////////////////////////

               /** swaps the specified repeat items by calling XFormsBean.swapRepeatItems. */
               swapRepeatItems: function(fromChild, toChild)
               {
                 var params = 
                 {
                   fromItemId: fromChild.xformsNode.getAttribute("id"),
                   toItemId: toChild.xformsNode.getAttribute("id"),
                   instanceId: this.getInstance().getAttribute("id")
                 };

                 var req = AjaxHelper.createRequest(this,
                                                    "swapRepeatItems",
                                                    params,
                                                    function(type, data, event)
                                                    {
                                                      this.target._handleEventLog(data.documentElement)
                                                    });
                 AjaxHelper.sendRequest(req);
               },

               /** sets the repeat indexes by calling XFormsBean.setRepeatIndeces. */
               setRepeatIndeces: function(repeatIndeces)
               {
                 dojo.debug("setting repeat indeces [" + repeatIndeces.join(", ") + "]");
                 var params = { };
                 params["repeatIds"] = [];
                 for (var i = 0; i < repeatIndeces.length; i++)
                 {
                   params.repeatIds.push(repeatIndeces[i].repeat.id);
                   params[repeatIndeces[i].repeat.id] = repeatIndeces[i].index;
                 }
                 params.repeatIds = params.repeatIds.join(",");
                 var req = AjaxHelper.createRequest(this,
                                                    "setRepeatIndeces",
                                                    params,
                                                    function(type, data, evt)
                                                    {
                                                      this.target._handleEventLog(data.documentElement);
                                                    });
                 AjaxHelper.sendRequest(req);
               },

               /** Fires an action specified by the id by calling XFormsBean.fireAction. */
               fireAction: function(id)
               {
                 var req = AjaxHelper.createRequest(this,
                                                    "fireAction",
                                                    { id: id },
                                                    function(type, data, evt)
                                                    {
                                                      dojo.debug("fireAction." + type);
                                                      this.target._handleEventLog(data.documentElement);
                                                    });
                 AjaxHelper.sendRequest(req);
               },

               /** Sets the value of the specified control id by calling XFormsBean.setXFormsValue. */
               setXFormsValue: function(id, value)
               {
                 value = value == null ? "" : value;
                 dojo.debug("setting value " + id + " = " + value);
                 var req = AjaxHelper.createRequest(this,
                                                    "setXFormsValue",
                                                    { id: id, value: value },
                                                    function(type, data, evt)
                                                    {
                                                      this.target._handleEventLog(data.documentElement);
                                                    });
                 AjaxHelper.sendRequest(req);
               },

               /** Handles the xforms event log resulting from a call to the XFormsBean. */
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
                     var index = Number(xfe.properties["index"]);
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
                       try
                       {
                         xfe.getTarget().setValue(xfe.properties["value"]);
                       }
                       catch(e)
                       {
                         //XXXarielb remove once setValues are implemented.
                         dojo.debug("Error in set value: " + e);
                       }
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
                       //clone = prototypeNode.cloneNode(true);
                       clone = prototypeNode.ownerDocument.createElement(alfresco_xforms_constants.XFORMS_PREFIX + ":group");
                       clone.setAttribute(alfresco_xforms_constants.XFORMS_PREFIX + ":appearance", "repeated");
                       for (var j = 0; j < prototypeNode.childNodes.length; j++)
                       {
                         clone.appendChild(prototypeNode.childNodes[j].cloneNode(true));
                       }
                       clone.setAttribute("id", prototypeId);
                     }

                     if (clone == null)
                     {
                       throw new Error("unable to clone prototype " + prototypeId);
                     }

                     dojo.debug("created clone " + clone.getAttribute("id") + 
                                " nodeName " + clone.nodeName +
                                " parentClone " + (prototypeClones.length != 0 
                                                   ? prototypeClones.peek().getAttribute("id") 
                                                   : null));
                     prototypeClones.push(clone);
                     break;
                   }
                   case "chiba-id-generated":
                   {
                     var originalId = xfe.properties["originalId"];
               
                     dojo.debug("handleIdGenerated(" + xfe.targetId + ", " + originalId + ")");
                     var node = _findElementById(prototypeClones.peek(), originalId);
                     if (!node)
                     {
                       throw new Error("unable to find " + originalId + 
                                       " in clone " + dojo.dom.innerXML(clone));
                     }
                     dojo.debug("applying id " + xfe.targetId + 
                                " to " + node.nodeName + "(" + originalId + ")");
                     node.setAttribute("id", xfe.targetId);
                     if (prototypeClones.length != 1)
                     {
                       var e = _findElementById(prototypeClones[prototypeClones.length - 2], originalId);
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
                     var clone = prototypeClones.pop();
                     if (prototypeClones.length != 0)
                     {
                       dojo.debug("using parentClone " + prototypeClones.peek().getAttribute("id") + 
                                  " of " + clone.getAttribute("id"));
                       var parentRepeat = _findElementById(prototypeClones.peek(), xfe.targetId);
                       parentRepeat.appendChild(clone);
                     }
                     else
                     {
                       dojo.debug("no parentClone found, directly insert " + clone.getAttribute("id") +
                                  " on " + xfe.targetId);
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
                   case "chiba-switch-toggled":
                   {
                     var switchElement = xfe.getTarget();
                     switchElement.handleSwitchToggled(xfe.properties["selected"], 
                                                       xfe.properties["deselected"]);
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
                     this.submitWidget = null;
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

////////////////////////////////////////////////////////////////////////////////
// error message display management
////////////////////////////////////////////////////////////////////////////////

/** hides the error message display. */
function _hide_errors()
{
  var errorDiv = document.getElementById(alfresco_xforms_constants.XFORMS_ERROR_DIV_ID);
  if (errorDiv)
  {
    dojo.dom.removeChildren(errorDiv);
    errorDiv.style.display = "none";
  }
}

/** shows the error message display. */
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

////////////////////////////////////////////////////////////////////////////////
// AJAX helpers
////////////////////////////////////////////////////////////////////////////////

function AjaxHelper()
{
}

/** Creates an ajax request object. */
AjaxHelper.createRequest = function(target, serverMethod, methodArgs, load, error)
{
  var result = new dojo.io.Request(alfresco_xforms_constants.WEBAPP_CONTEXT + 
                                   "/ajax/invoke/XFormsBean." + serverMethod, 
                                   "text/xml");
  result.target = target;
  result.content = methodArgs;

  result.load = load;
  dojo.event.connect(result, "load", function(type, data, evt)
                     {
                       AjaxHelper._loadHandler(result);
                     });
  result.error = error || function(type, e)
    {
      dojo.debug("error [" + type + "] " + e.message);
      _show_error(document.createTextNode(e.message));
      AjaxHelper._loadHandler(this);
    };
  return result;
}

/** Sends an ajax request object. */
AjaxHelper.sendRequest = function(req)
{
  AjaxHelper._sendHandler(req);
  dojo.io.queueBind(req);
}

/** 
 * Returns the ajax loader div element.  If it hasn't yet been created, it is created. 
 */
AjaxHelper._getLoaderElement = function()
{
  var result = document.getElementById(alfresco_xforms_constants.AJAX_LOADER_DIV_ID);
  if (result)
  {
    return result;
  }
  result = document.createElement("div");
  result.setAttribute("id", alfresco_xforms_constants.AJAX_LOADER_DIV_ID);
  dojo.html.setClass(result, "xformsAjaxLoader");
  dojo.style.hide(result);
  document.body.appendChild(result);
  return result;
}

/** All pending ajax requests. */
AjaxHelper._requests = [];

/** Updates the loader message or hides it if nothing is being loaded. */
AjaxHelper._updateLoaderDisplay = function()
{
  var ajaxLoader = AjaxHelper._getLoaderElement();
  ajaxLoader.innerHTML = (AjaxHelper._requests.length == 0
                          ? "Idle"
                          : "Loading" + (AjaxHelper._requests.length > 1
                                         ? " (" + AjaxHelper._requests.length + ")"
                                         : "..."));
  dojo.debug(ajaxLoader.innerHTML);
  if (/* djConfig.isDebug && */ AjaxHelper._requests.length != 0)
  {
    dojo.style.show(ajaxLoader);
  }
  else
  {
    dojo.style.hide(ajaxLoader);
  }
}

////////////////////////////////////////////////////////////////////////////////
// ajax event handlers
////////////////////////////////////////////////////////////////////////////////

AjaxHelper._sendHandler = function(req)
{
  AjaxHelper._requests.push(req);
  AjaxHelper._updateLoaderDisplay();
}

AjaxHelper._loadHandler = function(req)
{
  var index = AjaxHelper._requests.indexOf(req);
  if (index != -1)
  {
    AjaxHelper._requests.splice(index, 1);
  }
  else
  {
    var urls = [];
    for (var i = 0; i < AjaxHelper._requests.length; i++)
    {
      urls.push(AjaxHelper._requests[i].url);
    }
    throw new Error("unable to find " + req.url + 
                    " in [" + urls.join(", ") + "]");
  }
  AjaxHelper._updateLoaderDisplay();
}

////////////////////////////////////////////////////////////////////////////////
// DOM utilities
////////////////////////////////////////////////////////////////////////////////

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

function _hasAttribute(node, name)
{
  return (node == null
          ? false
          : (node.hasAttribute
             ? node.hasAttribute(name)
             : node.getAttribute(name) != null));
}

function _getElementsByTagNameNS(parentNode, ns, nsPrefix, tagName)
{
  return (parentNode.getElementsByTagNameNS
          ? parentNode.getElementsByTagNameNS(ns, tagName)
          : parentNode.getElementsByTagName(nsPrefix + ":" + tagName));
}

////////////////////////////////////////////////////////////////////////////////
// XPath wrapper
////////////////////////////////////////////////////////////////////////////////

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
                      ? xmlDocument.createNSResolver(xmlDocument.documentElement) 
                      : null);
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
      {
        namespaces.push(attr.nodeName + "=\'" + attr.nodeValue + "\'");
      }
    }
    dojo.debug("using namespaces " + namespaces.join(","));
    xmlDocument.setProperty("SelectionNamespaces", namespaces.join(' '));
    if (result_type == XPathResult.FIRST_ORDERED_NODE_TYPE)
    {
      result = xmlDocument.selectSingleNode(xpath);
    }
    else if (result_type == XPathResult.BOOLEAN_TYPE)
    {
      result = true;
    }
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
      {
        return i;
      }
    }
    return -1;
  }
}

if (!Array.prototype.peek)
{
  Array.prototype.peek = function(o)
  {
    return this[this.length - 1];
  }
}

////////////////////////////////////////////////////////////////////////////////
// Custom widget implementations.
////////////////////////////////////////////////////////////////////////////////

/**
 * The file picker widget.
 */
function FilePickerWidget(uploadId, node, value, readonly, change_callback, resize_callback)
{
  this.uploadId = uploadId;
  this.node = node;
  this.value = value == null || value.length == 0 ? null : value;
  this.readonly =  readonly || false;
  this.change_callback = change_callback;
  this.resize_callback = resize_callback;
}

// static methods and properties

FilePickerWidget._uploads = [];
FilePickerWidget._handleUpload = function(id, fileInput, webappRelativePath, widget)
{
  FilePickerWidget._uploads[id] = 
  {
    widget:widget, 
    path: fileInput.value, 
    webappRelativePath: webappRelativePath
  };

  handle_upload_helper(fileInput, 
                       id,
                       FilePickerWidget._upload_completeHandler,
                       alfresco_xforms_constants.WEBAPP_CONTEXT,
                       "/ajax/invoke/XFormsBean.uploadFile",
                       { currentPath: webappRelativePath });
}

FilePickerWidget._upload_completeHandler = function(id, path, fileName, fileTypeImage, error)
{
  var upload = FilePickerWidget._uploads[id];
  upload.widget._upload_completeHandler(fileName, 
                                        upload.webappRelativePath,
                                        fileTypeImage,
                                        error);
}

// instance methods and properties

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
_showStatus: function(text, isError)
{
  var d = this.node.ownerDocument;
  if (!this.statusDiv || !this.statusDiv.parentNode)
  {
    this.statusDiv = d.createElement("div");
    this.statusDiv.setAttribute("id", this.uploadId + "-status");
    this.statusDiv.widget = this;
    this.node.insertBefore(this.statusDiv, this.node.firstChild);
    dojo.html.setClass(this.statusDiv, "infoText xformsFilePickerStatus");
    if (isError)
    {
      dojo.html.addClass(this.statusDiv, "statusErrorText");
    }
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
  setTimeout("var _status = document.getElementById('" + this.uploadId + 
             "-status'); if (_status && _status) { _status.widget._hideStatus(); }", 5000);
},
_hideStatus: function()
{
  if (this.statusDiv)
  {
    var anim = dojo.lfx.html.fadeOut(this.statusDiv, 500);
    var _fp_widget = this;
    anim.onEnd = function()
    {
      if (_fp_widget.statusDiv && _fp_widget.statusDiv.parentNode)
      {
        _fp_widget.node.style.height = (parseInt(_fp_widget.node.style.height) -
                                        _fp_widget.statusDiv.offsetHeight) + "px";
        dojo.dom.removeChildren(_fp_widget.statusDiv);
        dojo.dom.removeNode(_fp_widget.statusDiv);
        _fp_widget.resize_callback(_fp_widget);
        _fp_widget.statusDiv = null;
      }
    };
    
    anim.play();
  }
},
_showSelectedValue: function()
{
  var d = this.node.ownerDocument;
  dojo.dom.removeChildren(this.node);
  this.statusDiv = null;
  this.contentDiv = null;
  this.addContentDiv = null;

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
  var req = AjaxHelper.createRequest(this,
                                     "getFilePickerData",
                                     {},
                                     function(type, data, evt)
                                     {
                                       this.target._showPicker(data.documentElement);
                                     });
  req.content.currentPath = path;
  AjaxHelper.sendRequest(req);
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
                       var w = event.target.filePickerWidget;
                       if (w.addContentDiv)
                       {
                         w._hideAddContent();
                       }
                       else
                       {
                         w._showAddContent(event.target.getAttribute("webappRelativePath"));
                       }
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
  dojo.style.setOpacity(navigateToParentNodeImage, (currentPathName == "/" ? .3 : 1));
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

  var childNodes = data.getElementsByTagName("child-node");
  for (var i = 0; i < childNodes.length; i++)
  {
    if (childNodes[i].nodeType != dojo.dom.ELEMENT_NODE)
    {
      continue;
    }
    var webappRelativePath = childNodes[i].getAttribute("webappRelativePath");
    var fileName = webappRelativePath.replace(/.*\/([^/]+)/, "$1");
    var row = this._createRow(fileName,
                              webappRelativePath,
                              childNodes[i].getAttribute("type") == "directory",
                              childNodes[i].getAttribute("image"),
                              "xformsRow" + (i % 2 ? "Even" : "Odd"));
    this.contentDiv.appendChild(row);
  }
},
_createRow: function(fileName, webappRelativePath,  isDirectory, fileTypeImage, rowClass)
{
  var d = this.contentDiv.ownerDocument;
  var result = d.createElement("div");
  result.setAttribute("id", fileName + "-row");

  dojo.html.setClass(result, "xformsFilePickerRow " + rowClass);
  dojo.event.browser.addListener(result,
                                 "mouseover", 
                                 function(event)
                                 {
                                   var prevHover = event.currentTarget.parentNode.hoverNode;
                                   if (prevHover)
                                   {
                                     dojo.html.removeClass(prevHover, "xformsRowHover");
                                   }
                                   event.currentTarget.parentNode.hoverNode = event.currentTarget;
                                   dojo.html.addClass(event.currentTarget, "xformsRowHover");
                                 },
                                 true);
  dojo.event.browser.addListener(result,
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
  e.setAttribute("src", alfresco_xforms_constants.WEBAPP_CONTEXT + fileTypeImage);
  result.appendChild(e);

  if (isDirectory)
  {
    e = d.createElement("a");
    e.filePickerWidget = this;
    e.setAttribute("href", "javascript:void(0)");
    e.setAttribute("webappRelativePath", webappRelativePath); 
    dojo.event.connect(e, "onclick", function(event)
                       {
                         var w = event.target.filePickerWidget;
                         w._navigateToNode(event.target.getAttribute("webappRelativePath"));
                         return true;
                       });
    e.appendChild(d.createTextNode(fileName));
    result.appendChild(e);
  }
  else
  {
    result.appendChild(d.createTextNode(fileName));
  }

  e = d.createElement("input");
  e.filePickerWidget = this;
  e.type = "button";
  e.name = webappRelativePath;
  e.value = "Select";
  result.appendChild(e);
    
  e.style.position = "absolute";
  e.style.right = "10px";
  e.style.top = (.5 * result.offsetHeight) - (.5 * e.offsetHeight) + "px";
  dojo.event.connect(e, "onclick", function(event)
                     {
                       var w = event.target.filePickerWidget;
                       w.setValue(event.target.name);
                       w._showSelectedValue();
                     });
  return result;
},
_hideAddContent: function()
{
  if (this.addContentDiv)
  {
    dojo.dom.removeChildren(this.addContentDiv);
    dojo.dom.removeNode(this.addContentDiv);
    this.addContentDiv = null;
  }
},
_showAddContent: function(currentPath)
{
  if (this.addContentDiv)
  {
    return;
  }
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
  fileInput.name = this.uploadId + "_file_input";
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
                       if (w.addContentDiv)
                       {
                         var d = w.addContentDiv.ownerDocument;
                         dojo.dom.removeChildren(w.addContentDiv);

                         var fileName = event.target.value.replace(/.*[\/\\]([^\/\\]+)/, "$1");
                         w.addContentDiv.appendChild(d.createTextNode("Upload: " + fileName));
                         var img = d.createElement("img");
                         img.setAttribute("src", alfresco_xforms_constants.WEBAPP_CONTEXT + 
                                          "/images/icons/process_animation.gif");
                         img.style.position = "absolute";
                         img.style.right = "10px";
                         img.style.height = (.5 * w.addContentDiv.offsetHeight)  + "px";
                         img.style.top = (.25 * w.addContentDiv.offsetHeight) + "px";
                         w.addContentDiv.appendChild(img);
                       }

                       FilePickerWidget._handleUpload(w.uploadId, 
                                                      event.target,
                                                      event.target.getAttribute("webappRelativePath"),
                                                      w);
                     });
},
_upload_completeHandler: function(fileName, webappRelativePath, fileTypeImage, error)
{
  if (error)
  {
    this._showStatus(error, true);
    this._hideAddContent();
    this._showAddContent(webappRelativePath);
  }
  else
  {
    var nextRow = dojo.dom.nextElement(this.addContentDiv);
    var rowClass = (nextRow
                    ? ("xformsRow" + (dojo.html.hasClass(nextRow, "xformsRowEven")
                                      ? "Odd"
                                      : "Even"))
                    : "xformsRowEvent");
    var row = this._createRow(fileName,
                              webappRelativePath == "/" ? "/" + fileName : webappRelativePath + "/" + fileName, 
                              false,
                              fileTypeImage,
                              rowClass);
    this.contentDiv.replaceChild(row, this.addContentDiv);
    this.addContentDiv = null;
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
    parentNodeImage.setAttribute("src", alfresco_xforms_constants.WEBAPP_CONTEXT + 
                                 "/images/icons/space_small.gif");
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
