package juliet.test;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.cli.*;

/**
 * Static class for generating test request data. Takes user-supplied params
 * on the command-line, or uses default values obtained from client discussions
 *
 * @author c1prindi
 */
public class StClementsTestData {

    // Parameters for generated data. Defaults come from our discussions
    // with Ye.
    private static int MAX_PARENT_REQUESTS = 9;
    private static int MIN_PARENT_REQUESTS = 4;
    private static int N_TEACHERS = 65;
    private static int N_PARENTS = 200;
    private static int N_PARENTS_OF_2 = 20;

    // Output parent IDs beginning with this one
    private static int FIRST_ID = 1;
        
    // Output file params (defaults to stdout)
    private static boolean OUT_OVERWRITE = false;
    private static boolean OUT_SQL = false;
    private static String OUT_NAME;
    private static PrintStream OUT_STREAM = System.out; 

    // SQL output params (meaningless without -sql flag)
    private static int SQL_DID = 1;
    private static int SQL_RSID = 1;
    private static final String SQL_QUERY =
        "INSERT INTO ptaweb.requests VALUES (default, %d, %d, %d, %d);\n";
        // Params in the query are as follows:
        //  request id = default
        //  day id = cmd-line option SQL_DID
        //  teacher id
        //  parent id
        //  reg session id = cmd-line option SQL_RSID
    
    // Weights of a parent choosing each session. Probability
    // of each is WT_x / (WT_AFT+WT_EVE+WT_BOTH)
    // Skew towards the evening by default
    private static float WT_AFT =  4.0f;
    private static float WT_EVE =  5.0f;
    private static float WT_BOTH = 1.0f;
    
    static Set<Class> allClasses;
    static Set<Kid> allKids;
    static List<Grade> grades;
    static List<Teacher> teachers;
    static Set<Kid> chosenKids;

    /**
     * Main function
     * 
     * @param args command-line arguments (handled with Apache commons-cli)
     */
    public static void main(String[] args) {
        cli_setup(args);
        setup();
        choose();
        output();
//        outputInfo();
        OUT_STREAM.close();
    }
    
