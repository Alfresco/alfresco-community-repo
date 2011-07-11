function getCalendarContainer(site)
{
   var calendar;
   
   if (site.hasContainer("calendar"))
   {
      calendar = site.getContainer("calendar");
   }
   else
   {
      calendar = site.createContainer("calendar");
   }

   if(calendar != null)
   {
      if (!calendar.isTagScope)
      {
         calendar.isTagScope = true;
      }
   }
   
   return calendar;
}
