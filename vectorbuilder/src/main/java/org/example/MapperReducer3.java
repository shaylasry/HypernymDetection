package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.HashPartitioner;
import org.tartarus.snowball.ext.englishStemmer;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

public class MapperReducer3 {


    public static class MapperClass extends Mapper<Nouns, NounsVector, Nouns, NounsVector> {
        HashMap<String, String> hypernym = getHypernym();


        @Override
        public void map(Nouns key, NounsVector value, Context context) throws IOException,  InterruptedException {

            String isHypernym = hypernymContainsPair(key);
            if (!isHypernym.equals("X")) {
                value.setHypernym(isHypernym);
                context.write(key, value);
            }
        }
//        comoros	territory	True
//        motility	movement	True
//        dispersion	chart	False
//        blood	descent	True
//        prospect	nuptials	False
//        foundation	breakthrough	False
//        line	channel	True
//        cog	teeth	True
        //key: pair  value:[0,0,0,0,0]  ->  [100,200,0,0,0]  , True/False
        //toString: doc/cat  100 200 0 0 0\tTrue
        public HashMap<String, String> getHypernym(){
            englishStemmer stemmer = new englishStemmer();

            HashMap<String, String> hypernymSet = new HashMap<>();

            Region region = Region.US_EAST_1;

            //s3
            S3Client s3 = S3Client.builder()
                    .region(region)
                    .build();

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket("ofiwjoiwf")
                    .key("hypernym.txt")
                    .build();

            ResponseInputStream<GetObjectResponse> input = s3.getObject(getObjectRequest);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
            while (true) {
                String line;
                try {
                    line = reader.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (line == null)
                    break;
                String [] splitLine = line.split("\\s+");
                String nouns = stem(splitLine[0], stemmer) + "/" + stem(splitLine[1], stemmer);
                String isHypernym = splitLine[2];
                hypernymSet.put(nouns, isHypernym);
            }
            return hypernymSet;
        }


        //check if hypernym contains the pair or not
        public String hypernymContainsPair(Nouns nouns) {


            String word1 = nouns.getWord1();
            String word2 = nouns.getWord2();

            String pair1 = word1 + "/" + word2;
            String pair2 = word2 + "/" + word1;
            if (hypernym.containsKey(pair1)) {
                return hypernym.get(pair1);
            }
            if (hypernym.containsKey(pair2)) {
                return hypernym.get(pair2);
            }
            return "X";
        }

        public String stem (String word, englishStemmer stemmer){

            stemmer.setCurrent(word);
            stemmer.stem();
            return stemmer.getCurrent();
        }

        public void cleanup(Context context) throws IOException,  InterruptedException {
        }
    }



    public static class ReducerClass extends Reducer<Nouns, NounsVector, Nouns, NounsVector> {


        @Override
        public void reduce(Nouns key, Iterable<NounsVector> values, Context context) throws IOException,  InterruptedException {

            for (NounsVector value : values) {
                context.write(key, value);
            }
        }




        public void cleanup(Context context) throws IOException,  InterruptedException {
        }

    }



    public static class PartitionerClass extends Partitioner<Text, DpData> {
        @Override
        public int getPartition(Text key, DpData value, int numPartitions) {
            return Math.abs(key.hashCode()) % numPartitions;
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "neg dif");
        job.setNumReduceTasks(1);
        job.setJarByClass(MapperReducer3.class);
        job.setMapperClass(MapperClass.class);
//        job.setPartitionerClass(HashPartitioner.class);
        job.setCombinerClass(ReducerClass.class);
        job.setReducerClass(ReducerClass.class);
        job.setMapOutputKeyClass(Nouns.class);
        job.setMapOutputValueClass(NounsVector.class);
        job.setOutputKeyClass(Nouns.class);
        job.setOutputValueClass(NounsVector.class);
        FileInputFormat.addInputPath(job, new Path(args[1]));
        FileOutputFormat.setOutputPath(job, new Path(args[2]));
        job.setInputFormatClass(SequenceFileInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }



}
