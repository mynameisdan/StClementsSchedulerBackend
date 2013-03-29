package juliet.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.cli.*;

/**
 * RandomGenerator Class.
 * RandomGenerator class takes in parameters from the main program and sets up 
 * the parent teacher interview environment with all the details 
 * (# teachers, # parents, # meeting slots per session, min/max interviews 
 * allowed, etc) which is then stored in a text file in a proprietary format
 * which describes the environment set up. This file can be interpreted by
 * the Scheduler class via LoadDataFromFile function to fully set up the
 * environment.
 */
public class RandomGenerator {

    // Generation parameters default values.
    private static int N_TEACHERS = 65;
    private static int N_AFT_PARENTS = 80;
    private static int N_EVE_PARENTS = 90;
    private static int N_BOTH_PARENTS = 30;
    private static int MIN_INTERVIEWS = 5;
    private static int MAX_INTERVIEWS = 9;

    // Output file params (defaults to stdout)
    private static boolean OUT_OVERWRITE = false;
    private static String OUT_NAME;
    private static PrintStream OUT_STREAM = System.out; 
    
    /**
     * Temporary adapter for Scheduler. This will be removed.
     */
    public RandomGenerator(String fileName, Integer numTeachers, Integer numParentsAfternoon, Integer numParentsEvening, Integer numParentsBoth, Integer minInterviews, Integer maxInterviews) {
        try {
            new RandomGenerator(new PrintStream(fileName), numTeachers, numParentsAfternoon, numParentsEvening, numParentsBoth, minInterviews, maxInterviews );
        } catch (FileNotFoundException e) {
                System.out.println("Error opening file " + fileName);
                System.exit(1);
        }
    }
    /**
     * Class constructor
     * @param out PrintStream to which we're writing
     * @param numTeachers Number of teachers in the meeting
     * @param numParentsAfternoon Number of parents that requested to have
     * their meetings in afternoon session
     * @param numParentsEvening Number of parents that requested to have their 
     * meetings in evening session
     * @param numParentsBoth Number of parents that is able to have meetings in
     * both afternoon and evening sessions
     * @param minInterviews Minimum number of interviews the parents are allowed
     * to request
     * @param maxInterviews Maximum number of interviews the parents are allowed
     * to request
     */
    public RandomGenerator(PrintStream out, Integer numTeachers, Integer numParentsAfternoon, Integer numParentsEvening, Integer numParentsBoth, Integer minInterviews, Integer maxInterviews)
    {
        Integer parentIndex = 0;
        
        out.println(numTeachers.toString());
        /*Populate the Afternoon parents*/
        for (Integer i = 1; i <= numParentsAfternoon; i++)
        {
            /*The parents ID*/
            parentIndex++;
            out.print(parentIndex.toString() + ";");
            
            /*The interviews they want*/
            Integer numInverviews = minInterviews + (int)(Math.random() * ((maxInterviews - minInterviews) + 1));/*How many interviews do they want*/
            out.print(numInverviews.toString() + ";");
            ArrayList<Integer> interviewsWanted = new ArrayList<Integer>();
            while(interviewsWanted.size() < numInverviews)
            {
                Integer newRequest = 1 + (int)(Math.random() * ((numTeachers - 1) + 1));
                if (!interviewsWanted.contains(newRequest))
                    {interviewsWanted.add(newRequest);}
            }

            Iterator<Integer> interviewsWantedItterator = interviewsWanted.iterator();
            while(interviewsWantedItterator.hasNext())
            {
                out.print(interviewsWantedItterator.next().toString());
                if (interviewsWantedItterator.hasNext())
                {
                    out.print(",");
                }
            }
            out.print(";A\n");
        }
        
        /*Populate the Evening parents*/
        for (int i = 1; i <= numParentsEvening; i++)
        {
            /*The parents ID*/
            parentIndex++;
            out.print(parentIndex.toString() + ";");
            
            /*The interviews they want*/
            Integer numInverviews = minInterviews + (int)(Math.random() * ((maxInterviews - minInterviews) + 1));/*How many interviews do they want*/
            out.print(numInverviews.toString() + ";");
            ArrayList<Integer> interviewsWanted = new ArrayList<Integer>();
            while(interviewsWanted.size() < numInverviews)
            {
                Integer newRequest = 1 + (int)(Math.random() * ((numTeachers - 1) + 1));
                if (!interviewsWanted.contains(newRequest))
                    {interviewsWanted.add(newRequest);}
            }

            Iterator<Integer> interviewsWantedItterator = interviewsWanted.iterator();
            while(interviewsWantedItterator.hasNext())
            {
                out.print(interviewsWantedItterator.next().toString());
                if (interviewsWantedItterator.hasNext())
                {
                    out.print(",");
                }
            }
            out.print(";E\n"); 
        }

        /*Populate the Both parents*/
        for (int i = 1; i <= numParentsBoth; i++)
        {
            /*The parents ID*/
            parentIndex++;
            out.print(parentIndex.toString() + ";");
            
            /*The interviews they want*/
            Integer numInverviews = minInterviews + (int)(Math.random() * ((maxInterviews - minInterviews) + 1));/*How many interviews do they want*/
            out.print(numInverviews.toString() + ";");
            ArrayList<Integer> interviewsWanted = new ArrayList<Integer>();
            while(interviewsWanted.size() < numInverviews)
            {
                Integer newRequest = 1 + (int)(Math.random() * ((numTeachers - 1) + 1));
                if (!interviewsWanted.contains(newRequest))
                    {interviewsWanted.add(newRequest);}
            }

            Iterator<Integer> interviewsWantedItterator = interviewsWanted.iterator();
            while(interviewsWantedItterator.hasNext())
            {
                out.print(interviewsWantedItterator.next().toString());
                if (interviewsWantedItterator.hasNext())
                {
                    out.print(",");
                }
            }
            out.print(";B\n");
        }
        
        out.close();

    }
    
