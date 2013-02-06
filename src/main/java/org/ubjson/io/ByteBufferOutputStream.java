package org.ubjson.io;

import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;

public class ByteBufferOutputStream extends OutputStream {
    protected ByteBuffer bbuffer;

    public ByteBufferOutputStream(ByteBuffer data)
            throws IllegalArgumentException {
        if (data == null)
            throw new IllegalArgumentException("data cannot be null");

        this.bbuffer = data;
    }
	
	/*
	 * TODO: Need to audit this impl for the correct re-usability workflow.
	 */

    @Override
    public void write(int b) throws ReadOnlyBufferException,
            BufferOverflowException {
        bbuffer.put((byte) b);
    }

    @Override
    public void write(byte[] data, int offset, int length)
            throws ReadOnlyBufferException, IndexOutOfBoundsException,
            BufferOverflowException {
        bbuffer.put(data, offset, length);
    }

    public void reset() {
        bbuffer.clear();
    }

    public ByteBuffer getBuffer() {
        return bbuffer;
    }
}