/**
 * StringConverter.java
 *
 * Created: Jun 3, 2006
 */
package com.captiveimagination.jgn.convert;

import java.io.*;
import java.lang.reflect.*;
import java.nio.*;

import com.captiveimagination.jgn.*;

/**
 * @author Matthew D. Hicks
 */
public class StringConverter implements Converter {
	public void set(Message message, Method setter, ByteBuffer buffer) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, UTFDataFormatException {
		int length = buffer.getShort() & 0xfff;
		String s = null;
		if (length != 0xfff) {
			byte[] bytes = new byte[length];
			char[] chars = new char[length];
			
			int c;
			buffer.get(bytes, 0, length);
			
			int i = 0;
			for (; i < length; i++) {
				c = bytes[i] & 0xff;
				if (c > 127) break;
				chars[i] = (char)c;
			}
			int charPosition = i;
			while (i < length) {
				c = bytes[i] & 0xff;
				switch (c >> 4) {
					case 0:
					case 1:
					case 2:
					case 3:
					case 4:
					case 5:
					case 6:
					case 7:
						/* 0xxxxxxx */
						i++;
						chars[charPosition++] = (char)c;
						break;
					case 12:
					case 13:
						/* 110x xxxx 10xx xxxx */
						i += 2;
						if (i > length) throw new UTFDataFormatException("Malformed input: partial character at end");
						if ((bytes[i - 1] & 0xc0) != 0x80) throw new UTFDataFormatException("Malformed input around byte " + i);
						chars[charPosition++] = (char)(((c & 0x1f) << 6) | (bytes[i - 1] & 0x3f));
						break;
					case 14:
						/* 1110 xxxx 10xx xxxx 10xx xxxx */
						i += 3;
						if (i > length) throw new UTFDataFormatException("Malformed input: partial character at end");
						if (((bytes[i - 2] & 0xc0) != 0x80) || ((bytes[i - 1] & 0xc0) != 0x80)) throw new UTFDataFormatException("Malformed input around byte " + (i - 1));
						chars[charPosition++] = (char)(((c & 0x0f) << 12) | ((bytes[i - 2] & 0x3f) << 6) | ((bytes[i - 1] & 0x3f) << 0));
						break;
					default:
						/* 10xx xxxx, 1111 xxxx */
						throw new UTFDataFormatException("Malformed input around byte " + i);
				}
			}
			s = new String(chars, 0, charPosition);
		}
		setter.invoke(message, new Object[] {s});
	}

	public void get(Message message, Method getter, ByteBuffer buffer) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, UTFDataFormatException {
		String s = (String)getter.invoke(message, EMPTY_ARRAY);
		if (s == null) {
			buffer.putShort((short)0xffff);
		} else {
			byte[] bytes;
			int length = s.length();
			int utfLength = 0;
			int c, count = 0;
			
			// Determine UTF length
			for (int i = 0; i < length; i++) {
				c = s.charAt(i);
				if ((c >= 0x0001) && (c <= 0x007f)) utfLength++;
				else if (c > 0x07ff) utfLength += 3;
				else utfLength += 2;
			}
			
			if (utfLength > 65535) throw new UTFDataFormatException("Encoded string too long: " + utfLength + " bytes");
			
			bytes = new byte[utfLength + 2];
			bytes[count++] = (byte)((utfLength >>> 8) & 0xff);
			bytes[count++] = (byte)((utfLength >>> 0) & 0xff);
			
			int i = 0;
			for (i = 0; i < length; i++) {
				c = s.charAt(i);
				if (!((c >= 0x0001) && (c <= 0x007f))) break;
				bytes[count++] = (byte)c;
			}
			for (; i < length; i++) {
				c = s.charAt(i);
				if ((c >= 0x0001) && (c <= 0x007f)) {
					bytes[count++] = (byte)c;
				} else if (c > 0x07ff) {
					bytes[count++] = (byte)(0xe0 | ((c >> 12) & 0x0f));
					bytes[count++] = (byte)(0x80 | ((c >> 6) & 0x3f));
					bytes[count++] = (byte)(0x80 | ((c >> 0) & 0x3f));
				} else {
					bytes[count++] = (byte)(0xc0 | ((c >> 6) & 0x1f));
					bytes[count++] = (byte)(0x80 | ((c >> 0) & 0x3f));
				}
			}
			buffer.put(bytes, 0, bytes.length);
		}
	}
}
