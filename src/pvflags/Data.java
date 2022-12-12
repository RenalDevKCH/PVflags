package pvflags;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author RenalDevKCH
 */
public class Data
{
    public void getDemographics(DatabaseConnection dbc)
    {
        dbc = new DatabaseConnection();
        System.out.println("getting data...");
        String query = "";
        try
        {
            PreparedStatement prep = dbc.getReadOnlyConn().prepareStatement(query);
//            prep.setString(1, eachItem);
            ResultSet rs = prep.executeQuery();
            while (rs.next())
            {
                //do stuff
            }
        }
        catch (SQLException ex)
        {
            System.out.println("unable to read SQL " + ex);
            System.exit(0);
        }
    }
}
