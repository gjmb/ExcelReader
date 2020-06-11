/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package excelreader;

import com.itextpdf.text.DocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author gabri
 *
 *
 */

public class ExcelReader {

    public static String[] condNames = {"MAYFAIR GARDENS", "PIAZZA VENEZIA", "SOLAR DO SUMARE", "DEL REY", "MARIA PAULA", "ASTORGA",
        "MARCIA", "CLASSIC PLAZA", "BANDEIRANTES", "NEW ORLEANS GARDENS", "RODESIA", "DONA NENE",
        "RIO VERDE", "EMPRESARIAL PAULISTA", "VENEZA", "OFFICE TOWER", "BLUE SKY", "MIRANTE DA LAPA",
        "BOULEVARD TILLI", "PERDIZES POINT", "PANORAMA", "EXPLENDOR", "CELEBRITIES JARDINS",
        "VILLA REALE", "PINA VAZ", "PERDIZES LASER",
        "ROCELY", "SAO GOTARDO", "DOM ANDRE", "FELLICI ANORIZZE EMPRESARIAL",
        "HAWAY", "MONT PARNASSE", "ITAPEVA ITAPURA ITAPEMA",
        "ICONE SANTANA"};
    public static String[] condKeysG = {"0001", "0003", "0004", "0005", "0015", "0022",
        "0023", "0027", "0028", "0029", "0033", "0037",
        "0038", "0039", "0041", "0045", "0048", "0049",
        "0050", "0051", "0056", "0057", "0061",
        "0062", "0065", "0069",
        "0070", "0071", "0073", "0074",
        "0075", "0076", "0084",
        "0086"};

    public static String[] getFilesNames(String directory) {
        File folder = new File(directory);
        File[] listOfFiles = folder.listFiles();
        String[] fileNames = new String[listOfFiles.length];
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                //String [] splitedName=listOfFiles[i].getName().split(".");
                //List<String> l = Arrays.asList(splitedName);
                //  if(l.contains("xls"))
                fileNames[i] = listOfFiles[i].getName();
            }
        }
        return fileNames;
    }

    /**
     * @param args the command line arguments
     */
    public static Map<String, CondominiumG> extractor (String source) {
        // TODO code application logic here
        Map<String, CondominiumG> condMapG = new HashMap<>();
        for (int i = 0; i < condNames.length; i++) {
            condMapG.put(condKeysG[i], new CondominiumG(condNames[i]));
        }
        source = source.replace("\\","/");
        //System.out.println(source);
        String[] fileNames = getFilesNames(source);
        for (int i = 0; i < fileNames.length; i++) {
            String[] splitedName = fileNames[i].split("\\.");
            //System.out.println(splitedName.length);
            List<String> l = Arrays.asList(splitedName);
            if (l.contains("xls")) {
                ArrayList<String> lines = new ArrayList<>();
                try {
                    FileInputStream file = new FileInputStream(new File(source+ "/" + fileNames[i]));

                    //Create Workbook instance holding reference to .xlsx file
                    XSSFWorkbook workbook = new XSSFWorkbook(file);

                    //Get first/desired sheet from the workbook
                    XSSFSheet sheet = workbook.getSheetAt(0);

                    //Iterate through each rows one by one
                    Iterator<Row> rowIterator = sheet.iterator();
                    FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
                    int index = 0;
                    while (rowIterator.hasNext()) {
                        Row row = rowIterator.next();
                        lines.add("");

                        //For each row, iterate through all the columns
                        Iterator<Cell> cellIterator = row.cellIterator();

                        while (cellIterator.hasNext()) {
                            Cell cell = cellIterator.next();
                            //Check the cell type and format accordingly
                            switch (formulaEvaluator.evaluateInCell(cell).getCellTypeEnum()) {
                                case NUMERIC:
                                    lines.set(index, lines.get(index) + cell.getNumericCellValue() + " ");
                                    break;
                                case STRING:
                                    lines.set(index, lines.get(index) + cell.getStringCellValue() + " ");
                                    break;
                                default:
                                    break;

                            }
                        }
                        index++;
                    }
                    file.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //for(int i = 0;i<lines.size();i++ ){
                //   System.out.println(lines.get(i));
                // }
                CondominiumG cond = null;
                String balance = "";

                String[] lineFrag = lines.get(3).split(" ");
                String key = lineFrag[1];
                //System.out.println(key);
                if (condMapG.containsKey(key)) {
                    cond = condMapG.get(key);
                } else {
                    cond = null;
                }

                if (cond != null) {
                    String[] b = lines.get(8).split(" ");
                    l = Arrays.asList(b);
                    //Primeira pagina
                    boolean band = false;
                    if (cond.name.equals("BANDEIRANTES")) {
                        b = lines.get(10).split(" ");
                        band = true;
                    }

                    if ((l.contains("SALDO") && l.contains("ANTERIOR")) || band) {
                        //System.out.println(lines.size());
                        balance = b[2];

                        int index = 9;
                        if (band) {
                            index = 11;
                        }

                        for (int j = index; j < lines.size(); j++) {

                            b = lines.get(j).split(" ");

                            String[] dt = null;
                            if (b.length > 2) {
                                dt = b[1].split("/");
                            } else {
                                continue;
                            }
                            if (dt.length != 3) {
                                dt = b[0].split("/");
                                if (dt.length != 3) {
                                    continue;
                                }
                            }
                            //System.out.println(b[1]);
                            //System.out.println(lines.get(j));
                            String bal = b[b.length - 2];

                            //DEBITO
                            //System.out.println(balance+" "+ bal);
                            if (Double.parseDouble(balance) > Double.parseDouble(bal)) {
                                cond.debitEntry.add(new Entry(lines.get(j)));
                                balance = bal;
                                //CREDITO
                            } else {
                                cond.creditEntry.add(new Entry(lines.get(j)));
                                balance = bal;
                            }

                        }//for int j = index; j < lines.size(); j++

                    } //if l.contains("SALDO") && l.contains("ANTERIOR")) || band

                }// if cond!=null 

                // System.out.println(fileNames[i]);
            }
        }
        //System.out.println(condMapG.size() + " " +condNames.length + " " + condKeysG.length);
     return condMapG;
    }// MAIN                             
}
