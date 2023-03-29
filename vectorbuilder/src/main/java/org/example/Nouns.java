package org.example;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class Nouns implements WritableComparable<Nouns> {

    protected String word1;
    protected String word2;

    public Nouns() {

    }

    public Nouns(String word1, String word2) {
        this.word1 = word1;
        this.word2 = word2;
    }


    public void readFields(DataInput in) throws IOException {
        this.word1 = in.readUTF();
        this.word2 = in.readUTF();

    }

    public void write(DataOutput out) throws IOException {
        out.writeUTF(this.word1);
        out.writeUTF(this.word2);

    }

    public String getWord1() {
        return word1;
    }

    public String getWord2() {
        return word2;
    }

    public String toString() {
        return ("\"" + this.word1 + "\",\"" + this.word2 + "\",");
    }

    @Override
    public int compareTo(Nouns o) {

        //same sequence
        int word1ToWord1 = this.word1.compareTo(o.getWord1());
        int word2ToWord2 = this.word2.compareTo(o.getWord2());

        //revers sequence
        int word1ToWord2 = this.word1.compareTo(o.getWord2());
        int word2ToWord1 = this.word2.compareTo(o.getWord1());

        if (word1ToWord1 == 0) {
            return word2ToWord2;
        }

        if(word1ToWord2 == 0) {
            return word2ToWord1;
        }

        //if we got here it mean that this.word1 and o.word1 are not the same so
        //we won't get 0 and this and o will define as different keys.
        return word1ToWord1;
    }
}
