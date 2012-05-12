var MyTasks = {
   ANIM_LENGTH: 300,
   Filter: null,
   ServiceContext: null,
   
   start: function()
   {
      if ($('taskPanel'))
      {
         var messagePanel = $("taskMessagePanel");
         messagePanel.setStyle('opacity', 0);
         messagePanel.setStyle('display', 'block');

         // fire off the ajax request to populate the task panel - the 'mytaskspanel' webscript
         // is responsible for rendering just the contents of the main panel div
         YAHOO.util.Connect.asyncRequest(
            "GET",
            MyTasks.ServiceContext + '/ui/mytaskspanel?f='+MyTasks.Filter,
            {
               success: function(response)
               {
                  // push the response into the task panel div
                  $('taskPanel').setHTML(response.responseText);
                  // extract the count value from a hidden div and display it
                  $('taskCount').setHTML($('taskCountValue').innerHTML);
                  // wire up all the events and animations
                  MyTasks.init();
               },
               failure: function(response)
               {
                  // display the error
                  $('taskPanel').setHTML($('displayTheError').innerHTML);
                  
                  // hide the ajax wait panel and show the main task panel
                  $('taskPanelOverlay').setStyle('visibility', 'hidden');
                  $('taskPanel').setStyle('visibility', 'visible');
               }
            }
         );
      }
   },
   
   init: function()
   {
      MyTasks.sortTasks();
      MyTasks.parseTaskPanels();
      
      // hide the ajax wait panel and show the main task panel
      $('taskPanelOverlay').setStyle('visibility', 'hidden');
      $('taskPanel').setStyle('visibility', 'visible');

      if (MyTasks.postInit)
      {
         MyTasks.postInit();
         MyTasks.postInit = null;
      }
   },

   parseTaskPanels: function()
   {
      var tasks = $$('#taskPanel .taskRow');
      var items = $$('#taskPanel .taskItem');
      var infos = $$('#taskPanel .taskInfo');
      var details = $$('#taskPanel .taskDetail');
      var resources = $$('#taskPanel .taskResources');
      var fxInfo = new Fx.Elements(infos, {wait: false, duration: MyTasks.ANIM_LENGTH, transition: Fx.Transitions.sineInOut});
      var fxDetail = new Fx.Elements(details, {wait: false, duration: MyTasks.ANIM_LENGTH, transition: Fx.Transitions.sineInOut,
         onComplete: function()
         {
            // event handler to ensure scrollable area style is set
            this.elements.each(function(detail, i)
            {
               if (detail.parentNode.isOpen == true)
               {
                  $E('.taskResources', detail).setStyle('overflow', 'auto');
               }
            });
         }
      });
      tasks.each(function(task, i)
      {
         var item = items[i];
         var info = infos[i];
         var detail = details[i];
         var resource = resources[i];

         // animated elements defaults
         task.isOpen = false;
         task.loadingResources = false;
         item.defBColor = (item.getStyle('background-color') == 'transparent') ? '' : item.getStyle('background-color');
         detail.defHeight = detail.offsetHeight + 3; // Add some extra spacing for the button
         detail.setStyle('opacity', 0);
         detail.setStyle('display', 'block');
         detail.setStyle('height', 1);

         info.setStyle('opacity', 0);

         // register 'mouseenter' event for each task
         task.addEvent('mouseenter', function(e)
         {
            if (task.isOpen)
               return;

            // highlight the item title
            task.addClass('taskItemSelected');

            // fade in info button
            var animInfo = {};
            var infoOpacity = info.getStyle('opacity');
            if (infoOpacity != 1)
            {
               animInfo[i] = {'opacity': [infoOpacity, 1]};
            }

            // reset styles on all closed tasks
            tasks.each(function(otherTask, j)
            {
               var otherInfo = infos[j];
               
               if ((otherTask != task) && (!otherTask.isOpen))
               {
                  // reset selected class
                  otherTask.removeClass('taskItemSelected');
                  // does the info button need fading out
                  var otherOpacity = otherInfo.getStyle('opacity');
                  if (otherOpacity != 0)
                  {
                     animInfo[j] = {'opacity': [otherOpacity, 0]};
                  }
               }
            });
            fxInfo.start(animInfo);
         });

         // register 'mouseleave' event for each task
         task.addEvent('mouseleave', function(e)
         {
            if (task.isOpen)
               return;

            // unhighlight the item title
            task.removeClass('taskItemSelected');

            // fade out info button
            var animInfo = {};
            var infoOpacity = info.getStyle('opacity');
            if (infoOpacity != 0)
            {
               animInfo[i] = {'opacity': [infoOpacity, 0]};
            }

            // reset styles on all closed tasks - needed in case any element is mid-animation
            tasks.each(function(otherTask, j)
            {
               var otherInfo = infos[j];
               
               if ((otherTask != task) && (!otherTask.isOpen))
               {
                  // does the info button need fading out
                  var otherOpacity = otherInfo.getStyle('opacity');
                  if (otherOpacity != 0)
                  {
                     animInfo[j] = {'opacity': [otherOpacity, 0]};
                  }
               }
            });
            fxInfo.start(animInfo);
         });

         // register 'click' event for each task
         task.addEvent('click', function(e)
         {
            var animInfo = {},
               animDetail = {},
               infoOpacity = info.getStyle('opacity');
            
            if (!task.isOpen)
            {
               // open up this task
               // flag this task as open
               task.isOpen = true;
               task.addClass("taskItemSelected");
               task.addClass("taskItemSelectedOpen");
               
               if (task.loadingResources == false)
               {
                  // ajax call to populate the task resource list
                  task.loadingResources = true;
                  YAHOO.util.Connect.asyncRequest(
                     "POST",
                     getContextPath() + '/ajax/invoke/TaskInfoBean.sendTaskResources',
                     { 
                        success: function(response)
                        {
                           // set the resource list panel html
                           var resource = response.argument[0];
                           resource.innerHTML = response.responseText;
                        },
                        failure: handleErrorYahoo,    // global error handler
                        argument: [resource]
                     },
                     "taskId=" + task.id);
               }
               
               // fade in info button
               animInfo[i] = {'opacity': [infoOpacity, 1]};

               // slide and fade in the details panel
               animDetail[i] = {
                  'height': [1, detail.defHeight],
                  'opacity': [detail.getStyle('opacity'), 1]};

               // close other open tasks and toggle this one if it's already open
               tasks.each(function(otherTask, j)
               {
                  var otherInfo = infos[j],
                      otherDetail = details[j];
                  
                  if (otherTask != task)
                  {
                     // close any other open tasks
                     otherTask.isOpen = false;

                     // unhighlight the item title
                     otherTask.removeClass('taskItemSelected');
                     otherTask.removeClass("taskItemSelectedOpen");

                     // does this task detail panel need resetting back to it's default height?
                     var otherHeight = otherDetail.offsetHeight;
                     if (otherHeight != 1)
                     {
                        animDetail[j] = {
                           'height': [otherHeight, 1],
                           'opacity': [otherDetail.getStyle('opacity'), 0]};
                     }
                     // does the info button need fading out?
                     var otherOpacity = otherInfo.getStyle('opacity');
                     if (otherOpacity != 0)
                     {
                        animInfo[j] = {'opacity': [otherOpacity, 0]};
                     }
                     
                     $E('.taskResources', otherDetail).setStyle('overflow', 'hidden');
                  }
               });
            }
            else
            {
               // close this task
               // flag this task as closed
               task.isOpen = false;
               task.removeClass("taskItemSelectedOpen");
               
               // fade in info button
               animInfo[i] = {'opacity': [infoOpacity, 1]};

               // reset task back to it's default height
               animDetail[i] = {
                  'height': [detail.defHeight, 1],
                  'opacity': [detail.getStyle('opacity'), 0]};
               
               $E('.taskResources', detail).setStyle('overflow', 'hidden');
            }
            fxInfo.start(animInfo);
            fxDetail.start(animDetail);
         });
      });

      $('taskPanel').addEvent('mouseleave', function(e)
      {
         var animInfo = {};
         // handler for mouse leaving the entire task panel
         tasks.each(function(task, i)
         {
            if (!task.isOpen)
            {
               task.removeClass('taskItemSelected');
               animInfo[i] = {'opacity': [infos[i].getStyle('opacity'), 0]};
            }
            else
            {
               animInfo[i] = {'opacity': [infos[i].getStyle('opacity'), 1]};
            }
         });
         fxInfo.start(animInfo);
      });
   },
   
   transitionTask: function(commandUrl, successMessage)
   {
      YAHOO.util.Connect.asyncRequest(
         "GET",
         getContextPath() + commandUrl,
         { 
            success: function(response)
            {
               MyTasks.displayMessage(successMessage);
               MyTasks.refreshList();
            }.delay(500),
            failure: function(e)
            {
               alert(e.status + " : ERROR failed to transition task.");
            }
         }
      );
   },
   
   /**
    * Refresh the main data list contents within the taskPanel container
    */
   refreshList: function(reopenActive)
   {
      // do we want to remember which panel was open?
      if (reopenActive)
      {
         // do we have an open panel?
         var openPanel = $E('#taskPanel .taskItemSelected');
         var openPanelId = null;
         if (openPanel != null)
         {
            openPanelId = openPanel.id;
            // Re-open the panel if the id still exists
            MyTasks.postInit = function()
            {
               if ($(openPanelId))
               {
                  $(openPanelId).fireEvent("click");
   
                  // scroll the open panel into view               
                  var fxScroll = new Fx.Scroll($('taskPanel'),
                  {
                     duration: MyTasks.ANIM_LENGTH,
                     transition: Fx.Transitions.linear
                  });
                  fxScroll.toElement($(openPanelId));
               }
            }
         }
      }

      // empty the main panel div and restart by reloading the panel contents
      var taskPanel = $('taskPanel');
      taskPanel.setStyle('visibility', 'hidden');
      // show the ajax wait panel
      $('taskPanelOverlay').setStyle('visibility', 'visible');
      taskPanel.empty();
      taskPanel.removeEvents('mouseleave');
      MyTasks.start();
   },
   
   /**
    * Update the view filter
    */
   filter: function(filter)
   {
      $$('#taskFilterBar li').each(function(filterLink, i)
      {
         if (i == filter)
         {
            filterLink.addClass("taskCurrent");
         }
         else
         {
            filterLink.removeClass("taskCurrent");
         }
      });
      MyTasks.Filter = filter;
      MyTasks.refreshList();
   },
   
   sortTasks: function()
   {
      var taskArray = new Array();
      $$('#taskPanel .taskRow').each(function(taskDiv, i)
      {
         taskArray[i] = {dueDate: taskDiv.getProperty('rel'), theDiv: taskDiv.clone()};
      });

      taskArray.sort(MyTasks.sortByDueDate);
      var taskPanel = $('taskPanel');
      taskPanel.empty();
      taskPanel.removeEvents('mouseleave');

      for(var i = 0; i < taskArray.length; i++)
      {
         taskArray[i].theDiv.injectInside(taskPanel);
      }
   },
   
   sortByDueDate: function(a, b)
   {
      var x = a.dueDate;
      var y = b.dueDate;
      return ((x < y) ? -1 : ((x > y) ? 1 : 0));
   },

   /**
    * Called when the Manage Task dialog returns
    */
   manageTaskCallback: function()
   {
      // The manage task dialog window has closed
      MyTasks.refreshList(true);
   },
   
   /**
    * Display a message bubble of helpful info to the user. Calling this function in quick 
    * succession will cause previous message to be lost as the new ones are displayed.
    * 
    * @param message    Message text to display
    */
   displayMessage: function(message)
   {
      var panel = $("taskMessagePanel");
      if ($defined(panel.timeout))
      {
         clearTimeout(panel.timeout);
         panel.timeout = null;
      }
      
      panel.setStyle("opacity", 0);
      panel.setStyle("margin-top", -90);
      
      panel.getChildren()[1].setHTML(message);
      
      // reset the close box animation by refreshing the image source
      $("taskMessagePanelCloseImage").src = getContextPath() + "/images/icons/close_portlet_animation.gif";
      
      panel.fxMessage = new Fx.Styles(panel, 
      {
         duration: 1000,
         transition: Fx.Transitions.sineInOut
      });
      panel.fxMessage.start({'margin-top': -70, 'opacity': [0, 0.75]});

      
      panel.timeout = window.setTimeout(this.fadeOutMessage, 9000);
   },
   
   /**
    * Timer callback function to fade out the message panel
    */
   fadeOutMessage: function()
   {
      var panel = $("taskMessagePanel");
      panel.timeout = null;
      
      var fxMessage = new Fx.Styles(panel, 
      {
         duration: 1000,
         transition: Fx.Transitions.sineInOut
      });
      fxMessage.start({'margin-top': -90, 'opacity': [0]});
   },
   
   /**
    * Close the message panel immediately when the user clicks the close icon
    */
   closeMessage: function()
   {
      var panel = $("taskMessagePanel");
      if ($defined(panel.timeout))
      {
         clearTimeout(panel.timeout);
         panel.timeout = null;
      }
      panel.fxMessage.stop();
      panel.setStyle("opacity", 0);
   }   
};

window.addEvent('load', MyTasks.start);