package juliet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.*;

/**
 * Scheduler class.
 * Main class for the Scheduler program. Currently a placeholder to manual test
 * the program. Upon execution it prompts the user to input values to set up
 * the parent teacher meeting sessions (# of teachers, # of parents in 
 * the sessions, min/max interviews allowed, etc). Once the program has received
 * all the inputs successfully it inputs the parameters into the 
 * RandomGenerator program to generate a text file of the instance describing
 * the set up meeting night scenario and is then inputted into the Scheduler
 * program to set up initial values of all the variables. Upon doing so the
 * Strategy class is called and attempts to create a feasible schedule based
 * on given input and outputs the schedule produced in a text file.
 */
public class Scheduler {
    
    /**
     * Default constructor
     */
    public Scheduler() {
        this.parents = new HashSet<Parent>();
        this.teachers = new HashSet<Teacher>();
    }

    // Instance variables
    private Set<Parent> parents;
    private Set<Teacher> teachers;
    private Map<Integer, Teacher> teachersById;
    
    // DB connection params
    private boolean hasDBArgs;
    private String dbFile;
    private String dbSchema;
    private String dbUser;
    private String dbPass;

    // Written to the schedule table in the DB
    private String comment = "";
    
    // File params
    private boolean hasFileArgs;
    private String inFile;
    private BufferedReader inBufRead;
    private String outFile;
    private PrintStream outStream;
    private boolean overwrite;
    private int afternoonSlots = -1;
    private int eveningSlots = -1;
    private int numTeachers;
	private int dayId;
    
    
    /**
     * Main method
     * @param args 
     */
    public static void main(String[] args){

        // New instance
        Scheduler pgm = new Scheduler();

        // Set-up and parse CL-args
        cliSetup(pgm, args);
        
        // Populate parents+teachers
        try {
            pgm.loadData();
        } catch (Exception e) {
        	e.printStackTrace();
            System.err.println("Exiting. Exception while loading data: " +
                    e.getMessage());
            System.exit(1);
        }
        
        // Do the schedule
        new Strategy(pgm.parents, pgm.afternoonSlots, pgm.eveningSlots);
        // This ^^ is weird now. maybe should be a static function smth like:
        // Strategy.schedule(p, a, e)?
        // TODO: Refactor Strategy?
        
        OutputModule outputModule = new OutputModule(pgm.parents, pgm.teachers);
        //outputModule.WriteParentsToCSV("OutputParentData.csv");
        //outputModule.WriteTeachersToCSV("OutputTeacherData.csv");
        outputModule.WriteReqsVsSchedToCSV("ReqsVsSched.csv");
        

        try {
            pgm.writeSchedule(pgm.parents, pgm.teachers);
        } catch (Exception e) {
            System.out.println("Exiting. Exception while writing data: " +
                    e.getMessage());
            System.exit(1);
        }


    }
    
    /**
     * Write to DB or file based on settings.
     * @param p parents
     * @param t teachers
     * @throws Exception 
     */
    private void writeSchedule(Set<Parent> p, Set<Teacher> t) throws
        Exception {
        if (this.hasDBArgs) {
            this.writeScheduleToDB(p, t);
        } else if (this.hasFileArgs) {
            this.writeScheduleToFile(p, t);
        }
    }

    /**
     * Insert schedule data into DB
     * @param p parents
     * @param t teachers
     * @throws Exception 
     */
    private void writeScheduleToDB(Set<Parent> parents, Set<Teacher> teachers)
        throws Exception {
        // TODO: Complete this.

        int i;
        
        // get a new database connection
        DBConn dbc = new DBConn(this.dbFile, this.dbSchema, this.dbUser,
                this.dbPass, this.dayId, this.comment);
        
        
        
        for (Teacher t : teachers) {
            // save the afternoon slots
            for(i=0; i < this.afternoonSlots; i++) {
                Parent pt = t.GetAftAppts().get(i);
                if (pt != null) {
                    // add to the database
                    dbc.addAppointment(pt.getId(), t.getId(), i, Parent.Session.AFTERNOON);
                }
            }
            // save the evening slots
            for(i=0; i < this.eveningSlots; i++) {
                Parent pt = t.GetEveAppts().get(i);
                if (pt != null) {
                    // add to the database
                    dbc.addAppointment(pt.getId(), t.getId(), i, Parent.Session.EVENING);
                }
            }
        }

    }

