
package pvflags;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author RenalDevKCH
 */
public class Data
{
    ArrayList<String> patientsWhereFlagIsYes = new ArrayList<>();
    ArrayList<String> patientsToBeRemoved = new ArrayList<>();

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
        System.out.println("Getting patients who should be removed... ");
        System.out.println("Reading excel file...");
        //read excel file
        //https://howtodoinjava.com/java/library/readingwriting-excel-files-in-java-poi-tutorial/
        //create workbook instance from an excel sheet
        //get to the desired sheet
        //increment row number
        //iterate over all cells in a row (might not need to do this as I'm only reading one column)
        //repeat steps 3 and 4 until all data is read.
        //get list from column
    }
}
