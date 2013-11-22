/*
 * <H1>Angry Birds AI</H1>
 * <H2>CPE 480 - Artificial Intelligence</H2>
 * <H3>Group Project - Team 11</H3>
 * 
 * <H2>Team Member(s):</H2>
 * <LI>Chris N. Hartley (cnhartle@calpoly.edu)
 * <LI>Brent Williams (brent.robert.williams@gmail.com)
 * <LI>Alex Bozarth (ajbando@gmail.com)
 * <LI>Taylor Nesheim (tnesheim@calpoly.edu)
 */
package team11.core;

import java.io.File;
import team11.core.agent.Team11Agent;
import team11.gui.AgentFrame;
import cmdln.CommandLineParser;
import cmdln.CommandLineParser.CommandLineTemplate;
import cmdln.args.CommandLineParameter;
import cmdln.args.basic.FileArgument;


/**
 * <P>This class provides the main entry point for an implementation of an AI 
 * agent to play Angry Birds through the extension, provided by AIBirds.org,
 * that must be enabled in Google's Chrome web browser. The specified agent to
 * load must extend the {@link team11.core.agent.Team11Agent Team11Agent} 
 * abstract class; otherwise this will immediately terminate with the 
 * corresponding error code. Refer to the documentation for the 
 * {@link team11.core.agent.Team11Agent Team11Agent} on the specific
 * requirements for implementing the agent.
 * 
 * <H3>Command-line Parameters</H3>
 * <P>This application uses command-line parameters to adjust what will be 
 * loaded and how it will behave while running the agent.
 * 
 * <H3>Options for {@code team11.jar}</H3>
 * <P><TABLE>
 * <TR><TD><I>Option(s)</I></TD><TD><I>Function(s)</I></TD></TR>
 * <TR><TD NOWRAP VALIGN="top">-a --agent [path]</TD><TD>Required to specify
 * your implementation of the agent which must extend the
 * {@code team11.core.agent.Team11Agent}.</TD></TR>
 * <TR><TD NOWRAP VALIGN="top">-d --debug</TD><TD><I>[optional]</I> Sets the
 * debugging for the agent to print out all debugging information when calling
 * the internal {@code debug(String)} method. Default the debugging is set to
 * {@code false}; not to print.</TD></TR>
 * <TR><TD NOWRAP VALIGN="top">-h --help</TD><TD><I>[optional]</I> If present,
 * all other parameters are ignored and the usage for the jar file is printed
 * to standard output, {@code System.out}.</TD></TR>
 * <TR><TD NOWRAP VALIGN="top">-l --log [path]</TD><TD><I>[optional]</I> Sets
 * the log file to be appended to when calling the internal {@code log(String)}
 * method. Default prints to {@code System.out}.</TD></TR>
 * <TR><TD NOWRAP VALIGN="top">-s --show</TD><TD><I>[optional]</I> Shows the
 * debugging screen capture frame.</TD></TR>
 * </TABLE></P>
 * 
 * <H3>Example Usage</H3>
 * <PRE><CODE> [file system]
 *   rootfolder
 *     +-- team11
 *     |     +-- agent
 *     |           +-- MyAgent.class          // Must extend the Team11Agent!
 *     +-- team11.jar
 *     +-- log.txt
 * [command prompt]
 * %rootfolder> java -jar team11.jar -d -a team11\agent\MyAgent.class -l log.txt
 * </CODE></PRE>
 * <P>This execution will; (1) turns {@code ON} debugging with the option "-d",
 * (2) load the specified agent based on the path following the "-a" option, and
 * (3) set the log output file to the path following the "-l" option.
 * 
 * @author Chris N. Hartley (cnhartle@calpoly.edu)
 * @see team11.core.agent.Team11Agent
 */
public class MainEntry {
	
	
	/**
     * Specifies the error code to use if the command-line arguments were not
     * in the correct format or unable to be parsed. This code is used in 
     * conjunction with the {@code System.ext(int)} termination method.
     */
    public static final int INVALID_COMMANDLINE_ARGUMENT_ERROR = 101;
    

