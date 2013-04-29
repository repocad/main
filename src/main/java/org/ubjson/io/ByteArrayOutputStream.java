/**
 * Copyright 2011 The Buzz Media, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ubjson.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Class used to implement a very efficient and re-usable (see {@link #reset()})
 * {@link OutputStream} that writes to an underlying (and dynamically-grown)
 * <code>byte[]</code> that the caller can get direct access to via
 * {@link #getArray()}. This class is a compliment to
 * {@link ByteArrayInputStream}.
 * <p/>
 * This class is meant to be a fast bridge between the stream-based Universal
 * Binary JSON I/O classes and a simple <code>byte[]</code> which can be helpful
 * when working with non-stream-based I/O, like Java NIO.
 * <h3>Performance</h3>
 * The JDK already provides a {@link java.io.ByteArrayOutputStream}
 * implementation, unfortunately it is neither re-usable nor can the underlying
 * <code>byte[]</code> be accessed without incurring the cost of a full array
 * copy.
 * <p/>
 * By allowing this {@link ByteArrayOutputStream} implementation to be both
 * re-usable and the underlying <code>byte[]</code> directly-accessible, it is
 * expected that in high-performance environments, this implementation will
 * perform an order of magnitude faster than the JDK's implementation.
 * <p/>
 * Utilizing this class avoids the performance burden caused by unnecessary
 * object creation and GC cleanup (especially in high-performance systems) as
 * well as the memory and CPU overhead caused by complete array duplication just
 * to get results. These performance wins can be significant.
 * <p/>
 * <strong>TIP</strong>: In a very high-performance system where data is being
 * written out as <code>byte[]</code>, you can keep a pool around of streams
 * wrapping this stream type (e.g. {@link UBJOutputStream} wrapping
 * {@link ByteArrayOutputStream}) and re-use them over and over again without
 * ever needing to create new instances or GC old ones. The performance benefits
 * in a high-frequency system can be significant; especially for long-running
 * applications.
 * <p/>
 * It is because of the re-usability nature of this stream type that allows any
 * wrapping streams to be implicitly (and safely) re-usable as well.
 * <h3>Reuse</h3>
 * The most powerful aspect of this {@link ByteArrayOutputStream} implementation
 * is that it is meant to be re-used over and over again by way of the
 * {@link #reset()} operation.
 * <p/>
 * An interesting side-effect of this stream being re-usable is that it makes
 * any wrapping stream around it implicitly re-usable as well since the
 * "state of the world" gets reset.
 * <p/>
 * Streams wrapping instances of {@link ByteArrayOutputStream} don't need to do
 * anything special to become re-usable; once they have
 * {@link OutputStream#flush()}'ed their state, if the underlying
 * {@link ByteArrayOutputStream} is reset under them, they don't need to know
 * anything about it.
 * <p/>
 * This allows more efficient, long-lived usage of other stream classes; namely
 * the Universal Binary JSON I/O streams.
 * <p/>
 * <strong>REMINDER</strong>: {@link #flush()} and {@link #close()} are no-op
 * operations. You don't need to worry about not being able to re-use instances
 * of this class because a wrapping stream type issued a flush or a close; the
 * state of streams of this type are not effected by those operations.
 * <h3>Usage</h3>
 * This class is designed such that you create an instance of this class, then
 * wrap it with a {@link UBJOutputStream} and write any amount of Universal
 * Binary JSON to the underlying <code>byte[]</code> stream.
 * <p/>
 * When ready, you can call {@link #getArray()} to retrieve the underlying array
 * (the underlying <code>byte[]</code> is not copied; the raw reference is
 * returned immediately) as well as {@link #getLength()} to determine how many
 * bytes were written to it and process the Universal Binary JSON byte data
 * accordingly (e.g. add it to a {@link ByteBuffer}, write it to a socket, etc).
 * <p/>
 * When you are done processing the <code>byte[]</code> contents, simply call
 * {@link #reset()} on the stream (resets the single <code>int</code> counter
 * internally and returns), and begin your next write operation to the wrapping
 * {@link UBJOutputStream}.
 *
 * Usage would look something like this:
 *
 * <pre>
 * <code>
 * // Create streams individually so we have access to baos.
 * {@link ByteArrayOutputStream} baos = new {@link ByteArrayOutputStream}();
 * {@link UBJOutputStream} out = new {@link UBJOutputStream}(baos);
 *
 * // Write some Universal Binary JSON
 * out.writeObjectHeader(2);
 *
 * // "userID": 22345
 * out.writeString("userID");
 * out.writeInt32(22345);
 *
 * // "username": "billg64" 
 * out.writeString("username");
 * out.writeString("billg64");
 *
 * // Hypothetical write method that accepts (byte[] data, int length) args 
 * writeUsingNIO(baos.getArray(), baos.getLength());
 *
 * // Reset the underlying stream so we make it like new.
 * baos.reset();
 *
 * // Begin writing some new constructs...
 * out.writeArrayHead(3);
 *
 * ... more code ...
 * </code>
 * </pre>
 *
 * Since {@link UBJOutputStream} maintains no internal state and simply acts as
 * a translation layer between Java data types and byte-based UBJ
 * representation, resetting the underlying stream that it wraps is a safe
 * operation.
 * <p/>
 * This provides a very efficient mechanism for working with Universal Binary
 * JSON via the core I/O classes without wasting CPU or memory resources
 * creating/destroying byte arrays or output stream.
 * <h3>Safety</h3>
 * This class intentionally has no constructor accepting a <code>byte[]</code>
 * argument because this class adjusts the size of the underlying data
 * <code>byte[]</code> on the fly as new data is written to it; meaning that if
 * a caller could provide a reference to their own <code>byte[]</code> at
 * instantiation time, it is possible that this class would create a new
 * (bigger) <code>byte[]</code> internally and the reference the caller provided
 * would no longer be valid (the caller and this stream would point at two
 * different <code>byte[]</code>).
 * <p/>
 * To avoid the potential for foot-shooting, this class doesn't allow that
 * scenario to occur.
 *
 * @author Riyad Kalla (software@thebuzzmedia.com)
 * @see ByteArrayInputStream
 */
