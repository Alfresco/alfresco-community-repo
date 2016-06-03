
package org.alfresco.repo.search.impl.solr.facet.handler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * A simple handler to get the full user name from the userID.
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public class UserNameDisplayHandler extends AbstractFacetLabelDisplayHandler
{

    public UserNameDisplayHandler(Set<String> supportedFieldFacets)
    {
        ParameterCheck.mandatory("supportedFieldFacets", supportedFieldFacets);

        this.supportedFieldFacets = Collections.unmodifiableSet(new HashSet<>(supportedFieldFacets));
    }

    @Override
    public FacetLabel getDisplayLabel(String value)
    {
        String name = null;

        final NodeRef personRef = serviceRegistry.getPersonService().getPersonOrNull(value);
        if (personRef != null)
        {
            NodeService nodeService = serviceRegistry.getNodeService();
            final String firstName = (String) nodeService.getProperty(personRef, ContentModel.PROP_FIRSTNAME);
            final String lastName = (String) nodeService.getProperty(personRef, ContentModel.PROP_LASTNAME);
            name = (firstName != null ? firstName + " " : "") + (lastName != null ? lastName : "");
        }
        return new FacetLabel(value, name == null ? value : name.trim(), -1);
    }
}
