/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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

djConfig.bindEncoding = "UTF-8";
djConfig.parseWidgets = false;
dojo.require("dojo.date.common");
dojo.require("dojo.debug.console");
dojo.require("dojo.lang.assert");
dojo.require("dojo.lfx.html");
//dojo.hostenv.writeIncludes();

function _xforms_init()
{
  document.xform = new alfresco.xforms.XForm();
}

dojo.addOnLoad(_xforms_init);

////////////////////////////////////////////////////////////////////////////////
// constants
//
// These are the client side declared constants.  Others relating to namespaces
// and the webapp context path are expected to be provided by the jsp including
// this script.
////////////////////////////////////////////////////////////////////////////////
alfresco.xforms.constants.XFORMS_ERROR_DIV_ID = "alfresco-xforms-error";

alfresco.xforms.constants.EXPANDED_IMAGE = new Image();
alfresco.xforms.constants.EXPANDED_IMAGE.src = 
  alfresco.constants.WEBAPP_CONTEXT + "/images/icons/expanded.gif";

alfresco.xforms.constants.COLLAPSED_IMAGE = new Image();
alfresco.xforms.constants.COLLAPSED_IMAGE.src = 
  alfresco.constants.WEBAPP_CONTEXT + "/images/icons/collapsed.gif";

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
             function(xform, xformsNode) 
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
               this.domNode = this.domNode || document.createElement("div");
               this.domNode.setAttribute("id", this.id + "-domNode");
               this.domNode.widget = this;
               dojo.html.setClass(this.domNode, "xformsItem");
             },
             {
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
                 if (this._modified != b)
                 {
                   this._modified = b;
                   this._updateDisplay(false);
                   if (this.isValidForSubmit())
                   {
                     this.hideAlert();
                   }
                 }
               },

               /** Sets the widget's valid state, as indicated by an XFormsEvent */
               setValid: function(b)
               {
                 if (this._valid != b)
                 {
                   this._valid = b;
                   this._updateDisplay(false);
                   if (this.isValidForSubmit())
                   {
                     this.hideAlert();
                   }
                   else
                   {
                     this.showAlert();
                   }
                 }
               },

               /** 
                * Heuristic approach to determine if the widget is valid for submit or
                * if it's causing an xforms-error.
                */
               isValidForSubmit: function()
               {
                 if (typeof this._valid != "undefined" && !this._valid)
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
                 if (this._required != b)
                 {
                   this._required = b;
                   this._updateDisplay(false);
                 }
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
                 this._initialValue = 
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
                 if (typeof this._initialValue != "undefined")
                 {
                   return this._initialValue;
                 }

                 var xpath = this._getXPathInInstanceDocument();
                 var d = this.xformsNode.ownerDocument;
                 var contextNode = this.xform.getInstance();
                 dojo.debug("locating " + xpath + " in " + contextNode.nodeName);
                 this._initialValue = _evaluateXPath("/" + xpath, 
                                                     this.xform.getInstance(), 
                                                     XPathResult.FIRST_ORDERED_NODE_TYPE);
                 if (!this._initialValue)
                 {
                   dojo.debug("unable to resolve xpath  /" + xpath + " for " + this.id);
                   this._initialValue = null;
                 }
                 else
                 {
                   this._initialValue = (this._initialValue.nodeType == dojo.dom.ELEMENT_NODE
                                         ? dojo.dom.textContent(this._initialValue)
                                         : this._initialValue.nodeValue);
                   if (typeof this._initialValue == "string" && this._initialValue.length == 0)
                   {
                     this._initialValue = null;
                   }
                   dojo.debug("resolved xpath " + xpath + " to " + this._initialValue);
                 }
                 return this._initialValue;
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

               /** Returns a child node by name within the xform. */
               _getChildXFormsNode: function(nodeName)
               {
                 var x = _getElementsByTagNameNS(this.xformsNode, 
                                                 alfresco.xforms.constants.XFORMS_NS,
                                                 alfresco.xforms.constants.XFORMS_PREFIX,
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
                 var node = this._getChildXFormsNode("label");
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
                 var node = this._getChildXFormsNode("alert");
                 return node ? dojo.dom.textContent(node) : "";
               },

               /** Returns the widget's alert text. */
               getHint: function()
               {
                 var node = this._getChildXFormsNode("hint");
                 return node ? dojo.dom.textContent(node) : null;
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

               /** Returns the value of the appearance attribute for widget */
               getAppearance: function()
               {
                 var result = (this.xformsNode.getAttribute("appearance") ||
                               this.xformsNode.getAttribute(alfresco.xforms.constants.XFORMS_PREFIX + ":appearance"));
                 return result == null || result.length == 0 ? null : result;
               },

               /** Updates the display of the widget.  This is intended to be overridden. */
               _updateDisplay: function(recursively)
               {
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
                */
               getParentGroups: function(appearance)
               {
                 var result = [];
                 var w = this;
                 while (w.parent)
                 {
                   if (w.parent instanceof alfresco.xforms.AbstractGroup)
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
             function(xform, xformsNode)
             {
             },
             {

               /////////////////////////////////////////////////////////////////
               // overridden methods
               /////////////////////////////////////////////////////////////////

               render: function(attach_point)
               {
                 dojo.html.prependClass(this.domNode, "xformsFilePicker");
                 attach_point.appendChild(this.domNode);
                 //XXXarielb support readonly and disabled
                 this.widget = new alfresco.FilePickerWidget(this.id,
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
                   alfresco.xforms.FilePicker.superclass.setValue.call(this, value, forceCommit);
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
                            dojo.html.getMargin(w.domNode.parentNode).height,
                            20) + "px";
               }
             });

/** The textfield widget which handle xforms widget xf:input with any string or numerical type */
dojo.declare("alfresco.xforms.TextField",
             alfresco.xforms.Widget,
             function(xform, xformsNode) 
             {
               this.domNode = document.createElement("span");
               this._maxLength = (_hasAttribute(this.xformsNode, alfresco.xforms.constants.ALFRESCO_PREFIX + ":maxLength")
                                  ? Number(this.xformsNode.getAttribute(alfresco.xforms.constants.ALFRESCO_PREFIX + ":maxLength"))
                                  : -1);
               this._length = (_hasAttribute(this.xformsNode, alfresco.xforms.constants.ALFRESCO_PREFIX + ":length")
                                  ? Number(this.xformsNode.getAttribute(alfresco.xforms.constants.ALFRESCO_PREFIX + ":length"))
                               : -1);

             },
             {

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
                 if (this._maxLength >= 0 || this._length >= 0)
                 {
                   this.widget.setAttribute("maxlength", this._maxLength >= 0 ? this._maxLength : this._length);
                   var maxWidth = this.domNode.offsetWidth;

                   this.widget.style.maxWidth = "100%";
                   this.widget.setAttribute("size", this._maxLength >= 0 ? this._maxLength : this._length);
                 }
                 else if (this.getAppearance() == "full")
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
                   alfresco.xforms.TextField.superclass.setValue.call(this, value, forceCommit);
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

/** The number range widget which handle xforms widget xf:range with any numerical type */
dojo.declare("alfresco.xforms.NumericalRange",
             alfresco.xforms.Widget,
             function(xform, xformsNode) 
             {
               dojo.require("dojo.widget.Slider");
               this._fractionDigits = (_hasAttribute(this.xformsNode, alfresco.xforms.constants.ALFRESCO_PREFIX + ":fractionDigits")
                                       ? Number(this.xformsNode.getAttribute(alfresco.xforms.constants.ALFRESCO_PREFIX + ":fractionDigits"))
                                       : -1);
             },
             {
               /////////////////////////////////////////////////////////////////
               // overridden methods
               /////////////////////////////////////////////////////////////////

               render: function(attach_point)
               {
                 var initial_value = this.getInitialValue() || "";
                 attach_point.appendChild(this.domNode);
                 var sliderDiv = document.createElement("div");
                 sliderDiv.style.fontWeight = "bold";
                 sliderDiv.style.marginBottom = "5px";
                 this.domNode.appendChild(sliderDiv);

                 var minimum = Number(this.xformsNode.getAttribute(alfresco.xforms.constants.XFORMS_PREFIX + ":start"));
                 var maximum = Number(this.xformsNode.getAttribute(alfresco.xforms.constants.XFORMS_PREFIX + ":end"));
                 var snapValues = 0;
                 if (this._fractionDigits == 0)
                 {
                   snapValues = maximum - minimum + 1;
                 }
                 sliderDiv.appendChild(document.createTextNode(minimum));

                 var sliderWidgetDiv = document.createElement("div");
                 sliderDiv.appendChild(sliderWidgetDiv);
                 this.widget = dojo.widget.createWidget("SliderHorizontal",
                                                        {
                                                          initialValue: initial_value,
                                                          minimumX: minimum,
                                                          maximumX: maximum,
                                                          showButtons: false,
                                                          activeDrag: false,
                                                          snapValues: snapValues
                                                        },
                                                        sliderWidgetDiv);
                 sliderDiv.appendChild(document.createTextNode(maximum));
                 
                 this.currentValueDiv = document.createElement("div");
                 this.domNode.appendChild(this.currentValueDiv);
                 this.currentValueDiv.appendChild(document.createTextNode("Value: " + initial_value));
                  
                 dojo.event.connect(this.widget,
                                    "onValueChanged", 
                                    this,
                                    this._hSlider_valueChangedHandler);
               },

               setValue: function(value, forceCommit)
               {
                 if (!this.widget)
                 {
                   this.setInitialValue(value, forceCommit);
                 }
                 else
                 {
                   alfresco.xforms.NumericalRange.superclass.setValue.call(this, value, forceCommit);
                   this.widget.setValue(value);
                 }
               },

               getValue: function()
               {
                 return this.widget.getValue();
               },

               /////////////////////////////////////////////////////////////////
               // DOM event handlers
               /////////////////////////////////////////////////////////////////
                 
               _hSlider_valueChangedHandler: function(value)
               {
                 if (this._fractionDigits >= 0)
                 {
                   value = Math.round(value * Math.pow(10, this._fractionDigits)) / Math.pow(10, this._fractionDigits);
                 }
                 this.currentValueDiv.replaceChild(document.createTextNode("Value: " + value),
                                                   this.currentValueDiv.firstChild);
                 if (!this.widget._isDragInProgress)
                 {
                   this._commitValueChange();
                 }
               }
             });

/** The text area widget handles xforms widget xf:textarea with appearance minimal */
dojo.declare("alfresco.xforms.PlainTextEditor",
             alfresco.xforms.Widget,
             function(xform, xformsNode)
             {
             },
             {
               /////////////////////////////////////////////////////////////////
               // overridden methods
               /////////////////////////////////////////////////////////////////

               render: function(attach_point)
               {
                 attach_point.appendChild(this.domNode);
                 dojo.html.prependClass(this.domNode, "xformsTextArea");
                 var initialValue = this.getInitialValue() || "";
                 this.widget = document.createElement("textarea");
                 this.domNode.appendChild(this.widget);
                 this.widget.setAttribute("id", this.id + "-widget");
                 this.widget.appendChild(document.createTextNode(initialValue));
                 if (this.isReadonly())
                 {
                   this.widget.setAttribute("readonly", this.isReadonly());
                 }
                 this.widget.style.width = "100%";
                 this.widget.style.height = "100%";
                 dojo.event.connect(this.widget, "onchange", this, this._textarea_changeHandler);
               },

               setValue: function(value, forceCommit)
               {
                 if (!this.widget)
                 {
                   this.setInitialValue(value, forceCommit);
                 }
                 else
                 {
                   alfresco.xform.PlainTextEditor.superclass.setValue.call(this, value, forceCommit);
                   this.widget.value = value;
                 }
               },

               getValue: function()
               {
                 return this.widget.value;
               },

               /////////////////////////////////////////////////////////////////
               // DOM event handlers
               /////////////////////////////////////////////////////////////////

               _textarea_changeHandler: function(event)
               {
                 this._commitValueChange();
               }
             });

/** The textfield widget which handle xforms widget xf:textarea. with appearance full or compact */
dojo.declare("alfresco.xforms.RichTextEditor",
             alfresco.xforms.Widget,
             function(xform, xformsNode, params) 
             {
               this._focused = false;
               this._tinyMCE_buttons = params;
               if (!this.statics.tinyMCEInitialized)
               {
                 this.statics.tinyMCEInitialized = true;
               }
             },
             {

               /////////////////////////////////////////////////////////////////
               // methods & properties
               /////////////////////////////////////////////////////////////////
               
               statics: { currentInstance: null, tinyMCEInitialized: false },

               _removeTinyMCE: function()
               {
                 var value = tinyMCE.getContent(this.id);
                 this._commitValueChange();
                 tinyMCE.removeMCEControl(this.id);
                 this._focused = false;
               },

               _createTinyMCE:function()
               {
                 if (this.statics.currentInstance &&
                     this.statics.currentInstance != this)
                 {
                   this.statics.currentInstance._removeTinyMCE();
                 }

                 this.statics.currentInstance = this;

                 tinyMCE.settings.theme_advanced_buttons1 = this._tinyMCE_buttons[0];
                 tinyMCE.settings.theme_advanced_buttons2 = this._tinyMCE_buttons[1];
                 tinyMCE.settings.theme_advanced_buttons3 = this._tinyMCE_buttons[2];
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
                 this.widget.style.border = "1px solid black";
                 this.widget.style.overflow = "auto";
                 this.widget.innerHTML = this.getInitialValue() || "";
                 var images = this.widget.getElementsByTagName("img");
                 for (var i = 0; i < images.length; i++)
                 {
                   if (images[i].getAttribute("src") && 
                       images[i].getAttribute("src").match("^/"))
                   {
                     images[i].setAttribute("src", alfresco.constants.AVM_WEBAPP_URL + images[i].getAttribute("src"));
                   }
                 }
                 if (!this.isReadonly())
                 {
//                   this._createTinyMCE();
                   var me = this;
                   dojo.event.browser.addListener(this.widget, 
                                                  "onmouseover", 
                                                  function(event) { me._div_mouseoverHandler(event) },
                                                  true);
                 }
               },

               setValue: function(value)
               {
                 if (this.statics.currentInstance == this)
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
                 else
                 {
                   this.widget.innerHTML = value;
                 }
               },

               getValue: function()
               {
                 var result = this.statics.currentInstance == this ? tinyMCE.getContent(this.id) : this.widget.innerHTML;
                 result = result.replace(new RegExp(alfresco.constants.AVM_WEBAPP_URL, "g"), "");
                 return result;
               },

               setReadonly: function(readonly)
               {
                 alfresco.xforms.RichTextEditor.superclass.setReadonly.call(this, readonly);
                 if (readonly && this.statics.currentInstance == this)
                 {
                   this._removeTinyMCE();
                 }
               },

               _destroy: function()
               {
                 alfresco.xforms.RichTextEditor.superclass._destroy.call(this);
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
                 this._focused = false;
               },

               _tinyMCE_focusHandler: function(event)
               {
                 var widget = event.target.widget;
                 var repeatIndices = widget.getRepeatIndices();
                 if (repeatIndices.length != 0 && !this._focused)
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
                 this._focused = true;
               },

               _div_mouseoverHandler: function(event)
               {
                 if (!this.hoverLayer)
                 {
                   this.hoverLayer = document.createElement("div");
                   dojo.html.setClass(this.hoverLayer, "xformsRichTextEditorHoverLayer");
                   this.hoverLayer.appendChild(document.createTextNode(alfresco.xforms.constants.resources["click_to_edit"]));
                 }
                 if (!this.hoverLayer.parentNode)
                 {
                   this.widget.appendChild(this.hoverLayer);
                   this.hoverLayer.style.lineHeight = this.hoverLayer.offsetHeight + "px";
                   var me = this;
                   dojo.event.browser.addListener(this.hoverLayer, 
                                                  "onmouseout", 
                                                  function(event) { me._hoverLayer_mouseoutHandler(event) },
                                                  true);

                   dojo.event.browser.addListener(this.hoverLayer,
                                                  "onclick",
                                                  function(event) { me._hoverLayer_clickHandler(event); },
                                                  true);
                 }
               },

               _hoverLayer_mouseoutHandler: function(event)
               {
                 if (this.hoverLayer.parentNode)
                 {
                   this.widget.removeChild(this.hoverLayer);
                 }
               },

               _hoverLayer_clickHandler: function(event)
               {
                 if (this.hoverLayer.parentNode)
                 {
                   this.widget.removeChild(this.hoverLayer);
                   this._createTinyMCE();
                 }
               }
             });

/** Base class for all select widgets. */
dojo.declare("alfresco.xforms.AbstractSelectWidget",
             alfresco.xforms.Widget,
             function(xform, xformsNode) 
             {
             },
             {
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
                                                      alfresco.xforms.constants.XFORMS_NS,
                                                      alfresco.xforms.constants.XFORMS_PREFIX, 
                                                      "item");
                 var result = [];
                 for (var i = 0; i < values.length; i++)
                 {
                   var label = _getElementsByTagNameNS(values[i], 
                                                       alfresco.xforms.constants.XFORMS_NS,
                                                       alfresco.xforms.constants.XFORMS_PREFIX,
                                                       "label")[0];
                   var value = _getElementsByTagNameNS(values[i], 
                                                       alfresco.xforms.constants.XFORMS_NS,
                                                       alfresco.xforms.constants.XFORMS_PREFIX, 
                                                       "value")[0];
                   var valid = true;
                   if (binding.constraint)
                   {
                     if (!dojo.render.html.ie)
                     {
                       valid = _evaluateXPath(binding.constraint, value, XPathResult.BOOLEAN_TYPE);
                       if (djConfig.isDebug)
                       {
                         dojo.debug("evaludated constraint " + binding.constraint + 
                                    " on " + dojo.dom.textContent(value) +
                                    " to " + valid);
                       }
                     }
                     else 
                     {
                       valid = !(dojo.dom.textContent(value) == dojo.dom.textContent(label) && 
                                 dojo.dom.textContent(value).match(/^\[.+\]$/));
                     }
                   }
                   result.push({ 
                     id: value.getAttribute("id"), 
                     label: valid ? dojo.dom.textContent(label) : "",
                     value: valid ? dojo.dom.textContent(value) : "_invalid_value_",
                     valid: valid
                   });

                   if (djConfig.isDebug)
                   {
                     dojo.debug("values["+ i + "] = {id: " + result[i].id + 
                                ",label: " + result[i].label + ",value: " + result[i].value + 
                                ",valid: " + result[i].valid + "}");
                   }
                 }
                 return result;
               }
             });

/** 
 * Handles xforms widget xf:select.  Produces either a multiselect list or a set of
 * checkboxes depending on the number of inputs.
 */
dojo.declare("alfresco.xforms.CheckboxSelect",
             alfresco.xforms.AbstractSelectWidget,
             function(xform, xformsNode) 
             {
             },
             {
               /////////////////////////////////////////////////////////////////
               // overridden methods
               /////////////////////////////////////////////////////////////////

               render: function(attach_point)
               {
                 var values = this._getItemValues();
                 var initial_value = this.getInitialValue();
                 initial_value = initial_value ? initial_value.split(' ') : [];
                 this._selectedValues = [];
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
               },

               setValue: function(value, forceCommit)
               {
                 if (!this.widget)
                 {
                   this.setInitialValue(value, forceCommit);
                 }
                 else
                 {
                   alfresco.xforms.ListSelect.superclass.setValue(this, value, forceCommit);
                   this._selectedValues = value.split(' ');
                   var checkboxes = this.widgets.getElementsByTagName("input");
                   for (var i = 0; i < checkboxes.length; i++)
                   {
                     checkboxes[i].checked = 
                       this._selectedValues.indexOf(checkboxes[i].getAttribute("value")) != -1;
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

               _checkbox_clickHandler: function(event)
               { 
                 this._selectedValues = [];
                 var all_checkboxes = this.widget.getElementsByTagName("input");
                 for (var i = 0; i < all_checkboxes.length; i++)
                 {
                   if (all_checkboxes[i] && all_checkboxes[i].checked)
                   {
                     this._selectedValues.push(all_checkboxes[i].getAttribute("value"));
                   }
                 }
                 this._commitValueChange();
               }
             });
/** 
 * Handles xforms widget xf:select.  Produces either a multiselect list or a set of
 * checkboxes depending on the number of inputs.
 */
dojo.declare("alfresco.xforms.ListSelect",
             alfresco.xforms.AbstractSelectWidget,
             function(xform, xformsNode) 
             {
             },
             {
               /////////////////////////////////////////////////////////////////
               // overridden methods
               /////////////////////////////////////////////////////////////////

               render: function(attach_point)
               {
                 var values = this._getItemValues();
                 var initial_value = this.getInitialValue();
                 initial_value = initial_value ? initial_value.split(' ') : [];
                 this._selectedValues = [];
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
               },

               setValue: function(value, forceCommit)
               {
                 if (!this.widget)
                 {
                   this.setInitialValue(value, forceCommit);
                 }
                 else
                 {
                   alfresco.xforms.CheckboxSelect.superclass.setValue(this, value, forceCommit);
                   this._selectedValues = value.split(' ');
                   var options = this.widgets.getElementsByTagName("option");
                   for (var i = 0; i < options.length; i++)
                   {
                     options[i].selected = 
                       this._selectedValues.indexOf(options[i].getAttribute("value")) != -1;
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
                     this._selectedValues.push(event.target.options[i].getAttribute("value"));
                   }
                 }
                 this._commitValueChange();
               },
             });

/** 
 * Handles xforms widget xf:select1.  Produces either a combobox or a set of
 * radios depending on the number of inputs.
 */
dojo.declare("alfresco.xforms.RadioSelect1",
             alfresco.xforms.AbstractSelectWidget,
             function(xform, xformsNode) 
             {
             },
             {
               /////////////////////////////////////////////////////////////////
               // overridden methods
               /////////////////////////////////////////////////////////////////

               render: function(attach_point)
               {
                 var values = this._getItemValues();
                 var initial_value = this.getInitialValue();
                 this.widget = document.createElement("div");
                 this.widget.style.width = "100%";
                 attach_point.appendChild(this.widget);
                 for (var i = 0; i < values.length; i++)
                 {
                   if (!values[i].valid)
                   {
                     // always skip the invalid values for radios
                     continue;
                   }

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
                   if (this.isReadonly())
                   {
                     radio.setAttribute("disabled", true);
                   }
                   dojo.event.connect(radio, "onclick", this, this._radio_clickHandler);
                 }
                 this.widget.style.height = this.widget.offsetHeight + "px";
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
                   alfresco.xforms.RadioSelect1.superclass.setValue.call(this, value, forceCommit);
                   this._selectedValue = value;
                   var radios = this.widget.getElementsByTagName("input");
                   for (var i = 0; i < radios.length; i++)
                   {
                     radios[i].checked = radios[i].getAttribute("value") == this._selectedValue;
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
 * Handles xforms widget xf:select1.  Produces either a combobox or a set of
 * radios depending on the number of inputs.
 */
dojo.declare("alfresco.xforms.ComboboxSelect1",
             alfresco.xforms.AbstractSelectWidget,
             function(xform, xformsNode) 
             {
             },
             {
               /////////////////////////////////////////////////////////////////
               // overridden methods
               /////////////////////////////////////////////////////////////////

               render: function(attach_point)
               {
                 var values = this._getItemValues();
                 var initial_value = this.getInitialValue();
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

                   if (this.isReadonly())
                   {
                     this.widget.setAttribute("disabled", true);
                   }
                 }
                 dojo.event.connect(this.widget, "onchange", this, this._combobox_changeHandler);
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
                   alfresco.xforms.ComboboxSelect1.superclass.setValue.call(this, value, forceCommit);
                   this._selectedValue = value;
                   var options = this.widget.getElementsByTagName("option");
                   for (var i = 0; i < options.length; i++)
                   {
                     options[i].selected = options[i].getAttribute("value") == this._selectedValue;
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
               }
             });

/** 
 * Handles xforms widget xf:select1 with a type of boolean.
 */
dojo.declare("alfresco.xforms.Checkbox",
             alfresco.xforms.Widget,
             function(xform, xformsNode) 
             {
             },
             {
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
                 if (this.isReadonly())
                 {
                   this.widget.setAttribute("disabled", true);
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
                   alfresco.xforms.Checkbox.superclass.setValue.call(this, value, forceCommit);
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
             function(xform, xformsNode) 
             {
               dojo.require("dojo.widget.DatePicker");
               this._minInclusive = (_hasAttribute(this.xformsNode, alfresco.xforms.constants.ALFRESCO_PREFIX + ":minInclusive")
                                     ? this.xformsNode.getAttribute(alfresco.xforms.constants.ALFRESCO_PREFIX + ":minInclusive")
                                     : null);
               this._maxInclusive = (_hasAttribute(this.xformsNode, alfresco.xforms.constants.ALFRESCO_PREFIX + ":maxInclusive")
                                     ? this.xformsNode.getAttribute(alfresco.xforms.constants.ALFRESCO_PREFIX + ":maxInclusive")
                                     : null);

               // XXXarielb - change to a static
               this._noValueSet = (alfresco.xforms.constants.resources["eg"] + " " + 
                                   dojo.date.format(new Date(), 
                                                    {datePattern: alfresco.xforms.constants.DATE_FORMAT, 
                                                     selector: 'dateOnly'}));
             },
             {
               _createPicker: function()
               {
                 var datePickerDiv = document.createElement("div");
                 this.domNode.parentNode.appendChild(datePickerDiv);
                   
                 var dp_initial_value = this.getValue() || null; //dojo.date.toRfc3339(new Date());
                 var datePickerProperties = { value: dp_initial_value };
                 if (this._minInclusive)
                 {
                   datePickerProperties.startDate = this._minInclusive;
                 }
                 if (this._maxInclusive)
                 {
                   datePickerProperties.endDate = this._maxInclusive;
                 }
                 
                 this.widget.picker = dojo.widget.createWidget("DatePicker", 
                                                               datePickerProperties,
                                                               datePickerDiv);
                 this.domContainer.style.height = 
                   Math.max(this.widget.picker.domNode.offsetHeight +
                            this.widget.offsetHeight +
                            dojo.html.getMargin(this.domNode.parentNode).height,
                            20) + "px";

                 dojo.event.connect(this.widget.picker,
                                    "onValueChanged", 
                                    this,
                                    this._datePicker_valueChangedHandler);
               },

               _destroyPicker: function()
               {
                 if (this.widget.picker)
                 {
                   this.domNode.parentNode.removeChild(this.widget.picker.domNode);
                   this.widget.picker = null;
                   this.domContainer.style.height = 
                     Math.max(this.widget.offsetHeight +
                              dojo.html.getMargin(this.domNode.parentNode).height,
                              20) + "px";
                 }
               },
               
               /////////////////////////////////////////////////////////////////
               // overridden methods
               /////////////////////////////////////////////////////////////////

               render: function(attach_point)
               {
                 var initial_value = this.getInitialValue();
                 attach_point.appendChild(this.domNode);
                 this.widget = document.createElement("input");
                 this.widget.setAttribute("id", this.id + "-widget");
                 this.widget.setAttribute("type", "text");
                 if (initial_value)
                 {
                   var jsDate = dojo.date.fromRfc3339(initial_value);
                   this.widget.setAttribute("value", 
                                            dojo.date.format(jsDate,
                                                             {datePattern: alfresco.xforms.constants.DATE_FORMAT, 
                                                              selector: 'dateOnly'}));
                 }
                 else
                 {
                   this.widget.setAttribute("value", this._noValueSet);
                   dojo.html.addClass(this.widget, "xformsGhostText");
                 }
                 if (this.isReadonly())
                 {
                   this.widget.setAttribute("disabled", true);
                 }
                 this.domNode.appendChild(this.widget);

                 var expandoImage = document.createElement("img");
                 expandoImage.setAttribute("src", alfresco.constants.WEBAPP_CONTEXT + "/images/icons/action.gif");
                 expandoImage.align = "absmiddle";
                 expandoImage.style.margin = "0px 5px";

                 this.domNode.appendChild(expandoImage);

                 if (!this.isReadonly())
                 {
                   dojo.event.connect(expandoImage, "onclick", this, this._expando_clickHandler);
                   dojo.event.connect(this.widget, "onfocus", this, this._dateTextBox_focusHandler);
                   dojo.event.connect(this.widget, "onchange", this, this._dateTextBox_changeHandler);
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
                   alfresco.xforms.DatePicker.superclass.setValue.call(this, value, forceCommit);
                   var jsDate = dojo.date.fromRfc3339(value);
                   this.widget.value = dojo.date.format(jsDate,
                                                        {datePattern: alfresco.xforms.constants.DATE_FORMAT, 
                                                         selector: 'dateOnly'});
                   dojo.html.removeClass(this.widget, "xformsGhostText");
                 }
               },

               getValue: function()
               {
                 if (this.widget.value == null || 
                     this.widget.value.length == 0 ||
                     this.widget.value == this._noValueSet)
                 {
                   return null;
                 }
                 else
                 {
                   var jsDate = dojo.date.parse(this.widget.value, 
                                                {datePattern: alfresco.xforms.constants.DATE_FORMAT, 
                                                 selector: 'dateOnly'});
                   return dojo.date.toRfc3339(jsDate, "dateOnly");
                 }
               },

               /////////////////////////////////////////////////////////////////
               // DOM event handlers
               /////////////////////////////////////////////////////////////////

               _dateTextBox_focusHandler: function(event)
               {
                 this._destroyPicker();
               },

               _dateTextBox_changeHandler: function(event)
               {
                 this._commitValueChange();
               },

               _datePicker_valueChangedHandler: function(date)
               {
                 var rfcDate = dojo.date.toRfc3339(date, "dateOnly");
                 this._destroyPicker();
                 this.setValue(rfcDate);
                 this._commitValueChange();
               },

               _expando_clickHandler: function()
               {
                 if (this.widget.picker)
                 {
                   this._destroyPicker();
                 }
                 else
                 {
                   this._createPicker();
                 }
               }
             });

/** The date picker widget which handles xforms widget xf:input with type xf:date */
dojo.declare("alfresco.xforms.TimePicker",
             alfresco.xforms.Widget,
             function(xform, xformsNode) 
             {
               dojo.require("dojo.widget.TimePicker");
               this._noValueSet = (alfresco.xforms.constants.resources["eg"] + " " + 
                                   dojo.date.format(new Date(), 
                                                    {timePattern: alfresco.xforms.constants.TIME_FORMAT, 
                                                     selector: "timeOnly"}));
               this._xformsFormat = "HH:mm:ss.S";
             },
             {
               /** */
               _createPicker: function()
               {
                 var timePickerDiv = document.createElement("div");
                 this.domNode.appendChild(timePickerDiv);
                 var jsDate = (this.getValue()
                               ? dojo.date.parse(this.getValue(),
                                                 {timePattern: this._xformsFormat, 
                                                  selector: "timeOnly"})
                               : new Date());
                 this.widget.picker = dojo.widget.createWidget("TimePicker", 
                                                               { 
                                                                 value: jsDate
                                                               }, 
                                                               timePickerDiv);
                 this.widget.picker.anyTimeContainerNode.innerHTML = "";

                 // don't let it float - it screws up layout somehow
                 this.widget.picker.domNode.style.cssFloat = "none";
                 this.domContainer.style.height = 
                   Math.max(this.widget.picker.domNode.offsetHeight +
                            this.widget.offsetHeight +
                            dojo.html.getMargin(this.domNode.parentNode).height,
                            20) + "px";
                 dojo.event.connect(this.widget.picker,
                                    "onValueChanged", 
                                    this,
                                    this._timePicker_valueChangedHandler);
               },

               _destroyPicker: function()
               {
                 if (this.widget.picker)
                 {
                   this.domNode.removeChild(this.widget.picker.domNode);
                   this.widget.picker = null;
                   this.domContainer.style.height = 
                     Math.max(this.widget.offsetHeight +
                              dojo.html.getMargin(this.domNode.parentNode).height,
                              20) + "px";
                 }
               },

               /////////////////////////////////////////////////////////////////
               // overridden methods
               /////////////////////////////////////////////////////////////////

               render: function(attach_point)
               {
                 var initial_value = this.getInitialValue();
               
                 attach_point.appendChild(this.domNode);
                 this.widget = document.createElement("input");
                 this.widget.setAttribute("id", this.id + "-widget");
                 this.widget.setAttribute("type", "text");
                 if (initial_value)
                 {
                   var jsDate = dojo.date.parse(initial_value, {timePattern: this._xformsFormat, selector: "timeOnly"});
                   this.widget.setAttribute("value",
                                            dojo.date.format(jsDate,
                                                             {timePattern: alfresco.xforms.constants.TIME_FORMAT,
                                                              selector: "timeOnly"}));
                 }
                 else
                 {
                   this.widget.setAttribute("value", this._noValueSet);
                   dojo.html.addClass(this.widget, "xformsGhostText");
                 }
                 if (this.isReadonly())
                 {
                   this.widget.setAttribute("disabled", true);
                 }
                 this.domNode.appendChild(this.widget);

                 var expandoImage = document.createElement("img");
                 expandoImage.setAttribute("src", alfresco.constants.WEBAPP_CONTEXT + "/images/icons/action.gif");
                 expandoImage.align = "absmiddle";
                 expandoImage.style.margin = "0px 5px";

                 this.domNode.appendChild(expandoImage);

                 if (!this.isReadonly())
                 {
                   dojo.event.connect(expandoImage, "onclick", this, this._expando_clickHandler);
                   dojo.event.connect(this.widget, "onfocus", this, this._timeTextBox_focusHandler);
                   dojo.event.connect(this.widget, "onchange", this, this._timeTextBox_changeHandler);
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
                   alfresco.xforms.TimePicker.superclass.setValue.call(this, value, forceCommit);
                   var jsDate = dojo.date.parse(value, {timePattern: this._xformsFormat, selector: "timeOnly"});
                   this.widget.value = dojo.date.format(jsDate,
                                                        {timePattern: alfresco.xforms.constants.TIME_FORMAT,
                                                         selector: "timeOnly"});
                   dojo.html.removeClass(this.widget, "xformsGhostText");
                 }
               },

               getValue: function()
               {
                 if (this.widget.value == null ||
                     this.widget.value.length == 0 ||
                     this.widget.value == this._noValueSet)
                 {
                   return null;
                 }
                 else
                 {
                   var jsDate = dojo.date.parse(this.widget.value,
                                                {timePattern: alfresco.xforms.constants.TIME_FORMAT,
                                                 selector: "timeOnly"});
                   return dojo.date.format(jsDate, {timePattern: this._xformsFormat, selector: "timeOnly"});
                 }
               },

               /////////////////////////////////////////////////////////////////
               // DOM event handlers
               /////////////////////////////////////////////////////////////////
               
               _timeTextBox_focusHandler: function(event)
               {
                 this._destroyPicker();
               },

               _timeTextBox_changeHandler: function(event)
               {
                 this._commitValueChange();
               },

               _timePicker_valueChangedHandler: function(date)
               {
                 var xfDate = dojo.date.format(date, {timePattern: this._xformsFormat, selector: "timeOnly"});
                 this.setValue(xfDate);
                 this._commitValueChange();
               },

               _expando_clickHandler: function()
               {
                 if (this.widget.picker)
                 {
                   this._destroyPicker();
                 }
                 else
                 {
                   this._createPicker();
                 }
               }
             });

/** The date time picker widget which handles xforms widget xf:input with type xf:datetime */
dojo.declare("alfresco.xforms.DateTimePicker",
             alfresco.xforms.Widget,
             function(xform, xformsNode) 
             {
               dojo.require("dojo.widget.DatePicker");
               dojo.require("dojo.widget.TimePicker");
               
               this._noValueSet = (alfresco.xforms.constants.resources["eg"] + " " + 
                                   dojo.date.format(new Date(), 
                                                    {datePattern: alfresco.xforms.constants.DATE_TIME_FORMAT,
                                                     selector: "dateOnly"}));
             },
             {
               /** */
               _createPicker: function()
               {
                 this._pickerDiv = document.createElement("div");
                 this._pickerDiv.style.position = "relative";
                 this._pickerDiv.style.width = this.widget.offsetWidth + "px";
                 this.domNode.appendChild(this._pickerDiv);

                 var datePickerDiv = document.createElement("div");
                 datePickerDiv.style.position = "absolute";
                 datePickerDiv.style.left = "0px";
                 datePickerDiv.style.top = "0px";
                 this._pickerDiv.appendChild(datePickerDiv);

                 var dp_initial_value = this.getValue() || dojo.date.toRfc3339(new Date());
                 this.widget.datePicker = dojo.widget.createWidget("DatePicker",
                                                                   {
                                                                     value: dp_initial_value
                                                                   },
                                                                   datePickerDiv);
                 var timePickerDiv = document.createElement("div");
                 timePickerDiv.style.position = "absolute";
                 timePickerDiv.style.right = "0px";
                 timePickerDiv.style.top = "0px";
                 this._pickerDiv.appendChild(timePickerDiv);

                 var jsDate = this.getValue() ? dojo.date.fromRfc3339(this.getValue()) : new Date();
                 this.widget.timePicker = dojo.widget.createWidget("TimePicker", 
                                                                   { 
                                                                     value: jsDate
                                                                   }, 
                                                                   timePickerDiv);
                 this.widget.timePicker.anyTimeContainerNode.innerHTML = "";

                 // don't let it float - it screws up layout somehow
                 this.widget.timePicker.domNode.style.cssFloat = "none";
                 this._pickerDiv.style.height = Math.max(this.widget.timePicker.domNode.offsetHeight,
                                                         this.widget.datePicker.domNode.offsetHeight);
                 this.domContainer.style.height = 
                   Math.max(this._pickerDiv.offsetHeight +
                            this.widget.offsetHeight +
                            dojo.html.getMargin(this.domNode.parentNode).height,
                            20) + "px";
                 dojo.event.connect(this.widget.datePicker,
                                    "onValueChanged", 
                                    this,
                                    this._datePicker_valueChangedHandler);
                 dojo.event.connect(this.widget.timePicker,
                                    "onValueChanged", 
                                    this,
                                    this._timePicker_valueChangedHandler);
               },

               _destroyPicker: function()
               {
                 if (this._pickerDiv)
                 {
                   this.domNode.removeChild(this._pickerDiv);
                   this.widget.datePicker = null;
                   this.widget.timePicker = null;
                   this._pickerDiv = null;
                   this.domContainer.style.height = 
                     Math.max(this.widget.offsetHeight +
                              dojo.html.getMargin(this.domNode.parentNode).height,
                              20) + "px";
                 }
               },

               /////////////////////////////////////////////////////////////////
               // overridden methods
               /////////////////////////////////////////////////////////////////

               render: function(attach_point)
               {
                 var initial_value = this.getInitialValue();
               
                 attach_point.appendChild(this.domNode);
                 this.widget = document.createElement("input");
                 this.widget.setAttribute("id", this.id + "-widget");
                 this.widget.setAttribute("type", "text");
                 if (initial_value)
                 {
                   var jsDate = dojo.date.fromRfc3339(initial_value);
                   this.widget.setAttribute("value",
                                            dojo.date.format(jsDate,
                                                             {timePattern: alfresco.xforms.constants.DATE_TIME_FORMAT,
                                                              selector: "timeOnly"}));
                 }
                 else
                 {
                   this.widget.setAttribute("value", this._noValueSet);
                   dojo.html.addClass(this.widget, "xformsGhostText");
                 }
                 this.domNode.appendChild(this.widget);
                 this.widget.style.width = (3 * this.widget.offsetWidth) + "px";

                 var expandoImage = document.createElement("img");
                 expandoImage.setAttribute("src", alfresco.constants.WEBAPP_CONTEXT + "/images/icons/action.gif");
                 expandoImage.align = "absmiddle";
                 expandoImage.style.margin = "0px 5px";

                 this.domNode.appendChild(expandoImage);

                 dojo.event.connect(expandoImage, "onclick", this, this._expando_clickHandler);
                 dojo.event.connect(this.widget, "onfocus", this, this._dateTimeTextBox_focusHandler);
                 dojo.event.connect(this.widget, "onchange", this, this._dateTimeTextBox_changeHandler);
               },

               setValue: function(value, forceCommit)
               {
                 if (!this.widget)
                 {
                   this.setInitialValue(value, forceCommit);
                 }
                 else
                 {
                   alfresco.xforms.DateTimePicker.superclass.setValue.call(this, value, forceCommit);
                   var jsDate = dojo.date.fromRfc3339(value);
                   this.widget.value = dojo.date.format(jsDate,
                                                        {datePattern: alfresco.xforms.constants.DATE_TIME_FORMAT,
                                                         selector: "dateOnly"});
                   dojo.html.removeClass(this.widget, "xformsGhostText");
                 }
               },

               getValue: function()
               {
                 if (this.widget.value == null ||
                     this.widget.value.length == 0 ||
                     this.widget.value == this._noValueSet)
                 {
                   return null;
                 }
                 else
                 {
                   var jsDate = dojo.date.parse(this.widget.value,
                                                {datePattern: alfresco.xforms.constants.DATE_TIME_FORMAT,
                                                 selector: "dateOnly"});
                   return dojo.date.toRfc3339(jsDate);
                 }
               },

               /////////////////////////////////////////////////////////////////
               // DOM event handlers
               /////////////////////////////////////////////////////////////////
               
               _dateTimeTextBox_focusHandler: function(event)
               {
                 this._destroyPicker();
               },

               _dateTimeTextBox_changeHandler: function(event)
               {
                 this._commitValueChange();
               },

               _timePicker_valueChangedHandler: function(date)
               {
                 var value = this.getValue() ? dojo.date.fromRfc3339(this.getValue()) : new Date();
                 value.setHours(date.getHours());
                 value.setMinutes(date.getMinutes());
                 value = dojo.date.toRfc3339(value);
                 this.setValue(value);
                 this._commitValueChange();
               },

               _datePicker_valueChangedHandler: function(date)
               {
                 var value = this.getValue() ? dojo.date.fromRfc3339(this.getValue()) : new Date();
                 value.setYear(date.getYear());
                 value.setMonth(date.getMonth());
                 value.setDate(date.getDate());
                 value = dojo.date.toRfc3339(value);
                 this.setValue(value);
                 this._commitValueChange();
               },

               _expando_clickHandler: function()
               {
                 if (this._pickerDiv)
                 {
                   this._destroyPicker();
                 }
                 else
                 {
                   this._createPicker();
                 }
               }
             });

/** The year picker handles xforms widget xf:input with a gYear type */
dojo.declare("alfresco.xforms.YearPicker",
             alfresco.xforms.TextField,
             function(xform, xformsNode) 
             {
             },
             {

               /////////////////////////////////////////////////////////////////
               // overridden methods
               /////////////////////////////////////////////////////////////////

               render: function(attach_point)
               {
                 alfresco.xforms.YearPicker.superclass.render.call(this, attach_point);
                 this.widget.size = "4";
                 this.widget.setAttribute("maxlength", "4");
               },

               getInitialValue: function()
               {
                 var result = alfresco.xforms.YearPicker.superclass.getInitialValue.call(this);
                 return result ? result.replace(/^0*([^0]+)$/, "$1") : result;
               },

               setValue: function(value, forceCommit)
               {
                 alfresco.xforms.YearPicker.superclass.setValue.call(this, 
                                                                     (value 
                                                                      ? value.replace(/^0*([^0]+)$/, "$1") 
                                                                      : null), 
                                                                     forceCommit);
               },

               getValue: function()
               {
                 var result = alfresco.xforms.YearPicker.superclass.getValue.call(this);
                 return result ? dojo.string.padLeft(result, 4, "0") : null;
               }
             });

/** The day picker widget which handles xforms widget xf:input with type xf:gDay */
dojo.declare("alfresco.xforms.DayPicker",
             alfresco.xforms.ComboboxSelect1,
             function(xform, xformsNode)
             {
             },
             {

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
             alfresco.xforms.ComboboxSelect1,
             function(xform, xformsNode)
             {
             },
             {

               /////////////////////////////////////////////////////////////////
               // overridden methods
               /////////////////////////////////////////////////////////////////
               _getItemValues: function()
               {
                 var result = [];
                 result.push({id: "month_empty", label: "", value: "", valid: false});
                 for (var i = 0; i <= 12; i++)
                 {
                   var d = new Date();
                   d.setMonth(i);
                   result.push({
                         id: "month_" + i, 
                         label: dojo.date.getMonthName(d),
                         value: "--" + (i + 1 < 10 ? "0" + (i + 1) : i + 1),
                         valid: true});
                 }
                 return result;
               }
             });

/** The month day picker widget which handles xforms widget xf:input with type xf:gMonthDay */
dojo.declare("alfresco.xforms.MonthDayPicker",
             alfresco.xforms.Widget,
             function(xform, xformsNode)
             {
               this.monthPicker = new alfresco.xforms.MonthPicker(xform, xformsNode);
               this.monthPicker._compositeParent = this;
               
               this.dayPicker = new alfresco.xforms.DayPicker(xform, xformsNode);
               this.dayPicker._compositeParent = this;
             },
             {

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
             function(xform, xformsNode)
             {
               this.yearPicker = new alfresco.xforms.YearPicker(xform, xformsNode);
               this.yearPicker._compositeParent = this;
               
               this.monthPicker = new alfresco.xforms.MonthPicker(xform, xformsNode);
               this.monthPicker._compositeParent = this;
             },
             {

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
dojo.declare("alfresco.xforms.AbstractGroup",
             alfresco.xforms.Widget,
             function(xform, xformsNode) 
             {
               this._children = [];
               dojo.html.removeClass(this.domNode, "xformsItem");
               if (!this.statics._requiredImage)
               {
                 this.statics._requiredImage = document.createElement("img");
                 this.statics._requiredImage.setAttribute("src", 
                                                          alfresco.constants.WEBAPP_CONTEXT + "/images/icons/required_field.gif");
                 this.statics._requiredImage.setAttribute("class", "xformsItemRequiredImage");
               }
             },
             {
               /////////////////////////////////////////////////////////////////
               // methods & properties
               /////////////////////////////////////////////////////////////////

               statics: { _requiredImage: null },

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
                   if (djConfig.isDebug)
                   {
                     dojo.debug(this.id + "[" + i + "]: " + 
                                " is " + this._children[i].id + 
                                " the same as " + child.id + "?");
                   }
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
         
                 child.domContainer = child.domContainer || document.createElement("div");
                 child.domContainer.setAttribute("id", child.id + "-domContainer");
                 dojo.html.addClass(child.domContainer, "xformsItemDOMContainer");


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

                     child.domContainer.group._updateDisplay(false);
                   };
                 anim.play();

                 this._childRemoved(child);

                 return child;
               },

               /** Event handler for when a child has been added. */
               _childAdded: function(child) { },

               /** Event handler for when a child has been removed. */
               _childRemoved: function(child) { },

               /** Utility function to create the a label container */
               _createLabelContainer: function(child)
               {
                 var labelNode = document.createElement("span");
                 labelNode.setAttribute("id", child.id + "-label");
                 labelNode.style.position = "relative";
                 labelNode.style.left = "0px";
                 child.domContainer.appendChild(labelNode);

                 var requiredImage = this.statics._requiredImage.cloneNode(true);            
                 labelNode.appendChild(requiredImage);

                 if (!child.isRequired())
                 {
                   requiredImage.style.visibility = "hidden";
                 }
                 var label = child.getLabel();
                 if (label)
                 {
                   child.labelNode = document.createElement("span");
                   child.labelNode.style.verticalAlign = "middle";
                   child.labelNode.style.marginRight = "5px";
                   labelNode.appendChild(child.labelNode);
                   child.labelNode.appendChild(document.createTextNode(label));

                 }
                 var hint = child.getHint();
                 if (hint)
                 {
                   labelNode.setAttribute("title", hint);
                   requiredImage.setAttribute("alt", hint);
                 }

                 return labelNode;
               },

               /////////////////////////////////////////////////////////////////
               // overridden methods & properties
               /////////////////////////////////////////////////////////////////

               isValidForSubmit: function()
               {
                 return true;
               },

               /** Iterates all children a produces an array of widgets which are invalid for submit. */
               getWidgetsInvalidForSubmit: function()
               {
                 var result = [];
                 for (var i = 0; i < this._children.length; i++)
                 {
                   if (this._children[i] instanceof alfresco.xforms.AbstractGroup)
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
                 alfresco.xforms.AbstractGroup.superclass._destroy.call(this);
                 for (var i = 0; i < this._children.length; i++)
                 {
                   this._children[i]._destroy();
                 }
               },

               setReadonly: function(readonly)
               {
                 alfresco.xforms.AbstractGroup.superclass.setReadonly.call(this, readonly);
                 for (var i = 0; i < this._children.length; i++)
                 {
                   this._children[i].setReadonly(readonly);
                 }
               },

               render: function(attach_point)
               {
                 this.domNode.widget = this;
                 return this.domNode;
               },

               _updateDisplay: function(recursively)
               {
                   if (recursively)
                   {
                     this._children[i]._updateDisplay(recursively);
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
               }
             });

/** 
 * Handles xforms widget xf:group.  A group renders and manages a set of children
 * and provides a header for expanding and collapsing the group.  A group header
 * is shown for all group that don't have xf:appearance set to 'repeated' and 
 * that are not the root group.
 */
dojo.declare("alfresco.xforms.VGroup",
             alfresco.xforms.AbstractGroup,
             function(xform, xformsNode) 
             {
             },
             {
               /////////////////////////////////////////////////////////////////
               // methods & properties
               /////////////////////////////////////////////////////////////////

               _groupHeaderNode: null,

               /** Inserts a child at the specified position. */
               _insertChildAt: function(child, position)
               {
                 if (!this.domNode.childContainerNode.parentNode)
                 {
                   // only add this to the dom once we're adding a child
                   this.domNode.appendChild(this.domNode.childContainerNode);
                 }

                 child.domContainer = 
                   alfresco.xforms.VGroup.superclass._insertChildAt.call(this, child, position);

                 if (this.parent && this.parent.domNode)
                 {
                   child.domContainer.style.top = this.parent.domNode.style.bottom;
                 }
         
                 function shouldInsertDivider(group, child, position)
                 {
                   if (group.getAppearance() != "full")
                   {
                     return false;
                   }
                   if (group instanceof alfresco.xforms.Repeat)
                   {
                     return false;
                   }

                   if (!child.isVisible())
                   {
                     return false;
                   }
                   if (group._children[position - 1] instanceof alfresco.xforms.AbstractGroup)
                   {
                     return true;
                   }
                   if (child instanceof alfresco.xforms.AbstractGroup)
                   {
                     for (var i = position - 1; i > 0; i--)
                     {
                       if (group._children[i].isVisible())
                       {
                         return true;
                       }
                     }
                   }
                   return false;
                 }

                 if (shouldInsertDivider(this, child, position))
                 {
                   var divider = document.createElement("div");
                   dojo.html.setClass(divider, "xformsGroupDivider");
                   this.domNode.childContainerNode.insertBefore(divider,
                                                                child.domContainer);
                 }

                 var labelNode = null;
                 if (!(child instanceof alfresco.xforms.AbstractGroup))
                 {
                   labelNode = this._createLabelContainer(child);
                   child.domContainer.appendChild(labelNode);
                 }

                 var contentDiv = document.createElement("div");
                 contentDiv.setAttribute("id", child.id + "-content");
                 dojo.html.setClass(contentDiv, "xformsGroupItem");
                 child.domContainer.appendChild(contentDiv);


                 contentDiv.style.left = (child instanceof alfresco.xforms.AbstractGroup 
                                          ? "0px" 
                                          : "30%");

                 contentDiv.style.width = (child instanceof alfresco.xforms.AbstractGroup
                                           ? "100%"
                                           : (1 - (contentDiv.offsetLeft / 
                                                   child.domContainer.offsetWidth)) * 100 + "%");
                 child.render(contentDiv);
                 if (!(child instanceof alfresco.xforms.AbstractGroup))
                 {
                   child.domContainer.style.height = 
                     Math.max(contentDiv.offsetHeight + 
                              dojo.html.getMargin(contentDiv).height, 20) + "px";
                 }

                 dojo.debug(contentDiv.getAttribute("id") + " offsetTop is " + contentDiv.offsetTop);
                 contentDiv.style.top = "-" + Math.max(0, contentDiv.offsetTop - 
                                                       dojo.html.getPixelValue(contentDiv, "margin-top")) + "px";
                 if (labelNode)
                 {
                   labelNode.style.top = (contentDiv.offsetTop + ((.5 * contentDiv.offsetHeight) -
                                                                 (.5 * labelNode.offsetHeight))) + "px";
                 }
                 contentDiv.widget = child;

                 this._updateDisplay(false);
                 this._childAdded(child);
                 return child.domContainer;
               },

               /////////////////////////////////////////////////////////////////
               // overridden methods & properties
               /////////////////////////////////////////////////////////////////

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

                 this.domNode.style.marginLeft = 10 + "px";

                 if (this.getAppearance() == "full")
                 {
                   dojo.html.setClass(this.domNode, "xformsGroup");
                   this.domNode.style.position = "relative";
                   this.domNode.style.marginRight = (parseInt(this.domNode.style.marginLeft) / 3) + "px";
                   if (dojo.render.html.ie)
                   {
                     this.domNode.style.width = "100%";
                   }
                   else
                   {
                     this.domNode.style.width = (1 - ((dojo.html.getBorder(this.domNode).width +
                                                       dojo.html.getPadding(this.domNode).width +
                                                       dojo.html.getMargin(this.domNode).width) /
                                                      attach_point.offsetWidth)) * 100 + "%";
                   }

                   this._groupHeaderNode = document.createElement("div");
                   this._groupHeaderNode.setAttribute("id", this.id + "-groupHeaderNode");
                   dojo.html.setClass(this._groupHeaderNode, "xformsGroupHeader");
                   this.domNode.appendChild(this._groupHeaderNode);

                   this.toggleExpandedImage = document.createElement("img");
                   this._groupHeaderNode.appendChild(this.toggleExpandedImage);
                   this.toggleExpandedImage.setAttribute("src", alfresco.xforms.constants.EXPANDED_IMAGE.src);
                   this.toggleExpandedImage.align = "absmiddle";
                   this.toggleExpandedImage.style.marginLeft = "5px";
                   this.toggleExpandedImage.style.marginRight = "5px";
                   
                   dojo.event.connect(this.toggleExpandedImage, 
                                      "onclick", 
                                      this, 
                                      this._toggleExpanded_clickHandler);
                   
                   this._groupHeaderNode.appendChild(document.createTextNode(this.getLabel()));
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
                         alfresco.xforms.constants.EXPANDED_IMAGE.src);
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
                      ? alfresco.xforms.constants.EXPANDED_IMAGE.src 
                      : alfresco.xforms.constants.COLLAPSED_IMAGE.src);
                   this.domNode.childContainerNode.style.display = expanded ? "block" : "none";
                 }
               },

               _updateDisplay: function(recursively)
               {
                 if (dojo.render.html.ie)
                 {
                   this.domNode.style.width = "100%";
                 }
                 else
                 {
                   this.domNode.style.width = (1 - ((dojo.html.getBorder(this.domNode).width +
                                                     dojo.html.getPadding(this.domNode).width +
                                                     dojo.html.getMargin(this.domNode).width) /
                                                    this.domNode.parentNode.offsetWidth)) * 100 + "%";
                 }

                 for (var i = 0; i < this._children.length; i++)
                 {
                   var contentDiv = document.getElementById(this._children[i].id + "-content");

                   contentDiv.style.position = "static";
                   contentDiv.style.top = "0px";
                   contentDiv.style.left = "0px";

                   contentDiv.style.position = "relative";
                   contentDiv.style.left = (this._children[i] instanceof alfresco.xforms.AbstractGroup
                                            ? "0px"
                                            : "30%");
                   contentDiv.style.width = (this._children[i] instanceof alfresco.xforms.AbstractGroup
                                             ? "100%"
                                             : (1 - (contentDiv.offsetLeft / 
                                                     this._children[i].domContainer.parentNode.offsetWidth)) * 100 + "%");

                   if (recursively)
                   {
                     this._children[i]._updateDisplay(recursively);
                   }

                   if (!(this._children[i] instanceof alfresco.xforms.AbstractGroup))
                   {
                     this._children[i].domContainer.style.height =
                       Math.max(contentDiv.offsetHeight +
                                dojo.html.getMargin(contentDiv).height, 20) + "px";
                   }

                   contentDiv.style.top = "-" + Math.max(0, contentDiv.offsetTop -
                                                         dojo.html.getPixelValue(contentDiv, "margin-top")) + "px";

                   var labelNode = document.getElementById(this._children[i].id + "-label");
                   if (labelNode)
                   {
                     labelNode.style.position = "static";
                     labelNode.style.top = "0px";
                     labelNode.style.left = "0px";
                     labelNode.style.position = "relative";

                     labelNode.style.top = (contentDiv.offsetTop + ((.5 * contentDiv.offsetHeight) -
                                                                   (.5 * labelNode.offsetHeight))) + "px";
                   }
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

/** 
 * Handles xforms widget xf:group.  A group renders and manages a set of children
 * and provides a header for expanding and collapsing the group.  A group header
 * is shown for all group that don't have xf:appearance set to 'repeated' and 
 * that are not the root group.
 */
dojo.declare("alfresco.xforms.HGroup",
             alfresco.xforms.AbstractGroup,
             function(xform, xformsNode) 
             {
             },
             {
               /////////////////////////////////////////////////////////////////
               // methods & properties
               /////////////////////////////////////////////////////////////////

               /** Inserts a child at the specified position. */
               _insertChildAt: function(child, position)
               {
                 child.domContainer = document.createElement("td");
                 child.domContainer.style.marginRight = "5px";
                 child.domContainer = 
                   alfresco.xforms.HGroup.superclass._insertChildAt.call(this, child, position);

                 var labelNode = this._createLabelContainer(child);
                 child.domContainer.appendChild(labelNode);

                 var contentDiv = document.createElement("div");
                 contentDiv.setAttribute("id", child.id + "-content");
                 dojo.html.setClass(contentDiv, "xformsGroupItem");
                 child.domContainer.appendChild(contentDiv);
                 contentDiv.style.left = labelNode.offsetWidth + "px";

                 child.render(contentDiv);
                 var childDomNodeWidth = (child.domNode.offsetWidth + 
                                          dojo.html.getMargin(child.domNode).width + 
                                          dojo.html.getBorder(child.domNode).width);
                 if (childDomNodeWidth < contentDiv.offsetWidth)
                 {
                   // if we don't have a greedy widget (width 100%) try to constrain the cells 
                   // width so we can fit as much as possible on a row
                   contentDiv.style.width = childDomNodeWidth + "px";
                   child.domContainer.style.width = (contentDiv.offsetWidth + labelNode.offsetWidth) + "px";
                 }
                 if (!(child instanceof alfresco.xforms.AbstractGroup))
                 {
                   child.domContainer.style.height = 
                     Math.max(contentDiv.offsetHeight + 
                              dojo.html.getMargin(contentDiv).height, 20) + "px";
                 }

                 dojo.debug(contentDiv.getAttribute("id") + " offsetTop is " + contentDiv.offsetTop);
                 contentDiv.style.top = "-" + Math.max(0, contentDiv.offsetTop - 
                                                       dojo.html.getPixelValue(contentDiv, "margin-top")) + "px";
                 if (labelNode)
                 {
                   labelNode.style.top = (contentDiv.offsetTop + ((.5 * contentDiv.offsetHeight) -
                                                                 (.5 * labelNode.offsetHeight))) + "px";
                 }
                 contentDiv.widget = child;

                 this._updateDisplay(false);
                 this._childAdded(child);
                 return child.domContainer;
               },

               /////////////////////////////////////////////////////////////////
               // overridden methods & properties
               /////////////////////////////////////////////////////////////////

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

                 this.domNode.style.marginLeft = 10 + "px";
                 this.domNode.style.position = "relative";
                 this.domNode.style.marginRight = (parseInt(this.domNode.style.marginLeft) / 3) + "px";

                 attach_point.appendChild(this.domNode);

                 var ccnode = document.createElement("table");
                 ccnode.style.width = "100%";
                 ccnode.setAttribute("cellpadding", "0px");
                 ccnode.setAttribute("cellspacing", "0px");
                 this.domNode.appendChild(ccnode);
                 this.domNode.childContainerNode = document.createElement("tr");
                 ccnode.appendChild(this.domNode.childContainerNode);
                 
                 this.domNode.childContainerNode.setAttribute("id", this.id + "-childContainerNode");
                 this.domNode.childContainerNode.style.position = "relative";
                 this.domNode.childContainerNode.style.width = "100%";

                 var me = this;
                 dojo.event.browser.addListener(window,
                                                "onresize",
                                                function() { me._updateDisplay(false) }, 
                                                false);

                 return this.domNode;
               },

               _updateDisplay: function(recursively)
               {
                 if (dojo.render.html.ie)
                 {
                   this.domNode.style.width = "100%";
                 }
                 else
                 {
                   this.domNode.style.width = (1 - ((dojo.html.getBorder(this.domNode).width +
                                                     dojo.html.getPadding(this.domNode).width +
                                                     dojo.html.getMargin(this.domNode).width) /
                                                    this.domNode.parentNode.offsetWidth)) * 100 + "%";
                 }
                 for (var i = 0; i < this._children.length; i++)
                 {
                   var contentDiv = document.getElementById(this._children[i].id + "-content");
                   var labelNode = document.getElementById(this._children[i].id + "-label");
                   contentDiv.style.left = labelNode.offsetWidth + "px";
                   contentDiv.style.width = (((this._children[i].domContainer.offsetWidth - labelNode.offsetWidth) /
                                              this._children[i].domContainer.offsetWidth) * 100) + "%";
                   if (recursively)
                   {
                     this._children[i]._updateDisplay(recursively);
                   }
                 }
               }
             });

dojo.declare("alfresco.xforms.SwitchGroup",
             alfresco.xforms.VGroup,
             function(xform, xformsNode)
             {
               if (this.getInitialValue())
               {
                 var initialValueTrigger = this._getCaseToggleTriggerByTypeValue(this.getInitialValue());
                 this._selectedCaseId = initialValueTrigger.getActions()["toggle"].properties["case"];
               }
             },
             {
               /////////////////////////////////////////////////////////////////
               // methods & properties
               /////////////////////////////////////////////////////////////////
               _getCaseToggleTriggers: function()
               {
                 var bw = this.xform.getBinding(this.xformsNode).widgets;
                 var result = [];
                 for (var i in bw)
                 {
                   if (! (bw[i] instanceof alfresco.xforms.Trigger))
                   {
                     continue;
                   }
                   
                   var action = bw[i].getActions()["toggle"];
                   if (action)
                   {
                     result.push(bw[i]);
                   }
                 }
                 return result;
               },

               _getCaseToggleTriggerByCaseId: function(caseId)
               {
                 var bw = this.xform.getBinding(this.xformsNode).widgets;
                 for (var i in bw)
                 {
                   if (! (bw[i] instanceof alfresco.xforms.Trigger))
                   {
                     continue;
                   }
                   
                   var action = bw[i].getActions()["toggle"];
                   if (!action)
                   {
                     continue;
                   }
                   if (action.properties["case"] == caseId)
                   {
                     return bw[i];
                   }
                 }
                 throw new Error("unable to find trigger " + type + 
                                 ", properties " + properties +
                                 " for " + this.id);

               },

               _getCaseToggleTriggerByTypeValue: function(typeValue)
               {
                 var bw = this.xform.getBinding(this.xformsNode).widgets;
                 for (var i in bw)
                 {
                   if (! (bw[i] instanceof alfresco.xforms.Trigger))
                   {
                     continue;
                   }
                   
                   var action = bw[i].getActions()["setvalue"];
                   if (!action)
                   {
                     continue;
                   }
                   if (action.properties["value"] == typeValue)
                   {
                     return bw[i];
                   }
                 }
                 throw new Error("unable to find toggle trigger for type value " + typeValue + 
                                 " for " + this.id);
               },

               /////////////////////////////////////////////////////////////////
               // overridden methods & properties
               /////////////////////////////////////////////////////////////////

               /** */
               _insertChildAt: function(child, position)
               {
                 var childDomContainer = 
                   alfresco.xforms.SwitchGroup.superclass._insertChildAt.call(this, child, position);
                 if (child.id == this._selectedCaseId)
                 {
                   this._getCaseToggleTriggerByCaseId(this._selectedCaseId).fire();
                 }
                 else
                 {
                   childDomContainer.style.display = "none";
                 }
                 return childDomContainer;
               },

               render: function(attach_point)
               {
                 alfresco.xforms.SwitchGroup.superclass.render.call(this, attach_point);
                 var cases = this._getCaseToggleTriggers();
                 var caseToggleSelect = document.createElement("select");
                 caseToggleSelect.setAttribute("id", this.id + "-toggle-select");
                 caseToggleSelect.style.position = "absolute";
                 caseToggleSelect.style.right = "0px";
                 caseToggleSelect.style.top = "0px";
                 this._groupHeaderNode.appendChild(caseToggleSelect);
                 for (var i = 0; i < cases.length; i++)
                 {
                   var option = document.createElement("option");
                   caseToggleSelect.appendChild(option);
                   var caseId = cases[i].getActions()["toggle"].properties["case"];
                   option.setAttribute("value", caseId);
                   option.appendChild(document.createTextNode(cases[i].getLabel()));
                   if (cases[i].getActions()["toggle"].properties["case"] == this._selectedCaseId)
                   {
                     option.selected = true;
                   }
                 }
                 dojo.event.connect(caseToggleSelect, "onchange", this, this._caseToggleSelect_changeHandler);
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
                 this._updateDisplay(true);
               },
               
               /////////////////////////////////////////////////////////////////
               // DOM event handlers
               /////////////////////////////////////////////////////////////////

               _caseToggleSelect_changeHandler: function(event)
               {
                 dojo.event.browser.stopEvent(event);
                 var t = this._getCaseToggleTriggerByCaseId(event.target.value);
                 t.fire();
               }
             });

/** */
dojo.declare("alfresco.xforms.CaseGroup",
             alfresco.xforms.VGroup,
             function(xform, xformsNode)
             {
             },
             {
             });

/** 
 * Handles xforms widget xf:group for the root group.  Does some special rendering
 * to present a title rather than a group header.
 */
dojo.declare("alfresco.xforms.ViewRoot",
             alfresco.xforms.VGroup,
             function(xform, xformsNode) 
             {
               this.focusedRepeat = null;
             },
             {

               /////////////////////////////////////////////////////////////////
               // overridden methods & properties
               /////////////////////////////////////////////////////////////////

               render: function(attach_point)
               {
                 this.domNode.widget = this;
                 this.domNode.style.position = "relative";
                 this.domNode.style.width = "100%";
                 dojo.html.setClass(this.domNode, "xformsViewRoot");

                 this._groupHeaderNode = document.createElement("div");
                 this._groupHeaderNode.id = this.id + "-groupHeaderNode";
                 dojo.html.setClass(this._groupHeaderNode, "xformsViewRootHeader");
                 this.domNode.appendChild(this._groupHeaderNode);

                 var icon = document.createElement("img");
                 this._groupHeaderNode.appendChild(icon);
                 icon.setAttribute("src", alfresco.constants.WEBAPP_CONTEXT + "/images/icons/file_large.gif");
                 icon.align = "absmiddle";
                 icon.style.marginLeft = "5px";
                 icon.style.marginRight = "5px";
                 this._groupHeaderNode.appendChild(document.createTextNode(this.getLabel()));
                 attach_point.appendChild(this.domNode);

                 this.domNode.childContainerNode = document.createElement("div");
                 this.domNode.childContainerNode.setAttribute("id", this.id + "-childContainerNode");
                 this.domNode.childContainerNode.style.position = "relative";
                 this.domNode.childContainerNode.style.width = "100%";

                 return this.domNode;
               },
                 
               /** */
               getLabel: function()
               {
                 var result = alfresco.xforms.ViewRoot.superclass.getLabel.call(this);
                 result += " " + alfresco.xforms.constants.FORM_INSTANCE_DATA_NAME;
                 return result;
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
             alfresco.xforms.VGroup,
             function(xform, xformsNode) 
             {
               this.repeatControls = [];
               this._selectedIndex = -1;
               this._locked = false;
             },
             {
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

                   var action = bw[i].getActions()[type];
                   if (!action)
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
                   oldFocusedRepeat._updateDisplay(false);
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
                   dojo.html.setOpacity(this.repeatControls[i].moveRepeatItemUpImage,
                                        i == 0 ? .3 : 1);
                   dojo.html.setOpacity(this.repeatControls[i].moveRepeatItemDownImage, 
                                        i == this.repeatControls.length - 1 ? .3 : 1);
                   dojo.html.setOpacity(this.repeatControls[i].insertRepeatItemImage,
                                        insertEnabled ? 1 : .3);
                   dojo.html.setOpacity(this.repeatControls[i].removeRepeatItemImage,
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
                   img.setAttribute("src", (alfresco.constants.WEBAPP_CONTEXT + "/images/icons/" + 
                                            images[i].src + ".gif"));
                   img.style.width = "16px";
                   img.style.height = "16px";
                   img.style.margin = "2px 5px 2px " + (i == 0 ? 5 : 0) + "px";
                   img.repeat = this;
                   repeatControlsWidth += (parseInt(img.style.width) + 
                                           dojo.html.getMargin(img).width);
                   this.repeatControls[position].appendChild(img);
                   dojo.event.connect(img, "onclick", this, images[i].action);
                 }

                 var result = alfresco.xforms.Repeat.superclass._insertChildAt.call(this, child, position);
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
                                                             dojo.html.getPixelValue(result, "margin-bottom") +
                                                             dojo.html.getBorderExtent(result, "bottom")) + "px";
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
                 return alfresco.xforms.Repeat.superclass._removeChildAt.call(this, position);
               },

               /** Disables insert before. */
               _childAdded: function(child)
               {
                 dojo.html.setOpacity(this.headerInsertRepeatItemImage, .3);
                 this._updateRepeatControls();
               },

               /** Reenables insert before if there are no children left. */
               _childRemoved: function(child)
               {
                 if (this._children.length == 0)
                 {
                   dojo.html.setOpacity(this.headerInsertRepeatItemImage, 1);
                 }
                 this._updateRepeatControls();
               },

               render: function(attach_point)
               {
                 this.domNode = alfresco.xforms.Repeat.superclass.render.call(this, attach_point);
                 dojo.html.addClass(this.domNode, "xformsRepeat");

                 // clear the border bottom for the group header since we'll be getting it
                 // from the repeat item border
                 this._groupHeaderNode.style.borderBottomWidth = "0px";

                 this._groupHeaderNode.repeat = this;
                 dojo.event.connect(this._groupHeaderNode, "onclick", function(event)
                                    {
                                      if (event.target == event.currentTarget)
                                      {
                                        event.currentTarget.repeat.setFocusedChild(null);
                                      }
                                    });
               
                 this.headerInsertRepeatItemImage = document.createElement("img"); 
                 this.headerInsertRepeatItemImage.repeat = this;
                 this._groupHeaderNode.appendChild(this.headerInsertRepeatItemImage);
                 this.headerInsertRepeatItemImage.setAttribute("src", 
                                                               alfresco.constants.WEBAPP_CONTEXT + 
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

               _updateDisplay: function(recursively)
               {
                 alfresco.xforms.Repeat.superclass._updateDisplay.call(this, recursively);
                 if (this.getViewRoot().focusedRepeat != null &&
                     (this.getViewRoot().focusedRepeat == this ||
                      this.getViewRoot().focusedRepeat.isAncestorOf(this)))
                 {
                   if (!dojo.html.hasClass(this._groupHeaderNode, "xformsRepeatFocusedHeader"))
                   {
                     dojo.html.addClass(this._groupHeaderNode, "xformsRepeatFocusedHeader");
                   }
                 }
                 else if (dojo.html.hasClass(this._groupHeaderNode, "xformsRepeatFocusedHeader"))
                 {
                   dojo.html.removeClass(this._groupHeaderNode, "xformsRepeatFocusedHeader");
                 }

                 for (var i = 0; i < this._children.length; i++)
                 {
                   var domContainerClasses = dojo.html.getClasses(this._children[i].domContainer);
                   if (i + 1 == this.getSelectedIndex() && this.getViewRoot().focusedRepeat == this)
                   {
                     if (domContainerClasses.indexOf("xformsRowOdd") >= 0)
                     {
                       dojo.html.removeClass(this._children[i].domContainer, "xformsRowOdd");
                     }
                     if (domContainerClasses.indexOf("xformsRowEven") >= 0)
                     {
                       dojo.html.removeClass(this._children[i].domContainer, "xformsRowEven");
                     }
                     if (domContainerClasses.indexOf("xformsRepeatItemSelected") < 0)
                     {
                       dojo.html.addClass(this._children[i].domContainer, "xformsRepeatItemSelected");
                     }
                   }
                   else
                   {
                     if (domContainerClasses.indexOf("xformsRepeatItemSelected") >= 0)
                     {
                       dojo.html.removeClass(this._children[i].domContainer, "xformsRepeatItemSelected");
                     }
                     if (domContainerClasses.indexOf("xformsRow" + (i % 2 ? "Odd" : "Even")) >= 0)
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
                 if (!repeat._locked && repeat.isInsertRepeatItemEnabled())
                 {
                   var index = repeat.repeatControls.indexOf(event.target.parentNode);
                   var repeatItem = repeat.getChildAt(index);
                   this.setFocusedChild(repeatItem);
                   var trigger = this._getRepeatItemTrigger("insert", { position: "after" });
                   trigger.fire();
                   repeat._locked = true;
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
                   if (!repeat._locked && repeat.isInsertRepeatItemEnabled())
                   {
                     this.setFocusedChild(null);
                     var trigger = this._getRepeatItemTrigger("insert", { position: "before" });
                     trigger.fire();
                     repeat._locked = true;
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
                 if (!repeat._locked && repeat.isRemoveRepeatItemEnabled())
                 {
                   var index = repeat.repeatControls.indexOf(event.target.parentNode);
                   var repeatItem = repeat.getChildAt(index);
                   this.setFocusedChild(repeatItem);
                   var trigger = this._getRepeatItemTrigger("delete", {});
                   trigger.fire();
                   repeat._locked = true;
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
                 if (!repeat._locked && index != 0 && repeat._children.length != 1)
                 {
                   var repeatItem = repeat.getChildAt(index);
                   this.setFocusedChild(repeatItem);
                   repeat._swapChildren(index, index - 1);
                   repeat._locked = true;
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
                 if (!repeat._locked && index != repeat._children.length - 1 && repeat._children.length != 1)
                 {
                   var repeatItem = repeat.getChildAt(index);
                   this.setFocusedChild(repeatItem);
                   repeat._swapChildren(index, index + 1);
                   repeat._locked = true;
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
                 this._updateDisplay(false);
               },

               /** Returns a clone of the specified prototype id. */
               handlePrototypeCloned: function(prototypeId)
               {
                 dojo.debug(this.id + ".handlePrototypeCloned("+ prototypeId +")");
                 var chibaData = _getElementsByTagNameNS(this.xformsNode, 
                                                         alfresco.xforms.constants.CHIBA_NS,
                                                         alfresco.xforms.constants.CHIBA_PREFIX,
                                                         "data");
                 chibaData = chibaData[chibaData.length - 1];
                 if (djConfig.isDebug)
                 {
                   dojo.debug(alfresco.xforms.constants.CHIBA_PREFIX + 
                              ":data == " + dojo.dom.innerXML(chibaData));
                 }
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
                 this._locked = false;
               },

               /** Deletes the item at the specified position. */
               handleItemDeleted: function(position)
               {
                 dojo.debug(this.id + ".handleItemDeleted(" + position + ")");
                 this._removeChildAt(position);
                 this._locked = false;
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
             function(xform, xformsNode) 
             {
             },
             {
               /////////////////////////////////////////////////////////////////
               // methods & properties
               /////////////////////////////////////////////////////////////////

               /** TODO: DOCUMENT */
               getActions: function()
               {
                 if (typeof this._actions == "undefined")
                 {
                   var actionNode = _getElementsByTagNameNS(this.xformsNode, 
                                                            alfresco.xforms.constants.XFORMS_NS,
                                                            alfresco.xforms.constants.XFORMS_PREFIX,
                                                            "action")[0];
                   this._actions = {};
                   for (var i = 0; i < actionNode.childNodes.length; i++)
                   {
                     if (actionNode.childNodes[i].nodeType != dojo.dom.ELEMENT_NODE)
                     {
                       continue;
                     }

                     var a = new alfresco.xforms.XFormsAction(this.xform, actionNode.childNodes[i]);
                     this._actions[a.getType()] = a;
                   }
                 }
                 return this._actions;
               },
               
               /** fires the xforms action associated with the trigger */
               fire: function()
               {
                 this.xform.fireAction(this.id);
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
                 attach_point.appendChild(this.domNode);
                 this.widget = document.createElement("input");
                 this.widget.setAttribute("type", "submit");
                 this.widget.setAttribute("id", this.id + "-widget");
                 this.widget.setAttribute("value", this.getLabel() + " " + this.id);
                 dojo.event.connect(this.widget, "onclick", this, this._clickHandler);
                 this.domContainer.style.display = "none";
               },

               /////////////////////////////////////////////////////////////////
               // DOM event handlers
               /////////////////////////////////////////////////////////////////
               _clickHandler: function(event)
               {
                 this.fire();
               }
             });

/** 
 * Handles xforms widget xf:submit.
 */
dojo.declare("alfresco.xforms.Submit",
             alfresco.xforms.Trigger,
             function(xform, xformsNode) 
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
                                                this._submitButton_clickHandler,
                                                false);
               }
             },
             {

               /////////////////////////////////////////////////////////////////
               // DOM event handlers
               /////////////////////////////////////////////////////////////////

               _clickHandler: function(event)
               {
                 this.done = false;
                 _hide_errors();
                 this.fire();
               },

               /** */
               _submitButton_clickHandler: function(event)
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
                   xform.submitWidget.fire();
                   return false;
                 }
               }
             });

/**
 * A struct describing an xforms action block.
 */
dojo.declare("alfresco.xforms.XFormsAction",
             null,
             function(xform, xformsNode)
             {
               this.xform = xform;
               this.xformsNode = xformsNode;
               /** All properties of the action as map of key value pairs */
               this.properties = [];
               for (var i = 0; i < this.xformsNode.attributes.length; i++)
               {
                 var attr = this.xformsNode.attributes[i];
                 if (attr.nodeName.match(new RegExp("^" + alfresco.xforms.constants.XFORMS_PREFIX + ":")))
                 {
                   this.properties[attr.nodeName.substring((alfresco.xforms.constants.XFORMS_PREFIX + ":").length)] = 
                     attr.nodeValue;
                 }
               }
               if (this.getType() == "setvalue" && !this.properties["value"])
               {
                 this.properties["value"] = this.xformsNode.firstChild.nodeValue;
               }
             },
             {
               /** Returns the action type. */
               getType: function()
               {
                 return this.xformsNode.nodeName.substring((alfresco.xforms.constants.XFORMS_PREFIX + ":").length);
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
             function(node)
             {
               this.type = node.nodeName;
               this.targetId = node.getAttribute("targetId");
               this.targetName = node.getAttribute("targetName");
               this.properties = {};
               for (var i = 0; i < node.childNodes.length; i++)
               {
                 if (node.childNodes[i].nodeType == dojo.dom.ELEMENT_NODE)
                 {
                   this.properties[node.childNodes[i].getAttribute("name")] =
                     node.childNodes[i].getAttribute("value");
                 }
               }
             },
             {
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
             function(xformsNode, parent)
             {
               this.xformsNode = xformsNode;
               this.id = this.xformsNode.getAttribute("id");
               this.nodeset =  this.xformsNode.getAttribute(alfresco.xforms.constants.XFORMS_PREFIX + ":nodeset");
               this._readonly =
                 (_hasAttribute(this.xformsNode, alfresco.xforms.constants.XFORMS_PREFIX + ":readonly")
                  ? this.xformsNode.getAttribute(alfresco.xforms.constants.XFORMS_PREFIX + ":readonly") == "true()"
                  : null);
               this._required =
                 (_hasAttribute(this.xformsNode, alfresco.xforms.constants.XFORMS_PREFIX + ":required")
                  ? this.xformsNode.getAttribute(alfresco.xforms.constants.XFORMS_PREFIX + ":required") == "true()"
                  : null);
               this._type =
                 (_hasAttribute(this.xformsNode, alfresco.xforms.constants.XFORMS_PREFIX + ":type")
                  ? this.xformsNode.getAttribute(alfresco.xforms.constants.XFORMS_PREFIX + ":type")
                  : null);
               this.constraint = 
                 (_hasAttribute(this.xformsNode, alfresco.xforms.constants.XFORMS_PREFIX + ":constraint")
                  ? this.xformsNode.getAttribute(alfresco.xforms.constants.XFORMS_PREFIX + ":constraint")
                  : null);
               this.maximum = this.xformsNode.getAttribute(alfresco.xforms.constants.XFORMS_PREFIX + ":maxOccurs");
               this.maximum = this.maximum == "unbounded" ? Number.MAX_VALUE : parseInt(this.maximum);
               this.minimum = parseInt(this.xformsNode.getAttribute(alfresco.xforms.constants.XFORMS_PREFIX + ":minOccurs"));
               this.parent = parent;
               this.widgets = {};
             },
             {
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
             /** Makes a request to the XFormsBean to load the xforms document. */
             function()
             {
               var req = alfresco.AjaxHelper.createRequest(this,
                                                           "getXForm",
                                                           {},
                                                           function(type, data, evt, kwArgs) 
                                                           {
                                                             this.target._loadHandler(data);
                                                           });
               alfresco.AjaxHelper.sendRequest(req);
             },
             {
               /////////////////////////////////////////////////////////////////
               // Initialization
               /////////////////////////////////////////////////////////////////

               /** Parses the xforms document and produces the widget tree. */
               _loadHandler: function(xformDocument)
               {
                 this.xformDocument = xformDocument;
                 this.xformsNode = xformDocument.documentElement;
                 this._bindings = this._loadBindings(this.getModel());
               
                 var bindings = this.getBindings();
                 var alfUI = document.getElementById(alfresco.xforms.constants.XFORMS_UI_DIV_ID);
                 alfUI.style.width = "100%";
                 var rootGroup = _getElementsByTagNameNS(this.getBody(),
                                                         alfresco.xforms.constants.XFORMS_NS,
                                                         alfresco.xforms.constants.XFORMS_PREFIX,
                                                         "group")[0];

                 this.rootWidget = new alfresco.xforms.ViewRoot(this, rootGroup);
                 this.rootWidget.render(alfUI);

                 this.loadWidgets(rootGroup, this.rootWidget);
               },

               /** Creates the widget for the provided xforms node. */
               createWidget: function(xformsNode)
               {
                 var appearance = (xformsNode.getAttribute("appearance") ||
                                   xformsNode.getAttribute(alfresco.xforms.constants.XFORMS_PREFIX + ":appearance"));
                 appearance = appearance == null || appearance.length == 0 ? null : appearance;

                 var xformsType = xformsNode.nodeName.toLowerCase();
                 var binding = this.getBinding(xformsNode);
                 var schemaType = binding ? binding.getType() : "*";

                 dojo.debug("creating widget for xforms type " + xformsType +
                            " schema type " + schemaType +
                            " with appearance " + appearance);
                 var x = alfresco.xforms.widgetConfig[xformsType];
                 if (!x)
                 {
                   throw new Error("unknown type " + xformsNode.nodeName);
                 }
                 x = schemaType in x ? x[schemaType] : x["*"];
                 x = appearance in x ? x[appearance] : x["*"];
//                 dojo.debug(xformsType + ":" + schemaType + ":" + appearance + " =>" + x);
                 if (x === undefined)
                 {
                   throw new Error("unable to find widget for xforms type " + xformsType +
                                   " schemaType " + schemaType +
                                   " appearance " + appearance);
                 }
                 if (x == null)
                 {
                   return null;
                 }
                 var result = new x.className(this, xformsNode, x.params);
                 if (result instanceof alfresco.xforms.Widget)
                 {
                   return result;
                 }
                 else
                 {
                   throw new Error("constructor for widget " + x + 
                                   " for xforms type " + xformsType +
                                   " schemaType " + schemaType +
                                   " appearance " + appearance +
                                   " is not an alfresco.xforms.Widget");
                 }
               },

               /** Loads all widgets for the provided xforms node's children. */
               loadWidgets: function(xformsNode, parentWidget)
               {
                 for (var i = 0; i < xformsNode.childNodes.length; i++)
                 {
                   if (xformsNode.childNodes[i].nodeType != dojo.dom.ELEMENT_NODE)
                   {
                     continue;
                   }
                   dojo.debug("loading " + xformsNode.childNodes[i].nodeName + 
                              " into " + parentWidget.id);
                   if (xformsNode.childNodes[i].getAttribute(alfresco.xforms.constants.ALFRESCO_PREFIX +
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
                     if (w instanceof alfresco.xforms.AbstractGroup)
                     {
                       this.loadWidgets(xformsNode.childNodes[i], w);
                     }
                   }
                 }
               },

               /** Loads all bindings from the xforms document. */
               _loadBindings: function(bind, parent, result)
               {
                 result = result || [];
                 for (var i = 0; i < bind.childNodes.length; i++)
                 {
                   if (bind.childNodes[i].nodeName.toLowerCase() == 
                       alfresco.xforms.constants.XFORMS_PREFIX + ":bind")
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
                                                alfresco.xforms.constants.XFORMS_NS, 
                                                alfresco.xforms.constants.XFORMS_PREFIX, 
                                                "model")[0];
               },

               /** Returns the instance section of the xforms document. */
               getInstance: function()
               {
                 var model = this.getModel();
                 return _getElementsByTagNameNS(model,
                                                alfresco.xforms.constants.XFORMS_NS,
                                                alfresco.xforms.constants.XFORMS_PREFIX,
                                                "instance")[0];
               },

               /** Returns the body section of the xforms document. */
               getBody: function()
               {
                 var b = _getElementsByTagNameNS(this.xformsNode,
                                                 alfresco.xforms.constants.XHTML_NS,
                                                 alfresco.xforms.constants.XHTML_PREFIX,
                                                 "body");
                 return b[b.length - 1];
               },

               /** Returns the binding corresponding to the provided xforms node. */
               getBinding: function(xformsNode)
               {
                 return this._bindings[xformsNode.getAttribute(alfresco.xforms.constants.XFORMS_PREFIX + ":bind")];
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

                 var req = alfresco.AjaxHelper.createRequest(this,
                                                             "swapRepeatItems",
                                                             params,
                                                             function(type, data, event)
                                                             {
                                                               this.target._handleEventLog(data.documentElement);
                                                             });
                 alfresco.AjaxHelper.sendRequest(req);
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
                 var req = alfresco.AjaxHelper.createRequest(this,
                                                             "setRepeatIndeces",
                                                             params,
                                                             function(type, data, evt)
                                                             {
                                                               this.target._handleEventLog(data.documentElement);
                                                             });
                 alfresco.AjaxHelper.sendRequest(req);
               },

               /** Fires an action specified by the id by calling XFormsBean.fireAction. */
               fireAction: function(id)
               {
                 var req = alfresco.AjaxHelper.createRequest(this,
                                                             "fireAction",
                                                             { id: id },
                                                             function(type, data, evt)
                                                             {
                                                               dojo.debug("fireAction." + type);
                                                               this.target._handleEventLog(data.documentElement);
                                                             });
                 alfresco.AjaxHelper.sendRequest(req);
               },

               /** Sets the value of the specified control id by calling XFormsBean.setXFormsValue. */
               setXFormsValue: function(id, value)
               {
                 value = value == null ? "" : value;
                 dojo.debug("setting value " + id + " = " + value);
                 var req = alfresco.AjaxHelper.createRequest(this,
                                                             "setXFormsValue",
                                                             { id: id, value: value },
                                                             function(type, data, evt)
                                                             {
                                                               this.target._handleEventLog(data.documentElement);
                                                             });
                 alfresco.AjaxHelper.sendRequest(req);
               },

               /** Handles the xforms event log resulting from a call to the XFormsBean. */
               _handleEventLog: function(events)
               {
                 var prototypeClones = [];
                 var generatedIds = null;
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
                     if ("valid" in xfe.properties)
                     {
                       xfe.getTarget().setValid(xfe.properties["valid"] == "true");
                     }
                     if ("required" in xfe.properties)
                     {
                       xfe.getTarget().setRequired(xfe.properties["required"] == "true");
                     }
                     if ("readonly" in xfe.properties)
                     {
                       xfe.getTarget().setReadonly(xfe.properties["readonly"] == "true");
                     }
                     if ("enabled" in xfe.properties)
                     {
                       xfe.getTarget().setEnabled(xfe.properties["enabled"] == "true");
                     }
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
                       //clone = prototypeNode.cloneNode(true);
                       clone = prototypeNode.ownerDocument.createElement(alfresco.xforms.constants.XFORMS_PREFIX + ":group");
                       clone.setAttribute(alfresco.xforms.constants.XFORMS_PREFIX + ":appearance", "repeated");
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
                     generatedIds = generatedIds || new Object();
                     generatedIds[xfe.targetId] = originalId;
                     if (prototypeClones.length != 1)
                     {
                       var e = _findElementById(prototypeClones[prototypeClones.length - 2], originalId);
                       if (e)
                       {
                         e.setAttribute(alfresco.xforms.constants.ALFRESCO_PREFIX + ":prototype", "true");
                       }
                     }
                     break;
                   }
                   case "chiba-item-inserted":
                   {
                     var position = Number(xfe.properties["position"]) - 1;
                     var originalId = xfe.properties["originalId"];
                     var clone = prototypeClones.pop();
                     // walk all nodes of the clone and ensure that they have generated ids.
                     // those that do not are nested repeats that should not be added
                     dojo.lang.assert(clone.getAttribute("id") in generatedIds,
                                      "expected clone id " + clone.getAttribute("id") +
                                      " to be a generated id");
                     function _removeNonGeneratedChildNodes(node, ids)
                     {
                       var child = node.firstChild;
                       while (child)
                       {
                         var next = child.nextSibling;
                         if (child.nodeType == dojo.dom.ELEMENT_NODE)
                         {
                           if (child.getAttribute("id") in ids)
                           {
                             _removeNonGeneratedChildNodes(child, ids);
                           }
                           else
                           {
                             node.removeChild(child);
                           }
                         }
                         child = next;
                       }
                     };
                     _removeNonGeneratedChildNodes(clone, generatedIds);

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
                     _show_error(document.createTextNode(alfresco.xforms.constants.resources["validation_provide_values_for_required_fields"]));
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
  var errorDiv = document.getElementById(alfresco.xforms.constants.XFORMS_ERROR_DIV_ID);
  if (errorDiv)
  {
    dojo.dom.removeChildren(errorDiv);
    errorDiv.style.display = "none";
  }
}

/** shows the error message display. */
function _show_error(msg)
{
  var errorDiv = document.getElementById(alfresco.xforms.constants.XFORMS_ERROR_DIV_ID);
  if (!errorDiv)
  {
    errorDiv = document.createElement("div");
    errorDiv.setAttribute("id", alfresco.xforms.constants.XFORMS_ERROR_DIV_ID);
    dojo.html.setClass(errorDiv, "infoText statusErrorText xformsError");
    var alfUI = document.getElementById(alfresco.xforms.constants.XFORMS_UI_DIV_ID);
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
  if (djConfig.isDebug)
  {
    dojo.debug("evaluating xpath " + xpath +
               " on node " + contextNode.nodeName +
               " in document " + xmlDocument);
  }
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

    if (djConfig.isDebug)
    {
      dojo.debug("using namespaces " + namespaces.join(","));
    }
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
  var XPathResult = 
  {
    ANY_TYPE: 0,
    NUMBER_TYPE: 1,
    STRING_TYPE:  2,
    BOOEAN_TYPE: 3,
    FIRST_ORDERED_NODE_TYPE: 9
  };
}
if (!String.prototype.startsWith)
{
  String.prototype.startsWith = function(s)
  {
    return this.indexOf(s) == 0;
  }
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

dojo.html.toCamelCase = function(str)
{
  return str.replace(/-./, function(str) { return str.charAt(1).toUpperCase(); });
}

////////////////////////////////////////////////////////////////////////////////
// tiny mce integration
////////////////////////////////////////////////////////////////////////////////
tinyMCE.init({
  theme: "advanced",
  mode: "exact",
  width: -1,
  auto_resize: false,
  force_p_newlines: false,
  encoding: "UTF-8",
  entity_encoding: "raw",
  add_unload_trigger: false,
  add_form_submit_trigger: false,
  execcommand_callback: "alfresco_TinyMCE_execcommand_callback",
  theme_advanced_toolbar_location: "top",
  theme_advanced_toolbar_align: "left",
  theme_advanced_buttons1: "",
  theme_advanced_buttons2: "",
  theme_advanced_buttons3: "",
  urlconverter_callback: "alfresco_TinyMCE_urlconverter_callback"
});

function alfresco_TinyMCE_urlconverter_callback(href, element, onsave)
{
  dojo.debug("request to convert " + href + " onsave = " + onsave);
  if (onsave)
  {
    return (href && href.startsWith(alfresco.constants.AVM_WEBAPP_URL)
            ? href.substring(alfresco.constants.AVM_WEBAPP_URL.length)
            : href);
  }
  else
  {
    return (href && href.startsWith("/")
            ? alfresco.constants.AVM_WEBAPP_URL + href
            : href);
  }
}

function alfresco_TinyMCE_execcommand_callback(editor_id, elm, command, user_interface, value)
{
  if (command == "mceLink")
  {
    // BEGIN COPIED FROM ADVANCED THEME editor_template_src.js
    var inst = tinyMCE.getInstanceById(editor_id);
    var doc = inst.getDoc();
    var selectedText = (tinyMCE.isMSIE 
                        ? doc.selection.createRange().text
                        : inst.getSel().toString());

    if (!tinyMCE.linkElement &&
        tinyMCE.selectedElement.nodeName.toLowerCase() != "img" && 
        selectedText.length <= 0)
    {
      return true;
    }

    var href = "", target = "", title = "", onclick = "", action = "insert", style_class = "";

    if (tinyMCE.selectedElement.nodeName.toLowerCase() == "a")
    {
      tinyMCE.linkElement = tinyMCE.selectedElement;
    }

    // Is anchor not a link
    if (tinyMCE.linkElement != null && tinyMCE.getAttrib(tinyMCE.linkElement, 'href') == "")
    {
      tinyMCE.linkElement = null;
    }

    if (tinyMCE.linkElement) 
    {
      href = tinyMCE.getAttrib(tinyMCE.linkElement, 'href');
      target = tinyMCE.getAttrib(tinyMCE.linkElement, 'target');
      title = tinyMCE.getAttrib(tinyMCE.linkElement, 'title');
      onclick = tinyMCE.getAttrib(tinyMCE.linkElement, 'onclick');
      style_class = tinyMCE.getAttrib(tinyMCE.linkElement, 'class');

      // Try old onclick to if copy/pasted content
      if (onclick == "")
      {
        onclick = tinyMCE.getAttrib(tinyMCE.linkElement, 'onclick');
      }
      onclick = tinyMCE.cleanupEventStr(onclick);

      href = eval(tinyMCE.settings['urlconverter_callback'] + "(href, tinyMCE.linkElement, true);");

      // Use mce_href if defined
      mceRealHref = tinyMCE.getAttrib(tinyMCE.linkElement, 'mce_href');
      if (mceRealHref != "") 
      {
        href = mceRealHref;
        if (tinyMCE.getParam('convert_urls'))
        {
          href = eval(tinyMCE.settings['urlconverter_callback'] + "(href, tinyMCE.linkElement, true);");
        }
      }

      action = "update";
    }

    var window_props = { file: alfresco.constants.WEBAPP_CONTEXT + "/jsp/wcm/tiny_mce_link_dialog.jsp",
                         width: 510 + tinyMCE.getLang('lang_insert_link_delta_width', 0),
                         height: 400 + tinyMCE.getLang('lang_insert_link_delta_height', 0) };
    var dialog_props = { href: href, 
                         target: target, 
                         title: title, 
                         onclick: onclick, 
                         action: action, 
                         className: style_class, 
                         inline: "yes" };
    tinyMCE.openWindow(window_props, dialog_props);
    return true;
  }
  else if (command == "mceImage")
  {
    var src = "", alt = "", border = "", hspace = "", vspace = "", width = "", height = "", align = "",
      title = "", onmouseover = "", onmouseout = "", action = "insert";
    var img = tinyMCE.imgElement;
    var inst = tinyMCE.getInstanceById(editor_id);

    if (tinyMCE.selectedElement != null && tinyMCE.selectedElement.nodeName.toLowerCase() == "img") 
    {
      img = tinyMCE.selectedElement;
      tinyMCE.imgElement = img;
    }

    if (img) 
    {
      // Is it a internal MCE visual aid image, then skip this one.
      if (tinyMCE.getAttrib(img, 'name').indexOf('mce_') == 0)
      {
        return true;
      }

      src = tinyMCE.getAttrib(img, 'src');
      alt = tinyMCE.getAttrib(img, 'alt');

      // Try polling out the title
      if (alt == "")
      {
        alt = tinyMCE.getAttrib(img, 'title');
      }

      // Fix width/height attributes if the styles is specified
      if (tinyMCE.isGecko) 
      {
        var w = img.style.width;
        if (w != null && w.length != 0)
        {
          img.setAttribute("width", w);
        }

        var h = img.style.height;
        if (h != null && h.length != 0)
        {
          img.setAttribute("height", h);
        }
      }

      border = tinyMCE.getAttrib(img, 'border');
      hspace = tinyMCE.getAttrib(img, 'hspace');
      vspace = tinyMCE.getAttrib(img, 'vspace');
      width = tinyMCE.getAttrib(img, 'width');
      height = tinyMCE.getAttrib(img, 'height');
      align = tinyMCE.getAttrib(img, 'align');
      onmouseover = tinyMCE.getAttrib(img, 'onmouseover');
      onmouseout = tinyMCE.getAttrib(img, 'onmouseout');
      title = tinyMCE.getAttrib(img, 'title');

      // Is realy specified?
      if (tinyMCE.isMSIE) 
      {
        width = img.attributes['width'].specified ? width : "";
        height = img.attributes['height'].specified ? height : "";
      }

      //onmouseover = tinyMCE.getImageSrc(tinyMCE.cleanupEventStr(onmouseover));
      //onmouseout = tinyMCE.getImageSrc(tinyMCE.cleanupEventStr(onmouseout));

      src = eval(tinyMCE.settings['urlconverter_callback'] + "(src, img, true);");

      // Use mce_src if defined
      mceRealSrc = tinyMCE.getAttrib(img, 'mce_src');
      if (mceRealSrc != "") 
      {
        src = mceRealSrc;

        if (tinyMCE.getParam('convert_urls'))
        {
          src = eval(tinyMCE.settings['urlconverter_callback'] + "(src, img, true);");
        }
      }
      action = "update";
    }

    var window_props = { file: alfresco.constants.WEBAPP_CONTEXT + "/jsp/wcm/tiny_mce_image_dialog.jsp",
                         width: 510 + tinyMCE.getLang('lang_insert_image_delta_width', 0),
                         height: 400 + (tinyMCE.isMSIE ? 25 : 0) + tinyMCE.getLang('lang_insert_image_delta_height', 0) };
    var dialog_props = { src: src, 
                         alt: alt, 
                         border: border, 
                         hspace: hspace, 
                         vspace: vspace, 
                         width: width, 
                         height: height, 
                         align: align, 
                         title: title,
                         onmouseover: onmouseover,
                         onmouseout: onmouseout, 
                         action: action,
                         inline: "yes" };
    tinyMCE.openWindow(window_props, dialog_props);
    return true;
  }
  else
  {
    return false;
  }
}

alfresco.xforms.widgetConfig =
{
  "xf:group": 
  {
    "*": { "minimal": { "className": alfresco.xforms.HGroup }, "*": { "className": alfresco.xforms.VGroup }}
  },
  "xf:repeat": 
  {
    "*": { "*": { "className": alfresco.xforms.Repeat } }
  },
  "xf:textarea":
  {
    "*": { "minimal": { "className": alfresco.xforms.PlainTextEditor }, 
           "*": { "className": alfresco.xforms.RichTextEditor, params: [ "bold,italic,underline,separator,forecolor,backcolor,separator,link,unlink,image", "", "" ] },
           "full": { "className": alfresco.xforms.RichTextEditor, params: ["bold,italic,underline,strikethrough,separator,fontselect,fontsizeselect", "link,unlink,image,separator,justifyleft,justifycenter,justifyright,justifyfull,separator,bullist,numlist,separator,undo,redo,separator,forecolor,backcolor", "" ] }},
  },
  "xf:upload":
  {
    "*": { "*": { "className": alfresco.xforms.FilePicker } }
  },
  "xf:range":
  {
    "*": { "*": { "className": alfresco.xforms.NumericalRange } }
  },
  "xf:input":
  {
    "date": { "*": { "className": alfresco.xforms.DatePicker }},
    "time": { "*": { "className": alfresco.xforms.TimePicker }},
    "gDay": { "*": { "className": alfresco.xforms.DayPicker }},
    "gMonth": { "*": { "className": alfresco.xforms.MonthPicker }},
    "gYear": { "*": { "className": alfresco.xforms.YearPicker }},
    "gMonthDay": { "*": { "className": alfresco.xforms.MonthDayPicker }},
    "gYearMonth": { "*": { "className": alfresco.xforms.YearMonthPicker }},
    "dateTime": { "*": { "className": alfresco.xforms.DateTimePicker }},
    "*": { "*": { "className": alfresco.xforms.TextField }}
  },
  "xf:select1":
  {
    "boolean": { "*": { "className": alfresco.xforms.Checkbox }},
    "*": { "full": { "className": alfresco.xforms.RadioSelect1},
           "*": { "className": alfresco.xforms.ComboboxSelect1 }}
  },
  "xf:select":
  {
    "*": { "full": { "className": alfresco.xforms.CheckboxSelect},
           "*": { "className": alfresco.xforms.ListSelect }}
  },
  "xf:submit":
  {
    "*": { "*": { "className": alfresco.xforms.Submit } }
  },
  "xf:trigger":
  {
    "*": { "*": { "className": alfresco.xforms.Trigger }}
  },
  "xf:switch":
  {
    "*": { "*": { "className": alfresco.xforms.SwitchGroup } }
  },
  "xf:case":
  {
    "*": { "*": { "className": alfresco.xforms.CaseGroup }}
  },
  "chiba:data": { "*": { "*": null } },
  "xf:label": { "*": { "*": null } },
  "xf:alert": { "*": { "*": null } }
}
