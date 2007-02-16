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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

#include "util\String.h"

using namespace Alfresco;
using namespace std;

/**
 * Default constructor
 */
String::String() {
	m_string = std::wstring();
}

/**
 * Class constructor
 * 
 * @param alloc const unsigned int
 */
String::String(const unsigned int alloc) {
	m_string = std::wstring();
	m_string.reserve( alloc);
}

/**
 * Class constructor
 *
 * @param str const char*
 */
String::String(const char* str) {

	//	Expand the characters to wide characters and append to the string

	wchar_t wch;

	while ( *str != '\0') {
		wch = (wchar_t) *str++;
		m_string += wch;
	}
}

/**
* Class constructor
*
* @param str const unsigned char*
*/
String::String(const unsigned char* str) {

	//	Expand the characters to wide characters and append to the string

	wchar_t wch;

	while ( *str != '\0') {
		wch = (wchar_t) *str++;
		m_string += wch;
	}
}

/**
 * Class constructor
 * 
 * @param buf const char*
 * @param offset const unsigned int
 * @param len const unsigned int
 */
String::String(const char* buf, const unsigned int offset, const unsigned int len) {

	//	Expand the characters to wide characters and append to the string

	wchar_t wch;

	const char* str = buf + offset;
	unsigned int sLen = len;

	while ( sLen--) {
		wch = (wchar_t) *str++;
		m_string += wch;
	}
}

/**
 * Class constructor
 *
 * @param str  const wchar_t*
 */
String::String(const wchar_t* str) {
	m_string = std::wstring( str);
}

/**
 * Class constructor
 * 
 * @param buf const wchar_t*
 * @param offset const unsigned int
 * @param len const unsigned int
 */
String::String(const wchar_t* buf, const unsigned int offset, const unsigned int len) {
	m_string = std::wstring(buf + offset, len);
}

/**
 * Class constructor
 *
 * @param str const std::wstring&
 */
String::String(const std::wstring& str) {
	m_string = str;
}

/**
 * Copy constructor
 *
 * @param str const String&
 */
String::String(const String& str) {
	m_string = std::wstring(str.data());
}

/**
* Class constructor
*
* @param byts ByteArray&
*/
String::String( ByteArray& byts) {

	//	Expand the characters to wide characters and append to the string

	wchar_t wch;

	for ( unsigned int idx = 0; idx < byts.getLength(); idx++) {
		wch = (wchar_t) byts[idx];
		m_string += wch;
	}
}

/**
 * Compare strings for equality
 *
 * @param str const wchar_t*
 * @return bool
 */
bool String::equals(const wchar_t* str) const {

	//	Check that the string is valid

	if ( str == NULL)
		return false;

	//	Compare the strings

	if ( m_string.compare(str) == 0)
		return true;
	return false;
}

/**
 * Compare strings for equality
 *
 * @param str const String&
 * @return bool
 */
bool String::equals(const String& str) const {

	//	Compare the strings

	if ( m_string.compare(str.data()) == 0)
		return true;
	return false;
}

/**
 * Compare strings for equality ignoring case
 *
 * @param str const wchar_t*
 * @return  bool
 */
bool String::equalsIgnoreCase(const wchar_t* str) const {
	return _wcsicmp( str, data()) == 0 ? true : false;
}

/**
 * Compare strings for equality ignoring case
 *
 * @param str const String&
 * @return bool
 */
bool String::equalsIgnoreCase(const String& str) const {
	return _wcsicmp( str.data(), data()) == 0 ? true : false;
}

/**
 * Compare strings
 * 
 * @param str const String&
 * @return int
 */
int String::compareTo( const String& str) const {
	return m_string.compare( str.getString());
}

/**
 * Compare strings
 * 
 * @param pStr const wchar_t*
 * @return int
 */
int String::compareTo( const wchar_t* pStr) const {
	return m_string.compare( pStr);
}

/**
 * Convert the string to lower case returning the resulting String
 *
 * @return String
 */
String String::toLowerCase() const {

	//	Create a copy of the string then convert to lowercase

	std::wstring lstr(m_string);

	for ( unsigned int i = 0; i < lstr.length(); i++)
		lstr[i] = tolower(lstr[i]);

	return String(lstr);
}

/**
 * Convert the string to upper case returning the resulting String
 *
 * @return String
 */
String String::toUpperCase() const {

	//	Create a copy of the string then convert to uppercase

	std::wstring ustr(m_string);

	for ( unsigned int i = 0; i < ustr.length(); i++)
		ustr[i] = toupper(ustr[i]);

	return String(ustr);
}

