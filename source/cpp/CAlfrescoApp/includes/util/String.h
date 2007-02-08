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

#ifndef _JavaString_H
#define _JavaString_H

//	Includes

#include <string>
#include <vector>
#include <iostream>

#include "util\ByteArray.h"
#include "util\Types.h"

//	Classes defined in this header file

namespace Alfresco {
	class String;
	class StringList;
}

/**
 * Java-like String Class
 */
class Alfresco::String {
public:
	//	Constructors

	String();
	String(const unsigned int alloc);
	String(const char* str);
	String(const unsigned char* str);
	String(const char* buf, const unsigned int offset, const unsigned int len);
	String(const wchar_t* str);
	String(const wchar_t* buf, const unsigned int offset, const unsigned int len);
	String(const String& str);
	String(const std::wstring& str);
	String(ByteArray& byts);

	//	Return the string length

	inline unsigned int length( void) const { return ( unsigned int) m_string.length(); }

	//	Check if a string is empty

	inline bool isNull( void) const { return m_string.length() > 0 ? false : true; }
	inline bool isNotEmpty( void) const { return m_string.length() > 0 ? true : false; }
	
	//	Compare strings for equality

	bool equals(const wchar_t* str) const;
	bool equals(const String& str) const;

	bool equalsIgnoreCase(const wchar_t* str) const;
	bool equalsIgnoreCase(const String& str) const;

	//	Compare strings

	int compareTo( const String& str) const;
	int compareTo( const wchar_t* pStr) const;

	//	Convert to lowercase/uppercase returning the new string

	String toLowerCase() const;
	String toUpperCase() const;

	//	Search for the occurrence of a character or string

	int indexOf(const wchar_t ch, int startIndex = 0) const;
	int indexOf(const wchar_t* str, int startIndex = 0) const;
	int indexOf(const String& str, int startIndex = 0) const;

	//	Search for the occurrence of a character or string

	int lastIndexOf(const wchar_t ch, int startIndex = -1) const;
	int lastIndexOf(const wchar_t* str, int startIndex = -1) const;
	int lastIndexOf(const String& str, int startIndex = -1) const;

	//	Check if the string starts with the specified string

	bool startsWith(const wchar_t* str) const;
	bool startsWith(const String& str) const;

	bool startsWithIgnoreCase(const wchar_t* str) const;
	bool startsWithIgnoreCase(const String& str) const;

	//	Check if the string ends with the specified string

	bool endsWith(const wchar_t* str) const;
	bool endsWith(const String& str) const;

	//	Replace all occurrences of the specified character in the string

	void replace( wchar_t oldCh, wchar_t newCh);

	//	Append character, string, integer values to the string

	void append( wchar_t ch);
	void append( const char* str);
	void append( const wchar_t* str);
	void append( const String& str);
	void append( const unsigned int ival);
	void append( const unsigned long lval);
	void append( const LONG64 l64val);

	//	Get the character at the specified position in the string

	inline wchar_t charAt(const unsigned int idx) const { return m_string[idx]; }

	//	Set the string length

	inline void setLength( unsigned int len) { m_string.resize( len, 0);	}

	//	Trim leading and trailing whitespace from the string

	String trim( void) const;

	//	Return the substring of this string

	String substring( unsigned int beginIndex) const;
	String substring( unsigned int beginIndex, unsigned int endIndex) const;

	//	Set the allocated capacity for the string by allocating or shrinking the current string buffer

	inline void reserve( const unsigned int capacity = 0) { m_string.reserve( capacity); }

	//	Assignment operator

	String& operator=(const wchar_t* str);
	String& operator=(const String& str);

	//	Append operator

	inline String& operator+=(wchar_t ch) { append( ch); return *this;	}
	inline String& operator+=(const char* str) { append( str); return *this;	}
	inline String& operator+=(const wchar_t* str) { append( str); return *this;	}
	inline String& operator+=(const String& str) { append( str); return *this;	}
	inline String& operator+=(const unsigned int ival) { append( ival); return *this;	}
	inline String& operator+=(const unsigned long lval) { append( lval); return *this;	}
	inline String& operator+=(const LONG64 l64val) { append( l64val); return *this; }

	//	Equality operator

	bool operator== ( const String& str) const;
	bool operator== ( const wchar_t* str) const;
	bool operator== ( const char* str) const;

	//	Less than operator

	bool operator< ( const String& str) const;

	//	Return the string data

	inline const wchar_t* data() const { return m_string.data(); }

	//	Conversion operator

	inline operator const wchar_t* ( void) const { return m_string.data(); }

	//	Return the string as an array of bytes

	ByteArray getBytes( ByteArray& byts) const;
	ByteArray getBytes( void) const;

	//	Split the string into tokens using the specified delimiters

	StringList tokenize( const String& delims) const;

	//	Streaming operators

	friend std::wostream& operator<<(std::wostream& out, const String& str);
	friend std::ostream& operator<<(std::ostream& out, const String& str);

	//	Access the internal string object

	inline std::wstring getString( void) { return m_string; }
	inline const std::wstring getString( void) const { return m_string; }

private:
	//	String data

	std::wstring m_string;
};

/**
 * String List Class
 */
class Alfresco::StringList {
public:
	//	Class constructor

	StringList( void);
	StringList( unsigned int reserve);
	StringList( const StringList& strList);

	//	Add a string to the list

	inline void addString( const String& str) { m_list.push_back( str); }

	//	Check if the list contains the specified string

	bool containsString( const String& str);
	bool containsStringCaseless ( const String& str);

	//	Return the index of the specified string, or -1 if not found

	int indexOf( const String& str) const;

	//	Remove a string from the list

	void removeString( const String& str);
	void removeStringCaseless( const String& str);

	//	Return the number of strings in the list

	inline size_t numberOfStrings( void) const { return m_list.size(); }

	//	Return the specified string

	inline const String& getStringAt( unsigned int idx) const { return m_list[idx]; }

	//	Assignment operator

	inline String& operator[] ( const unsigned int idx) { return m_list[idx]; }

	//	Remove all strings from the list

	inline void removeAllStrings( void) { m_list.clear(); }

	//	Copy the string list

	void copyFrom( const StringList& strList);

	//	Return the string list as a comma separated list

	String toString( void) const;

private:
	//	Instance variables
	//
	//	List of strings

	std::vector<String>	m_list;
};

#endif
