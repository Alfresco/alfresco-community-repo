var MyTasks = {

   start: function()
   {
      if ($('taskPanel'))
      {
         MyTasks.parseTaskPanels();
      }
   },

   parseTaskPanels: function()
   {
      var tasks = $$('#taskPanel .taskRow');
      var items = $$('#taskPanel .taskItem');
      var infos = $$('#taskPanel .taskInfo');
      var details = $$('#taskPanel .taskDetail');
      var resources = $$('#taskPanel .taskResources');
      var fxInfo = new Fx.Elements(infos, {wait: false, duration: 300, transition: Fx.Transitions.linear});
      var fxDetail = new Fx.Elements(details, {wait: false, duration: 300, transition: Fx.Transitions.linear,
         onComplete: function()
         {
            // event handler to ensure 
            this.elements.each(function(detail, i)
            {
               if (detail.parentNode.isOpen == true)
               {
                  detail.getElementsByTagName("div")[2].setStyle('overflow', 'auto');
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
         detail.defHeight = 0;
         detail.setStyle('opacity', 0);
         detail.setStyle('display', 'block');
         detail.setStyle('height', 0);
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
               detailHeight = detail.getStyle('height').toInt(),
               infoOpacity = info.getStyle('opacity');
            
            if (!task.isOpen)
            {
               // open up this task
               // flag this task as open
               task.isOpen = true;
               
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
                  'height': [detailHeight, detail.defHeight + 140],
                  'opacity': [detail.getStyle('opacity'), 1]};

               // close other open tasks and toggle this one if it's already open
               tasks.each(function(otherTask, j)
               {
                  var otherItem = items[j],
                      otherInfo = infos[j],
                      otherDetail = details[j];
                  
                  if (otherTask != task)
                  {
                     // close any other open tasks
                     otherTask.isOpen = false;

                     // unhighlight the item title
                     otherTask.removeClass('taskItemSelected');

                     // does this task detail panel need resetting back to it's default height?
                     var otherHeight = otherDetail.getStyle('height').toInt();
                     if (otherHeight != otherDetail.defHeight)
                     {
                        animDetail[j] = {
                           'height': [otherHeight, otherDetail.defHeight],
                           'opacity': [otherDetail.getStyle('opacity'), 0]};
                     }
                     // does the info button need fading out?
                     var otherOpacity = otherInfo.getStyle('opacity');
                     if (otherOpacity != 0)
                     {
                        animInfo[j] = {'opacity': [otherOpacity, 0]};
                     }
                     
                     otherDetail.getElementsByTagName("div")[2].setStyle('overflow', 'hidden');
                  }
               });
            }
            else
            {
               // close this task
               // flag this task as closed
               task.isOpen = false;
               
               // fade in info button
               animInfo[i] = {'opacity': [infoOpacity, 1]};

               // reset task back to it's default height
               animDetail[i] = {
                  'height': [detailHeight, detail.defHeight],
                  'opacity': [detail.getStyle('opacity'), 0]};
               
               detail.getElementsByTagName("div")[2].setStyle('overflow', 'hidden');
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
   }
};

window.addEvent('load', MyTasks.start);