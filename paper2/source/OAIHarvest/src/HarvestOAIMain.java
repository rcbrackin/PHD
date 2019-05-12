public class HarvestOAIMain {

    static String file_delimiter = "" ;

    final static int ERROR_LEVEL = 1 ;
    final static int WARN0_LEVEL = 2 ;
    final static int INFO0_LEVEL = 3 ;
    final static int INFO1_LEVEL = 4 ;
    final static int INFO2_LEVEL = 5 ;
    final static int INFO3_LEVEL = 6 ;

    public static void output_usage () {

        System.out.println("Usage: OAIHarvest <command> <error level> <baseURL> <outputPath> [<startNo>] [<resumptionCode>]");
        System.out.println("  <command>       : I=Identify, S=ListSets, R=ListRecords, PI=Process Identify XML, PR=Process ListRecords, PL=ProcessListSets");
        System.out.println("  <error level>   : 0-6: 0 = no errors, 3=INFO0 6=INFO2");
        System.out.println("  <startNo>       : for ListRecords when resuming. Next file number");
        System.out.println("  <resumptionCode>: for ListRecords when resuming. resumption code");
    }

    public static void main(String[] args) {


        StatusLogger logger = new StatusLogger () ;

        // System.out.println("INFO0-Start Of Processing" + " " + args[0] + " "+args[1]);

        String os = System.getProperty("os.name").toLowerCase() ;
        if (os.indexOf("win") >= 0) {
            file_delimiter = "\\" ;
        }
        else {
            file_delimiter = "/" ;
        }
        if (args[0].charAt(0) == 'I') {

            // ========================================================
            // OAI Harvest using Identify Verb
            // ========================================================

            if (args.length < 4) {
                output_usage () ;
                return ;
            }

            logger.openLogfile(args[3]+ file_delimiter + "Harv_Identify_statusLog.txt", Integer.parseInt(args[1]));
            logger.logResult(INFO0_LEVEL, "Start Of Processing" + " " + args[0] + " "+args[1] + " " + args[2] + " " + args[3]);

            HarvestOAI oai = new HarvestOAI();
            oai.openServiceObject(logger, file_delimiter);
            oai.harvestIdentify(args[2], args[3]);

            logger.logResult(INFO0_LEVEL,"End of Processing") ;
            logger.closeLogfile();
        }
        if (args[0].charAt(0) == 'L') {

            // ========================================================
            // OAI Harvest using ListSets Verb
            // ========================================================

            if (args.length < 4) {
                output_usage () ;
                return ;
            }

            logger.openLogfile(args[3]+ file_delimiter + "Harv_ListSets_statusLog.txt", Integer.parseInt(args[1])) ;
            logger.logResult(INFO0_LEVEL, "Start Of Processing" + " " + args[0] + " "+args[1] + " " + args[2] + " " + args[3]);

            HarvestOAI oai = new HarvestOAI();
            oai.openServiceObject(logger,file_delimiter);
            oai.harvestListSets(args[2], args[3]);

            logger.logResult(INFO0_LEVEL,"End of Processing") ;
            logger.closeLogfile();
        }
        if (args[0].charAt(0) == 'R') {

            // ========================================================
            // OAI Harvest using ListRecords Verb
            // ========================================================

            if (args.length < 5) {
                output_usage () ;
                return ;
            }

            logger.openLogfile(args[3]+ file_delimiter + "Harv_ListRecords_statusLog.txt", Integer.parseInt(args[1])) ;
            logger.logResult(INFO0_LEVEL, "Start Of Processing" + " " + args[0] + " "+args[1] + " " + args[2] + " " + args[3]);

            HarvestOAI oai = new HarvestOAI();
            oai.openServiceObject(logger, file_delimiter);
            oai.harvestListRecords(args[2], args[3], 0, "");

            logger.logResult(INFO0_LEVEL,"End of Processing") ;
            logger.closeLogfile();
        }
        if (args[0].charAt(0) == 'P' && args[0].charAt(1) == 'R') {

            // Process ListRecords XML
            // =======================

            if (args.length < 5) {
                output_usage () ;
                return ;
            }

            // 0 = Command (PL)
            // 1 = Logging level
            // 2 = File to process
            // 3 = log file folder
            // 4 = library number (we allocated)


            int library_number = Integer.valueOf(args[4]) ;

            logger.openLogfile(args[3]+ file_delimiter + "Proc_ListRecords_statusLog.txt", Integer.parseInt(args[1])) ;
            logger.logResult(INFO0_LEVEL, "Start Of Processing" + " " + args[0] + " "+args[1] + " " + args[2] + " " + args[3]);

            ProcessOAI proc = new ProcessOAI () ;
            proc.configureLogging(logger, file_delimiter) ;
            proc.processListRecord(library_number, args[2]);

            logger.logResult(INFO0_LEVEL,"End of Processing") ;
            logger.closeLogfile();

        }

        if (args[0].charAt(0) == 'P' && args[0].charAt(1) == 'Z') {

            // Process ListRecords XML
            // =======================

            if (args.length < 5) {
                output_usage () ;
                return ;
            }

            // 0 = Command (PL)
            // 1 = Logging level
            // 2 = File to process
            // 3 = log file folder
            // 4 = library number (we allocated)

          //  logger.openLogfile(args[3]+ file_delimiter + "Proc_ListRecords_list_statusLog.txt", Integer.parseInt(args[1])) ;
          //  logger.logResult(INFO0_LEVEL, "Start Of Processing" + " " + args[0] + " "+args[1] + " " + args[2] + " " + args[3]);

            ProcessOAI proc = new ProcessOAI () ;
            proc.initDB () ;
         //   proc.configureLogging(logger, file_delimiter) ;
//            proc.processListRecord(library_number, args[2]);
            proc.processListRecordListFile (args[2], args[3]) ;
            proc.closeDB () ;

         //   logger.logResult(INFO0_LEVEL,"End of Processing") ;
         //   logger.closeLogfile();
        }

        if (args[0].charAt(0) == 'P' && args[0].charAt(1) == 'C') {

            // Clean up ListRecords XML
            // =======================

            if (args.length < 5) {
                output_usage () ;
                return ;
            }

            // 0 = Command (PL)
            // 1 = Logging level
            // 2 = File to process
            // 3 = log file folder
            // 4 = library number (we allocated)

            //  logger.openLogfile(args[4]+ file_delimiter + "Proc_ListRecords_list_statusLog.txt", Integer.parseInt(args[1])) ;
            //  logger.logResult(INFO0_LEVEL, "Start Of Processing" + " " + args[0] + " "+args[1] + " " + args[2] + " " + args[3]);

            ProcessOAI proc = new ProcessOAI () ;
            //   proc.configureLogging(logger, file_delimiter) ;
//            proc.processListRecord(library_number, args[2]);
            proc.cleanupListRecordListFile (args[2], args[3], args[4]) ;

            //   logger.logResult(INFO0_LEVEL,"End of Processing") ;
            //   logger.closeLogfile();
        }

        if (args[0].charAt(0) == 'P' && args[0].charAt(1) == 'L') {

            // Process ListSets XML
            // ====================

            // 0 = Command (PL)
            // 1 = Logging level
            // 2 = File to process
            // 3 = log file folder

            if (args.length < 4) {
                output_usage () ;
                return ;
            }

            logger.openLogfile(args[4]+ file_delimiter + "Proc_ListSets_statusLog.txt", Integer.parseInt(args[1])) ;
            logger.logResult(INFO0_LEVEL, "Start Of Processing" + " " + args[0] + " "+args[1] + " " + args[2] + " " + args[3]);

            ProcessOAI proc = new ProcessOAI () ;
            proc.configureLogging(logger, file_delimiter) ;
            proc.processIdentifyRecordListFile(args[2],args[3]);
            logger.logResult(INFO0_LEVEL,"End of Processing") ;
            logger.closeLogfile();
        }

        if (args[0].charAt(0) == 'P' && args[0].charAt(1) == 'I') {

            // Process An individual Identify XML File
            // ===========================================

            if (args.length < 4) {
                output_usage () ;
                return ;
            }

            logger.openLogfile(args[3]+ file_delimiter + "Proc_Identify_statusLog.txt", Integer.parseInt(args[1])) ;
            logger.logResult(INFO0_LEVEL, "Start Of Processing" + " " + args[0] + " "+args[1] + " " + args[2] + " " + args[3]);

            ProcessOAI proc = new ProcessOAI () ;
            proc.configureLogging(logger, file_delimiter) ;
            proc.processIdentifyRecord(args[2]);
            logger.logResult(INFO0_LEVEL,"End of Processing") ;
            logger.closeLogfile();
        }

        if (args[0].charAt(0) == 'G') {

            // Load Ancillery CSV files into tables
            // ====================================

            ProcessOAI proc = new ProcessOAI () ;
            //proc.loadGartnerTopicAndDateFile (args[2]) ;
            proc.loadGartnerTopicFile (args[3]) ;
        }

        if (args[0].charAt(0) == 'S') {

            // Load Ancillery CSV files into tables
            // ====================================

            ProcessOAI proc = new ProcessOAI () ;
            proc.loadSupplementalFile (args[2]) ;

        }

        if (args[0].charAt(0) == 'T') {

            String testString = "aa/ bb (cc) dd ee" ;
            String[] testList =  testString.split("[ |/|(|)]+") ;

            for (int i = 0; i < testList.length;i++) {
                System.out.println("["+testList[i]+"]") ;
            }

//            logger.openLogfile(args[3]+ file_delimiter + "Proc_Identify_statusLog.txt", Integer.parseInt(args[1])) ;
//            logger.logResult(INFO0_LEVEL, "Start Of Processing" + " " + args[0] + " "+args[1] + " " + args[2] + " " + args[3]);
/*
            ProcessOAI proc = new ProcessOAI () ;

//            proc.configureLogging(logger, file_delimiter) ;

            proc.myFileConvert (args[2],args[3]) ;

//            logger.logResult(INFO0_LEVEL,"End of Processing") ;
//
            logger.closeLogfile();
*/        }

        // System.out.println("INFO0-End of Processing");
    }

}