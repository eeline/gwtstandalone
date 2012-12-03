package com.db.daohandler;

import java.sql.PreparedStatement;




/**
 * use this instead of passing strings about. Constructor gets data from agent, pass it to output service which passes
 * it to DAO handler. 
 *
 */
public interface SQLEntry {
	/** DAOHandler expects to pass this an initialized statement and receive one formatted with appropriate SQL. 
	 * It will be executed (but possibly not COMMITTED) immediately.
	 * 
	 * @param statement
	 * @return
	 */
	PreparedStatement get(PreparedStatement statement);
	/**
	 * DAO handler will commit immediately on ELEVATED priority, and will queue for later commit on NORMAL priority. 
	 * @return
	 */
	StatementPriority  get();
	
	/**
	 * SQL string used in the construction of the initial prepared statement.
	 */
	String getInitializtionSQL();
}
