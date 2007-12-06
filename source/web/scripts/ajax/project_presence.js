/*
* Prerequisites: mootools.v1.11.js
*/
var ProjectPresence =
{
   OFFLINE_OPACITY: 0.2,
   
	init: function()
	{
      window.contextPath = ProjectPresence.getContextPath();

		var statuses = $$("#projectColleagues .colleaguePresence");
		var rows = $$("#projectColleagues .colleagueRow");

      $('colleaguesNotOnline').setStyle("opacity", ProjectPresence.OFFLINE_OPACITY);
		
		statuses.each(function(status, i)
		{
			var userDetails = status.attributes["rel"].value.split("|");
			var proxyURL = window.contextPath + "/ajax/invoke/PresenceProxyBean.proxyRequest";
			var statusURL = ProjectPresence.getStatusURL(userDetails);
			
			if (statusURL != "")
			{
				// ajax call to load online status
				var myAjax = new Ajax(proxyURL, {
					method: 'get',
					headers: {'If-Modified-Since': 'Sat, 1 Jan 2000 00:00:00 GMT'},
					onComplete: function(textResponse, xmlResponse)
					{
						var statusType = ProjectPresence.getStatusType(userDetails[0], textResponse);
						status.addClass(userDetails[0] + "-" + statusType);
						if (statusType == "unknown")
						{
							status.title = "User's status is unknown, possibly due to client privacy settings";
						}
						else
						{
							status.title = "User's status is " + statusType;
						}
						if (statusType == "online")
						{
						   $('colleaguesOnline').adopt(rows[i]);
						}
						else
						{
						   $('colleaguesNotOnline').adopt(rows[i]);
						}
					}
				}).request("url=" + escape(statusURL));
			}
			else
			{
				status.addClass("none");
				status.title = "User's presence provider has not been configured by Alfresco admin";
			   $('colleaguesNotOnline').adopt(rows[i]);
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
				statusURL = "http://mystatus.skype.com/" + username + ".txt";
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
