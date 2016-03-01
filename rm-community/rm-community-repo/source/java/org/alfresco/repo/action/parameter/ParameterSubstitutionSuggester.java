 
package org.alfresco.repo.action.parameter;

import java.util.List;

public interface ParameterSubstitutionSuggester
{
    int DEFAULT_MAXIMUM_NUMBER_SUGGESTIONS = 10;

    List<String> getSubstitutionSuggestions(final String substitutionFragment);
}
