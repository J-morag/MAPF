package Environment.Metrics;

import Environment.IO_Package.IO_Manager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

class S_MetricsTest {

    private static final String outputFile = IO_Manager.buildPath(new String[]{   IO_Manager.testResources_Directory, "S_MetricsTest.csv"});

    @BeforeEach
    void setUp() {
        try {
            boolean deleted = Files.deleteIfExists(Paths.get(outputFile));
            if(deleted) System.out.println("Deleted previous output file...\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void writeSomeInstanceReports(boolean commitThem) throws IOException {
        InstanceReport ir1 = S_Metrics.newInstanceReport();
        ir1.putStringValue("iName", "ir1");
        ir1.putIntegerValue("int1", 1);
        ir1.putFloatValue("fl1", (float)1.1);
        if(commitThem) {ir1.commit();}
        InstanceReport ir2 = S_Metrics.newInstanceReport();
        ir2.putStringValue("iName", "ir2");
        ir2.putIntegerValue("int1", 1);
        ir2.putFloatValue("fl1", (float)1.1);
        ir2.integerAddition("int1", 1); //int1 = 2
        ir2.integerAddition("int2", 1); //int2 = 1
        ir2.floatAddition("fl1", (float)1.1); //fl1 = 2.2
        ir2.floatAddition("fl2", (float)1.1); //fl2 = 1.1
        if(commitThem) {ir2.commit();}
        InstanceReport ir3 = S_Metrics.newInstanceReport();
        ir3.putStringValue("iName", "ir3");
        ir3.putIntegerValue("int1", 1);
        ir3.putFloatValue("fl1", (float)1.1);
        ir3.integerMultiplication("int1", 2); //int1 = 2
        ir3.integerMultiplication("int2", 1); //int2 = 0
        ir3.floatMultiplication("fl1", (float)1.1); //fl1 = 1.21
        ir3.floatMultiplication("fl2", (float)1.1); //fl2 = 0
        if(commitThem) {ir3.commit();}
        InstanceReport ir4 = S_Metrics.newInstanceReport();
        ir4.putStringValue("iName", "ir4");
        ir4.putStringValue("iName", "new name"); // iName = new name
        ir4.putIntegerValue("int1", 1);
        ir4.putIntegerValue("int1", 2); // int1 = 2
        ir4.putFloatValue("fl1", (float)1.1);
        ir4.putFloatValue("fl1", (float)3.3); // fl1 = 3.3
        if(commitThem) {ir4.commit();}
        if(commitThem) {ir4.commit();} //should have no effect
    }

    @Test
    void manualTest_ConsoleOutput_withCommits(){
        try {
            S_Metrics.addOutputStream(System.out, S_Metrics::instanceReportToHumanReadableString);
            writeSomeInstanceReports(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void manualTest_FileOutput_FullHeader_withCommits(){
        try {
            S_Metrics.setHeader(new String[]{"iName", "int1", "fl1"});
            S_Metrics.addOutputStream(new FileOutputStream(outputFile));
            writeSomeInstanceReports(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void manualTest_FileOutput_PartialHeader_withCommits(){
        try {
            S_Metrics.setHeader(new String[]{"iName", "fl1"});
            S_Metrics.addOutputStream(new FileOutputStream(outputFile));
            writeSomeInstanceReports(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void exportAll() {
        try {
            S_Metrics.setHeader(new String[]{"iName", "int1", "fl1"});
            S_Metrics.addOutputStream(new FileOutputStream(outputFile), S_Metrics::instanceReportToHumanReadableString,
                    S_Metrics::headerArrayToStringCSV);
            S_Metrics.addOutputStream(System.out);
            writeSomeInstanceReports(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        S_Metrics.exportAll();
    }

    void exportCSV() {
        try {
            writeSomeInstanceReports(false);
            S_Metrics.exportCSV(new FileOutputStream(outputFile), new String[]{"iName", "int1", "fl1"});
            S_Metrics.exportCSV(System.out, new String[]{"iName", "int1", "fl1"});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void addSolutionExportOutputStream() {
        try{
            S_Metrics.addSolutionExportOutputStream("D:/");
            S_Metrics.addSolutionExportOutputStream("doesNotExists"); // still works, creates new directory
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // nicetohave also add automated test (using asserts)
}