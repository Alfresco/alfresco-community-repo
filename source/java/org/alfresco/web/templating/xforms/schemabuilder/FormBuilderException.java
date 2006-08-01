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
package org.alfresco.web.templating.xforms.schemabuilder;


/**
 * This exception is thrown when implementations of <code>SchemaFormBuilder</code> encounters an
 * error building a form.
 *
 * @author Brian Dueck
 * @version $Id: FormBuilderException.java,v 1.4 2005/01/31 22:49:31 joernt Exp $
 */
public class FormBuilderException extends java.lang.Exception {
    private Exception cause = null;

    /**
     * Creates a new instance of <code>FormBuilderException</code> without detail message.
     */
    public FormBuilderException() {
    }

    /**
     * Constructs an instance of <code>FormBuilderException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public FormBuilderException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>FormBuilderException</code> with the specified root exception.
     *
     * @param x The root exception.
     */
    public FormBuilderException(Exception x) {
        //THIS DOES NOT WORK WITH JDK 1.3 CAUSE THIS IS NEW IN JDK 1.4
        //super(x);
        super(x.getMessage());
    }
}


/*
   $Log: FormBuilderException.java,v $
   Revision 1.4  2005/01/31 22:49:31  joernt
   added copyright notice

   Revision 1.3  2004/08/15 14:14:07  joernt
   preparing release...
   -reformatted sources to fix mixture of tabs and spaces
   -optimized imports on all files

   Revision 1.2  2003/10/02 15:15:49  joernt
   applied chiba jalopy settings to whole src tree

   Revision 1.1  2003/07/12 12:22:48  joernt
   package refactoring: moved from xforms.builder
   Revision 1.1.1.1  2003/05/23 14:54:08  unl
   no message
   Revision 1.2  2003/02/19 09:09:15  soframel
   print the exception's message
   Revision 1.1  2002/12/11 14:50:42  soframel
   transferred the Schema2XForms generator from chiba2 to chiba1
   Revision 1.3  2002/06/11 17:13:03  joernt
   commented out jdk 1.3 incompatible constructor-impl
   Revision 1.2  2002/06/11 14:06:31  joernt
   commented out the jdk 1.4 constructor
   Revision 1.1  2002/05/22 22:24:34  joernt
   Brian's initial version of schema2xforms builder
 */
