/*
 * Prerequisites: mootools.v1.1.js
 *                office_addin.js
 */
var OfficeNavigation =
{
   TOGGLE_AMOUNT: 150,
   ANIM_LENGTH: 800,
   CREATE_SPACE_HEIGHT: 108,
   CREATE_SPACE_TEMPLATE: 16,
   OVERLAY_ANIM_LENGTH: 300,
   OVERLAY_OPACITY: 0.7,
   
   init: function()
   {
      $('overlayPanel').setStyle('opacity', 0);
      OfficeNavigation.setupToggles();
      OfficeNavigation.setupCreateSpace();

      // Did we arrive here from the "Create collaboration space" shortcut?      
      if (window.queryObject.cc)
      {
         OfficeNavigation.showCreateSpace();
      }

   },
   
   setupToggles: function()
   {
      // Elements of interest
      var panels = $$('.togglePanel');
      var toggles = $$('.toggle');

      // Animation
      var fxPanel = new Fx.Elements(panels, {wait: false, duration: OfficeNavigation.ANIM_LENGTH, transition: Fx.Transitions.Back.easeInOut});
      
      panels.each(function(panel, i)
      {
         toggle = toggles[i];

         panel.defaultHeight = panel.getStyle('height').toInt();
         panel.isToggled = false;
         
         toggle.addEvent('click', function(e)
         {
            var animPanel = {};
            
            if (panel.isToggled)
            {
               panel.isToggled = false;
               this.removeClass('toggled');
               animPanel[i] =
               {
                  'height': [panel.getStyle('height').toInt(), panel.defaultHeight]
               };
               
               // reset all other panels
               panels.each(function(otherPanel, j)
               {
                  if (otherPanel != panel)
                  {
                     // reset panel
                     otherPanel.isToggled = false;
                     toggles[j].removeClass('toggled');
                     animPanel[j] =
                     {
                        'height': [otherPanel.getStyle('height').toInt(), otherPanel.defaultHeight]
                     };
                  }
               });
            }
            else
            {
               panel.isToggled = true;
               this.addClass('toggled');
               animPanel[i] =
               {
                  'height': [panel.getStyle('height').toInt(), panel.defaultHeight + (OfficeNavigation.TOGGLE_AMOUNT * (panels.length - 1))]
               };

               // set all other panels
               panels.each(function(otherPanel, j)
               {
                  if (otherPanel != panel)
                  {
                     // set panel
                     otherPanel.isToggled = false;
                     toggles[j].removeClass('toggled');
                     animPanel[j] =
                     {
                        'height': [otherPanel.getStyle('height').toInt(), otherPanel.defaultHeight - OfficeNavigation.TOGGLE_AMOUNT]
                     };
                  }
               });
            }
            fxPanel.start(animPanel);
         });
      });
   },
   
   setupCreateSpace: function()
   {
      var panel = $('createSpacePanel');
      panel.defaultHeight = 0;
      panel.setStyle('display', 'block');
      panel.setStyle('height', panel.defaultHeight);
   },
   
   showCreateSpace: function()
   {
      var panel = $('createSpacePanel');
      // Animation
      var fxPanel = new Fx.Style(panel, 'height',
      {
         duration: OfficeNavigation.ANIM_LENGTH,
         transition: Fx.Transitions.Back.easeOut,
         onComplete: function()
         {
            $('spaceName').focus();
         }
      });

      if (!panel.isOpen)
      {      
         panel.isOpen = true;
         var openHeight = OfficeNavigation.CREATE_SPACE_HEIGHT;
         if ($('spaceTemplate'))
         {
            openHeight += OfficeNavigation.CREATE_SPACE_TEMPLATE;
         }
         fxPanel.start(panel.getStyle('height').toInt(), openHeight);
      }
      else
      {
         OfficeNavigation.hideCreateSpace();
      }
   },

   hideCreateSpace: function()
   {
      var panel = $('createSpacePanel');
      // Animation
      var fxPanel = new Fx.Style(panel, 'height',
      {
         duration: OfficeNavigation.ANIM_LENGTH,
         transition: Fx.Transitions.Back.easeIn,
         onComplete: function()
         {
            panel.isOpen = false;
         }
      });
      
      fxPanel.start(panel.getStyle('height').toInt(), panel.defaultHeight);
   },

   submitCreateSpace: function(commandURL, nodeId)
   {
      var spcName = $('spaceName').value,
         spcTitle = $('spaceTitle').value,
         spcDescription = $('spaceDescription').value;
         
      var spcTemplate;
      if ($('spaceTemplate'))
      {
         spcTemplate = $('spaceTemplate').value;
      }

      OfficeAddin.showStatusText("Creating space...", "ajax_anim.gif", false);
      var actionURL = commandURL + "?a=newspace&n=" + nodeId;
      actionURL += "&sn=" + encodeURI(spcName);
      actionURL += "&st=" + encodeURI(spcTitle);
      actionURL += "&sd=" + encodeURI(spcDescription);
      actionURL += "&t=" + encodeURI(spcTemplate);
      var myAjax = new Ajax(actionURL, {
         method: 'get',
         headers: {'If-Modified-Since': 'Sat, 1 Jan 2000 00:00:00 GMT'},
         onComplete: function(textResponse, xmlResponse)
         {
            // Remove any trailing hash
            var href = window.location.href.replace("#", "")
            // Remove any "st" and "cc" parameters
            href = OfficeAddin.removeParameters(href, "st|cc");
            // Optionally add a status string
            if (textResponse != "")
            {
               href += (href.indexOf("?") == -1) ? "?" : "&";
               href += "st=" + encodeURI(textResponse);
            }
            window.location.href = href;
         }
      });
      myAjax.request();
   },
   
   saveToAlfresco: function(currentPath)
   {
      // Does the current doc have an extension?
      if (!window.external.docHasExtension())
      {
         // No - we need to ask for a filename
         OfficeNavigation.showSaveFilenamePanel(currentPath);
      }
      else
      {
         window.external.saveToAlfresco(currentPath);
      }
   },
   
   showSaveFilenamePanel: function(currentPath)
   {   
      this.fxOverlay = $("overlayPanel").effect('opacity',
      {
         duration: OfficeNavigation.OVERLAY_ANIM_LENGTH
      });

      var panel = $("saveDetailsPanel");
      panel.setStyle("opacity", 0);
      panel.setStyle("display", "inline");
            
      var anim = new Fx.Styles(panel,
      {
         duration: OfficeNavigation.ANIM_LENGTH,
         transition: Fx.Transitions.linear,
         onComplete: function()
         {
            $('saveFilename').addEvent('keydown', function(event)
            {
               event = new Event(event);
               if (event.key == 'enter')
               {
                  OfficeNavigation.saveOK();
               }
               else if (event.key == 'esc')
               {
                  OfficeNavigation.saveCancel();
               }
            });
            $('saveFilename').focus();
         }
      }).start({'opacity': 1});

      this.fxOverlay.start(OfficeNavigation.OVERLAY_OPACITY);
      this.popupPanel = panel;
      this.popupPanel.currentPath = currentPath;
   },
   
   saveOK: function()
   {
      var filename = $('saveFilename').value;
      var currentPath = this.popupPanel.currentPath;
      if (filename.length > 0)
      {
         window.external.saveToAlfrescoAs(currentPath, filename);
      }
      OfficeNavigation.saveCancel();
   },
   
   saveCancel: function()
   {
      if (this.popupPanel != null)
      {
         this.popupPanel.setStyle("display", "none");
         this.popupPanel = null;
      }
      if (this.fxOverlay)
      {
         this.fxOverlay.start(0);
      }
   }
};

window.addEvent('domready', OfficeNavigation.init);