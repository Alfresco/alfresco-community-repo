Alfresco.toggleGeneratedFiles = function(icon, genFilesId)
{
   var currentState = icon.className;
   var genFilesDiv = document.getElementById(genFilesId);
   
   if (currentState == "collapsed")
   {
      icon.src = getContextPath() + "/images/icons/arrow_open.gif";
      icon.className = "expanded";
      
      // show the div holding the generated files
      if (genFilesDiv != null)
      {
         genFilesDiv.style.display = "block";
      }
   }
   else
   {
      icon.src = getContextPath() + "/images/icons/arrow_closed.gif";
      icon.className = "collapsed";
      
      // hide the div holding the generated files
      if (genFilesDiv != null)
      {
         genFilesDiv.style.display = "none";
      }
   }
}