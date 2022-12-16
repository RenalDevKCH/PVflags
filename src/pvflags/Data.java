
package pvflags;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author RenalDevKCH
 */
public class Data
{

    ArrayList<String> listOfNHSnumbers = new ArrayList<>();
    ArrayList<String> listOfPatientsFlaggedYes = new ArrayList<>();
    ArrayList<String> listOfUIDs = new ArrayList<>();
    ArrayList<String> testList = new ArrayList<>();
    ArrayList<Patient> patientList = new ArrayList<>();
    String sep = File.separator;
    String pathName = sep + sep + sep + sep + "user.ad.ekhuft.nhs.uk"
            + sep + "User"
            + sep + "simon.weatherley"
            + sep + "Documents"
            + sep + "RENAL"
            + sep + "PKB";
    String fileName = sep + "patients who should have flag turned off.xlsx";
//    String fileName = sep + "PV patients to be deleted test.xlsx";

    public ArrayList<Patient> getPatientList()
    {
        return patientList;
    }

    public void setPatientList(ArrayList<Patient> patientList)
    {
        this.patientList = patientList;
    }

    public ArrayList<String> getListOfPatientsFlaggedYesTEST()
    {
        testList.add("3");
        testList.add("4");
        testList.add("8");
        return testList;
    }

    public void getListOfNHSnumbers()
    {
        try
        {
            FileInputStream file = new FileInputStream(new File(pathName + fileName));
            try
            {
                //create workbook instance holding reference to excel file
                System.out.println("Getting list of patients... ");
                System.out.println("Reading excel file...");
                //create workbook instance holding reference to excel file
                XSSFWorkbook workbook = new XSSFWorkbook(file);
                //get relevant sheet from the workbook
                XSSFSheet sheet = workbook.getSheet("patients to turn flag off"); //this reads NHS numbers
                //iterate through each row
                Iterator<Row> rowIterator = sheet.iterator();
                String cellValue = "";
                while (rowIterator.hasNext())
                {
                    Row row = rowIterator.next();
                    //for each row, iterate all the columns
                    Iterator<Cell> cellIterator = row.cellIterator();
                    while (cellIterator.hasNext())
                    {
                        Cell cell = cellIterator.next();
//                        int r = row.getRowNum();
//                        int c = cell.getColumnIndex();
                        //check the cell type
                        switch (cell.getCellType())
                        {
                            case NUMERIC:
                                //convert to string
                                long excelData = (long) cell.getNumericCellValue();
                                cellValue = String.valueOf(excelData);
                                break;
                            case STRING:
                                cellValue = cell.getStringCellValue();
                                break;
                        }
                        //add each number to arraylist
                        if (!cellValue.equals("Identifier"))
                        {
                            Patient p = new Patient();
                            p.setNHSnumber(cellValue);
                            patientList.add(p);
                        }
                    }
                }
//                for (Patient p : patientList)
//                {
//                    System.out.println(p.getNHSnumber());
//                }
            }
            catch (IOException ex)
            {
                System.out.println("Unable to read from " + pathName + fileName
                        + "\nPlease ensure file exists." + ex);
                System.exit(0);
            }
        }
        catch (FileNotFoundException ex)
        {
            System.out.println("unable to find file " + ex);
            System.exit(0);
        }
    }

    public void addUIDtoPatient(DatabaseConnection dbc, Patient p)
    {
        System.out.println("getting list of UID\'s");
        try
        {
            dbc.openReadOnlyConnection();
            String UIDquery = "SELECT [UID] "
                    + "FROM [dbo].[Tbl_Demographics] "
                    + "WHERE [PS-NHS] = ? ";
            PreparedStatement prep = dbc.getReadOnlyConn().prepareStatement(UIDquery);
            for (Patient pSQL : patientList)
            {
                prep.setString(1, pSQL.getNHSnumberWithSpaces());
                ResultSet rs = prep.executeQuery();
                while (rs.next())
                {
                    pSQL.setUID(rs.getString("UID"));
                }
            }
            dbc.closeReadOnlyConnection();
        }
        catch (SQLException ex)
        {
            System.out.println("unable to read SQL to get UID list " + ex);
            System.exit(0);
        }
//        for (Patient pz : patientList)
//        {
//            System.out.println(pz.getNHSnumberWithSpaces() + ", " +  pz.getUID());
//        }
    }

