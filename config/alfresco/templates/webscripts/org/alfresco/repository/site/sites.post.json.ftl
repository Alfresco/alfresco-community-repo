{
	"url" : "${url.serviceContext}/api/sites/${site.shortName}",
	"sitePreset" : "${site.sitePreset}",
	"shortName" : "${site.shortName}",
	"title" : "${site.title}",
	"description" : "${site.description}",
	"isPublic" : ${site.isPublic?string("true", "false")}
}