    /*
     * Set up, parse CLI options
     */
    @SuppressWarnings("static-access")
    private static void cli_setup(String[] args) {
        
        // Configure options
        Options opts = new Options();
        
        opts.addOption( new Option("help", "print this message") );
        
        opts.addOption( OptionBuilder.withArgName("filename")
                .hasArg()
                .withType(new String())
                .withDescription("output filename (default is stdout)")
                .create('o') );
        
        opts.addOption( new Option("f", "force overwrite if output file exists") );

        opts.addOption( new Option("sql", "use sql output mode. This flag is" +
            " implied by the use of options -sqldid or -sqlrsid") );
        
        opts.addOption( OptionBuilder.withArgName("int")
                .hasArg()
                .withDescription("day id used in SQL INSERTS (default " + SQL_DID + ")" +
                    "\n Setting this option will cause the program to use SQL output." )
                .create("sqldid") );
        
        opts.addOption( OptionBuilder.withArgName("int")
                .hasArg()
                .withDescription("reg session id used in SQL INSERTS (default " + SQL_RSID + ")" +
                    "\n Setting this option will cause the program to use SQL output." )
                .create("sqlrsid") );
        
        opts.addOption( OptionBuilder.withArgName("int")
                .hasArg()
                .withDescription("parent ID offset (default " + FIRST_ID + ")" +
                    "\n Parent IDs begin at this value and increase.")
                .create("id") );
        
        opts.addOption( OptionBuilder.withArgName("int")
                .hasArg()
                .withDescription("number of teachers (default " + N_TEACHERS + ")")
                .create('t') );
        
        opts.addOption( OptionBuilder.withArgName("int")
                .hasArg()
                .withDescription("number of parents (default " + N_PARENTS + ")")
                .create('p') );
        
        opts.addOption( OptionBuilder.withArgName("int")
                .hasArg()
                .withDescription("number of parents with two kids (default " + N_PARENTS_OF_2 + ")")
                .create("pp") );
        
        opts.addOption( OptionBuilder.withArgName("int")
                .hasArg()
                .withDescription("min interviews parents will request (default " + MIN_PARENT_REQUESTS + ")")
                .create("mini") );
        
        opts.addOption( OptionBuilder.withArgName("int")
                .hasArg()
                .withDescription("max interviews parents will request (default " + MAX_PARENT_REQUESTS + ")")
                .create("maxi") );
        
        opts.addOption( OptionBuilder.withArgName("float")
                .hasArg()
                .withDescription("weight of AFTERNOON session (default " + WT_AFT + "), " + 
                        "probability of parent choosing AFT is wa/(wa + we + wb)")
                .create("wa") );
        
        opts.addOption( OptionBuilder.withArgName("float")
                .hasArg()
                .withDescription("weight of EVENING session (default " + WT_EVE + "), " + 
                        "probability of parent choosing EVE is we/(wa + we + wb)")
                .create("we") );
        
        opts.addOption( OptionBuilder.withArgName("float")
                .hasArg()
                .withDescription("weight of BOTH session (default " + WT_BOTH + "), " + 
                        "probability of parent choosing BOTH is wb/(wa + we + wb)")
                .create("wb") );

        
        // Parse args, do basic sanity checks
        CommandLineParser parser = new PosixParser();
        try { 
            CommandLine cl = parser.parse(opts, args);
            int optI;
            float optF;
            
            if (cl.hasOption("help")) {
                HelpFormatter help = new HelpFormatter();
                help.printHelp("java -classpath juliet.jar " +
                        "juliet.test.StClementsTestData",
                        "\nGenerates test parent-request data according to the" +
                        " given parameters.",
                        opts,
                        "",
                        true);
                System.exit(0);
            }
            
            OUT_OVERWRITE = cl.hasOption("f");
            
            OUT_SQL = cl.hasOption("sql");
            
            if (cl.hasOption('o')) {
                OUT_NAME = cl.getOptionValue('o');
                check_filename();
            }
            
            if (cl.hasOption("sqldid")) {
                optI = Integer.parseInt(cl.getOptionValue("sqldid"));
                if (optI < 0)
                    throw new ParseException("\"sqldid\" may not be less than 0 " +
                            "(given " + optI + ")");
                SQL_DID = optI;
                OUT_SQL = true;
            }
            
            if (cl.hasOption("sqlrsid")) {
                optI = Integer.parseInt(cl.getOptionValue("sqlrsid"));
                if (optI < 0)
                    throw new ParseException("\"sqlrsid\" may not be less than 0 " +
                            "(given " + optI + ")");
                SQL_RSID = optI;
                OUT_SQL = true;
            }
            
            if (cl.hasOption("id")) {
                optI = Integer.parseInt(cl.getOptionValue("id"));
                if (optI < 0)
                    throw new ParseException("\"id\" may not be less than 0 " +
                            "(given " + optI + ")");
                FIRST_ID = optI;
            }
            
            if (cl.hasOption('t')) {
                optI = Integer.parseInt(cl.getOptionValue('t'));
                if (optI < 1)
                    throw new ParseException("\"t\" may not be less than 1 " +
                            "(given " + optI + ")");
                N_TEACHERS = optI;
            }
            
            if (cl.hasOption('p')) {
                optI = Integer.parseInt(cl.getOptionValue('p'));
                if (optI < 1)
                    throw new ParseException("\"p\" may not be less than 1 " +
                            "(given " + optI + ")");
                N_PARENTS = optI;
            }
            
            if (cl.hasOption("pp")) {
                optI = Integer.parseInt(cl.getOptionValue("pp"));
                if (optI < 0)
                    throw new ParseException("\"pp\" may not be negative " +
                            "(given " + optI + ")");
                N_PARENTS_OF_2 = optI;
            }
            
            if (cl.hasOption("mini")) {
                optI = Integer.parseInt(cl.getOptionValue("mini"));
                if (optI < 1)
                    throw new ParseException("\"mini\" may not be less than 1 " +
                            "(given " + optI + ")");
                MIN_PARENT_REQUESTS = optI;
            }
            
            if (cl.hasOption("maxi")) {
                optI = Integer.parseInt(cl.getOptionValue("maxi"));
                if (optI < MIN_PARENT_REQUESTS)
                    throw new ParseException("\"maxi\" may not be less than " +
                            "\"mini\" (given maxi=" + optI +
                            ", mini=" + MIN_PARENT_REQUESTS + ")");
                if (optI > N_TEACHERS)
                    throw new ParseException("\"maxi\" may not be greater " +
                            "than \"t\" (given maxi=" + optI + 
                            ", t=" + N_TEACHERS + ")");
                MAX_PARENT_REQUESTS = optI;
            }
            
            if (cl.hasOption("wa")) {
                optF = Float.parseFloat(cl.getOptionValue("wa"));
                if (optF < 0)
                    throw new ParseException("\"wa\" may not be negative " +
                            "(given " + optF + ")");
                WT_AFT = optF;
            }
            
            if (cl.hasOption("we")) {
                optF = Float.parseFloat(cl.getOptionValue("we"));
                if (optF < 0)
                    throw new ParseException("\"we\" may not be negative " +
                            "(given " + optF + ")");
                WT_EVE = optF;
            }
            
            if (cl.hasOption("wb")) {
                optF = Float.parseFloat(cl.getOptionValue("wb"));
                if (optF < 0)
                    throw new ParseException("\"wb\" may not be negative " +
                            "(given " + optF + ")");
                WT_BOTH = optF;
            }
            
        } catch (Exception e) {
            System.err.println("Exiting. Exception while parsing command-line: " +
                    e.getMessage());
            System.exit(1);
        }
        
    }

