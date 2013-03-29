package juliet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Output Module class.
 * The OutputModule class takes the set of parents and teachers that were
 * processed by the scheduler algorithm and proceeds to output the data
 * stored in each Parent and Teacher class onto their respective CSV files
 * (one for parents, one for teachers) to be submitted to and processed by the
 * database.
 */
public class OutputModule {
    
    // Container objects for parents and Teachers
    Set<Parent> parents;
    Set<Teacher> teachers;

    /**
     * Class constructor.
     * Takes in set of parents and teachers that have been processed by the
     * scheduler.
     * @param p Set of parents
     * @param t Set of teachers
     */
    public OutputModule(Set<Parent> p, Set<Teacher> t)
    {
        this.parents = p;
        this.teachers = t;
    }

    /**
     * WriteParentsToCSV method takes the set of parents and proceeds to
     * write to a CSV file. Takes in a fileName and saves the output in .csv
     * format
     * @param fileName Output file name
     */
//    public void WriteParentsToCSV(PrintStream out)
//    {
//        
//        try
//        {
//            Iterator<Parent> parentIterator = parents.iterator();
//            
//            while (parentIterator.hasNext())
//            {
//                Parent currentParent = parentIterator.next();
//               
//                out.println(currentParent.getId().toString());
//                out.println(currentParent.getSession());
//
//                out.print("AFT:");
//                
//                Iterator<Teacher> aftAppts = currentParent.GetApptsAft().iterator();
//                while (aftAppts.hasNext()) {
//                    Teacher currentTeacher = aftAppts.next();
//                    if (currentTeacher != null) {
//                        out.print(currentTeacher.getId().toString());
//                    } else {
//                        out.print("N");
//                    }
//                    if (aftAppts.hasNext()) out.print(",");
//                }
//                out.println("\nEVE:");
//                
//                Iterator<Teacher> eveAppts = currentParent.GetApptsEve().iterator();
//                while (eveAppts.hasNext()) {
//                    Teacher currentTeacher = eveAppts.next();
//                    if (currentTeacher != null) {
//                        out.print(currentTeacher.getId().toString());
//                    } else {
//                        out.print("N");
//                    }
//                    if (eveAppts.hasNext()) out.print(",");
//                }
//                out.newLine(); 
//                
//                Set<Teacher> toCall = currentParent.GetToCall();
//                Iterator<Teacher> toCallItterator = toCall.iterator();
//                
//                out.write("TO CALL,");
//                while (toCallItterator.hasNext())
//                {
//                    Teacher currentTeacher = toCallItterator.next();
//                    out.write(currentTeacher.getId().toString());
//                    out.write(",");
//                }
//                  
//                out.newLine();   
//                
//                out.write("TR:,");
//                out.write(currentParent.GetNumberOfRequests().toString());
//                out.newLine();
//                
//                out.write("TS:,");
//                out.write(currentParent.GetNumberOfScheduledRequests().toString());
//                
//                out.newLine();
//                
//                out.write("TT:,");
//                out.write(currentParent.GetTotalTime().toString());
//                out.newLine();
//                out.newLine();
//            }
//            out.close();
//        }
//        catch (IOException e)
//        {
//            
//        }
//    }

