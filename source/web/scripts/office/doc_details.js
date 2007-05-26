var xmlHttp

function showStatus(url)
{
   xmlHttp=GetXmlHttpObject()
   if (xmlHttp==null)
   {
      alert ("Browser does not support HTTP Request")
      return
   }        
   xmlHttp.onreadystatechange=stateChanged 
   xmlHttp.open("GET",url,true)
   xmlHttp.send(null)
} 

function stateChanged() 
{ 
   if (xmlHttp.readyState==4 || xmlHttp.readyState=="complete")
   { 
      document.getElementById("statusArea").innerHTML=xmlHttp.responseText 
      window.location.reload();
   } 
} 

function GetXmlHttpObject()
{ 
   var objXMLHttp = null;
   if (window.XMLHttpRequest)
   {
      objXMLHttp=new XMLHttpRequest();
   }
   else if (window.ActiveXObject)
   {
      objXMLHttp=new ActiveXObject("Microsoft.XMLHTTP");
   }
   return objXMLHttp;
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


function getWindowHeight()
{
   var windowHeight = 0;
   if (typeof(window.innerHeight) == 'number')
   {
      windowHeight = window.innerHeight;
   }
   else
   {
      if (document.documentElement && document.documentElement.clientHeight)
      {
         windowHeight = document.documentElement.clientHeight;
      }
      else
      {
         if (document.body && document.body.clientHeight)
         {
            windowHeight = document.body.clientHeight;
         }
      }
   }
   return windowHeight;
}

function setContent()
{
   if (document.getElementById)
   {
      var windowHeight = getWindowHeight();
      if (windowHeight > 0)
      {
         var detailsListHeaderElement = document.getElementById('detailsListHeader');
         var detailsListElement = document.getElementById('detailsList');
         var tabBarElement = document.getElementById('tabBar');
         var bottomMarginElement = document.getElementById('bottomMargin');
         var documentActionsElement = document.getElementById('documentActions');
         
         var detailsListHeight = detailsListElement.offsetHeight;
         var detailsListHeaderHeight = detailsListHeaderElement.offsetHeight;
         var tabBarHeight = tabBarElement.offsetHeight;
         var bottomMarginHeight = bottomMarginElement.offsetHeight;
         var documentActionsHeight = documentActionsElement.offsetHeight;
   
         if (windowHeight > 0)
         {
            detailsListElement.style.height = ((windowHeight- (tabBarHeight + detailsListHeaderHeight + documentActionsHeight + bottomMarginHeight)) /3) * 2 + 'px';
            documentActionsElement.style.height = (windowHeight- (tabBarHeight + detailsListHeaderHeight + documentActionsHeight + bottomMarginHeight)) /3 + 'px';
         }
      }
   }
}

window.onload = function()
{
   setContent();
}

window.onresize = function()
{
   setContent();
}