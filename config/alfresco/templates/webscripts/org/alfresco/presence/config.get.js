if (args["n"] != null)
{
	var dest = search.findNode(args["n"]);
	if (args["add"] != null)
	{
		dest.addAspect("cm:webscriptable");
		dest.properties["cm:webscript"] = args["w"];
	}
	else
	{
		dest.removeAspect("cm:webscriptable");
	}
	dest.save();
}
