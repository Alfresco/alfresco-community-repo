package org.alfresco.rest.api.people;

import org.alfresco.rest.api.Preferences;
import org.alfresco.rest.api.model.Preference;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

@RelationshipResource(name = "preferences", entityResource = PeopleEntityResource.class, title = "Person Preferences")
public class PersonPreferencesRelation implements RelationshipResourceAction.Read<Preference>, RelationshipResourceAction.ReadById<Preference>, InitializingBean
{
    private static final Log logger = LogFactory.getLog(PersonPreferencesRelation.class);

    private Preferences preferences;

	public void setPreferences(Preferences preferences)
	{
		this.preferences = preferences;
	}

	@Override
    public void afterPropertiesSet()
    {
        ParameterCheck.mandatory("preferences", this.preferences);
    }
	
	/**
	 * Returns a paged list of preferences for the user personId.
	 * 
	 * If personId does not exist, NotFoundException (status 404).
	 * 
	 * @see org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction.Read#readAll(java.lang.String, org.alfresco.rest.framework.resource.parameters.Parameters)
	 */
    @Override
    @WebApiDescription(title = "A paged list of the persons preferences.")
    public CollectionWithPagingInfo<Preference> readAll(String personId, Parameters parameters)
	{
    	return preferences.getPreferences(personId, parameters.getPaging());
	}
    
	/**
	 * Returns information regarding the preference 'preferenceName' for user personId.
	 * 
	 * If personId does not exist, NotFoundException (status 404).
	 * 
	 * @see org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction.ReadById#readById(String, String, org.alfresco.rest.framework.resource.parameters.Parameters)
	 */
    @Override
    @WebApiDescription(title = "Preference value for preference 'preferenceName' for person 'personId'.")
    public Preference readById(String personId, String preferenceName, Parameters parameters)
	{
    	return preferences.getPreference(personId, preferenceName);
	}

}
