
import java.io.PrintWriter;
import java.io.FileNotFoundException ;

public class StatusLogger {

    final static int ERROR_LEVEL = 1 ;
    final static int WARN0_LEVEL = 2 ;
    final static int INFO0_LEVEL = 3 ;
    final static int INFO1_LEVEL = 4 ;
    final static int INFO2_LEVEL = 5 ;
    final static int INFO3_LEVEL = 6 ;

    int current_log_level = 0 ;

    PrintWriter outputStream = null;

    public int openLogfile (String logFilename, int level) {

        current_log_level =  level ;

        try {

            outputStream = new PrintWriter(logFilename) ;
            return 1;

        } catch (FileNotFoundException e) {

            System.out.println("ERROR-FileNotFound:<" + logFilename+">") ;   //Some error logging
            return 0 ;
        }

    }

    public void closeLogfile () {
        outputStream.close() ;
    }

    public void logResult(int log_level, String messageString) {

        if (log_level <= current_log_level) {
            switch (log_level) {

                case 1: {
                    System.out.println("ERROR-" + messageString);
                    outputStream.println("ERROR-" + messageString);
                    outputStream.flush();
                    break;
                }

                case 2: {
                    System.out.println("WARN0-" + messageString);
                    outputStream.println("WARN0-" + messageString);
                    outputStream.flush();
                    break;
                }

                case 3: {
                    System.out.println("INFO0-" + messageString);
                    outputStream.println("INFO0-" + messageString);
                    outputStream.flush();
                    break;
                }

                case 4: {
                    System.out.println("INFO1-" + messageString);
                    outputStream.println("INFO1-" + messageString);
                    outputStream.flush();
                    break;
                }

                case 5: {
                    System.out.println("INFO2-" + messageString);
                    outputStream.println("INFO2-" + messageString);
                    outputStream.flush();
                    break;
                }

                case 6: {
                    System.out.println("INFO3-" + messageString);
                    outputStream.println("INFO3-" + messageString);
                    outputStream.flush();
                    break;
                }
            }
        }
    }
}
