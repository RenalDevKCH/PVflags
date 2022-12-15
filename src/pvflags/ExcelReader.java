///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package pvflags;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFSheet;
////import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//
///**
// *
// * @author RenalDevKCH
// */
//public class ExcelReader
//{
//    String sep = File.separator;
//    String pathName = sep + sep + sep + sep + "user.ad.ekhuft.nhs.uk"
//            + sep + "User"
//            + sep + "simon.weatherley"
//            + sep + "Documents"
//            + sep + "RENAL"
//            + sep + "PKB";
//    String fileName = sep + "patients who should have flag turned off.xlsx";
//    Data d = new Data();
////    String fileName = sep + "PV patients to be deleted test.xlsx";
//
//    public void readExcelSheet()
//    {
//        ArrayList<String> listOfNHSnumbers = new ArrayList<>();
//        FileInputStream file = null;
//        try
//        {
//            file = new FileInputStream(new File(pathName + fileName));
//            try
//            {
//                //create workbook instance holding reference to excel file
//
//                {
//                    System.out.println("Getting patients who should be removed... ");
//                    System.out.println("Reading excel file...");
//                    //create workbook instance holding reference to excel file
//                    XSSFWorkbook workbook = new XSSFWorkbook(file);
//                    //get relevant sheet from the workbook
//                    XSSFSheet sheet = workbook.getSheet("patients to turn flag off"); //this reads NHS numbers
//                    //iterate through each row
//                    Iterator<Row> rowIterator = sheet.iterator();
//                    String cellValue = "";
//                    while (rowIterator.hasNext())
//                    {
//                        Row row = rowIterator.next();
//                        //for each row, iterate all the columns
//                        Iterator<Cell> cellIterator = row.cellIterator();
//                        while (cellIterator.hasNext())
//                        {
//                            Cell cell = cellIterator.next();
//                            int r = row.getRowNum();
//                            int c = cell.getColumnIndex();
//                            //check the cell type
//                            switch (cell.getCellType())
//                            {
//                                case NUMERIC:
//                                    //convert to string
//                                    long excelData = (long) cell.getNumericCellValue();
//                                    cellValue = String.valueOf(excelData);
//                                    break;
//                                case STRING:
//                                    cellValue = cell.getStringCellValue();
//                                    break;
//                            }
//                            //add each number to arraylist
//                            if (!cellValue.equals("Identifier"))
//                            {
//                                listOfNHSnumbers.add(cellValue);
//                            }
//                        }
//                    }
////                    System.out.println(d.getListOfNHSnumbers());
//                }
//            }
//            catch (IOException ex)
//            {
//                System.out.println("Unable to read from " + pathName + fileName
//                        + "\nPlease ensure file exists." + ex);
//                System.exit(0);
//            }
//        }
//        catch (FileNotFoundException ex)
//        {
//            System.out.println("unable to find file " + ex);
//            System.exit(0);
//        }
//        finally
//        {
//            try
//            {
//                file.close();
//            }
//            catch (IOException ex)
//            {
//                System.out.println("Unable to read from " + pathName + fileName
//                        + "\nPlease ensure file exists." + ex);
//                System.exit(0);
//            }
//        }
//    }
//}
