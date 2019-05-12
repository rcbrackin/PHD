import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Base64.Encoder;
import java.util.concurrent.TimeUnit;
	
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

public class HarvestOAI {

    static String file_delimiter = "" ;

    StatusLogger local_logger = null ;

	HttpClient client;
	HttpGet getRequest;
	HttpPost postRequest;
	HttpResponse response;
	BufferedReader rd;
	StringBuffer result;
	String line;

	String searchURL;
	String resumptionURL;
	int    HTTPtimeout = 30000 ;

    final static int ERROR_LEVEL = 1 ;
    final static int WARN0_LEVEL = 2 ;
    final static int INFO0_LEVEL = 3 ;
    final static int INFO1_LEVEL = 4 ;
    final static int INFO2_LEVEL = 5 ;
    final static int INFO3_LEVEL = 6 ;

	// --------------------------------------------
	// Delay plus messages
	// --------------------------------------------

	public void delay (int seconds, boolean log) {

        local_logger.logResult (INFO2_LEVEL,"delay(): Sleep for 5 secs") ;

		try {
		  TimeUnit.SECONDS.sleep(5);
		} catch (InterruptedException e) {
            local_logger.logResult (ERROR_LEVEL, e.toString()) ;
		}

        local_logger.logResult (INFO2_LEVEL,"delay(): Sleep for 5 secs - End" ) ;
	}

	// sadasd
	// -----------------------------

	private String extractToken (String line) {

		String token = "" ;

	    if (line.contains("<resumptionToken")) {

			local_logger.logResult (INFO3_LEVEL, "extractToken(): " + line ) ;

            int start = line.indexOf(">") ;
            int end = line.indexOf("</resumptionToken>") ;
            if (end-start > 1) {
              token = line.substring(start+1, end) ;
              int delim = token.indexOf("|") ;

              if (delim > 0)
              {
                int length = token.length();
	  	   	    String resStart = token.substring(0, delim) ;
			    String resEnd   = token.substring(delim+1, length) ;
			    token = resStart + "%7C" + resEnd ;
              }

                local_logger.logResult (INFO3_LEVEL, "extractToken() Token: " + token ) ;

            }
            else
            {
              token = "" ;
            }
        }
	    return token ;
	}

	// Issue HTTP Request and log results)

	private HttpResponse issueHTTPGetRequest (String url, boolean log) {

		try {
            local_logger.logResult (INFO2_LEVEL, "issueHTTPGetRequest(): Request: " + url) ;

			getRequest = new HttpGet(url);
			response = client.execute(getRequest);

            local_logger.logResult (INFO2_LEVEL, "Response Code : "
					+ response.getStatusLine().getStatusCode() + ": "
					+ response.getStatusLine().getReasonPhrase()) ;

		} catch (IOException e) {
            local_logger.logResult (ERROR_LEVEL, "issueHTTPGetRequest()" + e.toString()) ;
		} finally {
			// Close files
		}

		return response ;
	}

	public HttpResponse issueHTTPGetRequestWithTimeout (String url, boolean log, int timeoutms) {

		try {

			local_logger.logResult (INFO2_LEVEL, "issueHTTPGetRequestWithTimeout(): " + "http Request :" + url) ;

			getRequest = new HttpGet(url);

			RequestConfig config = RequestConfig.custom()
					.setConnectTimeout(timeoutms)
					.setConnectionRequestTimeout(timeoutms)
					.setSocketTimeout(timeoutms).build();

			CloseableHttpClient httpClient =
					HttpClientBuilder.create().setDefaultRequestConfig(config).build();

			getRequest.setConfig(config);

			response = httpClient.execute(getRequest);

            local_logger.logResult (INFO2_LEVEL, "issueHTTPGetRequestWithTimeout(): "+ "Response Code : "
					+ response.getStatusLine().getStatusCode() + ": "
					+ response.getStatusLine().getReasonPhrase());

		} catch (IOException e) {
            local_logger.logResult (ERROR_LEVEL, "issueHTTPGetRequestWithTimeout(): " + e.toString()) ;
		} finally {
			//		// Close files
		}

		return response ;
	}

	// Spool contents of HTTP Connection to a file
	// -------------------------------------------

