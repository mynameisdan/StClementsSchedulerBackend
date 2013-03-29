package juliet;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Strategy Class
 * The Strategy class is the main algorithm for the scheduling program. Once
 * given the set up inputs the Strategy class attempts to create a feasible
 * schedule. The algorithm selects a parent based on priority (determined by
 * -1 * # of requests still to be handled). Parent with the greatest priority
 * will be processed first. After a parent has been chosen the algorithm then
 * selects a teacher based on the same priority and checks if the chosen parent
 * and teacher have a compatible meeting slot. If so schedule it. If else add
 * the parent and teacher to each other's To Call list and look for another best
 * suitable teacher for the parent. Each parent and teacher have their own
 * individual schedules to work with. Teachers however have 2 schedules, 1 for
 * the Afternoon meetings and 1 for the evening meetings to handle parents in
 * afternoon and parents in evening sessions separately. For the parents that
 * chose both sessions the algorithm will first (before doing any sort of
 * scheduling) decide if they will fit better in the afternoon slot or
 * the evening slot by observing the demand the teachers they requested for
 * experience in each session and then that parent will be treated like a parent
 * that chose a strict afternoon/evening session until there are no compatible
 * slots left in the afternoon slot, afterwards the algorithm will consider
 * placing said parent in evening session.
 */
public class Strategy{

    // "Copies" to handle states which the algorithm experiences
    private Set<Parent> MasterCopy;
    private Set<Parent> WorkingCopy;
    // slot trackers
    private Integer aftSlots;
    private Integer eveSlots;

    /**
     * Class constructor
     * @param p Parent set
     * @param t Teacher set
     * @param aft Number of afternoon slots
     * @param eve Number of evening slots
     */
    public Strategy(Set<Parent> p, int aft, int eve){    
        
        Iterator<Parent> parentIterator = p.iterator();
        
        this.MasterCopy = new HashSet<Parent>();
        this.WorkingCopy = new HashSet<Parent>();
        
        while (parentIterator.hasNext())
        {
            Parent currParent = parentIterator.next();
            this.MasterCopy.add(currParent);            
        }
        
        this.aftSlots = aft;
        this.eveSlots = eve;
        this.groupBothParents();
        this.doSchedule();    
    }

    /**
     * groupBothParents goes through all the information the algorithm has in
     * store and decide if parents that chose a Both session will have better
     * chance of having successful requests in afternoon or evening slot.
     * This is done by considering the demand factor the teachers that the
     * parents have requested for experience in each session.
     */
    private void groupBothParents()
    {
        Iterator<Parent> parentIterator = this.MasterCopy.iterator();
        Integer totalAft = 0, totalEve = 0;
        
        while(parentIterator.hasNext())
        {
            Parent currentParent = parentIterator.next();
            
            /*Go over BOTH parents only*/
            if (currentParent.getSession() == Parent.Session.BOTH)
            {
                Integer aftProb = 0;
                Integer eveProb = 0;
                
                Iterator<Teacher> teacherIterator = currentParent.getRequests().iterator();
                
                /*Go over all of the teachers and decide whether parent should be in the eve or aft*/
                while(teacherIterator.hasNext())
                {
                    Teacher currentTeacher = teacherIterator.next();

                    if (((this.aftSlots) - currentTeacher.getAftRequestSize()) > (( this.eveSlots) - currentTeacher.getEveRequestSize()))
                    {
                        aftProb++;
                    }
                    else
                    {
                        eveProb++;
                    }
                }
                
                /*Change the parent's session*/
                if (aftProb >= eveProb)
                {
                    totalAft++;
                    currentParent.setSchedulingSession(Parent.Session.BOTH_AFTERNOON);
                }
                else
                {
                    totalEve++;
                    currentParent.setSchedulingSession(Parent.Session.BOTH_EVENING);
                }
                
                teacherIterator = currentParent.getRequests().iterator();

                while(teacherIterator.hasNext())
                {
                    Teacher currentTeacher = teacherIterator.next();
                    
                    if(currentParent.getSchedulingSession() == Parent.Session.BOTH_AFTERNOON)
                    {
                        currentTeacher.moveBothParentToNewSession(currentParent,
                                Parent.Session.AFTERNOON);
                    }
                    else
                    {
                        currentTeacher.moveBothParentToNewSession(currentParent,
                                Parent.Session.EVENING);
                    }
                }
                
            }
            
        }
        //System.out.println(totalAft.toString() + " " + totalEve.toString());
    }

    /**
     * doSchedule method initiates the scheduling algorithm and will run until
     * the MasterCopy of the scheduling process is empty - ie all parents have
     * been dealt with.
     */
    private void doSchedule()
    {
        /*Run in this loop untill we have burned through all of the parents
         that is untill no more parents have valid requests left*/
        while (!this.MasterCopy.isEmpty())
        {
            this.CopyMasterToWorking();
            /*Run in this loop where we schedule 1 teacher to each parent*/
            while (!this.WorkingCopy.isEmpty())
            {
                ScheduleTriple triple = new ScheduleTriple();
                /*Get the best parent to schedule next*/
                this.GetBestParentInWorkingCopy(triple);
                /*Get the best teacher for this parent null if no more matches*/
                this.GetBestTeacherForParent(triple);
                this.UpdateWorkingCopy(triple);
                /* Parent has no compatible slots with the teachers they requested */
                if(triple.GetSlot() == null)
                {
                    this.RemoveParentFromWorkingAndMaster(triple);
                }
                /* A compatible best teacher has been found. Schedule the meeting */
                else
                {
                    triple.Schedule();
                    this.UpdateMasterCopy(triple);
                }
            }
        }
    }

    /**
     * CopyMasterToWorking method updates the working state of the algorithm to
     * the master state. This is used to update the working state with the changes
     * the algorithm has made in its first working cycle.
     */
    private void CopyMasterToWorking()
    {
        Iterator<Parent> masterItter = this.MasterCopy.iterator();
        while(masterItter.hasNext())
        {
            this.WorkingCopy.add(masterItter.next());
        }
    }

    /**
     * GetBestParentInWorkingCopy goes through the list of parents and finds the
     * parent with least amount of requests to be handled (priority is based on
     * it). This parent will then be selected to have a meeting scheduled with
     * a teacher of their request if there exists a compatible one.
     * @param triple The ScheduleTriple object to store highest priority parent in
     */
    private void GetBestParentInWorkingCopy(ScheduleTriple triple)
    {
        Parent bestParent = null;
        Iterator<Parent> iterator = this.WorkingCopy.iterator();
        bestParent = iterator.next();
        
        while(iterator.hasNext())
        {
            Parent currentParent = iterator.next();
            if (currentParent.GetPriority() > bestParent.GetPriority())
            {
                bestParent = currentParent;
            }
        }        
        triple.SetParent(bestParent);
    }

    /**
     * GetBestTeacherForParent is a wrapper method for the GetBestTeacherForParentXXX
     * where XXX is replaced by the session name identified in this method.
     * @param triple The ScheduleTriple the parent is stored in
     */
    private void GetBestTeacherForParent(ScheduleTriple triple)
    {
        Parent bestParent = triple.GetParent();
        // identify which session the parent at hand is in and call appropriate handler method
        if (bestParent.getSchedulingSession() == Parent.Session.AFTERNOON)
        {
            this.GetBestTeacherForParentAfternoon(triple);
        }
        else if (bestParent.getSchedulingSession() == Parent.Session.BOTH_AFTERNOON)
        {
            this.getBestTeacherForParentBothAfternoon(triple);
        }
        else if (bestParent.getSchedulingSession() == Parent.Session.EVENING)
        {
            this.GetBestTeacherForParentEvening(triple);
        }
        else if (bestParent.getSchedulingSession() == Parent.Session.BOTH_EVENING)
        {
            this.getBestTeacherForParentBothEvening(triple);
        }

    }

    /**
     * GetBestTeacherForParentAfternoon gets called by its wrapper method
     * GetBestTeacherForParent if the parent is in afternoon session
     * @param triple ScheduleTriple used for this working copy
     */
    private void GetBestTeacherForParentAfternoon(ScheduleTriple triple)
    {
        triple.SetSession(Parent.Session.AFTERNOON);
        Integer slot = null;
        Teacher bestTeacher = null;
        
        Parent parent = triple.GetParent();
        Iterator<Teacher> teacherIterator = parent.getRequests().iterator();

        /*
         * for each teacher in iterator find the first available common slot
         * the parent and teacher share. If there is no such slot then this
         * teacher is incompatible so remove from each other's lists. If there
         * is a slot number see if this teacher has highest priority. If so then
         * store into the triple for further processing. The iterator goes through
         * all viable teachers until the highest priority teacher is found
         */
        while (teacherIterator.hasNext())
        {
            Teacher currentTeacher = teacherIterator.next();
            
            slot = FirstMatchInAft(parent, currentTeacher) ;
            
            if (slot != null)
            {
                if (bestTeacher == null)
                {
                    bestTeacher = currentTeacher;
                    triple.SetTeacher(bestTeacher);
                    triple.SetSlot(slot);
                }
                else if (bestTeacher.GetPriority(Parent.Session.AFTERNOON) <
                        currentTeacher.GetPriority(Parent.Session.AFTERNOON))
                {
                    bestTeacher = currentTeacher;
                    triple.SetTeacher(bestTeacher);
                    triple.SetSlot(slot);
                }
            }
            else/*This teacher has no corresponding slots*/
            {
                teacherIterator.remove();
                currentTeacher.CantScheduleParent(parent);
                parent.CantScheduleTeacher(currentTeacher);
            }
        }
    }
    /**
     * FirstMatchInAft finds the earliest slot in the afternoon the parent and
     * teacher both are vacant in.
     * @param p Parent
     * @param t Teacher
     * @return Earliest afternoon slot both parent and teacher are free in
     */
    private Integer FirstMatchInAft(Parent p, Teacher t)
    {            
        for (Integer i = 0; i < this.aftSlots; i++)
        {
            if (p.FreeAftAptAtSlot(i) && t.FreeAftAptAtSlot(i))
            {
                return i;
            }
        }
        return null;
    }

    /**
     * LastMatchInAft finds the latest slot in the afternoon the parent and
     * teacher both are vacant in.
     * @param p Parent
     * @param t Teacher
     * @return Latest afternoon slot both parent and teacher are free in
     */
    private Integer LastMatchInAft(Parent p, Teacher t)
    {
        for (Integer i = this.aftSlots - 1; i >= 0; i--)
        {
            if (p.FreeAftAptAtSlot(i) && t.FreeAftAptAtSlot(i))
            {
                return i;
            }
        }
        return null;
    }

    /**
     * FirstMatchInEve finds the earliest slot in the evening the parent and
     * teacher both are vacant in.
     * @param p Parent
     * @param t Teacher
     * @return Earliest evening slot both parent and teacher are free in
     */
    private Integer FirstMatchInEve(Parent p, Teacher t)
    {
        for (Integer i = 0; i < this.eveSlots; i++)
        {
            if (p.FreeEveAptAtSlot(i) && t.FreeEveAptAtSlot(i))
            {
                return i;
            }
        }
        return null;
    }

    /**
     * LastMatchInEve finds the latest slot in the evening the parent and
     * teacher both are vacant in.
     * @param p Parent
     * @param t Teacher
     * @return Latest Evening slot both parent and teacher are free in
     */
    private Integer LastMatchInEve(Parent p, Teacher t)
    {
        for (Integer i = this.eveSlots - 1; i >= 0; i--)
        {
            if (p.FreeEveAptAtSlot(i) && t.FreeEveAptAtSlot(i))
            {
                return i;
            }
        }
        return null;
    }
       
    private void getBestTeacherForParentBothEvening(ScheduleTriple triple)
    {
        GetBestTeacherForParentEvening(triple);//same deal as a normal evening parent
                                               //we want them to be scheduled early.
    }
    private void getBestTeacherForParentBothAfternoon(ScheduleTriple triple)
    {
        triple.SetSession(Parent.Session.AFTERNOON);
        Integer slot = null;
        Teacher bestTeacher = null;
        
        Parent parent = triple.GetParent();
        Iterator<Teacher> teacherIterator = parent.getRequests().iterator();

        /*
         * for each teacher in iterator find the first available common slot
         * the parent and teacher share. If there is no such slot then this
         * teacher is incompatible so remove from each other's lists. If there
         * is a slot number see if this teacher has highest priority. If so then
         * store into the triple for further processing. The iterator goes through
         * all viable teachers until the highest priority teacher is found
         */
        while (teacherIterator.hasNext())
        {
            Teacher currentTeacher = teacherIterator.next();
            
            slot = LastMatchInAft(parent, currentTeacher) ;
            
            if (slot != null)
            {
                if (bestTeacher == null)
                {
                    bestTeacher = currentTeacher;
                    triple.SetTeacher(bestTeacher);
                    triple.SetSlot(slot);
                }
                else if (bestTeacher.GetPriority(Parent.Session.AFTERNOON) <
                        currentTeacher.GetPriority(Parent.Session.AFTERNOON))
                {
                    bestTeacher = currentTeacher;
                    triple.SetTeacher(bestTeacher);
                    triple.SetSlot(slot);
                }
            }
            else/*This teacher has no corresponding slots*/
            {
                teacherIterator.remove();
                currentTeacher.CantScheduleParent(parent);
                parent.CantScheduleTeacher(currentTeacher);
            }
        }
        
    }
    
    /**
     * GetBestTeacherForParentEvening gets called by its wrapper method 
     * GetBestTeacherForParent if the parent is in evening session
     * @param triple ScheduleTriple used for this working copy
     */
    private void GetBestTeacherForParentEvening(ScheduleTriple triple)
    {
        triple.SetSession(Parent.Session.EVENING);
        Integer slot = null;
        Teacher bestTeacher = null;
//        Integer bestSlot = null; // unused
        
        Parent parent = triple.GetParent();
        Iterator<Teacher> teacherIterator = parent.getRequests().iterator();
        
        while (teacherIterator.hasNext())
        {
            Teacher currentTeacher = teacherIterator.next();
            
            /*
             * for each teacher in iterator find the first available common slot
             * the parent and teacher share. If there is no such slot then this
             * teacher is incompatible so remove from each other's lists. If there
             * is a slot number see if this teacher has highest priority. If so then
             * store into the triple for further processing. The iterator goes through
             * all viable teachers until the highest priority teacher is found
             */
            slot = FirstMatchInEve(parent, currentTeacher);
            if (slot != null)
            {
                if (bestTeacher == null)
                {
                    bestTeacher = currentTeacher;
                    triple.SetTeacher(bestTeacher);
                    triple.SetSlot(slot);
                }
                else if (bestTeacher.GetPriority(Parent.Session.EVENING) <
                        currentTeacher.GetPriority(Parent.Session.EVENING))
                {
                    bestTeacher = currentTeacher;
                    triple.SetTeacher(bestTeacher);
                    triple.SetSlot(slot);
                }
            }
            else/*This teacher has no corresponding slots*/
            {
                teacherIterator.remove();
                currentTeacher.CantScheduleParent(parent);
                parent.CantScheduleTeacher(currentTeacher);
            }
        }
    }

    /**
     * UpdateWorkingCopy updates the working copy by removing incompatible parents
     * from the working copy
     * @param triple ScheduleTriple class for the working copy state
     */
    private void UpdateWorkingCopy(ScheduleTriple triple)
    {
        this.WorkingCopy.remove(triple.GetParent());
    }

    /**
     * UpdateMasterCopy updates the Master copy state of the algorithm up to date.
     * As in it goes to search all incompatible matches and takes care of it.
     * Also if the parent has been fully depleted of requests, then they are
     * done so remove them from Master state iterator
     * @param triple ScheduleTriple class
     */
    private void UpdateMasterCopy(ScheduleTriple triple)
    {
        Iterator<Parent> iterator = this.MasterCopy.iterator();
        Teacher schedTeacher = triple.GetTeacher();
        
        while (iterator.hasNext())
        {
            Parent currentParent = iterator.next();
            
            if (currentParent.getSchedulingSession() == Parent.Session.AFTERNOON &&
                    currentParent.getRequests().contains(schedTeacher))
            {
                if (this.FirstMatchInAft(currentParent, schedTeacher) == null)
                {
                    currentParent.CantScheduleTeacher(schedTeacher);
                    schedTeacher.CantScheduleParent(currentParent);
                }
            }
            else if (currentParent.getSchedulingSession() == Parent.Session.EVENING &&
                    currentParent.getRequests().contains(schedTeacher))
            {
                if (this.FirstMatchInEve(currentParent, schedTeacher) == null)
                {
                    currentParent.CantScheduleTeacher(schedTeacher);
                    schedTeacher.CantScheduleParent(currentParent);
                } 
            }
            else if (currentParent.getSchedulingSession() == Parent.Session.BOTH_EVENING &&
                    currentParent.getRequests().contains(schedTeacher))
            {
                if (this.FirstMatchInEve(currentParent, schedTeacher) == null)
                {
                    currentParent.CantScheduleTeacher(schedTeacher);
                    schedTeacher.CantScheduleParent(currentParent);
                } 
            }            
            else if (currentParent.getSchedulingSession() == Parent.Session.BOTH_AFTERNOON &&
                    currentParent.getRequests().contains(schedTeacher))
            {
                if (this.FirstMatchInAft(currentParent, schedTeacher) == null)
                {
                    currentParent.CantScheduleTeacher(schedTeacher);
                    schedTeacher.CantScheduleParent(currentParent);
                } 
            }                        
            else if (currentParent.getSchedulingSession() == Parent.Session.BOTH &&
                    currentParent.getRequests().contains(schedTeacher))
            {
                if (this.FirstMatchInAft(currentParent, schedTeacher) == null &&
                        this.FirstMatchInEve(currentParent, schedTeacher) == null)
                {
                    currentParent.CantScheduleTeacher(schedTeacher);
                    schedTeacher.CantScheduleParent(currentParent);
                }
            }
            
            /*Did we just take care of their last request?*/
            if (currentParent.getRequests().isEmpty())
            {
                iterator.remove();
            }
        }
    }

    /**
     * RemoveParentFromWorkingAndMaster removes the parent completely from
     * the algorithm. 
     * @param triple ScheduleTriple class
     */
    private void RemoveParentFromWorkingAndMaster(ScheduleTriple triple)
    {
        this.MasterCopy.remove(triple.GetParent());
        this.WorkingCopy.remove(triple.GetParent());
    }
}

