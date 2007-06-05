/*
 * Prerequisites: mootools.v1.1.js
 *                autocompleter.js
 *                office_addin.js
 */
var OfficeMyTasks =
{
   init: function()
   {
      OfficeMyTasks.setupEventHandlers();      
      
      if (window.queryObject.t)
      {
         OfficeMyTasks.openTask(window.queryObject.t);
      }
      
      if ($('wrkUser'))
      {
         var autoUser = new Autocompleter.Ajax.Json($('wrkUser'), window.contextPath + '/service/office/getUsers',
         {
            postVar: 's',
            ajaxOptions:
            {
               method: 'get',
               headers: {'If-Modified-Since': 'Sat, 1 Jan 2000 00:00:00 GMT'}
            },
            onRequest: function(el)
            {
               $('ajxUser').setStyle('display', '');
            },
            onComplete: function(el)
            {
               $('ajxUser').setStyle('display', 'none');
            }
         });      
		}
   },

   setupEventHandlers: function()
   {
      var tasks = $$('#taskList .taskItem');
      
      tasks.each(function(task, i)
      {
         task.isOpen = false;

         // register 'mouseenter' event for each task
         task.addEvent('mouseenter', function(e)
         {
            if (task.isOpen)
               return;

            // highlight the item title
            task.addClass('taskItemSelected');

            // reset styles on all closed tasks
            tasks.each(function(otherTask, j)
            {
               if ((otherTask != task) && (!otherTask.isOpen))
               {
                  // reset selected class
                  otherTask.removeClass('taskItemSelected');
               }
            });
         });
            
         // register 'mouseleave' event for each task
         task.addEvent('mouseleave', function(e)
         {
            if (task.isOpen)
               return;

            // unhighlight the item title
            task.removeClass('taskItemSelected');
         });
         
         // register 'click' event for each task
         task.addEvent('click', function(e)
         {
            if (!task.isOpen)
            {
               // open up this task
               // flag this task as open
               task.isOpen = true;

               // highlight the item title
               task.addClass('taskItemSelected');

               OfficeAddin.showStatusText("Loading task...", "ajax_anim.gif", false);

               // ajax call to load task details               
               var actionURL = window.contextPath + "/service/office/myTasksDetail?t=" + task.id;
               var myAjax = new Ajax(actionURL, {
                  method: 'get',
                  headers: {'If-Modified-Since': 'Sat, 1 Jan 2000 00:00:00 GMT'},
                  onComplete: function(textResponse, xmlResponse)
                  {
                     OfficeAddin.hideStatusText();
                     $("taskDetails").innerHTML = textResponse;
                  }
               });
               myAjax.request();
               
               // close other open tasks
               tasks.each(function(otherTask, j)
               {
                  if (otherTask != task)
                  {
                     // close any other open tasks
                     otherTask.isOpen = false;

                     // unhighlight the item title
                     otherTask.removeClass('taskItemSelected');
                  }
               });
            }
         });
      });

      $('taskList').addEvent('mouseleave', function(e)
      {
         // handler for mouse leaving the entire task list
         tasks.each(function(task, i)
         {
            if (!task.isOpen)
            {
               task.removeClass('taskItemSelected');
            }
         });
      });
   },

   openTask: function(taskId)
   {
      $(taskId).fireEvent("click");
   },
   
   transitionTask: function(taskId, commandURL, successMessage)
   {
      OfficeAddin.showStatusText("Running workflow...", "ajax_anim.gif", false);

      // ajax call to run workflow
      var myAjax = new Ajax(commandURL, {
         method: 'get',
         headers: {'If-Modified-Since': 'Sat, 1 Jan 2000 00:00:00 GMT'},
         onComplete: function(textResponse, xmlResponse)
         {
            // Remove any trailing hash
            var href = window.location.href.replace("#", "")
            // Remove any previous "&st=" string
            href = href.replace(/[?&]st=([^&$]+)/g, "");
            // Remove any previous "&t=" string
            href = href.replace(/[?&]t=([^&$]+)/g, "");
            // Optionally add a status string
            if (successMessage != "")
            {
               var json = "{\"statusString\":\"" + successMessage + "\",\"statusCode\":true}";
               href += "&st=" + encodeURI(json);
               href += "&t=" + taskId;
            }
            window.location.href = href;
         },
         onFailure: function()
         {
            OfficeAddin.showStatusText("Couldn't run workflow", "action_failed.gif", true);
         }
      });
      myAjax.request();
   }
};

window.addEvent('domready', OfficeMyTasks.init);
