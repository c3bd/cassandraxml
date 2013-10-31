/* 
 *  `gnu.iou' I/O buffers and utilities.
 *  Copyright (C) 1998, 1999, 2000, 2001, 2002 John Pritchard.
 *
 *  This program is free software; you can redistribute it or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free
 *  Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307 USA
 */

package org.exist.util;

/**
 * This class contains two static tools for doing UTF-8 encoding and
 * decoding.  
 *
 * <p> UTF-8 is ASCII- transparent.  It supports character sets
 * requiring more than the seven bit ASCII base range of UTF-8,
 * including Unicode, ISO-8859, ISO-10646, etc..
 * 
 * <p> We do not use an ISO UCS code signature, and we do not use a
 * Java Data I/O- style strlen prefix.  
 *
 * @author John Pritchard (john@syntelos.org)
 */
public class UTF8 {

	/**
	 * Encode string in UTF-8.
	 */
	public final static byte[] encode(char[] str) {

		if (null == str || 0 >= str.length)
			return null;

		return encode(str, 0, str.length, null, 0);
	}

	/**
	 * Encode string in UTF-8.
	 * 
	 * Warning: the size of bytbuf is not checked. Use encoded() to determine
	 * the size needed.
	 */
	public final static byte[] encode(
		char[] str,
		int start,
		int length,
		byte[] bytbuf,
		int offset) {

		if (null == str || 0 >= length)
			return bytbuf;

		if (bytbuf == null)
			bytbuf = new byte[encoded(str, start, length)];

		char ch, sch;
		int end = start + length;
		for (int c = start; c < end; c++) {

			ch = str[c];

			if (0x7f >= ch) {
				bytbuf[offset++] = (byte) ch;
			} else if (0x7ff >= ch) {

				sch = (char) (ch >>> 6);

				if (0 < sch) {
					bytbuf[offset++] = (byte) (b11000000 | (sch & b00011111));
				} else
					bytbuf[offset++] = (byte) (b11000000);

				bytbuf[offset++] = (byte) (b10000000 | (ch & b00111111));
			} else {

				sch = (char) (ch >>> 12);

				if (0 < sch) {

					bytbuf[offset++] = (byte) (b11100000 | (sch & b00001111));
				} else
					bytbuf[offset++] = (byte) (b11100000);

				bytbuf[offset++] = (byte) (b10000000 | ((ch >>> 6) & b00111111));

				bytbuf[offset++] = (byte) (b10000000 | (ch & b00111111));
			}
		}

		return bytbuf;
	}

	public final static byte[] encode(String str, byte[] bytbuf, int offset) {
		return encode(str, 0, str.length(), bytbuf, offset);
	}

	/**
		 * Encode string in UTF-8.
		 * 
		 * Warning: the size of bytbuf is not checked. Use encoded() to determine
		 * the size needed.
		 */
	public final static byte[] encode(
		String str,
		int start,
		int length,
		byte[] bytbuf,
		int offset) {

		if (null == str || 0 >= length)
			return bytbuf;

		char ch, sch;
		int end = start + length;
		for (int c = start; c < end; c++) {

			ch = str.charAt(c);

			if (0x7f >= ch) {
				bytbuf[offset++] = (byte) ch;
			} else if (0x7ff >= ch) {

				sch = (char) (ch >>> 6);

				if (0 < sch) {
					bytbuf[offset++] = (byte) (b11000000 | (sch & b00011111));
				} else
					bytbuf[offset++] = (byte) (b11000000);

				bytbuf[offset++] = (byte) (b10000000 | (ch & b00111111));
			} else {

				sch = (char) (ch >>> 12);

				if (0 < sch) {

					bytbuf[offset++] = (byte) (b11100000 | (sch & b00001111));
				} else
					bytbuf[offset++] = (byte) (b11100000);

				bytbuf[offset++] = (byte) (b10000000 | ((ch >>> 6) & b00111111));

				bytbuf[offset++] = (byte) (b10000000 | (ch & b00111111));
			}
		}

		return bytbuf;
	}

