package info.rsdev.xb4j.util.file;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64InputStream;
import org.junit.Test;

public class DefaultXmlCodingFactoryTest {

	@Test
	public void testGetDecodingStream() throws Exception {
		byte[] buffer = "SGVsbG8gd29ybGQh".getBytes();	//Base64 encoded version of 'Hello world!'
		ByteArrayInputStream in = new ByteArrayInputStream(buffer);
		InputStream decodingStream = DefaultXmlCodingFactory.INSTANCE.getEncodingStream(in, "base64", Boolean.FALSE);	//Force decoding by setting doEncode to false via contructorParam
		assertNotNull(decodingStream);
		assertSame(Base64InputStream.class, decodingStream.getClass());
		
		InputStreamReader reader = new InputStreamReader(decodingStream, "UTF-8");
		char[] decoded = new char[1024];
		int length = reader.read(decoded);
		assertArrayEquals("Hello world!".toCharArray(), Arrays.copyOf(decoded, length));
	}

}
