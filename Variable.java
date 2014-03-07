
import java.util.ArrayList;

/*
 * Created on 23.10.2004
 * Defines variable and the interface to work with it
 */

/**
 * @author shaman
 *  
 */
public class Variable {

    /*
     * The array of the variables names
     */
    private ArrayList Variables;

    /*
     * The array of the variables values
     */
    private ArrayList Values;

    /*
     * True, if last valu was found
     */
    public boolean last_val;

    public Variable() {
        try {
            Variables = new ArrayList();
            Values = new ArrayList();
            last_val = true;
        } catch (Exception e) {
            System.out.print("<EXCEPTION message=Variable()></EXCEPTION>");
            System.out.println(e);
        }
    }

    public double GetValue(String name) {
        try {
            last_val = false;
            double val = 0;
            for (int j = 0; j < Variables.size(); j++) {
                /** If label names matches */
                if (name.compareTo((String) Variables.get(j)) == 0) {
                    val = Double.valueOf((String) Values.get(j)).doubleValue();
                    last_val = true;
                }
            }

            return val;

        } catch (Exception e) {
            System.out.print("<EXCEPTION message=Variable()></EXCEPTION>");
            System.out.println(e);
            last_val = false;
            return 0;
        }
    }

    public String s_GetValue(String name) {
        try {
            last_val = false;
            String val;
            val = new String();
            for (int j = 0; j < Variables.size(); j++) {
                /** If label names matches */
                if (name.compareTo((String) Variables.get(j)) == 0) {
                    val = (String) Values.get(j);
                    last_val = true;
                }
            }

            return val;

        } catch (Exception e) {
            System.out.print("<EXCEPTION message=Variable()></EXCEPTION>");
            System.out.println(e);
            last_val = false;
            return "0";
        }
    }

    public void AddValue(String name, double value) {
        try {
            Variables.add(name);
            String tmp;
            tmp = new String();
            tmp = String.valueOf(value);
            Values.add(tmp);
        } catch (Exception e) {
            System.out
                    .print("<EXCEPTION message=SetValue(String name, double value)></EXCEPTION>");
            System.out.println(e);
        }

    }

    public void SetValue(String name, double value) {
        try {
            last_val = false;
            String tmp;
            tmp = new String();
            tmp = String.valueOf(value);
            for (int j = 0; j < Variables.size(); j++) {
                /** If label names matches */
                if (name.compareTo((String) Variables.get(j)) == 0) {
                    Values.set(j, tmp);
                    last_val = true;
                }
            }

        } catch (Exception e) {
            System.out
                    .print("<EXCEPTION message=SetValue(String name, double value)></EXCEPTION>");
            System.out.println(e);
            last_val = false;
        }

    }

    public int ReturnCount() {
        try {
            return Variables.size();
        } catch (Exception e) {
            System.out.print("<EXCEPTION message=ReturnCount() ></EXCEPTION>");
            System.out.println(e);
            return -1;
        }
    }

    public String Get_Name(int i) {
        try {
            return (String) Variables.get(i);
        } catch (Exception e) {
            System.out
                    .print("<EXCEPTION message= String Get_Name(int i) ></EXCEPTION>");
            System.out.println(e);
            return "__NULL";
        }
    }

    public double Get_Val(int i) {
        try {
            return Double.valueOf((String) Values.get(i)).doubleValue();
        } catch (Exception e) {
            System.out
                    .print("<EXCEPTION message= String Get_Val(int i) ></EXCEPTION>");
            System.out.println(e);
            return 0.0;

        }
    }
}
