package juliet;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Date;

public class DBConn {
    
    private static final int SESSION_STATUS_NEW = 1;
    private static final int SESSION_STATUS_OPEN = 2;
    private static final int SESSION_STATUS_CLOSED = 3;
    
    private static final int TIME_BLOCK_AFT = 1;
    private static final int TIME_BLOCK_EVE = 2;
    private static final int TIME_BLOCK_BOTH = 0;

	private Connection conn = null;
	private String connStr = "jdbc:h2:";
	private String schema;
	private String username;
	private String password;
    private String comment;
	private int aftSlots = -1, eveSlots = -1, meetLen = -1, rsid = -1, schedule = -1, dayId;
	private HashMap<Integer, ArrayList<Integer>> parentRequests;
	private Set<Parent> parents;

   	public DBConn(String dbFile, String schema, String username,
        String password, int dayId, String comment) throws ClassNotFoundException {

        this(dbFile, schema, username, password, dayId);
        this.comment = comment;
    }

	public DBConn(String dbFile, String schema, String username, String password, int dayId)
			throws ClassNotFoundException {
		this.username = username;
		this.password = password;
		this.schema = schema;
		connStr += dbFile;
		Class.forName("org.h2.Driver");
        this.dayId = dayId;
	}
	
	public Set<Parent> getParentsForCurrSession() throws Exception {
		try {
			this.rsid = this.getSessionID();
			this.meetLen = this.getMeetingLen();
			this.aftSlots = this.getAftSlots();
			this.eveSlots = this.getEveSlots();
			this.getParentsForSession(this.getSessionID(), this.dayId);
			this.conn.close();
			this.conn = null;
			return this.parents;
		} catch (ClassNotFoundException cnfe) {
			System.err.println(cnfe.getMessage());
		} catch (SQLException sqle) {
			System.err.println(sqle.getMessage());
		}
		return null;
	}
	
	public HashMap<Integer, ArrayList<Integer>> getRequestsForCurrSession() throws Exception {
		try {
			this.rsid = this.getSessionID();
			this.meetLen = this.getMeetingLen();
			this.aftSlots = this.getAftSlots();
			this.eveSlots = this.getEveSlots();
			this.getRequests(rsid);
			this.conn.close();
			this.conn = null;
			return this.parentRequests;
		} catch (ClassNotFoundException cnfe) {
			System.err.println(cnfe.getMessage());
		} catch (SQLException sqle) {
			System.err.println(sqle.getMessage());
		}
		return null;
	}
	
	public int getNumTeachersForCurrSession() throws Exception {
		int result = -1;
		try {
			result = getNumTeachersForSession(this.getSessionID(), this.dayId);
			this.conn.close();
			this.conn = null;
		} catch (ClassNotFoundException cnfe) {
			System.err.println(cnfe.getMessage());
		} catch (SQLException sqle) {
			System.err.println(sqle.getMessage());
		}
		return result;
	}
	
	/**
	 * 
	 * @param pids
	 * @param tids
	 * @param slots
	 * @return number of inserted/updated records; -1 if failed
	 * @throws Exception 
     */
    public int addAppointment(int pid, int tid, int slot, Parent.Session sess) throws Exception {
        int result = -1;
        try {
            
            this.rsid = this.getSessionID();
            if(this.schedule <= 0) {
                this.schedule = this.addSchedule(this.rsid, this.comment);
            }
            
            result = this.addAppointment(this.schedule, pid, tid, slot,
                sessionToTimeBlock(sess));

        } catch (ClassNotFoundException cnfe) {
            System.err.println(cnfe.getMessage());
        } catch (SQLException sqle) {
            System.err.println(sqle.getMessage());
        }
        return result;
    }

    private int sessionToTimeBlock(Parent.Session s) {
        switch(s) {
            case BOTH_AFTERNOON:
            case AFTERNOON:
                return TIME_BLOCK_AFT;
            case BOTH_EVENING:
            case EVENING:
                return TIME_BLOCK_EVE;
            case BOTH:
                return TIME_BLOCK_BOTH;
            default:
                return -1;
        }
    }
	