/**
 * Return the index of the specified character, or -1 if not found
 *
 * @param ch const wchar_t
 * @param startIndex int
 * @return int
 */
int String::indexOf(const wchar_t ch, int startIndex) const {
	return (int) m_string.find_first_of( ch, startIndex);
}

/**
 * Return the index of the specified string, or -1 if not found
 *
 * @param str const wchar_t*
 * @param startIndex int
 * @return int
 */
int String::indexOf(const wchar_t* str, int startIndex) const {
	return (int) m_string.find_first_of( str, startIndex);
}

/**
 * Return the index of the specified string, or -1 if not found
 *
 * @param str const String&
 * @param startIndex int
 * @return int
 */
int String::indexOf(const String& str, int startIndex) const {
	return (int) m_string.find_first_of( str, startIndex);
}

/**
 * Return the last index of the specified character, or -1 if not found
 *
 * @param ch const wchar_t
 * @param startIndex int
 * @return int
 */
int String::lastIndexOf(const wchar_t ch, int startIndex) const {
	return (int) m_string.find_last_of( ch, startIndex);
}

/**
 * Return the last index of the specified string, or -1 if not found
 *
 * @param str const wchar_t*
 * @param startIndex int
 * @return int
 */
int String::lastIndexOf(const wchar_t* str, int startIndex) const {
	return (int) m_string.find_last_of( str, startIndex);
}

/**
 * Return the last index of the specified string, or -1 if not found
 *
 * @param str const String&
 * @param startIndex int
 * @return int
 */
int String::lastIndexOf(const String& str, int startIndex) const {
	return (int) m_string.find_last_of( str, startIndex);
}

/**
 * Check if this string starts with the specified string.
 *
 * @param str const wchar_t*
 * @return bool
 */
bool String::startsWith(const wchar_t* str) const {

	//	Check if the string to check is valid

	if ( str == NULL)
		return false;

	//	Get the string length, if the comparison string is longer than this string
	//	then there is no match.

	size_t len = wcslen(str);
	if ( str == NULL || wcslen(str) > m_string.length())
		return false;

	//	Check if this string starts with the specified string

	if ( m_string.compare(0, len, str) == 0)
		return true;
	return false;
}

/**
 * Check if this string starts with the specified string.
 *
 * @param str const String&
 * @return bool
 */
bool String::startsWith(const String& str) const {

	//	Get the string length, if the comparison string is longer than this string
	//	then there is no match.

	if ( str.length() > m_string.length())
		return false;

	//	Check if this string starts with the specified string

	if ( m_string.compare(0, str.length(), str.data()) == 0)
		return true;
	return false;
}

/**
 * Check if this string starts with the specified string, ignoring case.
 *
 * @param str const wchar_t*
 * @return bool
 */
bool String::startsWithIgnoreCase(const wchar_t* str) const {

	//	Check if the string to check is valid

	if ( str == NULL)
		return false;

	//	Get the string length, if the comparison string is longer than this string
	//	then there is no match.

	size_t len = wcslen(str);
	if ( str == NULL || wcslen(str) > m_string.length())
		return false;

	//	Check if this string starts with the specified string

	if ( _wcsnicmp(str, data(), len) == 0)
		return true;
	return false;
}

/**
 * Check if this string starts with the specified string, ignoring case.
 *
 * @param str const String&
 * @return bool
 */
bool String::startsWithIgnoreCase(const String& str) const {

	//	Get the string length, if the comparison string is longer than this string
	//	then there is no match.

	if ( str.length() > m_string.length())
		return false;

	//	Check if this string starts with the specified string

	if ( _wcsnicmp( str.data(), data(), str.length()) == 0)
		return true;
	return false;
}

/**
 * Check if this string ends with the specified string.
 *
 * @param str const wchar_t*
 * @return bool
 */
bool String::endsWith(const wchar_t* str) const {

	//	Check if the string to check is valid

	if ( str == NULL)
		return false;

	//	Get the string length, if the comparison string is longer than this string
	//	then there is no match.

	size_t len = wcslen(str);
	if ( str == NULL || wcslen(str) > m_string.length())
		return false;

	//	Check if this string ends with the specified string

	if ( m_string.compare(m_string.length() - len, len, str) == 0)
		return true;
	return false;
}

/**
 * Check if this string ends with the specified string.
 *
 * @param str const String&
 * @return bool
 */
