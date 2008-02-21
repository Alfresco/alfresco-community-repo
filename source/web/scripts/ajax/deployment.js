Alfresco.deployServerTypeChanged = function() 
{
   var typeDropDown = document.getElementById('wizard:wizard-body:deployServerType');
   if (typeDropDown != null)
   {
      var selectedType = typeDropDown.options[typeDropDown.selectedIndex].value;
      
      // show or hide the label
      var autoDeployLabel = document.getElementById('autoDeployLabel');
      if (autoDeployLabel != null)
      {
         if (selectedType == "test")
         {
            autoDeployLabel.style.display = "none";
         }
         else
         {
            autoDeployLabel.style.display = "block";
         }
      }
      
      // show or hide the checkbox
      var autoDeployCheckbox = document.getElementById('wizard:wizard-body:autoDeployCheckbox');
      if (autoDeployCheckbox != null)
      {
         if (selectedType == "test")
         {
            autoDeployCheckbox.style.display = "none";
         }
         else
         {
            autoDeployCheckbox.style.display = "block";
         }
      }
   }
}