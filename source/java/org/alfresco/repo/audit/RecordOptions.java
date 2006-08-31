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
package org.alfresco.repo.audit;

import org.alfresco.repo.audit.model.TrueFalseUnset;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.dom4j.Element;

public interface RecordOptions
{
    public TrueFalseUnset getRecordFilters();

    public TrueFalseUnset getRecordPath();

    public TrueFalseUnset getRecordSerializedExceptions();

    public TrueFalseUnset getRecordSerializedKeyPropertiesAfterEvaluation();

    public TrueFalseUnset getRecordSerializedKeyPropertiesBeforeEvaluation();

    public TrueFalseUnset getRecordSerializedMethodArguments();

    public TrueFalseUnset getRecordSerializedReturnValue();
}
