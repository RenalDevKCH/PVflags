
package pvflags;

/**
 *
 * @author RenalDevKCH
 */
public class Patient
{
    String NHSnumber;
    String NHSnumberWithSpaces;
    String UID;

    public String getNHSnumberWithSpaces()
    {
        return NHSnumberWithSpaces;
    }

    public void setNHSnumberWithSpaces(String NHSnumberWithSpaces)
    {
        this.NHSnumberWithSpaces = NHSnumberWithSpaces;
    }

    public String getNHSnumber()
    {
        return NHSnumber;
    }

    public void setNHSnumber(String NHSnumber)
    {
        this.NHSnumber = NHSnumber;
    }

    public String getUID()
    {
        return UID;
    }

    public void setUID(String UID)
    {
        this.UID = UID;
    }
    
}
