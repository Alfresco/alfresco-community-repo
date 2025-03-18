/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.policy;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Contract disabling and enabling policy behaviours.
 * <p/>
 * Since 4.0, the behaviour enabling/disabling is recorded using reference counting, meaning that the outermost disable call in a stack has an effective veto. Use proper try-finally patterns to ensure behaviour is released after it is no longer needed.
 * 
 * <pre>
 * <code>
 * behaviourFilter.disableBehaviour(abc);
 * try
 * {
 *    behaviourFilter.disableBehaviour(abc);
 *    try
 *    {
 *       // Do something that might have triggered 'abc' but will not
 *    }
 *    finally
 *    {
 *        behaviourFilter.enableBehaviour(abc);
 *    }
 *    // Do something that might have triggered 'abc' but will not despite the last enable call
 * }
 * finally
 * {
 *     behaviourFilter.enableBehaviour(abc);
 * }
 * </code>
 * </pre>
 * 
 * @see org.alfresco.repo.policy.PolicyComponent
 * 
 * @author Derek Hulley
 */
@AlfrescoPublicApi
public interface BehaviourFilter
{
    /**
     * @deprecated Since 4.0 use {@link #enableBehaviour(NodeRef)}
     */
    public void enableBehaviours(NodeRef nodeRef);

    /**
     * @deprecated Since 4.0 use {@link #enableBehaviour(NodeRef)}
     */
    public void disableAllBehaviours();

    /**
     * @deprecated Since 4.0 use {@link #disableBehaviour()}
     */
    public void enableAllBehaviours();

    /**
     * Disable behaviour for all types
     * <p>
     * The change applies <b>ONLY</b> to the current transaction.
     */
    public void disableBehaviour();

    /**
     * Disable behaviour for a type or aspect for all nodes. <br>
     * Given a direct instance of className (ie. not a subclass) all behaviour is disabled (including superclass behaviour). </br>
     * <br>
     * The same as calling {@link #disableBehaviour(QName, boolean)} with <code>false</code></br>
     * <p>
     * The change applies <b>ONLY</b> to the current transaction.
     *
     * @param className
     *            the type/aspect behaviour to disable
     */
    public void disableBehaviour(QName className);

    /**
     * Disable behaviour for a type or aspect for all nodes. <br>
     * Given an instance of className (including instances that are subclasses of className, if includeSubClasses is true) all behaviour is disabled (including superclass behaviour). </br>
     * <br>
     * Successive calls (within the current transaction) will overwrite the filter for this class.</br>
     * The change applies <b>ONLY</b> to the current transaction.
     * 
     * @param className
     *            the type/aspect behaviour to disable
     * @param includeSubClasses
     *            set to <code>true</code> to disable the behaviours of subclasses
     * 
     * @since 5.1
     */
    public void disableBehaviour(QName className, boolean includeSubClasses);

    /**
     * Disable behaviour for specific node and class
     * <p>
     * The change applies <b>ONLY</b> to the current transaction.
     * 
     * @param nodeRef
     *            the node to disable for
     * @param className
     *            the type/aspect behaviour to disable
     */
    public void disableBehaviour(NodeRef nodeRef, QName className);

    /**
     * Disable all behaviours for a given node
     * 
     * @param nodeRef
     *            the node to disable for
     */
    public void disableBehaviour(NodeRef nodeRef);

    /**
     * Enable behaviours for all classes.
     * <p>
     * The change applies <b>ONLY</b> to the current transaction.
     */
    public void enableBehaviour();

    /**
     * Enable behaviour for all nodes <br>
     * This is also applied to the to the disabled behaviours with {@link #disableBehaviour(QName, boolean)}</br>
     * <p>
     * The change applies <b>ONLY</b> to the current transaction.
     * 
     * @param className
     *            the type/aspect behaviour to enable
     */
    public void enableBehaviour(QName className);

    /**
     * Enable behaviour for specific node
     * <p>
     * The change applies <b>ONLY</b> to the current transaction.
     * 
     * @param nodeRef
     *            the node to enable for
     * @param className
     *            the type/aspect behaviour to enable or <tt>null</tt> for all classes
     */
    public void enableBehaviour(NodeRef nodeRef, QName className);

    /**
     * Enable behaviour for a specific node
     * <p>
     * The change applies <b>ONLY</b> to the current transaction.
     * 
     * @param nodeRef
     *            the node to enable for
     * 
     * @since 4.0
     */
    public void enableBehaviour(NodeRef nodeRef);

    /**
     * Determine if behaviour is globally enabled.
     * <p>
     * The change applies <b>ONLY</b> to the current transaction.
     * 
     * @return true => behaviour is enabled
     * 
     * @since 4.0
     */
    public boolean isEnabled();

    /**
     * Determine if behaviour is enabled for a class.
     * <p>
     * The change applies <b>ONLY</b> to the current transaction.
     * 
     * @param className
     *            the behaviour to test for
     * @return true => behaviour is enabled
     */
    public boolean isEnabled(QName className);

    /**
     * Determine if behaviour is enabled for specific node and class.
     * <p>
     * Note: A node behaviour is enabled only when: a) the behaviour is not disabled across all nodes b) the behaviour is not disabled specifically for the provided node
     * <p>
     * The change applies <b>ONLY</b> to the current transaction.
     * 
     * @param nodeRef
     *            the node to test for
     * @param className
     *            the behaviour to test for
     * @return true => behaviour is enabled
     */
    public boolean isEnabled(NodeRef nodeRef, QName className);

    /**
     * Determine if behaviour is enabled for a specific node.
     * <p>
     * The change applies <b>ONLY</b> to the current transaction.
     * 
     * @param nodeRef
     *            the node to test for
     * @return true => behaviour is enabled
     */
    public boolean isEnabled(NodeRef nodeRef);

    /**
     * Determine if any behaviours have been disabled or altered.
     * <p>
     * The change applies <b>ONLY</b> to the current transaction.
     * 
     * @return true => behaviours have been altered
     */
    public boolean isActivated();
}
