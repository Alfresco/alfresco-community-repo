/*
 * Prerequisites: mootools.v1.11.js
 *                autocompleter.js
 *                office_addin.js
 */
var OfficeMyTasks =
{
   MAX_DESCRIPTION: 100,
   
   init: function()
   {
      OfficeAddin.sortTasks($('taskList'));
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
      
      if ($('wrkDescription'))
      {
         var desc = $('wrkDescription');
         
         desc.onkeyup = desc.onchange = desc.onblur = function(event)
         {
            if (this.value.length > OfficeMyTasks.MAX_DESCRIPTION)
            {
              this.setProperty('value', this.getProperty('value').substr(0, OfficeMyTasks.MAX_DESCRIPTION));
            }
         };
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
            {
               return;
            }

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
            {
               return;
            }

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

               if (!window.queryObject.st)
               {
                  OfficeAddin.showStatusText("Loading task...", "ajax_anim.gif", false);
               }

               // ajax call to load task details               
               var actionURL = window.serviceContextPath + "/office/myTasksDetail" + OfficeAddin.defaultQuery + "&t=" + task.id.replace(/\./, "$");
               var myAjax = new Ajax(actionURL,
               {
                  method: 'get',
                  headers: {'If-Modified-Since': 'Sat, 1 Jan 2000 00:00:00 GMT'},
                  onComplete: function(textResponse, xmlResponse)
                  {
                     if (!window.queryObject.st)
                     {
                        OfficeAddin.hideStatusText();
                     }
                     $("taskDetails").innerHTML = textResponse;
                  }
               }).request();
               
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
      if ($(taskId))
      {
         $(taskId).fireEvent("click");
      }
   },
   
   transitionTask: function(taskId, commandURL, successMessage)
   {
      OfficeAddin.showStatusText("Running workflow...", "ajax_anim.gif", false);

      // ajax call to run workflow
      var myAjax = new Ajax(commandURL,
      {
         method: 'get',
         headers: {'If-Modified-Since': 'Sat, 1 Jan 2000 00:00:00 GMT'},
         onComplete: function(textResponse, xmlResponse)
         {
            // Remove any trailing hash
            var href = window.location.href.replace("#", "");
            // Remove any "st" and "w" parameters
            href = OfficeAddin.removeParameters(href, "st|w");
            // Optionally add a status string
            if (successMessage !== "")
            {
               var json = "{\"statusString\":\"" + successMessage + "\",\"statusCode\":true}";
               href += (href.indexOf("?") == -1) ? "?" : "&";
               href += "st=" + encodeURIComponent(json);
            }
            window.location.href = href;
         },
         onFailure: function()
         {
            OfficeAddin.showStatusText("Couldn't run workflow", "action_failed.gif", true);
         }
      }).request();
   },
   
   startWorkflow: function(commandURL, Doc)
   {
      var wrkType = $('wrkType').value,
         wrkAssignTo = $('wrkAssignTo').value,
         wrkDueDate = $('wrkDueDate').value,
         wrkDescription=$('wrkDescription').value;

      if (wrkAssignTo.test(/(?:\(([^\)]+)\))/))
      {
         // Extract the Username - should be "First Last (Username)"
         wrkAssignTo = wrkAssignTo.match(/(?:\(([^\)]+)\))/)[1];
      }
      
      if (wrkAssignTo === "")
      {
         OfficeAddin.showStatusText("Assign to cannot be empty", "info.gif", true);
         return;
      }
      
      OfficeAddin.showStatusText("Starting workflow...", "ajax_anim.gif", false);
      var actionURL = commandURL + "?a=workflow&n=" + encodeURIComponent(Doc);
      actionURL += "&wt=" + encodeURIComponent(wrkType);
      actionURL += "&at=" + encodeURIComponent(wrkAssignTo);
      // Date supplied?
      if (wrkDueDate !== "")
      {
         actionURL += "&dd=" + encodeURIComponent(wrkDueDate);
      }
      actionURL += "&desc=" + encodeURIComponent(wrkDescription);
      var myAjax = new Ajax(actionURL,
      {
         method: 'get',
         headers: {'If-Modified-Since': 'Sat, 1 Jan 2000 00:00:00 GMT'},
         onComplete: function(textResponse, xmlResponse)
         {
            // Remove any trailing hash
            var href = window.location.href.replace("#", ""),
               success = Json.evaluate(textResponse).statusCode;
            
            // Remove any previous "st", "w" or "wd" parameters
            href = OfficeAddin.removeParameters(href, success ? "st|w|wd" : "st");
            // Optionally add a status string
            if (textResponse !== "")
            {
               href += (href.indexOf("?") == -1) ? "?" : "&";
               href += "st=" + encodeURIComponent(textResponse);
            }
            window.location.href = href;
         }
      });
      myAjax.request();
   },
   
   /* AJAX call to perform server-side actions */
   runTaskAction: function(useTemplate, action, nodeId, confirmMsg)
   {
      // Re-select a selected task after reload
      var taskSel = $E('#taskList .taskItemSelected'),
         outParams = null;
      if (taskSel !== null)
      {
         var taskId = taskSel.id;
         outParams = "t=" + encodeURIComponent(taskId);
      }
      
      return OfficeAddin.getAction(useTemplate, action, nodeId, confirmMsg, null, outParams);
   },

   refreshPage: function()
   {
      // Remove any trailing hash
      var href = window.location.href.replace("#", "");
      // Remove any previous "st", "w", "wd" or "t" parameters
      href = OfficeAddin.removeParameters(href, "st|w|wd|t");
      // Re-select a selected task after reload
      var taskSel = $E('#taskList .taskItemSelected');
      if (taskSel !== null)
      {
         var taskId = taskSel.id;
         href += (href.indexOf("?") == -1) ? "?" : "&";
         href += "t=" + encodeURIComponent(taskId);
      }
      window.location.href = href;
   }
};

window.addEvent('domready', OfficeMyTasks.init);
