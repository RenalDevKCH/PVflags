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
        //create new instance of class
        Data d = new Data();
        DatabaseConnection dbc = new DatabaseConnection();
        //get list of patients where flag "yes"
        d.getListOfPatientsFlaggedYes(dbc);
        //get list of patients who should be turned of
        d.getListOfPatientsWhoShouldBeRemoved(dbc);
        //compare to list that should be turned off
        //if nhs is present in both then turn off flag
    }

}
