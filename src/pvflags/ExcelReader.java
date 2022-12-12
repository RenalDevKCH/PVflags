/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pvflags;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
//import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author RenalDevKCH
 */
public class ExcelReader
{
    String filePath = "";
    String fileName = "";

    public void readExcelSheet()
    {
        try
        {
            //create workbook instance holding reference to excel file
            try (FileInputStream file = new FileInputStream(new File(filePath + fileName)))
            {
                //create workbook instance holding reference to excel file
                XSSFWorkbook workbook = new XSSFWorkbook(file);
                //get relevant sheet from the workbook
                XSSFSheet sheet = workbook.getSheetAt(0);
                //iterate through each row
                Iterator<Row> rowIterator = sheet.iterator();
                while (rowIterator.hasNext())
                {
                    Row row = rowIterator.next();
                    //for each row, iterate all the columns
                    Iterator<Cell> cellIterator = row.cellIterator();
                    while (cellIterator.hasNext())
                    {
                        Cell cell = cellIterator.next();
                        
                        //check the cell type
                        switch (cell.getCellType())
                        {
                            case NUMERIC:
                                //do stuff
                                break;
                            case STRING:
                                //do stuff
                                break;
                            case FORMULA:
                                //Do stuff
                        }
                    }
//                System.out.println("");
                }
            }
        }
        catch (IOException ex)
        {
            System.out.println("Unable to read from " + filePath + fileName
                    + "\nPlease ensure file exists." + ex);
        }
    }
}