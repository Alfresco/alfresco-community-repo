
package org.alfresco.repo.virtual.template;

import java.io.Serializable;

import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.SearchLanguageConversion;

/**
 * Specifies a constraint on a property value, as for e.g.
 * ContentModel.PROP_NAME, to be applied as name pattern to queries given in the virtual folder
 * template.
 *
 *@author sdinuta
 */
public class NamePatternPropertyValueConstraint extends PropertyValueConstraint
{
    private QName property;

    private Serializable value;

    private NamespacePrefixResolver nspResolver;

    public NamePatternPropertyValueConstraint(VirtualQueryConstraint decoratedConstraint, QName property,
                Serializable value, NamespacePrefixResolver nspResolver)
    {
        super(decoratedConstraint,
              property,
              value,
              nspResolver);
        this.property = property;
        this.value = value;
        this.nspResolver = nspResolver;
    }

    @Override
    protected SearchParameters applyFTS(SearchParameters searchParameters)
    {
        String filePattern;
        StringBuffer luceneReserved = new StringBuffer();
        for(int i=0;i<value.toString().length();i++){
            if(SearchLanguageConversion.DEF_LUCENE.isReserved(value.toString().charAt(i))){
                luceneReserved.append(value.toString().charAt(i));
            }
        }
        String luceneReservedStr=luceneReserved.toString();
        String pattern =org.alfresco.util.ISO9075.encode(value.toString());
        for (int i = 0; i < luceneReservedStr.length(); i++)
        {
            pattern = pattern.replace(org.alfresco.util.ISO9075.encode(luceneReservedStr.substring(i,i + 1)),
                                      luceneReservedStr.substring(i,i + 1));
        }
        filePattern=SearchLanguageConversion.escapeForLucene(pattern);

        SearchParameters constrainedParameters = searchParameters.copy();
        String theQuery = constrainedParameters.getQuery();

        StringBuilder sb = new StringBuilder();
        sb.append("(" + theQuery + ")");
        sb.append(" and (");
        sb.append("TEXT:(").append(filePattern).append(") ");
        sb.append("or (");
        sb.append(" =").append(property.toPrefixString(this.nspResolver));
        sb.append(":").append(filePattern);
        sb.append(" ) ");
        sb.append(")");
        theQuery = sb.toString();
        constrainedParameters.setQuery(theQuery);

        return constrainedParameters;
    }

}