    /**
     * WriteTeachersToCSV method takes the set of parents and proceeds to
     * write to a CSV file. Takes in a fileName and saves the output in .csv
     * file.
     * @param fileName Output file name
     */
    public void WriteTeachersToCSV(String fileName)
    {
        BufferedWriter out;
        try
        {
            out = new BufferedWriter(new FileWriter(fileName));
            
            Iterator<Teacher> iterator = this.teachers.iterator();
            
            while(iterator.hasNext())
            {
                Teacher currentTeacher = iterator.next();
                
                out.write(currentTeacher.getId().toString());
                
                out.newLine();

                // Write the afternoon schedule for teacher
                out.write("AFT:,");
                
                List<Parent> aftAppts = currentTeacher.GetAftAppts();
                for (int i = 0; i < aftAppts.size(); i ++)
                {
                    Parent currentParent = aftAppts.get(i);
                    if (currentParent != null)
                    {
                        out.write(currentParent.getId().toString());
                    }
                    else
                    {
                        out.write("N");
                    }
                    out.write(",");
                }
                out.newLine();

                // Write the evening schedule for teacher
                out.write("EVE:,");
                
                List<Parent> eveAppts = currentTeacher.GetEveAppts();
                for (int i = 0; i < eveAppts.size(); i ++)
                {
                    Parent currentParent = eveAppts.get(i);
                    if (currentParent != null)
                    {
                        out.write(currentParent.getId().toString());
                    }
                    else
                    {
                        out.write("N");
                    }
                    out.write(",");
                }
                out.newLine();
                
                Set<Parent> toCallAft = currentTeacher.GetToCallAft();
                Iterator<Parent> toCallItteratorAft = toCallAft.iterator();
                
                // Write the TO CALL list for parents that picked afternoon session
                out.write("TO CALL AFT,");
                while (toCallItteratorAft.hasNext())
                {
                    Parent currentParent = toCallItteratorAft.next();
                    out.write(currentParent.getId().toString());
                    out.write(",");
                }
                  
                out.newLine();   
                Set<Parent> toCallEve = currentTeacher.GetToCallEve();
                Iterator<Parent> toCallItteratorEve = toCallEve.iterator();

                // Write the TO CALL list for parents that picked evening session
                out.write("TO CALL EVE,");
                while (toCallItteratorEve.hasNext())
                {
                    Parent currentParent = toCallItteratorEve.next();
                    out.write(currentParent.getId().toString());
                    out.write(",");
                }
                  
                out.newLine();

                // Write the Total Meetings requested amount
                out.write("TR:,");
                out.write(currentTeacher.GetTotalReqAmount().toString());
                out.newLine();
                // Write the Total meetings scheduled amount
                out.write("TS:,");
                out.append(currentTeacher.GetTotalSchedAmount().toString());
                out.newLine();
                out.newLine();
                
            }
            out.close();
        }
        catch (IOException e)
        {            
        }
    }

    /**
     * GroupOutputByType method iterates through the sets of parents and
     * places them into their appropriate Map group.
     * @param groupByReq The group to organize parents by the number of requests they made
     * @param groupByApptsAft The group to contain parents that are in Afternoon session
     * @param groupByApptsEve The group to contain parents that are in Evening session
     */
    private void GroupOutputByType(Map<Parent, Integer> groupByReq,
            Map<Parent, Integer> groupByApptsAft,
            Map<Parent, Integer> groupByApptsEve)
    {
        Iterator<Parent> parentIterator = this.parents.iterator();
        
        /*put the parents in relavent groupings*/
        while (parentIterator.hasNext())
        {
            Parent currentParent = parentIterator.next();
            
            groupByReq.put(currentParent, currentParent.GetNumberOfRequests());

            if (currentParent.getSession() == Parent.Session.AFTERNOON)
            {
                groupByApptsAft.put(currentParent,
                        currentParent.GetNumberOfScheduledRequests());
            }
            if (currentParent.getSession() == Parent.Session.EVENING)
            {
                groupByApptsEve.put(currentParent,
                        currentParent.GetNumberOfScheduledRequests());
            }
        }
    }    