    /**
     * Check for conflicting -f and -o options, if they are ok, set up
     * the OUT_STREAM, otherwise throw an exception.
     * 
     * @throws Exception 
     */
    private static void check_filename() throws Exception {
        
        File out = new File(OUT_NAME);
        
        // Can't use this file? Throw an exception
        if (out.exists() && !OUT_OVERWRITE) {
                throw new Exception("File " + OUT_NAME +
                        " exists, set -f to force overwrite.");
        }
        
        // Otherwise, try to set us up for output. This may throw a
        // FileNotFoundException if it can't create the file. If the
        // file exists here it gets truncated.
        OUT_STREAM = new PrintStream(out);
        
    }

    private static void choose() {

        chosenKids = new CopyOnWriteArraySet<Kid>();

        for (int i = 0; (i < grades.size())
                && (chosenKids.size() < (N_PARENTS + N_PARENTS_OF_2)); i++) {
//            OUT_STREAM.println("# " + chosenKids.size() + " kids chosen after " + i + " grades.");
            Grade g = grades.get(i);
            Iterator<Kid> iK = g.getKids().iterator();
            int chooseFromG = (int) (N_PARENTS * (1.0 / grades.size()) * g.parentProbability);
            for (; chooseFromG > 0; chooseFromG--) {
                try {
                    chosenKids.add(iK.next());
                } catch (NoSuchElementException e) {
                    break;
                }
            }
        }
        
        Iterator<Kid> iK = allKids.iterator();
        while (chosenKids.size() < (N_PARENTS + N_PARENTS_OF_2)) {
            try {
                chosenKids.add(iK.next());
            } catch (NoSuchElementException e) {
                break;
            }
        }
        
//        OUT_STREAM.println("# " + chosenKids.size() + " kids chosen.");

    }

    private static void setup() {

        allClasses = new CopyOnWriteArraySet<Class>();
        allKids = new CopyOnWriteArraySet<Kid>();

        // Populate teachers
        teachers = new ArrayList<Teacher>(65);
        for (int i = 0; i < N_TEACHERS; i++) {
            teachers.add(i, new Teacher(i+1));
        }

        // Populate grades
        grades = new ArrayList<Grade>(12);
        grades.add(0, new Grade(20, 0.9f, 20, 0)); // Grade 1
        grades.add(1, new Grade(20, 0.9f, 20, 0)); // Grade 2
        grades.add(2, new Grade(20, 0.9f, 20, 0)); // Grade 3
        grades.add(3, new Grade(20, 0.9f, 20, 0)); // Grade 4
        grades.add(4, new Grade(20, 0.9f, 20, 0)); // Grade 5
        grades.add(5, new Grade(20, 0.9f, 20, 0)); // Grade 6
        grades.add(6, new Grade(47, 0.9f, 20, 4)); // Grade 7
        grades.add(7, new Grade(47, 0.6f, 20, 4)); // Grade 8
        grades.add(8, new Grade(60, 0.9f, 20, 15)); // Grade 9
        grades.add(9, new Grade(60, 0.6f, 20, 15)); // Grade 10
        grades.add(10, new Grade(60, 0.6f, 20, 16)); // Grade 11
        grades.add(11, new Grade(60, 0.9f, 20, 16)); // Grade 12

        // Create kids
        for (int i = 0; i < grades.size(); i++) {
            Grade g = grades.get(i);
            int nKids = g.getNKids();

            for (int j = 0; j < nKids; j++) {
                Kid k = new Kid(g);

                for (int n = 0; n < Kid.MAX_CLASSES; n++)
                    k.addAClass();
            }
        }

        // Assign teachers to classes round-robin
        Iterator<Class> iClasses = allClasses.iterator();
        Iterator<Teacher> iTeachers = teachers.iterator();
        while (iClasses.hasNext()) {
            Class c = iClasses.next();
            if (!iTeachers.hasNext())
                iTeachers = teachers.iterator();
            c.setTeacher(iTeachers.next());
        }
    }

