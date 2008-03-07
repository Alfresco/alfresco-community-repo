/*
* Prerequisites: mootools.v1.11.js
*/
var ProjectPresence =
{
   OFFLINE_OPACITY: 0.2,
   
   init: function()
   {
      window.contextPath = ProjectPresence.getContextPath();
		$("refreshColleagues").addEvent("click", ProjectPresence.refreshStatus);
		ProjectPresence.refreshStatus();
	},
	
	refreshStatus: function()
	{
      var statuses = $$("#projectColleagues .colleaguePresence");
      var rows = $$("#projectColleagues .colleagueRow");
      
      $("colleaguesNotOnline").setStyle("opacity", ProjectPresence.OFFLINE_OPACITY);
      
      statuses.each(function(stat, i)
      {
         var row = rows[i];
         var userDetails = stat.attributes["rel"].value.split("|");
         var proxyURL = window.contextPath + "/ajax/invoke/PresenceProxyBean.proxyRequest";
         var statusURL = ProjectPresence.getStatusURL(userDetails);

         row.removeEvent("click");
         row.setStyle("cursor", "auto");
         
         if (statusURL != "")
         {
            stat.attributes["class"].value = "colleaguePresence";
            
            // ajax call to load online status
            var myAjax = new Ajax(proxyURL, {
               method: 'get',
               headers: {'If-Modified-Since': 'Sat, 1 Jan 2000 00:00:00 GMT'},
               onComplete: function(textResponse, xmlResponse)
               {
                  var statusType = ProjectPresence.getStatusType(userDetails[0], textResponse);
                  stat.addClass(userDetails[0] + "-" + statusType);
                  if (statusType == "unknown")
                  {
                     stat.title = "User's status is unknown, possibly due to client privacy settings";
                  }
                  else
                  {
                     stat.title = "User's status is " + statusType;
                  }
                  if (statusType == "online")
                  {
                     $("colleaguesOnline").adopt(row);
                     if ((userDetails[0] == "skype") && (row.attributes["rel"] == null))
                     {
                        row.addEvent("click", function()
                        {
                           window.location = "skype:" + userDetails[1] + "?chat";
                        });
                        row.setStyle("cursor", "pointer");
                     }
                  }
                  else
                  {
                     $("colleaguesNotOnline").adopt(rows[i]);
                     // Fix-up IE overlay for images
                     if (window.ie)
                     {
                        stat.getParent().setStyle("opacity", ProjectPresence.OFFLINE_OPACITY);
                     }
                  }
               }
            }).request("url=" + escape(statusURL));
         }
         else
         {
            stat.addClass("none");
            stat.title = "User's presence provider has not been configured by Alfresco admin";
            $("colleaguesNotOnline").adopt(row);
            // Fix-up IE overlay for images
            if (window.ie)
            {
               stat.getParent().setStyle("opacity", ProjectPresence.OFFLINE_OPACITY);
            }
         }
      });
   },
   
   /* Calculates and returns the context path for the current page */
   getContextPath: function()
   {
      var path = window.location.pathname;
      var idx = path.indexOf("/", 1);
      var contextPath = "";
      if (idx != -1)
      {
         contextPath = path.substring(0, idx);
      }
      else
      {
         contextPath = "";
      }
   
      return contextPath;
   },

   getStatusURL: function(userDetails)
   {
      var provider = userDetails[0];
      var username = userDetails[1];
      var statusURL = "";
   
      switch(provider)
      {
         case "skype":
            statusURL = "http://mystatus.skype.com/" + username + ".txt.en";
            break;
         case "yahoo":
            statusURL = "http://opi.yahoo.com/online?u=" + username + "&m=t&t=1";
            break;
      }
      
      return statusURL;
   },
   
   getStatusType: function(provider, response)
   {
      var statusType = "unknown";
   
      switch(provider)
      {
         case "skype":
            switch (response)
            {
               case "Online":
                  statusType = "online";
                  break;
               case "Offline":
                  statusType = "offline";
                  break;
               default:
                  statusType = "unknown";
            }
            break;
         case "yahoo":
            statusType = (response == "01") ? "online" : "offline";
            break;
      }
   
      return statusType;
   }
}

window.addEvent('domready', ProjectPresence.init);
