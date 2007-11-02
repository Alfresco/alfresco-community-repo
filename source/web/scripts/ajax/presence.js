/*
* Prerequisites: mootools.v1.11.js
*/
var Presence =
{
	init: function()
	{
      window.contextPath = Presence.getContextPath();

		var users = $$("#presenceContainer .presenceStatus");
		users.each(function(user, i)
		{
			// ajax call to load online status
			var userDetails = user.attributes["rel"].value.split("|");
			var proxyURL = window.contextPath + "/ajax/invoke/PresenceProxyBean.proxyRequest";
			var statusURL = Presence.getStatusURL(userDetails);
			
			if (statusURL != "")
			{
				var myAjax = new Ajax(proxyURL, {
					method: 'get',
					headers: {'If-Modified-Since': 'Sat, 1 Jan 2000 00:00:00 GMT'},
					onComplete: function(textResponse, xmlResponse)
					{
						var statusType = Presence.getStatusType(userDetails[0], textResponse);
						user.addClass(statusType);
					}
				});
				myAjax.request("url=" + escape(statusURL));
			}
			else
			{
				user.addClass("unknown");
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
				statusType = (response == "Online") ? "online" : "offline";
				break;
			case "yahoo":
				statusType = (response == "01") ? "online" : "offline";
				break;
		}
	
		return statusType;
	}
}

window.addEvent('domready', Presence.init);
