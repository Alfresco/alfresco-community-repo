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
// FilePickerWidget
//
// This script communicates with the XFormBean to manage a file picker widget
// for selecting and uploading files in the avm.
//
// This script requires dojo.js, ajax_helper.js and upload_helper.js to be
// loaded in advance.
////////////////////////////////////////////////////////////////////////////////
if (typeof alfresco == "undefined")
{
  throw new Error("file_picker_widget requires alfresco be defined");
}

if (typeof alfresco.constants == "undefined")
{
  throw new Error("file_picker_widget requires alfresco.constants be defined");
}

if (typeof alfresco.resources == "undefined")
{
  throw new Error("file_picker_widget requires alfresco.resources be defined");
}

/**
 * The file picker widget.
 */
alfresco.FilePickerWidget = function(uploadId, 
                                     node, 
                                     value, 
                                     readonly, 
                                     change_callback, 
                                     cancel_callback,
                                     resize_callback,
                                     selectableTypes,
                                     filterMimetypes)
{
  this.uploadId = uploadId;
  this.node = node;
  this.value = value == null || value.length == 0 ? null : value;
  this.readonly =  readonly || false;
  this.change_callback = change_callback;
  this.cancel_callback = cancel_callback || function() {};
  this.resize_callback = resize_callback || function() {};
  this.selectableTypes = selectableTypes;
  this.filterMimetypes = filterMimetypes;
}

// static methods and properties

alfresco.FilePickerWidget._uploads = [];
alfresco.FilePickerWidget._handleUpload = function(id, fileInput, webappRelativePath, widget)
{
  alfresco.FilePickerWidget._uploads[id] = 
  {
    widget:widget, 
    path: fileInput.value, 
    webappRelativePath: webappRelativePath
  };
  handle_upload_helper(fileInput, 
                       id,
                       alfresco.FilePickerWidget._upload_completeHandler,
                       alfresco.constants.WEBAPP_CONTEXT,
                       "/ajax/invoke/FilePickerBean.uploadFile",
                       { currentPath: webappRelativePath });
}

alfresco.FilePickerWidget._upload_completeHandler = function(id, path, fileName, fileTypeImage, error)
{
  var upload = alfresco.FilePickerWidget._uploads[id];
  upload.widget._upload_completeHandler(fileName, 
                                        upload.webappRelativePath,
                                        fileTypeImage,
                                        error);
}

// instance methods and properties

alfresco.FilePickerWidget.prototype = {

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
  if (this._selectButton)
  {
    this._selectButton.disabled = this.readonly;
  }
  else if (this.readonly)
  {
    this._showSelectedValue();
  }
},

destroy: function()
{
  dojo.dom.removeChildren(this.node);
  this.node.parentNode.removeChild(this.node);
  this.node = null;
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
                              dojo.html.getMargin(this.statusDiv).height +
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
  if (this.node == null)
  {
    return;
  }
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

  this._selectButton = d.createElement("input");
  this._selectButton.filePickerWidget = this;
  this._selectButton.type = "button";

  this._selectButton.value = (this.value == null 
                              ? alfresco.resources["select"] 
                              : alfresco.resources["change"]);
  this._selectButton.disabled = this.readonly;
  this._selectButton.style.margin = "0px 10px";
  this.node.appendChild(this._selectButton);

  this.selectedPathInput.style.width = (1 -
                                        ((this._selectButton.offsetWidth +
                                          dojo.html.getMargin(this._selectButton).width) /
                                         dojo.html.getContentBox(this.node).width)) * 100 + "%";

  dojo.event.browser.addListener(this._selectButton, 
                                 "onclick", 
                                 this._selectButton_clickHandler);
},

_selectButton_clickHandler: function(event)
{
  var w = event.target.filePickerWidget;
  w._navigateToNode(w.getValue() || "");
},

_selectPathInput_changeHandler: function(event)
{
  this.setValue(event.target.value);
},

_navigateToNode: function(path)
{
  var params = { currentPath:  path};
  if (this.selectableTypes)
  {
    params.selectableTypes = this.selectableTypes;
  }
  if (this.filterMimetypes)
  {
    params.filterMimetypes = this.filterMimetypes;
  }

  alfresco.AjaxHelper.sendRequest("FilePickerBean.getFilePickerData",
                                  params,
                                  true,
                                  this._showPicker.bindAsEventListener(this));
},