	/**
	 * Encode string in UTF-8.
	 */
	public final static byte[] encode(String s) {

		if (null == s)
			return null;
		else {

			return encode(s.toCharArray(), 0, s.length(), null, 0);
		}
	}

	private final static char b10000000 = (char) 0x80;
	private final static char b11000000 = (char) 0xC0;
	private final static char b11100000 = (char) 0xE0;
	private final static char b11110000 = (char) 0xF0;
	private final static char b11111000 = (char) 0xF8;
	private final static char b11111100 = (char) 0xFC;
	private final static char b11111110 = (char) 0xFE;

	private final static char b01111111 = (char) 0x7F;
	private final static char b00111111 = (char) 0x3F;
	private final static char b00011111 = (char) 0x1F;
	private final static char b00001111 = (char) 0x0F;
	//private final static char b00000111 = (char) 0x07;
	//private final static char b00000011 = (char) 0x03;
	//private final static char b00000001 = (char) 0x01;

	/**
	 * Returns the length of the string encoded in UTF-8.
	 */
	public final static int encoded(String str) {

		if (null == str)
			return 0;

		int bytlen = 0;

		char ch;
		//char sch;
		for (int c = 0; c < str.length(); c++) {

			ch = str.charAt(c);

			if (0x7f >= ch)
				bytlen++;

			else if (0x7ff >= ch)
				bytlen += 2;

			else
				bytlen += 3;

		}

		return bytlen;
	}
	
	/**
	 * Returns the length of the string encoded in UTF-8.
	 */
	public final static int encoded(char[] str, int start, int len) {

		if (null == str || 0 >= len)
			return 0;

		int bytlen = 0;

		char ch;
		//char sch;
		int end = start + len;
		for (int c = start; c < end; c++) {

			ch = str[c];

			if (0x7f >= ch)
				bytlen++;

			else if (0x7ff >= ch)
				bytlen += 2;

			else
				bytlen += 3;

		}

		return bytlen;
	}
    
    /**
     * Static method to generate the UTF-8 representation of a Unicode character.
     * This particular code is taken from saxon (see http://saxon.sf.net).
     * 
     * @param in the Unicode character, or the high half of a surrogate pair
     * @param in2 the low half of a surrogate pair (ignored unless the first argument is in the
     * range for a surrogate pair)
     * @param out an array of at least 4 bytes to hold the UTF-8 representation.
     * @return the number of bytes in the UTF-8 representation
     */
     public static int getUTF8Encoding(char in, char in2, byte[] out) {
         // See Tony Graham, "Unicode, a Primer", page 92
         int i = (int)in;
         if (i<=0x7f) {
             out[0] = (byte)i;
             return 1;
         } else if (i<=0x7ff) {
             out[0] = (byte)(0xc0 | ((in >> 6) & 0x1f));
             out[1] = (byte)(0x80 | (in & 0x3f));
             return 2;
         } else if (i>=0xd800 && i<=0xdbff) {
             // surrogate pair
             int j = (int)in2;
             if (!(j>=0xdc00 && j<=0xdfff)) {
                 throw new IllegalArgumentException("Malformed Unicode Surrogate Pair (" + i + "," + j + ")");
             }
             byte xxxxxx = (byte)(j & 0x3f);
             byte yyyyyy = (byte)(((i & 0x03) << 4) | ((j >> 6) & 0x0f));
             byte zzzz = (byte)((i >> 2) & 0x0f);
             byte uuuuu = (byte)(((i >> 6) & 0x0f) + 1);
             out[0] = (byte)(0xf0 | ((uuuuu >> 2) & 0x07));
             out[1] = (byte)(0x80 | ((uuuuu & 0x03) << 4) | zzzz);
             out[2] = (byte)(0x80 | yyyyyy);
             out[3] = (byte)(0x80 | xxxxxx);
             return 4;
         } else if (i>=0xdc00 && i<=0xdfff) {
             // second half of surrogate pair - ignore it
             return 0;
         } else {
             out[0] = (byte)(0xe0 | ((in >> 12) & 0x0f));
             out[1] = (byte)(0x80 | ((in >> 6) & 0x3f));
             out[2] = (byte)(0x80 | (in & 0x3f));
             return 3;
         }
     }

}
