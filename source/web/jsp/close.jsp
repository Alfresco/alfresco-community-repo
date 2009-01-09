<script type="text/javascript">
	if ((window.opener) && (window.opener.alfrescoCallback))
	{
		window.opener.alfrescoCallback();
	}

	// Prevent Windows WebBrowser control from asking user
	// whether they want to close the window   
	// See http://dotnetslackers.com/Community/blogs/haissam/archive/2007/04/20/Javascript_3A00_-Close-window-without-the-prompt-message-in-IE7.aspx 
    var ie7 = (document.all && !window.opera && window.XMLHttpRequest) ? true : false;  
    if (ie7)
    {    
		window.open('','_parent','');
		window.close();
    }
	else
    {
		window.opener = window;
		window.close();
	}
</script>
