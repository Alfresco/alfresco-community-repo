var MyDocs = {

   start: function()
   {
      if ($('docPanel'))
      {
         MyDocs.parseDocPanels();
      }
   },

   parseDocPanels: function()
   {
      var docs = $$('#docPanel .docRow');
      var items = $$('#docPanel .docItem');
      var infos = $$('#docPanel .docInfo');
      var details = $$('#docPanel .docDetail');
      var fxDetail = new Fx.Elements(details, {wait: false, duration: 500, transition: Fx.Transitions.linear});
      var fxInfo = new Fx.Elements(infos, {wait: false, duration: 500, transition: Fx.Transitions.linear});
      docs.each(function(doc, i)
      {
         var item = items[i];
         var info = infos[i];
         var detail = details[i];

         // animated elements defaults
         item.defBColor = (item.getStyle('background-color') == 'transparent') ? '' : item.getStyle('background-color');
         detail.defHeight = 0;
         detail.setStyle('opacity', 0);
         detail.setStyle('display', 'block');
         detail.setStyle('height', 0);
         info.setStyle('opacity', 0);

         // register 'mouseenter' (subclassed mouseover) event for each doc
         doc.addEvent('mouseenter', function(e)
         {
            var animInfo = {},
            animDetail = {};
            // highlight the item title
            doc.addClass('docItemSelected');
            // fade in the info button
            animInfo[i] = {'opacity': [0, 1]};
            // slide and fade in the details panel
            animDetail[i] = {
               'height': [detail.getStyle('height').toInt(), detail.defHeight + 100],
               'opacity': [detail.getStyle('opacity'), 1]};

            // reset styles on all other docs
            docs.each(function(otherDoc, j)
            {
               var otherItem = items[j];
               var otherInfo = infos[j];
               var otherDetail = details[j];
               if (otherDoc != doc)
               {
                  // reset selected class?
                  otherDoc.removeClass('docItemSelected');
                  // does this doc detail panel need resetting back to it's default height?
                  var h = otherDetail.getStyle('height').toInt();
                  if (h != otherDetail.defHeight)
                  {
                     animDetail[j] = {
                        'height': [h, otherDetail.defHeight],
                        'opacity': [otherDetail.getStyle('opacity'), 0]};
                  }
                  // does the info button need fading out
                  var o = otherInfo.getStyle('opacity');
                  if (o != 0)
                  {
                     animInfo[j] = {'opacity': [o, 0]};
                  }
               }
            });
            fxInfo.start(animInfo);
            fxDetail.start(animDetail);
         });
      });

      $('docPanel').addEvent('mouseleave', function(e)
      {
         // handler for mouse leaving the entire doc panel
         var animInfo = {},
         animDetail = {};

         docs.each(function(doc, i)
         {
            var item = items[i];
            var detail = details[i];

            doc.removeClass('docItemSelected');
            animDetail[i] = {
               'height': [detail.getStyle('height').toInt(), detail.defHeight],
               'opacity': [detail.getStyle('opacity'), 0]};
            animInfo[i] = {'opacity': [infos[i].getStyle('opacity'), 0]};
         });
         fxInfo.start(animInfo);
         fxDetail.start(animDetail);
      });
   }
};

window.addEvent('load', MyDocs.start);