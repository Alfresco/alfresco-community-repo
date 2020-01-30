/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.repo.virtual.bundle;

import org.alfresco.repo.policy.traitextender.BehaviourFilterExtension;
import org.alfresco.repo.policy.traitextender.BehaviourFilterTrait;
import org.alfresco.repo.virtual.store.VirtualStore;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.traitextender.SpringBeanExtension;

/**
 * Extends the disabling/enabling service when it comes to virtual nodes.
 *
 * @author Oussama Messeguem
 */
public class VirtualBehaviourFilterExtension
        extends SpringBeanExtension<BehaviourFilterExtension, BehaviourFilterTrait>
        implements BehaviourFilterExtension
{

    private VirtualStore smartStore;

    public VirtualBehaviourFilterExtension()
    {
        super(BehaviourFilterTrait.class);
    }

    public void setSmartStore(VirtualStore smartStore)
    {
        this.smartStore = smartStore;
    }

    @Override
    public void disableBehaviour(NodeRef nodeRef, QName className)
    {
        getTrait().disableBehaviour(smartStore.materializeIfPossible(nodeRef), className);
    }

    @Override
    public void disableBehaviour(NodeRef nodeRef)
    {
        getTrait().disableBehaviour(smartStore.materializeIfPossible(nodeRef));
    }

    @Override
    public void enableBehaviour(NodeRef nodeRef, QName className)
    {
        getTrait().enableBehaviour(smartStore.materializeIfPossible(nodeRef), className);
    }

    @Override
    public void enableBehaviour(NodeRef nodeRef)
    {
        getTrait().enableBehaviour(smartStore.materializeIfPossible(nodeRef));
    }

    @Override
    public boolean isEnabled(NodeRef nodeRef, QName className)
    {
        return getTrait().isEnabled(smartStore.materializeIfPossible(nodeRef), className);
    }

    @Override
    public boolean isEnabled(NodeRef nodeRef)
    {
        return getTrait().isEnabled(smartStore.materializeIfPossible(nodeRef));
    }

    @Override
    public void disableBehaviour()
    {
        getTrait().disableBehaviour();
    }

    @Override
    public void disableBehaviour(QName className)
    {
        getTrait().disableBehaviour(className);
    }

    @Override
    public void disableBehaviour(QName className, boolean includeSubClasses)
    {
        getTrait().disableBehaviour(className, includeSubClasses);
    }

    @Override
    public void enableBehaviour()
    {
        getTrait().enableBehaviour();
    }

    @Override
    public void enableBehaviour(QName className)
    {
        getTrait().enableBehaviour(className);
    }

    @Override
    public boolean isEnabled()
    {
        return getTrait().isEnabled();
    }

    @Override
    public boolean isEnabled(QName className)
    {
        return getTrait().isEnabled(className);
    }

    @Override
    public boolean isActivated()
    {
        return getTrait().isActivated();
    }

    @Deprecated
    @Override
    public void enableBehaviours(NodeRef nodeRef)
    {
        getTrait().enableBehaviours(smartStore.materializeIfPossible(nodeRef));
    }

    @Deprecated
    @Override
    public void disableAllBehaviours()
    {
        getTrait().disableAllBehaviours();
    }

    @Deprecated
    @Override
    public void enableAllBehaviours()
    {
        getTrait().enableAllBehaviours();
    }

}
