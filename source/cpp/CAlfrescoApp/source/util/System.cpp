/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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