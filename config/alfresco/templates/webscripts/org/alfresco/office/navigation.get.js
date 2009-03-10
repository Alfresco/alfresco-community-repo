if ((args.n) && (args.n != ""))
{
   model.node = search.findNode("workspace://SpacesStore/" + args.n);
}

// Check here in case invalid nodeRef passed-in
if (model.node == null)
{
   if ((args.p) && (args.p != ""))
   {
      var path = args.p;
      if (path == "/" + companyhome.name)
      {
         model.node = companyhome;
      }
      else
      {
         var node = companyhome.childByNamePath(path.substring(companyhome.name.length));
         if (node != null)
         {
            model.node = node;
         }
         else
         {
            model.node = userhome;
         }
      }
   }
}

// Last chance - default to userhome
if (model.node == null)
{
   model.node = userhome;
}
