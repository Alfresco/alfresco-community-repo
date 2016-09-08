function existsTitle(caveatConfig, title)
{
   var constraints = caveatConfig.allConstraints;

   // Check for existing constraint...
   var alreadyExists = false;
   for (var i = 0; i < constraints.length; i++)
   {
      var currTitle = constraints[i].title;
      if (currTitle + "" == title)
      {
         alreadyExists = true;
         break;
      }
   }

   return alreadyExists;
}