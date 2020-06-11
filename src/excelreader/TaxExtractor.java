/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package excelreader;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.DocumentException;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 *
 * @author gabri
 */
public class TaxExtractor {

    public static String[] taxesNames = {"TARIFA", "DOC/TED", "IOF", "ANUIDADE", "ENCARGOS", "ENCARGO", "TAR"};
    public static String[] retrieve = {"BAIXA", "RESGATE", "BX"};
    public static String[] investment = {"APLIC.INVEST", "APLICACAO", "APLICACOES"};

    public static String[] condNames = {"MAYFAIR GARDENS", "PIAZZA VENEZIA", "SOLAR DO SUMARE", "DEL REY", "MARIA PAULA", "ASTORGA",
        "MARCIA", "CLASSIC PLAZA", "BANDEIRANTES", "NEW ORLEANS GARDENS", "RODESIA", "DONA NENE",
        "RIO VERDE", "EMPRESARIAL PAULISTA", "VENEZA", "OFFICE TOWER", "BLUE SKY", "MIRANTE DA LAPA",
        "BOULEVARD TILLI", "PERDIZES POINT", "PANORAMA", "EXPLENDOR", "CELEBRITIES JARDINS",
        "VILLA REALE", "PINA VAZ", "PERDIZES LASER",
        "ROCELY", "SAO GOTARDO", "DOM ANDRE", "FELLICI ANORIZZE EMPRESARIAL",
        "HAWAY", "MONT PARNASSE", "ITAPEVA ITAPURA ITAPEMA",
        "ICONE SANTANA"};

    public static String[] condKeys = {"56.711.997/0001-42", "59.844.472/0001-73", "53.824.306/0001-29", "54.220.199/0001-92",
        "67.181.933/0001-01", "61.568.408/0001-59", "00.000.057/0001-90", "67.644.468/0001-90",
        "54.323.308/0001-05", "04.077.520/0001-53", "64.046.600/0001-73", "05.473.910/0001-05",
        "55.218.069/0001-88", "02.929.850/0001-02", "06.990.342/0001-82", "07.878.533/0001-10",
        "65.524.142/0001-01", "54.538.327/0001-40", "00.914.621/0001-80", "07.352.439/0001-22",
        "54.241.906/0001-27", "10.891.272/0001-82", "09.002.594/0001-44", "11.050.803/0001-77",
        "67.640.110/0001-99", "59.396.325/0001-88",
        "54.575.378/0001-42", "54.061.908/0001-34", "59.178.533/0001-00",
        "03.519.305/0001-00", "73.570.590/0001-61", "57.811.259/0001-30",
        "55.905.269/0001-09", "55.947.410/0001-36"};

    @SuppressWarnings("unchecked")
    public static void disableAccessWarnings() {
        try {
            Class unsafeClass = Class.forName("sun.misc.Unsafe");
            Field field = unsafeClass.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            Object unsafe = field.get(null);

            Method putObjectVolatile = unsafeClass.getDeclaredMethod("putObjectVolatile", Object.class, long.class, Object.class);
            Method staticFieldOffset = unsafeClass.getDeclaredMethod("staticFieldOffset", Field.class);

            Class loggerClass = Class.forName("jdk.internal.module.IllegalAccessLogger");
            Field loggerField = loggerClass.getDeclaredField("logger");
            Long offset = (Long) staticFieldOffset.invoke(unsafe, loggerField);
            putObjectVolatile.invoke(unsafe, loggerClass, offset, null);
        } catch (Exception ignored) {
        }
    }

