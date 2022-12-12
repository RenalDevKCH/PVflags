
package pvflags;

import java.time.LocalDate;

/**
 *
 * @author RenalDevKCH
 */
public class Patient
{
    private String NHSnumber;
    private String hospitalNumber;
    private String title;
    private String firstName;
    private String surname;
    private LocalDate DOB;
    private String gender;
    private int age; //derived from DOB
    private String modality;
    private String DXunit;

    public LocalDate getDOB()
    {
        return DOB;
    }

    public int getAge()
    {
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        int currentMonth = today.getMonthValue();
        int currentDay = today.getDayOfMonth();
        if (getDOB().getMonthValue() < currentMonth) //birthday < today
        {
            this.age = currentYear - getDOB().getYear();
        }
        else
        {
            if (getDOB().getMonthValue() == currentMonth)
            {
                if (getDOB().getDayOfMonth() < currentDay) // birthday < today
                {
                    this.age = currentYear - getDOB().getYear();
                }
                else
                {
                    if (getDOB().getDayOfMonth() == currentDay)//birthday == today
                    {
                        this.age = currentYear - getDOB().getYear();
                    }
                    else //birthday > today
                    {
                        this.age = currentYear - getDOB().getYear() - 1;
                    }
                }
            }
            else //birthday > today
            {
                this.age = currentYear - getDOB().getYear() - 1;
            }
        }
        return age;
    }
}