    /**
     * Output a tabular view of the whole schedule.
     * @param p parents
     * @param t teachers
     */
    private void writeScheduleToFile(Set<Parent> p, Set<Teacher> t) {
        // We'll print afternoon then evening with the teachers as columns,
        // and the time slots as rows.
        
        int fieldWidth = 4;
        final String fieldBlank    = "     ";
        final String fieldNoAppt   = "   --";
        final String headerSeg     = "-----";
        final String fieldFormat   = " % "+fieldWidth+"d";
        final String slotFormat    = "%-"+fieldWidth+"d|";
        String headerLine          = "";
        
        // Per-schedule stats
        int nSched = 0;
        int nUnsched = 0;
        
        // Decide on an ordering (arbitrarily) for the teachers
        List<Teacher> tOrder = new ArrayList<Teacher>(this.numTeachers);
        Iterator<Teacher> iT = this.teachers.iterator();
        while (iT.hasNext())
            tOrder.add(iT.next());

        // Print a header for the table
        this.outStream.print(fieldBlank);
        iT = tOrder.iterator();
        while (iT.hasNext()) {
            this.outStream.printf(fieldFormat, iT.next().getId());
            headerLine = headerLine + headerSeg;
        }
        
        // Afternoon
        this.outStream.println("\nAFT +" + headerLine);        
        // Iterate through the time slots in aft
        for (int i = 0; i < this.afternoonSlots; i++) {
            iT = tOrder.iterator();
            this.outStream.printf(slotFormat, i);
            // Iterate through ordering of teachers,
            // printing appointment (if any)
            while (iT.hasNext()) {
                Teacher currentTeacher = iT.next();
                Parent appt = currentTeacher.GetAftAppts().get(i); 
                if (appt == null) {
                    this.outStream.print(fieldNoAppt);
                    nUnsched++;
                } else {
                    this.outStream.printf(fieldFormat, appt.getId());
                    nSched++;
                }
            }
            this.outStream.print("\n"); // End of row
        }
        
        // Space between aft/eve
        this.outStream.println("\nEVE +" + headerLine);
        
        // Iterate through the time slots in eve
        for (int i = 0; i < this.eveningSlots; i++) {
            iT = tOrder.iterator();
            this.outStream.printf(slotFormat, i);
            // Iterate through ordering of teachers,
            // printing appointment (if any)
            while (iT.hasNext()) {
                Teacher currentTeacher = iT.next();
                Parent appt = currentTeacher.GetEveAppts().get(i); 
                if (appt == null) {
                    this.outStream.print(fieldNoAppt);
                    nUnsched++;
                } else {
                    this.outStream.printf(fieldFormat, appt.getId());
                    nSched++;
                }
            }
            this.outStream.print("\n"); // End of row
        }
        
        // Per parent data
        int idleTime;
        String sched, toCall;
        Teacher currentT;
        Parent currentP;
        this.outStream.println("\n-- Per Parent data ----- "
                + "(Id Sess Idle/TotTime Appts/Reqs Schedule|Unscheduled)");
        Iterator<Parent> iP = this.parents.iterator();
        while (iP.hasNext()) {
            
            sched = "";
            toCall = "|";
            currentT = null;
            currentP = iP.next();
            
            if (currentP.getSession() == Parent.Session.AFTERNOON) {

                Iterator<Teacher> iAft = currentP.GetApptsAft().iterator();
                while (iAft.hasNext()) {
                    currentT = iAft.next();
                    if (currentT !=  null) {
                        sched = String.format("%s% 4d",
                                sched, currentT.getId());
                    } else {
                        sched = sched + "  --";
                    }
                    if (iAft.hasNext())
                        sched = sched + " ";
                }
            } else if (currentP.getSession() == Parent.Session.EVENING) {
                // Evening appts
                Iterator<Teacher> iEve = currentP.GetApptsEve().iterator();
                while (iEve.hasNext()) {
                    currentT = iEve.next();
                    if (currentT != null) {
                        sched = String.format("%s% 4d",
                                sched, currentT.getId());
                    } else {
                        sched = sched + "  --";
                    }
                    if (iEve.hasNext())
                        sched = sched + " ";
                }
            }
            
            // Uscheduled requests
            if (currentP.GetToCall().size() > 0) {
                Iterator<Teacher> iToCall = currentP.GetToCall().iterator();
                while (iToCall.hasNext()) {
                    currentT = iToCall.next();
                    if (currentT != null) {
                        toCall = String.format("%s% 4d",
                                toCall, currentT.getId());
                        if (iToCall.hasNext())
                            toCall = toCall + " ";
                    }
                }
            }
            
            idleTime = currentP.GetTotalTime() -
                7*currentP.GetNumberOfScheduledRequests();
            
            this.outStream.printf("% 4d %s% 4d/%-4d% 3d/%-3d %s %s\n",
                    currentP.getId(), currentP.getSession(),
                    idleTime, currentP.GetTotalTime(),
                    currentP.GetNumberOfScheduledRequests(),
                    currentP.GetNumberOfRequests(),
                    sched, toCall);
        }
        
        // Per teacher data
        String aftSched, eveSched;
//        Teacher curTeacher;
        this.outStream.println("\n\n-- Per Teacher data ----- "
                + "(Id Appts/Reqs AftSched|EveSchedule|Unscheduled)");
        iT = this.teachers.iterator();
        while (iT.hasNext()) {
            aftSched = "";
            eveSched = "|";
            toCall = "|";
            currentP = null;
            currentT = iT.next();
            
            // Aft appts
            Iterator<Parent> iAft = currentT.GetAftAppts().iterator();
            while (iAft.hasNext()) {
                currentP = iAft.next();
                if (currentP !=  null) {
                    aftSched = String.format("%s% 4d",
                            aftSched, currentP.getId());
                } else {
                    aftSched = aftSched + "  --";
                }
                if (iAft.hasNext())
                    aftSched = aftSched + " ";
            }
            
            // Eve appts
            Iterator<Parent> iEve = currentT.GetEveAppts().iterator();
            while (iEve.hasNext()) {
                currentP = iEve.next();
                if (currentP !=  null) {
                    eveSched = String.format("%s% 4d",
                            eveSched, currentP.getId());
                } else {
                    eveSched = eveSched + "  --";
                }
                if (iEve.hasNext())
                    eveSched = eveSched + " ";
            }
            
            // Uscheduled requests
            Set<Parent> toCallAll = currentT.getToCallAll();
            if (toCallAll.size() > 0) {
                Iterator<Parent> iToCall = toCallAll.iterator();
                while (iToCall.hasNext()) {
                    currentP = iToCall.next();
                    if (currentP != null) {
                        toCall = String.format("%s% 4d",
                                toCall, currentP.getId());
                        if (iToCall.hasNext())
                            toCall = toCall + " ";
                    }
                }
            }
            
            this.outStream.printf("% 4d % 3d/%-3d %s %s %s\n",
                    currentT.getId(),
                    currentT.GetTotalSchedAmount(),
                    currentT.GetTotalReqAmount(),
                    aftSched, eveSched, toCall);
            
        }
         
        
    }

