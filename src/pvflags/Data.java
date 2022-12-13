
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
    ArrayList<String> patientsWhereFlagIsYes = new ArrayList<>();
    ArrayList<String> patientsToBeRemoved = new ArrayList<>();
    ArrayList<String> patientsToTurnFlagOff = new ArrayList<>();
    String sep = File.separator;
    String pathname = sep + sep + sep + sep + "user.ad.ekhuft.nhs.uk"
            + sep + "User"
            + sep + "simon.weatherley"
            + sep + "Documents"
            + sep + "RENAL"
            + sep + "PKB";
    String fileName = sep + "PV patients to be deleted.xlsx";

    public void getListOfPatientsFlaggedYes(DatabaseConnection dbc)
    {
        System.out.println("Getting patients flagged \"Yes\"... ");
        String queryFlaggedYes = "SELECT "
                + "[Pat_ID], "
                + "SUBSTRING([PS-NHS],1,3) + SUBSTRING([PS-NHS],5,3) + SUBSTRING([PS-NHS],9,4) AS 'NHS', "
                + "[ID-FNAM], "
                + "[ID-NAM], "
                + "[ID-DOB], "
                + "[ID-STS], "
                + "("
                + "SELECT TOP 1 [TakingPart] "
                + "FROM [dbo].[tbl_PatientView_Release] "
                + "WHERE [fkPatient] = DG.[UID] "
                + "ORDER BY [AuthDate] DESC"
                + ") AS 'PV flag' "
                + "INTO #flag "
                + "FROM [dbo].[Tbl_Demographics] AS DG;"
                + " "
                + "SELECT #flag.[NHS] AS 'NHS'FROM #flag "
                + "WHERE #flag.[PV flag] = -1 "
                + "ORDER BY #flag.[NHS] ASC;"
                + " "
                + "DROP TABLE #flag";

        try
        {
            dbc.openReadOnlyConnection();
            PreparedStatement prep = dbc.getReadOnlyConn().prepareStatement(queryFlaggedYes);
            ResultSet rs = prep.executeQuery();
            while (rs.next())
            {
                patientsWhereFlagIsYes.add(rs.getString("NHS"));
            }
            dbc.closeReadOnlyConnection();
        }
        catch (SQLException ex)
        {
            System.out.println("unable to read SQL " + ex);
            System.exit(0);
        }
//        for (String s : patientsWhereFlagIsYes)
//        {
//            System.out.println(s);
//        }
    }

    void getListOfPatientsWhoShouldBeRemoved(DatabaseConnection dbc)
    {
        FileInputStream file = null;
        try
        {
            System.out.println("Getting patients who should be removed... ");
            System.out.println("Reading excel file...");
            //read excel file
            //https://howtodoinjava.com/java/library/readingwriting-excel-files-in-java-poi-tutorial/
            //create workbook instance from an excel sheet
            file = new FileInputStream(new File(pathname + fileName));
            XSSFWorkbook patientSpreadsheet = new XSSFWorkbook(file); //holds reference to xlsx file
            //get to the desired sheet
            XSSFSheet sheet = patientSpreadsheet.getSheet(" flag should be turned off");
            //iterate each row
            Iterator<Row> rowIterator = sheet.iterator();
            String cellValue = "";
            
            while (rowIterator.hasNext())
            {
                Row row = rowIterator.next();
                //for each row, iterate through all the columns
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext())
                {
                    Cell cell = cellIterator.next();
                    int r = row.getRowNum();
                    int c = cell.getColumnIndex();
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
                    //add each item to the arraylist
                    if (!cellValue.equals("Identifier"))
                    {
                        patientsToBeRemoved.add(cellValue);
                    }
                    
                }
            }
        }
        catch (FileNotFoundException ex)
        {
            System.out.println("Unable to find file at " + pathname + "\n" + ex);
            System.exit(0);
        }
        catch (IOException ex)
        {
            System.out.println("IO Exception. Unable to access file at " + pathname + "\n" + ex);
            System.exit(0);
        }
        finally
        {
            try
            {
                file.close();
            }
            catch (IOException ex)
            {
                System.out.println("IO Exception. Unable to access file at " + pathname + "\n" + ex);
                System.exit(0);
            }
        }
        compareBothLists(patientsToBeRemoved, patientsWhereFlagIsYes);
    }

    private void compareBothLists(ArrayList<String> patientsToBeRemoved, ArrayList<String> patientsWhereFlagIsYes)
    {
        for (String item : patientsToBeRemoved)
        {
            if (patientsWhereFlagIsYes.contains(item))
            {
                patientsToTurnFlagOff.add(item);
            }
        }
    }
}
