/**
 *
 */
package com.aceql.client.jdbc.driver;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;

import com.aceql.client.jdbc.driver.util.framework.FrameworkFileUtil;
import com.aceql.client.jdbc.driver.util.framework.Tag;

/**
 * Blob implementation. <br>
 * <br>
 *
 * @since 6.0
 * @author Nicolas de Pomereu
 *
 */
public class AceQLBlob implements Blob {

    private InputStream inputStream;
    private byte[] bytes;
    private EditionType editionType;

    private File file;
    private OutputStream outputStream;

    /**
     * Protected constructor to be used only for upload by
     * {@code AceQLConnection.createBlob()}
     */
    AceQLBlob(EditionType editionType) {
	this.editionType = Objects.requireNonNull(editionType, "editionType cannot be null!");
	this.file = createBlobFile();
    }

    /**
     * To be used with ResultSet. Package protected constructor to be used only for
     * Community Edition that does not support Streams
     *
     * @param inputStream
     */
    AceQLBlob(InputStream inputStream, EditionType editionType) {
	this.inputStream = inputStream;
	this.bytes = null;
	this.editionType = Objects.requireNonNull(editionType, "editionType cannot be null!");
    }

    /**
     * To be used with ResultSet. Package protected constructor to be used only for
     * Community Edition & Professional Editions
     *
     * @param bytes
     */
    AceQLBlob(byte[] bytes, EditionType editionType) {
	this.bytes = bytes;
	this.inputStream = null;
	this.editionType = Objects.requireNonNull(editionType, "editionType cannot be null!");
    }

    @Override
    public long length() throws SQLException {
	if (editionType.equals(EditionType.Professional)) {
	    return -1;
	} else {
	    return bytes == null ? 0 : bytes.length;
	}
    }

    @Override
    public byte[] getBytes(long pos, int length) throws SQLException {
	return getbytesStatic(pos, length, bytes);
    }

    /**
     * @param pos
     * @param length
     * @return
     */
    public static byte[] getbytesStatic(long pos, int length, byte [] bytes) {
	List<Byte>bytesList = new ArrayList<>();
	for (int i = (int)pos - 1; i < length; i++) {
	    bytesList.add(bytes[i]);
	}

	Byte [] bytesToReturnArray = bytesList.toArray(new Byte[bytesList.size()]);
	byte[] bytesToReturn = ArrayUtils.toPrimitive(bytesToReturnArray);
	return bytesToReturn;
    }

    @Override
    public InputStream getBinaryStream() throws SQLException {
	if (editionType.equals(EditionType.Professional)) {
	    return inputStream;
	} else {
	    if (bytes == null) {
		return null;
	    }

	    ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(bytes);
	    return arrayInputStream;
	}
    }

    @Override
    public long position(byte[] pattern, long start) throws SQLException {
	throw new UnsupportedOperationException(Tag.METHOD_NOT_YET_IMPLEMENTED);
    }

    @Override
    public long position(Blob pattern, long start) throws SQLException {
	throw new UnsupportedOperationException(Tag.METHOD_NOT_YET_IMPLEMENTED);
    }

    @Override
    public int setBytes(long pos, byte[] bytes) throws SQLException {
	if (pos != 1) {
	    throw new SQLException(Tag.PRODUCT + " \"pos\" value can be 1 only.");
	}

	this.bytes = bytes;
	return bytes == null ? 0 : bytes.length;
    }

    @Override
    public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
	throw new UnsupportedOperationException(Tag.METHOD_NOT_YET_IMPLEMENTED);
    }

    @Override
    public OutputStream setBinaryStream(long pos) throws SQLException {

	if (editionType.equals(EditionType.Community)) {
	    throw new UnsupportedOperationException(Tag.PRODUCT +  " " + "Blob.setBinaryStream(long) call " + Tag.REQUIRES_ACEQL_JDBC_DRIVER_PROFESSIONAL_EDITION);
	}

	if (pos != 1) {
	    throw new SQLException(Tag.PRODUCT + " \"pos\" value can be 1 only.");
	}

	if (file == null) {
	    throw new SQLException(Tag.PRODUCT + " Can not call setBinaryStream() when reading a Blob file.");
	}

	try {
	    outputStream = new BufferedOutputStream(new FileOutputStream(file));
	    return outputStream;
	} catch (FileNotFoundException e) {
	    throw new SQLException(e);
	}
    }

    /**
     * @return the file associated with the {@code OutputStream} created by
     *         {@link AceQLBlob#setBinaryStream(long)}.
     */
    File getFile()  {
	try {
	    outputStream.close();
	} catch (IOException e) {
	    System.err.println(
		    Tag.PRODUCT + " Warning: error when closing Blob OutputStream: " + e.getLocalizedMessage());
	}

	return file;
    }

    private static File createBlobFile() {
	File file = new File(FrameworkFileUtil.getKawansoftTempDir() + File.separator + "blob-file-for-server-"
		+ FrameworkFileUtil.getUniqueId() + ".txt");
	return file;
    }

    @Override
    public void truncate(long len) throws SQLException {
	throw new UnsupportedOperationException(Tag.METHOD_NOT_YET_IMPLEMENTED);
    }

    @Override
    public void free() throws SQLException {
	if (editionType.equals(EditionType.Professional) && inputStream != null) {
	    try {
		inputStream.close();
	    } catch (IOException e) {
		throw new SQLException(e);
	    }
	}

	if (file != null) {
	    file.delete();
	}
    }

    @Override
    public InputStream getBinaryStream(long pos, long length) throws SQLException {
	if (pos != 1) {
	    throw new SQLException(Tag.PRODUCT + " \"pos\" value can be 1 only.");
	}
	if (length != 0) {
	    throw new SQLException(Tag.PRODUCT + " \"length\" value can be 0 only (meaning all bytes are returned).");
	}
	return getBinaryStream();
    }

}
