package com.garage.crypt;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;

public class LiteralDataOutputStream extends OutputStream {

	static final int BUFFER_SIZE = 1 << 16;

	PGPLiteralDataGenerator _literalDataGenerator;
	OutputStream _literalDataGeneratorOutputStream;

	public LiteralDataOutputStream(
		String fileName,
		long fileLength,
		Date fileModificationTime,
		OutputStream outputStream) throws IOException {

		_literalDataGenerator = new PGPLiteralDataGenerator();

		_literalDataGeneratorOutputStream =
			_literalDataGenerator.open(
				outputStream,
				PGPLiteralData.BINARY,
				fileName,
				fileLength,
				fileModificationTime);

	}

	public void write(int b) throws IOException {
		_literalDataGeneratorOutputStream.write(b);
	}

	public void close() throws IOException {

		if (_literalDataGeneratorOutputStream != null)
		{
			_literalDataGeneratorOutputStream.flush();
			_literalDataGeneratorOutputStream.close();
			_literalDataGeneratorOutputStream = null;
		}

		if (_literalDataGenerator != null)
		{
			_literalDataGenerator.close();
			_literalDataGenerator = null;
		}
	}
}
