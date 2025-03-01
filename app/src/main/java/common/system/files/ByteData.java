package common.system.files;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import common.system.fake.FakeImage;

interface ByteData extends FileData {

	@Override
	byte[] getBytes();

	@Override
	default FakeImage getImg() {
		return FakeImage.read(getBytes());
	}

	@Override
	default InputStream getStream() {
		return new ByteArrayInputStream(getBytes());
	}

	@Override
	default int size() {
		return getBytes().length;
	}

	@Override
	default String getPath() {
		return "";
	}

}