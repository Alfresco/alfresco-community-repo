package org.alfresco.repo.version;

import org.alfresco.repo.copy.CopyBehaviourCallback.AssocCopySourceAction;
import org.alfresco.repo.copy.CopyBehaviourCallback.CopyAssociationDetails;
import org.alfresco.service.namespace.QName;

/**
 * A callback to modify version revert behaviour associated with a given type or aspect.  This
 * callback is called per type and per aspect.
 * 
 * @since 4.2
 * @author mrogers
 *
 */
public interface VersionRevertCallback 
{
	/**
	 * 
	 */
    public enum RevertAspectAction implements Comparable<RevertAspectAction>
    {
    	/**
    	 * Revert this aspect, if it does not exist on the target version then it will be removed.
    	 */
    	REVERT,
    	
        /**
         * Ignore the aspect, do not remove it or its properties.
         */
        IGNORE,
    }
    
	/**
	 * How should the specified aspect be reverted?
	 * 
	 * @param aspectName  the name of the aspect to revert
	 * @param details details of the aspect to revert
	 * 
	 */
	public RevertAspectAction getRevertAspectAction(QName aspectName, VersionRevertDetails details);
	
	/**
	 * 
	 */
    public enum RevertAssocAction implements Comparable<RevertAssocAction>
    {
    	/**
    	 * Revert this assoc, if it does not exist on the target version then it will be removed.
    	 */
    	REVERT,
    	
        /**
         * Ignore the assoc, do not remove it or add it.
         */
        IGNORE,
    }
	
	/**
	 * How should the specified assoc be reverted?
	 * 
	 * @param assocName  the name of the assoc to revert
	 * @param details details of the node to revert
	 * 
	 */
	public RevertAssocAction getRevertAssocAction(QName assocName, VersionRevertDetails details);

	

    

}
