var MySpaces = {

   start: function()
   {
      if ($('spacePanel'))
      {
         MySpaces.parseSpacePanels();
         $('spacePanel').setStyle('visibility', 'visible');
      }
   },

   parseSpacePanels: function()
   {
      var spaces = $$('#spacePanel .spaceRow');
      var items = $$('#spacePanel .spaceItem');
      var infos = $$('#spacePanel .spaceInfo');
      var details = $$('#spacePanel .spaceDetail');
      var icons = $$('#spacePanel .spaceIcon');
      var imgs = $$('#spacePanel .spaceIconImage');
      var imgs64 = $$('#spacePanel .spaceIconImage64');
      var fxItem = new Fx.Elements(items, {wait: false, duration: 300, transition: Fx.Transitions.linear});
      var fxDetail = new Fx.Elements(details, {wait: false, duration: 300, transition: Fx.Transitions.linear});
      var fxInfo = new Fx.Elements(infos, {wait: false, duration: 300, transition: Fx.Transitions.linear});
      var fxIcon = new Fx.Elements(icons, {wait: false, duration: 300, transition: Fx.Transitions.linear});
      var fxImage = new Fx.Elements(imgs,
      {
         wait: false,
         duration: 300,
         transition: Fx.Transitions.linear,
         onComplete: function()
         {
            this.elements.each(function(img, i)
            {
               img.src = (img.getStyle('height').toInt() == 16) ? img.defSrc : img.bigSrc;
            });
         }
      });

      spaces.each(function(space, i)
      {
         var item = items[i],
             info = infos[i],
             detail = details[i],
             img = imgs[i];

         // animated elements defaults
         item.defMarginLeft = item.getStyle('margin-left').toInt();
         item.defFontSize = item.getStyle('font-size').toInt();
         item.defBColor = (item.getStyle('background-color') == 'transparent') ? '' : item.getStyle('background-color');
         detail.defHeight = 1;
         detail.setStyle('opacity', 0);
         detail.setStyle('display', 'block');
         detail.setStyle('height', detail.defHeight);
         info.setStyle('opacity', 0);
         // NOTE: special check for images without special 64x64 pixel version
         if (imgs64[i].src.lastIndexOf("_default.png") != -1)
         {
            img.src = img.src.replace(new RegExp("/filetypes/.*\.png$"), "/filetypes/_default.png");
         }
         img.defSrc = img.src;
         img.bigSrc = imgs64[i].src;
         
         // register 'mouseenter' (subclassed mouseover) event for each space
         space.addEvent('mouseenter', function(e)
         {
            var animItem = {},
               animDetail = {},
               animInfo = {};
               animImage = {};
            // highlight the item title
            space.addClass('spaceItemSelected');
            // move the item title to the right
            animItem[i] = {
               'margin-left': [item.getStyle('margin-left').toInt(), 56],
               'font-size': [item.getStyle('font-size').toInt(), 18]
            };
            // fade in the info button
            animInfo[i] = {'opacity': [info.getStyle('opacity'), 1]};
            // slide and fade in the details panel
            animDetail[i] = {
               'height': [detail.getStyle('height').toInt(), detail.defHeight + 64],
               'opacity': [detail.getStyle('opacity'), 1]
            };
            // grow the spacetype image
            animImage[i] = {
               'height': [img.getStyle('height').toInt(), 64],
               'width': [img.getStyle('width').toInt(), 64]
            };
            img.src = img.bigSrc;

            // reset styles on all other space
            spaces.each(function(otherSpace, j)
            {
               var otherItem = items[j];
               var otherInfo = infos[j];
               var otherDetail = details[j];
               var otherImg = imgs[j];
               if ((otherSpace != space) && (!otherSpace.isOpen))
               {
                  // reset selected class?
                  otherSpace.removeClass('spaceItemSelected');
                  // move the title back to the left?
                  var ml = otherItem.getStyle('margin-left').toInt();
                  if (ml != otherItem.defMarginLeft)
                  {
                     animItem[j] = {
                        'margin-left': [ml, otherItem.defMarginLeft],
                        'font-size': [otherItem.getStyle('font-size').toInt(), otherItem.defFontSize]
                     };
                  }
                  // does this space detail panel need resetting back to it's default height?
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
                  // does the image need shrinking?
                  var ih = otherImg.getStyle('height').toInt();
                  if (ih != 16)
                  {
                     animImage[j] = {
                        'height': [ih, 16],
                        'width': [ih, 16]
                     };
                  }
               }
            });
            fxItem.start(animItem);
            fxDetail.start(animDetail);
            fxInfo.start(animInfo);
            fxImage.start(animImage);
         });
      });

      $('spacePanel').addEvent('mouseleave', function(e)
      {
         // handler for mouse leaving the entire space panel
         var animItem = {},
             animDetail = {},
             animInfo = {},
             animImage = {};

         spaces.each(function(space, i)
         {
            var item = items[i],
                detail = details[i],
                info = infos[i],
                img = imgs[i];

            if (space.isOpen)
            {
               // continue animations that may have been going on before the click
               // move the item title to the right
               animItem[i] = {
                  'margin-left': [item.getStyle('margin-left').toInt(), 56],
                  'font-size': [item.getStyle('font-size').toInt(), 18]
               };
               // fade in the info button
               animInfo[i] = {'opacity': [info.getStyle('opacity'), 1]};
               // slide and fade in the details panel
               animDetail[i] = {
                  'height': [detail.getStyle('height').toInt(), detail.defHeight + 64],
                  'opacity': [detail.getStyle('opacity'), 1]
               };
               // grow the spacetype image
               animImage[i] = {
                  'height': [img.getStyle('height').toInt(), 64],
                  'width': [img.getStyle('width').toInt(), 64]
               };
            }
            else
            {
               space.removeClass('spaceItemSelected');
               animItem[i] = {
                  'margin-left': [item.getStyle('margin-left').toInt(), item.defMarginLeft],
                  'font-size': [item.getStyle('font-size').toInt(), item.defFontSize]
               };
               animDetail[i] = {
                  'height': [detail.getStyle('height').toInt(), detail.defHeight],
                  'opacity': [detail.getStyle('opacity'), 0]
               };
               animInfo[i] = {'opacity': [infos[i].getStyle('opacity'), 0]};
               animImage[i] = {
                  'height': [img.getStyle('height').toInt(), 16],
                  'width': [img.getStyle('width').toInt(), 16]
               };
            }
         });
         fxItem.start(animItem);
         fxDetail.start(animDetail);
         fxInfo.start(animInfo);
         fxImage.start(animImage);
      });
   },
   
   upload: function(actionEl)
   {
      var panel = $E(".spaceUploadPanel", actionEl);
      panel.setStyle("display", "block");
   }
};

window.addEvent('load', MySpaces.start);