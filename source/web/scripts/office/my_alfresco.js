/*
 * Prerequisites: mootools.v1.1.js
 *                office_addin.js
 */
var OfficeMyAlfresco =
{
   init: function()
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
   }
   
};

window.addEvent('domready', OfficeMyAlfresco.init);