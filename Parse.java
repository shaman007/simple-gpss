
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * parse.java
 * 
 * GPSS courcecode parser = GPSS frontend Created on 30 Januarry 2004, 18:55
 * 
 * @author Andrey Bondarenko @e-mail bondarenko007@aport2000.ru @license GPL
 */
/*
 * Output XML format for code parcer
 * 
 * <INFO> - information messages <ERROR> - error messages.
 * 
 * @author shaman
 */
public class Parse {
    /*
     * GPSS source file name
     */
    private String input_file;

    /**
     * Current string of GPSS source
     */
    private String current;

    /**
     * Known blocks array 1 - block name 2 - is block allowed at this place of
     * sourcecode, block code
     */
    private String known_blocks[][];

    /**
     * Known blocks count
     */
    private int known_blocks_count;

    /**
     * Known maps count
     */
    private int known_maps_count;

    /**
     * Known maps array 1 - map name 2 - is map allowed at this place of
     * sourcecode, map code
     */
    private String known_maps[][];

    /**
     * Source code array
     */
    private ArrayList source;

    /**
     * Array list of labels
     */
    private ArrayList labels;

    /**
     * Label mask
     */
    private String label_mask;

    /**
     * Comment mask
     */
    private String comment_mask;

    /**
     * Empty string mask
     */
    private String empty_mask;

    /**
     * Real line counter
     */
    private int real;

    /**
     * Regex stuff
     */
    private static Pattern pattern;

    private static Matcher matcher;

    /**
     * Parser verdict
     */
    public int errorcode;

    public String error;

    private int debug;

    /**
     * System stuff
     */
    private int Queue_amount;

    private int Fac_amount;

    private int Loop_amount;

    private int SQueue_amount;

    /**
     * Assebmles
     */
    private int Assembles_amount;

    private int Current_Assemble;

    private Time tmr;

    private Queue que;

    private Schema sch;

    private int start;

    private int end;

    private boolean found;

    private int cont;

    /** Array of queue names for Facilities and Storages */
    private String que_arr[];

    private String sque_arr[];

    /*
     * Tabs amount
     */
    private int Tabs_amount;

    private int curr_tab;

    /*
     * Argument parser
     */
    private Args arg;