    public void addSpacesToNHS()
    {
        System.out.println("adding spaces to the NHS numbers...");
        for (Patient p : patientList)
        {
            p.setNHSnumberWithSpaces(p.getNHSnumber().substring(0, 3) + " "
                    + p.getNHSnumber().substring(3, 6) + " "
                    + p.getNHSnumber().substring(6, 10));
        }
//        for (Patient p : patientList)
//        {
//            System.out.println(p.getNHSnumber().substring(0, 3) + " "
//                    + p.getNHSnumber().substring(3, 6) + " "
//                    + p.getNHSnumber().substring(6, 10));
//        }
    }

//    private String createNHSlistForSQL()
//    {
//        StringBuilder sb = new StringBuilder();
//        for (Patient p : patientList)
//        {
//            sb.append(p.getNHSnumberWithSpaces()).append(",");
//        }
//        String str = sb.toString();
//        str = str.substring(0, str.length() - 1);
//        return str;
//    }
    public void turnOffFlag(DatabaseConnection dbc)
    {
        dbc.openLiveConnection();
        //SQL query needs to use list in the where clause

        //need to set "authority date"  and "last updated by" and where should check for radar comment as
        //a double check. In theory, they are all filtered out already
        String flagQuery = "BEGIN TRANSACTION "
                + "UPDATE [dbo].[tbl_PatientView_Release] "
                + "SET [TakingPart] = 0 "
                + "WHERE [fkPatient] IN (" + createUIDlistForSQL() + ")"; //needs to be UID !!
        try
        {
            dbc.getLiveConn().setAutoCommit(false);
            PreparedStatement prep = dbc.getLiveConn().prepareStatement(flagQuery);
            prep.executeUpdate();
            if (prep.getUpdateCount() == patientList.size()) //list size is 719
            {
//                dbc.getLiveConn().commit();
                dbc.getLiveConn().rollback();
                System.out.println("update count = " + prep.getUpdateCount());
//                System.out.println("list size = " + getPatientsToTurnFlagOffList().size());

            }
            else
            {
                dbc.getLiveConn().rollback();
            }
        }
        catch (SQLException ex)
        {
            System.out.println("unable to read SQL to turn flag off. " + ex);
            System.exit(0);
        }
        dbc.closeLiveConnection();
    }

    public String createUIDlistForSQL()
    {
        StringBuilder sb = new StringBuilder();
//        int x = 0;
        for (Patient p : patientList)
        {
//            if (x < 21)
//            {
            sb.append(p.getUID()).append(",");
//            }
//            System.out.println(p.getNHSnumberWithSpaces());
//            x += 1;
        }
        String str = sb.toString();
        str = str.substring(0, str.length() - 1);
        System.out.println(str);
        return str;
    }

}

