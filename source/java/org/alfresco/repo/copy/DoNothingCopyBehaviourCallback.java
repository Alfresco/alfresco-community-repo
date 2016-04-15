package org.alfresco.repo.copy;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * Simple <i>copy behaviour</i> to prevent any copying.
 * <p>
 * This implementation is {@link #getInstance() stateless} and therefore thread-safe.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class DoNothingCopyBehaviourCallback extends AbstractCopyBehaviourCallback
{
    private static CopyBehaviourCallback instance = new DoNothingCopyBehaviourCallback();
    
    /**
     * @return          Returns a stateless singleton
     */
    public static CopyBehaviourCallback getInstance()
    {
        return instance;
    }
    
    /**
     * @return          Returns <tt>false</tt> always
     */
    public boolean getMustCopy(QName classQName, CopyDetails copyDetails)
    {
        return false;
    }

    /**
     * @return          Returns <tt>false</tt> always
     */
    public ChildAssocCopyAction getChildAssociationCopyAction(
            QName classQName,
            CopyDetails copyDetails,
            CopyChildAssociationDetails childAssocCopyDetails)
    {
        return ChildAssocCopyAction.IGNORE;
    }

    /**
     * @return          Returns an empty map always
     */
    public Map<QName, Serializable> getCopyProperties(
            QName classQName,
            CopyDetails copyDetails,
            Map<QName, Serializable> properties)
    {
        return Collections.emptyMap();
    }

    @Override
    public Pair<AssocCopySourceAction, AssocCopyTargetAction> getAssociationCopyAction(QName classQName,
            CopyDetails copyDetails, CopyAssociationDetails assocCopyDetails)
    {
        return new Pair<AssocCopySourceAction, AssocCopyTargetAction>(AssocCopySourceAction.IGNORE, 
                AssocCopyTargetAction.USE_COPIED_OTHERWISE_ORIGINAL_TARGET);
    }
    
    
}