    private String writeResultsToFile (HttpResponse response, String filename, boolean log) {

    	String token_lines ;
		String token = "" ;
		boolean resumption = false ;

		try {

            local_logger.logResult (INFO3_LEVEL, "WriteResultsToFile(): Start Spool Data: " + filename) ;

			BufferedWriter resultsFile = new BufferedWriter(
		  	  new FileWriter(filename));

			rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			result = new StringBuffer();

			line = "";
			token_lines = "";

			while ((line = rd.readLine()) != null) {

				resultsFile.write(line);
				if (line.contains("<resumptionToken")) {
					resumption = true;
				}

				if (resumption == true) {
					token_lines = token_lines + line ;
				}
			}

			token = extractToken(token_lines) ;

			resultsFile.close();

            local_logger.logResult (INFO3_LEVEL, "writeResultsToFile(): End: Spool data") ;

		} catch (IOException e) {
            local_logger.logResult (ERROR_LEVEL, "writeResultsToFile()" + e.toString()) ;
		} finally {

		// Close files
        }

		return token ;

    }

	// Open Service
	// ------------

	public void openServiceObject(StatusLogger logger, String delimiter) {

        local_logger = logger ;
        file_delimiter = delimiter ;
		client = HttpClientBuilder.create().build();
	}

	private void query (String baseURL, String outFileName) {

		// Build query from Base URL
		// -------------------------

		try {

    		searchURL = baseURL + "?verb=ListRecords&metadataPrefix=oai_dc" ;
	    	resumptionURL = baseURL + "?verb=ListRecords&resumptionToken=" ;

		    // Issue request for records
	  	    // -------------------------

    		// issueHTTPGetRequest (searchURL, true) ;
	    	response = issueHTTPGetRequestWithTimeout(searchURL, true, HTTPtimeout);

	    	// Loop round while 'Retry' response
		    // ---------------------------------

            local_logger.logResult (INFO1_LEVEL, "query(): " + response.getStatusLine().getStatusCode());

    		while (response.getStatusLine().getStatusCode() == 503) {

	    		delay (20, true) ;
		    	// issueHTTPGetRequest (searchURL, true) ;
			    response = issueHTTPGetRequestWithTimeout(searchURL, true, HTTPtimeout);

		    }

    		int filename_count = 0;
	    	String token = "" ;
		    boolean dataAvailable = true ;

    		// If data available, stream it into a file
	    	// ----------------------------------------

		    while (response.getStatusLine().getStatusCode() == 200
				&& dataAvailable) {

                local_logger.logResult (INFO1_LEVEL, "query: Data Available") ;

    			// Spool results to file
	    		// ---------------------

                token = writeResultsToFile(response, outFileName + "_"+ filename_count + ".xml", true) ;

                local_logger.logResult (INFO1_LEVEL, "query(): Data Write Done") ;

            // If there are more records (resumption block, get the next block, otherwise we are done)
            // ---------------------------------------------------------------------------------------

                local_logger.logResult (INFO1_LEVEL, "Check Token") ;

               if (!token.isEmpty()) {
                   local_logger.logResult (INFO1_LEVEL, "query(): Token Not Empty" + token + "Wait 5") ;

            	// Wait a bit

                	delay (2, true) ;

                   local_logger.logResult (INFO1_LEVEL, "query(): Issue Resumption Request" + resumptionURL + token) ;

            	// issueHTTPGetRequest (resumptionURL + token, true) ;
				response = issueHTTPGetRequestWithTimeout(resumptionURL, true, HTTPtimeout);

				while (response.getStatusLine().getStatusCode() == 503) {
                    local_logger.logResult (INFO1_LEVEL, "query: Having to Delay") ;
					delay (20, true) ;
            		// issueHTTPGetRequest (resumptionURL + token, true) ;
					response = issueHTTPGetRequestWithTimeout(resumptionURL, true, HTTPtimeout);

				}
            }
            else
            {
                local_logger.logResult (INFO1_LEVEL, "query(): Token Empty") ;
            	dataAvailable = false ;
            }

            filename_count++ ;

                local_logger.logResult (INFO1_LEVEL, "query(): End of Dataset") ;

		}

            local_logger.logResult (INFO1_LEVEL, "query(): End of Query") ;

		} catch (Exception e) {
            local_logger.logResult(ERROR_LEVEL,"query():" + e.toString() );
		}


	}

