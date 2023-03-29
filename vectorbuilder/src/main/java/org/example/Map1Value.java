package org.example;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;
import java.util.LinkedList;
import java.util.List;

public class Map1Value implements WritableComparable<Map1Value> {

    String nouns;
    long count;


    public Map1Value() {
    }
    public Map1Value(String nouns, long count) {
        this.nouns = nouns;
        this.count = count;
    }


    public void readFields(DataInput in) throws IOException {
        this.nouns = in.readUTF();
        this.count = in.readLong();
    }

    public void write(DataOutput out) throws IOException {
        out.writeUTF(this.nouns);
        out.writeLong(this.count);
    }

    public long getCount() {
        return count;
    }

    public String getNouns() {
        return nouns;
    }

    public String toString() {
        return ("nouns: " + this.nouns + " ,  count: " + this.count);
    }
    @Override
    public int compareTo(Map1Value o) {
        return 0;
    }
}
