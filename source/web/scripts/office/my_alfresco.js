/*
 * Prerequisites: mootools.v1.1.js
 *                office_addin.js
 */
var OfficeMyAlfresco =
{
   TOGGLE_AMOUNT: 150,
   ANIM_LENGTH: 800,

   init: function()
   {
      OfficeAddin.sortTasks($('taskList'));
      OfficeMyAlfresco.setupTasks();
      OfficeMyAlfresco.setupToggles();
   },

   setupTasks: function()
   {
      var tasks = $$('#taskList .taskItem');
      
      tasks.each(function(task, i)
      {
         // register 'mouseenter' event for each task
         task.addEvent('mouseenter', function(e)
         {
            // highlight the item title
            task.addClass('taskItemSelected');

            // reset styles on all closed tasks
            tasks.each(function(otherTask, j)
            {
               if (otherTask != task)
               {
                  // reset selected class
                  otherTask.removeClass('taskItemSelected');
               }
            });
         });
            
         // register 'mouseleave' event for each task
         task.addEvent('mouseleave', function(e)
         {
            // unhighlight the item title
            task.removeClass('taskItemSelected');
         });
         
         // register 'click' event for each task
         task.addEvent('click', function(e)
         {
            window.location.href = window.serviceContextPath + "/office/myTasks?p=" + window.queryObject.p + "&t=" + task.id;
         });
      });

      $('taskList').addEvent('mouseleave', function(e)
      {
         // handler for mouse leaving the entire task list
         tasks.each(function(task, i)
         {
            task.removeClass('taskItemSelected');
         });
      });
   },
   
   setupToggles: function()
   {
      // Elements of interest
      var panels = $$('.togglePanel');
      var toggles = $$('.toggle');

      // Animation
      var fxPanel = new Fx.Elements(panels, {wait: false, duration: OfficeMyAlfresco.ANIM_LENGTH, transition: Fx.Transitions.Back.easeInOut});
      
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
                  'height': [panel.getStyle('height').toInt(), panel.defaultHeight + (OfficeMyAlfresco.TOGGLE_AMOUNT * (panels.length - 1))]
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
                        'height': [otherPanel.getStyle('height').toInt(), otherPanel.defaultHeight - OfficeMyAlfresco.TOGGLE_AMOUNT]
                     };
                  }
               });
            }
            fxPanel.start(animPanel);
         });
      });
   }
};

window.addEvent('domready', OfficeMyAlfresco.init);