    /**
     * Populate our parents and teachers members from file or db based on
     * command-line arguments.
     * @throws Exception 
     */
    private void loadData() throws Exception {
        if (this.hasDBArgs) {
            this.loadDataFromDB();
        } else if (this.hasFileArgs) {
            this.loadDataFromFile();
        }
    }

    /**
     * Populate from DB
     * @throws Exception 
     */
    private void loadDataFromDB() throws Exception {
        try {
            // get a new database connection
            DBConn dbc = new DBConn(this.dbFile, this.dbSchema, this.dbUser,
                    this.dbPass, this.dayId);
            // get the parents for this session
            this.parents = dbc.getParentsForCurrSession();
            // get the parent requests for this session
            HashMap<Integer, ArrayList<Integer>> 
                requests = dbc.getRequestsForCurrSession();

            this.afternoonSlots = dbc.getAftSlots();
            this.eveningSlots = dbc.getEveSlots();
            this.numTeachers = dbc.getNumTeachersForCurrSession();
            this.teachersById = new HashMap<Integer, Teacher>(this.numTeachers);
            // add the requests in every Parent object
            // create the teachers as we go along
            for (Parent p : this.parents) {
                ArrayList<Integer> teacherIds = requests.get(p.getId());
                for (int tid : teacherIds) {
                    p.addRequest(this.getTeacherById(tid));
                }
            }
        } catch (ClassNotFoundException cnfe) {

        }
    }

