package Environment.Metrics;

import Environment.IO_Package.IO_Manager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.fail;

class MetricsTest {

    private static final String outputFile = IO_Manager.buildPath(new String[]{   IO_Manager.testResources_Directory, "S_MetricsTest.csv"});

    @BeforeEach
    void setUp(TestInfo testInfo) {
        System.out.printf("test started: %s: %s\n", testInfo.getTestClass().isPresent() ? testInfo.getTestClass().get() : "", testInfo.getDisplayName());
    }

    @BeforeEach
    void setUp() {
        try {
            boolean deleted = Files.deleteIfExists(Paths.get(outputFile));
            if(deleted) System.out.println("Deleted previous output file...\n");
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    void writeSomeInstanceReports(boolean commitThem) throws IOException {
        InstanceReport ir1 = Metrics.newInstanceReport();
        ir1.putStringValue("iName", "ir1");
        ir1.putIntegerValue("int1", 1);
        ir1.putFloatValue("fl1", (float)1.1);
        if(commitThem) {ir1.commit();}
        InstanceReport ir2 = Metrics.newInstanceReport();
        ir2.putStringValue("iName", "ir2");
        ir2.putIntegerValue("int1", 1);
        ir2.putFloatValue("fl1", (float)1.1);
        ir2.integerAddition("int1", 1); //int1 = 2
        ir2.integerAddition("int2", 1); //int2 = 1
        ir2.floatAddition("fl1", (float)1.1); //fl1 = 2.2
        ir2.floatAddition("fl2", (float)1.1); //fl2 = 1.1
        if(commitThem) {ir2.commit();}
        InstanceReport ir3 = Metrics.newInstanceReport();
        ir3.putStringValue("iName", "ir3");
        ir3.putIntegerValue("int1", 1);
        ir3.putFloatValue("fl1", (float)1.1);
        ir3.integerMultiplication("int1", 2); //int1 = 2
        ir3.integerMultiplication("int2", 1); //int2 = 0
        ir3.floatMultiplication("fl1", (float)1.1); //fl1 = 1.21
        ir3.floatMultiplication("fl2", (float)1.1); //fl2 = 0
        if(commitThem) {ir3.commit();}
        InstanceReport ir4 = Metrics.newInstanceReport();
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
            Metrics.addOutputStream(System.out, Metrics::instanceReportToHumanReadableString);
            writeSomeInstanceReports(true);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    void manualTest_FileOutput_FullHeader_withCommits(){
        try {
            Metrics.setHeader(new String[]{"iName", "int1", "fl1"});
            Metrics.addOutputStream(new FileOutputStream(outputFile));
            writeSomeInstanceReports(true);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    void manualTest_FileOutput_PartialHeader_withCommits(){
        try {
            Metrics.setHeader(new String[]{"iName", "fl1"});
            Metrics.addOutputStream(new FileOutputStream(outputFile));
            writeSomeInstanceReports(true);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    void exportAll() {
        try {
            Metrics.setHeader(new String[]{"iName", "int1", "fl1"});
            Metrics.addOutputStream(new FileOutputStream(outputFile), Metrics::instanceReportToHumanReadableString,
                    Metrics::headerArrayToStringCSV);
            Metrics.addOutputStream(System.out);
            writeSomeInstanceReports(false);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        Metrics.exportAll();
    }

    void exportCSV() {
        try {
            writeSomeInstanceReports(false);
            Metrics.exportCSV(new FileOutputStream(outputFile), new String[]{"iName", "int1", "fl1"});
            Metrics.exportCSV(System.out, new String[]{"iName", "int1", "fl1"});
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    // nicetohave also add automated test (using asserts)
}