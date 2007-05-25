var MyDocs = {
   IMG_SMALL: 16,
   IMG_LARGE: 64,
   ANIM_LENGTH: 300,
   DETAIL_MARGIN: 56,
   TITLE_FONT_SIZE: 18,
   RESOURCE_PANEL_HEIGHT: 150,

   start: function()
   {
      if ($('docPanel'))
      {
         MyDocs.parseDocPanels();
         $('docPanel').setStyle('visibility', 'visible');
      }
   },

   parseDocPanels: function()
   {
      var docs = $$('#docPanel .docRow');
      var items = $$('#docPanel .docItem');
      var infos = $$('#docPanel .docInfo');
      var details = $$('#docPanel .docDetail');
      var icons = $$('#docPanel .docIcon');
      var imgs = $$('#docPanel .docIconImage');
      var imgs64 = $$('#docPanel .docIconImage64');
      var resources = $$('#docPanel .docResource');
      var fxItem = new Fx.Elements(items, {wait: false, duration: MyDocs.ANIM_LENGTH, transition: Fx.Transitions.linear});
      var fxDetail = new Fx.Elements(details, {wait: false, duration: MyDocs.ANIM_LENGTH, transition: Fx.Transitions.linear});
      var fxInfo = new Fx.Elements(infos, {wait: false, duration: MyDocs.ANIM_LENGTH, transition: Fx.Transitions.linear});
      var fxIcon = new Fx.Elements(icons, {wait: false, duration: MyDocs.ANIM_LENGTH, transition: Fx.Transitions.linear});
      var fxResource = new Fx.Elements(resources,
      {
         wait: false,
         duration: 500,
         transition: Fx.Transitions.linear,
         onComplete: function()
         {
            this.elements.each(function(resource, i)
            {
               if (resource.parentNode.isOpen)
               {
                  if (resource.isLoaded)
                  {
                     resource.needsOverflow = false;
                     var elePrev = $E('.docPreview', resource)
                     if (elePrev)
                     {
                        elePrev.setStyle('overflow', 'auto');
                     }
                  }
                  else
                  {
                     // defer style change to ajax complete event
                     resource.needsOverflow = true;
                  }
               }
            });
         }
      });
      var fxImage = new Fx.Elements(imgs,
      {
         wait: false,
         duration: MyDocs.ANIM_LENGTH,
         transition: Fx.Transitions.linear,
         onComplete: function()
         {
            this.elements.each(function(img, i)
            {
               img.src = (img.getStyle('height').toInt() == MyDocs.IMG_SMALL) ? img.defSrc : img.bigSrc;
            });
         }
      });

      docs.each(function(doc, i)
      {
         var item = items[i],
            info = infos[i],
            detail = details[i],
            img = imgs[i],
            resource = resources[i];

         // animated elements defaults
         item.defMarginLeft = item.getStyle('margin-left').toInt();
         item.defFontSize = item.getStyle('font-size').toInt();;
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
         resource.defHeight = 1;
         resource.setStyle('height', resource.defHeight);
         
         // register 'mouseenter' (subclassed mouseover) event for each doc
         doc.addEvent('mouseenter', function(e)
         {
            var animItem = {},
               animDetail = {},
               animInfo = {};
               animImage = {};
            // highlight the item title
            doc.addClass('docItemSelected');
            // move the item title to the right
            animItem[i] = {
               'margin-left': [item.getStyle('margin-left').toInt(), MyDocs.DETAIL_MARGIN],
               'font-size': [item.getStyle('font-size').toInt(), MyDocs.TITLE_FONT_SIZE]
            };
            // fade in the info button
            animInfo[i] = {'opacity': [info.getStyle('opacity'), 1]};
            // slide and fade in the details panel
            animDetail[i] = {
               'height': [detail.getStyle('height').toInt(), detail.defHeight + MyDocs.IMG_LARGE],
               'opacity': [detail.getStyle('opacity'), 1]
            };
            // grow the doctype image
            animImage[i] = {
               'height': [img.getStyle('height').toInt(), MyDocs.IMG_LARGE],
               'width': [img.getStyle('width').toInt(), MyDocs.IMG_LARGE]
            };
            img.src = img.bigSrc;

            // reset styles on all other docs
            docs.each(function(otherDoc, j)
            {
               var otherItem = items[j];
               var otherInfo = infos[j];
               var otherDetail = details[j];
               var otherImg = imgs[j];
               if ((otherDoc != doc) && (!otherDoc.isOpen))
               {
                  // reset selected class?
                  otherDoc.removeClass('docItemSelected');
                  // move the title back to the left?
                  var ml = otherItem.getStyle('margin-left').toInt();
                  if (ml != otherItem.defMarginLeft)
                  {
                     animItem[j] = {
                        'margin-left': [ml, otherItem.defMarginLeft],
                        'font-size': [otherItem.getStyle('font-size').toInt(), otherItem.defFontSize]
                     };
                  }
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
                  // does the image need shrinking?
                  var ih = otherImg.getStyle('height').toInt();
                  if (ih != MyDocs.IMG_SMALL)
                  {
                     animImage[j] = {
                        'height': [ih, MyDocs.IMG_SMALL],
                        'width': [ih, MyDocs.IMG_SMALL]
                     };
                  }
               }
            });
            fxItem.start(animItem);
            fxDetail.start(animDetail);
            fxInfo.start(animInfo);
            fxImage.start(animImage);
         });

         doc.addEvent('click', function(e)
         {
            var animItem = {},
               animDetail = {},
               animInfo = {};
               animImage = {},
               animResource = {},
               resourceHeight = resource.getStyle('height').toInt();
            
            if (!doc.isOpen)
            {
               if (!resource.isLoaded)
               {
                  // fire off the ajax request to get the resources for this task
                  YAHOO.util.Connect.asyncRequest(
                     "POST",
                     getContextPath() + '/ajax/invoke/NodeInfoBean.sendNodeInfo',
                     { 
                        success: function(response)
                        {
                           // remove the ajax waiting animation class
                           resource.removeClass("doclistAjaxWait");
                           // populate the resource div with the result
                           resource.innerHTML = response.responseText;
                           // flag this resource as loaded
                           resource.isLoaded = true;
                           // deferred from transition complete event
                           if (resource.needsOverflow)
                           {
                              var elePrev = $E('.docPreview', resource)
                              if (elePrev)
                              {
                                 elePrev.setStyle('overflow', 'auto');
                              }
                           }
                        },
                        failure: function(response)
                        {
                           resource.innerHTML = "Sorry, preview currently unavailable.";
                        },
                        argument: [resource]
                     }, 
                     "noderef=" + resource.id + "&template=doclist_preview_panel.ftl"
                  );
               }
               
               // open up this document's resources
               // flag this document as open
               doc.isOpen = true;
               
               // slide and fade in the resources panel
               animResource[i] = {
                  'height': [resourceHeight, resource.defHeight + MyDocs.RESOURCE_PANEL_HEIGHT],
                  'opacity': [resource.getStyle('opacity'), 1]};
   
               // close other open documents and toggle this one if it's already open
               docs.each(function(otherDoc, j)
               {
                  var otherResource = resources[j],
                     otherItem = items[j],
                     otherInfo = infos[j],
                     otherDetail = details[j],
                     otherImg = imgs[j];
                     
                  if (otherDoc == doc)
                  {
                     // continue animations that may have been going on before the click
                     // move the item title to the right
                     animItem[j] = {
                        'margin-left': [otherItem.getStyle('margin-left').toInt(), MyDocs.DETAIL_MARGIN],
                        'font-size': [otherItem.getStyle('font-size').toInt(), MyDocs.TITLE_FONT_SIZE]
                     };
                     // fade in the info button
                     animInfo[j] = {'opacity': [otherInfo.getStyle('opacity'), 1]};
                     // slide and fade in the details panel
                     animDetail[j] = {
                        'height': [otherDetail.getStyle('height').toInt(), otherDetail.defHeight + MyDocs.IMG_LARGE],
                        'opacity': [otherDetail.getStyle('opacity'), 1]
                     };
                     // grow the doctype image
                     animImage[j] = {
                        'height': [otherImg.getStyle('height').toInt(), MyDocs.IMG_LARGE],
                        'width': [otherImg.getStyle('width').toInt(), MyDocs.IMG_LARGE]
                     };
                  }
                  else
                  {
                     // close any other open documents
                     otherDoc.isOpen = false;

                     // reset selected class?
                     otherDoc.removeClass('docItemSelected');
                     // move the title back to the left?
                     var ml = otherItem.getStyle('margin-left').toInt();
                     if (ml != otherItem.defMarginLeft)
                     {
                        animItem[j] = {
                           'margin-left': [ml, otherItem.defMarginLeft],
                           'font-size': [otherItem.getStyle('font-size').toInt(), otherItem.defFontSize]
                        };
                     }
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
                     // does the image need shrinking?
                     var ih = otherImg.getStyle('height').toInt();
                     if (ih != MyDocs.IMG_SMALL)
                     {
                        animImage[j] = {
                           'height': [ih, MyDocs.IMG_SMALL],
                           'width': [ih, MyDocs.IMG_SMALL]
                        };
                     }
   
                     // does this document resource panel need resetting back to it's default height?
                     var otherHeight = otherResource.getStyle('height').toInt();
                     if (otherHeight != otherResource.defHeight)
                     {
                        animResource[j] = {
                           'height': [otherHeight, otherResource.defHeight],
                           'opacity': [otherResource.getStyle('opacity'), 0]};
                     }
                     
                     var otherMeta = $E('.docPreview', otherResource)
                     if (otherMeta)
                     {
                        otherMeta.setStyle('overflow', 'hidden');
                     }
                  }
               });
            }
            else
            {
               // close this document panel
               // flag this document as closed
               doc.isOpen = false;
               
               // reset resource panel back to it's default height
               animResource[i] = {
                  'height': [resourceHeight, resource.defHeight],
                  'opacity': [resource.getStyle('opacity'), 0]};
               
               var elePrev = $E('.docPreview', resource)
               if (elePrev)
               {
                 elePrev.setStyle('overflow', 'hidden');
               }
            }
            fxResource.start(animResource);
            fxItem.start(animItem);
            fxDetail.start(animDetail);
            fxInfo.start(animInfo);
            fxImage.start(animImage);
         });
      });

      $('docPanel').addEvent('mouseleave', function(e)
      {
         // handler for mouse leaving the entire doc panel
         var animItem = {},
            animDetail = {},
            animInfo = {},
            animImage = {};

         docs.each(function(doc, i)
         {
            var item = items[i],
               detail = details[i],
               info = infos[i],
               img = imgs[i];

            if (doc.isOpen)
            {
               // continue animations that may have been going on before the click
               // move the item title to the right
               animItem[i] = {
                  'margin-left': [item.getStyle('margin-left').toInt(), MyDocs.DETAIL_MARGIN],
                  'font-size': [item.getStyle('font-size').toInt(), MyDocs.TITLE_FONT_SIZE]
               };
               // fade in the info button
               animInfo[i] = {'opacity': [info.getStyle('opacity'), 1]};
               // slide and fade in the details panel
               animDetail[i] = {
                  'height': [detail.getStyle('height').toInt(), detail.defHeight + MyDocs.IMG_LARGE],
                  'opacity': [detail.getStyle('opacity'), 1]
               };
               // grow the doctype image
               animImage[i] = {
                  'height': [img.getStyle('height').toInt(), MyDocs.IMG_LARGE],
                  'width': [img.getStyle('width').toInt(), MyDocs.IMG_LARGE]
               };
            }
            else
            {
               doc.removeClass('docItemSelected');
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
                  'height': [img.getStyle('height').toInt(), MyDocs.IMG_SMALL],
                  'width': [img.getStyle('width').toInt(), MyDocs.IMG_SMALL]
               };
            }
         });
         fxItem.start(animItem);
         fxDetail.start(animDetail);
         fxInfo.start(animInfo);
         fxImage.start(animImage);
      });
   }
};

window.addEvent('load', MyDocs.start);