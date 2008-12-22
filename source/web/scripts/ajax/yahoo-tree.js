//
// Alfresco Yahoo Tree support library
// Gavin Cornwell 01-12-2006
//
// NOTE: This script relies on common.js so therefore needs to be loaded
//       prior to this one on the containing HTML page.

var _loadDataUrl = null;
var _collapseUrl = null;
var _nodeSelectedHandler = null;

/**
 * Sets the URL to use to retrive child nodes
 */
function setLoadDataUrl(url)
{
	_loadDataUrl = url;
}

/**
 * Sets the URL to inform the server that a node collapsed
 */
function setCollapseUrl(url)
{
	_collapseUrl = url;
}

/**
 * Sets the name of the handler function to use for the node selected event
 */
function setNodeSelectedHandler(handler)
{
	_nodeSelectedHandler = handler;
}

/**
 * Callback method used by the tree to load the given node's children
 */
function loadDataForNode(node, onCompleteCallback) 
{
   if (_loadDataUrl == null)
   {
      alert("AJAX URL has not been set for retrieving child nodes, call setLoadDataUrl()!");
      return;
   }
   
   var nodeRef = node.data.nodeRef;
   
   // TODO: add method to add param to url
   var transaction = YAHOO.util.Connect.asyncRequest('GET', getContextPath() + _loadDataUrl + "&nodeRef=" + nodeRef, 
   {
      success: function(o)
      {
         var parentNode = o.argument[0];
         var data = o.responseXML.documentElement;
         
         // parse the child data to create the child nodes
         parseChildData(parentNode, data);

         // execute the callback method
         o.argument[1]();
      },
      failure: function(o)
      {
         if (o.status == 401)
         {
            document.location = window.location.protocol + "//" + window.location.host + getContextPath();
         }
         else
         {
            handleErrorYahoo("Error: Failed to retrieve children for node: " + o.argument[0].data.nodeRef);

            // execute the callback method
            o.argument[1]();
         }
      },
      argument: [node, onCompleteCallback]
   }
   , null);
}

/**
 * Parses the given data returned from the server into the required child nodes
 */
function parseChildData(parentNode, data)
{
   if (data != undefined && data != null)
   {
      var nodes = data.getElementsByTagName("node");
      
      for (var i = 0; i < nodes.length; i++)
      {
         var node = nodes[i];
         var nodeRef = node.getAttribute("ref");
         var name = node.getAttribute("name");
         var icon = node.getAttribute("icon");
         
         // create the new node
        	createYahooTreeNode(parentNode, nodeRef, name, icon, false, false);
      }
   }
   else
   {
      alert("No data returned from server!");
   }
}

/**
 * Generates an HTML tree node and adds it to the given parent node
 */
function createYahooTreeNode(parentNode, nodeRef, name, icon, expanded, selected)
{
	var nodeData = { label: name, nodeRef: nodeRef, icon: icon, selectedHandler: _nodeSelectedHandler};
   return new YAHOO.widget.AlfrescoNode(nodeData, parentNode, expanded, selected);
}

/**
 * Callback used to inform the server that the given node was collapsed in the UI
 */
function informOfCollapse(node)
{
   if (_collapseUrl == null)
   {
      alert("AJAX URL has not been set for collapsing nodes, call setCollapseUrl()!");
      return;
   }
   
   var nodeRef = node.data.nodeRef;
   
   // remove the children from the node so when it's expanded again it re-queries the server
   node.childrenRendered = false;
   node.dynamicLoadComplete = false;
   while (node.children.length) 
   {
      tree.removeNode(node.children[0], false);
   }
   
   // TODO: add method to add param to url
   var transaction = YAHOO.util.Connect.asyncRequest('GET', getContextPath() + _collapseUrl + "&nodeRef=" + nodeRef, 
   {
      success: function(o)
      {
         // nothing to do on the client
      },
      failure: function(o)
      {
         handleErrorYahoo("Error: Failed to collapse node: " + o.argument[0].data.nodeRef);
      },
      argument: [node]
   }
   , null);
}

/**
 * Extension of the default Node object. This node always shows the 
 * expand/collapse arrow and an icon for the node and 
 *
 * @namespace YAHOO.widget
 * @class AlfrescoNode
 * @extends YAHOO.widget.Node
 * @constructor
 * @param oData {object} an object containing the data that will
 * be used to render this node
 * @param oParent {YAHOO.widget.Node} this node's parent node
 * @param expanded {boolean} the initial expanded/collapsed state
 * @param selected {boolean} the initial selected state
 */
YAHOO.widget.AlfrescoNode = function(oData, oParent, expanded, selected) 
{
   if (oData) 
   { 
      this.init(oData, oParent, expanded);
      this.initContent(oData, selected);
   }
};

