import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Properties;

public class Algorithm{

    private ArrayList<Double[]> selectivityArr;

    private Double[] currentSels;

    private Properties props;

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

        // Read in config file values and store in a global Java Properties object
        props = new Properties();
        try{
            props.load(new FileInputStream(configFile));
        }
        catch(Exception e){
            e.printStackTrace();
        }
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
            /* System.out.println(binary); */
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
        double p = 1.0;
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
           //  System.out.println(A[i]); 
        }
    }

    // rename and put somewhere else later
    public double getCostNoBranch(ArrayList<Integer> indexes){
        double k = (double)indexes.size();

        //k*Cost.r + (k-1)*Cost.l + k*Cost.f + Cost.a;
        return k*Double.parseDouble(props.getProperty("r")) + (k-1.0)*Double.parseDouble(props.getProperty("l")) + k*Double.parseDouble(props.getProperty("f")) + Double.parseDouble(props.getProperty("a"));
    }

    public double getCostLogicalAnd(ArrayList<Integer> indexes){
        double k = (double)indexes.size();

        double p = getCombinedSelectivity(indexes);

        // q = p if p<.5, else q = 1-p
        double q = p;
        if (p >= .5){ q = 1-p; }

        //return k*Cost.r + (k-1)*Cost.l + k*Cost.f + Cost.m*q + p*Cost.a;
        return k*Double.parseDouble(props.getProperty("r")) + (k-1.0)*Double.parseDouble(props.getProperty("l")) + k*Double.parseDouble(props.getProperty("f")) + p*Double.parseDouble(props.getProperty("a")) + q*Double.parseDouble(props.getProperty("m")) + Double.parseDouble(props.getProperty("t"));
    }

    public double getFcost(Record r){
        double k = (double)r.num;
        //k*r + (k-1)*l + k*f + t
        return k*Double.parseDouble(props.getProperty("r")) + 
            (k-1.0)*Double.parseDouble(props.getProperty("l")) + 
            k*Double.parseDouble(props.getProperty("f"))+ 
            Double.parseDouble(props.getProperty("t"));

    }


    // Helper function to determine if two sets don't intersect
    //  Sets are represented as ints
    private boolean isDisjoint(int a, int b){
        // when a bitwise-OR b == a+B
        return ((a|b) == (a+b));
    }

    // Return the index of A[] of the item that has content n
    private int findIndexWithContent(Record[] A, int n){
        for (int i=0; i<A.length; i++){
            if (A[i].content == n){
                return i;
            }
        }
        return -1; // shouldn't happen
    }

    //Perform a traversal of the subtrees of A[S] after Step 2.  Currently preorder, though it's easy enough to change to inorder or postorder if we want later.
    private void traverseSubtrees(Record[] A, int index, ArrayList<Record> plan) {
        Record r = A[index];
        if(r.left < 0 && r.right < 0) {
            /* System.out.println("LEAF INDEX = " + index + " : " + r); */
            plan.add(A[index]);
        }
        else {
            /* System.out.println("INDEX = " + index + " : " + r); */
            if(r.left >= 0){
                // According to the algorithm, the left child is an &-term,
                // not a noBranch
                A[r.left].isNoBranch = false;

                traverseSubtrees(A, r.left, plan);
            }
            if(r.right >=0){
                traverseSubtrees(A, r.right, plan);
            }
        }
    }

    public void runAll(){
        //TODO: foreach index in selectivityArr, set currentSels to selectivityArr.get(index), and run();
        for (int i=0; i<selectivityArr.size(); i++){
            System.out.println("===================");
            currentSels = selectivityArr.get(i);
            run();
        }
    }

    public void run(){
        int k = currentSels.length;

        // Step 1
        //Generate all 2^k - 1 plans

        int num = (int)(Math.pow(2,k)) - 1;
        Record[] A = new Record[num];
        ArrayList<ArrayList<Integer>> combinations  = 
            generateAllCombinations(num);

        initializeRecords(A, combinations);

        Arrays.sort(A);

        // Step 2

        // outer loop: s = s1 = R child
        for (int i=0;i<A.length;i++){
            // inner loop: s' = s2 = L child
            for (int j=0;j<A.length;j++){
                int s1 = A[i].content;
                int s2 = A[j].content;
                if (isDisjoint(s1,s2)){
                    //If they're disjoint, we need to check their c and d-metrics against each other- note we probably should check these conditionals and make sure they're right

                    A[j].cmetric = (A[j].selectivity-1.0)/getFcost(A[j]);
                    A[i].cmetric = (A[i].selectivity-1.0)/getFcost(A[i]);
                    if (A[j].cmetric <= A[i].cmetric) {
                        // Do nothing
                    } else if (A[j].selectivity <= 0.5 && 
                            getFcost(A[j]) < getFcost(A[i])){
                        // Do nothing
                    } else {
                        //Calculate combined-plan cost from equation 1
                        /* double fcostE = A[j].cost; //fcost(E) */
                        double fcostE = getFcost(A[j]);

                        //Calculate m here- the cost of a branch misprediction; read from the configuration file
                        double m = Double.parseDouble(props.getProperty("m"));
                        //Calculate q here, min(1-selectivity of S', selectivity of S')
                        /* double q = Math.min(1.0-A[j].selectivity, A[j].selectivity); */
                        /* //Calculate p*C here, where p is selectivity of S', C is the cost of S) */
                        double p = A[j].selectivity;



                        //double p = A[j].selectivity * A[i].selectivity;
                        double q = Math.min(1.0-p, p);
                        double pc = p * A[i].cost;
                        double combinedCost = fcostE + m*q + pc;

                        //Update A[s' union s]
                        int unionIndex = findIndexWithContent(A,s1+s2);
                        /* System.out.println("new cost: "+ combinedCost + " old cost: " + A[unionIndex].cost); */
                        if (combinedCost < A[unionIndex].cost){
                            A[unionIndex].cost = combinedCost;
                            A[unionIndex].left = j;
                            A[unionIndex].right = i;
                            /* System.out.println("Updated:" + A[unionIndex]);  */
                        }
                    }
                }
            }
        }

        //At this point, A[S] should contain the optimal plan.  We can use this to reconstruct the actual order of terms and output appropriate C-code to the terminal.
        //TODO: Actually implement this
        Record optimalRecord = A[A.length-1];
        /* System.out.println("A[S] = " + optimalRecord); */


        // AL to hold plan in order
        ArrayList<Record> plan = new ArrayList<Record>();


        traverseSubtrees(A, A.length-1, plan);

        /* System.out.println("\n\nPLAN"); */
        /* for(Record r:plan){ */
        /*     System.out.println(r); */
        /* } */

        System.out.println(getCodeFromPlan(plan));
        System.out.println("Cost: " + optimalRecord.cost);

        
        /* if(optimalRecord.left < 0 && optimalRecord.right < 0) //Optimal plan is the initial no-branch or logical-and plan */
        /* { */
        /*      */
        /* } */
        /* else //Plan has child subtrees, we need to recurse through them until we hit leaf nodes in order to get the term order? */

        /* //TODO: Comment this out; debug code to display every single record and its location in the record array */
        /* System.out.println("Printing out all records..."); */
        /* for(int i =0; i< A.length; i++) */
        /*     System.out.println("Index = " + i + ": " + A[i]); */
    }

    public static void main(String[] args){
        try{
            Algorithm a = new Algorithm(args[0], args[1]); //take in query file and config file as command-line arguments as specified in assignment
            a.runAll();

        } catch (Exception e){
            e.printStackTrace();
        }

    }
    
    // Given int n, return tn[on[i]]
    private String getArrayString(int n){
        return "t" + n + "[o" + n + "[i]]";
    }

    public String getCodeFromPlan(ArrayList<Record> plan){
        String andCode = "";
        String noBranchCode = "";
        for (Record r: plan){
            String s = "";
            ArrayList<Integer> indexes = getSelIndexesFromContent(r.content);
            for (int i=0; i<indexes.size(); i++){
                int selIndex = indexes.get(i).intValue() + 1;

                // If not first item, add an &
                if (s.length() > 0){ s += " & ";}

                s += getArrayString(selIndex);
            }

            if (r.isNoBranch){
                if (noBranchCode.length() > 0){ noBranchCode += " & ";}
                noBranchCode += s;
            } else{
                if (andCode.length() > 0){ andCode += " && ";}
                andCode += "(" + s + ")";
            }
        }
        String conditional = "if(" + andCode + "){\n";

        String inner = "\tanswer[j] = i;\n\tj += (" + noBranchCode + ");";

        // If there is noBranchCode, then use the default answer[j++] = i
        if (noBranchCode.length() == 0){
            inner = "answer[j++] = i;";
        }

        if (andCode.length() == 0){
            // If everything is noBranch, don't need conditional
            // Also, get rid of tabs
            return inner.replace("\t","");
        } else{
            return conditional + inner + "\n}";
        }
            
    }

    // Helper method, get selectivity indexes from a content, 
    // which is an int (which is just a bitmap)
    private ArrayList<Integer> getSelIndexesFromContent(int content){
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        String binary = Integer.toBinaryString(content);
        for (int i=0;i<binary.length();i++){
            if (binary.charAt(binary.length()-1-i) == '1'){
                indexes.add(new Integer(i));
            }
        }
        return indexes;
    }

}

