Alfresco.tabSelected = function(tab)
{
   var tabElem = document.getElementById(_alfCurrentTab);
   if (tabElem != null)
   {
      tabElem.className = "";
   }
   
   var tabContentElem = document.getElementById(_alfCurrentTab + "Content");
   if (tabContentElem != null)
   {
      tabContentElem.style.display = "none";
   }
   
   tabElem = document.getElementById(tab + "Tab");
   if (tabElem != null)
   {
      tabElem.className = "selectedTab";
      _alfCurrentTab = tab + "Tab";
   }
   
   tabContentElem = document.getElementById(tab + "TabContent");
   if (tabContentElem != null)
   {
      tabContentElem.style.display = "block";
   }
}

Alfresco.toggleBrokenLinks = function(icon, brokenLinksId)
{
   var currentState = icon.className;
   var brokenLinksDiv = document.getElementById(brokenLinksId);
   
   if (currentState == "linkValToggleCollapsed")
   {
      icon.src = getContextPath() + "/images/icons/arrow_open.gif";
      icon.className = "linkValToggleExpanded";
      
      // show the div holding the broken links
      if (brokenLinksDiv != null)
      {
         brokenLinksDiv.style.display = "block";
      }
   }
   else
   {
      icon.src = getContextPath() + "/images/icons/arrow_closed.gif";
      icon.className = "linkValToggleCollapsed";
      
      // hide the div holding the broken links
      if (brokenLinksDiv != null)
      {
         brokenLinksDiv.style.display = "none";
      }
   }
}

Alfresco.toggleGeneratedFiles = function(icon, genFilesId)
{
   var currentState = icon.className;
   var genFilesDiv = document.getElementById(genFilesId);
   
   if (currentState == "linkValToggleCollapsed")
   {
      icon.src = getContextPath() + "/images/icons/arrow_open.gif";
      icon.className = "linkValToggleExpanded";
      
      // show the div holding the generated files
      if (genFilesDiv != null)
      {
         genFilesDiv.style.display = "block";
      }
   }
   else
   {
      icon.src = getContextPath() + "/images/icons/arrow_closed.gif";
      icon.className = "linkValToggleCollapsed";
      
      // hide the div holding the generated files
      if (genFilesDiv != null)
      {
         genFilesDiv.style.display = "none";
      }
   }
}

Alfresco.increaseTabSize = function(tabBodyId)
{
   var tabElem = document.getElementById(tabBodyId);
   
   if (tabElem != null)
   {
      var currentHeight = YAHOO.util.Dom.getStyle(tabElem, "height"); 
      var size = currentHeight.substring(0, currentHeight.length-2);
      var newSize = Number(size) + 100;
      
      YAHOO.util.Dom.setStyle(tabElem, "height", newSize + "px");
   }
}

Alfresco.decreaseTabSize = function(tabBodyId)
{
   var tabElem = document.getElementById(tabBodyId);
   
   if (tabElem != null)
   {
      var currentHeight = YAHOO.util.Dom.getStyle(tabElem, "height"); 
      var size = currentHeight.substring(0, currentHeight.length-2);
      var newSize = Number(size) - 100;
      if (newSize < 100)
      {
         newSize = 100;
      }
      
      YAHOO.util.Dom.setStyle(tabElem, "height", newSize + "px");
   }
}
