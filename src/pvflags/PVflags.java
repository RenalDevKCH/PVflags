/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pvflags;

/**
 *
 * @author RenalDevKCH
 */
public class PVflags
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        Data d = new Data();
        DatabaseConnection dbc = new DatabaseConnection();
        Patient p = new Patient();
        //get list of patients who should have flag turned of (this will be nhs numbers)
        d.getListOfNHSnumbers();
        //convert the nhs numbers so that the database can use them
        d.addSpacesToNHS();
        //get UID for each patient
        d.getUIDlist(dbc, p);
        //turn off the flag using the UID
        d.turnOffFlag(dbc);
    }

}
