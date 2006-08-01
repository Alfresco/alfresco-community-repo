/*
 iframecontentmws.js - Foteos Macrides
   Initial: October 10, 2004 - Last Revised: May 9, 2005
 Simple script for using an HTML file as iframe content in overlibmws popups.
 Include WRAP and TEXTPADDING,0 in the overlib call to ensure that the width
 arg is respected (unless the CAPTION plus CLOSETEXT widths add up to more than
 the width arg, in which case you should increase the width arg).  The name arg
 should be a unique string for each popup with iframe content in the document.
 The frameborder arg should be 1 (browser default if omitted) or 0.

 See http://www.macridesweb.com/oltest/IFRAME.html for demonstration.
*/

function OLiframeContent(src, width, height, name, frameborder) {
 return ('<iframe src="'+src+'" width="'+width+'" height="'+height+'"'
 +(name!=null?' name="'+name+'" id="'+name+'"':'')
 +(frameborder!=null?' frameborder="'+frameborder+'"':'')
 +' scrolling="auto">'
 +'<div>[iframe not supported]</div></iframe>');
}
