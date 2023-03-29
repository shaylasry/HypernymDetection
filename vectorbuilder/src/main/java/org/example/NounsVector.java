package org.example;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NounsVector implements WritableComparable<NounsVector> {

    ArrayList<LongWritable> counts = new ArrayList<LongWritable>();
    String isHypernym;

    public NounsVector() {
    }

    public NounsVector(List<LongWritable> counts) {
        for (int i = 0; i < counts.size(); i++) {
            this.counts.add(counts.get(i));
        }
        this.isHypernym = "X";
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(this.counts.size());
        for (LongWritable item : this.counts) {
            item.write(out);
        }
        out.writeUTF(this.isHypernym);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        int size = in.readInt();
        this.counts = new ArrayList<LongWritable>(size);
        for (int i = 0; i < size; i++) {
            LongWritable item = new LongWritable();
            item.readFields(in);
            this.counts.add(item);
        }
        this.isHypernym = in.readUTF();
    }
    public void setHypernym(String hypernym) {
        isHypernym = hypernym;
    }

    public List getCounts() {
        return counts;
    }

    public String toString() {
        String result = "";

        result += this.counts.toString();
        result = result.substring(1, result.length() - 1);
        result = result.replaceAll("\\s+", "");
        result += "," + isHypernym;

        return result;
    }


    @Override
    public int compareTo(NounsVector o) {
        return 0;
    }
}