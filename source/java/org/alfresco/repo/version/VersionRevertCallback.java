/*
 * Copyright (C) 2013-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
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
	 * @param aspectName,  the name of the aspect to revert
	 * @param details, details of the aspect to revert
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
	 * @param assocName,  the name of the assoc to revert
	 * @param details, details of the node to revert
	 * 
	 */
	public RevertAssocAction getRevertAssocAction(QName assocName, VersionRevertDetails details);

	

    

}
