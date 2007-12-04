/*
 * Prerequisites: mootools.v1.11.js
 *                office_addin.js
 */
var OfficeDocDetails =
{
   init: function()
   {
      OfficeDocDetails.setupTabs();
      OfficeDocDetails.setupTags();
   },

   setupTabs: function()
   {
      var tabs = $$('.tabBarInline li');
      var tablinks = $$('.tabBarInline li a'); 
      var panels = $$('.tabPanel');
      
      tabs.each(function(tab, i)
      {
         // register 'click' event for each tab
         tablinks[i].addEvent('click', function(e)
         {
            // highlight the current tab
            tab.addClass('current');
            // show the tab panel
            panels[i].removeClass('tabPanelHidden');

            // reset styles on all closed tasks
            tabs.each(function(otherTab, j)
            {
               if (otherTab != tab)
               {
                  // reset selected class
                  otherTab.removeClass('current');
                  // hide the tab panel
                  panels[j].addClass('tabPanelHidden');
               }
            });
         });
      });
   },
   
   setupTags: function()
   {
      var tags = $$('#panelTags .tagName');
      
      tags.each(function(tag, i)
      {
         tag.addEvent('click', function(e)
         {
            window.location.href = window.serviceContextPath + "/office/tags?p=" + window.queryObject.p + "&tag=" + tag.innerHTML;
         });
      });
   },

   showAddTagForm: function()
   {
      $("addTagLinkContainer").setStyle("display", "none");
      $("addTagFormContainer").setStyle("display", "block");
      $("addTagBox").focus();
   },
   
   hideAddTagForm: function()
   {
      $("addTagFormContainer").setStyle("display", "none");
      $("addTagLinkContainer").setStyle("display", "block");
      return false;
   },
   
   addTag: function(nodeId, tagName)
   {
      OfficeAddin.postAction(window.serviceContextPath + "/collaboration/tagActions", "add", nodeId, null, "t=" + tagName);
      return false;
   },
   
   removeTag: function(nodeId, tagName)
   {
      OfficeAddin.postAction(window.serviceContextPath + "/collaboration/tagActions", "remove", nodeId , null, "t=" + tagName);
      return false;
   }
};

window.addEvent('domready', OfficeDocDetails.init);