    /**
     * Main function, by default will
     * @param args
     */
    @SuppressWarnings("static-access") // This is for the use of OptionBuilder
    public static void main(String[] args) {
        // Configure options
        Options opts = new Options();
        
        opts.addOption( new Option("help", "print this message") );
        
        opts.addOption( OptionBuilder.withArgName("filename")
                .hasArg()
                .withType(new String())
                .withDescription("output filename (default is stdout)")
                .create('o') );
        
        opts.addOption( new Option("f", "force overwrite if output file exists") );
        
        opts.addOption( OptionBuilder.withArgName("int")
                .hasArg()
                .withDescription("number of teachers (default " + N_TEACHERS + ")")
                .create('t') );
        
        opts.addOption( OptionBuilder.withArgName("int")
                .hasArg()
                .withDescription("number of parents in the afternoon " +
                        "(default " + N_AFT_PARENTS + ")")
                .create("pa") );
        
        opts.addOption( OptionBuilder.withArgName("int")
                .hasArg()
                .withDescription("number of parents in the evening " +
                        "(default " + N_EVE_PARENTS + ")")
                .create("pe") );
        
        opts.addOption( OptionBuilder.withArgName("int")
                .hasArg()
                .withDescription("number of parents who chose both sessions " +
                        "(default " + N_BOTH_PARENTS + ")")
                .create("pb") );
        
        opts.addOption( OptionBuilder.withArgName("int")
                .hasArg()
                .withDescription("min interviews parents will request " +
                        "(default " + MIN_INTERVIEWS + ")")
                .create("mini") );
        
        opts.addOption( OptionBuilder.withArgName("int")
                .hasArg()
                .withDescription("max interviews parents will request " +
                        "(default " + MAX_INTERVIEWS + ")")
                .create("maxi") );
        
        // Parse args, do basic sanity checks
        CommandLineParser parser = new PosixParser();
        try {
            CommandLine cl = parser.parse(opts, args);
            int optI;
            
            if (cl.hasOption("help")) {
                HelpFormatter help = new HelpFormatter();
                help.printHelp("java -classpath juliet.jar " +
                        "juliet.test.RandomGenerator",
                        "\nGenerates random test parent-request data " +
                        "according to the given parameters.",
                        opts,
                        "",
                        true);
                System.exit(0);
            }
            
            OUT_OVERWRITE = cl.hasOption("f");
            
            if (cl.hasOption('o')) {
                OUT_NAME = cl.getOptionValue('o');
                check_filename();
            }
            
            if (cl.hasOption('t')) {
                optI = Integer.parseInt(cl.getOptionValue('t'));
                if (optI < 1)
                    throw new ParseException("\"t\" may not be less than 1 " +
                            "(given " + optI + ")");
                N_TEACHERS = optI;
            }
            
            if (cl.hasOption("pa")) {
                optI = Integer.parseInt(cl.getOptionValue("pa"));
                if (optI < 1)
                    throw new ParseException("\"pa\" may not be less than 1 " +
                            "(given " + optI + ")");
                N_AFT_PARENTS = optI;
            }
            
            if (cl.hasOption("pe")) {
                optI = Integer.parseInt(cl.getOptionValue("pe"));
                if (optI < 1)
                    throw new ParseException("\"pe\" may not be less than 1 " +
                            "(given " + optI + ")");
                N_EVE_PARENTS = optI;
            }
            
            if (cl.hasOption("pb")) {
                optI = Integer.parseInt(cl.getOptionValue("pb"));
                if (optI < 1)
                    throw new ParseException("\"pb\" may not be less than 1 " +
                            "(given " + optI + ")");
                N_BOTH_PARENTS = optI;
            }
            
            if (cl.hasOption("mini")) {
                optI = Integer.parseInt(cl.getOptionValue("mini"));
                if (optI < 1)
                    throw new ParseException("\"mini\" may not be less than 1 " +
                            "(given " + optI + ")");
                MIN_INTERVIEWS = optI;
            }
            
            if (cl.hasOption("maxi")) {
                optI = Integer.parseInt(cl.getOptionValue("maxi"));
                if (optI < MIN_INTERVIEWS)
                    throw new ParseException("\"maxi\" may not be less than " +
                            "\"mini\" (given maxi=" + optI +
                            ", mini=" + MIN_INTERVIEWS + ")");
                if (optI > N_TEACHERS)
                    throw new ParseException("\"maxi\" may not be greater " +
                            "than \"t\" (given maxi=" + optI + 
                            ", t=" + N_TEACHERS + ")");
                MAX_INTERVIEWS = optI;
            }
            
        } catch (Exception e) {
            System.err.println("Exiting. Exception while parsing command-line: " +
                    e.getMessage());
            System.exit(1);
        }
        
        // Construct a RandomGenerator and throw it away. This is kind of a
        // weird architecture for this to have. *shrug* (Jon)
        new RandomGenerator(OUT_STREAM, N_TEACHERS, N_AFT_PARENTS,
                N_EVE_PARENTS, N_BOTH_PARENTS, MIN_INTERVIEWS, MAX_INTERVIEWS);
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
}
