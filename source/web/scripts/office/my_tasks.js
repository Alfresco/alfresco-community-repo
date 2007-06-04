/*
 * Prerequisites: mootools.v1.1.js
 */
var OfficeMyTasks =
{
   init: function()
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

               OfficeAddin.showStatusText("Loading task...", "ajax_anim.gif", false);

               // ajax call to load task details               
               var actionURL = "/alfresco/service/office/myTasksDetail?t=" + task.id;
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
   }   
};

window.addEvent('domready', OfficeMyTasks.init);