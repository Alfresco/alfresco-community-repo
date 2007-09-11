var MyWebForms = {
   ANIM_LENGTH: 300,
   
   start: function()
   {
      if ($('formsPanel'))
      {
         MyWebForms.parseFormsPanels();
         $('formsPanel').setStyle('visibility', 'visible');
      }
   },

   parseFormsPanels: function()
   {
      var projects = $$('#formsPanel .webProjectRow');
      var frms = $$('#formsPanel .webProjectForms');
      var fxForm = new Fx.Elements(frms, {wait: false, duration: MyWebForms.ANIM_LENGTH, transition: Fx.Transitions.sineInOut});

      projects.each(function(project, i)
      {
         var frm = frms[i];

         // animated elements defaults
         frm.maxHeight = Math.max(frm.getStyle('height').toInt(), 1);
         frm.defHeight = 1;
         frm.setStyle('height', frm.defHeight);
         frm.setStyle('opacity', 0);

         // register 'mouseenter' (subclassed mouseover) event for each project
         project.addEvent('mouseenter', function(e)
         {
            var animForm = {};

            // slide and fade in the frms panel
            animForm[i] =
            {
               'height': [frm.getStyle('height').toInt(), frm.maxHeight],
               'opacity': [frm.getStyle('opacity'), 1]
            };

            // reset styles on all other frms
            projects.each(function(otherProject, j)
            {
               var otherfrm = frms[j];

               if (otherProject != project)
               {
                  // does this frm panel need resetting back to it's default height?
                  var h = otherfrm.getStyle('height').toInt();
                  if (h != otherfrm.defHeight)
                  {
                     animForm[j] =
                     {
                        'height': [h, otherfrm.defHeight],
                        'opacity': [otherfrm.getStyle('opacity'), 0]};
                  }
               }
            });
            fxForm.start(animForm);
         });
      });

      $('formsPanel').addEvent('mouseleave', function(e)
      {
         // handler for mouse leaving the entire panel
         var animForm = {};

         projects.each(function(project, i)
         {
            var frm = frms[i];

            animForm[i] =
            {
               'height': [frm.getStyle('height').toInt(), frm.defHeight],
               'opacity': [frm.getStyle('opacity'), 0]
            };
         });
         fxForm.start(animForm);
      });
   }
};

window.addEvent('load', MyWebForms.start);