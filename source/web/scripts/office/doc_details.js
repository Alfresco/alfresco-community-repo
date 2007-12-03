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
      OfficeAddin.postAction(window.serviceContextPath + "/collaboration/tagActions", "add", nodeId, null, "&t=" + tagName);
      return false;
   },
   
   tagAction: function(action, nodeId, tagName, msg)
   {
      if (msg != "" && !confirm(msg))
      {
         return;
      }
   
      OfficeAddin.showStatusText("Processing...", "ajax_anim.gif", false);
      var actionURL = window.serviceContextPath + "/collaboration/tagActions?a=" + action + "&n=" + nodeId + "&t=" + tagName;
      var myAjax = new Ajax(actionURL, {
         method: 'post',
         headers: {'If-Modified-Since': 'Sat, 1 Jan 2000 00:00:00 GMT'},
         onComplete: function(textResponse, xmlResponse)
         {
            // Optionally add a status string
            if (textResponse != "")
            {
               var objResponse = Json.evaluate(textResponse);
               var imgSuccess = (objResponse.statusCode ? "action_successful.gif" : "action_failed.gif");
               var colBackground = (objResponse.statusCode ? "#50ff50" : "#ff5050");
               OfficeAddin.showStatusText(objResponse.statusString, imgSuccess, true, colBackground);
            }
         },
         onFailure: function()
         {
            OfficeAddin.showStatusText("Action failed", "action_failed.gif", true);
         }
      }).request();
   }
};

window.addEvent('domready', OfficeDocDetails.init);