package org.example;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class DpData implements WritableComparable<DpData> {

    protected String dp;
    protected long count;
    protected int index;


    public DpData() {

    }

    public DpData(String dp, long count, int index) {
        this.dp = dp;
        this.count = count;
        this.index = index;
    }


    public void readFields(DataInput in) throws IOException {
        this.dp = in.readUTF();
        this.count = in.readLong();
        this.index = in.readInt();
    }

    public void write(DataOutput out) throws IOException {
        out.writeUTF(this.dp);
        out.writeLong(this.count);
        out.writeInt(this.index);
    }

    public String getDp() {
        return dp;
    }

    public long getCount() {
        return count;
    }

    public int getIndex() {
        return index;
    }

    public String toString() {
        return ("dp: " + this.dp + " ,  count: " + this.count + ",   index: " + this.index);
    }


    @Override
    public int compareTo(DpData o) {
        return this.dp.compareTo(o.getDp());
    }
}
