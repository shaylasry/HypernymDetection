package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.HashPartitioner;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

public class MapperReducer2 {


    public static class MapperClass extends Mapper<Text, NounsData, Nouns, DpData> {

        @Override
        public void map(Text key, NounsData value, Context context) throws IOException,  InterruptedException {

            String pair = value.getNouns();
            String [] pairAsArr = pair.split("/");
            Nouns newKey = new Nouns(pairAsArr[0], pairAsArr[1]);
            DpData newValue = new DpData(key.toString(), value.getCount(), value.getIndex());

            //switch between nouns and dp, now nouns is the key.
            //DpData comparator compare according to index which make it easier to make the vector in reducer
            context.write(newKey, newValue);
        }




        public void cleanup(Context context) throws IOException,  InterruptedException {
        }
    }



    public static class ReducerClass extends Reducer<Nouns, DpData, Nouns, NounsVector> {
        static int vectorSize ;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            //TODO init vector size from s3.
            String size = "";
            Region region = Region.US_EAST_1;

            //s3
            S3Client s3 = S3Client.builder()
                    .region(region)
                    .build();

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket("ofiwjoiwf")
                    .key("dp_counter.txt")
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
                size = size+line;
            }
            vectorSize = Integer.parseInt(size);
        }


        @Override
        public void reduce(Nouns key, Iterable<DpData> values, Context context) throws IOException,  InterruptedException {
            long [] curVector = new long[vectorSize];



            //
            // verb/eat/verb     ,  "dog/cat", long: 100,int: 0
            //"dog/cat"          ,   verb/eat/verb , long: 100 ,int 0
            //"dog/cat"          ,   verb/down/verb , long: 200 ,int 1
            for (DpData curDp : values) {
                System.out.println(curDp.getIndex() + "and count:  " + curDp.getCount());

                long count = curDp.getCount();
                int index = curDp.getIndex();

                curVector[index] += count;
            }

            ArrayList<LongWritable> curVectorAsList = new ArrayList<LongWritable>(vectorSize);
            for (int i = 0; i < curVector.length; i++) {
                curVectorAsList.add(new LongWritable(curVector[i]));
            }

            //key: dog/cat      value:[0,0,0,0,0]  ->  dog/cat [100,200,0,0,0]
            NounsVector newValue = new NounsVector(curVectorAsList);

            context.write(key, newValue);
        }


        public void cleanup(Context context) throws IOException,  InterruptedException {
        }

    }



    public static class PartitionerClass extends Partitioner<Nouns, DpData> {
        @Override
        public int getPartition(Nouns key, DpData value, int numPartitions) {
            return (Math.abs(new Text(key.getWord1()).hashCode() + new Text(key.getWord2()).hashCode()) % numPartitions);        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "hits dif");
        job.setJarByClass(MapperReducer2.class);
        job.setMapperClass(MapperClass.class);
        job.setPartitionerClass(PartitionerClass.class);
        job.setReducerClass(ReducerClass.class);
        job.setMapOutputKeyClass(Nouns.class);
        job.setMapOutputValueClass(DpData.class);
        job.setOutputKeyClass(Nouns.class);
        job.setOutputValueClass(NounsVector.class);
        FileInputFormat.addInputPath(job, new Path(args[1]));
        FileOutputFormat.setOutputPath(job, new Path(args[2]));
        job.setInputFormatClass(SequenceFileInputFormat.class);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }



}
