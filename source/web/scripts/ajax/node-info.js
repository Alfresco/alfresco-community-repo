//
// Supporting JavaScript for the NodeInfo component
// Gavin Cornwell 17-07-2006
//

var _launchElement = null;
var _popupElement = null;

/**
 * Makes the AJAX request back to the server to get the node info.
 *
 * @param nodeRef The node reference to get information for
 * @param launchElement The element that requested the summary panel
 */
function showNodeInfo(nodeRef, launchElement)
{
   _launchElement = launchElement;
   
   dojo.io.bind({
      method: 'post',
      url: WEBAPP_CONTEXT + '/ajax/invoke/NodeInfoBean.sendNodeInfo',
      content: { noderef: nodeRef },
      load: showNodeInfoHandler,
      error: handleErrorDojo,
      mimetype: 'text/html'
   });
}

/**
 * Fades in the summary panel containing the node information.
 * This function is called back via the dojo bind call above.
 */
function showNodeInfoHandler(type, data, evt)
{
   // create a 'div' to hold the summary table
   var div = document.createElement("div");
   
   // get the position of the element we are showing info for
   var pos = dojo.style.getAbsolutePosition(_launchElement, false);
   
   // setup the div with the correct appearance
   div.innerHTML = data;
   div.setAttribute("class", "summaryPopupPanel");
   // NOTE: use className for IE
   div.setAttribute("className", "summaryPopupPanel");
   div.style.position = "absolute";
   div.style.left = pos[0];
   div.style.top = pos[1] + 16;
   div.style.zIndex = 99;
   
   // is there a better way of doing this, dojo.dom.insertBefore??
   var body = document.getElementsByTagName("body")[0];
   dojo.style.setOpacity(div, 0);
   _popupElement = div;
   body.appendChild(div);
   
   dojo.lfx.html.fadeIn(div, 300).play();
}

/**
 * Fades out the summary panel with the node info
 * and then removes it from the DOM
 */
function hideNodeInfo()
{
   // remove the node from the DOM and reset variables
   dojo.lfx.html.fadeOut(_popupElement, 300, dojo.lfx.easeOut, function(nodes)
   {
      dojo.lang.forEach(nodes, dojo.dom.removeNode);
      _popupElement = null;
      _launchElement = null;
   }).play();
}
