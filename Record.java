public class Record implements Comparable{

    private int num;
    private double selectivity;
    
    // isNoBranch, left, and right are public for easy access/modification
    public boolean isNoBranch;
    public Record left, right;

    // An int that, when converted to binary and read backwards, acts as a
    // bitmap that shows which selectivities are used.
    public int content;
    public Record(int n, double p, boolean b, Record l, Record r){
        num = n;
        selectivity = p;
        isNoBranch = b;
        left = l;
        right = r;


        // default 0
        content = 0;
    }
        

    public String toString(){
        return "Record: n = " + num + " p = " + selectivity +
            " b = " + isNoBranch + " content = " + content;
    }

    // For sorting in increasing order: sorted by num (number of
    // selectivities), low to high
    public int compareTo(Object other){
        return this.num - ((Record)other).num;
    }
}
