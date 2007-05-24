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
      
      // create a 'div' to hold the summary table - extend with mootools prototypes
      var div = $(document.createElement("div"));
      
      // setup the div with the correct appearance
      div.innerHTML = response.responseText;
      div.setAttribute("class", "summaryDropShadow");
      // NOTE: use className for IE
      div.setAttribute("className", "summaryDropShadow");
      div.setStyle('position', "absolute");
      div.setStyle('display', "none");
      div.setStyle('left', 0);
      div.setStyle('top', 0);
      
      var body = document.getElementsByTagName("body")[0];
      body.appendChild(div);
      
      // store a ref to this panel outer object
      div.panel = panel;
      panel.loading = false;
      
      // drag-drop object
      new Drag.Move(div,
         {
            onStart : function(el)
            {
               el.setStyle("zIndex", _zIndex++);
            }
         });
      
      // keep track of the div element we created
      panel.popupElement = div;
      
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
            this.popupElement.setStyle("opacity", 0);
            this.popupElement.setStyle("display", "block");
            this.popupElement.setStyle("zIndex", _zIndex++);   // pop to front
            
            Alfresco.Dom.smartAlignElement(this.popupElement, this.launchElement, 700);
            
            // animate the fade-in transition
            var fxAnim = new Fx.Style(this.popupElement, 'opacity',
            {
               duration: 300,
               transition: Fx.Transitions.linear,
               onComplete: function()
               {
                  this.element.panel.visible = true;
               }
            });
            fxAnim.start(0, 1);
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
         // fade out and set the visiblilty flag on complete of the anim
         /*var fxAnim = new Fx.Style(this.popupElement, 'opacity',
         {
            duration: 300,
            transition: Fx.Transitions.linear,
            onComplete: function()
            {
               this.element.panel.visible = false;
            }
         });
         fxAnim.start(1, 0);*/
         this.popupElement.setStyle("opacity", 0);
         this.popupElement.setStyle("display", "none");
         this.popupElement.panel.visible = false;
      }
   }
}
