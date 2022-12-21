
package pvflags;

/**
 *
 * @author RenalDevKCH
 */
public class Patient
{
    String NHSnumber;
    String NHSnumberWithSpaces;
    int UID;

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

    public int getUID()
    {
        return UID;
    }

    public void setUID(int UID)
    {
        this.UID = UID;
    }
    
}
