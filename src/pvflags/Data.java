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
    public void getListOfNHSnumbersWhereDataIsSent(DatabaseConnection dbc)
    {
        System.out.println("getting data...");
        String query = "SELECT "
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
                + ""
                + "SELECT #flag.[NHS] AS 'NHS'FROM #flag WHERE #flag.[PV flag] = -1;"
                + ""
                + "DROP TABLE #flag";
        ArrayList<String> NHSnumbersWhereDataIsSent = new ArrayList<>();
        try
        {
            dbc.openReadOnlyConnection();
            PreparedStatement prep = dbc.getReadOnlyConn().prepareStatement(query);
            ResultSet rs = prep.executeQuery();
            while (rs.next())
            {
                NHSnumbersWhereDataIsSent.add(rs.getString("NHS"));
            }
            dbc.closeReadOnlyConnection();
        }
        catch (SQLException ex)
        {
            System.out.println("unable to read SQL " + ex);
            System.exit(0);
        }
        for (String s : NHSnumbersWhereDataIsSent)
        {
            System.out.println(s);
        }
    }
}
