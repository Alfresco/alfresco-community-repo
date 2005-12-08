/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.service.namespace;


/**
 * Provides pattern matching against {@link org.alfresco.service.namespace.QName qnames}.
 * <p>
 * Implementations will use different mechanisms to match against the
 * {@link org.alfresco.service.namespace.QName#getNamespaceURI() namespace} and
 * {@link org.alfresco.service.namespace.QName#getLocalName()() localname}.
 * 
 * @see org.alfresco.service.namespace.QName
 * 
 * @author Derek Hulley
 */
public interface QNamePattern
{
    /**
     * Checks if the given qualified name matches the pattern represented
     * by this instance
     * 
     * @param qname the instance to check
     * @return Returns true if the qname matches this pattern
     */
    public boolean isMatch(QName qname);
}
