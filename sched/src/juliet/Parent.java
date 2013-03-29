package juliet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Parent class.
 * Container class for each parent, storing their preferences and requests made
 * once extracted from the web interface.
 */
public class Parent {
    // meeting sessions
    public enum Session {
        AFTERNOON, EVENING, BOTH, BOTH_AFTERNOON, BOTH_EVENING;
        
        public String toString () {
            switch (this) {
                case AFTERNOON: return "A";
                case EVENING: return "E";
                case BOTH: return "B";
                case BOTH_AFTERNOON: return "BA";
                case BOTH_EVENING: return "BE";
                default: return "?";
            }
        }
    } 
    
    private Integer id; // parent identification #
    private int totalRequests; // total number of meetings requested by parent
    private int totalScheduled; // total number of meetings scheduled
    private Session session; // the meeting session parent chose
    private Session schedulingSession;//Session we use for the scheduling algo (Since both parents are going to be modified).
    private Set<Teacher> requests; // requests yet to be handled
    
    private Integer earliestSlot; // the earliest meeting slot parent has
    private Integer latestSlot; // the latest meeting slot parent has
    private Parent.Session earliestSession; // the earliest meeting session parent has
    private Parent.Session latestSession; // the latest meeting session parent has
    
    private List<Teacher> aftAppts; // list of afternoon appointments
    private List<Teacher> eveAppts; // list of evening appointments
    private Set<Teacher> toCall; // To call list

    public Parent(int aftSlots, int eveSlots) {
        this.requests = new HashSet<Teacher>();
        this.aftAppts = initSlots(aftSlots);
        this.eveAppts = initSlots(eveSlots);
        this.requests = new HashSet<Teacher>();
        this.toCall = new HashSet<Teacher>();
    }
    
    /**
     * Class constructor
     * @param id Parent id
     * @param session Meeting session the parent chose
     * @param aftSlots Total number of afternoon slots
     * @param eveSlots Total number of evening slots
     */
    public Parent(int aftSlots, int eveSlots, int id, Session session) {
        this(aftSlots, eveSlots);
        this.id = id;
        this.session = session;
        this.schedulingSession = session;
    }
    
    private List<Teacher> initSlots(int n) {
        ArrayList<Teacher> ret = new ArrayList<Teacher>();
        for (int i = 0; i < n; i++)
            ret.add(null);
        return ret;
    }
    
    public Parent(int id, Session session, Integer aftSlots, Integer eveSlots) {
        this(aftSlots, eveSlots, id, session);
        this.totalScheduled = 0;
        this.earliestSession = null;
        this.latestSession = null;
        this.earliestSlot = null;
        this.latestSlot = null;
    }
    
    /**
     * Get this parents ID
     * @return the value of parent's ID. 
     */
    public Integer getId() {
        return id;
    }


    /**
     * Set this parent's ID
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Get the session this parent registered in
     * @return the value of parent's session
     */
    public Session getSession() {
        return session;
    }
    
    public Session getSchedulingSession()
    {
        return this.schedulingSession;
    }
    
    /**
     * Set this parent's session preference
     * @param s
     */
    public void setSession(Session s) {
        this.session = s;
        this.schedulingSession = s;
    }
    
    public void setSchedulingSession(Session s) {
        this.schedulingSession = s;
    }
    /**
     * Set the parents scheduling session, this is ONLY for parents in BOTH.
     * @param s the new session for the both parent
     */
    public void moveSessionBoth(Session s)
    {
        this.schedulingSession = s;
    }
    
    /**
     * Assign this parent with the requests made
     * @param requests teachers the parents wish to have a meeting with
     */
    public void setRequests(Set<Teacher> requests) {
        this.requests = requests;
    }
    
    /**
     * Get the set of meetings the parent has requested
     * @return the set of meetings the parent has requested
     */    
    public Set<Teacher> getRequests() {
        return requests;
    }

    /**
     * Add a teacher for this parent to request meeting with
     * @param t Teacher requested to have meeting with
     */
    public void addRequest(Teacher t)
    {
        this.requests.add(t);
        t.IncreaseRequests(this);
        this.totalRequests++;
    }

    /**
     * Returns the total number of meeting requests made by parent.
     * @return Number of requests made
     */
    public Integer GetNumberOfRequests()
    {
        return this.totalRequests;
    }

    /**
     * Returns the number of requests scheduled
     * @return Number of requests scheduled
     */
    /**
     * Return string representation of this parent:
     * <id>;<n requests>;<req1>,<req2>,...,<reqn>;<session>
     */
    public String toString() {
        String ret = "" + this.id + ";" + this.requests.size() + ";";
        Iterator<Teacher> iT = this.requests.iterator();
        
        while (iT.hasNext()) {
            ret = ret + iT.next().getId();
            if (iT.hasNext()) {
                ret = ret + ",";
            } else {
                ret = ret + ";";
            }
        }
                
        return ret + this.session;
    }

