package org.example;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NounsData implements WritableComparable<NounsData> {

    protected String nouns;
    protected long count;
    protected int index;

    public NounsData() {

    }

    public NounsData(String nouns, long count, int index) {
        this.nouns = nouns;
        this.count = count;
        this.index = index;
    }


    public void readFields(DataInput in) throws IOException {
        this.nouns = in.readUTF();
        this.count = in.readLong();
        this.index = in.readInt();
    }

    public void write(DataOutput out) throws IOException {
        out.writeUTF(this.nouns);
        out.writeLong(this.count);
        out.writeInt(this.index);
    }

    public String getNouns() {
        return nouns;
    }

    public long getCount() {
        return count;
    }

    public int getIndex() {
        return index;
    }

    public String toString() {
        return ("nouns: " + this.nouns + " ,  count: " + this.count + ",   index: " + this.index);
    }
    @Override
    public int compareTo(NounsData o) {
        return 0;
    }
}
