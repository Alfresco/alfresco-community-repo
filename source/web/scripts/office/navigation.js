/*
 * Prerequisites: mootools.v1.1.js
 *                office_addin.js
 */
var OfficeNavigation =
{
   TOGGLE_AMOUNT: 150,
   ANIM_LENGTH: 800,
   
   init: function()
   {
      OfficeNavigation.setupToggles();
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
   }
};

window.addEvent('domready', OfficeNavigation.init);