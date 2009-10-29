script:
{
    var serviceUrl = (args.service === null) ? "/api/repository" : args.service;
    var conn = remote.connect("alfresco");
    var result = conn.get(stringUtils.urlEncodeComponent(serviceUrl));
	
    var service = atom.toService(result.response);
    var workspace = service.workspaces.get(0);
    model.repo = workspace.getExtension(atom.names.cmisra_repositoryInfo);
}
