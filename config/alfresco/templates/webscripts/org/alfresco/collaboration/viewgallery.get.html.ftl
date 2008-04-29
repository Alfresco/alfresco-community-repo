
<#assign dataURL=absurl(url.context) + "/wcs/collaboration/gallery/" + path>

<html lang="en">
<head>

</head>

<body scroll='no' height="100%" width="100%">
	<object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"
			id="Flexbook" width="100%" height="100%"
			codebase="http://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab">
			<param name="movie" value="/alfresco/swf/Flexbook.swf?dataURL=${dataURL?url}&ticket=${ticket}" />
			<param name="quality" value="high" />
			<param name="bgcolor" value="#282828" />
			<param name="allowScriptAccess" value="sameDomain" />
			<embed src="/alfresco/swf/Flexbook.swf?dataURL=${dataURL?url}&ticket=${ticket}" quality="high" bgcolor="#000000"
				width="100%" height="310" name="Flexbook" align="middle"
				play="true"
				loop="false"
				quality="high"
				allowScriptAccess="sameDomain"
				type="application/x-shockwave-flash"
				pluginspage="http://www.adobe.com/go/getflashplayer">
			</embed>
	</object>
</body>
</html>