YAHOO.extend(YAHOO.widget.AlfrescoNode, YAHOO.widget.Node, 
{
   /**
    * The label for the node
    * @property label
    * @type string
    */
   label: null,
   
   /**
    * The nodeRef of the item this node is representing
    * @property nodeRef
    * @type string
    */
   nodeRef: null,
   
   /**
    * The name of the icon to use for the node, defaults to 'space-icon-default'.
    * @property icon
    * @type string
    */
   icon: "space-icon-default",
   
   /**
    * The number of pixels to indent a child node from it's parent.
    * @property indentSize
    * @type int
    */
   indentSize: 14,
   
   /**
    * The name of the function to call when this node is clicked.
    * @property selectedHandler
    * @type string
    */
   selectedHandler: null,
   
   /**
    * The node's selected state
    * @property selected
    * @type boolean
    */
   selected: false,
    
   /**
    * The generated id that will contain the data passed in by the implementer.
    * @property contentElId
    * @type string
    */
   contentElId: null,

   /**
    * Sets up the node label
    * @property initContent
    * @param {object} An object containing the nodeRef of the item, it's icon,
    *                 the selectedHandler and the ident size to use for child nodes
    */
   initContent: function(oData, selected) 
   {
      this.label = oData.label;
      this.nodeRef = oData.nodeRef;
      this.selected = selected;
      this.contentElId = "ygtvcontentel" + this.index;
      
      if (oData.icon != null)
      {
         this.icon = oData.icon;
      }
      
      if (oData.indentSize != null)
      {
         this.indentSize = oData.indentSize;
      }
      
      if (oData.selectedHandler != null)
      {
         this.selectedHandler = oData.selectedHandler;
      }
   },
   
   /**
    * Returns the outer html element for this node's content
    * @method getContentEl
    * @return {HTMLElement} the element
    */
   getContentEl: function() 
   { 
      return document.getElementById(this.contentElId);
   },

   // overrides YAHOO.widget.Node
   getNodeHtml: function() 
   { 
      var sb = [];

      sb[sb.length] = '<div class="treeNode"';

      // calculate the margin required depending on the depth of this node
      if (this.depth > 0)
      {
         var depthSize = this.indentSize * this.depth;
         sb[sb.length] = ' style="margin-left: ' + depthSize + 'px;"';
      }

      sb[sb.length] = '>';
      sb[sb.length] = '<table cellpadding="0" cellspacing="0" border="0"><tr>';
      
      // render the toggle image (take into account this.expanded)
      sb[sb.length] = '<td><img';
      sb[sb.length] = ' id="' + this.getToggleElId() + '"';
      sb[sb.length] = ' class="' + this.getStyle() + '"';
      sb[sb.length] = ' onclick="javascript:' + this.getToggleLink() + '"';
      sb[sb.length] = ' src="' + getContextPath() + "/images/icons/arrow_";
      if (this.expanded)
      {
         sb[sb.length] = 'open';
      }
      else
      {
         sb[sb.length] = 'closed';
      }
      sb[sb.length] = '.gif"></td>';
      
      // render the icon (with node selected handler)   
      sb[sb.length] = '<td';
      if (this.selectedHandler != null)
      {
         sb[sb.length] = ' onclick=' + this.selectedHandler + '("' + this.nodeRef + '");';
      }
      sb[sb.length] = '><img src="' + getContextPath() + '/images/icons/' + this.icon + '-16.gif"';
      sb[sb.length] = ' class="treeNodeIcon"></td>';
      
      // render the label (with node selected handler) (apply selected css class if approp.) (add contentElId)
      sb[sb.length] = '<td id="' + this.contentElId + '"';
      if (this.selectedHandler != null)
      {
         sb[sb.length] = ' onclick=' + this.selectedHandler + '("' + this.nodeRef + '");';
      }
      sb[sb.length] = '><span class="treeNodeLabel';
      if (this.selected)
      {
         sb[sb.length] = ' treeNodeSelected';
      }
      sb[sb.length] = '">&nbsp;' + this.label + '</span></td>';

      // close off the containing row, table and div
      sb[sb.length] = '</tr></table></div>';
      
      return sb.join("");
   },

   updateIcon: function()
   {
      var el = this.getToggleEl();
      if (el)
      {
         if (this.isLoading)
         {
            el.src = getContextPath() + '/scripts/ajax/yahoo/treeview/assets/loading.gif';
         }
         else if (this.expanded)
         {
            el.src = getContextPath() + '/images/icons/arrow_open.gif';
         }
         else
         {
            el.src = getContextPath() + '/images/icons/arrow_closed.gif';
         }
      }
   },
    
   toString: function() 
   { 
      return "AlfrescoNode (" + this.index + ")";
   }
});