    public static void print(Map<String, Condominium> condMap,  String dest) {

        try {
            BufferedWriter StrW = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dest +"/AppResc.csv"), "UTF-8"));

            for (int i = 0; i < condKeys.length; i++) {
                Condominium c = condMap.get(condKeys[i]);
                String date = "";
                String value = "=";
                //System.out.println(c.name);
                StrW.write(c.name + "\n");
                for (int j = 0; j < c.appRescDate.size(); j++) {
                    if (!date.equals(c.appRescDate.get(j))) {
                        if (j == 0) {
                            date = c.appRescDate.get(j);
                            value = value + c.appRescValue.get(j).replace(".", "");
                        } else {
                            //System.out.println(date+" "+value.replace(".", ","));
                            StrW.write(date + ";" + value + "\n");
                            date = c.appRescDate.get(j);
                            value = "=" + c.appRescValue.get(j).replace(".", "");
                        }
                    } else {
                        value = value + "+" + c.appRescValue.get(j).replace(".", "");
                    }
                }
                //System.out.println(date+" "+value.replace(".", ","));
                StrW.write(date + ";" + value + "\n");
            }
            StrW.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//TODO: RESOLVER BUG DO DIA 31

    public static void printT(Map<String, Condominium> condMap,  String dest) {

        try {
            BufferedWriter StrW = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dest+"/Tax.csv"), "UTF-8"));

            for (int i = 0; i < condKeys.length; i++) {
                Condominium c = condMap.get(condKeys[i]);
                String date = "";
                String value = "=";
                int dt = 1;
                StrW.write(c.name + "\n");
                for (int j = 0; j < c.taxDate.size(); j++) {
                    if (!date.equals(c.taxDate.get(j))) {
                        if (j == 0) {
                            date = c.taxDate.get(j);
                            value = value + c.taxValue.get(j);
                        } else {
                            //System.out.println(date+" "+value.replace(".", ","));
                            String[] d = date.split("/");
                            for (int k = dt; k < Integer.parseInt(d[0]); k++) {
                                StrW.write(k + "\n");
                            }
                            StrW.write(Integer.parseInt(d[0]) + ";" + value.replace(".", ",") + "\n");
                            date = c.taxDate.get(j);
                            value = "=" + c.taxValue.get(j);
                            dt = Integer.parseInt(d[0]) + 1;
                        }
                    } else {
                        value = value + "+" + c.taxValue.get(j);
                    }
                }
                //System.out.println(date+" "+value.replace(".", ","));
                String[] d = date.split("/");
                //System.out.println(date + " " + value);
                if(d.length==3){
                    StrW.write(Integer.parseInt(d[0]) + ";" + value.replace(".", ",") + "\n");
                    if (Integer.parseInt(d[0]) < 31) {
                        for (int k = Integer.parseInt(d[0]) + 1; k < 32; k++) 
                             StrW.write(k + "\n");
                    
                    }
                }   
            }
            StrW.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Condominium> extractor(String bankStatement, String month, String dest) throws IOException, DocumentException {
        disableAccessWarnings();
        Map<String, Condominium> condMap = new HashMap<>();
        dest=dest.replace("\\","/");
        bankStatement=bankStatement.replace("\\","/");

        for (int i = 0; i < condNames.length; i++) {
            condMap.put(condKeys[i], new Condominium(condNames[i]));
        }

        PdfReader reader = new PdfReader(bankStatement);

        List<String> condList = Arrays.asList(condKeys);
        int totalPages = reader.getNumberOfPages();
        String textFromPage = "";
        Condominium cond = null;
        
        ArrayList<String> listInvestDoc = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            //	if(i==146)
            //		break;
            textFromPage = PdfTextExtractor.getTextFromPage(reader, i);

            String[] lines = textFromPage.split("\\r?\\n");
            String[] lineFrag = null;
            //System.out.println(i);
            if (lines.length == 2) {
                lineFrag = lines[0].split(" ");
            } else if (Arrays.asList(lines[2].split(" ")).contains("Demonstrativo")) {
                lineFrag = lines[4].split(" ");
            } else {
                lineFrag = lines[3].split(" ");
            }

            String c = lineFrag[1];

            if (lineFrag[0].equals("CNPJ/CPF:")) {
                //System.out.println(c.equals("05.473.910/0001-05"));

                if (condMap.containsKey(c)) {
                    List<String> l = Arrays.asList(lineFrag);
                    cond = condMap.get(c);
                    if (l.contains("CIBRACON") || l.contains("MAISON") || l.contains("PEDRAS")) {
                        cond = null;
                    }
                } else {
                    cond = null;
                }
            }
            //if(c.equals("05.473.910/0001-05"))
            //System.out.println(cond.name);
            
            if (lineFrag[0].equals("CNPJ/CPF:")) {

                if (condList.contains(lineFrag[1]) && cond != null) {
                    
                    
                    for (int j = 5; j < lines.length; j++) {
                        String[] lineSplited = lines[j].split(" ");
                        if (lineSplited.length > 6) {
                            String[] date = lineSplited[0].split("/");
                            if (date.length == 3 && date[1].equals(month)) {
                                List<String> lineList = Arrays.asList(lineSplited);
                                //TODO: ARRUMAR ESSA MERDA
                                if ((lineList.contains(taxesNames[0]) || lineList.contains(taxesNames[1])
                                        || lineList.contains(taxesNames[2]) || lineList.contains(taxesNames[3]) || lineList.contains(taxesNames[4])
                                        || lineList.contains(taxesNames[5]) || lineList.contains(taxesNames[6])) && !lineList.contains("ESTORNO")) {
                                    cond.taxDate.add(lineSplited[0]);
                                    String v = lineSplited[lineSplited.length - 2];
                                    cond.taxValue.add(v);

                                    //System.out.println(lines[j]);
                                    //resgate: valor negativo
                                    
                                   // TODO: VERIFICAR ESTORNO DE APLICACAO 
                                    
                                } else if (lineList.contains(retrieve[0]) || lineList.contains(retrieve[1]) || lineList.contains(retrieve[2])) {
                                    cond.appRescDate.add(lineSplited[0]);
                                    String v = lineSplited[lineSplited.length - 2];
                                    cond.appRescValue.add("-" + v);

                                    //System.out.println(lines[j]);
                                    //aplicacao: valor positivo
                                    //AQUI
                                    
                                } else if (lineList.contains(investment[0]) || lineList.contains(investment[1]) || lineList.contains(investment[2])) {
                                    cond.appRescDate.add(lineSplited[0]);
                                    String v = lineSplited[lineSplited.length - 2];
                                    //System.out.println(lineSplited[lineSplited.length - 4]);
                                    listInvestDoc.add(lineSplited[lineSplited.length - 4]);
                                    cond.appRescValue.add(v);

                                    //ystem.out.println(lines[j]);
                                } else {

                                    if (lineSplited[lineSplited.length - 1].equals("C")) {
                                        
                                         if(lineList.contains("ESTORNO")){
                                        
                                       //System.out.println(listInvestDoc.contains(lineSplited[lineSplited.length - 4]));
                                        cond.appRescDate.add(lineSplited[0]);
                                        String v = lineSplited[lineSplited.length - 2];
                                          //System.out.println(v);
                                         cond.appRescValue.add("-" + v);
                                        continue;
                                    }
                                          cond.creditEntry.add(new Entry(lines[j]));
                                        //System.out.println(lines[j]);
                                    } else {
                                        cond.debitEntry.add(new Entry(lines[j]));
                                        //  System.out.println(lines[j]);
                                    }
                                }
                            }

                        } else {
                            if (lineSplited.length > 5) {
                                //System.out.println(lines[j]);
                            }
                        }

                    }
                    //count++;
                    //System.out.println(lines[4]);

                }
            } else if (cond != null) {
                for (int j = 0; j < lines.length; j++) {
                    String[] lineSplited = lines[j].split(" ");
                    if (lineSplited.length > 6) {
                        String[] date = lineSplited[0].split("/");
                        if (date.length == 3 && date[1].equals(month)) {
                            List<String> lineList = Arrays.asList(lineSplited);
                            if ((lineList.contains(taxesNames[0]) || lineList.contains(taxesNames[1])
                                    || lineList.contains(taxesNames[2]) || lineList.contains(taxesNames[3]) || lineList.contains(taxesNames[4])
                                    || lineList.contains(taxesNames[5]) || lineList.contains(taxesNames[6])) && !lineList.contains("ESTORNO")) {
                                cond.taxDate.add(lineSplited[0]);
                                String v = lineSplited[lineSplited.length - 2];
                                cond.taxValue.add(v);

                                //System.out.println(lines[j]);
                                //resgate: valor negativo
                            } else if (lineList.contains(retrieve[0]) || lineList.contains(retrieve[1]) || lineList.contains(retrieve[2])) {
                                cond.appRescDate.add(lineSplited[0]);
                                String v = lineSplited[lineSplited.length - 2];
                                //System.out.println(v);
                                cond.appRescValue.add("-" + v);
                                // System.out.println(lines[j]);

                            } else if (lineList.contains(investment[0]) || lineList.contains(investment[1]) || lineList.contains(investment[2])) {
                                //System.out.println(lines[j]);
                                //System.out.println(cond.name);
                                cond.appRescDate.add(lineSplited[0]);
                                String v = lineSplited[lineSplited.length - 2];
                                listInvestDoc.add(lineSplited[lineSplited.length - 4]);
                                //System.out.println(lineSplited[lineSplited.length - 4]);
                                cond.appRescValue.add(v);

                                // System.out.println(lines[j]);
                            } else {
                                if (lineSplited[lineSplited.length - 1].equals("C")) {
                                    if(lineList.contains("ESTORNO")){
                                        //System.out.println(listInvestDoc.contains(lineSplited[lineSplited.length - 4]));
                                        cond.appRescDate.add(lineSplited[0]);
                                        String v = lineSplited[lineSplited.length - 2];
                                          //System.out.println(v);
                                         cond.appRescValue.add("-" + v);
                                        continue;
                                    }
                                    cond.creditEntry.add(new Entry(lines[j]));
                                    //System.out.println(lines[j]);
                                } else {
                                    cond.debitEntry.add(new Entry(lines[j]));
                                    //System.out.println(lines[j]);
                                }
                            }
                        }

                    } else {
                        if (lineSplited.length > 5) {
                            //  System.out.println(lines[j]);
                        }
                    }

                }
            }
        }//for i
        reader.close();
        //System.out.println("Count: "+ count + "condList:"+condKeys.length);
        //System.out.println("CondNames: "+ condNames.length + "condList:"+condKeys.length);
        //System.out.println(condMap.size());
        /**
         * for(int i = 0;i< condKeys.length;i++){ Condominium c =
         * condMap.get(condKeys[i]);
         *
         * if(c.taxDate.size() == c.taxValue.size()) for(int j=0; j<
         * c.taxValue.size();j++) System.out.println(c.taxDate.get(j)+"
         * "+c.taxValue.get(j)); }
         *
         * for(int i = 0;i< condKeys.length;i++){ Condominium c =
         * condMap.get(condKeys[i]); System.out.println(c.taxDate.size()); }
         *
         */
        print(condMap, dest);
        printT(condMap, dest);
        //Condominium c = condMap.get("05.473.910/0001-05");
        //System.out.println(c.appRescDate.size());

        return condMap;
    }

}
