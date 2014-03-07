
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Created on 13.02.2005
 *
 */

/**
 * @author shaman
 *  
 */
public class Args {
    /*
     * Amount of arguments. -1 something is wrong
     */
    public int argc;

    /*
     * Array of arguments
     */
    public String argv[];

    /**
     * Regex stuff
     */

    private static Pattern pattern;

    private static Matcher matcher;

    /*
     * if 1 - debug mode enabeled
     */
    private int debug;

    public Args() {
        debug = 0;
        argc = 0;
    }

    /*
     * input - string to parse operator - operator mask to remove
     */
    public void process(String input, String operator) {
        try {

            /*
             * We should: 1 - determine amount of arguments 2 - extract them all
             */

            int start;
            int end;
            int index;
            /*
             * Removing the operator
             */

            start = 0;
            end = 0;
            index = 0;
            argc = 0;

            pattern = Pattern.compile(operator);
            matcher = pattern.matcher(input);
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher = "
                            + matcher.group() + " start =" + matcher.start()
                            + " end = " + matcher.end() + "</PARSER>"
                            + "></DEBUG>");
                }
                start = matcher.start();
                end = matcher.end();
            }

            input = input.substring(end, input.length());

            /*
             * Counting arguments by counting ","'s
             */

            for (int i = 0; i < input.length(); i++) {
                if (input.charAt(i) == ',') {
                    argc++;
                }
            }

            if (debug == 1)
                System.out.println("<DEBUG argc = " + argc + "/>");

            argv = new String[argc + 1];
            for (int i = 0; i < argc + 1; i++) {
                argv[i] = new String();
            }

            /*
             * Extracting parameters
             */
            int j;
            j = 0;
            String tmp;
            tmp = new String();
            for (int i = 0; i < input.length(); i++) {
                if (input.charAt(i) != ' ' && input.charAt(i) != ','
                        && input.charAt(i) != '\t') {

                    tmp = String.valueOf(input.charAt(i)).toString();
                    argv[j] = argv[j].concat(tmp);
                } else if (input.charAt(i) == ',')
                    j++;
            }
            argc = argc +1;
        } catch (Exception e) {
            System.out.println("<ERROR exception = \"" + e + "\" />");
            System.out.println("<ERROR message= \"parsing error \"  string= \""
                    + input + "\" />");
            argc = -1;
        }

    }
}