	private Connection getConn() throws SQLException, ClassNotFoundException {
		if (conn == null) {
			Class.forName("org.h2.Driver");
			conn = DriverManager.getConnection(connStr, username, password);
		}
		return conn;
	}

    /** Return registration session ID
     * @throws Exception 
     */
	private int getSessionID() throws Exception {
		if(this.rsid < 0)
            setSessionInfo();

        return this.rsid;
	}

    /** Set this.{rsid,aftSlots,eveSlots,meetLen}. We try to get a session
      * which is closed. Exception if none such found.
     * @throws Exception 
      */
    private void setSessionInfo() throws Exception {

        getConn();

        String sql = "SELECT * FROM " + schema + ".reg_session "
                + "WHERE (status = " + SESSION_STATUS_CLOSED + ")";
                //+ "LIMIT 1";

        Statement stm = conn.createStatement();
        stm.execute(sql);
        ResultSet rs = stm.getResultSet();

        if (rs.next()) {

            this.meetLen = rs.getInt("meet_len");

            // Time block length in minutes (converting down from ms)
            long aftLen = ( rs.getTime("aft_end").getTime() -
                rs.getTime("aft_start").getTime() ) / 1000 / 60;
            this.aftSlots = (int) Math.floor( aftLen / this.meetLen );

            long eveLen = ( rs.getTime("eve_end").getTime() -
                rs.getTime("eve_start").getTime() ) / 1000 / 60;
            this.eveSlots = (int) Math.floor( eveLen  / this.meetLen );

            this.rsid = rs.getInt("rsid");
            
            return;
        }

        throw new Exception("No closed registration session found");

    }
    
    /** Get the requests for the day we were given
     */	
	private void getRequests(int regSessionId) throws SQLException,
			ClassNotFoundException {
		
		Integer tid, pid;
		parentRequests = new HashMap<Integer, ArrayList<Integer>>();
		
		getConn();
		String sql = "SELECT pid, tid FROM " + schema + ".requests "
				+ "WHERE (rsid = ? AND did = ?) GROUP BY pid, tid";

		PreparedStatement stm = conn.prepareStatement(sql);
		stm.setInt(1, regSessionId);
        stm.setInt(2, this.dayId);
		stm.execute();
		ResultSet rs = stm.getResultSet();
		while (rs.next()) {
			tid = new Integer(rs.getInt("tid"));
			pid = new Integer(rs.getInt("pid"));
			
			if(!parentRequests.containsKey(pid)) {
				parentRequests.put(pid, new ArrayList<Integer>());
			}
			parentRequests.get(pid).add(tid);
		}
	}

    /** Get all of the parents for the given reg session
     * @throws Exception 
     */
	private void getParentsForSession(int regSessionId, int dayId)
			throws Exception {

		int pid, block;
		Parent pt;
		this.parents = new HashSet<Parent>(); 

		getConn();

		String sql = "SELECT DISTINCT requests.pid AS pid, parents.tb AS tb FROM " + schema + ".requests "
            + "INNER JOIN " + schema + ".parents ON (requests.pid=parents.pid) "
            + "WHERE (rsid = ? AND did = ?)";

		PreparedStatement stm = conn.prepareStatement(sql);
		stm.setInt(1, regSessionId);
        stm.setInt(2, dayId);
		stm.execute();
		ResultSet rs = stm.getResultSet();
		while (rs.next()) {
			pid = rs.getInt("pid");
			pt = new Parent(this.getAftSlots(), this.getEveSlots());
			pt.setId(pid);
			block = rs.getInt("tb"); //getTimeBlock(regSessionId, pid);
			if (block == TIME_BLOCK_AFT) {
				pt.setSession(Parent.Session.AFTERNOON);
			} else if (block == TIME_BLOCK_EVE) {
				pt.setSession(Parent.Session.EVENING);
			} else if (block == TIME_BLOCK_BOTH) {
				pt.setSession(Parent.Session.BOTH);
			}	
			parents.add(pt);
		}
	}

