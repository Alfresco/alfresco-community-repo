if (args["n"] != null)
{
	var dest = search.findNode(args["n"]);
	dest.addAspect("cm:webscriptable");
	dest.properties["cm:webscript"] = args["w"];
	dest.save();
}
