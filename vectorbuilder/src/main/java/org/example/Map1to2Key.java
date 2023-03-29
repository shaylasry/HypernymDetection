package org.example;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

public class Map1to2Key implements WritableComparable<Map1to2Key> {

    String dp;
    long index;


    public Map1to2Key() {
    }
    public Map1to2Key(String dp, long index) {
        this.dp = dp;
        this.index = index;
    }


    public void readFields(DataInput in) throws IOException {
        this.dp = in.readUTF();
        this.index = in.readLong();
    }

    public void write(DataOutput out) throws IOException {
        out.writeUTF(this.dp);
        out.writeLong(this.index);
    }


    public long getIndex() {
        return index;
    }

    public String getDp() {
        return dp;
    }

    public String toString() {
        return ("dp: " + this.dp + " ,  unique count: " + this.index);
    }
    @Override
    public int compareTo(Map1to2Key other) {
        int dpCompare = this.dp.compareTo(other.getDp());
        if (dpCompare == 0) {
            return Long.compare(this.getIndex(), other.getIndex());
        }
        return dpCompare;
    }
}