    /** Do output in a file format that the scheduler can understand, unless
     * OUT_SQL is true (got -sql on the command-line) in which case, output SQL
     * queries (for use in testing web-interface).
     */
    private static void output() {

        if (!OUT_SQL)
            OUT_STREAM.println(teachers.size());
        
        assert (chosenKids.size() == (N_PARENTS + N_PARENTS_OF_2));
        
        Iterator<Kid> iK = chosenKids.iterator();
        Kid k;
        Set<Teacher> requests;
        for (int i = FIRST_ID;
            (i <= (N_PARENTS + FIRST_ID)) && (iK.hasNext());
            i++) {

            k = iK.next();
            
            requests = new CopyOnWriteArraySet<Teacher>(k.getTeachers());
            
            // We're pretending some parents have two kids.
            // Should choose another child randomly, though. Not the next out of the
            // iterator.
            if (i < N_PARENTS_OF_2) {
                k = iK.next();
                requests.addAll(k.getTeachers());
            }
            
            requests = trimRequests(requests, MIN_PARENT_REQUESTS, MAX_PARENT_REQUESTS);
             
            if (!OUT_SQL)   
                OUT_STREAM.print("" + i + ";" + requests.size() + ";");

            Iterator<Teacher> iR = requests.iterator();
            while (iR.hasNext()) {
                if (!OUT_SQL) {
                    OUT_STREAM.print("" + iR.next().getId());
                    if (iR.hasNext()) OUT_STREAM.print(",");
                } else { 
                    OUT_STREAM.format(SQL_QUERY,
                        SQL_DID,                // Day id from cmd-line
                        iR.next().getId(),      // teacher id
                        i,                      // parent id
                        SQL_RSID);              // reg session id from cmd-line
                }
            }
           
            if (!OUT_SQL) {
                                
                // Randomly decide a session preference.
                String when;
                double dWhen = (WT_AFT + WT_EVE + WT_BOTH) * Math.random();
                
                if (dWhen < WT_BOTH)
                    when = "B";
                else if (dWhen < (WT_BOTH+WT_AFT))
                    when = "A";
                else // WT_BOTH+WT_AFT <= dWhen < WT_AFT+WT_BOTH+WT_EVE
                    when = "E";
                
                OUT_STREAM.print(";" + when + ";\n");
            }

        }
    }

    /*
     * Given a request set of at least min entries, returns a new set, with some
     * removed at random to ensure we have no more than max entries.
     */
    private static Set<Teacher> trimRequests(Set<Teacher> requests, int min, int max) {
        
        int s = requests.size();
        
        // Bad request
        if (min > max) return null;
        
        // Can't remove any in this case
        if (min >= s) return requests;
        
        // Randomly decide how many elements to take out
        int loseMin = s - max;
        int loseMax = (s - min) + 1;
        int lose = loseMin + (int) (Math.random() * (loseMax-loseMin));
        
        Teacher[] t = requests.toArray(new Teacher[0]);
        for (int l = 0; l < lose; l++) {
            // Choose a random index that we haven't blanked out yet...
            int i = (int) (s * Math.random());
            while (t[i] == null) i = (i+1) % s;
            // ... and lose it from the array
            t[i] = null;
        }
        
        // Put the remaining elements back in a set and return it
        Set<Teacher> ret = new CopyOnWriteArraySet<Teacher>();
        for (int i = 0; i < s; i++)
            if (t[i] != null)
                ret.add(t[i]);
        return ret;
        
    }

