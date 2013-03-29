/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package juliet;

/**
 *
 * @author Tyrone
 */
public class ScheduleTriple {
    private Parent.Session session;
    private Teacher teacher;
    private Parent parent;
    private Integer slot;
    
    ScheduleTriple()
    {
        this.session = null;
        this.teacher = null;
        this.slot = null;
        this.parent = null;
    }
    
    public void SetParent(Parent p)
    {
        this.parent = p;
    }
    
    public void SetTeacher(Teacher t)
    {
        this.teacher = t;
    }
    
    public void SetSession(Parent.Session s)
    {
        this.session = s;
    }
    
    public void SetSlot(Integer i)
    {
        this.slot = i;
    }
    
    public Parent.Session GetSession()
    {
        return this.session;
    }
    
    public Parent GetParent()
    {
        return this.parent;
    }
    
    public Integer GetSlot()
    {
        return this.slot;
    }
    
    public Teacher GetTeacher()
    {
        return this.teacher;
    }
    
    public void Schedule()
    {
        this.parent.ScheduleTeacher(slot, session, teacher);
        this.teacher.ScheduleParent(slot, session, parent);
    }
}
