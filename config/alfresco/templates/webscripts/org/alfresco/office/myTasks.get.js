// Was a document passed-in for New Workflow?
if ((args.wd) && (args.wd != ""))
{
   model.docWorkflow = search.findNode("workspace://SpacesStore/" + args.wd);
}
