public class Record{

    private int num;
    private double selectivity;
    
    // isNoBranch, left, and right are public for easy access/modification
    public boolean isNoBranch;
    public Record left, right;
    public Record(int n, double p, boolean b, Record l, Record r){
        num = n;
        selectivity = p;
        isNoBranch = b;
        left = l;
        right = r;
    }
        

    public String toString(){
        return "Record: n = " + num + " p = " + selectivity +
            " b = " + isNoBranch;
    }
}