    /**
     * Creates a new instance of parse in - name of GPSS source file name sch -
     * schema instance to fill dbg - if 1, show verbose output
     */
    public Parse(String in, int dbg) {
        try {
            arg = new Args();
            que = new Queue();
            tmr = new Time();
            sch = new Schema(1);
            Queue_amount = 0;
            SQueue_amount = 0;
            Fac_amount = 0;
            Loop_amount = 0;
            Tabs_amount = -1;
            Assembles_amount = 0;
            Current_Assemble = 0;
            cont = 0;
            found = false;
            errorcode = 0;
            error = "OK";
            debug = dbg;
            real = 0;
            curr_tab = 0;
            /**
             * Reading source to buffer
             */
            System.out
                    .println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
            try {
                input_file = new String();
                input_file = in;
                source = new ArrayList();
                labels = new ArrayList();
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        new FileInputStream(input_file)));
                String line = "";
                while ((line = br.readLine()) != null) {
                    source.add(line);
                }
                br.close();
            } catch (Exception e) {
                System.out.println("<ERROR> <SOURCE href=\"FALTURE:" + e
                        + "\"></SOURCE> </ERROR>");
            }
            System.out.println("<INFO> <SOURCE href=\"" + input_file
                    + "\"></SOURCE> </INFO>");
            /**
             * Language definitions
             */
            label_mask = "^[ \t]*[a-zA-Z]+[0-9]*:";
            comment_mask = "[;#].*$";
            empty_mask = "^[ \t]*$";
            known_blocks_count = 19;
            known_maps_count = 4;
            known_blocks = new String[known_blocks_count][3];
            known_blocks[0][0] = "TERMINATE";
            known_blocks[0][1] = "1";
            known_blocks[0][2] = "0";
            known_blocks[1][0] = "QUEUE";
            known_blocks[1][1] = "1";
            known_blocks[1][2] = "1";
            known_blocks[2][0] = "DEPART";
            known_blocks[2][1] = "1";
            known_blocks[2][2] = "2";
            known_blocks[3][0] = "SEIZE";
            known_blocks[3][1] = "1";
            known_blocks[3][2] = "3";
            known_blocks[4][0] = "RELEASE";
            known_blocks[4][1] = "1";
            known_blocks[4][2] = "4";
            known_blocks[5][0] = "ADVANCE";
            known_blocks[5][1] = "1";
            known_blocks[5][2] = "5";
            known_blocks[6][0] = "SPLIT";
            known_blocks[6][1] = "1";
            known_blocks[6][2] = "6";
            known_blocks[7][0] = "PRIORITY";
            known_blocks[7][1] = "1";
            known_blocks[7][2] = "7";
            known_blocks[8][0] = "BUFFER";
            known_blocks[8][1] = "1";
            known_blocks[8][2] = "8";
            known_blocks[9][0] = "ASSEMBLE";
            known_blocks[9][1] = "1";
            known_blocks[9][2] = "9";
            known_blocks[10][0] = "LOOP";
            known_blocks[10][1] = "1";
            known_blocks[10][2] = "10";
            known_blocks[11][0] = "TRANSFER";
            known_blocks[11][1] = "1";
            known_blocks[11][2] = "11";
            known_blocks[12][0] = "ENTER";
            known_blocks[12][1] = "1";
            known_blocks[12][2] = "12";
            known_blocks[13][0] = "LEAVE";
            known_blocks[13][1] = "1";
            known_blocks[13][2] = "13";
            known_blocks[14][0] = "TEST";
            known_blocks[14][1] = "1";
            known_blocks[14][2] = "14";
            known_blocks[15][0] = "ASSIGN";
            known_blocks[15][1] = "1";
            known_blocks[15][2] = "15";
            known_blocks[16][0] = "SAVEVALUE";
            known_blocks[16][1] = "1";
            known_blocks[16][2] = "16";
            known_blocks[17][0] = "TABULATE";
            known_blocks[17][1] = "1";
            known_blocks[17][2] = "17";
            known_blocks[18][0] = "VARIABLE";
            known_blocks[18][1] = "1";
            known_blocks[18][2] = "18";
            /*
             * Maps definitions
             */
            known_maps = new String[known_maps_count][3];
            known_maps[0][0] = "SIMULATE";
            known_maps[0][1] = "1";
            known_maps[0][2] = "0";
            known_maps[1][0] = "START";
            known_maps[1][1] = "1";
            known_maps[1][2] = "1";
            known_maps[2][0] = "GENERATE";
            known_maps[2][1] = "1";
            known_maps[2][2] = "2";
            known_maps[3][0] = "TABLE";
            known_maps[3][1] = "1";
            known_maps[3][2] = "3";
            /**
             * Start parsing
             */
            for (int i = 0; i < source.size(); i++) {
                cont = 1;
                int start = 0;
                int end = 0;
                current = (String) source.get(i);
                /**
                 * 1 - remove comment
                 */
                pattern = Pattern.compile(comment_mask);
                matcher = pattern.matcher(current);
                while (matcher.find()) {
                    if (debug == 1) {
                        System.out.println("<DEBUG> <PARSER matcher="
                                + matcher.group() + " start=" + matcher.start()
                                + " end= " + matcher.end() + "></PARSER>"
                                + "</DEBUG>");
                    }
                    found = true;
                    start = matcher.start();
                    end = matcher.end();
                }
                if (found) {
                    String tmp;
                    tmp = new String();
                    tmp = current.substring(0, start);
                    current = tmp;
                    if (debug == 1) {
                        System.out.println("<INFO>Comment removed" + current
                                + "</INFO>");
                    }
                    found = false;
                }
                /**
                 * 2 - Remove empty lines
                 */
                pattern = Pattern.compile(empty_mask);
                matcher = pattern.matcher(current);
                while (matcher.find()) {
                    if (debug == 1) {
                        System.out.println("<DEBUG> <PARSER matcher="
                                + matcher.group() + " start=" + matcher.start()
                                + " end= " + matcher.end() + "></PARSER>"
                                + "></DEBUG>");
                    }
                    found = true;
                    start = matcher.start();
                    end = matcher.end();
                }
                if (found) {
                    if (debug == 1) {
                        System.out.println("<INFO>Empty string found</INFO>");
                    }
                    cont = 0;
                    found = false;
                } else {
                    real++;
                }
                /**
                 * Building labels array
                 */
                pattern = Pattern.compile(label_mask);
                matcher = pattern.matcher(current);
                while (matcher.find()) {
                    if (debug == 1) {
                        System.out.println("<DEBUG> <PARSER matcher="
                                + matcher.group() + " start=" + matcher.start()
                                + " end= " + matcher.end() + "></PARSER>"
                                + "</DEBUG>");
                    }
                    found = true;
                    start = matcher.start();
                    end = matcher.end();
                }
                if (found) {
                    Label_parser(i, start, end, current);
                    String tmp;
                    tmp = new String();
                    tmp = current.substring(end, current.length());
                    current = tmp;
                }
                found = false;
                /**
                 * 3 - Parsing really valueble content
                 */
                if (cont == 1) {
                    /**
                     * Maps parsing
                     */
                    for (int k = 0; k < known_maps_count; k++) {
                        pattern = Pattern.compile(known_maps[k][0]);
                        matcher = pattern.matcher(current);
                        while (matcher.find()) {
                            if (debug == 1) {
                                System.out.println("<DEBUG> <PARSER matcher="
                                        + matcher.group() + " start="
                                        + matcher.start() + " end= "
                                        + matcher.end() + "></PARSER>"
                                        + "</DEBUG>");
                            }
                            found = true;
                            start = matcher.start();
                            end = matcher.end();
                        }
                        if (found) {
                            if (debug == 1) {
                                System.out.println("");
                            }
                            cont = 1;
                            switch (Integer.valueOf(known_maps[k][2])
                                    .intValue()) {
                            case 0:
                                if (known_maps[0][1] == "1") {
                                    //	known_maps[2][1] = "1";//Enable
                                    // GENERATE
                                    //known_maps[0][1] = "0";//Disable
                                    // SIMULATE
                                    Simulate_parser(current);
                                } else {
                                    System.out.println("<ERROR line=" + (i + 1)
                                            + " >Mispacesed SIMULATE</ERROR>");
                                    errorcode = 1;
                                }
                                break;
                            case 1:
                                /**
                                 * START can be runed any time anywhere, but not
                                 * before simulate
                                 */
                                if (known_maps[1][1] == "1") {
                                    Start_parser(current);
                                } else {
                                    System.out.println("<ERROR line=" + (i + 1)
                                            + ">Misplaced START</ERROR>");
                                    errorcode = 1;
                                }
                                break;
                            case 2:
                                if (known_maps[2][1] == "1") {
                                    for (int ii = 0; ii < known_maps_count; ii++) {
                                        known_maps[ii][1] = "1";
                                    }
                                    for (int ii = 0; ii < known_blocks_count; ii++) {
                                        known_blocks[ii][1] = "1";
                                    }
                                    //known_maps[0][1] = "0";//Disable
                                    // SIMULATE
                                    //known_blocks[2][1] = "0";//OFF
                                    // DEPART
                                    //known_blocks[4][1] = "0";//OFF
                                    // RELEASE
                                    Generate_parser(current);
                                } else {
                                    System.out
                                            .println("<ERROR line="
                                                    + (i + 1)
                                                    + ">GENERATE before SIMULATE</ERROR>");
                                    errorcode = 1;
                                }
                                break;
                            case 3:
                                if (known_maps[3][1] == "1") {
                                    Table_parser(current);
                                } else {
                                    System.out.println("<ERROR line=" + (i + 1)
                                            + ">Misplased TABLE</ERROR>");
                                    errorcode = 1;
                                }
                                break;
                            default:
                                System.out.println("<ERROR line=" + (i + 1)
                                        + ">Unknown map type</ERROR>");
                            }
                            found = false;
                        }
                    }
                    /**
                     * Blocks parsing
                     */
                    for (int l = 0; l < known_blocks_count; l++) {
                        pattern = Pattern.compile(known_blocks[l][0]);
                        matcher = pattern.matcher(current);
                        while (matcher.find()) {
                            if (debug == 1) {
                                System.out.println("<DEBUG> <PARSER matcher="
                                        + matcher.group() + " start="
                                        + matcher.start() + " end= "
                                        + matcher.end() + "</PARSER>"
                                        + "></DEBUG>");
                            }
                            found = true;
                            start = matcher.start();
                            end = matcher.end();
                        }
                        if (found) {
                            if (debug == 1) {
                                System.out.println("");
                            }
                            cont = 1;
                            switch (Integer.valueOf(known_blocks[l][2])
                                    .intValue()) {
                            case 0:
                                if (known_blocks[0][1] == "1") {
                                    Terminate_parser(current);
                                } else {
                                    System.out.println("<ERROR line=" + (i + 1)
                                            + ">Misplaced TERMINATE</ERROR>");
                                    errorcode = 1;
                                }
                                break;
                            case 1:
                                if (known_blocks[1][1] == "1") {
                                    Queue_parser(current);
                                    //known_blocks[1][1] = "0";//OFF
                                    // QUEUE
                                    //known_blocks[2][1] = "1";//ON
                                    // DEPART
                                } else {
                                    System.out.println("<ERROR line=" + (i + 1)
                                            + ">Misplaced QUEUE</ERROR>");
                                    errorcode = 1;
                                }
                                break;
                            case 2:
                                if (known_blocks[2][1] == "1") {
                                    Depart_parser(current);
                                    //known_blocks[1][1] = "1";//ON
                                    // QUEUE
                                    //known_blocks[2][1] = "0";//OFF
                                    // DEPART
                                } else {
                                    System.out.println("<ERROR line=" + (i + 1)
                                            + ">Misplaced DEPART</ERROR>");
                                    errorcode = 1;
                                }
                                break;
                            case 3:
                                if (known_blocks[3][1] == "1") {
                                    Seize_parser(current);
                                    //known_blocks[3][1] = "0";//OFF
                                    // SEIZE
                                    //known_blocks[4][1] = "1";//ON
                                    // RELEASE
                                } else {
                                    System.out.println("<ERROR line=" + (i + 1)
                                            + " >Misplaced SEIZE</ERROR>");
                                    errorcode = 1;
                                }
                                break;
                            case 4:
                                if (known_blocks[4][1] == "1") {
                                    Release_parser(current);
                                    //known_blocks[3][1] = "1";//ON
                                    // SEIZE
                                    //known_blocks[4][1] = "0";//OFF
                                    // RELEASE
                                } else {
                                    System.out
                                            .println("<ERROR line="
                                                    + (i + 1)
                                                    + " >RELEASE without SEIZE</ERROR>");
                                    errorcode = 1;
                                }
                                break;
                            case 5:
                                if (known_blocks[5][1] == "1") {
                                    Advance_parser(current);
                                } else {
                                    System.out.println("<ERROR line=" + (i + 1)
                                            + ">Misplaced ADVANCE</ERROR>");
                                    errorcode = 1;
                                }
                                break;
                            case 6:
                                if (known_blocks[6][1] == "1") {
                                    Split_parser(current);
                                } else {
                                    System.out.println("<ERROR line=" + (i + 1)
                                            + ">Misplaced SPLIT</ERROR>");
                                    errorcode = 1;
                                }
                                break;
                            case 7:
                                if (known_blocks[7][1] == "1") {
                                    Priority_parser(current);
                                } else {
                                    System.out.println("<ERROR line=" + (i + 1)
                                            + ">Misplaced PRIORITY></ERROR>");
                                    errorcode = 1;
                                }
                                break;
                            case 8:
                                if (known_blocks[8][1] == "1") {
                                    Buffer_parser(current);
                                } else {
                                    System.out.println("<ERROR line=" + (i + 1)
                                            + ">Misplaced BUFFER </ERROR>");
                                    errorcode = 1;
                                }
                                break;
                            case 9:
                                if (known_blocks[9][1] == "1") {
                                    Assemble_parser(current);
                                } else {
                                    System.out.println("<ERROR line=" + (i + 1)
                                            + "Misplaced ASSEMBLE </ERROR>");
                                    errorcode = 1;
                                }
                                break;
                            case 10:
                                if (known_blocks[10][1] == "1") {
                                    Loop_parser(current);
                                } else {
                                    System.out.println("<ERROR line=" + (i + 1)
                                            + ">Misplaced LOOP <?/ERROR>");
                                    errorcode = 1;
                                }
                                break;
                            case 11:
                                if (known_blocks[11][1] == "1") {
                                    Transfer_parser(current);
                                } else {
                                    System.out.println("<ERROR line=" + (i + 1)
                                            + ">Misplaced TRANSFER</ERROR>");
                                    errorcode = 1;
                                }
                                break;
                            case 12:
                                if (known_blocks[12][1] == "1") {
                                    Enter_parser(current);
                                    //known_blocks[12][1] = "0";//OFF
                                    // ENTER
                                    //known_blocks[13][1] = "1";//ON
                                    // LEAVE
                                } else {
                                    System.out.println("<ERROR line=" + (i + 1)
                                            + ">Misplaced ENTER</ERROR>");
                                    errorcode = 1;
                                }
                                break;
                            case 13:
                                if (known_blocks[13][1] == "1") {
                                    Leave_parser(current);
                                    //known_blocks[12][1] = "1";//ON
                                    // ENTER
                                    //known_blocks[13][1] = "0";//OFF
                                    // LEAVE
                                } else {
                                    System.out.println("<ERROR line=" + (i + 1)
                                            + ">LEAVE without ENTER</ERROR>");
                                    errorcode = 1;
                                }
                                break;
                            case 14:
                                if (known_blocks[14][1] == "1") {
                                    Test_parser(current);
                                } else {
                                    System.out.println("<ERROR line=" + (i + 1)
                                            + ">Misplaced TEST</ERROR>");
                                    errorcode = 1;
                                }
                                break;
                            case 15:
                                if (known_blocks[14][1] == "1") {
                                    Assign_parser(current);
                                } else {
                                    System.out.println("<ERROR line=" + (i + 1)
                                            + "Misplaced ASSIGN</ERROR>");
                                    errorcode = 1;
                                }
                                break;
                            case 16:
                                if (known_blocks[14][1] == "1") {
                                    Savevalue_parser(current);
                                } else {
                                    System.out.println("<ERROR line=" + (i + 1)
                                            + ">Misplaced SAVEVALUE</ERROR>");
                                    errorcode = 1;
                                }
                                break;
                            case 17:
                                if (known_blocks[17][1] == "1") {
                                    Tabulate_parser(current);
                                } else {
                                    System.out.println("<ERROR line=" + (i + 1)
                                            + "Misplaced TABULATE</ERROR>");
                                    errorcode = 1;
                                }
                                break;
                            case 18:
                                if (known_blocks[18][1] == "1") {
                                    Variable_parser(current);
                                } else {
                                    System.out.println("<ERROR line=" + (i + 1)
                                            + "Misplaced VARIABLE</ERROR>");
                                    errorcode = 1;
                                }
                                break;
                            default:
                                System.out.println("<ERROR line=" + (i + 1)
                                        + ">Unknown block type </ERROR>");
                            }
                            found = false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("<EXCEPTION function=\"Parse(String input)\">"
                    + e + "</EXCEPTION>");
        }
    }

    /*
     * SIMULATE map parser
     */
    private void Simulate_parser(String input) {
        try {
            System.out
                    .println("<INFO> <SOURCE type=\"gpss\"></SOURCE> </INFO>");
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Simulate_parser(String input)\">"
                            + e + "</EXCEPTION>");
        }
    }

    /*
     * START map parcer, finished
     */
    private void Start_parser(String input) {
        try {
            System.out.println("<INFO>Starting modelling process</INFO>");
            int res;
            int time = 0;
            /**
             * We think, that it should be START <NUMBER>
             */
            pattern = Pattern.compile("[0-9]+");
            matcher = pattern.matcher(current);
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end= " + matcher.end() + "</PARSER>"
                            + "></DEBUG>");
                }
                found = true;
                start = matcher.start();
                end = matcher.end();
                break;
            }
            if (found) {
                time = Integer.valueOf(current.substring(start, end))
                        .intValue();
                if (debug == 1) {
                    System.out.println(time);
                }
                found = false;
            }
            /** Start modeling */
            if (errorcode == 0) {
                sch.Labels(labels);
                sch.Modify(time);
                tmr.Timer_Correct(que, sch);
                while (true) {
                    res = sch.Process(que, tmr, 10);
                    if (res != -1)
                        tmr.Timer_Correct(que, sch);
                    else
                        break;
                }
            } else {
                System.out.println("<ERROR>Exiting due to errors</ERROR>");
            }
        } catch (Exception e) {
            System.out.println("<ERROR>Can not unrestand " + current
                    + "</ERROR>");
        }
    }

    /*
     * GENERATE map parcer
     */
    private void Generate_parser(String input) {
        try {
            int counter = 0;
            int args[];
            System.out.println("<GENERATE>");
            pattern = Pattern.compile("[A-Z]*[0-9]+");
            matcher = pattern.matcher(current);
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end= " + matcher.end() + "</PARSER>"
                            + "></DEBUG>");
                }
                counter++;
            }
            if (counter > 5)
                System.out
                        .println("<WARNING>Too much arguments for GENERATE</WARNING>");
            System.out.println("\t\t\t<VARIABLE argc=" + counter
                    + "></VARIABLE>");
            args = new int[counter];
            int i = 0;
            /*
             * For Java 5
             */
            pattern = Pattern.compile("[A-Z]*[0-9]+");
            matcher = pattern.matcher(current);
            /*
             * End
             */
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end=" + matcher.end() + "</PARSER>"
                            + "</DEBUG>");
                }
                start = matcher.start();
                end = matcher.end();
                try {
                    args[i] = Integer.valueOf(current.substring(start, end))
                            .intValue();
                } catch (Exception e) {
                    boolean match;
                    match = false;
                    for (int j = 0; j < labels.size(); j++) {
                        /** If label names matches */
                        if (current.substring(start, end).compareTo(
                                ((Label) labels.get(j)).name) == 0) {
                            args[i] = ((Label) labels.get(j)).location;
                            match = true;
                        }
                    }
                    if (match == false) {
                        System.out
                                .println("<WARNING>matching label found, label="
                                        + args[i] + "</WARNING>");
                        args[i] = 0;
                    } else
                        match = false;

                }
                i++;
            }
            for (i = 0; i < counter; i++) {
                System.out.println("\t\t\t<VARIABLE argv[" + i + "]=" + args[i]
                        + "></VARIABLE>");
            }
            Generate gen;
            gen = new Generate(args[0], args[1], args[2], args[3], que, args[4]);
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Generate_parser(String input)\">"
                            + e + "</EXCEPTION>");
        }
        System.out.println("</GENERATE>");
    }

    /*
     * TERMINATE map parser
     */
    private void Terminate_parser(String input) {
        try {
            String decr[];
            decr = new String[1];
            pattern = Pattern.compile("[a-zA-Z]*[0-9]+");
            matcher = pattern.matcher(current);
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end= " + matcher.end() + "</PARSER>"
                            + "</DEBUG>");
                }
                found = true;
                start = matcher.start();
                end = matcher.end();
                break;
            }
            if (found) {
                decr[0] = current.substring(start, end);
                sch.Add_Block(1, 1, decr, que);
            }
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Terminate_parser(String input)\">"
                            + e + "</EXCEPTION>");
        }
    }

    /**
     * QUEUE block parser
     */
    private void Queue_parser(String input) {
        try {
            /**
             * 1 - Determine total amount of QUEUE's
             */
            boolean found;
            int need = 0;
            System.out.println("<QUEUE>");
            String currentt;
            current = new String();
            if (Queue_amount == 0) {
                for (int ii = 0; ii < source.size(); ii++) {
                    found = false;
                    currentt = (String) source.get(ii);
                    /**
                     * remove comment
                     */
                    pattern = Pattern.compile(comment_mask);
                    matcher = pattern.matcher(currentt);
                    while (matcher.find()) {
                        if (debug == 1) {
                            System.out
                                    .println("<DEBUG> <PARSER matcher="
                                            + matcher.group() + " start="
                                            + matcher.start() + " end= "
                                            + matcher.end() + "</PARSER>"
                                            + "></DEBUG>");
                        }
                        found = true;
                        start = matcher.start();
                        end = matcher.end();
                    }
                    if (found) {
                        String tmp;
                        tmp = new String();
                        tmp = currentt.substring(0, start);
                        currentt = tmp;
                        if (debug == 1) {
                            System.out.println("<DEBUG>Comment removed"
                                    + current + "</DEBUG>");
                        }
                        found = false;
                    }
                    pattern = Pattern.compile("QUEUE");
                    matcher = pattern.matcher(currentt);
                    while (matcher.find()) {
                        if (debug == 1) {
                            System.out
                                    .println("<DEBUG> <PARSER matcher="
                                            + matcher.group() + " start="
                                            + matcher.start() + " end= "
                                            + matcher.end() + "</PARSER>"
                                            + "></DEBUG>");
                        }
                        Queue_amount++;
                        need = 1;
                    }
                    pattern = Pattern.compile("ENTER");
                    matcher = pattern.matcher(currentt);
                    while (matcher.find()) {
                        if (debug == 1) {
                            System.out
                                    .println("<DEBUG> <PARSER matcher="
                                            + matcher.group() + " start="
                                            + matcher.start() + " end= "
                                            + matcher.end() + "</PARSER>"
                                            + "></DEBUG>");
                        }
                        SQueue_amount++;
                        need = 1;
                    }
                }
            }
            System.out.println("\t\t\t<VARIABLE queue_amount=" + Queue_amount
                    + "></VARIABLE>");
            System.out.println("\t\t\t<VARIABLE squeue_amount=" + SQueue_amount
                    + "></VARIABLE>");
            /**
             * Allocate memory
             */
            if (need == 1) {
                sch.Create_Queues(Queue_amount);
                que_arr = new String[Queue_amount];
                sch.Create_Storages(SQueue_amount);
                sque_arr = new String[SQueue_amount];
                sch.Create_Fac(Queue_amount);
                for (int ii = 0; ii < Queue_amount; ii++) {
                    que_arr[ii] = "null";
                }
                for (int ii = 0; ii < SQueue_amount; ii++) {
                    sque_arr[ii] = "null";
                }
            }
            /*
             * Check, if we have "," in string. If yes - we should deal with
             * STORAGES.
             */
            boolean storage = false;
            pattern = Pattern.compile(",");
            matcher = pattern.matcher(input);
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end= " + matcher.end() + "</PARSER>"
                            + "></DEBUG>");
                }
                storage = true;
            }
            if (storage == false) {
                /**
                 * Parse QUEUE
                 */
                System.out
                        .println("\t\t\t<VARIABLE type=\"queue\"></VARIABLE>");
                pattern = Pattern.compile("[ \t]*[A-Z]+[ \t]*$");
                matcher = pattern.matcher(input);
                while (matcher.find()) {
                    if (debug == 1) {
                        System.out.println("<DEBUG> <PARSER matcher="
                                + matcher.group() + " start=" + matcher.start()
                                + " end= " + matcher.end() + "</PARSER>"
                                + "></DEBUG>");
                    }
                    start = matcher.start();
                    end = matcher.end();
                }
                /**
                 * Find queue name
                 */
                pattern = Pattern.compile("[A-Z]+");
                input = input.substring(start, end);
                matcher = pattern.matcher(input);
                while (matcher.find()) {
                    if (debug == 1) {
                        System.out.println("<DEBUG> <PARSER matcher="
                                + matcher.group() + " start=" + matcher.start()
                                + " end= " + matcher.end() + "</PARSER>"
                                + "</DEBUG>");
                    }
                    start = matcher.start();
                    end = matcher.end();
                }
                /**
                 * Check and store
                 */
                String name;
                int name_place = 0;
                name = new String();
                name = input.substring(start, end);
                System.out.println("\t\t\t<VARIABLE name=\"" + name
                        + "\"></VARIABLE>");
                for (int ii = 0; ii < Queue_amount; ii++) {
                    if (que_arr[ii].compareTo(name) == 0) {
                        System.out.println("<ERROR>Name " + name
                                + " already exists!</ERROR>");
                        errorcode = 1;
                    }
                    if (que_arr[ii] == "null") {
                        que_arr[ii] = name;
                        name_place = ii;
                        break;
                    }
                }
                /**
                 * Insert queue
                 */
                String str2[];
                str2 = new String[1]; //Queue
                str2[0] = String.valueOf(name_place);
                sch.Add_Block(2, 1, str2, que);//Queue
            }
            /*
             * Parse Queue-Storage
             */
            else {
                System.out
                        .println("\t\t\t<VARIABLE type=\"storage\"></VARIABLE>");
                /*
                 * Parsing name
                 */
                pattern = Pattern.compile("QUEUE");
                matcher = pattern.matcher(input);
                while (matcher.find()) {
                    if (debug == 1) {
                        System.out.println("<DEBUG> <PARSER matcher="
                                + matcher.group() + " start=" + matcher.start()
                                + " end= " + matcher.end() + "</PARSER>"
                                + "></DEBUG>");
                    }
                    start = matcher.start();
                    end = matcher.end();
                }
                pattern = Pattern.compile("[A-Z]+");
                input = input.substring(end, input.length());
                matcher = pattern.matcher(input);
                while (matcher.find()) {
                    if (debug == 1) {
                        System.out.println("<DEBUG> <PARSER matcher="
                                + matcher.group() + " start=" + matcher.start()
                                + " end= " + matcher.end() + "</PARSER>"
                                + "></DEBUG>");
                    }
                    start = matcher.start();
                    end = matcher.end();
                }
                /**
                 * Check and store
                 */
                String sname;
                int sname_place = 0;
                /*comment*/
                sname = new String();
                sname = input.substring(start, end);
                System.out.println("\t\t\t<VARIABLE name=\"" + sname
                        + "\"></VARIABLE>");
                for (int ii = 0; ii < SQueue_amount; ii++) {
                    if (sque_arr[ii].compareTo(sname) == 0) {
                        System.out.println("<ERROR>Storage name " + sname
                                + " already exists!</ERROR>");
                        errorcode = 1;
                    }
                    if (sque_arr[ii] == "null") {
                        sque_arr[ii] = sname;
                        sname_place = ii;
                        break;
                    }
                }
                String name;
                int name_place = 0;
                name = new String();
                name = input.substring(start, end);
                for (int ii = 0; ii < Queue_amount; ii++) {
                    if (que_arr[ii].compareTo(name) == 0) {
                        System.out.println("<ERROR>Queue of the Storage name "
                                + name + " already exists!</ERROR>");
                        errorcode = 1;
                    }
                    if (que_arr[ii] == "null") {
                        que_arr[ii] = name;
                        name_place = ii;
                        break;
                    }
                }
                pattern = Pattern.compile(",[ /t]*[0-9]+[ /t]*$");
                matcher = pattern.matcher(input);
                while (matcher.find()) {
                    if (debug == 1) {
                        System.out.println("<DEBUG> <PARSER matcher="
                                + matcher.group() + " start=" + matcher.start()
                                + " end= " + matcher.end() + "</PARSER>"
                                + "></DEBUG>");
                    }
                    start = matcher.start();
                    end = matcher.end();
                }
                String iinput;
                iinput = new String();
                iinput = input.substring(start, end);
                pattern = Pattern.compile("[0-9]+");
                matcher = pattern.matcher(iinput);
                while (matcher.find()) {
                    if (debug == 1) {
                        System.out.println("<DEBUG> <PARSER matcher="
                                + matcher.group() + " start=" + matcher.start()
                                + " end= " + matcher.end() + "</PARSER>"
                                + "></DEBUG>");
                    }
                    start = matcher.start();
                    end = matcher.end();
                }
                int param = Integer.valueOf(iinput.substring(start, end))
                        .intValue();
                /*
                 * Now, insert Queue-Storage and allocate storage
                 */
                sch.Alloc_Storage(sname_place, param);
                System.out
                        .println("\t\t\t<VARIABLE storage_param=\"queue\"></VARIABLE>");
                String str11[];
                str11 = new String[3]; //Queue-Storage
                str11[0] = String.valueOf(sname_place);//Storage
                // name
                str11[1] = String.valueOf(name_place);//Queue
                // name
                str11[2] = String.valueOf(param);//Store
                sch.Add_Block(18, 3, str11, que);//Queue-Storage
            }
            System.out.println("</QUEUE>");
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Queue_parser(String input)\">"
                            + e + "</EXCEPTION>");
        }
    }

    /**
     * Depart block parcer
     */
    private void Depart_parser(String input) {
        try {
            System.out.println("<DEPART>");

            arg.process(input, "DEPART");
            /**
             * Check and store
             */
            String name;
            int name_place = 0;
            name = new String();
            //name = input.substring(start, end);
            name = arg.argv[0];
            System.out.println("\t\t\t<VARIABLE name=\"" + name
                    + "\"></VARIABLE>");
            for (int tmp_counter = 0; tmp_counter < Queue_amount; tmp_counter++) {
                if (que_arr[tmp_counter].compareTo(name) == 0) {
                    name_place = tmp_counter;
                    break;
                }
            }
            /**
             * Insert depart
             */
            String str2[];
            str2 = new String[1]; //Depart
            str2[0] = String.valueOf(name_place);
            //System.out.println(str2[0]);
            sch.Add_Block(3, 1, str2, que);//Depart
            System.out.println("</DEPART>");
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Depart_parser(String input)\">"
                            + e + "</EXCEPTION>");
        }
    }

    /**
     * Seize block parcer
     */
    private void Seize_parser(String input) {
        try {
            System.out.println("<SEIZE>");
            arg.process(input, "SEIZE");
            /**
             * Check and store
             */
            String name;
            int name_place = 0;
            name = new String();
            name = arg.argv[0];
            System.out.println("\t\t\t<VARIABLE name=\"" + name
                    + "\"></VARIABLE>");
            for (int tmp_counter = 0; tmp_counter < Queue_amount; tmp_counter++) {
                if (que_arr[tmp_counter].compareTo(name) == 0) {
                    name_place = tmp_counter;
                    break;
                }
            }
            /**
             * Insert seize
             */
            String str2[];
            str2 = new String[1]; //Seize
            str2[0] = String.valueOf(name_place);
            //System.out.println(str2[0]);
            sch.Add_Block(4, 1, str2, que);//Seize
            System.out.println("</SEIZE>");
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Seize_parser(String input)\">"
                            + e + "</EXCEPTION>");
        }
    }

    /**
     * Release block parcer
     */
    private void Release_parser(String input) {
        try {
            System.out.println("<RELEASE>");
            pattern = Pattern.compile("[ \t]*[A-Z]+[ \t]*$");
            matcher = pattern.matcher(input);
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end= " + matcher.end() + "</PARSER>"
                            + "</DEBUG>");
                }
                start = matcher.start();
                end = matcher.end();
            }
            /**
             * Find release name
             */
            pattern = Pattern.compile("[A-Z]+");
            input = input.substring(start, end);
            matcher = pattern.matcher(input);
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end= " + matcher.end() + "</PARSER>"
                            + "</DEBUG>");
                }
                start = matcher.start();
                end = matcher.end();
            }
            /**
             * Check and store
             */
            String name;
            int name_place = 0;
            name = new String();
            name = input.substring(start, end);
            System.out.println("\t\t\t<VARIABLE name=\"" + name
                    + "\"></VARIABLE>");
            for (int tmp_counter = 0; tmp_counter < Queue_amount; tmp_counter++) {
                if (que_arr[tmp_counter].compareTo(name) == 0) {
                    name_place = tmp_counter;
                    break;
                }
            }
            /**
             * Insert release
             */
            String str2[];
            str2 = new String[1]; //Release
            str2[0] = String.valueOf(name_place);
            //System.out.println(str2[0]);
            sch.Add_Block(6, 1, str2, que);//Release
            System.out.println("</RELEASE>");
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Release_parser(String input)\">"
                            + e + "</EXCEPTION>");
        }
    }

    /*
     * ADVANCE block parcer
     */
    private void Advance_parser(String input) {
        try {
            int counter = 0;
            String args[];
            System.out.println("<ADVANCE>");
            arg.process(input, "ADVANCE");
            /**
             * Incert advance
             */
            String str5[];
            str5 = new String[2]; //Advance
            if (arg.argc > 1) {
                str5[0] = arg.argv[0];
                str5[1] = arg.argv[1];
                System.out.println("\t\t\t<VARIABLE argv[0]=" + str5[0]
                        + "></VARIABLE>");
                System.out.println("\t\t\t<VARIABLE argv[1]=" + str5[1]
                        + "></VARIABLE>");
            } else {
                str5[0] = "0";
                str5[1] = "0";
                System.out
                        .println("\t\t\t<WARNING message = \"wrong arguments count\" />");
            }
            sch.Add_Block(8, 2, str5, que);//Advance
            System.out.println("</ADVANCE>");
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Advance_parser(String input)\">"
                            + e + "</EXCEPTION>");
        }
    }

    /*
     * SPLIT block parcer
     */
    private void Split_parser(String input) {
        try {
            int counter = 0;
            String args[];
            System.out.println("<SPLIT>");
            pattern = Pattern.compile("[a-zA-Z]*[0-9:]+");
            matcher = pattern.matcher(current);
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end= " + matcher.end() + "></PARSER></DEBUG>");
                }
                counter++;
            }
            if (counter > 3) {
                System.out
                        .println("<WARNING>Too much arguments for SPLIT</WARNING>");
            }
            System.out.println("\t\t\t<VARIABLE argc=" + counter
                    + "></VARIABLE>");
            args = new String[counter];
            int i = 0;
            /*
             * For Java 5
             */
            pattern = Pattern.compile("[a-zA-Z]*[0-9:]+");
            matcher = pattern.matcher(current);
            /*
             * End
             */
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end= " + matcher.end() + "></PARSER></DEBUG>");
                }
                start = matcher.start();
                end = matcher.end();
                args[i] = current.substring(start, end);
                System.out.println("\t\t\t<VARIABLE argv[" + i + "]=" + args[i]
                        + "></VARIABLE>");
                i++;
            }
            /**
             * Insert split
             */
            String str5[];
            switch (counter) {
            case 1:
                str5 = new String[1]; //Split
                str5[0] = args[0];
                sch.Add_Block(5, 1, str5, que);//Split
                break;
            case 2:
                str5 = new String[2]; //Split
                str5[0] = args[0];
                str5[1] = "";
                str5[2] = args[1];
                sch.Add_Block(5, 2, str5, que);//Split
                break;
            case 3:
                str5 = new String[3]; //Split
                str5[0] = args[0];
                str5[1] = args[1];
                str5[2] = args[2];
                sch.Add_Block(5, 3, str5, que);//Split
                break;
            default:
                System.out.println("<ERROR>Too much arguments</ERROR>");
                errorcode = 1;
            }
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Split_parser(String input)\">"
                            + e + "</EXCEPTION>");
            System.out
                    .println("<ERROR>I can not guess labeled block number<ERROR>");
            errorcode = 1;
        }
        System.out.println("</SPLIT>");
    }

    /*
     * Priority block parcer
     */
    private void Priority_parser(String input) {
        try {
            int counter = 0;
            String args[];
            System.out.println("<PRIORITY>");
            args = new String[2];
            arg.process(input, "PRIORITY");
            for (int i = 0; i < arg.argc; i++) {
                args[i] = arg.argv[i];
                System.out.println("\t\t\t<VARIABLE argv[" + i + "]=" + args[i]
                        + "></VARIABLE>");
            }
            /**
             * Insert priority
             */
            String str5[];
            str5 = new String[2]; //Priority
            str5[0] = args[0];
            sch.Add_Block(16, 1, str5, que);//Priority
            System.out.println("</PRIORITY>");
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Priority_parser(String input)\">"
                            + e + "</EXCEPTION>");
        }
    }

    private void Buffer_parser(String input) {
        try {
            System.out.println("<BUFFER/>");
            String str5[];
            str5 = new String[0]; //Buffer
            sch.Add_Block(19, 0, str5, que);//Buffer
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Buffer_parser(String input)\""
                            + e + "/>");
        }
    }

    /**
     * Parse label i - source file string number start - dirty label start end -
     * sirty label end
     */
    private void Label_parser(int i, int start, int end, String input) {
        try {
            System.out.println("<LABEL>");
            Label lab;
            String current;
            current = new String();
            current = input.substring(start, end);
            pattern = Pattern.compile("[a-zA-Z]+[0-9]*:");
            matcher = pattern.matcher(current);
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end=" + matcher.end() + "</PARSER>"
                            + "</DEBUG>");
                }
                start = matcher.start();
                end = matcher.end();
                current = current.substring(start, (end - 1));
            }
            if (labels.size() == 0) {
                lab = new Label(current, sch.Chain_Size());
                labels.add(lab);
                System.out.println("\t\t\t<VARIABLE name=" + current
                        + "></VARIABLE>");
            } else {
                for (int tmp_counter = 0; tmp_counter < labels.size(); tmp_counter++) {
                    if (current
                            .compareTo(((Label) labels.get(tmp_counter)).name) == 0) {
                        System.out.println("<ERROR>We already have label "
                                + current + "<ERROR>");
                        errorcode = 1;
                        error = "Bad label";
                    }
                }
                if (errorcode == 0) {
                    lab = new Label(current, sch.Chain_Size());
                    labels.add(lab);
                    System.out.println("\t\t\t<VARIABLE name=\"" + current
                            + "\"></VARIABLE>");
                }
            }
            System.out.println("</LABEL>");
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Release_parser(String input)\">"
                            + e + "</EXEPTION>");
        }
    }

    private void Assemble_parser(String input) {
        try {
            /**
             * 1 - Determine total mount of QUEUE's
             */
            System.out.println("<ASSEMBLE>");
            boolean found;
            int need = 0;
            String currentt;
            current = new String();
            if (Assembles_amount == 0) {
                for (int tmp_counter = 0; tmp_counter < source.size(); tmp_counter++) {
                    found = false;
                    currentt = (String) source.get(tmp_counter);
                    /**
                     * remove comment
                     */
                    pattern = Pattern.compile(comment_mask);
                    matcher = pattern.matcher(currentt);
                    while (matcher.find()) {
                        if (debug == 1) {
                            System.out
                                    .println("<DEBUG> <PARSER matcher="
                                            + matcher.group() + " start="
                                            + matcher.start() + " end= "
                                            + matcher.end() + "</PARSER>"
                                            + "></DEBUG>");
                        }
                        found = true;
                        start = matcher.start();
                        end = matcher.end();
                    }
                    if (found) {
                        String tmp;
                        tmp = new String();
                        tmp = currentt.substring(0, start);
                        currentt = tmp;
                        if (debug == 1) {
                            System.out.println("<DEBUG>Comment removed>"
                                    + current + "<DEBUG>");
                        }
                        found = false;
                    }
                    pattern = Pattern.compile("ASSEMBLE");
                    matcher = pattern.matcher(currentt);
                    while (matcher.find()) {
                        if (debug == 1) {
                            System.out
                                    .println("<DEBUG> <PARSER matcher="
                                            + matcher.group() + " start="
                                            + matcher.start() + " end= "
                                            + matcher.end() + "</PARSER>"
                                            + "></DEBUG>");
                        }
                        Assembles_amount++;
                        need = 1;
                    }
                }
            }
            /**
             * Allocate memory
             */
            if (need == 1) {
                sch.Create_Assembles(Assembles_amount);
                System.out.println("\t\t\t<VARIABLE amount=" + Assembles_amount
                        + "></VARIABLE>");
            }
            /**
             * Parsing Assemble
             */
            int counter = 0;
            String args[];
            pattern = Pattern.compile("[a-zA-Z]*[0-9:]+");
            matcher = pattern.matcher(input);
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end= " + matcher.end() + "></DEBUG>");
                }
                counter++;
            }
            if (counter > 1)
                System.out
                        .println("<WARNING>Too much arguments for ASSEMBLE</WARNING>");
            System.out.println("\t\t\t<VARIABLE argc=" + counter + ">");
            args = new String[counter];
            int i = 0;
            pattern = Pattern.compile("[a-zA-Z]*[0-9:]+");
            matcher = pattern.matcher(input);
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end= " + matcher.end() + "</PARSER>"
                            + "></DEBUG>");
                }
                start = matcher.start();
                end = matcher.end();
                args[i] = input.substring(start, end);
                System.out.println("\t\t\t<VARIABLE argv[" + i + "]=" + args[i]
                        + "></VARIABLE>");
                i++;
            }
            /**
             * Insert ASSEMBLE
             */
            String str5[];
            str5 = new String[2]; //Assemble
            str5[0] = String.valueOf(Current_Assemble);
            str5[1] = args[0];
            sch.Assign_Assemble(Current_Assemble, Integer.valueOf(str5[0])
                    .intValue());
            sch.Add_Block(17, 2, str5, que);//Assemble
            Current_Assemble++;
            System.out.println("</ASSEMBLE>");
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Assemble_parser(String input)\">"
                            + e + "</EXCEPTION>");
        }
    }

    private void Loop_parser(String input) {
        try {
            System.out.println("<LOOP>");
            int counter = 0;
            String args[];
            pattern = Pattern.compile("[a-zA-Z]*[0-9:]+");
            matcher = pattern.matcher(current);
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end= " + matcher.end() + "</PARSER>"
                            + "></DEBUG>");
                }
                counter++;
            }
            if (counter > 2)
                System.out
                        .println("<WARNING>Too much arguments for LOOP</WARNING>");
            System.out.println("\t\t\t<VARIABLE argc=" + counter
                    + "></VARIABLE>");
            args = new String[counter];
            int i = 0;
            pattern = Pattern.compile("[a-zA-Z]*[0-9:]+");
            matcher = pattern.matcher(current);
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end= " + matcher.end() + "></DEBUG>");
                }
                start = matcher.start();
                end = matcher.end();
                args[i] = current.substring(start, end);
                System.out.println("\t\t\t<VARIABLE argv[" + i + "]=" + args[i]
                        + "></VARIABLE>");
                i++;
            }
            /**
             * Insert Loop
             */
            String str5[];
            str5 = new String[2]; //Loop
            str5[0] = args[0];
            str5[1] = args[1];
            sch.Add_Block(11, 2, str5, que);//Loop
            System.out.println("</LOOP>");
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Loop_parser(String input)\">"
                            + e + "</EXCEPTION>");
        }
    }

    private void Transfer_parser(String input) {
        try {
            System.out.println("<TRANSFER>");
            int counter = 0;
            String args[];
            pattern = Pattern.compile("[a-zA-Z]*[0-9.:]+");
            matcher = pattern.matcher(current);
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end= " + matcher.end() + "</PARSER>"
                            + "></DEBUG>");
                }
                counter++;
            }
            if (counter > 3) {
                System.out
                        .println("<WARNING>Too much arguments for TRANSFER></WARNING>");
            }
            System.out.println("\t\t\t<VARIABLE argc=" + counter
                    + "></VARIABLE>");
            args = new String[counter];
            int i = 0;
            pattern = Pattern.compile("[a-zA-Z]*[0-9.:]+");
            matcher = pattern.matcher(current);
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end= " + matcher.end() + "</PARSER>"
                            + "></DEBUG>");
                }
                start = matcher.start();
                end = matcher.end();
                args[i] = current.substring(start, end);
                System.out.println("\t\t\t<VARIABLE argv[" + i + "]=" + args[i]
                        + "></VARIABLE>");
                i++;
            }
            /**
             * Insert Transfer
             */
            String str5[];
            str5 = new String[3];
            str5[0] = args[0];
            str5[1] = args[1];
            str5[2] = args[2];
            sch.Add_Block(12, 3, str5, que);//Transfer
            System.out.println("</TRANSFER>");
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Transfer_parser(String input)\">"
                            + e + "</EXCEPTION>");
        }
    }

    private void Enter_parser(String input) {
        try {
            System.out.println("<ENTER>");
            pattern = Pattern.compile("ENTER");
            matcher = pattern.matcher(input);
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end= " + matcher.end() + "</PARSER>"
                            + "></DEBUG>");
                }
                start = matcher.start();
                end = matcher.end();
            }
            pattern = Pattern.compile("[A-Z]+");
            input = input.substring(end, input.length());
            matcher = pattern.matcher(input);
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end= " + matcher.end() + "</PARSER>"
                            + "></DEBUG>");
                }
                start = matcher.start();
                end = matcher.end();
            }
            /**
             * Check and store
             */
            String sname;
            int sname_place = 0;
            sname = new String();
            sname = input.substring(start, end);
            for (int tmp_counter = 0; tmp_counter < SQueue_amount; tmp_counter++) {
                if (sque_arr[tmp_counter].compareTo(sname) == 0) {
                    sname_place = tmp_counter;
                }
            }
            String name;
            int name_place = 0;
            name = new String();
            name = input.substring(start, end);
            for (int tmp_counter = 0; tmp_counter < Queue_amount; tmp_counter++) {
                if (que_arr[tmp_counter].compareTo(name) == 0) {
                    name_place = tmp_counter;
                }
            }
            System.out.println("\t\t\t<VARIABLE name=\"" + name
                    + "\"></VARIABLE>");
            pattern = Pattern.compile(",[ /t]*[0-9]+");
            matcher = pattern.matcher(input);
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end= " + matcher.end() + "</PARSER>"
                            + "></DEBUG>");
                }
                start = matcher.start();
                end = matcher.end();
            }
            String tmp_counternput;
            tmp_counternput = new String();
            //tmp_counternput = input.substring(end,input.length());
            tmp_counternput = input.substring(start, end);
            pattern = Pattern.compile("[0-9]+");
            matcher = pattern.matcher(tmp_counternput);
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end= " + matcher.end() + "</PARSER>"
                            + "></DEBUG>");
                }
                start = matcher.start();
                end = matcher.end();
            }
            int param = Integer.valueOf(tmp_counternput.substring(start, end))
                    .intValue();
            String str11[];
            System.out.println("\t\t\t<VARIABLE argc=3></VARIABLE>");
            str11 = new String[3]; //Enter
            str11[0] = String.valueOf(sname_place);//Storage
            // name
            System.out.println("\t\t\t<VARIABLE argv[0]=" + str11[0]
                    + "></VARIABLE>");
            str11[1] = String.valueOf(name_place);//Queue
            // name
            System.out.println("\t\t\t<VARIABLE argv[1]=" + str11[1]
                    + "></VARIABLE>");
            str11[2] = String.valueOf(param);//Store
            System.out.println("\t\t\t<VARIABLE argv[2]=" + str11[2]
                    + "></VARIABLE>");
            sch.Add_Block(9, 3, str11, que);//Enter
            System.out.println("</ENTER>");
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Enter_parser(String input)\">"
                            + e + "</EXCEPTION>");
        }
    }

    private void Leave_parser(String input) {
        try {
            System.out.println("<LEAVE>");
            pattern = Pattern.compile("LEAVE");
            matcher = pattern.matcher(input);
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end= " + matcher.end() + "</PARSER>"
                            + "></DEBUG>");
                }
                start = matcher.start();
                end = matcher.end();
            }
            pattern = Pattern.compile("[A-Z]+");
            input = input.substring(end, input.length());
            matcher = pattern.matcher(input);
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end= " + matcher.end() + "</PARSER>"
                            + "></DEBUG>");
                }
                start = matcher.start();
                end = matcher.end();
            }
            /**
             * Check and store
             */
            String sname;
            int sname_place = 0;
            sname = new String();
            sname = input.substring(start, end);
            for (int tmp_counter = 0; tmp_counter < SQueue_amount; tmp_counter++) {
                if (sque_arr[tmp_counter].compareTo(sname) == 0) {
                    sname_place = tmp_counter;
                }
            }
            String name;
            int name_place = 0;
            name = new String();
            name = input.substring(start, end);
            for (int tmp_counter = 0; tmp_counter < Queue_amount; tmp_counter++) {
                if (que_arr[tmp_counter].compareTo(name) == 0) {
                    name_place = tmp_counter;
                }
            }
            System.out.println("\t\t\t<VARIABLE name=\"" + name
                    + "\"></VARIABLE>");
            pattern = Pattern.compile(",[ /t]*[0-9]+[ /t]*$");
            matcher = pattern.matcher(input);
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end= " + matcher.end() + "</PARSER>"
                            + "></DEBUG>");
                }
                start = matcher.start();
                end = matcher.end();
            }
            String tmp_counternput;
            tmp_counternput = new String();
            tmp_counternput = input.substring(start, end);
            pattern = Pattern.compile("[0-9]+");
            matcher = pattern.matcher(tmp_counternput);
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end= " + matcher.end() + "</PARSER>"
                            + "></DEBUG>");
                }
                start = matcher.start();
                end = matcher.end();
            }
            int param = Integer.valueOf(tmp_counternput.substring(start, end))
                    .intValue();
            String str11[];
            System.out.println("\t\t\t<VARIABLE argc=3></VARIABLE>");
            str11 = new String[3]; //Enter
            str11[0] = String.valueOf(sname_place);//Storage
            // name
            System.out.println("\t\t\t<VARIABLE argv[0]=" + str11[0]
                    + "></VARIABLE>");
            str11[1] = String.valueOf(name_place);//Queue
            // name
            System.out.println("\t\t\t<VARIABLE argv[1]=" + str11[1]
                    + "></VARIABLE>");
            str11[2] = String.valueOf(param);//Store
            System.out.println("\t\t\t<VARIABLE argv[2]=" + str11[2]
                    + "></VARIABLE>");
            sch.Add_Block(10, 3, str11, que);//Leave
            System.out.println("</LEAVE>");
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Leave_parser(String input)\">"
                            + e + "</EXCEPTION>");
        }
    }

    private void Test_parser(String input) {
        try {
            System.out.println("<TEST>");
            arg.process(input, "TEST");
            /**
             * Insert Test
             */
            String str5[];
            str5 = new String[4];
            if (arg.argc > 3) {
                str5[0] = arg.argv[0];
                str5[1] = arg.argv[1];
                str5[2] = arg.argv[2];
                str5[3] = arg.argv[3];
                System.out.println("\t\t\t<VARIABLE argv[0]=" + str5[0]
                        + "></VARIABLE>");
                System.out.println("\t\t\t<VARIABLE argv[1]=" + str5[1]
                        + "></VARIABLE>");
                System.out.println("\t\t\t<VARIABLE argv[2]=" + str5[2]
                        + "></VARIABLE>");
                System.out.println("\t\t\t<VARIABLE argv[3]=" + str5[3]
                        + "></VARIABLE>");
            } else {
                str5[0] = "0";
                str5[1] = "0";
                str5[2] = "0";
                str5[3] = "0";
                System.out
                        .println("\t\t\t<WARNING message = \"wrong arguments count\" />");
            }
            sch.Add_Block(13, 4, str5, que);//Test
            System.out.println("</TEST>");
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Test_parser(String input)\">"
                            + e + "</EXCEPTION>");
        }
    }

    private void Assign_parser(String input) {
        try {
            System.out.println("<ASSIGN>");
            String name2;
            String name3;
            pattern = Pattern.compile("ASSIGN[ \t]");//Parameters
            matcher = pattern.matcher(current);
            found = false;
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end= " + matcher.end() + "</PARSER>"
                            + "></DEBUG>");
                }
                found = true;
                start = matcher.start();
                end = matcher.end();
            }
            String tmp_str;
            tmp_str = current.substring(end, input.length());
            pattern = Pattern.compile(",[ \t]*[/+-///*]+[ \t]*,");//Parameters
            matcher = pattern.matcher(tmp_str);
            found = false;
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end= " + matcher.end() + "</PARSER>"
                            + "></DEBUG>");
                }
                found = true;
            }
            if (found == true) {
                /*
                 * We have the calculatings
                 */
                name2 = new String();
                pattern = Pattern.compile(",[ \t]*[/+-///*]+[ \t]*,");//Parameters
                matcher = pattern.matcher(tmp_str);
                found = false;
                while (matcher.find()) {
                    if (debug == 1) {
                        System.out.println("<DEBUG> <PARSER matcher="
                                + matcher.group() + " start=" + matcher.start()
                                + " end= " + matcher.end() + "</PARSER>"
                                + "></DEBUG>");
                    }
                    found = true;
                    start = matcher.start();
                    end = matcher.end();
                }
                name2 = tmp_str.substring(0, start);
                name2 = name2.substring(0, start);
                String oper;
                oper = new String();
                oper = tmp_str.substring(start + 1, end - 1);
                System.out.println("\t\t\t<VARIABLE op1_name = " + name2
                        + "></VARIABLE>");
                System.out.println("\t\t\t<VARIABLE operand = " + oper
                        + "></VARIABLE>");

                name3 = new String();
                name3 = tmp_str.substring(end, tmp_str.length());
                String argv[];
                argv = new String[3];
                argv[0] = name2;
                argv[1] = name3;
                argv[2] = oper;
                sch.Add_Block(14, 3, argv, que);
                System.out.println("\t\t\t<VARIABLE op2_name = " + name3
                        + "></VARIABLE>");
            } else {
                /*
                 * We have replacement
                 */
                int counter = 0;
                String args[];
                pattern = Pattern.compile("[a-zA-Z]*[0-9.:]+");
                matcher = pattern.matcher(current);
                while (matcher.find()) {
                    if (debug == 1) {
                        System.out.println("<DEBUG> <PARSER matcher="
                                + matcher.group() + " start=" + matcher.start()
                                + " end= " + matcher.end() + "</PARSER>"
                                + "></DEBUG>");
                    }
                    counter++;
                }
                if (counter > 2) {
                    System.out
                            .println("<WARNING>Too much arguments for ASSIGN</WARNING>");
                }
                System.out.println("\t\t\t<VARIABLE argc=" + counter
                        + "><VARIABLE>");
                args = new String[counter];
                int i = 0;
                pattern = Pattern.compile("[a-zA-Z]*[0-9.:]+");
                matcher = pattern.matcher(current);
                while (matcher.find()) {
                    if (debug == 1) {
                        System.out.println("<DEBUG> <PARSER matcher="
                                + matcher.group() + " start=" + matcher.start()
                                + " end= " + matcher.end() + "</PARSER>"
                                + "></DEBUG>");
                    }
                    start = matcher.start();
                    end = matcher.end();
                    args[i] = current.substring(start, end);
                    System.out.println("\t\t\t<VARIABLE argv[" + i + "]="
                            + args[i] + "></VARIABLE>");
                    i++;
                }
                String argv[];
                argv = new String[3];
                argv[0] = args[0];
                argv[1] = args[1];
                argv[2] = "=";
                sch.Add_Block(14, 3, argv, que);
            }
            System.out.println("</ASSIGN>");
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Assign_parser(String input)\">"
                            + e + "</EXCEPTION>");
        }
    }

    private void Savevalue_parser(String input) {
        try {
            String name2;
            String name3;
            System.out.println("<SAVEVALUE>");
            pattern = Pattern.compile("SAVEVALUE[ \t]");//Parameters
            matcher = pattern.matcher(current);
            found = false;
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end= " + matcher.end() + "</PARSER>"
                            + "></DEBUG>");
                }
                found = true;
                start = matcher.start();
                end = matcher.end();
            }
            String tmp_str;
            tmp_str = current.substring(end, input.length());
            pattern = Pattern.compile(",[ \t]*[/+-///*]+[ \t]*,");//Parameters
            matcher = pattern.matcher(tmp_str);
            found = false;
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end= " + matcher.end() + "</PARSER>"
                            + "></DEBUG>");
                }
                found = true;
            }
            if (found == true) {
                /*
                 * We have the calculatings
                 */
                name2 = new String();
                pattern = Pattern.compile(",[ \t]*[/+-///*]+[ \t]*,");//Parameters
                matcher = pattern.matcher(tmp_str);
                found = false;
                while (matcher.find()) {
                    if (debug == 1) {
                        System.out.println("<DEBUG> <PARSER matcher="
                                + matcher.group() + " start=" + matcher.start()
                                + " end= " + matcher.end() + "</PARSER>"
                                + "></DEBUG>");
                    }
                    found = true;
                    start = matcher.start();
                    end = matcher.end();
                }
                name2 = tmp_str.substring(0, start);
                name2 = name2.substring(0, start);
                String oper;
                oper = new String();
                oper = tmp_str.substring(start + 1, end - 1);
                System.out.println("\t\t\t<VARIABLE op1_name = " + name2
                        + "></VARIABLE>");
                System.out.println("\t\t\t<VARIABLE operand = " + oper
                        + "></VARIABLE>");

                name3 = new String();
                name3 = tmp_str.substring(end, tmp_str.length());
                String argv[];
                argv = new String[3];
                argv[0] = name2;
                argv[1] = name3;
                argv[2] = oper;
                sch.Add_Block(15, 3, argv, que);
                System.out.println("\t\t\t<VARIABLE op2_name = " + name3
                        + "></VARIABLE>");
            } else {
                /*
                 * We have replacement
                 */
                int counter = 0;
                String args[];
                pattern = Pattern.compile("[a-zA-Z]*[0-9.:]+");
                matcher = pattern.matcher(current);
                while (matcher.find()) {
                    if (debug == 1) {
                        System.out.println("<DEBUG> <PARSER matcher="
                                + matcher.group() + " start=" + matcher.start()
                                + " end= " + matcher.end() + "</PARSER>"
                                + "></DEBUG>");
                    }
                    counter++;
                }
                if (counter > 2) {
                    System.out
                            .println("<WARNING>Too much arguments for SAVEVALUE</WARNING>");
                }
                System.out.println("\t\t\t<VARIABLE argc=" + counter
                        + "><VARIABLE>");
                args = new String[counter];
                int i = 0;
                pattern = Pattern.compile("[a-zA-Z]*[0-9.:]+");
                matcher = pattern.matcher(current);
                while (matcher.find()) {
                    if (debug == 1) {
                        System.out.println("<DEBUG> <PARSER matcher="
                                + matcher.group() + " start=" + matcher.start()
                                + " end= " + matcher.end() + "</PARSER>"
                                + "></DEBUG>");
                    }
                    start = matcher.start();
                    end = matcher.end();
                    args[i] = current.substring(start, end);
                    System.out.println("\t\t\t<VARIABLE argv[" + i + "]="
                            + args[i] + "></VARIABLE>");
                    i++;
                }
                String argv[];
                argv = new String[3];
                argv[0] = args[0];
                argv[1] = args[1];
                argv[2] = "=";
                sch.Add_Block(15, 3, argv, que);
            }

            System.out.println("</SAVEVALUE>");
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Savevalue_parser(String input)\">"
                            + e + "</EXCEPTION>");
        }
    }

    private void Table_parser(String input) {
        try {
            System.out.println("<TABLE>");
            /*
             * Determine, how many tabs do we have
             */
            if (Tabs_amount < 0) /* We have not counted yet */
            {
                String currentt;
                currentt = new String();
                Tabs_amount = 0;
                for (int tmp_counter = 0; tmp_counter < source.size(); tmp_counter++) {
                    found = false;
                    currentt = (String) source.get(tmp_counter);
                    /**
                     * remove comment
                     */
                    pattern = Pattern.compile(comment_mask);
                    matcher = pattern.matcher(currentt);
                    while (matcher.find()) {
                        if (debug == 1) {
                            System.out
                                    .println("<DEBUG> <PARSER matcher="
                                            + matcher.group() + " start="
                                            + matcher.start() + " end= "
                                            + matcher.end() + "</PARSER>"
                                            + "></DEBUG>");
                        }
                        found = true;
                        start = matcher.start();
                        end = matcher.end();
                    }
                    if (found) {
                        String tmp;
                        tmp = new String();
                        tmp = currentt.substring(0, start);
                        currentt = tmp;
                        if (debug == 1) {
                            System.out.println("<DEBUG>Comment removed"
                                    + current + "</DEBUG>");
                        }
                        found = false;
                    }
                    pattern = Pattern.compile("TABLE");
                    matcher = pattern.matcher(currentt);
                    while (matcher.find()) {
                        if (debug == 1) {
                            System.out
                                    .println("<DEBUG> <PARSER matcher="
                                            + matcher.group() + " start="
                                            + matcher.start() + " end= "
                                            + matcher.end() + "</PARSER>"
                                            + "></DEBUG>");
                        }
                        Tabs_amount++;
                    }
                }
                /*
                 * Allocating memory for tables
                 */
                sch.Create_Tabulates(Tabs_amount);
                System.out.println("<VARIABLE>amount = " + Tabs_amount
                        + "</VARIABLE>");
            }
            /*
             * Extracting table information 1 - Name 2 - Transact parameter
             * number 3 - Left margin 4 - Shift 5 - Sifts amount
             */
            found = false;
            pattern = Pattern.compile("^[ \t]*[a-zA-Z]+[0-9]+[ \t]");/*
                                                                        * Name
                                                                        * mask
                                                                        */
            matcher = pattern.matcher(current);
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end= " + matcher.end() + "</PARSER>"
                            + "></DEBUG>");
                }
                found = true;
                start = matcher.start();
                end = matcher.end();
            }
            int counter = 0;
            String args[];
            args = new String[5];
            String name;
            name = new String();
            if (found == true) {
                pattern = Pattern.compile("[a-zA-Z]+[0-9]*");//Name
                // sub
                // mask
                matcher = pattern.matcher(current.substring(start, end));
                while (matcher.find()) {
                    if (debug == 1) {
                        System.out.println("<DEBUG> <PARSER matcher="
                                + matcher.group() + " start=" + matcher.start()
                                + " end= " + matcher.end() + "</PARSER>"
                                + "></DEBUG>");
                    }
                    found = true;
                    start = matcher.start();
                    end = matcher.end();
                }
            } else {
                errorcode = 1;
                System.out.println("<ERROR>Tabulate name not found</ERROR>");
                start = 0;
                end = 0;
            }
            name = current.substring(start, end);
            System.out.println("\t\t\t<VARIABLE>name = \"" + name
                    + "\" </VARIABLE>");
            /*
             * Trying to determine, if other fields exists
             */
            pattern = Pattern.compile("P[0-9]+,[0-9]+,[0-9]+,[0-9]+");//Parameters
            // something
            // is
            // wrong!
            matcher = pattern.matcher(current);
            found = false;
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end= " + matcher.end() + "</PARSER>"
                            + "></DEBUG>");
                }
                found = true;
                start = matcher.start();
                end = matcher.end();
            }
            debug = 0;
            if (found == false) {
                errorcode = 1;
                System.out.println("<ERROR>Incorrect TABLE call</ERROR>");
            } else {
                pattern = Pattern.compile("P[0-9]+");//Parameters
                matcher = pattern.matcher(current);
                found = false;
                while (matcher.find()) {
                    if (debug == 1) {
                        System.out.println("<DEBUG> <PARSER matcher="
                                + matcher.group() + " start=" + matcher.start()
                                + " end= " + matcher.end() + "</PARSER>"
                                + "></DEBUG>");
                    }
                    found = true;
                    start = matcher.start();
                    end = matcher.end();
                }
                int parcode = 0;
                if (found == true) {
                    parcode = Integer
                            .valueOf(current.substring(start + 1, end))
                            .intValue();
                    System.out.println("\t\t\t<VARIABLE parcode =" + parcode
                            + "></VARIABLE>");
                    found = false;
                    args[0] = current.substring(start + 1, end);
                    String temp_str;
                    temp_str = new String();
                    temp_str = current.substring(end, current.length());
                    pattern = Pattern.compile("[0-9]+");
                    matcher = pattern.matcher(temp_str);
                    counter = 0;
                    found = false;
                    while (matcher.find()) {
                        if (debug == 1) {
                            System.out
                                    .println("<DEBUG> <PARSER matcher="
                                            + matcher.group() + " start="
                                            + matcher.start() + " end= "
                                            + matcher.end() + "</PARSER>"
                                            + "></DEBUG>");
                        }
                        start = matcher.start();
                        end = matcher.end();
                        counter++;
                    }
                    if (counter != 3) {
                        System.out
                                .println("<ERROR>Too much or too few arguments for GENERATE</ERROR");
                        errorcode = 19;
                    }
                    System.out.println("\t\t\t<VARIABLE argc=" + counter
                            + "></VARIABLE>");
                    int args_i[];
                    args_i = new int[counter];
                    int i = 0;
                    pattern = Pattern.compile("[0-9]+");
                    matcher = pattern.matcher(temp_str);
                    while (matcher.find()) {
                        if (debug == 1) {
                            System.out.println("<DEBUG> <PARSER matcher="
                                    + matcher.group() + " start="
                                    + matcher.start() + " end=" + matcher.end()
                                    + "</PARSER>" + "</DEBUG>");
                        }
                        start = matcher.start();
                        end = matcher.end();
                        args_i[i] = Integer.valueOf(
                                temp_str.substring(start, end)).intValue();
                        i++;
                    }
                    for (i = 0; i < counter; i++) {
                        System.out.println("\t\t\t<VARIABLE argv[" + i + "]="
                                + args_i[i] + "></VARIABLE>");
                    }
                    System.out.println("\t\t\t<VARIABLE curr_tab = " + curr_tab
                            + "></VARIABLE>");
                    sch.Add_Table(curr_tab, parcode, args_i[0], args_i[1],
                            args_i[2], name);
                }
            }
            curr_tab++;
            System.out.println("</TABLE>");
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Table_parser(String input)\">"
                            + e + "</EXCEPTION>");
        }
    }

    private void Tabulate_parser(String input) {
        try {
            System.out.println("<TABULATE>");
            pattern = Pattern.compile("TABULATE[ \t]");//Parameters
            matcher = pattern.matcher(current);
            found = false;
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end= " + matcher.end() + "</PARSER>"
                            + "></DEBUG>");
                }
                found = true;
                start = matcher.start();
                end = matcher.end();
            }
            String tmp_str;
            tmp_str = current.substring(end, current.length());
            pattern = Pattern.compile("[a-zA-Z]+[0-9]*");//Parameters
            matcher = pattern.matcher(tmp_str);
            found = false;
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end= " + matcher.end() + "</PARSER>"
                            + "></DEBUG>");
                }
                found = true;
                start = matcher.start();
                end = matcher.end();
            }
            if (found == true) {
                tmp_str = tmp_str.substring(start, end);
                System.out.println("\t\t\t<VARIABLE> name = " + tmp_str
                        + "</VARIABLE>");
                String arg[];
                arg = new String[1];
                arg[0] = tmp_str;
                sch.Add_Block(7, 1, arg, que);
            } else {
                System.out.println("<ERROR>incorrect TABULATE call</ERROR>");
            }
            System.out.println("</TABULATE>");
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Tabulate_parser(String input)\">"
                            + e + "</EXCEPTION>");
        }
    }

    private void Variable_parser(String input) {
        try {
            System.out.println("<VARIABLE>");
            pattern = Pattern.compile("VARIABLE[ \t]");//Parameters
            matcher = pattern.matcher(current);
            found = false;
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end= " + matcher.end() + "</PARSER>"
                            + "></DEBUG>");
                }
                found = true;
                start = matcher.start();
                end = matcher.end();
            }
            String tmp_str;
            tmp_str = current.substring(end, input.length());
            pattern = Pattern.compile("[/+-///*]+");//Parameters
            matcher = pattern.matcher(tmp_str);
            found = false;
            while (matcher.find()) {
                if (debug == 1) {
                    System.out.println("<DEBUG> <PARSER matcher="
                            + matcher.group() + " start=" + matcher.start()
                            + " end= " + matcher.end() + "</PARSER>"
                            + "></DEBUG>");
                }
                found = true;
            }
            if (found == true) {
                /*
                 * We have the calculatings
                 */
                String name1;
                name1 = new String();
                pattern = Pattern.compile("^[A-Z]+[0-9]*");//Parameters
                matcher = pattern.matcher(input);
                found = false;
                while (matcher.find()) {
                    if (debug == 1) {
                        System.out.println("<DEBUG> <PARSER matcher="
                                + matcher.group() + " start=" + matcher.start()
                                + " end= " + matcher.end() + "</PARSER>"
                                + "></DEBUG>");
                    }
                    found = true;
                    start = matcher.start();
                    end = matcher.end();
                }
                name1 = input.substring(start, end);
                System.out.println("\t\t\t<VARIABLE acc_name = " + name1
                        + "></VARIABLE>");
                String name2;
                name2 = new String();
                pattern = Pattern.compile(",[ \t]*[/+-///*]+[ \t]*,");//Parameters
                matcher = pattern.matcher(tmp_str);
                found = false;
                while (matcher.find()) {
                    if (debug == 1) {
                        System.out.println("<DEBUG> <PARSER matcher="
                                + matcher.group() + " start=" + matcher.start()
                                + " end= " + matcher.end() + "</PARSER>"
                                + "></DEBUG>");
                    }
                    found = true;
                    start = matcher.start();
                    end = matcher.end();
                }
                name2 = tmp_str.substring(0, start);
                name2 = name2.substring(0, start);
                String oper;
                oper = new String();
                oper = tmp_str.substring(start + 1, end - 1);
                System.out.println("\t\t\t<VARIABLE op1_name = " + name2
                        + "></VARIABLE>");
                System.out.println("\t\t\t<VARIABLE operand = " + oper
                        + "></VARIABLE>");
                String name3;
                name3 = new String();
                name3 = tmp_str.substring(end, tmp_str.length());
                System.out.println("\t\t\t<VARIABLE op2_name = " + name3
                        + "></VARIABLE>");
                String argv[];
                argv = new String[4];
                argv[0] = name1;
                argv[1] = name2;
                argv[2] = oper;
                argv[3] = name3;
                sch.Add_Block(20, 4, argv, que);
            } else {
                /*
                 * We have definitions
                 */
                String name;
                name = new String();
                pattern = Pattern.compile("[A-Z]+[0-9]*");//Parameters
                matcher = pattern.matcher(tmp_str);
                found = false;
                while (matcher.find()) {
                    if (debug == 1) {
                        System.out.println("<DEBUG> <PARSER matcher="
                                + matcher.group() + " start=" + matcher.start()
                                + " end= " + matcher.end() + "</PARSER>"
                                + "></DEBUG>");
                    }
                    found = true;
                    start = matcher.start();
                    end = matcher.end();
                }
                name = tmp_str.substring(start, end);
                sch.Add_variable(name);
                System.out.println("\t\t\t<VARIABLE name = " + name
                        + "></VARIABLE>");
            }
            System.out.println("</VARIABLE>");
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Variable_parser(String input)\">"
                            + e + "</EXCEPTION>");
        }
    }
}
