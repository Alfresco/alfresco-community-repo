
if ((args.n) && (args.n != ""))
{
   model.node = search.findNode("workspace://SpacesStore/" + args.n);
}
else
{
   model.node = companyhome;
}
model.path = args.p;
