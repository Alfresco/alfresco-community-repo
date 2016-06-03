
package org.alfresco.service.cmr.site;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Site member's information. The member can either be an individual or a group.
 * 
 * @author Jamal Kaabi-Mofrad
 * @since odin
 */
@AlfrescoPublicApi
public interface SiteMemberInfo
{

    /**
     * Get the member's name. The name can either be the name of an individual
     * or a group
     * 
     * @return String member's name
     */
    public String getMemberName();


    /**
     * Get the member's role
     * 
     * @return String member's role
     */
    public String getMemberRole();


    /**
     * Indicates whether a member belongs to a group with access rights to the
     * site or not
     * 
     * @return <tt>true</tt> if the member belongs to a group with access
     *         rights, otherwise <tt>false</tt>
     */
    public boolean isMemberOfGroup();

}
