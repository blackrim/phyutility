package jebl.evolution.io;

/**
 * Similar to a StringBuilder, but its internal buffer is a byte[] with
 * one entry for each character, so it can only correctly append single-byte
 * characters.
 * 
 * @author Joseph Heled
 * @version $Id: ByteBuilder.java 648 2007-03-12 01:27:50Z twobeers $
 */
public class ByteBuilder implements CharSequence {
    int maxCapacity;
    int current;
    byte[] data;

    void ensureCapacity(final int cap) {
       if( cap > data.length ) {
           int newLen = 2*(cap+1);
           if( newLen <= 0 ) {
               newLen += 256;
           }
           if( newLen > maxCapacity) newLen = maxCapacity;
           if( newLen < cap ) newLen = cap;
           byte[] d  = new byte[newLen];
           System.arraycopy(data, 0, d, 0, data.length);
           data = d;
       }
    }

    /**
     * Constructs a ByteBuilder that will never grow beyond <code>maxCapacity</code>
     * bytes in length.
     * @param maxCapacity
     */
    public ByteBuilder(int maxCapacity) {
        this.maxCapacity = maxCapacity;
        current = 0;
        data = new byte[16];
    }


    public ByteBuilder append(char c) {
        if( current + 1 > data.length ) {
            ensureCapacity(current + 1);
        }
        // will throw an exception if insufficient capacity (maxCapacity reached) 
        data[current] = (byte)c;
        ++current;
        return this;
    }

    public int length() {
        return current;
    }

    public char charAt(int index) {
        return (char)data[index];
    }

    public CharSequence subSequence(int start, int end) {
        return new String(data, start, end - start);
    }

    public String toString() {
        return new String(data, 0, current);
    }
}