	private void resumption (String baseURL, String resumption_token, String outFileName) {

		try {

		// Build query from Base URL
		// -------------------------

	    	resumptionURL = baseURL + "?verb=ListRecords&resumptionToken=" ;

    		// Issue request for resumption records
	      	// ------------------------------------

    	    // issueHTTPGetRequest (resumptionURL + resumption_token, true) ;
    		response = issueHTTPGetRequestWithTimeout(resumptionURL + resumption_token, true, HTTPtimeout);

    		// Loop round while 'Retry' response
	    	// ---------------------------------

            local_logger.logResult (INFO0_LEVEL, "resumption(): " + response.getStatusLine().getStatusCode());

		    while (response.getStatusLine().getStatusCode() == 503) {

			    delay (20, true) ;
	    	// issueHTTPGetRequest (resumptionURL + resumption_token, true) ;
			    response = issueHTTPGetRequestWithTimeout(resumptionURL + resumption_token, true, HTTPtimeout);
		    }

    		int filename_count = 3;
	    	String token = "" ;
		    boolean dataAvailable = true ;

    		// If data available, stream it into a file
	    	// ----------------------------------------

		    while (response.getStatusLine().getStatusCode() == 200
				&& dataAvailable) {

                local_logger.logResult (INFO1_LEVEL, "resumption(): Data Available") ;

    			// Spool results to file
	    		// ---------------------

                token = writeResultsToFile(response, outFileName + "_"+ filename_count + ".xml", true) ;

                local_logger.logResult (INFO1_LEVEL, "resumption(): Data Write Done") ;

                // If there are more records (resumption block, get the next block, otherwise we are done)
                // ---------------------------------------------------------------------------------------
		    }
            local_logger.logResult (INFO1_LEVEL, "resumption(): End of Query") ;

    	} catch (Exception e) {
		    local_logger.logResult(ERROR_LEVEL,"query2():" + e.toString() );
	    }

	}


  private String query2 (String baseURL, String outFileName, String resumptionToken) {

	String nextToken ;

	try {

	// Build query from Base URL
	// -------------------------

	if (resumptionToken.isEmpty()) {
		searchURL = baseURL + "?verb=ListRecords&metadataPrefix=oai_dc" ;
	}
	else {
		searchURL = baseURL + "?verb=ListRecords&resumptionToken="+resumptionToken ;
	}

	// Issue request for records
  	// -------------------------

  	//issueHTTPGetRequest (searchURL, true) ;
	response = issueHTTPGetRequestWithTimeout(searchURL, true, HTTPtimeout);

	// Loop round while 'Retry' response
	// ---------------------------------

        local_logger.logResult (INFO3_LEVEL, "query2(): " + response.getStatusLine().getStatusCode());

	int trys = 0;
	while (response.getStatusLine().getStatusCode() == 503 && trys < 10) {


        local_logger.logResult (INFO1_LEVEL, "query2(): " + "Waiting");
		delay (20, true) ;
		// issueHTTPGetRequest (searchURL, true) ;
		response = issueHTTPGetRequestWithTimeout(searchURL, true, HTTPtimeout);
        local_logger.logResult (INFO3_LEVEL, "query2(): " + response.getStatusLine().getStatusCode());
		trys++ ;

	}

	// Spool results to file
	// ---------------------

	if (response.getStatusLine().getStatusCode() == 200) {
        local_logger.logResult (INFO3_LEVEL, "query2(): " + "Write out Data") ;
        nextToken = writeResultsToFile(response, outFileName, true) ;
        local_logger.logResult (INFO3_LEVEL, "query2(): " + "Data Write Done") ;
		return nextToken ;

        // If there are more records (resumption block, get the next block, otherwise we are done)
        // ---------------------------------------------------------------------------------------
	}
	else
	{
        local_logger.logResult (INFO3_LEVEL, "query2(): " + "End of Query (no 200 or 503 return") ;
		return "";
	}

	} catch (Exception e) {
        local_logger.logResult(ERROR_LEVEL,"query2():" + e.toString() );
		return "" ;
	}

  }


  // Primary visible methods
  // -----------------------

