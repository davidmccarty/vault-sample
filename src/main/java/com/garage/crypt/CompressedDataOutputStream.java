package com.garage.crypt;

import java.io.IOException;
import java.io.OutputStream;

import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;

public class CompressedDataOutputStream extends OutputStream {

	static final int BUFFER_SIZE = 1 << 16;

	PGPCompressedDataGenerator compressedDataGenerator;
	OutputStream compressedDataGeneratorOutputStream;

	public CompressedDataOutputStream(OutputStream outputStream) throws IOException {
		compressedDataGenerator = new PGPCompressedDataGenerator(PGPCompressedData.ZIP);
		compressedDataGeneratorOutputStream = compressedDataGenerator.open(outputStream);
	}

	public void write(int b) throws IOException {
		compressedDataGeneratorOutputStream.write(b);
	}

	public void close() throws IOException {

		if (compressedDataGeneratorOutputStream != null) {
			compressedDataGeneratorOutputStream.flush();
			compressedDataGeneratorOutputStream.close();
			compressedDataGeneratorOutputStream = null;
		}

		if (compressedDataGenerator != null) {
			compressedDataGenerator.close();
			compressedDataGenerator = null;
		}
	}
}
