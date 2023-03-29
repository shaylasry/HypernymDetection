package org.example;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.emr.EmrClient;
import software.amazon.awssdk.services.emr.model.*;


public class Main {


    public static void main(String[] args){
        Region region = Region.US_EAST_1;
        ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create();

        //s3
        EmrClient emr = EmrClient.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();

        HadoopJarStepConfig hadoopJarStepmapreducer1 = HadoopJarStepConfig.builder()
                .jar("s3://ofiwjoiwf/1assignment3-jar-with-dependencies.jar")
                .mainClass("com.exmaple.MapperReducer1")
                .args("s3://ofiwjoiwf/biarcs","s3://ofiwjoiwf/map1output")
                .build();

        HadoopJarStepConfig hadoopJarStepmapreducer2 = HadoopJarStepConfig.builder()
                .jar("s3://ofiwjoiwf/2assignment3-jar-with-dependencies.jar")
                .mainClass("com.exmaple.MapperReducer2")
                .args("s3://ofiwjoiwf/map1output/","s3://ofiwjoiwf/map2output/")
                .build();

        HadoopJarStepConfig hadoopJarStepmapreducer3 = HadoopJarStepConfig.builder()
                .jar("s3://ofiwjoiwf/3assignment3-jar-with-dependencies.jar")
                .mainClass("com.exmaple.MapperReducer3")
                .args("s3://ofiwjoiwf/map2output/","s3://ofiwjoiwf/map3output/")
                .build();

        StepConfig step1 = StepConfig.builder()
                .name("mapreducer1")
                .hadoopJarStep(hadoopJarStepmapreducer1)
                .actionOnFailure("TERMINATE_JOB_FLOW")
                .build();

        StepConfig step2 = StepConfig.builder()
                .name("mapreducer2")
                .hadoopJarStep(hadoopJarStepmapreducer2)
                .actionOnFailure("TERMINATE_JOB_FLOW")
                .build();

        StepConfig step3 = StepConfig.builder()
                .name("mapreducer3")
                .hadoopJarStep(hadoopJarStepmapreducer3)
                .actionOnFailure("TERMINATE_JOB_FLOW")
                .build();


        PlacementType placement = PlacementType.builder()
                .availabilityZones("us-east-1a")
                .build();

        JobFlowInstancesConfig instances= JobFlowInstancesConfig.builder()
                .instanceCount(5)
                .masterInstanceType("m4.xlarge")
                .slaveInstanceType("m4.large")
                .ec2KeyName("vockey")
                .keepJobFlowAliveWhenNoSteps(false)
                .placement(placement)
                .build();

        RunJobFlowRequest runFlowRequest = RunJobFlowRequest.builder()
                .name("WordPrediction")
                .instances(instances)
                .steps(step1,step2,step3)
                .releaseLabel("emr-5.11.0")
                .logUri("s3://ofiwjoiwf/log/")
                .serviceRole("EMR_DefaultRole")
                .jobFlowRole("EMR_EC2_DefaultRole")
                .build();

        RunJobFlowResponse runJobFlowResult = emr.runJobFlow(runFlowRequest);

        String jobFlowId = runJobFlowResult.jobFlowId();
        System.out.println("Ran job flow with id: " + jobFlowId);
        String result = runJobFlowResult.toString();
        System.out.println(result);
    }
}
