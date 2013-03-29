package juliet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Teacher class.
 * Container class for each teacher in St.Clements. 
 * Teachers themselves have no function, however contain vital contents to
 * keep track of available requests to meet with parents in the schedule.
 */
public class Teacher {

    // Instance variables
    private Integer id; // teacher id
    
    private int freeAftSlots; // # free afternoon slots
    private int freeEveSlots; // # free evening slots
    
    private List<Parent> aftAppts; // teacher's schedule of appointments in afternoon
    private List<Parent> eveAppts; // teacher's schedule of appointments in evening
    private Set<Parent> aftRequests; // teacher's list of requests in afternoon
    private Set<Parent> eveRequests; // teacher's list of requests in evening
    private Set<Parent> bothRequests; // teacher's list of requests by both session parents
    
    private Set<Parent> toCallAft; // teacher's afternoon call list
    private Set<Parent> toCallEve; // teacher's evening call list
    private Set<Parent> toCallBoth; // teacher's both session call list
    
    private Integer totalReqs; // total requests teacher received
    private Integer totalSched; // total meetings scheduled with teacher
    
    /**
     * Class constructor
     * @param aftSlots Number of afternoon slots for meeting day
     * @param eveSlots Number of evening slots for meeting day
     */
    public Teacher(Integer aftSlots, Integer eveSlots)
    {
        this.aftAppts = new ArrayList<Parent>(aftSlots);
        this.eveAppts = new ArrayList<Parent>(eveSlots);
        this.aftRequests = new HashSet<Parent>();
        this.eveRequests = new HashSet<Parent>();
        this.bothRequests = new HashSet<Parent>();
        
        this.toCallAft = new HashSet<Parent>();
        this.toCallEve = new HashSet<Parent>();
        this.toCallBoth = new HashSet<Parent>();
        
        this.freeAftSlots = aftSlots;
        this.freeEveSlots = eveSlots;
        
        for (Integer i = 0; i < aftSlots; i++)
        {
            this.aftAppts.add(null);
        }
        for (Integer i = 0; i < eveSlots; i++)
        {
            this.eveAppts.add(null);
        }        
        
        this.totalReqs = 0;
        this.totalSched = 0;
        
    }

    /**
     * Sets teacher's identification number
     * @param id identification number
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns teacher's identification number
     * @return identification number
     */
    public Integer getId() {
        return id;
    }
    
    /**
     * Adds a parent to teacher's request list and then increments the request
     * count
     * @param parent Parent requesting meeting with Teacher
     */
    public void IncreaseRequests(Parent parent)
    {
        if (parent.getSession() == Parent.Session.AFTERNOON)
        {
            this.aftRequests.add(parent);
        }
        else if (parent.getSession() == Parent.Session.EVENING)
        {
            this.eveRequests.add(parent);
        }
        this.totalReqs++;
    }

    /**
     * Returns afternoon request count
     * @return Afternoon request count
     */
    public Integer getAftRequestSize()
    {
        return this.aftRequests.size();
    }

    /**
     * Returns evening request count
     * @return Evening request count
     */
    public Integer getEveRequestSize()
    {
        return this.eveRequests.size();
    }

    /**
     * Moves parent that was once in Both session to a strict evening or
     * afternoon session as determined by Strategy class.
     * @param parent Parent having session changed
     * @param session Session to be changed to
     */
    public void moveBothParentToNewSession(Parent parent, Parent.Session session)
    {
        boolean contains = this.bothRequests.remove(parent);
        if (session == Parent.Session.AFTERNOON && contains)
        {
            this.aftRequests.add(parent);
        }
        else if (session == Parent.Session.EVENING && contains)
        {
            this.eveRequests.add(parent);
        }
    }

    /**
     * Schedules the parent with the Teacher
     * @param slot Slot to be scheduled in
     * @param session Session the meeting is placed
     * @param parent Parent to have meeting with
     */
    public void ScheduleParent(Integer slot, Parent.Session session, Parent parent)
    {
        if (session == Parent.Session.AFTERNOON)
        {
            this.freeAftSlots--;
            this.aftAppts.set(slot, parent);
        }
        else if (session == Parent.Session.EVENING)
        {
            this.freeEveSlots--;
            this.eveAppts.set(slot, parent);
        }
        this.totalSched++;
    }    

    /**
     * Removes parent from Teacher's request list as they cannot be scheduled
     * in any way possible
     * @param p Parent to remove
     */
    public void CantScheduleParent(Parent p)
    {
        if (p.getSession() == Parent.Session.AFTERNOON)
        {
            this.aftRequests.remove(p);
            this.toCallAft.add(p);
        }
        else if (p.getSession() == Parent.Session.EVENING)
        {
            this.eveRequests.remove(p);
            this.toCallEve.add(p);
        }
        
    }

    /**
     * Returns priority of the Teacher
     * @param session Session to base priority on
     * @return Priority of the Teacher
     */
    public Integer GetPriority(Parent.Session session)
    {
        if (session == Parent.Session.AFTERNOON)
        {
            return (-1 * this.freeAftSlots);
        }
        else 
        {
            return (-1 * this.freeEveSlots);
        }
    }

    /**
     * Checks if the Teacher is free in the specified afternoon slot
     * @param slot Slot to check if teacher is free in afternoon
     * @return True if teacher is free at slot in afternoon, false if else.
     */
    public boolean FreeAftAptAtSlot(Integer slot)
    {
        return (this.aftAppts.get(slot) == null);
    }

    /**
     * Checks if the Teacher is free in the specified evening slot
     * @param slot Slot to check if teacher is free in Evening
     * @return True if teacher is free at slot in evening, false if else
     */
    public boolean FreeEveAptAtSlot(Integer slot)
    {
        return (this.eveAppts.get(slot) == null);
    }

    /**
     * Returns the Teacher's afternoon appointments
     * @return Afternoon appointments
     */
    public List<Parent> GetAftAppts()
    {
        return this.aftAppts;
    }

    /**
     * Returns the Teacher's evening appointments
     * @return Evening appointments
     */
    public List<Parent> GetEveAppts()
    {
        return this.eveAppts;
    }

    /**
     * Returns the Teacher's To Call list for afternoon session
     * @return Afternoon To Call list
     */
    public Set<Parent> GetToCallAft()
    {
        return this.toCallAft;
    }

    /**
     * Returns the Teacher's To Call list for evening session
     * @return Evening To Call list
     */
    public Set<Parent> GetToCallEve()
    {
        return this.toCallEve;
    }

    /**
     * Returns the Teacher's total number of requests made by parent
     * @return total number of requests made by parent for said Teacher
     */
    public Set<Parent> getToCallAll() {
        HashSet<Parent> tmp = new HashSet<Parent>(this.toCallAft);
        tmp.addAll(this.toCallEve);
//        tmp.addAll(this.toCallBoth);
        return tmp;
    }
    
    public Integer GetTotalReqAmount()
    {
        return this.totalReqs;
    }

    /**
     * Returns the total number of parents scheduled with Teacher
     * @return Total number of parents scheduled with Teacher
     */
    public Integer GetTotalSchedAmount()
    {
        return this.totalSched;
    }
}