    /**
     * Populate parents and teachers from file (or stdin)
     * 
     * @throws IOException 
     * @throws NumberFormatException 
     */
    private void loadDataFromFile() throws NumberFormatException, IOException {
    
        // Read first line, get teacher structures ready.
        this.numTeachers = Integer.parseInt(this.inBufRead.readLine());
        this.teachersById = new HashMap<Integer,Teacher>(numTeachers);
        
        // Build a parent from each line of the input
        String line;
        while ((line = this.inBufRead.readLine()) != null) {
            
            Parent p = new Parent(this.afternoonSlots, this.eveningSlots);
            String parentLine[] = line.split(";");
            
            // ID
            p.setId(Integer.parseInt(parentLine[0]));
            
            // Requests
            String requests[] = parentLine[2].split(",");
            for (int i = 0; i < requests.length; i++) {
                p.addRequest(this.getTeacherById(Integer.parseInt(requests[i])));
            }
            
            // Session
            if (parentLine[3].equals("A"))
                p.setSession(Parent.Session.AFTERNOON);
            if (parentLine[3].equals("E"))
                p.setSession(Parent.Session.EVENING);
            if (parentLine[3].equals("B"))
                p.setSession(Parent.Session.BOTH);
            
            this.parents.add(p);
            
        }
    
    }

    /**
     * Set up and parse command-line arguments
     * 
     * @param pgm Scheduler instance
     * @param args command-line
     */
    @SuppressWarnings("static-access")
    private static void cliSetup(Scheduler pgm, String[] args) {

        // Configure options
        Options opts = new Options();
        
        opts.addOption( new Option("help", "print this message") );
        
        opts.addOption( OptionBuilder.withArgName("filename")
                .hasArg()
                .withType(new String())
                .withDescription("output filename (default is stdout)")
                .create('o') );
        
        opts.addOption( OptionBuilder.withArgName("filename")
                .hasArg()
                .withType(new String())
                .withDescription("input filename (default is stdin)")
                .create('i') );
        
        opts.addOption( new Option("f",
                "force overwrite if output file exists") );
        
        opts.addOption( OptionBuilder.withArgName("int")
                .hasArg()
                .withDescription("number of afternoon slots")
                .create('a') );
        
        opts.addOption( OptionBuilder.withArgName("int")
                .hasArg()
                .withDescription("number of evening slots")
                .create('e') );
        
        opts.addOption( OptionBuilder.withArgName("filename")
                .hasArg()
                .withType(new String())
                .withDescription("database filename")
                .create("db") );
        
        opts.addOption( OptionBuilder.withArgName("schema")
                .hasArg()
                .withType(new String())
                .withDescription("database schema")
                .create("dbs") );

        opts.addOption( OptionBuilder.withArgName("comment")
                .hasArg()
                .withType(new String())
                .withDescription("database schedule comment")
                .create("dbc") );
        
        opts.addOption( OptionBuilder.withArgName("id")
                .hasArg()
                .withType(new String())
                .withDescription("id of day to schedule")
                .create("dbd") );
        
        opts.addOption( OptionBuilder.withArgName("user")
                .hasArg()
                .withType(new String())
                .withDescription("database username")
                .create("u") );
        
        opts.addOption( OptionBuilder.withArgName("pass")
                .hasArg()
                .withType(new String())
                .withDescription("database password")
                .create("p") );
        
        
        // Parse args, do basic sanity checks
        CommandLineParser parser = new PosixParser();
        try {
            CommandLine cl = parser.parse(opts, args);
            int optI;
            
            if (cl.hasOption("help")) {
                printHelp(opts);
                System.exit(0);
            }
            
            pgm.overwrite = cl.hasOption("f");
            pgm.hasFileArgs = pgm.overwrite;
            
            // -o output file
            if (cl.hasOption('o')) {
                pgm.hasFileArgs = true;
                pgm.outFile = cl.getOptionValue('o');
                
                File out = new File(pgm.outFile);
                
                // Can't use this file? Throw an exception
                if (out.exists() && !pgm.overwrite) {
                        throw new Exception("File " + pgm.outFile +
                                " exists, set -f to force overwrite.");
                }
                
                // Otherwise, try to set us up for output. This may throw a
                // FileNotFoundException if it can't create the file. If the
                // file exists here it gets truncated.
                pgm.outStream = new PrintStream(out);
            } else {
                // Didn't specify an output file, use stdout
                pgm.outStream = System.out;
            }
            
            // -i input file
            if (cl.hasOption('i')) {
                pgm.hasFileArgs = true;
                pgm.inFile = cl.getOptionValue('i');

                File in = new File(pgm.inFile);
                
                // Set up buffered reader, or throw something if we can't
                // read the file
                if (in.canRead()) {
                   pgm.inBufRead = new BufferedReader(new FileReader(in));
                } else {
                    throw new Exception("File " + pgm.inFile
                            + "can not be read.");
                }
                
            } else {
                // Didn't specify an input file, use stdin
                pgm.inBufRead = new BufferedReader(new InputStreamReader(
                        System.in));
            }
            
            // -a afternoon slots
            if (cl.hasOption('a')) {
                pgm.hasFileArgs = true;
                optI = Integer.parseInt(cl.getOptionValue('a'));
                if (optI < 1)
                    throw new ParseException("\"a\" may not be < 1 "
                            + "(given " + optI + ")");
                pgm.afternoonSlots = optI;
            }
            
            // -e evening slots
            if (cl.hasOption('e')) {
                pgm.hasFileArgs = true;
                optI = Integer.parseInt(cl.getOptionValue('e'));
                if (optI < 1)
                    throw new ParseException("\"e\" may not be < 1 "
                            + "(given " + optI + ")");
                pgm.eveningSlots = optI;
            }
            
            // -db db file
            if (cl.hasOption("db")) {
                pgm.hasDBArgs = true;
                // Check now? just when we try to connect?
                pgm.dbFile = cl.getOptionValue("db");
            }
            
            // -dbs db schema
            if (cl.hasOption("dbs")) {
                pgm.hasDBArgs = true;
                pgm.dbSchema = cl.getOptionValue("dbs");
            }

            // -dbc db comment
            if (cl.hasOption("dbc")) {
                pgm.hasDBArgs = true;
                pgm.comment = cl.getOptionValue("dbc");
            }
            
            // -dbd day id
            if (cl.hasOption("dbd")) {
                pgm.hasDBArgs = true;
                optI = Integer.parseInt(cl.getOptionValue("dbd"));
                if (optI < 1)
                    throw new ParseException("\"dbd\" may not be < 1 "
                            + "(given " + optI + ")");
                pgm.dayId = optI;
            }
            
            // -u db username
            if (cl.hasOption("u")) {
                pgm.hasDBArgs = true;
                pgm.dbUser = cl.getOptionValue("u");
            }
            
            // -u db password
            if (cl.hasOption("p")) {
                pgm.hasDBArgs = true;
                pgm.dbPass = cl.getOptionValue("p");
            }
            
            // User set too many params
            if ( pgm.hasFileArgs && pgm.hasDBArgs ) {
                printHelp(opts);
                throw new Exception("You may not set both file and database "
                        + "parameters");
            }
            
            // User didn't set any params, print help, quit.
            if ( !pgm.hasFileArgs && !pgm.hasDBArgs ) {
                printHelp(opts);
                System.exit(0);
            }
            
            // Did not specify one or both slots
            if (pgm.hasFileArgs)
                if (pgm.afternoonSlots < 0) {
                    printHelp(opts);
                    throw new Exception("You must set the number of after"
                            + "noon time slots. (-a)");
                } else if (pgm.eveningSlots < 0) {
                    printHelp(opts);
                    throw new Exception("You must set the number of evening"
                            + " time slots. (-e)");
                }
            
        } catch (Exception e) {
            System.err.println("Exiting. Exception while parsing "
                    + "command-line: " + e.getMessage());
            System.exit(1);
        }
        
    }

