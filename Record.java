/**
Sean Yeh, Michael Eng
COMS 4112 Project 2
Record.java- Data class for an individual plan and its pertinent characteristics
**/

public class Record implements Comparable{

    // public fields for easy access/modification
    public int num;
    public double selectivity;
    
    public boolean isNoBranch;
    public double cost;
    public int left, right;
    // An int that, when converted to binary and read backwards, acts as a
    // bitmap that shows which selectivities are used.
    public int content;
    // The c-metric needs to be calculated and stored when the Record is constructed, as we can calculate it from cost and selectivity alone.
    // d-metric is just the cost, so we can leave it at that without adding anything new internally.
    public double cmetric;

    public Record(int n, double p, boolean b, double c, int l, int r){
        num = n;
        selectivity = p;
        isNoBranch = b;
        cost = c;
        left = l;
        right = r;

        cmetric = (p-1.0)/cost;

        // default 0
        content = 0;
    }
        

    public String toString(){
        return "Record: n = " + num + " p = " + selectivity +
            " b = " + isNoBranch + " content = " + content +
            " cost = " + cost + " c-metric = " + cmetric +
            " l = " + left + " r = " + right;
    }

    // For sorting in increasing order: sorted by num (number of
    // selectivities), low to high
    public int compareTo(Object other){
        return this.num - ((Record)other).num;
    }
}
