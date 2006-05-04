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

#include "util\System.h"

#include <sys\timeb.h>

using namespace Alfresco;

/**
 * Return the current system time in milliseconds since Jan 1 1970
 * 
 * @return DATETIME
 */
DATETIME System::currentTimeMillis( void) {

	//	Get the current system time

	struct __timeb64 timeNow;
	_ftime64( &timeNow);

	//	Build the milliseconds time

	DATETIME timeNowMillis = ( timeNow.time * 1000L) + (DATETIME) timeNow.millitm;
	return timeNowMillis;
}