var MyWebFiles = {
   ANIM_LENGTH: 300,
   FILE_PANEL_HEIGHT: 128,
   
   start: function()
   {
      if ($('webFilesPanel'))
      {
         MyWebFiles.parseFilesPanels();
         $('webFilesPanel').setStyle('visibility', 'visible');
      }
   },

   parseFilesPanels: function()
   {
      var projects = $$('#webFilesPanel .webProjectRow');
      var files = $$('#webFilesPanel .webProjectFiles');
      var fxFile = new Fx.Elements(files, {wait: false, duration: MyWebFiles.ANIM_LENGTH, transition: Fx.Transitions.sineInOut,
         onComplete: function()
         {
            // event handler to ensure scrollable area style is set
            this.elements.each(function(file, i)
            {
               if (file.parentNode.isOpen == true)
               {
                  $E('.fileResources', file).setStyle('overflow', 'auto');
               }
            });
         }
      });

      projects.each(function(project, i)
      {
         var file = files[i];
         
         // animated elements defaults
         file.maxHeight = Math.max(file.getStyle('height').toInt(), 1);
         file.defHeight = 1;
         file.setStyle('height', file.defHeight);
         file.setStyle('display', 'block');
         file.setStyle('opacity', 0);
         
         // register 'mouseenter' event for each project
         project.addEvent('mouseenter', function(e)
         {
            if (projects.isOpen)
               return;
            
            // highlight the item title
            projects.addClass('webProjectRowSelected');
            
            // reset styles on all closed projects
            projects.each(function(otherProject, j)
            {
               if ((otherProject != project) && (!otherProject.isOpen))
               {
                  // reset selected class
                  otherProject.removeClass('webProjectRowSelected');
               }
            });
         });
         
         // register 'mouseleave' event for each project
         project.addEvent('mouseleave', function(e)
         {
            if (project.isOpen)
               return;
            
            // unhighlight the item title
            project.removeClass('webProjectRowSelected');
         });
         
         // register 'click' event for each project
         project.addEvent('click', function(e)
         {
            var animFile = {},
                fileHeight = file.getStyle('height').toInt();
            
            if (!project.isOpen)
            {
               // open up this project
               project.isOpen = true;
               
               // slide and fade in the file panel
               animFile[i] = {
                  'height': [fileHeight, file.defHeight + MyWebFiles.FILE_PANEL_HEIGHT],
                  'opacity': [file.getStyle('opacity'), 1]};
               
               // close other open projects and toggle this one if it's already open
               projects.each(function(otherProject, j)
               {
                  var otherFile = files[j];
                  
                  if (otherProject != project)
                  {
                     // close any other open projects
                     otherProject.isOpen = false;
                     
                     // unhighlight the item title
                     otherProject.removeClass('webProjectRowSelected');
                     
                     // does this file panel need resetting back to it's default height?
                     var otherHeight = otherFile.getStyle('height').toInt();
                     if (otherHeight != otherFile.defHeight)
                     {
                        animFile[j] = {
                           'height': [otherHeight, otherFile.defHeight],
                           'opacity': [otherFile.getStyle('opacity'), 0]};
                     }
                     
                     $E('.fileResources', otherFile).setStyle('overflow', 'hidden');
                  }
               });
            }
            else
            {
               // close this project
               project.isOpen = false;
               
               // reset project back to it's default height
               animFile[i] = {
                  'height': [fileHeight, file.defHeight],
                  'opacity': [file.getStyle('opacity'), 0]};
               
               $E('.fileResources', file).setStyle('overflow', 'hidden');
            }
            fxFile.start(animFile);
         });
      });
   }
};

window.addEvent('load', MyWebFiles.start);