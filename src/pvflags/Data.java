
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
    String flagQuery; //stringBuilder
    int UIDcount;

    public int getUIDcount()
    {
        return UIDcount;
    }

    public ArrayList<Patient> getPatientList()
    {
        return patientList;
    }

    public void setPatientList(ArrayList<Patient> patientList)
    {
        this.patientList = patientList;
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
                if (pSQL.getNHSnumberWithSpaces() != null)
                {
                    prep.setString(1, pSQL.getNHSnumberWithSpaces());
                    ResultSet rs = prep.executeQuery();
                    while (rs.next())
                    {
                        pSQL.setUID(rs.getInt("UID"));
                    }
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
        String spacedRegex = "^\\d{3}\\s\\d{3}\\s\\d{4}$";
        String nonSpacedRegex = "^\\d{10}$";
        for (Patient p : patientList)
        {
            String NHSnum = p.getNHSnumber();
            if (NHSnum.matches(spacedRegex))
            {
                p.setNHSnumberWithSpaces(NHSnum);
            }
            else
            {
                if (NHSnum.matches(nonSpacedRegex))
                {
                    p.setNHSnumberWithSpaces(NHSnum.substring(0, 3) + " "
                            + NHSnum.substring(3, 6) + " "
                            + NHSnum.substring(6, 10));
                }
                else
                {
                    System.out.println(NHSnum + " is invalid");
                }
            }
        }
    }

    public void turnOffFlag(DatabaseConnection dbc)
    {
        flagQuery = "INSERT INTO [RENALPLUS].[dbo].[tbl_PatientView_Release] "
                + "([fkPatient],[TakingPart],[AuthDate],[Comments],[UpdatedBy]) "
                + getSQLinsertValues();
        printSQLstring();
        System.exit(0);
        dbc.openLiveConnection();
//        dbc.openReadOnlyConnection();
        /*
        take first patient and check manually on pkb to check they haven't logged on and
        then in RP make them inactive as of today and then db table
        DO INSERT NOT UPDATE AND ADD COMMENT WITH TODAYS DATE AND USER ETC ETC
         */
        try
        {
            dbc.getLiveConn().setAutoCommit(false);
//            PreparedStatement prep = dbc.getReadOnlyConn().prepareStatement(flagQuery);
            PreparedStatement prep = dbc.getLiveConn().prepareStatement(flagQuery);
            prep.executeUpdate();
            if (prep.getUpdateCount() == getUIDcount()) //THIS WILL ALWAYS BE TRUE ??
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
        UIDcount = 0;
        for (Patient x : patientList)
        {
            if (x.getUID() != 0)
            {
                sb.append("(")
                        .append(x.getUID()).append(", ") //fkPatient (int)
                        .append(0).append(", ") //Taking part (int)
                        .append("GETDATE()").append(", ") //current time (authDate) (dateTime)
                        .append("\'").append("inactive patient").append("\', ") // comment
                        .append(2317) //user id (int)
                        .append("),");
                UIDcount += 1;
            }
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

    public void getNumberOfPatients()
    {
        System.out.println("number of patients = " + patientList.size());
    }

    public void printSQLstring()
    {
        System.out.println(flagQuery);
    }
}