public class ByteArrayOutputStream extends OutputStream {
    protected int i;
    protected byte[] bbuffer;

    /**
     * Creates an stream backed by a <code>byte[]</code> with an initial length
     * of 8,192 (8KB).
     */
    public ByteArrayOutputStream() {
        this(8192);
    }

    /**
     * Creates a stream backed by a <code>byte[]</code> with an initial length
     * of <code>initialSize</code>.
     *
     * @param initialSize
     *            The initial size of the underlying <code>byte[]</code>.
     *
     * @throws IllegalArgumentException
     *             if <code>initialSize</code> is &lt; <code>0</code>.
     */
    public ByteArrayOutputStream(int initialSize)
            throws IllegalArgumentException {
        if (initialSize < 0)
            throw new IllegalArgumentException("initialSize [" + initialSize
                    + "] must be >= 0");

        i = 0;
        bbuffer = new byte[initialSize];
    }

    @Override
    public void write(int value) throws IOException {
        ensureCapacity(i + 1);
        bbuffer[i++] = (byte) value;
    }

    @Override
    public void write(byte[] data, int offset, int length) throws IOException {
        if (data == null)
            throw new IllegalArgumentException("data cannot be null");
        if (offset < 0 || length < 0 || (offset + length) > data.length)
            throw new IllegalArgumentException("offset [" + offset
                    + "] and length [" + length
                    + "] must be >= 0 and (offset + length)["
                    + (offset + length) + "] must be <= data.length ["
                    + data.length + "]");

        ensureCapacity(i + length);
        System.arraycopy(data, offset, bbuffer, i, length);
        i += length;
    }

    /**
     * Used to reset the internal index used to track the size and insertion
     * position in the underlying <code>byte[]</code>; effectively resetting
     * this {@link OutputStream} and preparing it for re-use.
     * <p/>
     * This is a very fast operation that adjusts a single <code>int</code> and
     * returns.
     */
    public void reset() {
        i = 0;
    }

    /**
     * Used to get direct access to the <code>byte[]</code> that backs this
     * {@link OutputStream} implementation.
     *
     * @return the <code>byte[]</code> that backs this {@link OutputStream}
     *         implementation.
     */
    public byte[] getArray() {
        return bbuffer;
    }

    /**
     * Used to get the current length of data written to the underlying
     * <code>byte[]</code>.
     *
     * @return the current length of data written to the underlying
     *         <code>byte[]</code>.
     */
    public int getLength() {
        return i;
    }

    protected void ensureCapacity(int capacity) {
        if (capacity <= bbuffer.length)
            return;

        byte[] tmp = new byte[capacity];
        System.arraycopy(bbuffer, 0, tmp, 0, bbuffer.length);
        bbuffer = tmp;
    }
}