    private static void outputInfo() {
        OUT_STREAM.println("# Info");
        OUT_STREAM.println("# " + teachers.size() + " teachers; "
                + allKids.size() + " kids; " + allClasses.size() + " classes;");
        
//        // Class sizes per teacher
//        Iterator<Teacher> iT = teachers.iterator();
//        while (iT.hasNext()) {
//            Teacher t = iT.next();
//            OUT_STREAM.print("# T" + t.getId() + " has classes of size:");
//            Iterator<Class> iTC = t.getClasses().iterator();
//            while (iTC.hasNext())
//                OUT_STREAM.print(" " + iTC.next().getKids().size());
//            OUT_STREAM.print("\n");
//        }
        
//        // Kids chosen per grade
//        Iterator<Kid> iK = chosenKids.iterator();
//        Map<Grade,Integer> kpg = new HashMap<Grade, Integer>();
//        Integer old;
//        while (iK.hasNext()) {
//            Kid k = iK.next();
//            old = kpg.get(k.getGrade());
//            if (old == null)
//                kpg.put(k.getGrade(), new Integer(1));
//            else
//                kpg.put(k.getGrade(), old + 1);
//        }
//        Iterator<Grade> iG = kpg.keySet().iterator();
//        while (iG.hasNext()) {
//            Grade g = iG.next();
//            OUT_STREAM.println("# Grade " + g + ": " + kpg.get(g) + " kids chosen." );
//        }
    }

    /**
     * Kid inner class for generating test data.
     */
    private static class Kid {
        static private final int MAX_CLASSES = 9;
        private Grade g;
        private Set<Teacher> teachers;
        private Set<Class> classes;

        public Kid(Grade g) {
            StClementsTestData.allKids.add(this);

            this.g = g;
            g.addKid(this);

            this.classes = new CopyOnWriteArraySet<Class>();
            this.teachers = new CopyOnWriteArraySet<Teacher>();
        }

        public Set<Teacher> getTeachers() {
            return this.teachers;
        }

        public Grade getGrade() {
            return this.g;
        }

        public void addAClass() {
            Set<Class> all = this.g.getClasses();
            Class newClass = null;

            Iterator<Class> iAll = all.iterator();
            while (iAll.hasNext()) {
                Class c = iAll.next();
                if (c.hasRoom() && !this.classes.contains(c)) {
                    newClass = c;
                    break;
                }
            }
            // Didn't find a class to join, make a new one.
            if (newClass == null)
                newClass = new Class(this.g);

            classes.add(newClass);
            newClass.addKid(this);
            return;
        }
    }

    /**
     * Grade inner class for generating test data.
     */
    private static class Grade {
        private int classCap = 20;
        private int classFuzz = 0;
        private int nKids;
        private float parentProbability;
        private Set<Class> classes;
        private Set<Kid> kids;

        public Grade(int nKids, float parentProbability) {
            super();
            this.nKids = nKids;
            this.kids = new CopyOnWriteArraySet<Kid>();
            this.parentProbability = parentProbability;
            this.classes = new CopyOnWriteArraySet<Class>();
        }

        public Grade(int nKids, float parentProbability, int cap, int fuzz) {
            this(nKids, parentProbability);
            this.classCap = cap;
            this.classFuzz = fuzz;
        }

        public void addKid(Kid kid) {
            this.kids.add(kid);
        }

        public Set<Kid> getKids() {
            return this.kids;
        }

        public int getNKids() {
            return this.nKids;
        }

        public Set<Class> getClasses() {
            return classes;
        }

        public void addClass(Class c) {
            this.classes.add(c);
        }
    }

    /**
     * Teacher inner class for generating test data.
     */
    private static class Teacher {
        private int id;
        private Set<Class> classes;

        public Teacher(int id) {
            this.id = id;
            this.classes = new CopyOnWriteArraySet<Class>();
        }

        public int getId() {
            return this.id;
        }

        public Set<Class> getClasses() {
            return classes;
        }
    }

    /**
     * Class inner class for generating test data.
     */
    private static class Class {
        private int cap;
        private Teacher teacher;
        private Grade grade;
        private Set<Kid> kids;

        public Class(Grade g) {
            StClementsTestData.allClasses.add(this);

            this.grade = g;
            g.addClass(this);

            this.cap = grade.classCap - (int) (Math.random() * grade.classFuzz);
            if (grade.classCap == grade.classFuzz)
                this.cap = this.cap
                        - (int) (Math.random() * Math.random() * grade.classFuzz);

            this.kids = new CopyOnWriteArraySet<Kid>();
        }

        public boolean hasRoom() {
            return kids.size() < cap;
        }

        public boolean addKid(Kid k) {
            return this.kids.add(k);
        }

        public Set<Kid> getKids() {
            return this.kids;
        }

        public void setTeacher(Teacher teacher) {
            this.teacher = teacher;
            teacher.getClasses().add(this);
            Iterator<Kid> iK = this.kids.iterator();
            while (iK.hasNext()) {
                Kid k = iK.next();
                k.getTeachers().add(teacher);
            }
        }

        public Teacher getTeacher() {
            return teacher;
        }
    }
}
