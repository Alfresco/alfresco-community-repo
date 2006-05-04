/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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

#ifndef _JavaSystem_H
#define _JavaSystem_H

//	Includes

#include "util\Types.h"

//	Classes defined in this header file

namespace Alfresco {
	class System;
}

/**
 * Java-like System Class
 */
class Alfresco::System {
public:

	//	Get the current system time in milliseconds

	static DATETIME currentTimeMillis( void);

private:
	//	Hide constructors, static only class

	System( void) {};
	System ( const System& sys) {};
};

#endif