  public void harvestIdentify (String baseURL, String outPath) {

		searchURL = baseURL + "?verb=Identify";
		String outFileName = outPath + file_delimiter + "Results_Identify.xml" ;

		try {

		    // Issue request for records
		    // -------------------------

    		//issueHTTPGetRequest(searchURL, true);
	        response = issueHTTPGetRequestWithTimeout(searchURL, true, HTTPtimeout);

		    // Loop round while 'Retry' response
		    // ---------------------------------

            local_logger.logResult (INFO1_LEVEL, "harvestIdentify(): " + response.getStatusLine().getStatusCode());

    		int trys = 0;
	    	while (response.getStatusLine().getStatusCode() == 503 && trys < 5) {

                local_logger.logResult (INFO1_LEVEL, "harvestIdentify(): " + "Waiting");
			    delay(20, true);
			    //issueHTTPGetRequest(searchURL, true);
			    response = issueHTTPGetRequestWithTimeout(searchURL, true, HTTPtimeout);

                local_logger.logResult (INFO1_LEVEL, "harvestIdentify(): " + response.getStatusLine().getStatusCode());
			    trys++;

    		}

	    	// Spool results to file
		    // ---------------------

		if (response.getStatusLine().getStatusCode() == 200) {

            local_logger.logResult (INFO1_LEVEL, "harvestIdentify(): " + "Write out Data");
			writeResultsToFile(response, outFileName, true);
            local_logger.logResult (INFO1_LEVEL, "harvestIdentify(): " + "Data Write Done");
			delay (20, true) ;
			return ;

			// If there are more records (resumption block, get the next block, otherwise we are done)
			// ---------------------------------------------------------------------------------------

		} else {

            local_logger.logResult (INFO1_LEVEL, "harvestIdentify(): " + "End of ListSets (no 200 or 503 return");
			delay (20, true) ;
			return ;

		}

		} catch (Exception e) {
            local_logger.logResult(ERROR_LEVEL,"harvestIdentify():" + e.toString() );
		}
  }

	public void harvestListSets (String baseURL, String outPath) {

		try {

			searchURL = baseURL + "?verb=ListSets";
			String outFileName = outPath + file_delimiter + "Results_ListSets.xml";

			// Issue request for records
			// -------------------------

			// issueHTTPGetRequest(searchURL, true);
			response = issueHTTPGetRequestWithTimeout(searchURL, true, HTTPtimeout);

			// Loop round while 'Retry' response
			// ---------------------------------

            local_logger.logResult(INFO1_LEVEL, "harvestListSets(): " + response.getStatusLine().getStatusCode());

			int trys = 0;
			while (response.getStatusLine().getStatusCode() == 503 && trys < 5) {

                local_logger.logResult(INFO1_LEVEL, "harvestListSets(): Waiting");
				delay(20, true);
				// issueHTTPGetRequest(searchURL, true);
				response = issueHTTPGetRequestWithTimeout(searchURL, true, HTTPtimeout);
                local_logger.logResult(INFO1_LEVEL, "harvestListSets(): " + response.getStatusLine().getStatusCode());
				trys++;

			}

			// Spool results to file
			// ---------------------

			if (response.getStatusLine().getStatusCode() == 200) {
                local_logger.logResult(INFO1_LEVEL, "harvestListSets(): " + "Write out Data");
				writeResultsToFile(response, outFileName, true);
                local_logger.logResult(INFO1_LEVEL, "harvestListSets(): " + "Data Write Done");
				delay(20, true);
				return;

				// If there are more records (resumption block, get the next block, otherwise we are done)
				// ---------------------------------------------------------------------------------------
			} else {
                local_logger.logResult(INFO1_LEVEL, "harvestListSets(): " + "End of ListSets (no 200 or 503 return");
				delay(20, true);
				return;
			}
		} catch (Exception e) {
            local_logger.logResult(ERROR_LEVEL,"harvestListSets():" + e.toString() );
		}
	}

	public void harvestListRecords (String baseURL, String outPath, int count, String token) {

    	int filename_count = count  ;
    	String resumptionToken ;
    	String outFileName ;

    	try {

    		outFileName = outPath + file_delimiter + "results_ListRecords_" + filename_count + ".xml" ;

            local_logger.logResult (INFO1_LEVEL, "harvestListRecords(): " + "Harvest Start (First Query Start): " + outFileName) ;
    		resumptionToken = query2 (baseURL, outFileName,  token) ;
            local_logger.logResult (INFO1_LEVEL, "harvestListRecords(): " + "Harvest Start (First Query) End") ;

    		while (!resumptionToken.isEmpty()) {
    			filename_count++ ;
				outFileName = outPath + file_delimiter + "results_ListRecords_" + filename_count /* + "_" + resumptionToken */ + ".xml" ;
                local_logger.logResult (INFO1_LEVEL, "harvestListRecords(): Harvest (Resumption): " + outFileName + " Token: " + resumptionToken) ;
				resumptionToken = query2 (baseURL, outFileName, resumptionToken) ;
                local_logger.logResult (INFO1_LEVEL, "harvestListRecords(): Harvest (Resumption) End") ;
    		}

    	} catch (Exception e) {
            local_logger.logResult(ERROR_LEVEL,"harvestListSets():" + e.toString() );
		}
	}
}
