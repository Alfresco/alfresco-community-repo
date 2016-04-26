package org.alfresco.repo.copy;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.copy.traitextender.DefaultCopyBehaviourCallbackExtension;
import org.alfresco.repo.copy.traitextender.DefaultCopyBehaviourCallbackTrait;
import org.alfresco.service.namespace.QName;
import org.alfresco.traitextender.AJExtender;
import org.alfresco.traitextender.Extend;
import org.alfresco.traitextender.ExtendedTrait;
import org.alfresco.traitextender.Extensible;
import org.alfresco.traitextender.Trait;
import org.alfresco.util.Pair;


/**
 * The default behaviour that a type of aspect implements if there is no associated
 * <{@link CopyBehaviourCallback behaviour}.
 * <p>
 * This implementation is {@link #getInstance() stateless} and therefore thread-safe.
 * <p>
 * The default behaviour is:
 * <ul>
 *    <li><b>Must Copy:</b>         YES</li>
 *    <li><b>Must Cascade:</b>      YES, if cascade is on</li>
 *    <li><b>Properties to Copy:</b>ALL</li>
 * </ul>
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class DefaultCopyBehaviourCallback extends AbstractCopyBehaviourCallback implements Extensible
{
    private static CopyBehaviourCallback instance = new DefaultCopyBehaviourCallback();
    
    private final ExtendedTrait<DefaultCopyBehaviourCallbackTrait> defaultCopyBehaviourCallbackTrait;
    
    public DefaultCopyBehaviourCallback()
    {
        defaultCopyBehaviourCallbackTrait=new ExtendedTrait<DefaultCopyBehaviourCallbackTrait>(createTrait());
    }
    /**
     * @return          Returns a stateless singleton
     */
    public static CopyBehaviourCallback getInstance()
    {
        return instance;
    }
    
    /**
     * Default behaviour: Always copy
     * 
     * @return          Returns <tt>true</tt> always
     */
    @Extend(traitAPI=DefaultCopyBehaviourCallbackTrait.class,extensionAPI=DefaultCopyBehaviourCallbackExtension.class)
    public boolean getMustCopy(QName classQName, CopyDetails copyDetails)
    {
        return true;
    }

    /**
     * Default behaviour:<br/>
     * * AssocCopySourceAction.COPY_REMOVE_EXISTING<br/>
     * * AssocCopyTargetAction.USE_COPIED_OTHERWISE_ORIGINAL_TARGET
     */
    @Override
    public Pair<AssocCopySourceAction, AssocCopyTargetAction> getAssociationCopyAction(
            QName classQName,
            CopyDetails copyDetails,
            CopyAssociationDetails assocCopyDetails)
    {
        return new Pair<AssocCopySourceAction, AssocCopyTargetAction>(
                AssocCopySourceAction.COPY_REMOVE_EXISTING,
                AssocCopyTargetAction.USE_COPIED_OTHERWISE_ORIGINAL_TARGET);
    }

    /**
     * Default behaviour: Cascade if we are copying children <b>AND</b> the association is primary
     * 
     * @return          Returns <tt>true</tt> if the association is primary and <code>copyChildren == true</code>
     */
    public ChildAssocCopyAction getChildAssociationCopyAction(
            QName classQName,
            CopyDetails copyDetails,
            CopyChildAssociationDetails childAssocCopyDetails)
    {
        if (!childAssocCopyDetails.isCopyChildren())
        {
            return ChildAssocCopyAction.IGNORE;
        }
        if (childAssocCopyDetails.getChildAssocRef().isPrimary())
        {
            return ChildAssocCopyAction.COPY_CHILD;
        }
        else
        {
            return ChildAssocCopyAction.COPY_ASSOC;
        }
    }

    /**
     * Default behaviour: Copy all associated properties
     * 
     * @return          Returns all the properties passes in
     */
    public Map<QName, Serializable> getCopyProperties(
            QName classQName,
            CopyDetails copyDetails,
            Map<QName, Serializable> properties)
    {
        return properties;
    }

    private DefaultCopyBehaviourCallbackTrait createTrait()
    {
        return new DefaultCopyBehaviourCallbackTrait()
        {
            @Override
            public boolean getMustCopy(final QName classQName, final CopyDetails copyDetails)
            {
                return AJExtender.run(new AJExtender.ExtensionBypass<Boolean>()
                {
                    @Override
                    public Boolean run()
                    {
                        return getInstance().getMustCopy(classQName, copyDetails);
                    };
                });
                
            }
        };
    }

    @Override
    public <T extends Trait> ExtendedTrait<T> getTrait(Class<? extends T> traitAPI)
    {
        return (ExtendedTrait<T>) defaultCopyBehaviourCallbackTrait;
    }
}
