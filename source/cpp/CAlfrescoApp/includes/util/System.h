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