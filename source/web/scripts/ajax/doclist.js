var MyDocs = {
   IMG_SMALL: 16,
   IMG_LARGE: 64,
   ANIM_LENGTH: 300,
   DETAIL_MARGIN: 8,
   TITLE_FONT_SIZE: 18,
   RESOURCE_PANEL_HEIGHT: 180,
   OVERLAY_OPACITY: 0.8,
   ServiceContext: null,
   Filter: null,
   Home: null,
   Query: null,
   FxAll: null,

   start: function()
   {
      if ($('docPanel'))
      {
         $('docPanelOverlay').setStyle('opacity', 0);
         // show AJAX loading overlay
         $('docPanelOverlayAjax').setStyle('display', 'block');
         $('docPanel').setStyle('visibility', 'hidden');

         var messagePanel = $('docMessagePanel');
         messagePanel.setStyle('opacity', 0);
         messagePanel.setStyle('display', 'block');

         // fire off the ajax request to populate the doc list - the 'doclistpanel' webscript
         // is responsible for rendering just the contents of the main panel div
         YAHOO.util.Connect.asyncRequest(
            "GET",
            MyDocs.ServiceContext + '/ui/doclistpanel?f=' + MyDocs.Filter + '&q=' + MyDocs.Query + (MyDocs.Home != null ? '&h=' + MyDocs.Home : ''),
            {
               success: function(response)
               {
                  // push the response into the doc panel div
                  $('docPanel').setHTML(response.responseText);
                  
                  // extract the count value from a hidden div and display it
                  $('docCount').setHTML($('docCountValue').innerHTML);
                  
                  // wire up all the events and animations
                  MyDocs.init();
               },
               failure: function(response)
               {
                  // display the error
                  $('docPanel').setHTML($('displayTheError').innerHTML);
                  
                  // hide the ajax wait panel and show the main doc panel
                  $('docPanelOverlayAjax').setStyle('display', 'none');
                  $('docPanel').setStyle('visibility', 'visible');
               }
            }
         );
      }
   },
   
   init: function()
   {
      MyDocs.parseDocPanels();
      // hide the ajax wait panel and show the main doc panel
      $('docPanel').setStyle('visibility', 'visible');
      $('docPanelOverlayAjax').setStyle('display', 'none');

      if (MyDocs.postInit)
      {
         MyDocs.postInit();
         MyDocs.postInit = null;
      }
   },

   parseDocPanels: function()
   {
      MyDocs.FxAll = [];

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

      // Store all the effects so we can globally stop them later
      MyDocs.FxAll.push(fxItem);
      MyDocs.FxAll.push(fxDetail);
      MyDocs.FxAll.push(fxInfo);
      MyDocs.FxAll.push(fxIcon);
      MyDocs.FxAll.push(fxResource);
      MyDocs.FxAll.push(fxImage);

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
            
            // make sure item title is highlighted
            doc.addClass('docItemSelected');

            if (!doc.isOpen)
            {
               doc.addClass("docItemSelectedOpen");

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
                           resource.innerHTML = $('previewCurrentlyUnavailable').innerHTML;
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
                     otherDoc.removeClass("docItemSelectedOpen");

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

               doc.removeClass("docItemSelectedOpen");
               
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
   },

   /**
    * Delete a document item
    */
   deleteItem: function(name, noderef)
   {
      if (confirm("Are you sure you want to delete: " + name))
      {
         MyDocs.applyModal();
         
         // ajax call to delete item
         YAHOO.util.Connect.asyncRequest(
            "POST",
            getContextPath() + '/ajax/invoke/PortletActionsBean.deleteItem',
            {
               success: function(response)
               {
                  if (response.responseText.indexOf("OK:") == 0)
                  {
                     MyDocs.refreshList();
                  }
                  else
                  {
                     alert("Error during delete of item: " + response.responseText);
                     MyDocs.removeModal();
                  }
               },
               failure: function(response)
               {
                  alert("Error during delete of item: " + response.responseText);
                  MyDocs.removeModal();
               }
            }, 
            "noderef=" + noderef
         );
      }
   },

   /**
    * Check Out a document item
    */
   checkoutItem: function(name, noderef)
   {
      MyDocs.applyModal();

      // ajax call to delete item
      YAHOO.util.Connect.asyncRequest(
         "POST",
         getContextPath() + '/ajax/invoke/PortletActionsBean.checkoutItem',
         {
            success: function(response)
            {
               if (response.responseText.indexOf("OK:") == 0)
               {
                  MyDocs.refreshList();
                  MyDocs.displayMessage(name);
               }
               else
               {
                  alert("Error during check out of item: " + response.responseText);
                  MyDocs.removeModal();
               }
            },
            failure: function(response)
            {
               alert("Error during check out of item: " + response.responseText);
               MyDocs.removeModal();
            }
         }, 
         "noderef=" + noderef
      );
   },

   /**
    * Check In a document item
    */
   checkinItem: function(name, noderef)
   {
      MyDocs.applyModal();

      // ajax call to delete item
      YAHOO.util.Connect.asyncRequest(
         "POST",
         getContextPath() + '/ajax/invoke/PortletActionsBean.checkinItem',
         {
            success: function(response)
            {
               if (response.responseText.indexOf("OK:") == 0)
               {
                  MyDocs.refreshList();
                  MyDocs.displayMessage(name);
               }
               else
               {
                  alert("Error during check in of item: " + response.responseText);
                  MyDocs.removeModal();
               }
            },
            failure: function(response)
            {
               alert("Error during check in of item: " + response.responseText);
               MyDocs.removeModal();
            }
         }, 
         "noderef=" + noderef
      );
   },

   /**
    * Display the Update File pop-up panel
    */
   updateItem: function(actionEl, nodeRef)
   {
      if (this.popupPanel != null) return;
      
      this.fxOverlay = $("docPanelOverlay").effect('opacity', {duration: MyDocs.ANIM_LENGTH});
      
      var panel = $("docUpdatePanel");
      panel.setStyle("opacity", 0);
      panel.setStyle("display", "inline");
      Alfresco.Dom.smartAlignElement(panel, actionEl);
      
      // Generate a file upload element
      // To perform the actual upload, the element is moved to a hidden iframe
      // from which the upload is performed - this is required as javascript cannot
      // set the important properties on a file upload element for security reasons.
      // <input size="35" style="width:100%" type="file" value="" id="_upload" name="_upload">
      if (this.fileInput == null)
      {
         var fileInput = $(document.createElement("input"));
         fileInput.type = "file";
         fileInput.name = "_upload";
         fileInput.size = "35";
         fileInput.contentEditable = false;
         fileInput.setStyle("width", "100%");
         fileInput.addClass("docFormItem");
         fileInput.injectTop(panel);
         this.fileInput = fileInput;
      }
      
      var anim = new Fx.Styles(panel, {duration: MyDocs.ANIM_LENGTH, transition: Fx.Transitions.linear});
      anim.start({'opacity': 1});
      this.fxOverlay.start(MyDocs.OVERLAY_OPACITY);
      
      this.popupPanel = panel;
      this.popupPanel.nodeRef = nodeRef;
   },
   
   /**
    * OK button click handler for the Update Content pop-up panel
    */
   updateOK: function(actionEl)
   {
      if (this.fileInput.value.length > 0)
      {
         // call the upload help to perform the upload
         handleUploadHelper(this.fileInput,
                         "1",   // TODO: generate unique ID? (parent space noderef?)
                         MyDocs.updateCompleteHandler,
                         getContextPath(),
                         "/ajax/invoke/ContentUpdateBean.updateFile",
                         {nodeRef: this.popupPanel.nodeRef});
         this.fileInput = null;
      }      
      this.closePopupPanel();
   },
   
   /**
    * Callback function executed after the upload of a new file is complete
    */
   updateCompleteHandler: function(id, path, fileName, error)
   {
      if (error == null)
      {
         MyDocs.refreshList();
      }
      else
      {
         alert("ERROR: " + error);
      }
      if (this.fxOverlay)
      {
         this.fxOverlay.start(0);
      }
   },

   /**
    * Cancel button click handler for various pop-up panels
    */
   closePopupPanel: function()
   {
      if (this.popupPanel != null)
      {
         this.popupPanel.setStyle("display", "none");
         this.popupPanel = null;
      }
      if (this.fxOverlay)
      {
         this.fxOverlay.start(0);
      }
   },
   
   /**
    * Update the view filter
    */
   filter: function(filter)
   {
      if (this.popupPanel != null) return;

      $$('.docfilterLink').each(function(filterLink, i)
      {
         if (i == filter)
         {
            filterLink.addClass("docfilterLinkSelected");
         }
         else
         {
            filterLink.removeClass("docfilterLinkSelected");
         }
      });
      MyDocs.Filter = filter;
      MyDocs.refreshList(true);
   },

   /**
    * Refresh the main data list contents within the docPanel container
    */
   refreshList: function(reopenActive)
   {
      // do we want to remember which panel was open?
      if (reopenActive)
      {
         // do we have an open panel?
         var openPanel = $E('#docPanel .docItemSelected');
         var openPanelId = null;
         if (openPanel != null)
         {
            openPanelId = openPanel.id;
            // Re-open the panel if the id still exists
            MyDocs.postInit = function()
            {
               if ($(openPanelId))
               {
                  $(openPanelId).fireEvent("click");
   
                  // scroll the open panel into view               
                  var fxScroll = new Fx.Scroll($('docPanel'),
                  {
                     duration: MyDocs.ANIM_LENGTH,
                     transition: Fx.Transitions.linear
                  });
                  fxScroll.toElement($(openPanelId));
               }
            }
         }
      }

      // empty the main panel div and restart by reloading the panel contents
      var docPanel = $('docPanel');
      docPanel.setStyle('visibility', 'hidden');
      // show the ajax wait panel
      $('docPanelOverlayAjax').setStyle('display', 'block');
      
      // Stop all the animation effects
      MyDocs.FxAll.each(function(fx, i)
      {
         fx.stop();
      });
      
      docPanel.empty();
      docPanel.removeEvents('mouseleave');
      MyDocs.start();
   },
   
   /**
    * Apply a semi-transparent modal overlay skin to the main panel area
    */
   applyModal: function()
   {
      $("docPanelOverlay").setStyle('opacity', MyDocs.OVERLAY_OPACITY);
   },
   
   /**
    * Remove the modal overlay skin from the main panel area
    */
   removeModal: function()
   {
      $("docPanelOverlay").setStyle('opacity', 0);
   },
   
   /**
    * Called when the Edit Details dialog returns
    */
   editDetailsCallback: function()
   {
      // Refresh the inner panel
      MyDocs.refreshList(true);
   },

   /**
    * Display a message bubble of helpful info to the user. Calling this function in quick 
    * succession will cause previous message to be lost as the new ones are displayed.
    * 
    * @param message    Message text to display
    */
   displayMessage: function(message)
   {
      var panel = $("docMessagePanel");
      if ($defined(panel.timeout))
      {
         clearTimeout(panel.timeout);
         panel.timeout = null;
      }
      
      panel.setStyle("opacity", 0);
      panel.setStyle("margin-top", -60);
      
      panel.getChildren()[1].setHTML(message);
      
      // reset the close box animation by refreshing the image source
      $("docMessagePanelCloseImage").src = getContextPath() + "/images/icons/close_portlet_animation.gif";
      
      panel.fxMessage = new Fx.Styles(panel, 
      {
         duration: 1000,
         transition: Fx.Transitions.sineInOut
      });
      panel.fxMessage.start({'margin-top': -40, 'opacity': [0, 0.75]});

      
      panel.timeout = window.setTimeout(this.fadeOutMessage, 9000);
   },
   
   /**
    * Timer callback function to fade out the message panel
    */
   fadeOutMessage: function()
   {
      var panel = $("docMessagePanel");
      panel.timeout = null;
      
      var fxMessage = new Fx.Styles(panel, 
      {
         duration: 1000,
         transition: Fx.Transitions.sineInOut
      });
      fxMessage.start({'margin-top': -60, 'opacity': [0]});
   },
   
   /**
    * Close the message panel immediately when the user clicks the close icon
    */
   closeMessage: function()
   {
      var panel = $("docMessagePanel");
      if ($defined(panel.timeout))
      {
         clearTimeout(panel.timeout);
         panel.timeout = null;
      }
      panel.fxMessage.stop();
      panel.setStyle("opacity", 0);
   }
};

window.addEvent('load', MyDocs.start);