    public Integer GetNumberOfScheduledRequests()
    {
        return this.totalScheduled;
    }

    /**
     * Returns the parent's priority in the algorithm
     * @return Priority of parent
     */
    public Integer GetPriority()
    {
        return (-1 * this.requests.size());
    }

    /**
     * Schedules the parent to meet with the teacher.
     * @param slot Meeting slot # the parent will meet with the teacher in
     * @param session The session which the meeting will take place in
     * @param teacher The teacher the parent will be meeting
     */
    public void ScheduleTeacher(Integer slot, Parent.Session session, Teacher teacher)
    {
        if (session == Parent.Session.AFTERNOON)
        {
            this.aftAppts.set(slot, teacher);
        }
        else if (session == Parent.Session.EVENING)
        {
            this.eveAppts.set(slot, teacher);
        }
        this.requests.remove(teacher);
        this.totalScheduled++;
        
        this.UpdateEndPoints(slot, session);
    }

    /**
     * Update the parent's earliest/latest slot/session variables to current
     * values.
     * @param slot Slot to be updated to
     * @param session Session to be updated to
     */
    private void UpdateEndPoints(Integer slot, Parent.Session session)
    {
        if (this.earliestSession == null)
        {
            this.earliestSession = session;
            this.earliestSlot = slot;
        }
        if (this.latestSession == null)
        {
            this.latestSession = session;
            this.latestSlot = slot;
        }
        
        if (session == Parent.Session.EVENING && this.latestSession == Parent.Session.AFTERNOON)
        {
            this.latestSession = session;
            this.latestSlot = slot;
        }
        else if(this.latestSession == session && this.latestSlot < slot)
        {
            this.latestSlot = slot;
        }
        
        if (session == Parent.Session.AFTERNOON && this.earliestSession == Parent.Session.EVENING)
        {
            this.earliestSession = session;
            this.earliestSlot = slot;
        }
        else if(this.earliestSession == session && this.earliestSlot > slot)
        {
            this.earliestSlot = slot;
        }
        
    }

    /**
     * Adds a teacher to the To Call list as there is no possible way of
     * scheduling a meeting with them.
     * @param teacher Teacher to be added to to call list
     */
    public void CantScheduleTeacher(Teacher teacher)
    {        
        this.toCall.add(teacher);
    }

    /**
     * Checks if parent is free in a specified afternoon slot
     * @param slot Slot to check if parent is free in, in the afternoon session
     * @return True if parent is free at slot in afternoon, false if else.
     */
    public boolean FreeAftAptAtSlot(Integer slot)
    {
        return (this.aftAppts.get(slot) == null);
    }

    /**
     * Checks if parent is free in a specified evening slot
     * @param slot Slot to check if parent is free in, in the evening session
     * @return True if parent is free at slot in evening, false if else
     */
    public boolean FreeEveAptAtSlot(Integer slot)
    {
        return (this.eveAppts.get(slot) == null);
    }

    /**
     * Retrieve the afternoon meeting appointment list
     * @return List of scheduled afternoon appointments
     */
    public List<Teacher> GetApptsAft()
    {
        return this.aftAppts;
    }

    /**
     * Retrieve the evening meeting appointment list
     * @return List of scheduled evening appointments
     */
    public List<Teacher> GetApptsEve()
    {
        return this.eveAppts;
    }

    /**
     * Retrieve the To Call list
     * @return To Call list
     */
    public Set<Teacher> GetToCall()
    {
        return this.toCall;
    }

    /**
     * Calculates the total amount of time the parent will spend in the meeting
     * night in minutes.
     * @return Minutes the parent will spend in the meeting night
     */
    // This is only applicable for the params that Ye told us about.
    public Integer GetTotalTime()
    {
        Integer totalTime = 0;
        
        /*Check if they are only in 1 session*/
        if (this.earliestSession == this.latestSession)
        {
            int diff = this.latestSlot - this.earliestSlot + 1;
            totalTime += diff * 7;
            return totalTime;
        }
        
        /*For sure they are in 2 sessions (earliest slot must be aft latest must be eve*/
        totalTime += 30;

        int diff = 0;
        diff = this.aftAppts.size() - this.earliestSlot + 1;
        totalTime += diff * 7;
        diff = this.latestSlot + 1;
        totalTime += diff * 7;
        return totalTime;        
    }
}
