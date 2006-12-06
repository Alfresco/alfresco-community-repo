var loadDataUrl = null;
var collapseUrl = null;
var nodeSelectedHandler = null;

/**
 * Sets the URL to use to retrive child nodes
 */
function setLoadDataUrl(url)
{
	loadDataUrl = url;
}

/**
 * Sets the URL to inform the server that a node collapsed
 */
function setCollapseUrl(url)
{
	collapseUrl = url;
}

/**
 * Sets the name of the handler function to use for the node selected event
 */
function setNodeSelectedHandler(handler)
{
	nodeSelectedHandler = handler;
}

/**
 * Callback method used by the tree to load the given node's children
 */
function loadDataForNode(node, onCompleteCallback) 
{
   if (loadDataUrl == null)
   {
      alert("AJAX URL has not been set for retrieving child nodes, call setLoadDataUrl()!");
      return;
   }
   
   var nodeRef = node.data.nodeRef;
   
   // TODO: add method to add param to url
   var transaction = YAHOO.util.Connect.asyncRequest('GET', WEBAPP_CONTEXT + loadDataUrl + "&nodeRef=" + nodeRef, 
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
         handleErrorYahoo("Error: Failed to retrieve children for node: " + o.argument[0].data.nodeRef);
         
         // execute the callback method
         o.argument[1]();
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
	var nodeHtml = "<table><tr";
	
	// add the node selected handler is provided
	if (nodeSelectedHandler != null)
	{
		nodeHtml += " onclick=" + nodeSelectedHandler + "('" + nodeRef + "');"
	}
	
	nodeHtml += "><td><img src='" + WEBAPP_CONTEXT + "/images/icons/" + icon + 
	            "-16.gif'/></td><td class='alflabel";
	
	// add selected class to label if node is selected
	if (selected)
	{
		nodeHtml += " alflabelselected";
	}
	
	nodeHtml += "'>" + name + "</td></tr></table>";

   return new YAHOO.widget.HTMLNode({ html: nodeHtml, nodeRef: nodeRef, icon: icon }, parentNode, expanded, 1);
}

/**
 * Callback used to inform the server that the given node was collapsed in the UI
 */
function informOfCollapse(node)
{
   if (collapseUrl == null)
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
   var transaction = YAHOO.util.Connect.asyncRequest('GET', WEBAPP_CONTEXT + collapseUrl + "&nodeRef=" + nodeRef, 
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


