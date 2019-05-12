import java.sql.*;
import java.util.Date;
import java.sql.Timestamp;

public class OAIDataToSQL {

    Connection connection = null;

    boolean establishConnection() {

        try {

            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/researchscan", "root", "admin");

            System.out.println("Java JDBC MySQL Connection");
            // When this class first attempts to establish a connection, it automatically loads any JDBC 4.0 drivers found within
            // the class path. Note that your application must manually load any JDBC drivers prior to version 4.0.
            // Class.forName("org.mysql.Driver");

            System.out.println("Connected to MySQL database!");
            return true;

        } catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
            return false;
        }
    }

    boolean closeConnection() {

       // try {
          //  connection.close();
          //  connection = null;
            return true;
       // } catch (SQLException e) {
       //     System.out.println("Connection closed failure.");
       //     e.printStackTrace();
       //     return false;
       // }
    }

    boolean writeIdentify(int lnum, String respDate, String repName, String baseURL, String protVers,
                          String earliestDate, String delRec, String granularity) {

        try {

            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO oai_identify VALUES (?,?,?,?,?,?,?,?)");

            Date date = new Date();
            long time = date.getTime();
            Timestamp ts = new Timestamp(date.getTime());

            statement.setInt(1, lnum); // a_library_num
            statement.setString(2, respDate); // a_responseDate
            statement.setString(3, repName); // repositoryName
            statement.setString(4, baseURL); // baseURL
            statement.setString(5, protVers); // protocolVersion
            statement.setString(6, earliestDate); // earliestDatestamp
            statement.setString(7, delRec); // deletedRecord
            statement.setString(8, granularity); // granularity

            statement.addBatch();
            statement.executeBatch();

        } catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
        }

        return true;

    }

    // --------------------
    // List Records Methods
    // --------------------

    boolean writeListRecord(int lnum, String url, String identifier, String datestamp) {
        try {

            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO oai_ListRecord VALUES (?,?,?,?)");

            statement.setInt   (1, lnum); // a_library_num
            statement.setString(2, url); // a_responseDate
            statement.setString(3, identifier); // repositoryName
            statement.setString(4, datestamp); // baseURL

            statement.addBatch();
            statement.executeBatch();
            statement.close();

        } catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
        }

        return true;
    }

    boolean writeTitleRecord(int lnum, String identifier, String lang, String title) {
        try {

            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO oaiLR_dc_title VALUES (?,?,?,?)");

            statement.setInt   (1, lnum); // a_library_num
            statement.setString(2, identifier); // header identifier
            statement.setString(3, lang); // repositoryName
            statement.setString(4, title); // baseURL

            statement.addBatch();
            statement.executeBatch();
            statement.close();

        } catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
        }

        return true;
    }

    boolean writeDescrRecord(int lnum, String identifier, String lang, String description) {
        try {

            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO oaiLR_dc_description VALUES (?,?,?,?)");

            statement.setInt   (1, lnum); // a_library_num
            statement.setString(2, identifier); // header identifier
            statement.setString(3, lang); // repositoryName
            statement.setString(4, description); // baseURL

            statement.addBatch();
            statement.executeBatch();
            statement.close();

        } catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
        }

        return true;
    }

    boolean writeCreatorRecord(int lnum, String identifier, String creator) {
        try {

            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO oaiLR_dc_creator VALUES (?,?,?)");

            statement.setInt   (1, lnum); // a_library_num
            statement.setString(2, identifier); // header identifier
            statement.setString(3, creator); // repositoryName

            statement.addBatch();
            statement.executeBatch();
            statement.close();

        } catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
        }

        return true;
    }

    boolean writeGartnerTopicAndDateRecord(int year, int sequence, String topic, double maturity, int phase) {
        try {

            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO gartner_topics_and_dates VALUES (?,?,?,?,?)");

            statement.setInt   (1, year);     // a_library_num
            statement.setInt   (2, sequence); // a_library_num
            statement.setString(3, topic);    // header identifier
            statement.setDouble(4, maturity); // repositoryName
            statement.setInt   (5, phase);     // a_library_num

            statement.addBatch();
            statement.executeBatch();
            statement.close();

        } catch (SQLException e) {
            System.out.println("SQL Execution Failure");
            e.printStackTrace();
        }

        return true;
    }

    boolean writeGartnerTopicRecord(String topic) {
        try {

            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO gartner_topics VALUES (?)");

            statement.setString(1, topic);    // header identifier

            statement.addBatch();
            statement.executeBatch();
            statement.close();

        } catch (SQLException e) {
            System.out.println("SQL Execution Failure");
            e.printStackTrace();
        }

        return true;
    }

    boolean writeGartnerTopicAndWordRecord(String topic, String topic_word) {
        try {

            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO gartner_topic_words VALUES (?,?)");

            statement.setString(1, topic);    // header identifier
            statement.setString(2, topic_word);    // header identifier

            statement.addBatch();
            statement.executeBatch();
            statement.close();

        } catch (SQLException e) {
            System.out.println("SQL Execution Failure");
            e.printStackTrace();
        }

        return true;
    }

    boolean writeGartnerTopicAndPhraseRecord(String topic, String topic_phrase) {
        try {

            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO gartner_topic_phrases VALUES (?,?)");

            statement.setString(1, topic);    // header identifier
            statement.setString(2, topic_phrase);    // header identifier

            statement.addBatch();
            statement.executeBatch();
            statement.close();

        } catch (SQLException e) {
            System.out.println("SQL Execution Failure");
            e.printStackTrace();
        }

        return true;
    }

    boolean writeSuppRecord(int ident    , String ccode   , String country,
                             String region, String source  , String mainURL,
                             String area  , String comment) {
        try {

            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO oai_supplemental VALUES (?,?,?,?,?,?,?,?)");

            statement.setInt   (1, ident  );     // a_library_num
            statement.setString(2, ccode  );    // header identifier
            statement.setString(3, country);    // header identifier
            statement.setString(4, region );    // header identifier
            statement.setString(5, source );    // header identifier
            statement.setString(6, mainURL);    // header identifier
            statement.setString(7, area   );    // header identifier
            statement.setString(8, comment);    // header identifier

            statement.addBatch();
            statement.executeBatch();
            statement.close();

        } catch (SQLException e) {
            System.out.println("SQL Execution Failure");
            e.printStackTrace();
        }

        return true;
    }

}