bool String::endsWith(const String& str) const {

	//	Get the string length, if the comparison string is longer than this string
	//	then there is no match.

	if ( str.length() > m_string.length())
		return false;

	//	Check if this string ends with the specified string

	if ( m_string.compare(m_string.length() - str.length(), str.length(), str.data()) == 0)
		return true;
	return false;
}

/**
 * Trim leading and trailing whitespace from the string returning the resulting String
 *
 * @return String
 */
String String::trim( void) const {
	std::wstring str = m_string;
	str.erase( str.find_last_not_of( L" ") + 1);

	return String( str);
}

/**
 * Return a substring of this string
 * 
 * @param beginIndex unsigned int
 * @return String
 */
String String::substring( unsigned int beginIndex) const {
	std::wstring str = m_string.substr( beginIndex);
	return String(str);
}

/**
 * Return a substring of this string
 * 
 * @param beginIndex unsigned int
 * @param endIndex unsigned int
 * @return String
 */
String String::substring( unsigned int beginIndex, unsigned int endIndex) const {
	std::wstring str = m_string.substr( beginIndex, (endIndex - beginIndex));
	return String(str);
}

/**
 * Assignment operator
 *
 * @param str const wchar_t*
 * @return String&
 */
String& String::operator=(const wchar_t* str) {
	m_string = str;
	return *this;
}

/**
 * Assignment operator
 *
 * @param str const String&
 * @return String&
 */
String& String::operator=(const String& str) {
	m_string = str.data();
	return *this;
}

/**
 * Return the string as an array of 8 bit bytes.
 *
 * @param byts ByteArray&
 * @return ByteArray
 */
ByteArray String::getBytes( ByteArray& byts) const {

	//	Create a byte array to hold the byte data

	byts.setLength( length());

	//	Convert the wide characters to ASCII characters

	for ( unsigned int i = 0; i < length(); i++)
		byts[ i] = (char) (charAt(i) & 0xFF);
	return byts;
}

/**
* Return the string as an array of 8 bit bytes.
*
* @return ByteArray
*/
ByteArray String::getBytes( void) const {

	//	Create a byte array to hold the byte data

	ByteArray byts;
	byts.setLength( length());

	//	Convert the wide characters to ASCII characters

	for ( unsigned int i = 0; i < length(); i++)
		byts[ i] = (char) (charAt(i) & 0xFF);
	return byts;
}

/**
 * Equality operator
 * 
 * @param str const String&
 * @return bool
 */
bool String::operator== ( const String& str) const {
	return equals( str);
}

/**
 * Equality operator
 * 
 * @param str const wchar_t*
 * @return bool
 */
bool String::operator== ( const wchar_t* str) const {
	return equals( str);
}

/**
 * Equality operator
 * 
 * @param str const char*
 * @return bool
 */
bool String::operator== ( const char* str) const {
	return equals( String( str));
}

/**
 * Wide character output stream operator
 * 
 * @param out wostream&
 * @param str const String&
 * @return wostream&
 */
std::wostream& Alfresco::operator<< ( std::wostream& out, const Alfresco::String& str) {
	return out << str.data();
}

/**
 * Less than operator
 *
 * @param str const String&
 * @return bool
 */
bool String::operator<( const String& str) const {
	return getString().compare( str.getString()) < 0 ? true : false;
}

/**
 * ASCII character output stream operator
 * 
 * @param out ostream&
 * @param str const String&
 * @return ostream&
 */
std::ostream& Alfresco::operator<< ( std::ostream& out, const Alfresco::String& str) {
	std::string ascStr;
	ascStr.reserve( str.length());

	for ( unsigned int i = 0; i < str.length(); i++)
		ascStr += (char) ( str.charAt( i) & 0xFF);
	return out << ascStr.c_str();
}

/**
* Replace occurrences of the character oldCh with newCh
* 
* @param oldCh wchar_t
* @param newCh wchar_t
*/
void String::replace( wchar_t oldCh, wchar_t newCh) {
	if ( m_string.size() == 0)
		return;
	for ( unsigned int i = 0; i < m_string.size(); i++) {
		if ( m_string.at( i) == oldCh)
			m_string[i] = newCh;
	}
}

/**
 * Append a character to this string
 * 
 * @param ch wchar_t
 */
void String::append( wchar_t ch) {
	m_string += ch;
}

/**
 * Append a string to this string
 * 
 * @param str const char*
 */
void String::append ( const char* str) {

	//	Expand the characters to wide characters and append to the string

	wchar_t wch;

	while ( *str != '\0') {
		wch = (wchar_t) *str++;
		m_string += wch;
	}
}

