package org.example;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.HashPartitioner;
import org.tartarus.snowball.ext.englishStemmer;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

//head_word<TAB>$$$$$syntactic-ngram$$$$<TAB>$$$$total_count$$$$<TAB>counts_by_year
//ngrams    cease/VB/ccomp/0 for/IN/prep/1 an/DT/det/4 instant/NN/pobj/2

public class MapperReducer1 {

    public static class MapperClass extends Mapper<LongWritable, Text, Map1to2Key, Map1Value> {
        englishStemmer stemmer = new englishStemmer();

        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException,  InterruptedException {

            //get all the data we need from the line, the syntactic ngram and total count of appearances through year
            String [] line = value.toString().split("\t");
            String syntactic_ngram = line[1];
            String total_count = line[2];


            String [] ngrams = syntactic_ngram.split("\\s+");
//            System.out.print(value.toString() + "             " );

            //seperate each ngram cease/VB/ccomp/0 and put each part in the match array by the index in ngrams array
            String [] words = new String[ngrams.length];
            String [] pos_tag = new String[ngrams.length];
            String [] labels = new String[ngrams.length];
            String [] indexes = new String[ngrams.length];

            for (int i = 0; i < ngrams.length; i++) {
                String [] ngramSplit = ngrams[i].split("/");

                if(ngramSplit.length!=4) {
//                    System.out.println("not good :   " + ngrams[i]);
                    return;
                }

//                System.out.println(ngramSplit[0] + " , " + ngramSplit[1] + " , " + ngramSplit[2] + " , " + ngramSplit [3]);
                words[i] = ngramSplit [0];
                pos_tag[i] = ngramSplit [1];
                labels[i] = ngramSplit [2];
                indexes[i] = ngramSplit [3];
            }

            Graph valueToGraph = makeGraph(words, labels, indexes);
//            System.out.println("finished graph");
            for (int i = 0; i < words.length; i++) {
                for (int j = i + 1; j < words.length; j++){
                    String pair = stem(words[i], stemmer) + "/" + stem(words[j], stemmer);
//                    System.out.println("after stem words are    " + pair);

                    //condition - if we should find the dp run bfs and return dp
                    if (isNoun(pos_tag[i]) && isNoun(pos_tag[j])){
//                        System.out.println("before bfs worrrrrds are   " + pair);
                        String path = valueToGraph.shortestPath (i, j);
//                        System.out.println("after bfs path is   " + path);

                        if(!path.equals("noPath")) {
                            System.out.println("path :   " + path);
                            Map1Value newValue = new Map1Value(pair, Integer.valueOf(total_count));
                            context.write(new Map1to2Key(path, 0), newValue);
                            context.write(new Map1to2Key(path, 1), newValue);
                        }

                        path = valueToGraph.shortestPath (j, i);
                        System.out.println("after bfs path is   " + path);

                        if(!path.equals("noPath")) {
                            Map1Value newValue = new Map1Value(pair, Integer.valueOf(total_count));
                            context.write(new Map1to2Key(key.toString(), 0), newValue);
                            context.write(new Map1to2Key(key.toString(), 1), newValue);
                        }
                    }
                }
            }

        }

        public String stem (String word, englishStemmer stemmer){

            stemmer.setCurrent(word);
            stemmer.stem();
            return stemmer.getCurrent();
        }





        public static Graph makeGraph(String [] words, String [] labels, String [] indexes) {
            Graph graph = new Graph();

            for (int i = 0; i < words.length; i++) {
                //add to graph index and the node
                graph.addNode(i, new Node(words[i]));
            }

            HashMap<Integer, Node> nodes = graph.getNodes();

            for (int i = 0; i < words.length; i++) {
                //

                int prev = Integer.valueOf(indexes[i]);
                if(prev != 0) {
                    Node target = nodes.get(i);
                    Node source = nodes.get(prev - 1);

                    source.addNeighbor(target, labels[prev - 1]);
                }

            }
            return graph;
        }




        public boolean isNoun (String word){
            String[] nouns = {"NN", "NNS", "NNP", "NNPS"};

            for (String noun : nouns) {
                if (word.equals(noun)) {
                    return true;
                }
            }
            return false;
        }





        public void cleanup(Context context) {
        }
    }





// dp dog friend with cat

    public static class ReducerClass extends Reducer<Map1to2Key, Map1Value,Text, NounsData> {
        //init counter
        AtomicInteger dp_counter = new AtomicInteger(0);
        static long uniques_counter = 0;
        static String curDp = "";
        @Override
        public void reduce(Map1to2Key key, Iterable<Map1Value> values, Context context) throws IOException,  InterruptedException {

            long dpmin = 20;
            String lastNouns = "";

            if (!key.getDp().equals(curDp)) {
                curDp = key.getDp();
                uniques_counter = 0;
            }
            //counts unique pairs
            for(Map1Value nounsAndCount : values){
                if (key.getIndex() == 0) {
                    if (!nounsAndCount.nouns.equals(lastNouns)) {
                        lastNouns = nounsAndCount.nouns;
                        uniques_counter++;
                    }
                } else if (key.getIndex() == 1 && uniques_counter >= dpmin) {
                        NounsData newValue = new NounsData(nounsAndCount.getNouns(), nounsAndCount.getCount(), dp_counter.get());
                        context.write(new Text(key.getDp()), newValue);
                        System.out.println(key.toString() + " ,,,  " + nounsAndCount.toString());
                }
            }

            if (key.getIndex() == 1 && uniques_counter >= dpmin) {
                dp_counter.getAndIncrement();
            }


        }

        //before we close reducer we will upload a new file to s3 after we write inside the dp_counter that represent the vector size
        public void cleanup(Context context) {
            if (dp_counter.get() != 0) {
                String filename = "dp_counter.txt";
                File file = new File(filename);
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(Integer.toString(dp_counter.get()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Region region = Region.US_EAST_1;

                //s3
                S3Client s3 = S3Client.builder()
                        .region(region)
                        .build();

                try {
                    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                            .bucket("ofiwjoiwf")
                            .key("dp_counter.txt")
                            .build();

                    s3.putObject(putObjectRequest, RequestBody.fromFile(file));
                } catch (S3Exception e) {
                    s3.close();
                    System.err.println(e.getMessage());
                    System.exit(1);
                }
            }
        }
    }



    public static class PartitionerClass extends Partitioner<Map1to2Key, Map1Value> {
        @Override
        public int getPartition(Map1to2Key key, Map1Value value, int numPartitions) {
            //use same partitioner to ensure one reducer only
            return (Math.abs((new Text(key.getDp())).hashCode()) % numPartitions);
        }
    }
    

    public static void main(String[] args) throws Exception {

//put counter value into conf object of the job where you need to access it
//you can choose any name for the conf key really (i just used counter enum name here)
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "3gram count");
        job.setNumReduceTasks(1);
        job.setJarByClass(MapperReducer1.class);
        job.setMapperClass(MapperClass.class);
//        job.setPartitionerClass(PartitionerClass.class);
        job.setReducerClass(ReducerClass.class);
        job.setMapOutputKeyClass(Map1to2Key.class);
        job.setMapOutputValueClass(Map1Value.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NounsData.class);
        FileInputFormat.addInputPath(job, new Path(args[1]));
        FileOutputFormat.setOutputPath(job, new Path(args[2]));
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);
        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }

}



