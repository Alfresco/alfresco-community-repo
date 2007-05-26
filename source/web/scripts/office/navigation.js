
var xmlHttp

function GetXmlHttpObject()
{ 
   var objXMLHttp=null;
   if (window.XMLHttpRequest)
   {
      objXMLHttp=new XMLHttpRequest()
   }
   else if (window.ActiveXObject)
   {
       objXMLHttp=new ActiveXObject("Microsoft.XMLHTTP")
   }

   return objXMLHttp;
} 

function showStatus(url)
{
   xmlHttp=GetXmlHttpObject();
   if (xmlHttp==null)
   {
       alert("Browser does not support HTTP Request");
       return;
   }        
   xmlHttp.onreadystatechange=stateChanged;
   xmlHttp.open("GET",url+"&sid="+Math.random(),true);
   xmlHttp.send(null);
} 

function stateChanged() 
{ 
   if (xmlHttp.readyState==4 || xmlHttp.readyState=="complete")
   { 
      if (xmlHttp.responseText.indexOf("System Error") > 0)
      {
          var myWindow = window.open("", "_blank", "scrollbars,height=500,width=400");
          myWindow.document.write(xmlHttp.responseText);
          document.getElementById("statusArea").innerHTML=""; 
      }
      else
      {
          document.getElementById("statusArea").innerHTML=xmlHttp.responseText; 
          window.location.reload();
      }
   } 
} 




function runAction(useTemplate, Action, Doc, Msg)
{
   if (Msg != "" && !confirm(Msg))
   {
       return;
   }
   document.getElementById("statusArea").innerHTML="Running action...";
   showStatus("/alfresco/command/script/execute/workspace/SpacesStore/" + useTemplate + "/workspace/SpacesStore/" + Doc + "?action=" + Action);
}

function getWindowHeight() {
			var windowHeight = 0;
			if (typeof(window.innerHeight) == 'number') {
				windowHeight = window.innerHeight;
			}
			else {
				if (document.documentElement && document.documentElement.clientHeight) {
					windowHeight = document.documentElement.clientHeight;
				}
				else {
					if (document.body && document.body.clientHeight) {
						windowHeight = document.body.clientHeight;
					}
				}
			}
			return windowHeight;
		}
		function setContent() {
			if (document.getElementById) {
				var windowHeight = getWindowHeight();
				if (windowHeight > 0) {
					var spaceListElement = document.getElementById('spaceList');
					var contentListElement = document.getElementById('contentList');
					var tabBarElement = document.getElementById('tabBar');
                                        var currentSpaceInfoElement = document.getElementById('currentSpaceInfo');
                                        var spaceListHeaderElement = document.getElementById('spaceListHeader');
                                        var contentListHeaderElement = document.getElementById('contentListHeader');
                                        var bottomMarginElement = document.getElementById('bottomMargin');
                                        var documentActionsElement = document.getElementById('documentActions');

					var spaceListHeight = spaceListElement.offsetHeight;
					var contentListHeight = contentListElement.offsetHeight;
					var tabBarHeight = tabBarElement.offsetHeight;
					var currentSpaceInfoHeight = currentSpaceInfoElement.offsetHeight;
					var spaceListHeaderHeight = spaceListHeaderElement.offsetHeight;
					var contentListHeaderHeight = contentListHeaderElement.offsetHeight;
					var bottomMarginHeight = bottomMarginElement.offsetHeight;
                                        var documentActionsHeight = documentActionsElement.offsetHeight;

					if (windowHeight > 0) {
						spaceListElement.style.height = (windowHeight- (tabBarHeight + currentSpaceInfoHeight + spaceListHeaderHeight + contentListHeaderHeight + documentActionsHeight  + bottomMarginHeight)) /2 + 'px';
						contentListElement.style.height = (windowHeight- (tabBarHeight + currentSpaceInfoHeight + spaceListHeaderHeight + contentListHeaderHeight + documentActionsHeight  + bottomMarginHeight)) /2 + 'px';
					}

				}
			}
		}
		window.onload = function() {
			setContent();
			stripe('spaceList', '#fff', '#f6f8fa');
                        stripe('contentList', '#fff', '#f6f8fa');
		}
		window.onresize = function() {
			setContent();
		}



  // this function is need to work around
  // a bug in IE related to element attributes
  function hasClass(obj) {
     var result = false;
     if (obj.getAttributeNode("class") != null) {
         result = obj.getAttributeNode("class").value;
     }
     return result;
  }

 function stripe(id) {

    // the flag we'll use to keep track of
    // whether the current row is odd or even
    var even = false;

    // if arguments are provided to specify the colours
    // of the even & odd rows, then use the them;
    // otherwise use the following defaults:
    var evenColor = arguments[1] ? arguments[1] : "#fff";
    var oddColor = arguments[2] ? arguments[2] : "#eee";

    // obtain a reference to the desired table
    // if no such table exists, abort
    var table = document.getElementById(id);
    if (! table) { return; }

    // by definition, tables can have more than one tbody
    // element, so we'll have to get the list of child
    // &lt;tbody&gt;s
    var tbodies = table.getElementsByTagName("tbody");

    // and iterate through them...
    for (var h = 0; h < tbodies.length; h++) {

     // find all the &lt;tr&gt; elements...
      var trs = tbodies[h].getElementsByTagName("tr");

      // ... and iterate through them
      for (var i = 0; i < trs.length; i++) {

	    // avoid rows that have a class attribute
        // or backgroundColor style
	    if (!hasClass(trs[i]) && ! trs[i].style.backgroundColor) {

         // get all the cells in this row...
          var tds = trs[i].getElementsByTagName("td");

          // and iterate through them...
          for (var j = 0; j < tds.length; j++) {

            var mytd = tds[j];

            // avoid cells that have a class attribute
            // or backgroundColor style
	        if (! hasClass(mytd) && ! mytd.style.backgroundColor) {

		      mytd.style.backgroundColor = even ? evenColor : oddColor;

            }
          }
        }
        // flip from odd to even, or vice-versa
        even =  ! even;
      }
    }
  }
