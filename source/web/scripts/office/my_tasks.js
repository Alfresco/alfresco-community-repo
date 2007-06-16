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
      
      if ($('wrkAssignTo'))
      {
         var autoAssignTo = new Autocompleter.Ajax.Json($('wrkAssignTo'), window.serviceContextPath + '/office/getUsers',
         {
            postVar: 's',
            minLength: 2,
            useSelection: false,
            markQuery: false,
            ajaxOptions:
            {
               method: 'get',
               headers: {'If-Modified-Since': 'Sat, 1 Jan 2000 00:00:00 GMT'}
            },
            onRequest: function(el)
            {
               $('ajxAssignTo').setStyle('display', '');
            },
            onComplete: function(el)
            {
               $('ajxAssignTo').setStyle('display', 'none');
            }
         });
      }
		
      if ($('wrkDueDate'))
      {
         var dueDate = new DatePicker($('wrkDueDate'),
         {
            readOnly: false,
            dateFormat: "dd MMMM yyyy"
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
               var actionURL = window.serviceContextPath + "/office/myTasksDetail?t=" + task.id.replace(/\./, "$");
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
            // Remove any "st" and "w" parameters
            href = OfficeAddin.removeParameters(href, "st|w");
            // Optionally add a status string
            if (successMessage != "")
            {
               var json = "{\"statusString\":\"" + successMessage + "\",\"statusCode\":true}";
               href += "&st=" + encodeURI(json);
            }
            window.location.href = href;
         },
         onFailure: function()
         {
            OfficeAddin.showStatusText("Couldn't run workflow", "action_failed.gif", true);
         }
      });
      myAjax.request();
   },
   
   startWorkflow: function(commandURL, Doc)
   {
      var wrkType=$('wrkType').value;
      // wrkAssignTo should be "First Last (Username)"
      var wrkAssignTo=$('wrkAssignTo').value;
      if (wrkAssignTo.test(/(?:\(([^\)]+)\))/))
      {
         // Extract the Username
         wrkAssignTo=wrkAssignTo.match(/(?:\(([^\)]+)\))/)[1];
      }
      var wrkDueDate = $('wrkDueDate').value;;
      var wrkDescription=$('wrkDescription').value;
      
      OfficeAddin.showStatusText("Starting workflow...", "ajax_anim.gif", false);
      var actionURL = commandURL + "?a=workflow&d=" + Doc;
      actionURL += "&wt=" + wrkType;
      actionURL += "&at=" + wrkAssignTo;
      // Date supplied?
      if (wrkDueDate != "")
      {
         actionURL += "&dd=" + wrkDueDate;
      }
      actionURL += "&desc=" + wrkDescription;
      var myAjax = new Ajax(actionURL, {
         method: 'get',
         headers: {'If-Modified-Since': 'Sat, 1 Jan 2000 00:00:00 GMT'},
         onComplete: function(textResponse, xmlResponse)
         {
            // Remove any trailing hash
            var href = window.location.href.replace("#", "")
            // Remove any previous "st", "w" or "wd" parameters
            href = OfficeAddin.removeParameters(href, "st|w|wd");
            // Optionally add a status string
            if (textResponse != "")
            {
               href += "&st=" + encodeURI(textResponse);
            }
            window.location.href = href;
         }
      });
      myAjax.request();
   }
};

window.addEvent('domready', OfficeMyTasks.init);