_showPicker: function(data)
{
  data = data.documentElement;
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
  // can't use dojo's event handling since it screws up when opened in another window
  var filePickerWidget = this;
  var headerMenuTriggerLink = this.headerMenuTriggerLink;
  this.headerMenuTriggerLink.onclick = function(event)
  {
    if (filePickerWidget.parentPathMenu)
    {
      filePickerWidget._closeParentPathMenu();
    }
    else
    {
      filePickerWidget._openParentPathMenu(headerMenuTriggerLink, 
                                           headerMenuTriggerLink.getAttribute("webappRelativePath"));
    }
  };

  this.headerMenuTriggerLink.appendChild(d.createTextNode(currentPathName));

  headerMenuTriggerImage = d.createElement("img");
  this.headerMenuTriggerLink.appendChild(headerMenuTriggerImage);
  this.headerMenuTriggerLink.image = headerMenuTriggerImage;
  headerMenuTriggerImage.setAttribute("src", alfresco.constants.WEBAPP_CONTEXT + "/images/icons/menu.gif");
  headerMenuTriggerImage.style.borderWidth = "0px";
  headerMenuTriggerImage.style.marginLeft = "4px";
  headerMenuTriggerImage.align = "absmiddle";

  var headerRightDiv = d.createElement("div");

  var addContentLink = d.createElement("a");
  headerRightDiv.appendChild(addContentLink);
  addContentLink.setAttribute("webappRelativePath", currentPath);
  addContentLink.style.textDecoration = "none";
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
  addContentImage.setAttribute("src", alfresco.constants.WEBAPP_CONTEXT + "/images/icons/add.gif");
  addContentLink.appendChild(addContentImage);

  addContentLink.appendChild(d.createTextNode(alfresco.resources["add_content"]));

  var navigateToParentLink = d.createElement("a");
  headerRightDiv.appendChild(navigateToParentLink);
  navigateToParentLink.setAttribute("webappRelativePath", currentPath);
  navigateToParentLink.filePickerWidget = this;
  navigateToParentLink.style.textDecoration = "none";
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
  dojo.html.setOpacity(navigateToParentNodeImage, (currentPathName == "/" ? .3 : 1));
  navigateToParentNodeImage.style.margin = "0px 2px 0px 2px";
  navigateToParentNodeImage.align = "absmiddle";
  navigateToParentNodeImage.setAttribute("src", alfresco.constants.WEBAPP_CONTEXT + "/images/icons/up.gif");
  navigateToParentLink.appendChild(navigateToParentNodeImage);
  navigateToParentLink.appendChild(d.createTextNode(alfresco.resources["go_up"]));

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

  cancelButton.value = alfresco.resources["cancel"];
  footerDiv.appendChild(cancelButton);

  cancelButton.style.margin = ((.5 * footerDiv.offsetHeight) - 
                               (.5 * cancelButton.offsetHeight)) + "px 0px";
  dojo.event.connect(cancelButton, "onclick", function(event)
                     {
                       var w = event.target.filePickerWidget;
                       w.cancel_callback(this);
                       w._showSelectedValue();
                     });
  this.contentDiv.style.height = (this.node.offsetHeight -
                                  (this.statusDiv ? this.statusDiv.offsetHeight : 0) -
                                  footerDiv.offsetHeight -
                                  headerDiv.offsetHeight - 10) + "px";
  var childNodes = data.getElementsByTagName("child-node");
  for (var i = 0; i < childNodes.length; i++)
  {
    if (childNodes[i].nodeType != document.ELEMENT_NODE)
    {
      continue;
    }
    var webappRelativePath = childNodes[i].getAttribute("webappRelativePath");
    var fileName = webappRelativePath.replace(/.*\/([^/]+)/, "$1");
    var row = this._createRow(fileName,
                              webappRelativePath,
                              childNodes[i].getAttribute("type") == "directory",
                              eval(childNodes[i].getAttribute("selectable")),
                              childNodes[i].getAttribute("image"),
                              "xformsRow" + (i % 2 ? "Even" : "Odd"));
    this.contentDiv.appendChild(row);
  }
  
  if (data.getAttribute("error") && data.getAttribute("error").length != 0)
  {
    this._showStatus(data.getAttribute("error"), true);
  }
},

