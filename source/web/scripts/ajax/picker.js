/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Prerequisites: common.js
 *                mootools.v1.11.js
 */

// Picker class definition
var AlfPicker = new Class(
{
   /* id of the picker */
   id: null,

   /* variable name being used */
   varName: null,
   
   /* form Id to submit when selection complete */
   formClientId: null,
   
   /* the item the picker will start with */
   startId: null,
   
   /* list of items currently selected */
   selected: null,

   /* list of items pre-selected */
   preselected: null,
   
   /* the current parent being shown */
   parent: null,
   
   /* the list of items currently displayed */
   items: [],
   
   /* parent stack for the Navigate Up action*/
   stack: [],
   
   /* row type toggle */
   oddRow: true,
   
   /* ajax service call to retrieve data */
   service: null,
   
   /* default icon to use if not provided by the associated service */
   defaultIcon: null,
   
   /* single selection mode flag */
   singleSelect: false,
   
   /* initial display style of the outer div */
   initialDisplayStyle: null,
   
   /* addition service request attributes if any */
   requestAttributes: null,
   
   initialize: function(id, varName, service, formClientId, singleSelect) 
   {
      this.id = id;
      this.varName = varName;
      this.service = service;
      this.formClientId = formClientId;
      if (singleSelect != undefined)
      {
         this.singleSelect = singleSelect;
      }
      this.selected = [];
      this.preselected = [];
   },
   
   setDefaultIcon: function(icon)
   {
      this.defaultIcon = icon;
   },
   
   setStartId: function(id)
   {
      this.startId = id;
   },
   
   setSelectedItems: function(jsonString)
   {
      this.preselected = Json.evaluate(jsonString);
   },
   
   setRequestAttributes: function(attrs)
   {
      this.requestAttributes = attrs;
   },
   
   showSelector: function()
   {
      // init selector state
      this.selected = [];
      this.stack = [];
      
      this.initialDisplayStyle = $(this.id + "-noitems").getStyle("display");
      $(this.id + "-selector").setStyle("display", "block");
      $(this.id + "-selected").empty();
      $(this.id + "-selected").setStyle("display", "block");
      $(this.id + "-noitems").setStyle("display", "none");
      if (this.singleSelect)
      {
         $(this.id + "-finish").setStyle("display", "none");
      }
      
      this.preselected.each(function(item, i)
      {
         this.addSelectedItem(item);
      }, this);
      
      // first ajax request for the children of the start item
      this.getNodeData(this.startId, null, this.populateChildren);
   },
   
   childClicked: function(index)
   {
      this.hidePanels();
      var item = this.items[index];
      // add an extra property to record the scroll position for this item
      item.scrollpos = $(this.id + "-results-list").scrollTop;
      this.stack.push(item);  // ready for the breadcrumb redraw after the child data request
      this.getNodeData(item.id, null, this.populateChildren)
   },
   
   upClicked: function()
   {
      this.hidePanels();
      // pop the parent off
      var parent = this.stack.pop();
      // peek for the grandparent - we may not have a grandparent (depending on start location) - so use the
      // getNodeData(parent, child, ...) call to pass in the child rather than the parent and let server calculate it
      var grandParent = null;
      if (this.stack.length != 0)
      {
         grandParent = this.stack[this.stack.length-1];
      }
      this.getNodeData(grandParent != null ? grandParent.id : null, grandParent == null ? parent.id : null, this.populateChildren, parent.scrollpos);
   },
   
   addItem: function(index)
   {
      var item;
      if (index != -1)
      {
         item = this.items[index];
      }
      else
      {
         item = this.parent;
      }
      
      if (this.singleSelect)
      {
         this.selected.push(item);
         this.doneClicked();
      }
      else
      {
         this.addSelectedItem(item);
         // hide the Add button as this item is now added
         $(this.id + "-add-" + item.id).setStyle("display", "none");
      }
   },

   addSelectedItem: function(item)
   {
      // add item to list of selected items
      this.selected.push(item);

      // add the item to list outside the selector
      var itemId = this.id + "-sel-" + item.id;
      var itemDiv = new Element("div", {"id": itemId, "class": "pickerSelectedItem"});
      
      var itemSpan = new Element("span", {"class": "pickerSelectedItemText"});
      itemSpan.appendText(item.name);
      itemSpan.injectInside(itemDiv);
      
      var actionSpan = new Element("span", {"class": "pickerSelectedItemAction"});
      var actionScript = "javascript:" + this.varName + ".delItem('" + item.id + "');";
      var actionLink = new Element("a", {"href": actionScript});
      var deleteIcon = new Element("img", {"src": getContextPath() + "/images/icons/minus.gif", "class": "pickerSelectedIcon",
                                   "border": 0, "title": "Remove", "alt": "Remove"});
      deleteIcon.injectInside(actionLink);
      actionLink.injectInside(actionSpan);
      actionSpan.injectInside(itemDiv);
      
      // add mouse enter/leave enter to toggle delete icon (and toggle margin on outer div)
      itemDiv.addEvent('mouseenter', function(e) {
         $E('.pickerSelectedIcon', itemDiv).setStyle("opacity", 1);
      });
      itemDiv.addEvent('mouseleave', function(e) {
         $E('.pickerSelectedIcon', itemDiv).setStyle("opacity", 0);
      });
      // add the item to the main selected item div
      itemDiv.injectInside($(this.id + "-selected"));
      
      // set the background image now the itemdiv has been added to the DOM (for IE)
      itemDiv.setStyle("background-image", "url(" + getContextPath() + item.icon + ")");
      
      // set opacity the style now the item has been added to the DOM (for IE)
      $E('.pickerSelectedIcon', itemDiv).setStyle("opacity", 0);
      
      // apply the effect
      var fx = new Fx.Styles(itemDiv, {duration: 1000, wait: false, transition: Fx.Transitions.Quad.easeOut});
      fx.start({'background-color': ['#faf7ce', '#ffffff']});
   },
   
   delItem: function(itemId)
   {
      // remove item from the selected items list
      for (i=0; i<this.selected.length; i++)
      {
         if (this.selected[i].id == itemId)
         {
            this.selected.splice(i, 1); break;
         }
      }
      
      // remove the div representing the selected item
      $(this.id + "-sel-" + itemId).remove();
      
      // unhide the Add button if visible
      var addBtn = $(this.id + "-add-" + itemId);
      if (addBtn != null)
      {
         addBtn.setStyle("display", "block");
      }
   },
   
   populateBreadcrumb: function()
   {
      var bcpanel = $(this.id + "-nav-bread");
      bcpanel.empty();
      
      // add each item from the navigation stack to the breadcrumb
      for (var i=0; i<this.stack.length; i++)
      {
         var div = new Element("div", {"class": "pickerNavBreadcrumbItem"});
         
         var actionScript = "javascript:" + this.varName + ".clickBreadcumb(" + i + ");";
         var actionLink = new Element("a", {"href": actionScript});
         actionLink.setText(this.stack[i].name);
         actionLink.injectInside(div);
         
         div.injectInside(bcpanel);
         
         // override left padding to indent the items appropriately
         div.setStyle("padding-left", (i<<3)+2);
      }
   },
   
   clickBreadcumb: function(index)
   {
      this.hidePanels();
      var item = this.stack[index];
      // remove all items under this one from the navigation stack
      var removeCount = (this.stack.length - index - 1);
      if (removeCount != 0)
      {
         this.stack.splice(index + 1, removeCount);
      }
      this.getNodeData(item.id, null, this.populateChildren);
   },
   
   breadcrumbToggle: function()
   {
      var bcpanel = $(this.id + "-nav-bread");
      if (bcpanel.getChildren().length != 0)
      {
         if (bcpanel.getStyle("display") == "none")
         {
            bcpanel.setStyle("opacity", 0);
            bcpanel.setStyle("display", "block");
            var fx = new Fx.Styles(bcpanel, {duration: 200, wait: false, transition: Fx.Transitions.Quad.easeOut});
            fx.start({'opacity': [0, 1]});
         }
         else
         {
            var fx = new Fx.Styles(bcpanel, {duration: 200, wait: false, transition: Fx.Transitions.Quad.easeOut,
                                   onComplete: function() {bcpanel.setStyle("display", "none");}});
            fx.start({'opacity': [1, 0]});
         }
      }
   },
   
   hidePanels: function()
   {
      $(this.id + "-nav-bread").setStyle("display", "none");
   },
   
   doneClicked: function()
   {
      var ids = "";
      for (i=0; i<this.selected.length; i++)
      {
         if (i != 0) ids += ",";
         ids += this.selected[i].id;
      }
      
      // special case for clearing out multi-select lists
      if (!this.singleSelect && (ids == ""))
      {
         ids = "empty";
      }
      
      $(this.id + "-value").setProperty("value", ids);
      
      document.forms[this.formClientId].submit();
      return false;
   },
   
   cancelClicked: function()
   {
      $(this.id + "-selector").setStyle("display", "none");
      $(this.id + "-selected").setStyle("display", "none");
      $(this.id + "-noitems").setStyle("display", this.initialDisplayStyle);
   },
   
   populateChildren: function(response, picker, scrollpos)
   {
      // clear any current results
      var results = $(picker.id + "-results-list");
      results.empty();
      
      // set the new parent
      picker.parent = {id: response.parent.id, name: response.parent.name};
      
      // if nav stack is empty - add the parent item as the first entry
      if (picker.stack.length == 0)
      {
         picker.stack.push(picker.parent);
      }
      
      // if the parent is null we're at the root so hide the up link
      // otherwise we need to render it with the correct details
      var upLink = $(picker.id + "-nav-up");
      if (picker.parent.id == null || response.parent.isroot == true)
      {
         upLink.setStyle("display", "none");
         upLink.setProperty("href", "#");
      }
      else
      {
         upLink.setStyle("display", "block");
         upLink.setProperty("href", "javascript:" + picker.varName + ".upClicked();");
      }
      
      // show what the parent next to the breadcrumb drop-down
      $(picker.id + "-nav-txt").setText(picker.parent.name);
      
      // render action for parent item (as it may be the root and not shown in child list!)
      $(picker.id + "-nav-add").empty();
      if (response.parent.selectable != false)
      {
         var isSelected = false;
         for (i=0; i<picker.selected.length; i++)
         {
            if (picker.selected[i].id == picker.parent.id)
            {
               isSelected = true; break;
            }
         }
         if (isSelected == false)
         {
            var actionId = picker.id + "-add-" + picker.parent.id;
            var actionScript = "javascript:" + picker.varName + ".addItem(-1);";
            var actionLink = new Element("a", {"href": actionScript});
            var actionImg = new Element("img", {"id": actionId, "src": getContextPath() + "/images/icons/plus.gif", "class": "pickerActionButton",
                                        "border": 0, "title": "Add", "alt": "Add"});
            actionImg.injectInside(actionLink);
            actionLink.injectInside($(picker.id + "-nav-add"));
            // style modification for this Add button - it's inside a floating div unlike the others
            if (document.all == undefined) actionImg.setStyle("vertical-align", "-18px");
         }
      }
      
      // iterate through the children and render a row for each one
      picker.items = [];
      picker.oddRow = true;
      
      for (var i=0; i<response.children.length; i++)
      {
         var item = response.children[i];
         if (item.icon == undefined)
         {
            item.icon = picker.defaultIcon;
         }
         picker.items.push(item);
         picker.renderResultItem(item, i);
      }
      
      // scroll back to last position if required
      results.scrollTop = (scrollpos == undefined ? 0 : scrollpos);
      
      picker.populateBreadcrumb();
   },
   
   renderResultItem: function(item, index)
   {
      var divClass = "pickerResultsRow " + (this.oddRow ? "pickerResultsOddRow" : "pickerResultsEvenRow");
      
      this.oddRow = !this.oddRow;
      
      var div = new Element("div", {"class": divClass});
      
      // render icon
      var iconSpan = new Element("span", {"class": "pickerResultIcon"});
      var iconImg = new Element("img", {"src": getContextPath() + item.icon});
      iconImg.injectInside(iconSpan);
      iconSpan.injectInside(div);
      
      // render actions
      var isSelected = false;
      if (item.selectable != false)
      {
         var actionsSpan = new Element("span", {"class": "pickerResultActions"});
         
         // display Add button for the item 
         for (i=0; i<this.selected.length; i++)
         {
            if (this.selected[i].id == item.id)
            {
               isSelected = true; break;
            }
         }
         // even if found in the selected list, still need to generate the button - but hide it later
         var actionId = this.id + "-add-" + item.id;
         var actionScript = "javascript:" + this.varName + ".addItem(" + index + ");";
         var actionLink = new Element("a", {"href": actionScript});
         var actionImg = new Element("img", {"id": actionId, "src": getContextPath() + "/images/icons/plus.gif", "class": "pickerActionButton",
                                     "border": 0, "title": "Add", "alt": "Add"});
         actionImg.injectInside(actionLink);
         actionLink.injectInside(actionsSpan);
         
         actionsSpan.injectInside(div);
      }
      
      // render name link
      var nameSpan = new Element("span", {"class": "pickerResultName"});
      var nameLink;
      if (item.url == undefined)
      {
         var link = "javascript:" + this.varName + ".childClicked(" + index + ");";
         nameLink = new Element("a", {"href": link});
      }
      else
      {
         nameLink = new Element("a", {"href": getContextPath() + item.url, "target": "new"});
      }
      nameLink.appendText(item.name);
      nameLink.injectInside(nameSpan);
      nameSpan.injectInside(div);
      
      // add results
      div.injectInside($(this.id + "-results-list"));
      
      // hide the Add button (now this item is in the DOM) if in the selected list
      if (isSelected)
      {
         actionImg.setStyle("display", "none");
      }
   },
   
   getNodeData: function(parent, child, callback, scrollpos)
   {
      // show ajax wait panel
      $(this.id + '-ajax-wait').setStyle('display', 'block');
      $(this.id + '-results-list').setStyle('visibility', 'hidden');
      
      var picker = this;
      
      // execute ajax service call to retrieve list of child nodes as JSON response
      new Ajax(getContextPath() + "/ajax/invoke/" + this.service +
               "?parent=" + (parent != null ? parent : "") +
               (child != null ? ("&child=" + child) : "") +
               (this.requestAttributes!=null ? ("&" + this.requestAttributes) : ""),
      {
         method: 'get',
         onComplete: function(r)
         {
            if (r.startsWith("ERROR:") == false)
            {
               result = Json.evaluate(r);
               result.children.sort(picker.sortByName);
               
               callback(result, picker, scrollpos);
               
               // display results list again and hide ajax wait panel
               $(picker.id + '-results-list').setStyle('visibility', 'visible');
               $(picker.id + '-ajax-wait').setStyle('display', 'none');
            }
            else
            {
               // display results list again and hide ajax wait panel
               $(picker.id + '-results-list').setStyle('visibility', 'visible');
               $(picker.id + '-ajax-wait').setStyle('display', 'none');
               
               // display the error
               alert(r);
            }
         },
         onFailure: function (r)
         {
         }
      }).request();
   },
   
   sortByName: function(a, b)
   {
      if (a.selectable == b.selectable)
      {
         return ((a.name < b.name) ? -1 : ((a.name > b.name) ? 1 : 0));
      }
      else
      {
         return (a.selectable == false) ? -1 : 1;
      }
   }
});