    /**
     * Prints the usage message.
     * 
     * @param opts
     */
    private static void printHelp(Options opts) {
        HelpFormatter help = new HelpFormatter();
        help.printHelp("java -jar juliet.jar [DB PARAMS || FILE PARAMS]",
                "\n\nAttempts to create a schedule for the request " +
                "data given.\n\nUsers must supply one (not both) of the " +
                "following sets of parameters:\nDB PARAMS: -db <filename> " +
                "-dbs <schema> -u <user> -p <pass>\nFILE PARAMS: [-i <file>]" +
                " [-o <file>] [-f] -a <int> -e <int>\n\nIf the input " +
                "and output files are not given, we'll use stdin and " +
                "stdout.\n\nIf both file and DB parameters are " +
                "supplied, the program will exit.",
                opts,
                "",
                false);
    }

    /**
     * If one already exists, return the Teacher object matching the given
     * id. If one does not exist yet, create one and return it. We're creating
     * teachers based on the requests attached to the parents rather than
     * assuming that the teacher ids will always be 0..numTeachers.
     * @param id
     * @return Teacher matching the given id
     */
    private Teacher getTeacherById(int id) {
        Teacher ret = this.teachersById.get(new Integer(id));
        if (ret == null) {
            // Don't have that teacher yet, create and add to our index.
            ret = new Teacher(this.afternoonSlots, this.eveningSlots);
            ret.setId(id);
            this.teachersById.put(new Integer(id), ret);
            this.teachers.add(ret);
        }
        return ret;
    }

}
