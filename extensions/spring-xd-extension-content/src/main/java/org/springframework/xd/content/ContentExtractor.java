package org.springframework.xd.content;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;

import org.springframework.messaging.Message;

/**
 * Created by ebottard on 08/07/15.
 */
public class ContentExtractor {
	public static void main(String[] args) throws Exception {
		BodyContentHandler handler = new BodyContentHandler();

		InputStream stream = new FileInputStream("/tmp/foo.pdf");
		AutoDetectParser parser = new AutoDetectParser();
		Metadata metadata = new Metadata();
		try {
			parser.parse(stream, handler, metadata);
			System.out.println(handler.toString());
		}
		finally {
			stream.close();
		}
	}

	public String extract(Message<?> in) throws Exception {
		BodyContentHandler handler = new BodyContentHandler();
		AutoDetectParser parser = new AutoDetectParser();
		Metadata metadata = new Metadata();
		byte[] payload = (byte[]) in.getPayload();
		parser.parse(new ByteArrayInputStream(payload), handler, metadata);
		return handler.toString();
	}

}
