
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
//    String fileName = sep + "patients who should have flag turned off test file.xlsx";

    public ArrayList<Patient> getPatientList()
    {
        return patientList;
    }

    public void setPatientList(ArrayList<Patient> patientList)
    {
        this.patientList = patientList;
    }

//    public ArrayList<String> getListOfPatientsFlaggedYesTEST()
//    {
//        testList.add("3");
//        testList.add("4");
//        testList.add("8");
//        return testList;
//    }

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
                    pSQL.setUID(rs.getInt("UID"));
                }
            }
            dbc.closeReadOnlyConnection();
        }
        catch (SQLException ex)
        {
            System.out.println("unable to read SQL to get UID list " + ex);
            System.exit(0);
        }
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
    }
    public void turnOffFlag(DatabaseConnection dbc)
    {
        dbc.openLiveConnection();
//        dbc.openReadOnlyConnection();
        /*
        
        take first patient and check manually on pkb to check they haven't logged on and
        then in RP make them inactive as of today and then db table
        DO INSERT NOT UPDATE AND ADD COMMENT WITH TODAYS DATE AND USER ETC ETC

         */
        String flagQuery = "INSERT INTO [RENALPLUS].[dbo].[tbl_PatientView_Release] "
                + "([fkPatient],[TakingPart],[AuthDate],[Comments],[UpdatedBy]) "
                + getSQLinsertValues(); //stringBuilder
        try
        {
            dbc.getLiveConn().setAutoCommit(false);
//            PreparedStatement prep = dbc.getReadOnlyConn().prepareStatement(flagQuery);
            PreparedStatement prep = dbc.getLiveConn().prepareStatement(flagQuery);
            prep.executeUpdate();
            if (prep.getUpdateCount() == patientList.size())
            {
//                commit(dbc);
                rollback(dbc);
            }
            else
            {
                rollback(dbc);
            }
        }
        catch (SQLException ex)
        {
            System.out.println("unable to read SQL to turn flag off. " + ex);
            System.exit(0);
        }
        finally
        {
            dbc.closeLiveConnection();
        }
        
    }

//    public String createUIDlistForSQL()
//    {
//        StringBuilder sb = new StringBuilder();
//        int UIDcount = 0;
//        for (Patient p : patientList)
//        {
//            sb.append(p.getUID()).append(",");
//            UIDcount += 1;
//        }
//        System.out.println("UID count = " + UIDcount);
//        String str = sb.toString();
//        str = str.substring(0, str.length() - 1);
//        return str;
//    }

    private void commit(DatabaseConnection dbc) throws SQLException
    {
        dbc.getLiveConn().commit();
        System.out.println("update successful");
    }

    private void rollback(DatabaseConnection dbc) throws SQLException
    {
        dbc.getLiveConn().rollback();
        System.out.println("changes have been rolled back! Update unsuccessful.");
    }

    private String getSQLinsertValues()
    {
        String str;
        StringBuilder sb = new StringBuilder();
        /*
        VALUES
        (fkpatient1 (int) ,TakingPart (int), authDate (dateTime), comments (nVarChar), updatedBy (int)),
        (fkpatient2 (int) ,TakingPart (int), authDate (dateTime), comments (nVarChar), updatedBy (int)),
        (fkpatient3 (int) ,TakingPart (int), authDate (dateTime), comments (nVarChar), updatedBy (int));
         */
        sb.append("VALUES ");
        for (Patient x : patientList)
        {
            sb.append("(")
                    .append(x.getUID()).append(", ") //fkPatient (int)
                    .append(0).append(", ") //Taking part (int)
                    .append("GETDATE()").append(", ") //current time (authDate) (dateTime)
                    .append("\'").append("inactive patient").append("\', ") // comment
                    .append(2317) //user id (int)
                    .append("),");
        }
        str = sb.toString();
        return str.substring(0, str.length() - 1);
    }

    public void checkPatientList()
    {
        for (Patient p : patientList)
        {
            System.out.println(p.getNHSnumber() + ", " + p.getNHSnumberWithSpaces() + ", " + p.getUID());
        }
    }

    void getNumberOfPatients()
    {
        System.out.println("number of patients = " + patientList.size());
    }

}