	private int getNumTeachersForSession(int regSessionId, int dayId)
        throws SQLException, ClassNotFoundException {

		getConn();

		String sql = "SELECT COUNT(DISTINCT TID) AS num FROM " + schema + ".requests "
				+ "WHERE (rsid = ? AND did = ?)";

		PreparedStatement stm = conn.prepareStatement(sql);
		stm.setInt(1, regSessionId);
		stm.setInt(2, dayId);
		stm.execute();
		ResultSet rs = stm.getResultSet();
		while (rs.next()) {
			return rs.getInt("num");
		}
		return -1;
	}

     /** Return meetLen, or if not set yet, have setSessionInfo() set the value
      * first.
     * @throws Exception 
      */
	private int getMeetingLen() throws Exception {

		if(this.meetLen < 0)
            setSessionInfo();

        return this.meetLen;
		
	}

    /** Return aftSlots, or if not set yet, have setSessionInfo() set the value
      * first.
      */
	int getAftSlots() throws Exception {

		if(this.aftSlots < 0)
            setSessionInfo();

        return this.aftSlots;

	}

	
    /** Return eveSlots, or if not set yet, have setSessionInfo() set the value
      * first.
     * @throws Exception 
      */
	int getEveSlots() throws Exception {
	
    	if (this.eveSlots < 0)
            setSessionInfo();

        return this.eveSlots;

	}

	private int addSchedule(int rsid, String comment)  throws Exception {
		getConn();
		ResultSet rs;
		String sql = "INSERT INTO " + schema + ".schedules (sid, did, rsid, comment) " +
						"VALUES (default, ?, ?, ?)";
		
		PreparedStatement stm = conn.prepareStatement(sql);
		
		stm.setInt(1, this.dayId);
		stm.setInt(2, getSessionID());
		stm.setString(3, comment);
		if(stm.executeUpdate() > 0) {
			sql = "SELECT MAX(sid) AS sid FROM " + schema + ".schedules WHERE (rsid = ?)";
			stm = conn.prepareStatement(sql);
			stm.setInt(1, getSessionID());
			if(stm.execute()) {
				rs = stm.getResultSet();
				while(rs.next()) {
					this.schedule = rs.getInt("sid"); 
					return this.schedule;
				}
			} 
		}
		return 0;
	}
	
    private int addAppointment(int sid, int pid, int tid, int slot, int tb)
        throws SQLException, ClassNotFoundException {

        getConn();
        String sql = "INSERT INTO " + schema + ".appointments" +
            " VALUES (?, ?, ?, ?, ?)";
        
        PreparedStatement stm = conn.prepareStatement(sql);
        
        stm.setInt(1, sid);
        stm.setInt(2, pid);
        stm.setInt(3, tid);
        stm.setInt(4, slot);
        stm.setInt(5, tb);
        
        return stm.executeUpdate();
    }
	
	private int addAppointments(int rsid, ArrayList<Integer> pids,
								ArrayList<Integer> tids, ArrayList<Integer> slots)
												throws SQLException, ClassNotFoundException {
		
		int i, total = 0;
		String sql;
		getConn();
		
		sql = "INSERT INTO " + schema + ".appointments (rsid, pid, tid, slot) " +
						"VALUES (?, ?, ?, ?)";
		
		PreparedStatement stm = conn.prepareStatement(sql);
		for(i=0; i < pids.size(); i++) {
			try {
				stm.setInt(1, rsid);
				stm.setInt(2, pids.get(i));
				stm.setInt(3, tids.get(i));
				stm.setInt(4, slots.get(i));
				total += stm.executeUpdate();
			} catch (SQLException sqle) {
				
			}
		}
		return total;
	}
	
}
