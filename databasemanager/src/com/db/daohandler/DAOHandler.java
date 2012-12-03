package com.db.daohandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.db.outputservice.OutputService;
import com.db.outputservice.OutputType;

/**
 * This class handles getting and putting with the database. 
 * @author eeline
 *
 * Currently includes numerous features intended for debugging, should not be used until those are all disabled.
 */
public class DAOHandler {
	private static DAOHandler INSTANCE;
	private static List<String> entries = new ArrayList<String>(); //No longer does anything
	private static Connection connection;
	private static PreparedStatement defaultStatement;
	
	/**
	 * Gets an instance of the handler, only used in the OutputService.  
	 * @return
	 */
	public static DAOHandler getInstance(){
		if(DAOHandler.INSTANCE == null)
			DAOHandler.INSTANCE = new DAOHandler();
		return DAOHandler.INSTANCE;
	}
	
	/**
	 * Call to commit inputs to the database
	 */
	public void commit() {
		try {
			connection.commit();
			//TODO Remove debug print statement, change to return processed ResultSet
			print(get());
		} catch (SQLException e) {
			OutputService.push(OutputType.ERROR, e.getMessage());
		}
	}

	/** 
	 * use this to input properly formed SQLEntries
	 * @param sqlEntry
	 */
	public void put(SQLEntry sqlEntry){
		try {
			sqlEntry.get(connection.prepareStatement(sqlEntry.getInitializtionSQL())).execute();
			if(sqlEntry.get() == StatementPriority.ELEVATED)
				connection.commit(); 
			//TODO
			/* include a notify that's heard here and ensures no new commits run until after this one does
			ThreadService.push(new SQLTask(){
				@Override
				public void run() {
					try {
						connection.commit();
					} catch (SQLException e) {
						OutputService.push(e);
					}	
				}
			});
			 */
		} catch (SQLException e) {
			OutputService.push(e);
		}
	}
	
	private DAOHandler(){		
		try {
			connection = DriverManager.getConnection(SQLStrings.PROTOCOL + ":derbyDB;create=true"
					/*,new Properties("username", "password") -OR- ,getProperties()*/);
			configure();
		} catch (SQLException e) {
            OutputService.push(e);
            OutputService.push(OutputType.ERROR, e.getMessage());
		} catch (InstantiationException e) {
            OutputService.push(OutputType.ERROR, e.getMessage());
		} catch (IllegalAccessException e) {
            OutputService.push(OutputType.ERROR, e.getMessage());
		} catch (ClassNotFoundException e) {
            OutputService.push(OutputType.ERROR, e.getMessage());
		}
	}
	
	private void print(ResultSet resultSet) throws SQLException {
		while(resultSet.next()){
			OutputService.push(OutputType.INFO,resultSet.getString(1));
		}	
	}

	private ResultSet get() throws SQLException {
		return connection.createStatement().executeQuery(SQLStrings.GET_RESULTS);
	}	

	/*
	 * Initializes a table when preparing configuration. Also sets up the default statement. Called within the constructor.
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	private void configure() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException{
		Class.forName(SQLStrings.DRIVER).newInstance();
		connection.setAutoCommit(false); //lets user control pace of update
		Statement statement = connection.createStatement();
		statement.execute(SQLStrings.INIT_TABLE);
		defaultStatement = connection.prepareStatement(SQLStrings.CREATE_ENTRY);
	}
		
	@MethodNotYetImplemented
	@Deprecated
	private List<String> getProperties(){
		//TODO Configure to properly use a Properties file loader.
		return new DBProperties().get();
	}
	
	@SuppressWarnings("unused")
	@Deprecated
	private void push() throws SQLException{
		for(String entry : entries){
			defaultStatement.setString(1, entry);
			defaultStatement.execute();
		}
	}
	
	@SuppressWarnings("unused")
	@Deprecated
	private void put(String sqlStatement) throws SQLException{
		entries.add(sqlStatement);
	}

}
