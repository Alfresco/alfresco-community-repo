//
// Supporting JavaScript for the Summary Info pop-up panel objects
// Kevin Roast 24-04-2007
//
// NOTE: This script requires common.js - which needs to be loaded
//       prior to this one on the containing HTML page.

var _zIndex = 99;

/**
 * Panel Manager constructor
 * 
 * @param serverCall    Server call to make on display e.g. NodeInfoBean.sendNodeInfo
 * @param argName       Argument name to pass panel ID object as e.g. nodeRef
 */
Alfresco.PanelManager = function(serverCall, argName, template)
{
   this.serverCall = serverCall;
   this.argName = argName;
   this.template = template;
}

/**
 * Definition of the Panel Manager class.
 * Responsible for open/closing InfoPanel dynamic summary panel objects.
 */
Alfresco.PanelManager.prototype =
{
   panels: [],
   displayed: [],
   serverCall: null,
   argName: null,
   template: null,
   
   /**
    * Request toggle of the open/close state of an info panel
    */
   toggle: function(id, launchElement)
   {
      if (this.displayed[id] == undefined || this.displayed[id] == null)
      {
         var panel = this.panels[id];
         if (panel == undefined || panel == null)
         {
            panel = new Alfresco.InfoPanel(this, id, launchElement);
            this.panels[id] = panel;
         }
         this.displayed[id] = true;
         panel.showInfo();
      }
      else
      {
         this.close(id);
      }
   },
   
   /**
    * Request a Close of the Summary info panel
    */
   close: function(id)
   {
      var panel = this.panels[id];
      if (panel != undefined && panel != null)
      {
         this.displayed[id] = null;
         panel.hideInfo();
      }
   },
   
   /**
    * Return if a given info panel is currently displayable
    */
   displayable: function(id)
   {
      return (this.displayed[id] != undefined && this.displayed[id] != null);
   }
}


/**
 * Constructor for the Info Panel object
 */
Alfresco.InfoPanel = function(manager, id, launchElement)
{
   this.manager = manager;
   this.id = id;
   this.launchElement = launchElement;
}

/**
 * Definition of the Info Panel object
 */
Alfresco.InfoPanel.prototype = 
{
   manager: null,
   id: null,
   launchElement: null,
   popupElement: null,
   visible: false,
   loading: false,
   
   /**
    * Makes the AJAX request back to the server to get the panel info.
    */
   showInfo: function()
   {
      if (this.loading == false)
      {
         if (this.popupElement == null)
         {
            this.loading = true;
            
            var elImg = Alfresco.Dom.getElementByTagName(this.launchElement, "img");
            if (elImg != null)
            {
               elImg.src = getContextPath() + "/images/icons/ajax_anim.gif";
            }
            
            YAHOO.util.Connect.asyncRequest(
               "POST",
               getContextPath() + '/ajax/invoke/' + this.manager.serverCall,
               { 
                  success: this.loadInfoHandler,
                  failure: handleErrorYahoo,    // global error handler
                  argument: [this]
               }, 
               this.manager.argName + "=" + this.id +
               (this.manager.template != null ? ("&template=" + this.manager.template) : ""));
         }
         else
         {
            this.displayInfo();
         }
      }
   },
   
   /**
    * Callback function for showInfo() above
    */
   loadInfoHandler: function(response)
   {
      var panel = response.argument[0];
      
      // create a 'div' to hold the summary table
      var div = document.createElement("div");
      
      // setup the div with the correct appearance
      div.innerHTML = response.responseText;
      div.setAttribute("class", "summaryDropShadow");
      // NOTE: use className for IE
      div.setAttribute("className", "summaryDropShadow");
      div.style.position = "absolute";
      div.style.display = "none";
      div.style.left = 0;
      div.style.top = 0;
      
      var body = document.getElementsByTagName("body")[0];
      body.appendChild(div);
      
      // keep track of the div element we created
      panel.popupElement = div;
      panel.loading = false;
      
      // display the div for the first time
      panel.displayInfo();
   },
   
   /**
    * Display the summary info panel for the panel
    */
   displayInfo: function()
   {
      var elImg = Alfresco.Dom.getElementByTagName(this.launchElement, "img");
      if (elImg != null)
      {
         elImg.src = getContextPath() + "/images/icons/popup.gif";
      }
      
      if (this.manager.displayable(this.id) == true)
      {
         if (this.popupElement != null && this.visible == false)
         {
            // set opacity in browser independant way
            YAHOO.util.Dom.setStyle(this.popupElement, "opacity", 0.0);
            this.popupElement.style.display = "block";
            this.popupElement.style.zIndex = _zIndex++;   // pop to front
            
            Alfresco.Dom.smartAlignElement(this.popupElement, this.launchElement, 700);
            
            var anim = new YAHOO.util.Anim(
               this.popupElement, { opacity: { to: 1.0 } }, 0.333, YAHOO.util.Easing.easeOut);
            anim.animate();
            
            // drag-drop object
            new YAHOO.util.DD(this.popupElement);
            
            this.visible = true;
         }
      }
   },
   
   /**
    * Hide the summary info panel
    */
   hideInfo: function()
   {
      if (this.popupElement != null && this.visible == true)
      {
         this.visible = false;
         
         YAHOO.util.Dom.setStyle(this.popupElement, "opacity", 0.0);
         this.popupElement.style.display = "none";
      }
   }
}
