import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Algorithm{

    private ArrayList<Double[]> selectivityArr;

    private Double[] currentSels;

    // Read in the query file and store in selectivityArr
    private ArrayList<Double[]> parseQueryFile(String queryFile)
        throws java.io.FileNotFoundException 
    {

        ArrayList<Double[]> result = new ArrayList<Double[]>();
        File file = new File(queryFile);
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()){
            String line = scanner.nextLine();
            String[] strArr = line.split("\\s+");

            // Convert string array to double array
            Double[] doubleArr = new Double[strArr.length];
            for (int i=0; i<strArr.length; i++){
                doubleArr[i] = Double.parseDouble(strArr[i]);
            }

            result.add(doubleArr);
        }
        return result;
    }

    public Algorithm(String queryFile, String configFile) 
        throws FileNotFoundException
    {
        selectivityArr = parseQueryFile(queryFile);

        // Use configFile values instead of Cost class hack
    }


    // Creates all combinations
    // Returns an AL of items,
    //  each of which is an AL of indexes which point to selectivities (double)
    //  (The indexes are Integers)
    private ArrayList<ArrayList<Integer>> generateAllCombinations(int n){
        ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>(n);

        for (int i=1;i<=n;i++){
            ArrayList<Integer> combination = new ArrayList<Integer>();
            // Convert int to binary representation
            String binary = Integer.toBinaryString(i);
            System.out.println(binary);
            // Traverse binary string backwards
            for (int j = 0; j<binary.length(); j++){
                char c = binary.charAt(binary.length()-1-j);
                if (c=='1'){
                    /* Double d = Double.parseDouble(currentSels[j]); */


                    // lets try j (index) instead of actual double
                    combination.add(new Integer(j));
                }

                
            }

            /* System.out.println("DEBUG"); */
            /* for (Integer d2: combination){ */
            /*     System.out.println(d2); */
            /* } */

            result.add(combination);
        }
        return result;
    }

    /**
     * Returns combined selectivity given an AL of indexes (to selectivities in
     * currentSels)
     */
    private double getCombinedSelectivity(ArrayList<Integer> indexes){
        double p = 1;
        for (Integer i: indexes){
            double tempP = currentSels[i.intValue()];
            p *= tempP;
        }
        return p;
    }

    public void initializeRecords(Record[] A, 
            ArrayList<ArrayList<Integer>> combinations)
    {
        // Initialize with all &-term plans.
        for (int i=0; i<combinations.size(); i++){
            ArrayList<Integer> indexes = combinations.get(i);

            double costLogicalAnd = getCostLogicalAnd(indexes);
            double costNoBranch = getCostNoBranch(indexes);

            System.out.println("logical: " + costLogicalAnd);
            System.out.println("nobranch: " + costNoBranch);

            boolean isNoBranch = false;
            double cost = costLogicalAnd;
            if (costNoBranch < costLogicalAnd){
                cost = costNoBranch;
                isNoBranch = true;
            }

            double p = getCombinedSelectivity(indexes);
            // Record(n,p,b,c,l,r)
            A[i] = new Record(indexes.size(),p,isNoBranch,cost,-1,-1);

            A[i].content = i+1;
            System.out.println(A[i]);
        }
    }

    // rename and put somewhere else later
    public double getCostNoBranch(ArrayList<Integer> indexes){
        int k = indexes.size();

        return k*Cost.r + (k-1)*Cost.l + k*Cost.f + Cost.a;
    }

    public double getCostLogicalAnd(ArrayList<Integer> indexes){
        int k = indexes.size();

        double p = getCombinedSelectivity(indexes);

        // q = p if p<.5, else q = 1-p
        double q = p;
        if (p >= .5){ q = 1-p; }

        return k*Cost.r + (k-1)*Cost.l + k*Cost.f + Cost.m*q + p*Cost.a;
    }

    public void run(){

        // just get second scenario for now for testing
        currentSels = selectivityArr.get(2);
        int k = currentSels.length;

        
        //Generate all 2^k - 1 plans

        int num = (int)(Math.pow(2,k)) - 1;
        Record[] A = new Record[num];
        ArrayList<ArrayList<Integer>> combinations  = 
            generateAllCombinations(num);

        initializeRecords(A, combinations);

        Arrays.sort(A);
        System.out.println("SORTED!");
        for (Record r: A){
            System.out.println(r);
        }
    }

    public static void main(String[] args){
        try{

            Algorithm a = new Algorithm("sample_query.txt","");
            a.run();

        } catch (Exception e){
            e.printStackTrace();
        }

    }

}