    /**
     * The specified agent, in the command-line arguments, that extends the
     * {@link Team11Agent} class to be executed to run and play the Angry Birds
     * game through the extensions in Google's Chrome web browser.
     */
	private static Team11Agent agent = null;

	
	/**
	 * <P>Main entry point for the implementation of a specified AI agent to 
	 * play the Angry Birds game through the extensions in Google's Chrome web 
	 * browser.
	 * 
	 * @param args  the list of arguments passed to this entry point of the
	 *              package.
	 */
	public static void main(String[] args) {
		CommandLineParser clp =
                new CommandLineParser(getCommandLineTemplate());
        try {
            clp.parse(args);
            registerParameters(clp);
            
            agent.run();
        }
        catch (IllegalArgumentException iae) {
            String msg = "Illegal argument specified in command-line: "
                    + iae.getMessage();
            
            System.err.println(msg);
            System.out.println(msg + "\n\n" + clp);
            
            System.exit(INVALID_COMMANDLINE_ARGUMENT_ERROR);
        }
	}
	
	
	/**
	 * Registers all of the command-line parameter after parsing to their
	 * appropriate variables an initialize required objects.
	 * 
	 * @param clp the {@link CommandLineParser} that parsed out the command-line
	 *            parameters.
	 */
	private static void registerParameters(CommandLineParser clp) {
		if (clp.hasParameter("help")) {
			System.out.println(clp);
			System.exit(0);
		}
		
		FileArgument fa =
        		(FileArgument) clp.getParameter("agent").getArgument();
		processAndLoadAgentFromFile(fa.getArgument());
        
		if (clp.hasParameter("log")) {
			fa = (FileArgument) clp.getParameter("log").getArgument();
			Team11Agent.setLogFile(fa.getArgument());
		}
		Team11Agent.DEBUGGING = clp.hasParameter("debug");
		
		if (clp.hasParameter("show")) {
			AgentFrame frm = new AgentFrame(agent.getClass().getSimpleName());
			new Thread(frm).start();
		}
	}
	

	/**
	 * Loads the specified file for the corresponding agent which must be an 
	 * extension of the {@link Team11Agent} class.
	 * 
	 * @param agentFile  the {@link File} object for the agent file to load.
	 * 
	 * @throws IllegalArgumentException when any exceptions are caught while
	 *                                  attempting to load the specified agent
	 *                                  file.
	 */
	private static void processAndLoadAgentFromFile(File agentFile) {
		if (agentFile.exists()) {
            try {
            	String classPath = agentFile.getPath()
            			.replace("\\", ".")
            			.replace(".class", "");

				Object obj = Class.forName(classPath).newInstance();
				agent = (Team11Agent)obj;
			}
            catch (ClassNotFoundException e) {
            	throw new IllegalArgumentException("specified agent file does"
            			+ " not contain a valid class. " + e);
			}
            catch (InstantiationException e) {
            	throw new IllegalArgumentException("the specified agent is"
            			+ " either an interface or an abstract object or"
            			+ " requires arguments for the constructor. " + e);
			}
            catch (IllegalAccessException e) {
            	throw new IllegalArgumentException("the specified agent's"
            			+ " constructor is not public. " + e);
			}
        }
        else
        	throw new IllegalArgumentException("specified agent file does not"
        			+ " exist. " + agentFile);
	}


	/**
	 * Returns the command-line argument template for this entry point of the
	 * package.
	 * 
	 * @return  the command-line argument template for the main method.
	 * 
	 * @see cmdln.CommandLineParser.CommandLineTemplate
	 */
	private static final CommandLineTemplate getCommandLineTemplate() {
		CommandLineTemplate clt = new CommandLineTemplate();
		clt.setHeader("This application provides a dynamic class loader for the"
				+ " AI to play the Angry Birds game through the extensions in"
			    + " Google Chrome.");
		
		clt.add( new CommandLineParameter('a', "agent", new FileArgument())
				.setDescription("Used to specify the specific agent to load for"
						+ " playing Angry Birds. The class must extend the"
						+ " team11.core.agent.Team11Agent or an exception will"
						+ "be thrown.") );
		
		clt.add( new CommandLineParameter('d', "debug")
        		.setDescription("Enables debugging information to be"
        				+ " broadcast on the output stream.") );
		
		clt.add( new CommandLineParameter('h', "help")
        		.setDescription("Displays help information on the command-"
        				+ "line options.") );
		
		clt.add( new CommandLineParameter('l', "log", new FileArgument())
				.setDescription("Used to specify the log file to append to.") );

		clt.add( new CommandLineParameter('s', "show")
				.setDescription("Shows the debugging screen capture frame.") );
		
		return clt;
	}

}
