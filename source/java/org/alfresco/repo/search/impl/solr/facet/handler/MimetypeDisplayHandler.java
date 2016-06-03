
package org.alfresco.repo.search.impl.solr.facet.handler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * A simple handler to get the Mimetype display label.
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public class MimetypeDisplayHandler extends AbstractFacetLabelDisplayHandler
{

    public MimetypeDisplayHandler(Set<String> supportedFieldFacets)
    {
        ParameterCheck.mandatory("supportedFieldFacets", supportedFieldFacets);

        this.supportedFieldFacets = Collections.unmodifiableSet(new HashSet<>(supportedFieldFacets));
    }

    @Override
    public FacetLabel getDisplayLabel(String value)
    {
        Map<String, String> mimetypes = serviceRegistry.getMimetypeService().getDisplaysByMimetype();
        String displayName = mimetypes.get(value);
        return new FacetLabel(value, displayName == null ? value : displayName.trim(), -1);
    }
}