//    }
//    private String iterateFlagOffList()
//    {
//        StringBuilder sb = new StringBuilder();
//        for (String item : getPatientsToTurnFlagOffList())
//        {
//            sb.append(item).append(",");
//        }
//        String str = sb.toString();
//        str = str.substring(0, str.length() - 1);
//        return str;
//    }
//    public void getListOfNHSnumbers(DatabaseConnection dbc)
//    {
//        FileInputStream file = null;
//        try
//        {
//            System.out.println("Getting patients who should be removed... ");
//            System.out.println("Reading excel file...");
//            //read excel file
//            //https://howtodoinjava.com/java/library/readingwriting-excel-files-in-java-poi-tutorial/
//            //create workbook instance from an excel sheet
//            file = new FileInputStream(new File(pathname + fileName));
//            XSSFWorkbook patientSpreadsheet = new XSSFWorkbook(file); //holds reference to xlsx file
//            //get to the desired sheet
//            XSSFSheet sheet = patientSpreadsheet.getSheet(" flag should be turned off");
//            //iterate each row
//            Iterator<Row> rowIterator = sheet.iterator();
//            String cellValue = "";
//
//            while (rowIterator.hasNext())
//            {
//                Row row = rowIterator.next();
//                //for each row, iterate through all the columns
//                Iterator<Cell> cellIterator = row.cellIterator();
//                while (cellIterator.hasNext())
//                {
//                    Cell cell = cellIterator.next();
//                    int r = row.getRowNum();
//                    int c = cell.getColumnIndex();
//                    //check the cell type
//                    switch (cell.getCellType())
//                    {
//                        case NUMERIC:
//                            //convert to string
//                            long excelData = (long) cell.getNumericCellValue();
//                            cellValue = String.valueOf(excelData);
//                            break;
//                        case STRING:
//                            cellValue = cell.getStringCellValue();
//                            break;
//                    }
//                    //add each item to the arraylist
//                    if (!cellValue.equals("Identifier"))
//                    {
//                        listOfNHSnumbers.add(cellValue);
//                    }
//                }
//            }
//        }
//        catch (FileNotFoundException ex)
//        {
//            System.out.println("Unable to find file at " + pathname + "\n" + ex);
//            System.exit(0);
//        }
//        catch (IOException ex)
//        {
//            System.out.println("IO Exception. Unable to access file at " + pathname + "\n" + ex);
//            System.exit(0);
//        }
//        finally
//        {
//            try
//            {
//                file.close();
//            }
//            catch (IOException ex)
//            {
//                System.out.println("IO Exception. Unable to access file at " + pathname + "\n" + ex);
//                System.exit(0);
//            }
//        }
//        er.readExcelSheet(getListOfNHSnumbers());
//        System.out.println(getListOfNHSnumbers());
//        System.exit(0);
//    }
//    public void getUIDlist(DatabaseConnection dbc)
//    {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//}
//    public void getListOfPatientsFlaggedYes(DatabaseConnection dbc)
//    {
//        System.out.println("Getting patients flagged \"Yes\"... ");
//        String queryFlaggedYes = "SELECT "
//                + "[Pat_ID], "
//                + "DG.[UID], "
//                + "SUBSTRING([PS-NHS],1,3) + SUBSTRING([PS-NHS],5,3) + SUBSTRING([PS-NHS],9,4) AS 'NHS', "
//                + "[ID-FNAM], "
//                + "[ID-NAM], "
//                + "[ID-DOB], "
//                + "[ID-STS], "
//                + "("
//                + "SELECT TOP 1 [TakingPart] "
//                + "FROM [dbo].[tbl_PatientView_Release] "
//                + "WHERE [fkPatient] = DG.[UID] "
//                + "ORDER BY [AuthDate] DESC"
//                + ") AS 'PV flag' "
//                + "INTO #flag "
//                + "FROM [dbo].[Tbl_Demographics] AS DG;"
//                + " "
//                + "SELECT #flag.[UID] AS 'UID'FROM #flag "
//                + "WHERE #flag.[PV flag] = -1 "
//                + "ORDER BY #flag.[NHS] ASC;"
//                + " "
//                + "DROP TABLE #flag";
//
//        try
//        {
//            dbc.openReadOnlyConnection();
//            PreparedStatement prep = dbc.getReadOnlyConn().prepareStatement(queryFlaggedYes);
//            ResultSet rs = prep.executeQuery();
//            while (rs.next())
//            {
//                patientsWhereFlagIsYesList.add(rs.getString("UID"));
//            }
//            dbc.closeReadOnlyConnection();
//        }
//        catch (SQLException ex)
//        {
//            System.out.println("unable to read SQL to get list of patients flagged \"yes\". " + ex);
//            System.exit(0);
//        }
//    }
//    public void getListOfPatientsWhoShouldBeRemoved(DatabaseConnection dbc)
//    {
//        FileInputStream file = null;
//        try
//        {
//            System.out.println("Getting patients who should be removed... ");
//            System.out.println("Reading excel file...");
//            //read excel file
//            //https://howtodoinjava.com/java/library/readingwriting-excel-files-in-java-poi-tutorial/
//            //create workbook instance from an excel sheet
//            file = new FileInputStream(new File(pathname + fileName));
//            XSSFWorkbook patientSpreadsheet = new XSSFWorkbook(file); //holds reference to xlsx file
//            //get to the desired sheet
//            XSSFSheet sheet = patientSpreadsheet.getSheet(" flag should be turned off");
//            //iterate each row
//            Iterator<Row> rowIterator = sheet.iterator();
//            String cellValue = "";
//
//            while (rowIterator.hasNext())
//            {
//                Row row = rowIterator.next();
//                //for each row, iterate through all the columns
//                Iterator<Cell> cellIterator = row.cellIterator();
//                while (cellIterator.hasNext())
//                {
//                    Cell cell = cellIterator.next();
//                    int r = row.getRowNum();
//                    int c = cell.getColumnIndex();
//                    //check the cell type
//                    switch (cell.getCellType())
//                    {
//                        case NUMERIC:
//                            //convert to string
//                            long excelData = (long) cell.getNumericCellValue();
//                            cellValue = String.valueOf(excelData);
//                            break;
//                        case STRING:
//                            cellValue = cell.getStringCellValue();
//                            break;
//                    }
//                    //add each item to the arraylist
//                    if (!cellValue.equals("Identifier"))
//                    {
//                        listOfNHSnumbers.add(cellValue);
//                    }
//                }
//            }
//        }
//        catch (FileNotFoundException ex)
//        {
//            System.out.println("Unable to find file at " + pathname + "\n" + ex);
//            System.exit(0);
//        }
//        catch (IOException ex)
//        {
//            System.out.println("IO Exception. Unable to access file at " + pathname + "\n" + ex);
//            System.exit(0);
//        }
//        finally
//        {
//            try
//            {
//                file.close();
//            }
//            catch (IOException ex)
//            {
//                System.out.println("IO Exception. Unable to access file at " + pathname + "\n" + ex);
//                System.exit(0);
//            }
//        }
//    }
//}
//
//    public ArrayList<String> getPatientsWhereFlagIsYesList()
//    {
//        return patientsWhereFlagIsYesList;
//    }
//
//    public ArrayList<String> getPatientsToBeRemovedList()
//    {
//        return patientsToBeRemovedList;
//    }
//
//    public ArrayList<String> getPatientsToTurnFlagOffList()
//    {
//        return patientsToTurnFlagOffList;
//    }
//    public void compareLists(ArrayList<String> flaggedYes, ArrayList<String> toBeRemoved)
//    {
//        System.out.println("comparing lists...");
//        for (String item : flaggedYes)
//        {
//            if (toBeRemoved.contains(item))
//            {
//                patientsToTurnFlagOffList.add(item);
//            }
//        }
//        System.out.println("list size = " + patientsToTurnFlagOffList);
//        System.out.println("list size = " + patientsToTurnFlagOffList.size());
//    }
//    public void turnOffFlag()
//    {
//        //open live connection
//        //write query
//        
//    }
//    public void getUIDlist(DatabaseConnection dbc)
//    {
//        try
//        {
//            dbc.openReadOnlyConnection();            
//            String uidQuery = "";
//            PreparedStatement prep = dbc.getReadOnlyConn().prepareStatement(uidQuery);
//            ResultSet rs = prep.executeQuery();
//            while (rs.next())
//            {
//                
//            }
//        }
//        catch (SQLException ex)
//        {
//            System.out.println("unable to read sql for UID list " + ex);
//            System.exit(0);
//        }
//    }
