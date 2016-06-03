package org.alfresco.repo.security.person;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.FileNameValidator;

/**
 * Creates home folders directly under the root path, based on the username of the user.
 * 
 * @deprecated 
 * Depreciated since 4.0. {@link UsernameHomeFolderProvider} should now be used.
 * 
 * @author Andy Hind
 */
public class UIDBasedHomeFolderProvider extends ExistingPathBasedHomeFolderProvider
{
    private String templatePath;

    private NodeRef templateNodeRef;

    public void setTemplatePath(String templatePath)
    {
        this.templatePath = templatePath;
    }

    protected synchronized NodeRef getTemplateNodeRef()
    {
        if (templateNodeRef == null && templatePath != null)
        {
            templateNodeRef = resolvePath(templatePath);
        }
        return templateNodeRef;
    }

    public List<String> getHomeFolderPath(NodeRef person)
    {
        List<String> path = new ArrayList<String>(1);
        path.add(FileNameValidator.getValidFileName(
                getHomeFolderManager().getPersonProperty(person, ContentModel.PROP_USERNAME)));
        return path;
    }

    protected HomeSpaceNodeRef getHomeFolder(NodeRef person)
    {
        return getHomeFolderManager().getHomeFolder(getV2Adaptor(), person, false);
    }
}
