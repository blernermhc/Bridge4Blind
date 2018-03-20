// -*- mode: java; standard-indent: 4; indent-tabs-mode: nil; -*-
// Copyright (c) 2001 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.main;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Category;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;


/***********************************************************************
 *  Singleton class used to initialize log4j within a JVM.
 *  ALL classes that use the logger should use this class to initialize
 *  the logging system.
 ***********************************************************************/
public class Logger
{
    private static Category s_cat = null;

    public static final String LOG_DEBUG = "debug";
    public static final String LOG_INFO  = "info";
    public static final String LOG_WARN  = "warn";
    public static final String LOG_ERROR = "error";
    public static final String LOG_FATAL = "fatal";

    /** some servlets look for this parameter in the request and, if found,
     * reinitialize the logger using the indicated logging level */
    public static final String RESET_LEVEL_PARAM_NAME = "ComClickshareLogLevel";
    
    public static void displayProcessId()
    {
        //------------------------------
		// display this thead's process ID
		//------------------------------
		//int pid = ProcessId.findProcessId();
		//if (s_cat != null) s_cat.info(" process Id: " + pid);   	
    }
    
    
    /***********************************************************************
     * Prepares the log4j logging service for use.
     *
     * @param p_level   the level at which we want logging.  It should be
     *                  one of Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR, or Level.FATAL.
     *
     * @return  true if initialized, false if already initialized (this is
     *          NOT an error indication.
     **********************************************************************/    
    public static boolean initialize ( Level p_level )
    {
    		if (s_cat != null) return false;
    		
    		s_cat = Category.getInstance(Logger.class.getName());
    		Configurator.setLevel("lerner", p_level);
    		
    		s_cat.info("Logging initialized at level: " + p_level);
    		return true;
    }
    
    /***********************************************************************
     * Prepares the log4j logging service for use.
     *
     * @param p_level   the level at which we want logging.  It should be
     *                  one of "debug", "info", "warn", "error", or "fatal".
     *
     * @return  true if initialized, false if already intitialized (this is
     *          NOT an error indication.
     *
     **********************************************************************/    
    public static boolean initialize ( String p_level )
    {

        if ( s_cat != null)
        {
            // already initialized
        		displayProcessId();
            return false;
        }

        // to save logging to a file, uncomment the next several lines
        // and comment out the next line of BasicConfigurator.configure();
        //
        //        // create a FileAppender to save all Clickshare logging
        //        FileAppender fileAppender = null;
        //        try
        //            {
        //                fileAppender =  
        //                    new FileAppender(new TTCCLayout(), "/var/opt/clickshare/logs/clickshare.log", true);
        //            }
        //        catch (IOException ex)
        //            {
        //                System.err.println("Failed to allocate a FileAppender for log4j logging: " + ex.toString());
        //            }
        //	BasicConfigurator.configure(fileAppender);

        // Basic configuration
        // TODO: is this needed? BasicConfigurator.configure();        
        // Create the object for this class to use for logging messages
        // (also indicates that initialization has been run)
        s_cat = Category.getInstance(Logger.class.getName());

        
        // Modify the logging level if specified
	if (p_level == null || p_level.equals("debug"))
	{
	    // Do nothing so that all logs occur
		Configurator.setLevel("lerner", Level.DEBUG);
	}
	else if (p_level.equals("info"))
	{
	    // Category.getDefaultHierarchy().disable(Priority.DEBUG);
		Configurator.setLevel("lerner", Level.INFO);
	}
	else if (p_level.equals("warn"))
	{
	    // Category.getDefaultHierarchy().disable(Priority.INFO);
		Configurator.setLevel("lerner", Level.WARN);
	}
	else if (p_level.equals("error"))
	{
	    // Category.getDefaultHierarchy().disable(Priority.WARN);
		Configurator.setLevel("lerner", Level.ERROR);
	}
	else if (p_level.equals("fatal"))
	{
	    // Category.getDefaultHierarchy().disable(Priority.ERROR);
		Configurator.setLevel("lerner", Level.FATAL);
	}
	else
	{
	    s_cat.info("Unrecognized log option: '" + p_level + "'");
	    // Category.getDefaultHierarchy().disableAll();
		Configurator.setLevel("lerner", Level.DEBUG);
	}
	s_cat.info("Logging initialized at level: " + p_level);

	displayProcessId();
       return true;	// initialized
    }

    public static void resetConfiguration ()
    {
        BasicConfigurator.resetConfiguration();
        s_cat = null;	// so init will run
    }

}
