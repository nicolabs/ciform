package net.jsunit;


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.Map;

import net.jsunit.configuration.Configuration;
import net.jsunit.configuration.ConfigurationProperty;
import net.jsunit.configuration.ConfigurationSource;
import net.jsunit.configuration.DelegatingConfigurationSource;
import net.jsunit.utility.SourcePathUtil;



/**
 * This configuration redefines {@link #url()} to point to a custom file
 * and gathers automatically some more properties.
 * 
 * @author http://nicobo.net/contact?subject=jsunit+ant
 */
public class TestLibRunnerConfigurationSource extends
        DelegatingConfigurationSource implements TestLibRunnerParameters
{
	protected static final String PARAM_TESTPAGE = "testPage";

	private File testPage = null;



	public TestLibRunnerConfigurationSource( ConfigurationSource source )
	{
		super( source );
	}



	public TestLibRunnerConfigurationSource()
	{
		this( Configuration.resolveSource() );
	}



	//
	// UTILITY METHODS
	//

	/**
	 * If the test page is not set yet or does not exist, creates it.
	 */
	public File getTestPage() throws URISyntaxException, IOException
	{
		if ( testPage == null || !testPage.exists() )
		{
			testPage = buildTestPage().writeToFile(); // throw URIx, IOx
		}
		return testPage;
	}



	public void setTestPage( File testPage )
	{
		this.testPage = testPage;
	}



	public URI getTestRunner() throws URISyntaxException, IOException
	{
		return getRequiredURISystemProperty( PROP_TESTRUNNER );
	}



	/**
	 * Simply throw an exception id the property is not found (helps
	 * keeping the code clear).
	 * 
	 * @param key
	 *            the name of the property to retrieve
	 * @return A well formed URI based on the value of the given property
	 * @throws IllegalArgumentException
	 *             if the given property doesn't exist.
	 */
	protected static URI getRequiredURISystemProperty( String key )
	        throws URISyntaxException
	{
		String val = System.getProperty( key );

		if ( val == null )
		{
			System.err.println( "Missing property : " + key );
			throw new IllegalArgumentException( "Missing property : " + key );
		}

		return SourcePathUtil.normalizePath( val );
	}



	/**
	 * <p>Builds a new test page from the current System properties.</p>
	 * 
	 * <p>See <tt>PROP_*</tt> constants for the list of recognised properties.</p>
	 */
	protected static TestPage buildTestPage() throws URISyntaxException,
	        IOException
	{
		// a. Gathers parameters from the System
		String project = System.getProperty( PROP_PROJECT, "Unknown project" );
		String jsUnitCore = getRequiredURISystemProperty( PROP_COREJS ).toASCIIString();
		String javascripts = System.getProperty( PROP_JAVASCRIPTS, "" );
		Map includes = new Hashtable();
		includes.put( TestPage.INCLUDE_JAVASCRIPT, SourcePathUtil.sourcePathToURI( javascripts ) );

		// b. Builds the test page from the parameters
		return new TestPage( project, jsUnitCore, includes );
	}



	//
	// ConfigurationSource IMPLEMENTATION
	//

	/**
	 * <p>Builds the URL based so that it points to the generated test page.</p>
	 * 
	 * <p>Before calling this method, make sure {@link #setTestSuitePage(File)} has been correctly set.</p>
	 * 
	 * <p>NOTE : This is a bit weird because the test page's file is created in this method (through a call to {@link #getTestPage()}),
	 * but must be deleted from "outside" by the calling unit test once done. This is because I had to hack into this class
	 * to reuse a maximum of existing code (in order to limit the risks of broken code with the future versions).</p>
	 * 
	 * @return The full URL to use with JsUnit (the existing property : {@value ConfigurationProperty#URL} is ignored)
	 * @throws IllegalArgumentException if a property is missing or is incorrect
	 * FIXME ? don't create the test page here ?
	 */
	public String url()
	{
		try
		{
			URI tr = getTestRunner();
			return new URI( tr.getScheme(), tr.getUserInfo(), tr.getHost(), tr.getPort(), tr.getPath(), PARAM_TESTPAGE
			        + "=" + getTestPage().getCanonicalPath(), tr.getFragment() ).toASCIIString();
		}
		catch ( URISyntaxException urise )
		{
			urise.printStackTrace( System.err );
			throw new IllegalArgumentException( urise );
		}
		catch ( IOException ioe )
		{
			ioe.printStackTrace( System.err );
			throw new IllegalArgumentException( ioe );
		}
	}

}
