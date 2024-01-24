package Environment.Metrics;


import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This is a static class, responsible for collecting and exporting metrics generated during experiments.
 * Many of its functions are optional.
 * A minimal use case would include:
 * a. adding {@link InstanceReport}s with {@link #newInstanceReport()}, and storing the results of experiments in them.
 * b. calling {@link #exportCSV(OutputStream, String[])} with a {@link java.io.FileOutputStream}, and an array of header fields.
 *
 * Example of a more advanced usecase:
 * a. calling {@link #addOutputStream(OutputStream, InstanceReportToString, HeaderToString)} with various {@link java.io.OutputStream}s.
 * b. calling {@link #setHeader(String[])} to set a header (enables valid csv output).
 * c1. adding {@link InstanceReport}s with {@link #newInstanceReport()}, and adding the results of experiments to them.
 * c2. after each report is filled, calling {@link InstanceReport#commit()} to immediately output its contents to the
 * output streams.
 */
public class Metrics {
    public static DateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm_ss-SSS");
    private static final char CSV_DELIMITER = ',';
    /**
     * Use ThreadLocal to store an instance of S_Metrics for each thread
     */
    private static final ThreadLocal<Metrics> threadLocalInstance = ThreadLocal.withInitial(Metrics::new);

    ////      MEMBERS      ////
    /**
     * Optional. Defines a header. Useful for formats such as CSV.
     */
    private String[] header = new String[0];
    /**
     *
     */
    private List<InstanceReport> reports = new ArrayList<>();
    // the following three lists are managed together, so that any index refers to the same OutputStream in outputStreams
    /**
     * OutputStreams for {@link InstanceReport#commit() comitted} {@link InstanceReport}s to be output to.
     */
    private List<OutputStream> outputStreams = new ArrayList<>();
    /**
     * Functions to convert {@link InstanceReport}s to strings for output.
     */
    private List<InstanceReportToString> instanceReportToStringsForOSs = new ArrayList<>();
    /**
     * Functions to convert the header to String to output at the start of an output stream. Can contain nulls, meaning
     * a header is not needed for the OutputStream of the same index.
     */
    private List<HeaderToString> headerToStringsForOSs = new ArrayList<>();

    ////      INTERFACES      ////
    /**
     * Defines a function which converts an {@link InstanceReport} to a String.
     * This class contains static methods which comply with this interface, and may be used when this interface is
     * required.
     * @see #instanceReportToStringCSV(InstanceReport).
     * @see #instanceReportToHumanReadableString(InstanceReport).
     */
    public interface InstanceReportToString{
        String instanceReportToString(InstanceReport instanceReport);
    }

    /**
     * Defines a function which converts a String array representing a header, to a String.
     * {@link Metrics} contains static methods which comply with this interface, and may be used when this
     * interface is required.
     * @see #headerArrayToStringCSV(String[]).
     */
    public interface HeaderToString{
        String headerToString(String[] headerArray);
    }

    // Access the singleton instance for the current thread
    private static Metrics getMetricsInstance() {
        return threadLocalInstance.get();
    }

    ////      SETTERS AND GETTERS      ////

    /**
     * Sets the {@link #header} and outputs the new header to all relevant streams.
     * @param newHeader the new header @NotNull
     * @throws IOException if an I/O error occurs when outputing the new header to one of the streams.
     */
    public void setHeaderAndOutputNewHeader(String[] newHeader) throws IOException {
        if(newHeader != null){
            getMetricsInstance().header = newHeader;
            if(newHeader.length > 0 ){outputHeaderToAllRelevantStreams();}
        }
    }

    /**
     * Sets the {@link #header}. Doesn't output the new header to any streams.
     * @param newHeader the new header @NotNull
     */
    public static void setHeader(String[] newHeader) throws IOException {
        if(newHeader != null){
            getMetricsInstance().header = newHeader;}
    }

    private static String[] getHeader() {
        return Arrays.copyOf(getMetricsInstance().header, getMetricsInstance().header.length);
    }

    public static void clearHeader() {
        getMetricsInstance().header = new String[0];}

    /**
     * Creates a new, empty, {@link InstanceReport}, saves a reference to it, and returns it.
     * @return a new instance of {@link InstanceReport}.
     */
    public static InstanceReport newInstanceReport(){
        InstanceReport newReport = new InstanceReport();
        getMetricsInstance().reports.add(newReport);
        return newReport;
    }

    /**
     * Returns the most recently created {@link InstanceReport}.
     * @return the most recently created {@link InstanceReport}.
     */
    public static InstanceReport getMostRecentInstanceReport(){
        return getMetricsInstance().reports.get(getMetricsInstance().reports.size()-1);
    }

    public static boolean removeReport(InstanceReport report){
        return getMetricsInstance().reports.remove(report);
    }

    public static void clearReports(){
        getMetricsInstance().reports.clear();
    }

    public static List<InstanceReport> getAllReports(){
        return List.copyOf(getMetricsInstance().reports);
    }

    /**
     * Adds the given output stream to the list of OutputStreams. When {@link InstanceReport}s are committed, or when
     * {@link #exportAll()} is called, {@link InstanceReport}s will be written to this given OutputStream.
     * If headerToString isn't null, writes the current {@link #header} to the given {@link OutputStream}.
     * @param outputStream an output stream.
     * @param instanceReportToString function to convert {@link InstanceReport}s to Strings to write them to the given {@link OutputStream}.
     * @param headerToString function to convert the header to String to write it to the given {@link OutputStream}. If null, header will not be written.
     * @throws IOException if an I/O error occurs.
     */
    public static void addOutputStream(OutputStream outputStream, InstanceReportToString instanceReportToString,
                                       HeaderToString headerToString) throws IOException {
        if (outputStream != null && instanceReportToString != null) {
            Metrics metricsInstance = getMetricsInstance();
            metricsInstance.outputStreams.add(outputStream);
            metricsInstance.instanceReportToStringsForOSs.add(instanceReportToString);
            metricsInstance.headerToStringsForOSs.add(headerToString); // null is interpreted as "no need for header"
            //output the header to the new stream if a header is needed and is set.
            if(metricsInstance.header.length > 0 && headerToString != null){
                outputStream.write(headerToString.headerToString(metricsInstance.header).getBytes());
            }
        }
    }

    /**
     * Adds the given output stream to the list of OutputStreams. When {@link InstanceReport}s are committed, or when
     * {@link #exportAll()} is called, {@link InstanceReport}s will be written to this given OutputStream.
     * Doesn't write a header to the stream.
     * @param outputStream an output stream.
     * @param instanceReportToString function to convert {@link InstanceReport}s to Strings to write them to the given {@link OutputStream}.
     * @throws IOException if an I/O error occurs.
     */
    public static void addOutputStream(OutputStream outputStream,
                                       InstanceReportToString instanceReportToString) throws IOException {
        addOutputStream(outputStream, instanceReportToString, null);
    }

    /**
     * Adds the given output stream to the list of OutputStreams. When {@link InstanceReport}s are committed, or when
     * {@link #exportAll()} is called, {@link InstanceReport}s will be written to this given OutputStream.
     * Uses the default {@link InstanceReportToString}.
     * Doesn't write a header to the stream.
     * @param outputStream an output stream.
     * @throws IOException if an I/O error occurs.
     */
    public static void addOutputStream(OutputStream outputStream) throws IOException {
        addOutputStream(outputStream, Metrics::instanceReportToStringCSV, Metrics::headerArrayToStringCSV);
    }

    public static void removeOutputStream(OutputStream outputStream){
        Metrics metricsInstance = getMetricsInstance();
        int streamIndex = metricsInstance.outputStreams.indexOf(outputStream);
        metricsInstance.outputStreams.remove(streamIndex);
        metricsInstance.headerToStringsForOSs.remove(streamIndex);
        metricsInstance.instanceReportToStringsForOSs.remove(streamIndex);
    }

    public static void clearOutputStreams(){
        Metrics metricsInstance = getMetricsInstance();
        metricsInstance.outputStreams.clear();
        metricsInstance.headerToStringsForOSs.clear();
        metricsInstance.instanceReportToStringsForOSs.clear();
    }

    /**
     * Clears all class fields, essentially resetting the class.
     */
    public static void clearAll(){
        clearHeader();
        clearReports();
        clearOutputStreams();
    }
    ////      OUTPUT      ////

    // nicetohave groupBy, which gets a comparator to group by
    // nicetohave sort, which gets a comparator to sort by

    ////    conversions to strings      ////

    //      csv     //

//    /**
//     * Returns a string representation of the current {@link #header}, in a format compatible with CSV.
//     * @return a string representation of the current {@link #header}, in a format compatible with CSV.
//     */
//    public static String currentHeaderToStringCSV(){
//        return headerArrayToStringCSV(header);
//    }

    /**
     * Returns a string representation of the given header, in a format compatible with CSV.
     * @param delimiter the delimiter to use to delimit the fields.
     * @return a string representation of the given header, in a format compatible with CSV.
     */
    private static String headerToStringCSV(String[] headerArray, char delimiter){
        StringBuilder headerLine = new StringBuilder();
        for(int i = 0 ; i < headerArray.length ; i++){
            String field = headerArray[i];
            headerLine.append(field);
            if(i != headerArray.length - 1) {headerLine.append(delimiter);} //no delimiter after the last field
        }
        headerLine.append('\n');
        return headerLine.toString();
    }

    /**
     * Returns a string representation of the given header, in a format compatible with CSV.
     * @return a string representation of the given header, in a format compatible with CSV.
     */
    public static String headerArrayToStringCSV(String[] headerArray){
        return headerToStringCSV(headerArray, Metrics.CSV_DELIMITER);
    }

    /**
     * Returns a string representation of the information in an instanceReport, in a format compatible with CSV.
     * Because CSV requires all lines adhere to a single header, only fields present in {@link #header} will be included.
     * @param instanceReport the InstanceReport to convert to a string. @NotNull.
     * @param delimiter the delimiter to use to delimit the fields.
     * @return a string representation of the information in an instanceReport, in a format compatible with CSV.
     */
    private static String instanceReportToStringCSV(InstanceReport instanceReport, char delimiter, String[] headerArray){
        StringBuilder reportLine = new StringBuilder();

        for(int i = 0; i< headerArray.length ; i++){
            String field = headerArray[i];
            if(instanceReport.hasField(field)){
                reportLine.append(wrapStringCSVSafety(instanceReport.getValue(field)));
            }
            if(i != headerArray.length - 1 ) {reportLine.append(delimiter);} //no delimiter after the last one
        }
        reportLine.append('\n');
        return reportLine.toString();
    }

    /**
     * Returns a string representation of the information in an instanceReport, in a format compatible with CSV.
     * Because CSV requires all lines adhere to a single header, only fields present in {@link #header} will be included.
     * @param instanceReport the InstanceReport to convert to a string. @NotNull.
     * @return a string representation of the information in an instanceReport, in a format compatible with CSV.
     */
    public static String instanceReportToStringCSV(InstanceReport instanceReport){
        return  instanceReportToStringCSV(instanceReport, Metrics.CSV_DELIMITER, getMetricsInstance().header);
    }

    /**
     * Adds "" marks in case the string might contain ',' to make it safe for CSV.
     * @param fieldValue - original value.
     * @return
     */
    private static String wrapStringCSVSafety(String fieldValue) {
        return "\"" + fieldValue + "\"";
    }

    //      human readable      //

    /**
     * Returns a string representation of the information in an instanceReport, in a format that is suitable for easy
     * reading. Useful for outputing to a console to monitor the experiment.
     * @param instanceReport the InstanceReport to convert to a string. @NotNull.
     * @return a string representation of the information in an instanceReport, in a format readable format.
     */
    public static String instanceReportToHumanReadableString(InstanceReport instanceReport){
        return instanceReport.toString() + '\n';
    }

    /**
     * Returns a string representation of the information in an instanceReport, in a format that is suitable for easy
     * reading. Useful for outputing to a console to monitor the experiment.
     * Skips outputting the solutions because they are long.
     * @param instanceReport the InstanceReport to convert to a string. @NotNull.
     * @return a string representation of the information in an instanceReport, in a format readable format.
     */
    public static String instanceReportToHumanReadableStringSkipSolutions(InstanceReport instanceReport){
        return instanceReport.toString(Set.of(InstanceReport.StandardFields.solution)) + '\n';
    }

    // nicetohave tosrting json

    ////      outputing to the streams      ////

    private static void outputHeaderToStream(OutputStream outputStream, String[] headerArray,
                                             HeaderToString headerToString) throws IOException {
        outputStream.write(headerToString.headerToString(headerArray).getBytes());
    }

    private static void outputHeaderToAllRelevantStreams() {
        Metrics metricsInstance = getMetricsInstance();
        for (int i = 0; i < metricsInstance.outputStreams.size(); i++) {
            HeaderToString headerToString = metricsInstance.headerToStringsForOSs.get(i);
            if(headerToString != null){
                try {
                    outputHeaderToStream(metricsInstance.outputStreams.get(i), getMetricsInstance().header, headerToString);
                } catch (IOException e) {
                    handleIO_Exception(e, metricsInstance.outputStreams.get(i), metricsInstance.headerToStringsForOSs.get(i).headerToString(getMetricsInstance().header));
                }
            }
        }
    }

    private static void outputInstanceReportToStream(OutputStream outputStream, InstanceReport instanceReport,
                                                     InstanceReportToString instanceReportToString) throws IOException {
        outputStream.write(instanceReportToString.instanceReportToString(instanceReport).getBytes());
    }

    private static void outputInstanceReportToAllStreams(InstanceReport instanceReport) {
        Metrics metricsInstance = getMetricsInstance();
        for (int i = 0; i < metricsInstance.outputStreams.size(); i++) {
            try {
                outputInstanceReportToStream(metricsInstance.outputStreams.get(i), instanceReport, metricsInstance.instanceReportToStringsForOSs.get(i));
            } catch (IOException e) {
                handleIO_Exception(e, metricsInstance.outputStreams.get(i), metricsInstance.instanceReportToStringsForOSs.get(i).instanceReportToString(instanceReport));
            }
        }
    }

    private static void outputAllInstanceReportToAllStreams() {
        Metrics metricsInstance = getMetricsInstance();
        for (int i = 0; i < metricsInstance.outputStreams.size(); i++) {
            OutputStream outputStream = metricsInstance.outputStreams.get(i);
            InstanceReportToString iToString = metricsInstance.instanceReportToStringsForOSs.get(i);
            InstanceReport currentInstanceReport = null;
            try {
                for (int j = 0; j < getMetricsInstance().reports.size(); j++) {
                    currentInstanceReport = getMetricsInstance().reports.get(j);
                    outputInstanceReportToStream(outputStream, currentInstanceReport, iToString);
                }
            } catch (IOException e) {
                handleIO_Exception(e, outputStream, iToString.instanceReportToString(currentInstanceReport));
            }
        }
    }

    private static void outputAllInstanceReportsToStream(OutputStream outputStream,
                                                         InstanceReportToString instanceReportToString) throws IOException {
        for (InstanceReport report :
                getMetricsInstance().reports) {
            outputInstanceReportToStream(outputStream, report, instanceReportToString);
        }
    }

    private static void handleIO_Exception(IOException e, OutputStream outputStream, String data){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();

        System.out.println("I/O error at time: " + dateFormat.format(date));
        System.out.println("          when writing: " + data);
        System.out.println("          to OutputStream: " + outputStream.toString());
        System.out.println("          printing stack trace:");
        e.printStackTrace();
    }

    private static void flushAllOutputStreams() {
        for (OutputStream os :
                getMetricsInstance().outputStreams) {
            try {
                os.flush();
            } catch (IOException e) {
                handleIO_Exception(e, os, "All data that had not yet been flushed.");
            }
        }
    }

    private static String[] createHeaderFromCurrentReports(){
        Set<String> headerSet = new HashSet<>();
        for (InstanceReport report :
                getMetricsInstance().reports) {
            headerSet.addAll(report.getAllFields());
        }
        return headerSet.toArray(String[]::new);
    }

    ////        OUTPUT API      ////

    /**
     * Writes the committed {@link InstanceReport} to all the OutputStreams in {@link #outputStreams}
     * @param instanceReport the committed {@link InstanceReport}
     * @throws IOException If an I/O error occurs.
     */
    static void commit(InstanceReport instanceReport) throws IOException {
        outputInstanceReportToAllStreams(instanceReport);
        flushAllOutputStreams();
    }

    /**
     * Exports all the {@link InstanceReport}s to the given output stream.
     * @param out the OutputStream to write to.
     * @param instanceReportToString the function with which to convert {@link InstanceReport}s to Strings.
     * @param headerToString the function with which to convert the {@link #header} to a String.
     * @throws IOException if an I/O error occurs
     */
    public static void exportToOutputStream(OutputStream out, InstanceReportToString instanceReportToString, HeaderToString headerToString) throws IOException {
        if(headerToString != null) {
            outputHeaderToStream(out, getMetricsInstance().header, headerToString);
            for (InstanceReport report :
                    getMetricsInstance().reports) {
                outputInstanceReportToStream(out, report, instanceReportToString);
            }
        }
        out.flush();
    }

    /**
     * Exports all the {@link InstanceReport}s to the given output stream. Uses default {@link InstanceReportToString}
     * and {@link HeaderToString} methods.
     * @param out the OutputStream to write to.
     * @throws IOException if an I/O error occurs
     */
    public static void exportToOutputStream(OutputStream out) throws IOException {
        exportToOutputStream(out, Metrics::instanceReportToStringCSV, Metrics::headerArrayToStringCSV);
        out.flush();
    }

    /**
     * Exports all the {@link InstanceReport}s to all the saved OutputStreams.
     * If an {@link IOException} occurs with one of the streams, error information will be printed, and the method will
     * move on to the other streams.
     */
    public static void exportAll() {
        outputAllInstanceReportToAllStreams();
        flushAllOutputStreams();
    }

    /**
     * Exports all the {@link InstanceReport}s to the given OutputStream, in CSV format.
     * @param outputStream the OutputStream to write to. Typically a {@link java.io.FileOutputStream}.
     * @param headerArray the desired header for the CSV output. Only {@link InstanceReport} fields which are contained
     *                    in this header will be written.
     * @throws IOException if an I/O error occurs
     */
    public static void exportCSV(OutputStream outputStream, String[] headerArray) throws IOException {
        outputHeaderToStream(outputStream, headerArray, Metrics::headerArrayToStringCSV);
        outputAllInstanceReportsToStream(outputStream,
                instanceReport -> getMetricsInstance().instanceReportToStringCSV(instanceReport, Metrics.CSV_DELIMITER, headerArray));
        outputStream.flush();
    }

    /**
     * Exports all the {@link InstanceReport}s to the given OutputStream, in CSV format. Creates a header that contains
     * all fields from all {@link InstanceReport}s.
     * @param outputStream the OutputStream to write to. Typically a {@link java.io.FileOutputStream}.
     * @throws IOException if an I/O error occurs
     */
    public static void exportCSV(OutputStream outputStream) throws IOException {
        exportCSV(outputStream, createHeaderFromCurrentReports());
    }

}
