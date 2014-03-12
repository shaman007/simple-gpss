import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GPSS back-end Contains all block - related business logic Schema.java
 * 14.12.2003
 * 
 * @author Andrey Bondarenko
 * @e-mail bondarenko007@aport2000.ru
 * @license GPL
 */
public class Schema {
    /**
     * Chain, made of blocks of our model
     */
    private ArrayList Global_Chain;

    /**
     * Regex stuff
     */
    private static Pattern pattern;

    private static Matcher matcher;

    private int start;

    private int end;

    private int debug;

    private boolean isnum;

    /** Global modelling counter. Modelling terminates, when it's less than 0 */
    private int Global_Counter;

    /** Array of queues for Facilities and Storages */
    private ArrayList que_arr[];

    /**
     * Facility locks array. facility_lock[1] 1 - second facility occuped by
     * transact
     */
    private int facility_lock[];

    /**
     * Storages Fields: [Storage ID][size, occuped size]
     */
    private int storage[][];

    /**
     * Assembles array. Here we store first transact, that had entered assemble
     */
    private Transact ass_arr[];

    /**
     * Here we store abount of transacts, that had enterd assemble
     * Fields:[size][current]
     */
    private int ass_hop[][];

    /**
     * Tabulates NxM array. Fields:[id][n][m]
     */
    private int tabs[][][];

    /** Tabulates scales array */
    private int tabs_param[];

    private int tabs_amount;

    /** Not 0 if priority block used in model */
    private String tab_names[];

    private int need_sort;

    /**
     * Random number generator.
     */
    private Random rnd;

    /*
     * Model variables
     */
    private Variable vars;

    /**
     * Array of labels.
     */
    private ArrayList labels;

    /**
     * Constructor. Allocates memory Sets Global counter to preffered value
     */
    public Schema(int counter) {
        try {
            Global_Chain = new ArrayList();
            Global_Counter = counter;
            vars = new Variable();
            need_sort = 1;
            rnd = new Random();
            debug = 0;
            start = 0;
            end = 0;
            isnum = false;
        } catch (Exception e) {
            System.out.print("Schema(int counter):");
            System.out.println(e);
        }
    }

    public void Modify(int counter) {
        Global_Counter = counter;
    }

    /**
     * Adds block to Global Chain type = block type argn - arguments amount
     * argv[] = block arguments que - queues storage pointer
     */
    public void Add_Block(int type, int argc, String argv[], Queue que) {
        try {
            Block tmp;
            tmp = new Block(type, argc, argv);
            Global_Chain.add(tmp);
        } catch (Exception e) {
            System.out
                    .print("Add_Block(int type, int argc, String argv[], Queue que):");
            System.out.println(e);
        }
    }

