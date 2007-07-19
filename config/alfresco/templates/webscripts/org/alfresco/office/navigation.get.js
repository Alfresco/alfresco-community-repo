
if ((args.n) && (args.n != ""))
{
   model.node = search.findNode("workspace://SpacesStore/" + args.n);
}
else if ((args.p) && (args.p != ""))
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
else
{
   model.node = userhome;
}
model.path = args.p;
