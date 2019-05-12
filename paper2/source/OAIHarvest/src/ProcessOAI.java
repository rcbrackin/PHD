import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ProcessOAI {

    static OAIDataToSQL oaisql = null ;

    static String file_delimiter = "\\" ;

    static StatusLogger local_logger = null ;

    public void initDB () {

        oaisql = new OAIDataToSQL () ;
        oaisql.establishConnection() ;

    }


    public static void configureLogging(StatusLogger logger, String file_delim)
    {
        local_logger   = logger         ;
        file_delimiter = file_delim ;
    }

    public static List<String[]> readData(String infilename)  {

        int count = 0;
        List<String[]> content = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(infilename))) {
            String line = "";
            while ((line = br.readLine()) != null) {
                content.add(line.split("\t"));
                count++ ;
            }
        } catch (FileNotFoundException e) {
            //Some error logging
        } catch (IOException i) {

        }
//	    System.out.println("NUMBER OF ROWS:" + count) ;
        return content;
    }

    public static boolean validateFileAsOAI( String infilename ) {


        List<String[]> content = new ArrayList<>();

        try(BufferedReader br1 = new BufferedReader(new FileReader(infilename))) {
            String line = "";
            boolean isXML = false ;
            boolean isOAI = false ;

            while ((line = br1.readLine()) != null) {
                if (line.contains("<?xml version=")) {
                    isXML = true ;
                }
                if (line.contains("<OAI-PMH")) {
                    isOAI = true ;
                }
                if (isXML && isOAI) {
 //                   System.out.println ("VALID OAI") ;
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            //Some error logging
            return false ;
        } catch (IOException i) {
            return false ;
        }
   //     System.out.println ("invalid OAI") ;
        return false;
    }

    static String processDescription(String description) {

        String norm_description = description.replaceAll("[().,;:]", " ");
        String norm_description1 = norm_description.replaceAll("[\"'’]", "");
        String norm_description2 = norm_description1.replaceAll("  ", " ");

        while (!norm_description2.equals(norm_description)) {

            norm_description = norm_description2;
            norm_description2 = norm_description.replaceAll("  ", " ");

        }

        System.out.println("Desc :" + description);
        System.out.println("NDesc:" + norm_description2);

        // find words

        Integer start_of_word = 0;
        Integer end_of_word = norm_description2.indexOf(" ");

        while (end_of_word != -1) {

            String word = norm_description2.substring(start_of_word, end_of_word);
            System.out.println("[" + word + "]");

            start_of_word = end_of_word + 1;
            end_of_word = norm_description2.indexOf(" ", start_of_word);
        }

        return "";
    }

    public static boolean processListRecord(int library_number, String identifyInputFile) {

        try {

            // Initialise the XML Reader (DOM Tree read)
            // -----------------------------------------

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(identifyInputFile);

            doc.getDocumentElement().normalize();
            System.out.println("<" + doc.getDocumentElement().getNodeName() + ">");

            /* Structure

            <record>
              <header/>
              <metadata/>

            */

            NodeList nListReq = doc.getElementsByTagName("request");
            Element request_elem = (Element) nListReq.item(0);
//            System.out.println("..<request>" + request_elem.getTextContent() );

            // Search for the key repeating node
            // ----------------------------------

            NodeList nList = doc.getElementsByTagName("record");

    //        System.out.println("..records [" + nList.getLength() + "]");

            for (int a = 0; a < nList.getLength(); a++) {

  //              System.out.println("..<record> [" + a + "]");

                Element records_elem = (Element) nList.item(a);
                Element header_elem = (Element) records_elem.getElementsByTagName("header").item(0);
      //          System.out.println("....<header> lib=" + library_number);

                Element identifier_elem = (Element) header_elem.getElementsByTagName("identifier").item(0);
                Element datestamp_elem = (Element) header_elem.getElementsByTagName("datestamp").item(0);

                String datestamp_text = datestamp_elem.getTextContent() ;

                // Update the Database
                // -------------------

     //           System.out.println ("OAI_ListRecords" +
       //                                library_number + " " +
         //                              request_elem.getTextContent() + " " +
           //                            datestamp_text.substring(0,10)) ;

                oaisql.writeListRecord(library_number,
                                       request_elem.getTextContent(),
                                       identifier_elem.getTextContent(),
                                       datestamp_text.substring(0,10)) ;

                Element setspec_elem = (Element) header_elem.getElementsByTagName("setSpec").item(0);

        //        System.out.println("......<identifier>" + identifier_elem.getTextContent());
          //      System.out.println("......<datestamp> " + datestamp_elem.getTextContent());
            //    System.out.println("......<setSpec>   " + setspec_elem.getTextContent());

                // ----------------------------------------------------------
                // get Metadata Items (assumes one per record)
                // ----------------------------------------------------------

                Element metadata_elem = (Element) records_elem.getElementsByTagName("metadata").item(0);
                if (metadata_elem != null) {

              //      System.out.println("....<metadata>");

                    Element oai_dc_elem = (Element) metadata_elem.getElementsByTagName("oai_dc:dc").item(0);
                //    System.out.println("......<oai_dc>");

                    // --------------------------------------------------------------------
                    // Get all Titles (multiple languages)
                    // --------------------------------------------------------------------

                    NodeList title_nl = oai_dc_elem.getElementsByTagName("dc:title");
                    for (int b = 0; b < title_nl.getLength(); b++) {
                        Element title_elem = (Element) title_nl.item(b);
                        String lang = title_elem.getAttribute("xml:lang");

                  //      System.out.println ("TITLE: ---[" + lang + "]" + title_nl.item(b).getTextContent());

                        // Write Title records

            //            System.out.println ("OAI_DC_Title " +
              //                  library_number + " " +
                //                request_elem.getTextContent() + " " +
                  //              lang + " " +
                    //            title_nl.item(b).getTextContent()) ;

                        oaisql.writeTitleRecord(library_number,
                                identifier_elem.getTextContent(),
                                lang,
                                title_nl.item(b).getTextContent()) ;
                    }

                    // --------------------------------------------------------------------
                    // Get all Descriptions (Multiple languages)
                    // --------------------------------------------------------------------

                    NodeList descr_nl = oai_dc_elem.getElementsByTagName("dc:description");
                    for (int b = 0; b < descr_nl.getLength(); b++) {
                        Element descr_elem = (Element) descr_nl.item(b);
                        String lang = descr_elem.getAttribute("xml:lang");
                    //    System.out.println ("DESCR: ---[" + lang + "]" + descr_nl.item(b).getTextContent());

                        // Write Title records

    //                    System.out.println ("OAI_DC_Descr " +
      //                          library_number + " " +
        //                        request_elem.getTextContent() + " " +
          //                      lang + " " + descr_nl.item(b).getTextContent()) ;

                       oaisql.writeDescrRecord(library_number,
                                identifier_elem.getTextContent(),
                                lang,descr_nl.item(b).getTextContent()) ;
                    }


                    //                    Element title_elem = (Element) oai_dc_elem.getElementsByTagName("dc:title").item(0);
//                    System.out.println("........<title>" + title_elem.getTextContent());

                    NodeList creator_nl = oai_dc_elem.getElementsByTagName("dc:creator");
                    for (int c = 0; c < creator_nl.getLength(); c++) {
                      //  System.out.println("........<creator> [" + c + "]" + creator_nl.item(c).getTextContent());

  //                      System.out.println ("OAI_DC_Creator " +
  //                              library_number + " " +
  //                              request_elem.getTextContent() + " " +
                    //            creator_nl.item(c).getTextContent()) ;

                        oaisql.writeCreatorRecord(library_number,
                                identifier_elem.getTextContent(),
                                creator_nl.item(c).getTextContent()) ;

                    }

                    Element description_elem = (Element) oai_dc_elem.getElementsByTagName("dc:description").item(0);
                 //   System.out.println("........<description>   " + description_elem.getTextContent());
                    //processDescription (description_elem.getTextContent()) ;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        oaisql.closeConnection();

        // write your code here
        return true;
    }


    public static void processListRecordListFile(String fileofLibraries, String listRecordsFolder) {

        List<String[]> fileContent = readData (fileofLibraries) ;

        if (fileContent == null) {
            System.out.println("No files found");
            return;
        }

        for (int i = 0; i < fileContent.size(); i++) {

            String[] linestrings = fileContent.get(i);
            if (linestrings.length > 1) {

                // Find each of the folders
                // ------------------------

                String sub_folder = linestrings[0].substring(0,linestrings[0].length()-4) ;
                String libnum     = sub_folder.substring(5,sub_folder.length()) ;
                String full_folder_name = listRecordsFolder + file_delimiter + sub_folder ;
                System.out.println(full_folder_name);

                // For each folder, find all the results files in it
                // -------------------------------------------------

                File dir = new File(full_folder_name);
                File[] listingAllFiles = dir.listFiles();

                if (listingAllFiles != null) {

                    int numfiles = listingAllFiles.length ;

                    for (int j = 0 ; j < numfiles; j++ ) {
                       String filename = listingAllFiles[j].getAbsolutePath() ;
                       if (filename.contains("results_ListRecords_")) {

                           int posn0 = filename.indexOf("info_");
                           int posn1 = filename.indexOf("results_ListRecords_");
                           int posn2 = filename.indexOf(".xml");

                           String subfolder = filename.substring(posn0,posn1-1) ;
                           String folder = filename.substring(0,posn1-1) ;
                           String number = filename.substring(posn1+20,posn2) ;
                           String fname = filename.substring(posn1,filename.length()) ;
                           String new_filename = listRecordsFolder + file_delimiter + subfolder + file_delimiter + fname ;

                           System.out.println("PROCESSING - Folder:" + folder + " Library number:" + libnum + " New:"+new_filename);
                           processListRecord(Integer.valueOf(libnum), new_filename) ;
               //            System.out.println("END PROCESSING " + new_filename ) ;
                       }
                    }
                }
            }
        }
    }

    public static void cleanupListRecordListFile(String fileofLibraries,
                                                 String inputListRecordsFolder,
                                                 String outputListRecordsFolder) {

        List<String[]> fileContent = readData (fileofLibraries) ;

        if (fileContent == null) {
            System.out.println("No files found");
            return;
        }

        for (int i = 0; i < fileContent.size(); i++) {

            String[] linestrings = fileContent.get(i);
            if (linestrings.length > 1) {

                // Find each of the folders
                // ------------------------

                String sub_folder = linestrings[0].substring(0,linestrings[0].length()-4) ;
                String full_folder_name = inputListRecordsFolder + file_delimiter + sub_folder ;
                System.out.println(full_folder_name);

                // For each folder, find all the results files in it
                // -------------------------------------------------

                File dir = new File(full_folder_name);
                File[] listingAllFiles = dir.listFiles();

                if (listingAllFiles != null) {

                    int numfiles = listingAllFiles.length ;

                    for (int j = 0 ; j < numfiles; j++ ) {
                        String filename = listingAllFiles[j].getAbsolutePath() ;
                        if (filename.contains("results_ListRecords_")) {

                            int posn0 = filename.indexOf("info_");
                            int posn1 = filename.indexOf("results_ListRecords_");
                            int posn2 = filename.indexOf(".xml");

                            String subfolder = filename.substring(posn0,posn1-1) ;
                            String folder = filename.substring(0,posn1-1) ;
                            String number = filename.substring(posn1+20,posn2) ;
                            String fname = filename.substring(posn1,filename.length()) ;

                            String new_filename = outputListRecordsFolder + file_delimiter
                                                  + subfolder + file_delimiter + fname ;

                            System.out.println("-- Folder:" + folder + " Number:" + number + " New:"+new_filename);

                            myFileConvert(filename, new_filename) ;
                        }
                    }
                    //                processIdentify (full_filename)
                }
            }
        }
    }

    // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
    // IDENTITY PROCESSING
    // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

    public static boolean processIdentify(String identifyInputFile) {

        try {

            // Get the sequence number for this repository from the filename
            // -------------------------------------------------------------

            int start_index = identifyInputFile.indexOf("info_") + 5;
            int end_index = identifyInputFile.indexOf(".xml") ;

            String refnumber = identifyInputFile.substring(start_index, end_index) ;
            int ref_num = Integer.parseInt(refnumber) ;

            // Initialise the XML Reader (DOM Tree read)
            // -----------------------------------------

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(identifyInputFile);
            doc.getDocumentElement().normalize();

            /* Structure

      	    <responseDate>
	        <request verb=""/>
        	<Identify>
		        <repositoryName>
        		<baseURL>
	        	<protocolVersion>2.0</protocolVersion>
		        <adminEmail>revista@ucm.cl</adminEmail>
		        <earliestDatestamp>
		        <deletedRecord>
		        <granularity>
		        <compression> (1)
		        <compression> (2)
		        <description> (1)
    			    <oai-identifier>
	        			<scheme>oai</scheme>
		        		<repositoryIdentifier>
			        	<delimiter>:</delimiter>
				        <sampleIdentifier>
		        <description> (2)
			        <toolkit>
				        <title>
				        <author>
					    <name>
					    <email>
				        <version>
            */

            // Search for the key repeating node
            // ----------------------------------

            NodeList nList0 = doc.getElementsByTagName("responseDate");
            NodeList nList1 = doc.getElementsByTagName("repositoryName");
            NodeList nList2 = doc.getElementsByTagName("baseURL");
            NodeList nList3 = doc.getElementsByTagName("protocolVersion");
            NodeList nList4 = doc.getElementsByTagName("earliestDatestamp");
            NodeList nList5 = doc.getElementsByTagName("deletedRecord");
            NodeList nList6 = doc.getElementsByTagName("granularity");

            Element respDate       = (Element) nList0.item(0) ;
            String  respDateText   = respDate.getTextContent() ;

            Element repName        = (Element) nList1.item(0) ;
            String  repNameText    = repName.getTextContent() ;

            Element URLName        = (Element) nList2.item(0) ;
            String  URLNameText    = URLName.getTextContent() ;

            Element protVers       = (Element) nList3.item(0) ;
            String  protVersText   = protVers.getTextContent() ;

            Element eDateStamp     = (Element) nList4.item(0) ;
            String  eDateStampText = eDateStamp.getTextContent() ;

            Element deletedRec     = (Element) nList5.item(0) ;
            String  deletedRecText = deletedRec.getTextContent() ;

            Element granularity    =  (Element) nList6.item(0) ;
            String granularityText = granularity.getTextContent() ;

            System.out.println ("INFO-Doing number: " + ref_num) ;

            OAIDataToSQL oaisql    = new OAIDataToSQL();
            oaisql.establishConnection();

            oaisql.writeIdentify(ref_num, respDateText.substring(0,10), repNameText, URLNameText,
                    protVersText, eDateStampText.substring(0,10), deletedRecText, granularityText);

            oaisql.closeConnection();

        } catch (Exception e) {
            e.printStackTrace();
        }
        // write your code here
        return true;
    }

    //xxxxxxxxxxxxxx

    public static void processIdentifyRecord(String full_filename) {

        boolean validOAIfile = validateFileAsOAI (full_filename) ;
        processIdentify (full_filename) ;
    }

    public static void processIdentifyRecordListFile(String fileofIdentifyRecords, String identifyFolder) {

        List<String[]> fileContent = readData (fileofIdentifyRecords) ;

        if (fileContent == null) {
            System.out.println("No files found");
            return;
        }

        for (int i = 0; i < fileContent.size(); i++) {

            String[] linestrings = fileContent.get(i);
            if (linestrings.length > 1) {
//                System.out.println(linestrings[0] + ": " + linestrings[1]);
                String full_filename = identifyFolder + file_delimiter + linestrings[0] ;
                System.out.println("Lib: " + linestrings[0] + " cc: " + linestrings[3]) ;

              //  boolean validOAIfile = validateFileAsOAI (full_filename) ;
              //  processIdentify (full_filename) ;
            }
        }
    }

    public static void processMultipleIdentify(String FileofIdentifyRecords) {

        try {

            // Open the input file, etc
            // ------------------------
            /*
                String instring = "" ;
                File inputFile = new File("C:\\Development\\Data\\ListRecords\\ListRecords_0\\results.xml");


                processIdentifyRecord(instring) ;

            // Initialise the XML Reader (DOM Tree read)
            // -----------------------------------------

*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean closeDB () {
        oaisql.closeConnection() ;
        oaisql = null ;
        return false;
    }

    public static boolean myFileConvert (String infilename, String outfilename) {

        try {
                BufferedReader inputFile = new BufferedReader(new FileReader(infilename)) ;
                BufferedWriter outputFile = new BufferedWriter(new FileWriter(outfilename));

                String line = "";
                while ((line = inputFile.readLine()) != null) {

//                    String outstr = line.replaceAll("[^\\x20-\\x7e]", "");
                    outputFile.write(line + "\n\r");
                }

                inputFile.close();
                outputFile.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false ;
                //Some error logging
        } catch (IOException i) {
            i.printStackTrace();
            return false ;
        }
        return true ;
    }

    public static void pushTopicWords(String topic) {

        String[] topicWords =  topic.split("[ |/|(|)]+") ;

        // Push individual words first

        for (int i = 0; i < topicWords.length;i++) {

            System.out.println(topic + "\t" + topicWords[i]) ;
          //  oaisql.writeGartnerTopicAndWordRecord(topic, topicWords[i]) ;
        }

        // Push groups of two words

        for (int i = 0; i < topicWords.length-1;i++) {

         //   System.out.println(topic + "\t" + topicWords[i]+" "+ topicWords[i+1]) ;
         //   oaisql.writeGartnerTopicAndPhraseRecord (topic, topicWords[i]+" " + topicWords[i+1]) ;

        }
    }

    public static void loadGartnerTopicFile(String filename) {

        List<String[]> fileContent = readData(filename);

        if (fileContent == null) {
            System.out.println("No files found");
            return;
        }

        oaisql = new OAIDataToSQL();
        oaisql.establishConnection();

        // ignore first line as it is the titles.

        System.out.println("Topic\tTopicWord") ;

        for (int i = 1; i < fileContent.size(); i++) {

            String[] linestrings = fileContent.get(i);
            if (linestrings.length > 0) {

              //  oaisql.writeGartnerTopicRecord(linestrings[0]);
                pushTopicWords(linestrings[0]) ;
            }
        }

        oaisql.closeConnection();
    }


    public static void loadGartnerTopicAndDateFile(String filename) {

        List<String[]> fileContent = readData(filename);

        if (fileContent == null) {
            System.out.println("No files found");
            return;
        }

        OAIDataToSQL oaisql = new OAIDataToSQL();
        oaisql.establishConnection();

        // ignore first line as it is the titles.

        for (int i = 1; i < fileContent.size(); i++) {

            String[] linestrings = fileContent.get(i);
            if (linestrings.length > 1) {

                int year = Integer.valueOf(linestrings[0]);
                int seq = Integer.valueOf(linestrings[1]);
                double mat = Double.valueOf(linestrings[3]);
                int phase = Integer.valueOf(linestrings[4]);

                if (phase != 0) {
                    System.out.println("TOPIC ------ [" + linestrings[2] + "] " + year + "," + seq + ", " + mat + ", " + phase);
                }
                oaisql.writeGartnerTopicAndDateRecord(year, seq, linestrings[2], mat, phase);
            }
        }

        oaisql.closeConnection();
    }

    public static void loadSupplementalFile(String filename) {

        List<String[]> fileContent = readData(filename);

        if (fileContent == null) {
            System.out.println("No files found");
            return;
        }

        OAIDataToSQL oaisql = new OAIDataToSQL();
        oaisql.establishConnection();

        // ignore first line as it is the titles.

        for (int i = 1; i < fileContent.size(); i++) {

            String[] linestrings = fileContent.get(i);
            if (linestrings.length > 1) {

                int ident = Integer.valueOf(linestrings[0]);

                String mainURL = "" ;
                String area    = "" ;
                String comment = "" ;

                if (!linestrings[7].isEmpty()) mainURL = linestrings[7] ;
                if (!linestrings[8].isEmpty()) area    = linestrings[8] ;
                if (!linestrings[9].isEmpty()) comment = linestrings[9] ;

                oaisql.writeSuppRecord(ident,
                        linestrings[1] ,
                        linestrings[3] ,
                        linestrings[4] ,
                        linestrings[5] ,
                        mainURL ,
                        area    ,
                        comment ) ;
                }
        }

        oaisql.closeConnection();
    }

    public static void analyse_language () {





    }
}