    /**
     * WriteReqsVsSchedToCSV function writes a CSV file containing the statistics
     * of the schedule run through and the parents requests and such.     
     * @param fileName Output file name
     */
    public void WriteReqsVsSchedToCSV(String fileName)
    { 
        BufferedWriter out;
        // Grouping of parents by number of requests made
        Map<Parent, Integer> groupByReq = new HashMap<Parent, Integer>();
        // Grouping of parents by the session they chose
        Map<Parent, Integer> groupByApptsAft = new HashMap<Parent, Integer>();
        Map<Parent, Integer> groupByApptsEve = new HashMap<Parent, Integer>();

        // Obtain all groupings needed
        this.GroupOutputByType(groupByReq, groupByApptsAft, groupByApptsEve);
        
        try
        {
            out = new BufferedWriter(new FileWriter(fileName));
            
            /**
             * This for loop outputs based on the number of scheduled requests vs number
             * of requests made.
             * Iterate over parents by number of requests
             */            
            for (Integer i =  1; i <= 9; i++)
            {
                Set<Parent> reqsForI = this.getKeysByValue(groupByReq, i);
                Iterator<Parent> reqIterator = reqsForI.iterator();
                Parent worstCase = null;

                Integer totalScheduled = 0;
                Integer count = 0;

                // iterate over each parent in request set
                while(reqIterator.hasNext())
                {
                    Parent currentParent = reqIterator.next();
                    count++;
                    totalScheduled += currentParent.GetNumberOfScheduledRequests();

                    if (worstCase == null)
                    {
                        worstCase = currentParent;
                    }
                    else if (currentParent.GetNumberOfScheduledRequests() <
                            worstCase.GetNumberOfScheduledRequests())
                    {
                        worstCase = currentParent;
                    }
                }

                // Make sure at least one parent made that many choices
                if (count != 0)
                {
                    double average = (double) totalScheduled / (double) (count);
                    out.write(i.toString());
                    out.newLine();
                    out.write("Avg:,");
                    out.write(String.valueOf(average));
                    out.newLine();
                    out.write("WC:,");
                    out.write(worstCase.GetNumberOfScheduledRequests().toString());
                    out.newLine();
                    out.write("NUM:,");
                    out.write(count.toString());
                    out.newLine();                    
                }
                
            }
            out.newLine();
            
            /**
             * This for loop iterates over the time a parent needs to stay vs 
             * the number of interviews.
             * Iterate over parents by number of requests.
             * This is only for afternoon parents.
             */
            out.write("AFT");
            out.newLine();
            for (Integer i =  1; i <= 9; i++)/*TODO: Get rid of hardcoding of 9*/
            {
                Set<Parent> schedForI = this.getKeysByValue(groupByApptsAft, i);
                Iterator<Parent> schedIterator = schedForI.iterator();
                Parent worstCase = null;

                Integer totalTime = 0;
                Integer count = 0;

                // iterate over each parent in request set
                while(schedIterator.hasNext())
                {
                    Parent currentParent = schedIterator.next();
                    count++;
                    totalTime += currentParent.GetTotalTime();

                    if (worstCase == null)
                    {
                        worstCase = currentParent;
                    }
                    else if (currentParent.GetTotalTime() > worstCase.GetTotalTime())
                    {
                        worstCase = currentParent;
                    }
                }

                // Make sure at least one parent made that many choices
                if (count != 0)
                {
                    double average = (double) totalTime / (double) (count);
                    out.write(i.toString());
                    out.newLine();
                    out.write("Avg:,");
                    out.write(String.valueOf(average));
                    out.newLine();
                    out.write("WC:,");
                    out.write(worstCase.GetTotalTime().toString());
                    out.newLine();
                    out.write("NUM:,");
                    out.write(count.toString());
                    out.newLine();
                }
                
            }
            out.newLine();

            out.write("EVE");
            out.newLine();
            /**
             * This for loop iterates over the time a parent needs to stay vs 
             * the number of interviews.
             * Iterate over parents by number of requests.
             * This is only for evening parents.
             */
            for (Integer i =  1; i <= 9; i++)
            {
                Set<Parent> schedForI = this.getKeysByValue(groupByApptsEve, i);
                Iterator<Parent> schedIterator = schedForI.iterator();
                Parent worstCase = null;

                Integer totalTime = 0;
                Integer count = 0;

                // iterate over each parent in request set
                while(schedIterator.hasNext())
                {
                    Parent currentParent = schedIterator.next();
                    count++;
                    totalTime += currentParent.GetTotalTime();

                    if (worstCase == null)
                    {
                        worstCase = currentParent;
                    }
                    else if (currentParent.GetTotalTime() > worstCase.GetTotalTime())
                    {
                        worstCase = currentParent;
                    }
                }

                // Make sure at least one parent made that many choices
                if (count != 0)
                {
                    double average = (double) totalTime / (double) (count);
                    out.write(i.toString());
                    out.newLine();
                    out.write("Avg:,");
                    out.write(String.valueOf(average));
                    out.newLine();
                    out.write("WC:,");
                    out.write(worstCase.GetTotalTime().toString());
                    out.newLine();
                    out.write("NUM:,");
                    out.write(count.toString());
                    out.newLine();                    
                }
                
            }
            out.newLine();            
            out.close();
        }
        catch(IOException e)
        {
            
        }

    }

    /**
     * getKeysByValue returns a set of parents that have key entry values that
     * matches the value desired
     * @param map Map to obtain values from
     * @param value The value to look for in the map
     * @return Set of parents that contain entry values that matches the value
     * desired.
     */
    private Set<Parent> getKeysByValue(Map<Parent, Integer> map, Integer value)
    {
         Set<Parent> keys = new HashSet<Parent>();
         for (Entry<Parent, Integer> entry : map.entrySet()) {
             if (value.equals(entry.getValue())) {
                 keys.add(entry.getKey());
             }
         }
         return keys;
    }
}