    /**
     * Main backend algorythm Here we have business logic of all GPSS blocks que -
     * main queue storage tmr = model timer
     */
    public int Process(Queue que, Time tmr, int loglevel) {
        try {
            System.out.println("<SCHEMA>");
            if (loglevel >= 10)
                Print_state(que, tmr);
            if (Global_Counter > 0) {
                int process = 0;
                int was_queued = 0;
                Block blk;
                Transact tmp;
                do {
                    /** While Current Event is not empty performing actions */
                    if (que.get_CEC() == 0) {
                        System.out.println("<WARNING>Nothing to do</WARNING>");
                        process = 0;
                        Global_Counter = -10000;
                        break;
                    }
                    tmp = que.remove_CE();
                    /**
                     * Doinig bussines-logic stuff on the tmp transact
                     */
                    do {
                        process = 1;
                        blk = (Block) Global_Chain.get(tmp.block_current);
                        was_queued = 0;
                        switch (blk.Type) {
                        case 1:
                            /**
                             * Terminate business logic 0 - Terminate decrement
                             */
                            System.out.println("<TERMINATE>");
                            String terminate_param;
                            terminate_param = new String();
                            terminate_param = blk.Args[0];
                            /*
                             * We should resolve count, if we have variable
                             * instead of the number in the first parameter
                             */
                            pattern = Pattern.compile("^[0-9]+");
                            matcher = pattern.matcher(blk.Args[0]);
                            while (matcher.find()) {
                                if (debug == 1) {
                                    System.out
                                            .println("<DEBUG> <PARSER matcher="
                                                    + matcher.group()
                                                    + " start="
                                                    + matcher.start()
                                                    + " end= " + matcher.end()
                                                    + "</PARSER>" + "></DEBUG>");
                                }
                                start = matcher.start();
                                end = matcher.end();
                                isnum = true;
                            }
                            if (isnum == true) {
                                isnum = false;
                            } else {
                                blk.Args[0] = vars.s_GetValue(blk.Args[0]);
                                blk.Args[0] = blk.Args[0].substring(0,
                                        blk.Args[0].indexOf("."));
                            }
                            /*
                             * now OK
                             */

                            System.out.println("\t\t\t<BLOCK transact_id="
                                    + tmp.ID + "></BLOCK>");
                            int discount = Integer.valueOf(blk.Args[0])
                                    .intValue();
                            System.out.println("\t\t\t<BLOCK discount="
                                    + discount + "></BLOCK>");
                            Global_Counter -= discount;
                            /**
                             * Deleting transact and calling timer correction
                             */
                            was_queued = 1;
                            process = 0;
                            System.out.println("</TERMINATE>");
                            blk.Args[0] = terminate_param;
                            break;
                        case 2:
                            /**
                             * Queue business logic 0 - queue ID
                             */
                            int el = Integer.valueOf(blk.Args[0]).intValue();
                            System.out.println("<QUEUE>");
                            System.out.println("\t\t\t<BLOCK transact_id="
                                    + tmp.ID + "></BLOCK>");
                            System.out.println("\t\t\t<BLOCK facility=" + el
                                    + "></BLOCK>");
                            /**
                             * Determining queue ID
                             */
                            /**
                             * If facility locked - store in queue
                             */
                            if (facility_lock[el] == 1) {
                                /** Store in queue */
                                was_queued = 1;
                                Add_Queues_Elem(tmp, el);
                                process = 0;
                            } else {
                                /**
                                 * Move to next block
                                 */
                                tmp.block_current++;
                            }
                            System.out.println("</QUEUE>");
                            break;
                        case 3:
                            /** Depart business logic */
                            el = Integer.valueOf(blk.Args[0]).intValue();
                            System.out
                                    .println("<DEPART>\n\t\t\t<BLOCK transact_id="
                                            + tmp.ID
                                            + "></BLOCK>\n\t\t\t<BLOCK facility="
                                            + el + "></BLOCK>\n</DEPART>");
                            /**
                             * Does nothing. All checks must be performed in
                             * SEIZE
                             */
                            tmp.block_current++;
                            break;
                        case 4:
                            /**
                             * Seize business logic 0 - faciliry ID
                             */
                            el = Integer.valueOf(blk.Args[0]).intValue();
                            System.out
                                    .println("<SEIZE>\n\t\t\t<BLOCK transact_id="
                                            + tmp.ID
                                            + "></BLOCK>\n\t\t\t<BLOCK facility="
                                            + el + "></BLOCK>");
                            /**
                             * Determining facility ID
                             */
                            /**
                             * Occuping facility
                             */
                            int err = Add_Fac_Elem(el);
                            /**
                             * If facility does not exist or olready occuped
                             */
                            if (err == 1) {
                                System.out
                                        .println("\t\t\t<BLOCK message=roll back, transact_id="
                                                + tmp.ID + "></BLOCK>");
                                process = 0;
                                /** Rolling back */
                                tmp.block_current--;
                                que_arr[el].add(tmp);
                                was_queued = 1;
                                break;
                            }
                            /**
                             * Move to next block
                             */
                            tmp.block_current++;
                            System.out.println("</SEIZE>");
                            break;
                        case 6:
                            /**
                             * Release 0 - facility ID
                             */
                            el = Integer.valueOf(blk.Args[0]).intValue();
                            System.out
                                    .println("<RELEASE>\n\t\t\t<BLOCK transact_id="
                                            + tmp.ID
                                            + "></BLOCK>\n\t\t\t<BLOCK facility="
                                            + el + "></BLOCK>");
                            /**
                             * Move to next block
                             */
                            tmp.block_current++;
                            /**
                             * Releasing facility
                             */
                            int err1 = Rem_Fac_Elem(el);
                            if (err1 == 1) {
                                System.out
                                        .println("<ERROR>Release Error, transact_id="
                                                + tmp.ID + "</ERROR>");
                                process = 0;
                                Global_Counter = -10000;
                                break;
                            }
                            /**
                             * Poping transcat from queue if there is something
                             * in it and processing it _NOW_.
                             */
                            if (!que_arr[el].isEmpty()) {
                                Transact tmp11;
                                tmp11 = new Transact();
                                tmp11 = Rem_Queues_Elem(el);
                                /**
                                 * Move to next block
                                 */
                                tmp11.block_current++;
                                que.add_CE(tmp11);
                            }
                            System.out.println("</RELEASE>");
                            break;
                        case 8:
                            /**
                             * 0 - static delay 1 - random factor
                             */
                            System.out
                                    .println("<ADVANCE>\n\t\t\t<BLOCK transact_id="
                                            + tmp.ID + "></BLOCK>");
                            /**
                             * Move to next block
                             */
                            String advance_param;
                            advance_param = new String();
                            advance_param = blk.Args[0];
                            String advance_param1;
                            advance_param1 = new String();
                            advance_param1 = blk.Args[1];
                            /*
                             * We should resolve count, if we have variable
                             * instead of the number in the first parameter
                             */
                            pattern = Pattern.compile("^[0-9]+");
                            matcher = pattern.matcher(blk.Args[0]);
                            while (matcher.find()) {
                                if (debug == 1) {
                                    System.out
                                            .println("<DEBUG> <PARSER matcher="
                                                    + matcher.group()
                                                    + " start="
                                                    + matcher.start()
                                                    + " end= " + matcher.end()
                                                    + "</PARSER>" + "></DEBUG>");
                                }
                                start = matcher.start();
                                end = matcher.end();
                                isnum = true;
                            }
                            if (isnum == true) {
                                isnum = false;
                            } else {
                                blk.Args[0] = vars.s_GetValue(blk.Args[0]);
                                blk.Args[0] = blk.Args[0].substring(0,
                                        blk.Args[0].indexOf("."));
                            }
                            /*
                             * now OK
                             */
                            /*
                             * We should resolve count, if we have variable
                             * instead of the number in the first parameter
                             */
                            pattern = Pattern.compile("^[0-9]+");
                            matcher = pattern.matcher(blk.Args[1]);
                            while (matcher.find()) {
                                if (debug == 1) {
                                    System.out
                                            .println("<DEBUG> <PARSER matcher="
                                                    + matcher.group()
                                                    + " start="
                                                    + matcher.start()
                                                    + " end= " + matcher.end()
                                                    + "</PARSER>" + "></DEBUG>");
                                }
                                start = matcher.start();
                                end = matcher.end();
                                isnum = true;
                            }
                            if (isnum == true) {
                                isnum = false;
                            } else {
                                blk.Args[1] = vars.s_GetValue(blk.Args[1]);
                                blk.Args[1] = blk.Args[1].substring(0,
                                        blk.Args[1].indexOf("."));
                            }
                            /*
                             * now OK
                             */
                            tmp.block_current++;
                            /** Calculating advance time */
                            int a = Integer.valueOf(blk.Args[0]).intValue();
                            int b = Integer.valueOf(blk.Args[1]).intValue();
                            int factor = 0;
                            if (b > 0) {
                                factor = rnd.nextInt(2 * b);
                                if (factor < 0)
                                    factor = factor * (-1);
                            } else
                                factor = 0;
                            tmp.time_next = tmr.Get_Timer() + a + factor - b
                                    / 2;
                            System.out.println("\t\t\t<BLOCK time_next="
                                    + tmp.time_next + "></BLOCK>");
                            que.add_FE(tmp);
                            was_queued = 1;
                            process = 0;

                            blk.Args[0] = advance_param;
                            blk.Args[1] = advance_param1;
                            System.out.println("</ADVANCE>");
                            break;
                        case 9:
                            System.out
                                    .println("<ENTER>\n\t\t\t<BLOCK transact_id="
                                            + tmp.ID + "></BLOCK>");
                            el = Integer.valueOf(blk.Args[0]).intValue();
                            int el12 = Integer.valueOf(blk.Args[1]).intValue();
                            int el22 = Integer.valueOf(blk.Args[2]).intValue();
                            System.out.println("\t\t\t<BLOCK queue=" + el12
                                    + ">\n\t\t\t<BLOCK amount=" + el22
                                    + "></BLOCK>");
                            err = Add_To_Storage(el, el22);
                            if (err < 0) {
                                System.out
                                        .println("\t\t\t<BLOCK> message=roll back, transact_id="
                                                + tmp.ID + "></BLOCK>");
                                process = 0;
                                tmp.block_current--;
                                Add_Queues_Elem(tmp, el12);
                                was_queued = 1;
                                break;
                            }
                            /**
                             * Move to next block
                             */
                            tmp.block_current++;
                            System.out.println("</ENTER>");
                            break;
                        case 10:
                            System.out
                                    .println("<LEAVE>\n\t\t\t<BLOCK transact_id="
                                            + tmp.ID + "></BLOCK>");
                            //Preparing to next block
                            tmp.block_current++;
                            err = Delete_From_Storage(Integer.valueOf(
                                    blk.Args[0]).intValue(), Integer.valueOf(
                                    blk.Args[2]).intValue());
                            int el11 = Integer.valueOf(blk.Args[1]).intValue(); //queueID
                            System.out.println("\t\t\t<BLOCK queue=" + el11
                                    + "></BLOCK>");
                            if (err < 0) {
                                System.out
                                        .println("<ERROR>leave error, transact_id="
                                                + tmp.ID + "</ERROR>");
                                process = 0;
                                Global_Counter = -10000;
                                break;
                            }
                            if (!que_arr[el11].isEmpty())//If
                            // queue
                            // is not
                            // empty
                            {
                                Transact tmp11;
                                tmp11 = new Transact();
                                tmp11 = Rem_Queues_Elem(el11);
                                /**
                                 * Move to next block
                                 */
                                tmp11.block_current++;
                                que.add_CE(tmp11);
                            }
                            //Going farther to next element
                            System.out.println("</LEAVE>");
                            break;
                        case 11:
                            /**
                             * 0 - Number of hops 1 - Label location Label is
                             * ABOVE! It's like a do->while().
                             */
                            System.out
                                    .println("<LOOP>\n\t\t\t<BLOCK transact_id="
                                            + tmp.ID
                                            + ">\n\t\t\t<BLOCK  label=\""
                                            + blk.Args[1] + "\"></BLOCK>");
                            //whither already in loop or not
                            System.out.println("\t\t\t<BLOCK in_loop="
                                    + tmp.in_loop + "></BLOCK>");
                            System.out.println("\t\t\t<BLOCK hops_to_go="
                                    + tmp.hops_to_go + "></BLOCK>");
                            String loop_param;
                            loop_param = new String();
                            loop_param = blk.Args[0];
                            /*
                             * We should resolve count, if we have variable
                             * instead of the number in the first parameter
                             */
                            pattern = Pattern.compile("^[0-9]+");
                            matcher = pattern.matcher(blk.Args[0]);
                            while (matcher.find()) {
                                if (debug == 1) {
                                    System.out
                                            .println("<DEBUG> <PARSER matcher="
                                                    + matcher.group()
                                                    + " start="
                                                    + matcher.start()
                                                    + " end= " + matcher.end()
                                                    + "</PARSER>" + "></DEBUG>");
                                }
                                start = matcher.start();
                                end = matcher.end();
                                isnum = true;
                            }
                            if (isnum == true) {
                                isnum = false;
                            } else {
                                try {
                                    blk.Args[0] = vars.s_GetValue(blk.Args[0]);
                                    blk.Args[0] = blk.Args[0].substring(0,
                                            blk.Args[0].indexOf("."));
                                } catch (Exception e) {
                                }
                            }
                            /*
                             * now OK
                             */
                            if (tmp.in_loop == 1)// Are we already
                            // in loop?
                            {
                                if (tmp.hops_to_go > 0) //Do we
                                // have
                                // hopes
                                // left?
                                {
                                    if (tmp.label_name.compareTo(blk.Args[1]) == 0) //Are
                                    // we
                                    // deal
                                    // with
                                    // our
                                    // label?
                                    {
                                        System.out
                                                .println("\t\t\t<BLOCK loop_type=\"our\"></BLOCK>");
                                        //if we are in and we have to leave
                                        // in
                                        tmp.hops_to_go--; //Decrease
                                        // counter
                                        boolean match = false;
                                        //Go to the labeled block
                                        for (int j = 0; j < labels.size(); j++) {
                                            /** If label names matches */
                                            if (blk.Args[1]
                                                    .compareTo(((Label) labels
                                                            .get(j)).name) == 0) {
                                                if (tmp.hops_to_go > 0)
                                                    tmp.block_current = ((Label) labels
                                                            .get(j)).location;
                                                else
                                                    tmp.block_current++;
                                                match = true;
                                            }
                                        }
                                        if (match == false)
                                            System.out
                                                    .println("<WARNING>matching label found, label="
                                                            + blk.Args[1]
                                                            + "</WARNING>");
                                        else
                                            match = false;
                                    } else {//If wrong name
                                        System.out
                                                .println("\t\t\t<BLOCK loop_type=\"foreign\"></BLOCK>");
                                        tmp.old_hopes.push(String
                                                .valueOf(tmp.hops_to_go));
                                        tmp.old_label_name.push(tmp.label_name);
                                        tmp.old_in_loop.push(String
                                                .valueOf(tmp.in_loop));
                                        tmp.hops_to_go = Integer.valueOf(
                                                blk.Args[0]).intValue() - 1;//Hopes
                                        // left
                                        tmp.label_name = blk.Args[1];
                                        boolean match = false;
                                        //Go to the labeled block
                                        for (int j = 0; j < labels.size(); j++) {
                                            /** If label names matches */
                                            if (blk.Args[1]
                                                    .compareTo(((Label) labels
                                                            .get(j)).name) == 0) {
                                                if (tmp.hops_to_go > 0)
                                                    tmp.block_current = ((Label) labels
                                                            .get(j)).location;
                                                else
                                                    tmp.block_current++;
                                                match = true;
                                            }
                                        }
                                        if (match == false)
                                            System.out
                                                    .println("<WARNING> matching label found, label="
                                                            + blk.Args[1]
                                                            + "</WARNING>");
                                        else
                                            match = false;
                                    }
                                } else//If 0 hopes left
                                {
                                    //Loosed Java "stack" can not store INT
                                    // type
                                    if (tmp.old_in_loop.empty() == false)
                                        tmp.in_loop = Integer.valueOf(
                                                (String) tmp.old_in_loop.pop())
                                                .intValue();
                                    else
                                        tmp.in_loop = 0;
                                    if (tmp.old_hopes.empty() == false)
                                        tmp.hops_to_go = Integer.valueOf(
                                                (String) tmp.old_hopes.pop())
                                                .intValue();
                                    else
                                        tmp.hops_to_go = 0;
                                    if (tmp.old_label_name.empty() == false)
                                        tmp.label_name = (String) tmp.old_label_name
                                                .pop();
                                    else
                                        tmp.label_name = "";
                                    /**
                                     * Move to next block
                                     */
                                    tmp.block_current++;
                                }
                            } else {//If not in loop
                                tmp.hops_to_go = Integer.valueOf(blk.Args[0])
                                        .intValue() - 1;//Hopes
                                // left
                                tmp.in_loop = 1;//Now in loop
                                tmp.label_name = blk.Args[1];
                                boolean match = false;
                                //Go to the labeled block
                                for (int j = 0; j < labels.size(); j++) {
                                    /** If label names matches */
                                    if (blk.Args[1].compareTo(((Label) labels
                                            .get(j)).name) == 0) {
                                        if (tmp.hops_to_go > 0)
                                            tmp.block_current = ((Label) labels
                                                    .get(j)).location;
                                        else
                                            tmp.block_current++;
                                        match = true;
                                    }
                                }
                                if (match == false)
                                    System.out
                                            .println("<WARNING>matching label found, label="
                                                    + blk.Args[1]
                                                    + "</WARNING>");
                                else
                                    match = false;
                            }
                            blk.Args[0] = loop_param;
                            System.out.println("</LOOP>");
                            break;
                        case 12:
                            //First parameter - labeled block ID
                            System.out
                                    .println("<TRANSFER>\n\t\t\t<BLOCK transact_id="
                                            + tmp.ID + "></BLOCK>");
                            double rnd;
                            rnd = Math.random();
                            //We need to make variables support.
                            System.out.println("\t\t\t<BLOCK factor=" + rnd
                                    + "></BLOCK>");
                            System.out.println("\t\t\t<BLOCK rnadom="
                                    + blk.Args[0] + "></BLOCK>");
                            System.out.println("\t\t\t<BLOCK first_label="
                                    + blk.Args[1] + "></BLOCK>");
                            System.out.println("\t\t\t<BLOCK second_label="
                                    + blk.Args[2] + "></BLOCK>");
                            if (rnd > Double.valueOf(blk.Args[0]).doubleValue()) {
                                //Move to label
                                for (int j = 0; j < labels.size(); j++) {
                                    /** If label names matches */
                                    if (blk.Args[1].compareTo(((Label) labels
                                            .get(j)).name) == 0) {
                                        tmp.block_current = ((Label) labels
                                                .get(j)).location;
                                    }
                                }
                            } else {
                                //Move to second label
                                for (int j = 0; j < labels.size(); j++) {
                                    /** If label names matches */
                                    if (blk.Args[2].compareTo(((Label) labels
                                            .get(j)).name) == 0) {
                                        tmp.block_current = ((Label) labels
                                                .get(j)).location;
                                    }
                                }
                            }
                            System.out.println("</TRANSFER>");
                            break;
                        case 5:
                            System.out
                                    .println("<SPLIT>\n\t\t\t<BLOCK transact_id="
                                            + tmp.ID + "></BLOCK>");
                            //Move forward
                            tmp.block_current++;
                            boolean match;
                            match = false;
                            String split_param;
                            split_param = new String();
                            split_param = blk.Args[0];
                            String split_param1;
                            split_param1 = new String();
                            split_param1 = blk.Args[1];
                            String split_param2;
                            split_param2 = new String();
                            split_param2 = blk.Args[2];
                            /*
                             * We should resolve count, if we have variable
                             * instead of the number in the first parameter
                             */
                            pattern = Pattern.compile("^[0-9]+");
                            matcher = pattern.matcher(blk.Args[0]);
                            while (matcher.find()) {
                                if (debug == 1) {
                                    System.out
                                            .println("<DEBUG> <PARSER matcher="
                                                    + matcher.group()
                                                    + " start="
                                                    + matcher.start()
                                                    + " end= " + matcher.end()
                                                    + "</PARSER>" + "></DEBUG>");
                                }
                                start = matcher.start();
                                end = matcher.end();
                                isnum = true;
                            }
                            if (isnum == true) {
                                isnum = false;
                            } else {
                                blk.Args[0] = vars.s_GetValue(blk.Args[0]);
                                blk.Args[0] = blk.Args[0].substring(0,
                                        blk.Args[0].indexOf("."));
                            }
                            /*
                             * now OK
                             */

                            int nn = Integer.valueOf(blk.Args[0]).intValue();
                            System.out.println("\t\t\t<BLOCK clones=" + nn
                                    + "></BLOCK>");

                            try {
                                System.out.println("\t\t\t<BLOCK label="
                                        + blk.Args[1] + "></BLOCK>");
                            } catch (Exception e) {
                            }
                            try {
                                /*
                                 * We should resolve count, if we have variable
                                 * instead of the number in the first parameter
                                 */
                                pattern = Pattern.compile("^[0-9]+");
                                matcher = pattern.matcher(blk.Args[2]);
                                while (matcher.find()) {
                                    if (debug == 1) {
                                        System.out
                                                .println("<DEBUG> <PARSER matcher="
                                                        + matcher.group()
                                                        + " start="
                                                        + matcher.start()
                                                        + " end= "
                                                        + matcher.end()
                                                        + "</PARSER>"
                                                        + "></DEBUG>");
                                    }
                                    start = matcher.start();
                                    end = matcher.end();
                                    isnum = true;
                                }
                                if (isnum == true) {
                                    isnum = false;
                                } else {
                                    blk.Args[2] = vars.s_GetValue(blk.Args[2]);
                                    blk.Args[2] = blk.Args[2].substring(0,
                                            blk.Args[2].indexOf("."));
                                }
                                /*
                                 * now OK
                                 */
                                System.out.println("\t\t\t<BLOCK increment="
                                        + blk.Args[2] + "></BLOCK>");
                            } catch (Exception e) {
                            }
                            nn--;
                            if (blk.Args.length == 1) {
                                for (int i = 0; i < nn; i++) {
                                    /** Create transact clones */
                                    Transact tmp_copy;
                                    tmp_copy = new Transact(0);
                                    tmp.Clone_Transact(tmp_copy);
                                    que.add_CE(tmp_copy);
                                    System.out.println("\t\t\t<BLOCK clone_id="
                                            + tmp_copy.ID + "></BLOCK>");
                                }
                            } else {
                                for (int i = 0; i < nn; i++) {
                                    /** Create transact clones */
                                    Transact tmp_copy;
                                    tmp_copy = new Transact(0);
                                    tmp.Clone_Transact(tmp_copy);
                                    for (int j = 0; j < labels.size(); j++) {
                                        /** If label names matches */
                                        if (blk.Args[1]
                                                .compareTo(((Label) labels
                                                        .get(j)).name) == 0) {
                                            tmp_copy.block_current = ((Label) labels
                                                    .get(j)).location;
                                            match = true;
                                            if (blk.Args.length == 3) {
                                                tmp_copy.param[Integer.valueOf(
                                                        blk.Args[2]).intValue()] += i + 1;
                                            }
                                        }
                                    }
                                    if (match == false)
                                        System.out
                                                .println("<WARNING>no matching label found, label="
                                                        + blk.Args[1]
                                                        + "</WARNING>");
                                    else
                                        match = false;
                                    que.add_CE(tmp_copy);
                                    System.out.println("\t\t\t<BLOCK clone_id="
                                            + tmp_copy.ID + "></BLOCK>");
                                }
                            }
                            blk.Args[0] = split_param;
                            blk.Args[1] = split_param1;
                            blk.Args[2] = split_param2;
                            System.out.println("</SPLIT>");
                            break;
                        case 13:
                            //First and Second parameters - labeled block
                            // ID
                            match = false;
                            String test_name1;
                            test_name1 = new String();
                            String test_name2;
                            test_name2 = new String();
                            test_name1 = blk.Args[2];
                            test_name2 = blk.Args[3];
                            System.out
                                    .println("<TEST>\n\t\t\t<BLOCK transact_id="
                                            + tmp.ID + "></BLOCK>");
                            System.out.println("\t\t\t<BLOCK operand="
                                    + blk.Args[0] + "></BLOCK>");
                            System.out.println("\t\t\t<BLOCK label="
                                    + blk.Args[1] + "></BLOCK>");
                            System.out.println("\t\t\t<BLOCK first_value="
                                    + blk.Args[2] + "></BLOCK>");
                            System.out.println("\t\t\t<BLOCK second_value="
                                    + blk.Args[3] + "></BLOCK>");

                            /*
                             * We should resolve count, if we have variable
                             * instead of the number in the first parameter
                             */
                            pattern = Pattern.compile("^[0-9]+");
                            matcher = pattern.matcher(blk.Args[2]);
                            while (matcher.find()) {
                                if (debug == 1) {
                                    System.out
                                            .println("<DEBUG> <PARSER matcher="
                                                    + matcher.group()
                                                    + " start="
                                                    + matcher.start()
                                                    + " end= " + matcher.end()
                                                    + "</PARSER>" + "></DEBUG>");
                                }
                                start = matcher.start();
                                end = matcher.end();
                                isnum = true;
                            }
                            if (isnum == true) {
                                isnum = false;
                            } else {
                                blk.Args[2] = vars.s_GetValue(blk.Args[2]);
                                blk.Args[2] = blk.Args[2].substring(0,
                                        blk.Args[2].indexOf("."));
                            }
                            /*
                             * now OK
                             */
                            /*
                             * We should resolve count, if we have variable
                             * instead of the number in the first parameter
                             */
                            pattern = Pattern.compile("^[0-9]+");
                            matcher = pattern.matcher(blk.Args[3]);
                            while (matcher.find()) {
                                if (debug == 1) {
                                    System.out
                                            .println("<DEBUG> <PARSER matcher="
                                                    + matcher.group()
                                                    + " start="
                                                    + matcher.start()
                                                    + " end= " + matcher.end()
                                                    + "</PARSER>" + "></DEBUG>");
                                }
                                start = matcher.start();
                                end = matcher.end();
                                isnum = true;
                            }
                            if (isnum == true) {
                                isnum = false;
                            } else {
                                try {
                                    blk.Args[3] = vars.s_GetValue(blk.Args[3]);
                                    blk.Args[3] = blk.Args[3].substring(0,
                                            blk.Args[3].indexOf("."));
                                } catch (Exception e) {
                                }
                            }
                            /*
                             * now OK
                             */
                            char sw_tmp;
                            sw_tmp = blk.Args[0].charAt(0);
                            char eq_tmp;
                            try {
                                eq_tmp = blk.Args[0].charAt(1);
                            } catch (Exception e) {
                                eq_tmp = 'U';
                            }

                            switch (sw_tmp) {
                            case 'E':
                                if (Integer.valueOf(blk.Args[2]).intValue() == Integer
                                        .valueOf(blk.Args[3]).intValue()) {
                                    //									Go to the labeled block
                                    for (int j = 0; j < labels.size(); j++) {
                                        /** If label names matches */
                                        if (blk.Args[1]
                                                .compareTo(((Label) labels
                                                        .get(j)).name) == 0) {
                                            //	if (tmp.hops_to_go > 0)
                                            tmp.block_current = ((Label) labels
                                                    .get(j)).location;
                                            //	else
                                            //		tmp.block_current++;
                                            match = true;
                                        }
                                    }
                                    if (match == false)
                                        System.out
                                                .println("<WARNING>matching label found, label="
                                                        + blk.Args[1]
                                                        + "</WARNING>");
                                    else
                                        match = false;

                                    System.out
                                            .println("\t\t\t<INFO>message = \"equal\"</INFO>");
                                } else {
                                    System.out
                                            .println("\t\t\t<INFO>message = \"uneqal\"</WARNING>");
                                    tmp.block_current++;
                                }
                                break;
                            case 'G':
                                if (eq_tmp == 'E') {
                                    if (Integer.valueOf(blk.Args[2]).intValue() >= Integer
                                            .valueOf(blk.Args[3]).intValue()) {
                                        //								Go to the labeled block
                                        for (int j = 0; j < labels.size(); j++) {
                                            /** If label names matches */
                                            if (blk.Args[1]
                                                    .compareTo(((Label) labels
                                                            .get(j)).name) == 0) {
                                                //if (tmp.hops_to_go > 0)
                                                tmp.block_current = ((Label) labels
                                                        .get(j)).location;
                                                //else
                                                //	tmp.block_current++;
                                                match = true;
                                            }
                                        }
                                        if (match == false)
                                            System.out
                                                    .println("<WARNING>matching label found, label="
                                                            + blk.Args[1]
                                                            + "</WARNING>");
                                        else
                                            match = false;

                                        System.out
                                                .println("\t\t\t<INFO>message = \"grater\"</INFO>");
                                    } else {
                                        System.out
                                                .println("\t\t\t<INFO>message = \"not grater\"</INFO>");
                                        tmp.block_current++;
                                    }
                                } else {
                                    if (Integer.valueOf(blk.Args[2]).intValue() > Integer
                                            .valueOf(blk.Args[3]).intValue()) {
                                        //								Go to the labeled block
                                        for (int j = 0; j < labels.size(); j++) {
                                            /** If label names matches */
                                            if (blk.Args[1]
                                                    .compareTo(((Label) labels
                                                            .get(j)).name) == 0) {
                                                //if (tmp.hops_to_go > 0)
                                                tmp.block_current = ((Label) labels
                                                        .get(j)).location;
                                                //else
                                                //	tmp.block_current++;
                                                match = true;
                                            }
                                        }
                                        if (match == false)
                                            System.out
                                                    .println("<WARNING>matching label found, label="
                                                            + blk.Args[1]
                                                            + "</WARNING>");
                                        else
                                            match = false;

                                        System.out
                                                .println("\t\t\t<INFO>message = \"grater\"</INFO>");
                                    } else {
                                        System.out
                                                .println("\t\t\t<INFO>message = \"not grater\"</INFO>");
                                        tmp.block_current++;
                                    }

                                }

                                break;

                            case 'L':
                                if (eq_tmp == 'E') {
                                    if (Integer.valueOf(blk.Args[2]).intValue() <= Integer
                                            .valueOf(blk.Args[3]).intValue()) {
                                        //								Go to the labeled block
                                        for (int j = 0; j < labels.size(); j++) {
                                            /** If label names matches */
                                            if (blk.Args[1]
                                                    .compareTo(((Label) labels
                                                            .get(j)).name) == 0) {
                                                //if (tmp.hops_to_go > 0)
                                                tmp.block_current = ((Label) labels
                                                        .get(j)).location;
                                                //else
                                                //	tmp.block_current++;
                                                match = true;
                                            }
                                        }
                                        if (match == false)
                                            System.out
                                                    .println("<WARNING>matching label found, label="
                                                            + blk.Args[1]
                                                            + "</WARNING>");
                                        else
                                            match = false;

                                        System.out
                                                .println("\t\t\t<INFO>message = \"less\"</INFO>");
                                    } else {
                                        System.out
                                                .println("\t\t\t<INFO>message = \"not less\"</INFO>");
                                        tmp.block_current++;
                                    }
                                } else {
                                    if (Integer.valueOf(blk.Args[2]).intValue() < Integer
                                            .valueOf(blk.Args[3]).intValue()) {
                                        //								Go to the labeled block
                                        for (int j = 0; j < labels.size(); j++) {
                                            /** If label names matches */
                                            if (blk.Args[1]
                                                    .compareTo(((Label) labels
                                                            .get(j)).name) == 0) {
                                                //if (tmp.hops_to_go > 0)
                                                tmp.block_current = ((Label) labels
                                                        .get(j)).location;
                                                //else
                                                //	tmp.block_current++;
                                                match = true;
                                            }
                                        }
                                        if (match == false)
                                            System.out
                                                    .println("<WARNING>matching label found, label="
                                                            + blk.Args[1]
                                                            + "</WARNING>");
                                        else
                                            match = false;

                                        System.out
                                                .println("\t\t\t<INFO>message = \"less\"</INFO>");
                                    } else {
                                        System.out
                                                .println("\t\t\t<INFO>message = \"not less\"</INFO>");
                                        tmp.block_current++;
                                    }

                                }
                                break;

                            case 'N':
                                if (Integer.valueOf(blk.Args[2]).intValue() != Integer
                                        .valueOf(blk.Args[3]).intValue()) {
                                    //								Go to the labeled block
                                    for (int j = 0; j < labels.size(); j++) {
                                        /** If label names matches */
                                        if (blk.Args[1]
                                                .compareTo(((Label) labels
                                                        .get(j)).name) == 0) {
                                            //if (tmp.hops_to_go > 0)
                                            tmp.block_current = ((Label) labels
                                                    .get(j)).location;
                                            //else
                                            //	tmp.block_current++;
                                            match = true;
                                        }
                                    }
                                    if (match == false)
                                        System.out
                                                .println("<WARNING>matching label found, label="
                                                        + blk.Args[1]
                                                        + "</WARNING>");
                                    else
                                        match = false;

                                    System.out
                                            .println("\t\t\t<INFO>message = \"unequal\"</INFO>");
                                } else {
                                    System.out
                                            .println("\t\t\t<INFO>message = \"equal\"</INFO>");
                                    tmp.block_current++;
                                }
                                break;

                            default: {
                                System.out
                                        .println("<WARNING> message=\"unknown operand\"</WARNING>");
                                tmp.block_current++;
                            }
                            }

                            blk.Args[2] = test_name1;
                            blk.Args[3] = test_name2;
                            System.out.println("</TEST>");
                            break;
                        case 14:
                            System.out
                                    .println("<ASSIGN>\n\t\t\t<BLOCK transact_id="
                                            + tmp.ID + "></BLOCK>\n");
                            System.out.println("\t\t\t<BLOCK> val=\""
                                    + blk.Args[0] + "\" </BLOCK>");
                            System.out.println("\t\t\t <BLOCK> tran=\""
                                    + blk.Args[1] + "\" </BLOCK>");
                            System.out.println("\t\t\t<BLOCK> operand=\""
                                    + blk.Args[2] + "\" </BLOCK>");
                            String assign_param;
                            assign_param = new String();
                            assign_param = blk.Args[0];
                            String assign_param1;
                            assign_param1 = new String();
                            assign_param1 = blk.Args[1];
                            /*
                             * We should resolve count, if we have variable
                             * instead of the number in the first parameter
                             */
                            pattern = Pattern.compile("^[0-9]+");
                            matcher = pattern.matcher(blk.Args[0]);
                            while (matcher.find()) {
                                if (debug == 1) {
                                    System.out
                                            .println("<DEBUG> <PARSER matcher="
                                                    + matcher.group()
                                                    + " start="
                                                    + matcher.start()
                                                    + " end= " + matcher.end()
                                                    + "</PARSER>" + "></DEBUG>");
                                }
                                start = matcher.start();
                                end = matcher.end();
                                isnum = true;
                            }
                            if (isnum == true) {
                                isnum = false;
                            } else {
                                blk.Args[0] = vars.s_GetValue(blk.Args[0]);
                                blk.Args[0] = blk.Args[0].substring(0,
                                        blk.Args[0].indexOf("."));
                            }
                            /*
                             * now OK
                             */
                            /*
                             * We should resolve count, if we have variable
                             * instead of the number in the first parameter
                             */
                            pattern = Pattern.compile("^[0-9]+");
                            matcher = pattern.matcher(blk.Args[1]);
                            while (matcher.find()) {
                                if (debug == 1) {
                                    System.out
                                            .println("<DEBUG> <PARSER matcher="
                                                    + matcher.group()
                                                    + " start="
                                                    + matcher.start()
                                                    + " end= " + matcher.end()
                                                    + "</PARSER>" + "></DEBUG>");
                                }
                                start = matcher.start();
                                end = matcher.end();
                                isnum = true;
                            }
                            if (isnum == true) {
                                isnum = false;
                            } else {
                                blk.Args[1] = vars.s_GetValue(blk.Args[1]);
                                blk.Args[1] = blk.Args[1].substring(0,
                                        blk.Args[1].indexOf("."));
                            }
                            /*
                             * now OK
                             */
                            char sw;
                            sw = blk.Args[2].charAt(0);
                            double acc_mm = 0;
                            double oper1_mm = 0;

                            oper1_mm = vars.GetValue(blk.Args[0]);
                            if (vars.last_val == false) {
                                try {
                                    oper1_mm = Double.valueOf(blk.Args[0])
                                            .doubleValue();
                                } catch (Exception e) {
                                    System.out
                                            .println("<WARNING>no matching variable name is not found, name="
                                                    + blk.Args[1]
                                                    + " </WARNING>");
                                }

                            }

                            switch (sw) {
                            case '*':
                                System.out
                                        .println("\t\t\t<BLOCK> operand * detected</BLOCK>");
                                tmp.param[Integer.valueOf(blk.Args[1])
                                        .intValue()] = (int) (acc_mm * oper1_mm);
                                if (vars.last_val == false)
                                    System.out
                                            .println("<WARNING>no matching variable name is not found, name="
                                                    + blk.Args[0]
                                                    + "</WARNING>");

                                break;
                            case '-':
                                System.out
                                        .println("\t\t\t<BLOCK> operand - detected</BLOCK>");
                                tmp.param[Integer.valueOf(blk.Args[1])
                                        .intValue()] = (int) (acc_mm - oper1_mm);

                                if (vars.last_val == false)
                                    System.out
                                            .println("<WARNING>no matching variable name is not found, name="
                                                    + blk.Args[0]
                                                    + "</WARNING>");

                                break;
                            case '+':
                                System.out
                                        .println("\t\t\t<BLOCK> operand + detected</BLOCK>");
                                tmp.param[Integer.valueOf(blk.Args[1])
                                        .intValue()] = (int) (acc_mm + oper1_mm);
                                if (vars.last_val == false)
                                    System.out
                                            .println("<WARNING>no matching variable name is not found, name="
                                                    + blk.Args[0]
                                                    + "</WARNING>");

                                break;
                            case '=':
                                System.out
                                        .println("\t\t\t<BLOCK> operand = detected</BLOCK>");
                                tmp.param[Integer.valueOf(blk.Args[1])
                                        .intValue()] = (int) (oper1_mm);
                                if (vars.last_val == false)
                                    System.out
                                            .println("<WARNING>no matching variable name is not found, name="
                                                    + blk.Args[0]
                                                    + "</WARNING>");

                                break;
                            case '/':
                                System.out
                                        .println("\t\t\t<BLOCK> operand / detected</BLOCK>");
                                tmp.param[Integer.valueOf(blk.Args[1])
                                        .intValue()] = (int) (acc_mm / oper1_mm);
                                if (vars.last_val == false)
                                    System.out
                                            .println("<WARNING>no matching variable name is not found, name="
                                                    + blk.Args[0]
                                                    + "</WARNING>");

                                break;

                            default:
                                System.out
                                        .println("<WARNING>unknown operand type!</WARNING>");
                            }
                            tmp.block_current++;
                            blk.Args[0] = assign_param;
                            blk.Args[1] = assign_param1;
                            System.out.println("</ASSIGN>");
                            break;
                        case 15:
                            System.out
                                    .println("<SAVEVELUE>\n\t\t\t<BLOCK transact_id="
                                            + tmp.ID + "></BLOCK>\n");
                            System.out.println("\t\t\t<BLOCK> val=\""
                                    + blk.Args[0] + "\" </BLOCK>");
                            System.out.println("\t\t\t <BLOCK> tran=\""
                                    + blk.Args[1] + "\" </BLOCK>");
                            System.out.println("\t\t\t<BLOCK> operand=\""
                                    + blk.Args[2] + "\" </BLOCK>");
                            /*
                             * We should resolve count, if we have variable
                             * instead of the number in the first parameter
                             */
                            String save_param;
                            save_param = new String();
                            save_param = blk.Args[1];
                            pattern = Pattern.compile("^[0-9]+");
                            matcher = pattern.matcher(blk.Args[1]);
                            while (matcher.find()) {
                                if (debug == 1) {
                                    System.out
                                            .println("<DEBUG> <PARSER matcher="
                                                    + matcher.group()
                                                    + " start="
                                                    + matcher.start()
                                                    + " end= " + matcher.end()
                                                    + "</PARSER>" + "></DEBUG>");
                                }
                                start = matcher.start();
                                end = matcher.end();
                                isnum = true;
                            }
                            if (isnum == true) {
                                isnum = false;
                            } else {
                                blk.Args[1] = vars.s_GetValue(blk.Args[1]);
                                blk.Args[1] = blk.Args[1].substring(0,
                                        blk.Args[1].indexOf("."));
                            }
                            /*
                             * now OK
                             */
                            sw = blk.Args[2].charAt(0);
                            acc_mm = 0;
                            oper1_mm = 0;

                            oper1_mm = vars.GetValue(blk.Args[0]);
                            if (vars.last_val == false) {
                                try {
                                    oper1_mm = Double.valueOf(blk.Args[0])
                                            .doubleValue();
                                } catch (Exception e) {
                                    System.out
                                            .println("<WARNING>no matching variable name is not found, name="
                                                    + blk.Args[0]
                                                    + " </WARNING>");
                                }

                            }

                            switch (sw) {
                            case '*':
                                System.out
                                        .println("\t\t\t<BLOCK> operand * detected</BLOCK>");
                                acc_mm = oper1_mm
                                        * tmp.param[Integer
                                                .valueOf(blk.Args[1])
                                                .intValue()];

                                vars.SetValue(blk.Args[0], acc_mm);
                                if (vars.last_val == false)
                                    System.out
                                            .println("<WARNING>no matching variable name is not found, name="
                                                    + blk.Args[0]
                                                    + "</WARNING>");

                                break;
                            case '-':
                                System.out
                                        .println("\t\t\t<BLOCK> operand - detected</BLOCK>");
                                acc_mm = oper1_mm
                                        - tmp.param[Integer
                                                .valueOf(blk.Args[1])
                                                .intValue()];

                                vars.SetValue(blk.Args[0], acc_mm);
                                if (vars.last_val == false)
                                    System.out
                                            .println("<WARNING>no matching variable name is not found, name="
                                                    + blk.Args[0]
                                                    + "</WARNING>");

                                break;
                            case '+':
                                System.out
                                        .println("\t\t\t<BLOCK> operand + detected</BLOCK>");
                                acc_mm = oper1_mm
                                        + tmp.param[Integer
                                                .valueOf(blk.Args[1])
                                                .intValue()];

                                vars.SetValue(blk.Args[0], acc_mm);
                                if (vars.last_val == false)
                                    System.out
                                            .println("<WARNING>no matching variable name is not found, name="
                                                    + blk.Args[0]
                                                    + "</WARNING>");

                                break;
                            case '=':
                                System.out
                                        .println("\t\t\t<BLOCK> operand = detected</BLOCK>");
                                acc_mm = tmp.param[Integer.valueOf(blk.Args[1])
                                        .intValue()];

                                vars.SetValue(blk.Args[0], acc_mm);
                                if (vars.last_val == false)
                                    System.out
                                            .println("<WARNING>no matching variable name is not found, name="
                                                    + blk.Args[0]
                                                    + "</WARNING>");

                                break;
                            case '/':
                                System.out
                                        .println("\t\t\t<BLOCK> operand / detected</BLOCK>");
                                acc_mm = oper1_mm
                                        / tmp.param[Integer
                                                .valueOf(blk.Args[1])
                                                .intValue()];

                                vars.SetValue(blk.Args[0], acc_mm);
                                if (vars.last_val == false)
                                    System.out
                                            .println("<WARNING>no matching variable name is not found, name="
                                                    + blk.Args[0]
                                                    + "</WARNING>");

                                break;

                            default:
                                System.out
                                        .println("<WARNING>unknown operand type!</WARNING>");
                            }
                            tmp.block_current++;
                            blk.Args[1] = save_param;
                            System.out.println("</SAVEVALUE>");
                            break;
                        case 16:
                            System.out
                                    .println("<PRIORITY>\n\t\t\t<BLOCK transact_id="
                                            + tmp.ID + "></BLOCK>");
                            String priority_param;
                            priority_param = new String();
                            priority_param = blk.Args[0];
                            /*
                             * We should resolve count, if we have variable
                             * instead of the number in the first parameter
                             */
                            pattern = Pattern.compile("^[0-9]+");
                            matcher = pattern.matcher(blk.Args[0]);
                            while (matcher.find()) {
                                if (debug == 1) {
                                    System.out
                                            .println("<DEBUG> <PARSER matcher="
                                                    + matcher.group()
                                                    + " start="
                                                    + matcher.start()
                                                    + " end= " + matcher.end()
                                                    + "</PARSER>" + "></DEBUG>");
                                }
                                start = matcher.start();
                                end = matcher.end();
                                isnum = true;
                            }
                            if (isnum == true) {
                                isnum = false;
                            } else {
                                blk.Args[0] = vars.s_GetValue(blk.Args[0]);
                                blk.Args[0] = blk.Args[0].substring(0,
                                        blk.Args[0].indexOf("."));
                            }
                            /*
                             * now OK
                             */

                            //Move forward
                            tmp.block_current++;
                            System.out.println("\t\t\t<BLOCK priority="
                                    + blk.Args[0] + "></BLOCK>");
                            tmp.priority = Integer.valueOf(blk.Args[0])
                                    .intValue();
                            blk.Args[0] = priority_param;
                            System.out.println("</PRIORITY>");
                            break;
                        case 17:
                            String ass_param;
                            ass_param = new String();
                            ass_param = blk.Args[1];
                            System.out
                                    .println("<ASSEMBLE>\n\t\t\t<BLOCK transact_id="
                                            + tmp.ID + "></BLOCK>");
                            /*
                             * We should resolve count, if we have variable
                             * instead of the number in the first parameter
                             */
                            pattern = Pattern.compile("^[0-9]+");
                            matcher = pattern.matcher(blk.Args[1]);
                            while (matcher.find()) {
                                if (debug == 1) {
                                    System.out
                                            .println("<DEBUG> <PARSER matcher="
                                                    + matcher.group()
                                                    + " start="
                                                    + matcher.start()
                                                    + " end= " + matcher.end()
                                                    + "</PARSER>" + "></DEBUG>");
                                }
                                start = matcher.start();
                                end = matcher.end();
                                isnum = true;
                            }
                            if (isnum == true) {
                                isnum = false;
                            } else {
                                blk.Args[1] = vars.s_GetValue(blk.Args[1]);
                                blk.Args[1] = blk.Args[1].substring(0,
                                        blk.Args[1].indexOf("."));
                            }
                            /*
                             * now OK
                             */

                            el = Integer.valueOf(blk.Args[0]).intValue();
                            System.out.println("\t\t\t<BLOCK assemble_id=" + el
                                    + "></BLOCK>");
                            System.out.println("\t\t\t<BLOCK hops="
                                    + ass_hop[el][1] + "></BLOCK>");
                            /*
                             * Dirty hack ;)
                             */
                            ass_hop[el][0] = Integer.valueOf(blk.Args[1])
                                    .intValue();

                            System.out.println("\t\t\t<BLOCK hops_total="
                                    + ass_hop[el][0] + "></BLOCK>");
                            if (ass_hop[el][1] == 0) {
                                //First time
                                //Save transact
                                ass_arr[el] = tmp;
                                ass_hop[el][1]++;
                                was_queued = 1;
                                process = 0;
                            } else {
                                if (ass_hop[el][1] < ass_hop[el][0]) {
                                    //Not a first time
                                    //Send transact to /dev/null
                                    was_queued = 1;
                                    process = 0;
                                    ass_hop[el][1]++;
                                } else {
                                    //Restore rtansact
                                    tmp = ass_arr[el];
                                    /**
                                     * Move to next block
                                     */
                                    tmp.block_current++;
                                    ass_hop[el][1] = 0;
                                }
                            }
                            blk.Args[1] = ass_param;
                            System.out.println("</ASSEMBLE>");
                            break;
                        case 18:
                            //Queue-Storage business logic
                            System.out
                                    .println("<SQUEUE>\n\t\t\t<BLOCK transact_id="
                                            + tmp.ID + "></BLOCK>");
                            el = Integer.valueOf(blk.Args[0]).intValue(); //storage
                            // ID
                            int el1 = Integer.valueOf(blk.Args[1]).intValue(); //queue
                            // ID
                            int el2 = Integer.valueOf(blk.Args[2]).intValue(); //Free
                            // buffer
                            // size
                            System.out.println("\t\t\t<BLOCK storege_id="
                                    + blk.Args[0] + "></BLOCK>");
                            System.out.println("\t\t\t<BLOCK queue_id="
                                    + blk.Args[1] + "></BLOCK>");
                            System.out.println("\t\t\t<BLOCK free_buffer_size="
                                    + blk.Args[2] + "></BLOCK>");
                            if (storage[el][0] <= (storage[el][1])) //If
                            // storage
                            // locked
                            // -
                            // store
                            // in
                            // queue
                            {//Store in queue
                                was_queued = 1;
                                Add_Queues_Elem(tmp, el1);
                                process = 0;
                                System.out
                                        .println("\t\t\t<BLOCK message=\"locked\"></BLOCK>");
                            } else {
                                /**
                                 * Move to next block
                                 */
                                tmp.block_current++;
                                System.out
                                        .println("\t\t\t<BLOCK message=\"unlocked\"></BLOCK>");
                            }
                            System.out.println("</SQUEUE>");
                            break;
                        case 7:
                            //[0] - name
                            System.out
                                    .println("<TABULATE>\n\t\t\t<BLOCK transact_id="
                                            + tmp.ID + "></BLOCK>");
                            /*
                             * Trying to find correct tab name
                             */
                            boolean found = false;
                            el = 0;
                            for (int i = 0; i < tabs_amount; i++) {
                                if (tab_names[i].compareTo(blk.Args[0]) == 0) {
                                    found = true;
                                    el = i;
                                    break;
                                }
                            }
                            if (found == false) {
                                System.out
                                        .println("<ERROR>Can not resolve tab name</ERROR>");
                            }
                            /*
                             * Modifying table
                             */
                            int id = tabs_param[el];
                            int value = tmp.param[el];
                            int start = tabs[el][0][1];
                            int end = tabs[el][0][1] + tabs[el][0][2]
                                    * tabs[el][0][3];
                            int shift = tabs[el][0][3];
                            int j = 0;
                            for (int i = start; i < end; i += shift) {
                                if (value < i + shift && value >= i) {
                                    tabs[el][j][0]++;
                                    j++;
                                }
                                System.out
                                        .println("\t\t\t<VARIABLE> table_name = "
                                                + blk.Args[0] + " </VARIABLE>");
                                j = 0;
                                for (i = start; i < end; i += shift) {
                                    System.out
                                            .println("\t\t\t<VARIABLE> gap["
                                                    + i + "," + (i + shift)
                                                    + "] = " + tabs[el][j++][0]
                                                    + " </VARIABLE>");
                                }
                            }
                            tmp.block_current++;
                            System.out.println("</TABULATE>");
                            break;
                        case 19:
                            System.out
                                    .println("<BUFFER>\n\t\t\t<BLOCK transact_id="
                                            + tmp.ID + "></BLOCK>\n</BUFFER>");
                            was_queued = 1;
                            que.add_CE(tmp);
                            break;
                        case 20:
                            System.out
                                    .println("<VARIABLE>\n\t\t\t<BLOCK transact_id="
                                            + tmp.ID + "></BLOCK>\n");
                            System.out.println("\t\t\t<BLOCK> acc=\""
                                    + blk.Args[0] + "\" </BLOCK>");
                            System.out.println("\t\t\t <BLOCK> op1=\""
                                    + blk.Args[1] + "\" </BLOCK>");
                            System.out.println("\t\t\t<BLOCK> operand=\""
                                    + blk.Args[2] + "\" </BLOCK>");
                            System.out.println("\t\t\t<BLOCK> op2=\""
                                    + blk.Args[3] + "\" </BLOCK>");

                            sw = blk.Args[2].charAt(0);
                            acc_mm = 0;
                            oper1_mm = 0;
                            double oper2_mm = 0;

                            oper1_mm = vars.GetValue(blk.Args[1]);
                            if (vars.last_val == false) {
                                try {
                                    oper1_mm = Double.valueOf(blk.Args[1])
                                            .doubleValue();
                                } catch (Exception e) {
                                    System.out
                                            .println("<WARNING>no matching variable name is not found, name="
                                                    + blk.Args[1]
                                                    + " </WARNING>");
                                }

                            }
                            oper2_mm = vars.GetValue(blk.Args[3]);
                            if (vars.last_val == false) {

                                try {
                                    oper2_mm = Double.valueOf(blk.Args[3])
                                            .doubleValue();
                                } catch (Exception e) {
                                    System.out
                                            .println("<WARNING>no matching variable name is not found, name="
                                                    + blk.Args[3]
                                                    + "</WARNING>");
                                }
                            }
                            switch (sw) {
                            case '*':
                                System.out
                                        .println("\t\t\t<BLOCK> operand * detected</BLOCK>");
                                acc_mm = oper1_mm * oper2_mm;

                                vars.SetValue(blk.Args[0], acc_mm);
                                if (vars.last_val == false)
                                    System.out
                                            .println("<WARNING>no matching variable name is not found, name="
                                                    + blk.Args[0]
                                                    + "</WARNING>");

                                break;
                            case '-':
                                System.out
                                        .println("\t\t\t<BLOCK> operand - detected</BLOCK>");
                                acc_mm = oper1_mm - oper2_mm;
                                System.out.println("\t\t\t<BLOCK> acc=\""
                                        + blk.Args[0] + "\" val="+acc_mm+" </BLOCK>");
                                vars.SetValue(blk.Args[0], acc_mm);
                                if (vars.last_val == false)
                                    System.out
                                            .println("<WARNING>no matching variable name is not found, name="
                                                    + blk.Args[0]
                                                    + "</WARNING>");

                                break;
                            case '+':
                                System.out
                                        .println("\t\t\t<BLOCK> operand + detected</BLOCK>");
                                acc_mm = oper1_mm + oper2_mm;
                                System.out.println("\t\t\t<BLOCK> acc=\""
                                        + blk.Args[0] + "\" val="+acc_mm+" </BLOCK>");
                                vars.SetValue(blk.Args[0], acc_mm);
                                if (vars.last_val == false)
                                    System.out
                                            .println("<WARNING>no matching variable name is not found, name="
                                                    + blk.Args[0]
                                                    + "</WARNING>");

                                break;
                            case '=':
                                System.out
                                        .println("\t\t\t<BLOCK> operand = detected</BLOCK>");
                                acc_mm = oper2_mm;

                                vars.SetValue(blk.Args[0], acc_mm);
                                if (vars.last_val == false)
                                    System.out
                                            .println("<WARNING>no matching variable name is not found, name="
                                                    + blk.Args[0]
                                                    + "</WARNING>");

                                break;
                            case '/':
                                System.out
                                        .println("\t\t\t<BLOCK> operand / detected</BLOCK>");
                                acc_mm = oper1_mm / oper2_mm;

                                vars.SetValue(blk.Args[0], acc_mm);
                                if (vars.last_val == false)
                                    System.out
                                            .println("<WARNING>no matching variable name is not found, name="
                                                    + blk.Args[0]
                                                    + "</WARNING>");

                                break;

                            default:
                                System.out
                                        .println("<WARNING>unknown operand type!</WARNING>");
                            }
                            tmp.block_current++;
                            System.out.println("</VARIABLE>");
                            break;
                        default:
                            System.out
                                    .println("<WARNING>unknown block type!</WARNING>");
                        }
                    } while (process != 0);
                    /**
                     * Finishing business stuff
                     */
                    if (was_queued == 0)
                        /** If not already stored in queue */
                        que.add_FE(tmp);
                } while (que.get_CEC() > 0);
                if (Global_Counter > 0) {
                    System.out.println("</SCHEMA>");
                    return 1;
                } else {
                    System.out.println("<INFO>simulation finished</INFO>");
                    return -1;
                }
            } else {
                System.out.println("<INFO>simulation finished</INFO>");
                return -1;
            }
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Process(Queue que,Time tmr)\">"
                            + e + "</EXCEPTION>");
            return -1;
        }
    }

    public void Create_Queues(int amount) {
        try {
            que_arr = new ArrayList[amount];
            for (int i = 0; i < amount; i++) {
                que_arr[i] = new ArrayList();
            }
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Create_Queues(int amount)\">"
                            + e + "</EXCEPTION>");
        }
    }

    private void Add_Queues_Elem(Transact tr, int el) {
        try {
            if (need_sort == 1) {
                sort(el);
                need_sort = 0;
            }
            /**
             * We at the last position, everything was sorted 0 - highest last -
             * lowest
             */
            int count = que_arr[el].size();
            Transact tmp;
            que_arr[el].add(count, tr);
            int i = count - 1;
            if (count > 1) {
                while (true && i > 0) {
                    if (((Transact) que_arr[el].get(i - 1)).priority > ((Transact) que_arr[el]
                            .get(i)).priority) {
                        tmp = (Transact) que_arr[el].remove(i);
                        que_arr[el].add(i - 1, tmp);
                    } else {
                        break;
                    }
                    i--;
                }
            }
            System.out.println("<ENVIROMENT>\n\t\t\t<QUEUE id=" + el
                    + " action=add stored=" + que_arr[el].size()
                    + "></QUEUE>\n</ENVIROMENT>");
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Add_Queues_Elem( Transact tr, int el)\">"
                            + e + "</EXCEPTION>");
        }
    }

    private Transact Rem_Queues_Elem(int el) {
        try {
            Transact tr;
            tr = new Transact();
            tr = (Transact) que_arr[el].remove(0);
            System.out.println("<ENVIROMENT>\n\t\t\t<QUEUE id=" + el
                    + " action=remove stored=" + que_arr[el].size()
                    + "></QUEUE>\n</ENVIROMENT>");
            return tr;
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Rem_Queues_Elem( Transact tr, int el)\">"
                            + e + "</EXCEPTION>");
            return null;
        }
    }

    public void Create_Fac(int amount) {
        try {
            facility_lock = new int[amount];
            for (int i = 0; i < amount; i++) {
                facility_lock[i] = 0;
            }
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Create_Fac(int amount)\">"
                            + e + "</EXCEPTION>");
        }
    }

    private int Add_Fac_Elem(int el) {
        try {
            if (facility_lock[el] == 0) //If facility does not occuped
            {
                facility_lock[el] = 1;
                return 0;
            } else {
                System.out.println("<WARNING>ocupped facility=" + el
                        + "<WARNING>");
                return 1;
            }
        } catch (Exception e) {
            System.out.println("<EXCEPTION function=\"Add_Fac_Elem(int el)\">"
                    + e + "</EXCEPTION>");
            return -1;
        }
    }

    private int Rem_Fac_Elem(int el) {
        try {
            if (facility_lock[el] == 1) {
                facility_lock[el] = 0;
                return 0;
            } else {
                System.out.println("<ERROR>empty or does not exist, facility="
                        + el + "</ERROR>");
                return 1;
            }
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Rem_Fac_Elem(  int el)\">"
                            + e + "</EXCEPTION>");
            ;
            return -1;
        }
    }

    public void Create_Storages(int num) {
        try {
            storage = new int[num][];
            for (int i = 0; i < num; i++) {
                storage[i] = new int[2];
                storage[i][0] = 0;
                storage[i][1] = 0;
            }
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Create_Storages(int num)\">"
                            + e + "</EXCEPTION>");
            ;
        }
    }

    public void Alloc_Storage(int id, int size) {
        try {
            storage[id][0] = size;
            storage[id][1] = 0;
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Alloc_Storage(int id,int size)\">"
                            + e + "</EXCEPTION>");
            ;
        }
    }

    private int Add_To_Storage(int id, int mem) {
        try {
            storage[id][1] = storage[id][1] + mem;
            if (storage[id][0] > storage[id][1]) {
                System.out.println("<ENVIROMENT>\n\t\t\t<STORAGE id=" + id
                        + " action=add amount=" + mem + " stored="
                        + storage[id][1] + "></STORAGE>\n</ENVIROMENT>");
                return 1;
            } else {
                storage[id][1] = storage[id][1] - mem;
                System.out.println("<ERROR>overloaded storage=" + id
                        + "</ERROR>");
                return -1;
            }
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Add_To_Storage(int id, int mem)\">"
                            + e + "</EXCEPTION>");
            ;
            return -1;
        }
    }

    private int Delete_From_Storage(int id, int mem) {
        try {
            storage[id][1] = storage[id][1] - mem;
            if (storage[id][1] >= 0) {
                System.out.println("<ENVIROMENT>\n\t\t\t<STORAGE id=" + id
                        + " action=remove amount=" + mem + " stored="
                        + storage[id][1] + "></STORAGE>\n</ENVIROMENT>");
                return 1;
            } else {
                storage[id][1] = storage[id][1] + mem;
                System.out.println("<WARNING>underloaded, storage=" + id
                        + "<WARNING>");
                return -1;
            }
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Delete_From_Storage(int id, int mem)\">"
                            + e + "</EXCEPTION>");
            ;
            return -1;
        }
    }

    public void Create_Assembles(int num) {
        try {
            ass_hop = new int[num][2];
            ass_arr = new Transact[num];
            for (int i = 0; i < num; i++) {
                ass_hop[i][0] = 0;
                ass_hop[i][1] = 0;
            }
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Create_Assembles(int num)\">"
                            + e + "</EXCEPTION>");
            ;
        }
    }

    public void Assign_Assemble(int id, int hops) {
        try {
            ass_hop[id][0] = hops;
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Assign_Assemble(int id,int hops)\">"
                            + e + "</EXCEPTION>");
            ;
        }
    }

    public void Create_Tabulates(int num) {
        try {
            /* if (num > 2) { */
            tabs = new int[num][][];
            tabs_param = new int[num];
            tab_names = new String[num];
            tabs_amount = num;
            /*
             * } else { System.out.println(" <WARNING>Too small tabulate
             * <WARNING>"); }
             */
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Create_Tabulates(int num)\">"
                            + e + "</EXCEPTION>");
        }
    }

    public void Add_Table(int id, int parcode, int n, int m, int shift,
            String name) {
        try {
            tabs_param[id] = parcode;//Parameter number
            tabs[id] = new int[n][4];
            tab_names[id] = name;
            for (int i = 0; i < n; i++) {
                tabs[id][i][0] = 0;//Initial values
            }
            tabs[id][0][1] = n;//Start
            tabs[id][0][2] = m;//How many
            tabs[id][0][3] = shift;//Hstogram shift
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Add_Table(int id, int n, int m, int par)\">"
                            + e + "</EXCEPTION>");
        }
    }

    public int Chain_Size() {
        try {
            return Global_Chain.size();
        } catch (Exception e) {
            System.out.println("<EXCEPTION function=\"Chain_Size()\">" + e
                    + "</EXCEPTION>");
            return -1;
        }
    }

    public void Labels(ArrayList in) {
        try {
            labels = new ArrayList();
            labels = in;
        } catch (Exception e) {
            System.out.println("<EXCEPTION function=\"Labels(ArrayList in)\">"
                    + e + "</EXCEPTION>");
        }
    }

    /*
     * Just adds new variable with name and zero value
     */
    public void Add_variable(String name) {
        try {
            vars.AddValue(name, 0);
        } catch (Exception e) {
            System.out
                    .println("<EXCEPTION function=\"Add_variable(Sting name)\">"
                            + e + "</EXCEPTION>");
        }
    }

    /*
     * Print information of current queues, facilities and storages state
     */
    public void Print_state(Queue que, Time tmr) {
        try {
            System.out.println("<SYS_QUEUES>");
            System.out.println("\t\t\t<ENVIROMENT currrent_event_count="
                    + que.get_CEC() + "></ENVIROMENT>");
            System.out.println("\t\t\t<ENVIROMENT future_event_count="
                    + que.get_FEC() + "></ENVIROMENT>");
            que.Print();
            System.out.println("</SYS_QUEUES>");
            System.out.println("<QUEUES>");
            for (int i = 0; i < que_arr.length; i++) {
                ArrayList fque_arr;
                fque_arr = new ArrayList();
                fque_arr.addAll(que_arr[i]);
                for (int j = 0; j < fque_arr.size(); j++)
                    System.out.println("\t\t\t<ENVIROMENT queque[" + i
                            + "]_member[" + j + "]id="
                            + ((Transact) fque_arr.get(j)).ID
                            + "></ENVIROMENT>");
            }
            System.out.println("</QUEUES>");
            System.out.println("<FACILITIES>");
            for (int i = 0; i < que_arr.length; i++) {
                System.out.println("\t\t\t<ENVIROMENT facility[" + i
                        + "]_lock=" + facility_lock[i] + "></ENVIROMENT>");
            }
            System.out.println("</FACILITIES>");
            System.out.println("<STOREGES>");
            for (int i = 0; i < storage.length; i++) {
                System.out.println("\t\t\t<ENVIROMENT storage[" + i + "]_size="
                        + storage[i][0] + "></ENVIROMENT>");
                System.out.println("\t\t\t<ENVIROMENT storage[" + i
                        + "]_occuped_size=" + storage[i][1] + "></ENVIROMENT>");
            }
            System.out.println("</STOREGES>");
            System.out.println("<VARIABLES>");
            System.out.println("\t\t\t<ENVIROMENT count = >"
                    + vars.ReturnCount() + "</ENVIROMENT>");

            for (int i = 0;i<vars.ReturnCount();i++)
            {
                System.out.println("\t\t\t<ENVIROMENT var["+vars.Get_Name(i)+"]="+vars.Get_Val(i)+"</ENVIROMENT>");
            }
            
            System.out.println("</VARIABLES>");
        } catch (Exception e) {
            System.out.println("<EXCEPTION function=\"Print_state()\">" + e
                    + "</EXCEPTION>");
        }
    }

    /**
     * Here we have the slowest part of the modelling itaration
     */
    /**
     * @(#)SortAlgorithm.java 1.6f 95/01/31 James Gosling
     * 
     * Copyright (c) 1994-1995 Sun Microsystems, Inc. All Rights Reserved.
     * 
     * Permission to use, copy, modify, and distribute this software and its
     * documentation for NON-COMMERCIAL or COMMERCIAL purposes and without fee
     * is hereby granted. Please refer to the file
     * http://java.sun.com/copy_trademarks.html for further important copyright
     * and trademark information and to http://java.sun.com/licensing.html for
     * further important licensing information for the Java (tm) Technology.
     * 
     * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
     * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
     * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE,
     * OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY
     * LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR
     * ITS DERIVATIVES.
     * 
     * THIS SOFTWARE IS NOT DESIGNED OR INTENDED FOR USE OR RESALE AS ON-LINE
     * CONTROL EQUIPMENT IN HAZARDOUS ENVIRONMENTS REQUIRING FAIL-SAFE
     * PERFORMANCE, SUCH AS IN THE OPERATION OF NUCLEAR FACILITIES, AIRCRAFT
     * NAVIGATION OR COMMUNICATION SYSTEMS, AIR TRAFFIC CONTROL, DIRECT LIFE
     * SUPPORT MACHINES, OR WEAPONS SYSTEMS, IN WHICH THE FAILURE OF THE
     * SOFTWARE COULD LEAD DIRECTLY TO DEATH, PERSONAL INJURY, OR SEVERE
     * PHYSICAL OR ENVIRONMENTAL DAMAGE ("HIGH RISK ACTIVITIES"). SUN
     * SPECIFICALLY DISCLAIMS ANY EXPRESS OR IMPLIED WARRANTY OF FITNESS FOR
     * HIGH RISK ACTIVITIES.
     */
    /**
     * A generic sort demonstration algorithm SortAlgorithm.java, Thu Oct 27
     * 10:32:35 1994
     * 
     * @author James Gosling with Andrey Bondarenko types modifications
     * @version 1.6f, 31 Jan 1995
     */
    private void QuickSort(Transact a[], int l, int r) throws Exception {
        int M = 4;
        int i = 0;
        int j = 0;
        Transact v;
        if ((r - l) > M) {
            i = (r + l) / 2;
            if (a[l].priority > a[i].priority)
                swap(a, l, i); // Tri-Median Methode!
            if (a[l].priority > a[r].priority)
                swap(a, l, r);
            if (a[i].priority > a[r].priority)
                swap(a, i, r);
            j = r - 1;
            swap(a, i, j);
            i = l;
            v = a[j];
            for (;;) {
                while (a[++i].priority < v.priority)
                    ;
                while (a[--j].priority > v.priority)
                    ;
                if (j < i)
                    break;
                swap(a, i, j);
            }
            swap(a, i, r - 1);
            QuickSort(a, l, j);
            QuickSort(a, i + 1, r);
        }
    }

    private void swap(Transact a[], int i, int j) {
        Transact T;
        T = a[i];
        a[i] = a[j];
        a[j] = T;
    }

    private void InsertionSort(Transact a[], int lo0, int hi0) throws Exception {
        int i;
        int j;
        Transact v;
        for (i = lo0 + 1; i <= hi0; i++) {
            v = a[i];
            j = i;
            while ((j > lo0) && (a[j - 1].priority > v.priority)) {
                a[j] = a[j - 1];
                j--;
            }
            a[j] = v;
        }
    }

    private void sort(int id) throws Exception {
        /**
         * This ugly function saves queue[id] in temporary storage and returns
         * transacts in queue[id] after processing
         */
        Transact a[];
        int c = que_arr[id].size();
        if (c > 1) /** No need to sort one-element array */
        {
            a = new Transact[c];
            for (int i = 0; i < c; i++) {
                a[i] = (Transact) que_arr[id].remove(0);
            }
            QuickSort(a, 0, c - 1);
            InsertionSort(a, 0, c - 1);
            for (int i = 0; i < c; i++) {
                que_arr[id].add(a[i]);
            }
        }
    }
}