/**
 * Append a string to this string
 * 
 * @param str const wchar_t*
 */
void String::append (const wchar_t* str) {
	while ( *str != 0)
		m_string += *str++;
}

/**
 * Append a string to this string
 * 
 * @param str const String&
 */
void String::append (const String& str) {
	m_string += str.getString();
}

/**
 * Append an integer value to this string
 * 
 * @param ival const unsigned int
 */
void String::append (const unsigned int ival) {
	wchar_t buf[32];
	swprintf( buf, 32, L"%u", ival);

	m_string += buf;
}

/**
* Append a long value to this string
* 
* @param lval const unsigned long
*/
void String::append (const unsigned long lval) {
	wchar_t buf[32];
	swprintf( buf, 32, L"%lu", lval);

	m_string += buf;
}

/**
* Append a long/64 bit value to this string
* 
* @param l64val const unsigned long
*/
void String::append (const LONG64 l64val) {
	wchar_t buf[32];
	swprintf( buf, 32, L"%I64u", l64val);

	m_string += buf;
}

/**
 * Split a string into tokens
 * 
 * @param delims const String&
 * @return StringList
 */
StringList String::tokenize( const String& delims) const {

	//	Skip leading delimiters

	StringList tokens;
	string::size_type lastPos = m_string.find_first_not_of( delims, 0);

	//	Find a non-delimiter character

	string::size_type pos = m_string.find_first_of( delims, lastPos);

	while ( pos != string::npos || lastPos != string::npos) {

		//	Add the current token to the list

		tokens.addString( m_string.substr( lastPos, pos - lastPos));

		//	Skip delimiter(s)

		lastPos = m_string.find_first_not_of( delims, pos);

		//	Find next token

		pos = m_string.find_first_of( delims, lastPos);
	}

	//	Return the token list

	return tokens;
}

/**
 * Default constructor
 */
StringList::StringList( void) {
}

/**
 * Class constructor
 * 
 * @param reserve unsigned int
 */
StringList::StringList( unsigned int reserve) {
	m_list.reserve( reserve);
}

/**
 * Copy constructor
 * 
 * @param strList const StringList&
 */
StringList::StringList( const StringList& strList) {
	copyFrom( strList);
}

/**
 * Copy strings from the specified list
 * 
 * @param strList const StringList&
 */
void StringList::copyFrom( const StringList& strList) {
	for ( unsigned int idx = 0; idx < strList.numberOfStrings(); idx++)
		addString( strList.getStringAt( idx));
}

/**
 * Check if the list contains the string
 * 
 * @param str const String&
 * @return bool
 */
bool StringList::containsString ( const String& str) {
	for ( std::vector<String>::iterator pos = m_list.begin(); pos < m_list.end(); pos++) {
		if ( str.equals( *pos))
			return true;
	}
	return false;
}

/**
 * Check if the list contains the string, ignoring case
 * 
 * @param str const String&
 * @return bool
 */
bool StringList::containsStringCaseless ( const String& str) {
	for ( std::vector<String>::iterator pos = m_list.begin(); pos < m_list.end(); pos++) {
		if ( str.equalsIgnoreCase( *pos))
			return true;
	}
	return false;
}

/**
 * Find the specified string and return the position within the list, or -1 if not found
 * 
 * @param str const String&
 * @return int
 */
int StringList::indexOf( const String& str) const {
	for ( unsigned int i = 0; i < m_list.size(); i++) {
		if ( m_list[i].equals( str))
			return (int) i;
	}
	return -1;
}

/**
 * Remove the specified string from the list
 * 
 * @param str const String&
 */
void StringList::removeString ( const String& str) {
	for ( std::vector<String>::iterator pos = m_list.begin(); pos < m_list.end(); pos++) {
		if ( str.equals( *pos)) {
			m_list.erase( pos);
			return;
		}
	}
}

/**
 * Remove the specified string from the list, ignoring case
 * 
 * @param str const String&
 */
void StringList::removeStringCaseless ( const String& str) {
	for ( std::vector<String>::iterator pos = m_list.begin(); pos < m_list.end(); pos++) {
		if ( str.equalsIgnoreCase( *pos)) {
			m_list.erase( pos);
			return;
		}
	}
}

/**
 * Return the string list as a comma separated string
 * 
 * @return String
 */
String StringList::toString( void) const {
	String ret;

	for ( unsigned int i = 0; i < numberOfStrings(); i++) {
		ret += getStringAt( i);
		ret += ",";
	}

	return ret;
}