_createRow: function(fileName, webappRelativePath,  isDirectory, isSelectable, fileTypeImage, rowClass)
{
  var d = this.contentDiv.ownerDocument;
  var result = d.createElement("div");
  result.setAttribute("id", fileName + "-row");
  result.setAttribute("webappRelativePath", webappRelativePath); 
  result.filePickerWidget = this;
  dojo.html.setClass(result, "xformsFilePickerRow " + rowClass);
  result.onmouseover = function()
    {
      var prevHover = result.parentNode.hoverNode;
      if (prevHover)
      {
        dojo.html.removeClass(prevHover, "xformsRowHover");
      }
      result.parentNode.hoverNode = result;
      dojo.html.addClass(result, "xformsRowHover");
    };
  var e = d.createElement("img");
  e.align = "absmiddle";
  e.style.margin = "0px 4px 0px 4px";
  e.setAttribute("src", alfresco.constants.WEBAPP_CONTEXT + fileTypeImage);
  result.appendChild(e);

  if (isDirectory)
  {
    e = d.createElement("a");
    e.style.textDecoration = "none";
    e.setAttribute("href", "javascript:void(0)");

    e.onclick = function() 
      {
        var w = result.filePickerWidget;
        w._navigateToNode(result.getAttribute("webappRelativePath"));
        return true;
      };
    e.appendChild(d.createTextNode(fileName));
    result.appendChild(e);
  }
  else
  {
    result.appendChild(d.createTextNode(fileName));
  }
  if (isSelectable)
  {
    e = d.createElement("input");
    e.type = "button";
    e.name = webappRelativePath;
    e.value = "Select";
    result.appendChild(e);
      
    e.style.position = "absolute";
    e.style.right = "10px";
    e.style.top = (.5 * result.offsetHeight) - (.5 * e.offsetHeight) + "px";
    e.onclick = function()
      {
        var w = result.filePickerWidget;
        w.setValue(result.getAttribute("webappRelativePath"));
        w._showSelectedValue();
      };
  }
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

  fileInput.onchange = function(event)
    {
      event = event || fileInput.ownerDocument.parentWindow.event;
      var target = event.target || event.srcElement;
      var w = target.widget;
      if (w.addContentDiv)
      {
        var d = w.addContentDiv.ownerDocument;
        dojo.dom.removeChildren(w.addContentDiv);
        
        var fileName = target.value.replace(/.*[\/\\]([^\/\\]+)/, "$1");
        w.addContentDiv.appendChild(d.createTextNode(alfresco.resources["upload"] + ": " + fileName));
        var img = d.createElement("img");
        img.setAttribute("src", alfresco.constants.WEBAPP_CONTEXT + 
                         "/images/icons/process_animation.gif");
        img.style.position = "absolute";
        img.style.right = "10px";
        img.style.height = (.5 * w.addContentDiv.offsetHeight)  + "px";
        img.style.top = (.25 * w.addContentDiv.offsetHeight) + "px";
        w.addContentDiv.appendChild(img);
      }
      
      alfresco.FilePickerWidget._handleUpload(w.uploadId, 
                                              target,
                                              target.getAttribute("webappRelativePath"),
                                              w);
    };
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
                              true /* this is potentially inaccurate - need to add some checks in the backing bean to check selectable */,
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
    event = event || d.parentWindow.event;
    var t = event.target || event.srcElement;

    // always remove - this handler only ever needs to handle a single click
    dojo.event.browser.removeListener(d, "click", parentPathMenu_documentClickHandler, true, true);
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

  dojo.event.browser.addListener(d, "click", parentPathMenu_documentClickHandler, true, true);

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

  pathTextDiv.appendChild(d.createTextNode(alfresco.resources["path"]));
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
    parentNodeImage.setAttribute("src", alfresco.constants.WEBAPP_CONTEXT + 
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
