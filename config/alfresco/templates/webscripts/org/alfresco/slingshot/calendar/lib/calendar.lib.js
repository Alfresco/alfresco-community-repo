function getCalendarContainer(site)
{
   var calendar;
   
   if (site.hasContainer("calendar"))
   {
      calendar = site.getContainer("calendar");
   }
   else
   {
      var perms = Array();
      perms["GROUP_EVERYONE"] = "SiteCollaborator"; 
      calendar = site.createContainer("calendar", null, perms);
   }

   if (!calendar.isTagScope)
   {
      calendar.isTagScope = true;
   }
   
   return calendar;
}