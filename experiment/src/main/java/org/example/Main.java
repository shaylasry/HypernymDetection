package org.example;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.trees.DecisionStump;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception{
        String inputFilename = "part-r-00000";
        String outputFilename = "dataset.arff";
        String relationName = "hypernym_demo";
        String classAttributeName = "hypernym";
        String[] attributeNames = new String[] {"noun1", "noun2"};

        try (BufferedReader br = new BufferedReader(new FileReader(inputFilename));
             BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilename))) {
            int vectorSize = 0;
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length > 3) {
                    vectorSize = parts.length - 3;
                    System.out.println(vectorSize);
                    break;
                }
            }
            if (vectorSize == 0) {
                System.out.println("Error: could not determine vector size.");
                return;
            }
            bw.write("@relation " + relationName + "\n");
            for (String attributeName : attributeNames) {
                bw.write("@attribute " + attributeName + " string\n");
            }
            for (int i = 1; i <= vectorSize; i++) {
                bw.write("@attribute numeric" + i + " numeric\n");
            }
            bw.write("@attribute " + classAttributeName + " {True,False}\n");
            bw.write("@data\n");
            BufferedReader br2 = new BufferedReader(new FileReader(inputFilename));
            while ((line = br2.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 3) {
                    continue;
                }
                for (int i = 0; i < attributeNames.length; i++) {
                    bw.write(parts[i] + ",");
                }
                for (int i = 2; i < parts.length - 1; i++) {
                    bw.write(parts[i] + ",");
                }
                bw.write(parts[parts.length - 1] + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Instances data1 = ConverterUtils.DataSource.read("dataset.arff");

        // remove the first two attributes
        data1.deleteAttributeAt(1);
        data1.deleteAttributeAt(0);

        // output modified instances to a new ARFF file
        BufferedWriter writer = new BufferedWriter(new FileWriter("newdataset.arff"));
        writer.write(data1.toString());
        writer.flush();
        writer.close();


        // Load the dataset from the ARFF file
        BufferedReader reader = new BufferedReader(new FileReader("newdataset.arff"));
        Instances data = new Instances(reader);
        reader.close();

        data.setClassIndex(data.numAttributes() - 1);


        Classifier classifier = new DecisionStump();

        classifier.buildClassifier(data);

        Evaluation eval = new Evaluation(data);
        eval.crossValidateModel(classifier,data,10,new Random(42));
        System.out.println("=== Evaluation Results ===");
//        System.out.println("Accuracy: " + eval.pctCorrect() + "%");
        System.out.println(eval.toSummaryString("Results",false));
        System.out.println("Precision: " + eval.precision(0));
        System.out.println("Recall: " + eval.recall(0));
        System.out.println("F1 Score: " + eval.fMeasure(0));
        ArrayList<Prediction> preds = eval.predictions();
        ArrayList<Integer> truepositive = new ArrayList<>();
        ArrayList<Integer> truenegative = new ArrayList<>();
        ArrayList<Integer> falsenegative = new ArrayList<>();
        ArrayList<Integer> falsepositive = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader("part-r-00000"));
        String line;
        int count = 0;
        HashMap<Integer,String> map = new HashMap<>();
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            String save = parts[0]+"/"+parts[1];
            map.put(count,save);
            count++;
        }


        for(int i = 0; i<preds.size();i++){
            Prediction pred= preds.get(i);
            if(pred.actual()==1.0 && pred.predicted()==1.0){
                //True Positive
                truepositive.add(i);
            }
            if(pred.actual()==0.0 && pred.predicted()==0.0){
                //True negative
                truenegative.add(i);
            }
            if(pred.actual()==1.0 && pred.predicted()==0.0){
                //False negative
                falsenegative.add(i);
            }
            if(pred.actual()==0.0 && pred.predicted()==1.0){
                //False positive
                falsepositive.add(i);
            }
        }

        Random random = new Random();

        System.out.println("TRUE POSITIVE:");
        for (int i = 0; i < truepositive.size(); i++) {
            if (i > 4) {
                break;
            }
            int randomIndex = random.nextInt(truepositive.size());
            System.out.println(map.get(truepositive.get(randomIndex)));
        }
        System.out.println("TRUE NEGATIVE:");
        for (int i = 0; i < truenegative.size(); i++) {
            if (i > 4) {
                break;
            }
            int randomIndex = random.nextInt(truenegative.size());
            System.out.println(map.get(truenegative.get(randomIndex)));
        }
        System.out.println("FALSE NEGATIVE:");
        for (int i = 0; i < falsenegative.size(); i++) {
            if (i > 4) {
                break;
            }
            int randomIndex = random.nextInt(falsenegative.size());
            System.out.println(map.get(falsenegative.get(randomIndex)));
        }
        System.out.println("FALSE POSITIVE:");
        for (int i = 0; i < falsepositive.size(); i++) {
            if (i > 4) {
                break;
            }
            int randomIndex = random.nextInt(falsepositive.size());
            System.out.println(map.get(falsepositive.get(randomIndex)));
        }
    }
}
