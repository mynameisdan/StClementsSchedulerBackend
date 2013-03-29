package juliet;

/**
 * ScheduleTriple
 * The ScheduleTriple class acts as a mediator for the Parent and Teacher who
 * are involved in the process of being scheduled together. It is used frequently
 * by the Strategy class to store a Parent the algorithm is currently handling
 * and it uses the stored information to find the best matching Teacher for
 * a meeting given the Parent's schedule.
 */
public class ScheduleTriple {

    // Instance variables
    private Parent.Session session;
    private Teacher teacher;
    private Parent parent;
    private Integer slot;

    /**
     * Class Constructor
     */
    ScheduleTriple()
    {
        this.session = null;
        this.teacher = null;
        this.slot = null;
        this.parent = null;
    }

    /**
     * Sets the parent for ScheduleTriple to hold
     * @param p Parent to store into ScheduleTriple
     */
    public void SetParent(Parent p)
    {
        this.parent = p;
    }

    /**
     * Sets the teacher for ScheduleTriple to hold
     * @param t Teacher to store into ScheduleTriple
     */
    public void SetTeacher(Teacher t)
    {
        this.teacher = t;
    }

    /**
     * Sets the session that ScheduleTriple will be working in
     * @param s Session to set
     */
    public void SetSession(Parent.Session s)
    {
        this.session = s;
    }

    /**
     * Sets the potential slot the meeting may occur between the parent and
     * teacher that ScheduleTriple is holding
     * @param i Slot number
     */
    public void SetSlot(Integer i)
    {
        this.slot = i;
    }

    /**
     * Retrieves the parent's session
     * @return Parent's session
     */
    public Parent.Session GetSession()
    {
        return this.session;
    }

    /**
     * Retrieves the Parent ScheduleTriple is holding
     * @return Parent ScheduleTriple is holding
     */
    public Parent GetParent()
    {
        return this.parent;
    }

    /**
     * Retrieves the potential slot number the meeting may be scheduled in
     * @return Slot number
     */
    public Integer GetSlot()
    {
        return this.slot;
    }

    /**
     * Retrieves the teacher stored in ScheduleTriple
     * @return Teacher stored
     */
    public Teacher GetTeacher()
    {
        return this.teacher;
    }

    /**
     * Schedule the meeting with the teacher and parent that is currently within
     * ScheduleTriple. The process which controls when and if this happens
     * is fully controlled within the Strategy class to make sure the
     * meeting only is booked if this is the best option available for both
     * teacher and parent.
     */
    public void Schedule()
    {
        this.parent.ScheduleTeacher(slot, session, teacher);
        this.teacher.ScheduleParent(slot, session, parent);
    }
}
