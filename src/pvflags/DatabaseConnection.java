
package pvflags;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author RenalDevKCH
 */
public class DatabaseConnection
{
    String sep = Character.toString(SeparatorCharactor.createSeparatorCharactor());
    private final String textFilePath = sep + sep + sep + sep + "user.ad.ekhuft.nhs.uk"
            + sep + sep + "User"
            + sep + sep + "simon.weatherley"
            + sep + sep + "Documents" + sep + sep;
    private final String readOnlyTextFileName = "PRD04DatabaseConnection.txt";
    private final String liveDBtextFileName = "liveDBconnection.txt";
    private Connection readOnlyConn;
    private Connection liveConn;

    public Connection getReadOnlyConn()
    {
        return readOnlyConn;
    }

    public Connection getLiveConn()
    {
        return liveConn;
    }

    /**
     * opens a connection to the database
     *
     * @return database connection
     */
    public Connection openReadOnlyConnection()
    {
        try
        {
            String url = readConnectionTxtFile(textFilePath, readOnlyTextFileName);
            readOnlyConn = DriverManager.getConnection(url);
            System.out.println("read only connection open");
        }
        catch (SQLException ex)
        {
            System.out.println("Unable to open read only connection. " + ex);
        }
        return readOnlyConn;
    }
    
    public Connection openLiveConnection()
    {
        try
        {
            String url = readConnectionTxtFile(textFilePath, liveDBtextFileName);
            liveConn = DriverManager.getConnection(url);
            System.out.println("live DB connection open");
        }
        catch (SQLException ex)
        {
            System.out.println("Unable to open read only connection. " + ex);
        }
        return liveConn;
    }

    /**
     * closes the database connection. Throws SQL exception if the connection
     * can't be closed
     */
    public void closeReadOnlyConnection()
    {
        try
        {
            readOnlyConn.close();
            System.out.println("Read only Connection closed.");
        }
        catch (SQLException ex)
        {
            System.out.println("Unable to close read only connection. " + ex);
        }
    }
    public void closeLiveConnection()
    {
        try
        {
            liveConn.close();
            System.out.println("Live DB Connection closed.");
        }
        catch (SQLException ex)
        {
            System.out.println("Unable to close live connection. " + ex);
        }
    }

    /**
     * reads the text file containing connection parameters
     *
     * @param textFilePath
     * @param textFileName
     * @return
     */
    private String readConnectionTxtFile(String textFilePath, String textFileName)
    {
        String st = null;
        try
        {
            File file = new File(textFilePath, textFileName);
            st = FileUtils.readFileToString(file, Charset.forName("UTF-8"));
        }
        catch (IOException ex)
        {
            System.out.println("Unable to read connection text file" + ex);
        }
        return st;
    }

}
