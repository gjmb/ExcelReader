/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package excelreader;

import java.io.FileOutputStream;
import java.io.IOException;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.DocumentException;
import static java.nio.charset.StandardCharsets.*;
import java.util.*;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.BaseColor;

/**
 *
 * @author gabri
 */
public class ConciliatorV2 {

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    } // round

    //Verifica os cheques pelo numero, nao verifica valores
    public static void checkFinder(CondominiumG g, Condominium c, ArrayList<EntryPair> p) {
        for (int i = 0; i < c.debitEntry.size(); i++) {
            String[] entryContent = c.debitEntry.get(i).content.split(" ");
            List<String> l = Arrays.asList(entryContent);
            if (l.contains("CHEQUE") || l.contains("CHQ.PAGO")) {
                EntryPair ep = new EntryPair(new Entry(c.debitEntry.get(i).content));
                p.add(ep);
                c.debitEntry.get(i).errorType = 0;
                for (int j = 0; j < g.debitEntry.size(); j++) {
                    String[] entryContentG = g.debitEntry.get(j).content.split(" ");
                    List<String> lG = Arrays.asList(entryContentG);
                    if (lG.contains("CH")) {
                        if (lG.contains(entryContent[entryContent.length - 4])) {
                            //System.out.println("    "+g.debitEntry.get(j).content);
                            if (lG.contains(entryContent[0])) {
                                ep.entry.errorType = 0;
                                g.debitEntry.get(j).errorType = 0;
                                ep.pair.add(g.debitEntry.get(j));
                            } else {
                                ep.entry.errorType = 1;
                                g.debitEntry.get(j).errorType = 0;
                                ep.pair.add(g.debitEntry.get(j));
                            }
                        }
                    }// if lG.contains("CH")
                }// for Entry debitEntryG : g.debitEntry
            } // if l.contains("CHEQUE")
        }// for Entry debitEntry : c.debitEntry
    } // checkFinder

    public static double creditTotalG(CondominiumG c) {
        double total = 0.00;

        //Soma
        //TODO: Ignorar transferencia
        for (int i = 0; i < c.creditEntry.size(); i++) {
            String[] e = c.creditEntry.get(i).content.split(" ");
            List<String> l = Arrays.asList(e);
            String v = e[e.length - 3];
            if (!l.contains("REC.") && !l.contains("RECEBIDO")) {
                total = total + Double.parseDouble(v);
            }
        }
        //Busca por isencao

        for (int i = 0; i < c.debitEntry.size(); i++) {
            String[] e = c.debitEntry.get(i).content.split(" ");
            List<String> l = Arrays.asList(e);
            if ((l.contains("00000213") || l.contains("00001040")) && l.contains("Recibo:")) {
                //System.out.println("Oi");
                String v = e[e.length - 3];
                total = total - Double.parseDouble(v);
                c.debitEntry.get(i).errorType = 0;
            }

        }

        return round(total, 2);
    } //creditTotalG

    public static double creditTotal(Condominium c) {
        double total = 0.00;

        //Soma
        for (int i = 0; i < c.creditEntry.size(); i++) {
            String[] e = c.creditEntry.get(i).content.split(" ");
            String v = e[e.length - 2].replace(".", "");
            v = v.replace(",", ".");
            total = total + Double.parseDouble(v);
        }

        return round(total, 2);
    } // creditTotal

    public static void wageTotal(Condominium c, double[] wage, String[] dtW, ArrayList<EntryPair> p) {
        String[] w = null;

        for (int i = 0; i < c.debitEntry.size(); i++) {
            String[] entryContent = c.debitEntry.get(i).content.split(" ");
            List<String> l = Arrays.asList(entryContent);
            String v = entryContent[entryContent.length - 2].replace(".", "");
            v = v.replace(",", ".");
            if (v.charAt(v.length() - 1) == '0') {
                v = v.substring(0, v.length() - 1);
            }
            String[] dt = entryContent[0].split("/");

            if (l.contains("PAGAMENTO") && l.contains("FUNCIONARIOS")) {
                if (Integer.parseInt(dt[0]) <= 10) {
                    c.debitEntry.get(i).errorType = 0;
                    dtW[0] = entryContent[0];
                    //System.out.println("dtW: " + dtW[0]);
                    w = entryContent;
                    wage[0] = round(round(wage[0], 2) + round(Double.parseDouble(v), 2), 2);
                } else {
                    break;
                }
            } // if l.contains("PAGAMENTO") && l.contains("FUNCIONARIOS")
        } // for int i = 0; i < c.debitEntry.size(); i++
        if (w == null) {
            return;
        }
        w[w.length - 2] = Double.toString(wage[0]);

        String wContent = "";

        for (int i = 0; i < w.length; i++) {
            wContent = wContent + w[i] + " ";
        }

        p.add(new EntryPair(new Entry(wContent)));

    } // wageTotal

    public static void advTotal(Condominium c, double[] adv, String[] dtA, ArrayList<EntryPair> p) {

        String[] a = null;
        for (int i = 0; i < c.debitEntry.size(); i++) {
            String[] entryContent = c.debitEntry.get(i).content.split(" ");
            List<String> l = Arrays.asList(entryContent);
            String v = entryContent[entryContent.length - 2].replace(".", "");
            v = v.replace(",", ".");
            if (v.charAt(v.length() - 1) == '0') {
                v = v.substring(0, v.length() - 1);
            }
            String[] dt = entryContent[0].split("/");

            if (l.contains("PAGAMENTO") && l.contains("FUNCIONARIOS")) {
                if (Integer.parseInt(dt[0]) > 10) {
                    c.debitEntry.get(i).errorType = 0;
                    dtA[0] = entryContent[0];
                    a = entryContent;
                    adv[0] = round(round(adv[0], 2) + round(Double.parseDouble(v), 2), 2);
                }
            } // if l.contains("PAGAMENTO") && l.contains("FUNCIONARIOS")
        } // for int i = 0; i < c.debitEntry.size(); i++
        if (a == null) {
            return;
        }

        a[a.length - 2] = Double.toString(adv[0]);

        String aContent = "";

        for (int i = 0; i < a.length; i++) {
            aContent = aContent + a[i] + " ";
        }

        p.add(new EntryPair(new Entry(aContent)));
    } // wageTotal

    public static void wageFinder(CondominiumG g, Condominium c, double[] wageDif, double[] wage, ArrayList<EntryPair> p) {
        String[] dtW = {""};
        
        //Busca por pagamentos de salario individuais 
        for (int i = 0; i < g.debitEntry.size(); i++) {
            String[] entryContentG = g.debitEntry.get(i).content.split(" ");
            List<String> l = Arrays.asList(entryContentG);
            if (l.contains("SALDO") && l.contains("00000167") && g.debitEntry.get(i).errorType == -1) {
                String vG = entryContentG[entryContentG.length - 3];
                for (Entry debitEntry : c.debitEntry) {
                    String[] entryContent = debitEntry.content.split(" ");
                    List<String> l2 = Arrays.asList(entryContent);
                    String v = entryContent[entryContent.length - 2].replace(".", "");
                    v = v.replace(",", ".");
                    if (v.charAt(v.length() - 1) == '0') {
                        v = v.substring(0, v.length() - 1);
                    }
                    if (vG.equals(v)) {
                        if (l2.contains(entryContentG[entryContentG.length - 4])) {
                            EntryPair ep = new EntryPair(new Entry(debitEntry.content));
                            Entry e = new Entry(g.debitEntry.get(i).content);
                            if (l2.contains(entryContentG[1])) {
                                ep.entry.errorType = 0;
                                e.errorType = 0;
                            } else {
                                ep.entry.errorType = 1;
                                e.errorType = 1;
                            }

                            ep.pair.add(e);
                            p.add(ep);
                            g.debitEntry.get(i).errorType = 0;
                            debitEntry.errorType = 0;
                            break;
                        }
                    }// if vG.equals(v)   
                }// for Entry debitEntry : c.debitEntry               
            } //l.contains("SALDO") && l.contains("00000167") && g.debitEntry.get(i).errorType == -1
        } // for (int i = 0; i < g.debitEntry.size(); i++)  
        wageTotal(c, wage, dtW, p);
        if (p.isEmpty()) {
            return;
        }
        if (wage[0] == 0.0) {
            p.removeAll(p);
            return;
        }
        EntryPair ep = null;
        //TODO: ARRUMAR 
        for (int i = 0; i < p.size(); i++) {
            String[] entryContent = p.get(i).entry.content.split(" ");
            //System.out.println("dtW: " + dtW[0] + " " + p.get(i).entry.content);
            List<String> l = Arrays.asList(entryContent);
            if (l.contains("PAGAMENTO") && l.contains("FUNCIONARIOS") && l.contains(dtW[0])) {
                ep = p.get(i);
                break;
            }
        }

        for (int i = 0; i < g.debitEntry.size(); i++) {
            String[] entryContentG = g.debitEntry.get(i).content.split(" ");
            List<String> l = Arrays.asList(entryContentG);
            if (l.contains("SALDO") && l.contains("00000167") && g.debitEntry.get(i).errorType == -1) {
                g.debitEntry.get(i).errorType = 0;
                String vG = entryContentG[entryContentG.length - 3];
                wageDif[0] = round(round(wageDif[0], 2) + round(Double.parseDouble(vG), 2), 2);
                Entry e = new Entry(g.debitEntry.get(i).content);
                ep.pair.add(e);
            }
        }// for (int i = 0; i < g.debitEntry.size(); i++)
        wageDif[0] = round(round(wageDif[0], 2) - round(wage[0], 2), 2);
        if (wageDif[0] == 0.0) {
            ep.entry.errorType = 0;
            for (int i = 0; i < ep.pair.size(); i++) {
                String[] entryContent = ep.pair.get(i).content.split(" ");

                List<String> l = Arrays.asList(entryContent);
                if (l.contains(dtW[0])) {
                    ep.pair.get(i).errorType = 0;
                } else {
                    ep.pair.get(i).errorType = 1;
                    ep.entry.errorType = 1;
                }

            }

        }

    }// public static void wageFinder
    //TODO: TESTAR wageFinder
    
     public static void advFinder(CondominiumG g, Condominium c, double[] wageDif, double[] wage, ArrayList<EntryPair> p) {
        String[] dtW = {""};
        
        //Busca por pagamentos de salario individuais 
        for (int i = 0; i < g.debitEntry.size(); i++) {
            String[] entryContentG = g.debitEntry.get(i).content.split(" ");
            List<String> l = Arrays.asList(entryContentG);
            if (l.contains("ADTO.") && l.contains("00000167") && g.debitEntry.get(i).errorType == -1) {
                String vG = entryContentG[entryContentG.length - 3];
                for (Entry debitEntry : c.debitEntry) {
                    String[] entryContent = debitEntry.content.split(" ");
                    List<String> l2 = Arrays.asList(entryContent);
                    String v = entryContent[entryContent.length - 2].replace(".", "");
                    v = v.replace(",", ".");
                    if (v.charAt(v.length() - 1) == '0') {
                        v = v.substring(0, v.length() - 1);
                    }
                    if (vG.equals(v)) {
                        if (l2.contains(entryContentG[entryContentG.length - 4])) {
                            EntryPair ep = new EntryPair(new Entry(debitEntry.content));
                            Entry e = new Entry(g.debitEntry.get(i).content);
                            if (l2.contains(entryContentG[1])) {
                                ep.entry.errorType = 0;
                                e.errorType = 0;
                            } else {
                                ep.entry.errorType = 1;
                                e.errorType = 1;
                            }

                            ep.pair.add(e);
                            p.add(ep);
                            g.debitEntry.get(i).errorType = 0;
                            debitEntry.errorType = 0;
                            break;
                        }
                    }// if vG.equals(v)   
                }// for Entry debitEntry : c.debitEntry               
            } //l.contains("SALDO") && l.contains("00000167") && g.debitEntry.get(i).errorType == -1
        } // for (int i = 0; i < g.debitEntry.size(); i++)  
        advTotal(c, wage, dtW, p);
        if (p.isEmpty()) {
            return;
        }
        if (wage[0] == 0.0) {
            p.removeAll(p);
            return;
        }
        EntryPair ep = null;
        //TODO: ARRUMAR 
        for (int i = 0; i < p.size(); i++) {
            String[] entryContent = p.get(i).entry.content.split(" ");
            //System.out.println("dtW: " + dtW[0] + " " + p.get(i).entry.content);
            List<String> l = Arrays.asList(entryContent);
            if (l.contains("PAGAMENTO") && l.contains("FUNCIONARIOS") && l.contains(dtW[0])) {
                ep = p.get(i);
                break;
            }
        }

        for (int i = 0; i < g.debitEntry.size(); i++) {
            String[] entryContentG = g.debitEntry.get(i).content.split(" ");
            List<String> l = Arrays.asList(entryContentG);
            if (l.contains("ADTO.") && l.contains("00000167") && g.debitEntry.get(i).errorType == -1) {
                g.debitEntry.get(i).errorType = 0;
                String vG = entryContentG[entryContentG.length - 3];
                wageDif[0] = round(round(wageDif[0], 2) + round(Double.parseDouble(vG), 2), 2);
                Entry e = new Entry(g.debitEntry.get(i).content);
                ep.pair.add(e);
            }
        }// for (int i = 0; i < g.debitEntry.size(); i++)
        wageDif[0] = round(round(wageDif[0], 2) - round(wage[0], 2), 2);
        if (wageDif[0] == 0.0) {
            ep.entry.errorType = 0;
            for (int i = 0; i < ep.pair.size(); i++) {
                String[] entryContent = ep.pair.get(i).content.split(" ");

                List<String> l = Arrays.asList(entryContent);
                if (l.contains(dtW[0])) {
                    ep.pair.get(i).errorType = 0;
                } else {
                    ep.pair.get(i).errorType = 1;
                    ep.entry.errorType = 1;
                }

            }

        }

    }

    public static void checker(Map<String, Condominium> condMap, Map<String, CondominiumG> condMapG, String dest) {
        dest = dest.replace("\\", "/");
        for (int index = 0; index < ExcelReader.condKeysG.length; index++) {

            Condominium c = condMap.get(TaxExtractor.condKeys[index]);
            CondominiumG g = condMapG.get(ExcelReader.condKeysG[index]);

            Document document = new Document(PageSize.A4, 0f, 0f, 0f, 0f);
            float fntSize, lineSpacing;
            fntSize = 9f;
            lineSpacing = 10f;

            try {
                PdfWriter.getInstance(document, new FileOutputStream(dest + "/relatorio_" + ExcelReader.condKeysG[index] + ".pdf"));
                document.open();
                Paragraph p = new Paragraph(new Phrase(lineSpacing, "   *****************************", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                document.add(p);
                p = new Paragraph(new Phrase(lineSpacing, "      " + ExcelReader.condKeysG[index] + " - " + c.name, FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                document.add(p);
                p = new Paragraph(new Phrase(lineSpacing, "   *****************************", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                document.add(p);
                p = new Paragraph(new Phrase(lineSpacing, "                ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                document.add(p);
                /////////////////////////////////////////////
                ///////verifica diferenca de credito////////
                ///////////////////////////////////////////
                double tg = creditTotalG(g);
                double t = creditTotal(c);
                boolean findDifCredit = false;
                if (tg > t) {
                    findDifCredit = true;
                    p = new Paragraph(new Phrase(lineSpacing, "     DIFERENCA DE CREDITO* ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                    document.add(p);
                    p = new Paragraph(new Phrase(lineSpacing, "                ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                    document.add(p);
                    Font f1 = FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize);
                    f1.setColor(BaseColor.BLUE);
                    p = new Paragraph(new Phrase(lineSpacing, "         VALOR MAIOR: GOSOFT", f1));
                    document.add(p);
                    p = new Paragraph(new Phrase(lineSpacing, "               DIFERENCA: " + round(tg - t, 2), f1));
                    document.add(p);
                    double dif = round((tg - t), 2);
                    //busca no gosoft um credito de mesmo valor
                    for (int i = 0; i < g.creditEntry.size(); i++) {
                        String[] entryContent = g.creditEntry.get(i).content.split(" ");
                        String v = entryContent[entryContent.length - 3];
                        if (dif == round(Double.parseDouble(v), 2)) {
                            byte[] text = g.creditEntry.get(i).content.getBytes(ISO_8859_1);
                            g.creditEntry.get(i).content = new String(text, UTF_8);
                            p = new Paragraph(new Phrase(lineSpacing, "               CREDITO DE MESMO VALOR: " + g.creditEntry.get(i).content, f1));
                            document.add(p);

                        }
                    }
                    //busca no gosoft um debito de mesmo valor
                    for (int i = 0; i < g.debitEntry.size(); i++) {
                        String[] entryContent = g.debitEntry.get(i).content.split(" ");
                        String v = entryContent[entryContent.length - 3];

                        if (dif == round(Double.parseDouble(v), 2)) {
                            byte[] text = g.debitEntry.get(i).content.getBytes(ISO_8859_1);
                            g.debitEntry.get(i).content = new String(text, UTF_8);
                            f1.setColor(BaseColor.RED);
                            p = new Paragraph(new Phrase(lineSpacing, "               DEBITO DE MESMO VALOR: " + g.debitEntry.get(i).content, f1));
                            document.add(p);

                        }
                    }

                } else if (t > tg) {
                    findDifCredit = true;
                    p = new Paragraph(new Phrase(lineSpacing, "     DIFERENCA DE CREDITO* ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                    document.add(p);
                    p = new Paragraph(new Phrase(lineSpacing, "                ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                    document.add(p);
                    Font f1 = FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize);
                    f1.setColor(BaseColor.BLUE);
                    p = new Paragraph(new Phrase(lineSpacing, "           VALOR MAIOR: BANCO", f1));
                    document.add(p);
                    p = new Paragraph(new Phrase(lineSpacing, "               DIFERENCA: " + round(t - tg, 2), f1));
                    document.add(p);
                    double dif = round(t - tg, 2);
                    //busca no banco um credito de mesmo valor
                    for (int i = 0; i < c.creditEntry.size(); i++) {
                        String[] entryContent = c.creditEntry.get(i).content.split(" ");
                        String v = entryContent[entryContent.length - 2].replace(".", "");
                        v = v.replace(",", ".");
                        if (v.charAt(v.length() - 1) == '0') {
                            v = v.substring(0, v.length() - 1);
                        }
                        if (dif == round(Double.parseDouble(v), 2)) {
                            p = new Paragraph(new Phrase(lineSpacing, "               CREDITO DE MESMO VALOR: " + c.creditEntry.get(i).content, f1));
                            document.add(p);

                        }
                    }
                    //busca no banco um debito de mesmo valor
                    for (int i = 0; i < c.debitEntry.size(); i++) {
                        String[] entryContent = c.debitEntry.get(i).content.split(" ");
                        String v = entryContent[entryContent.length - 2].replace(".", "");
                        v = v.replace(",", ".");
                        if (v.charAt(v.length() - 1) == '0') {
                            v = v.substring(0, v.length() - 1);
                        }
                        if (dif == round(Double.parseDouble(v), 2)) {
                            f1.setColor(BaseColor.RED);
                            p = new Paragraph(new Phrase(lineSpacing, "               DEBITOS DE MESMO VALOR: " + c.debitEntry.get(i).content, f1));
                            document.add(p);

                        }
                    }
                }
                /////////////////////////////////////////////
                ///////verifica diferenca de credito////////
                ///////////////////////////////////////////

                /////////////////////////////////////////////
                ///////      Verifica os cheques    ////////
                ///////////////////////////////////////////
                ArrayList<EntryPair> checkPair = new ArrayList<>();
                checkFinder(g, c, checkPair);
                ArrayList<EntryPair> checkPairD = new ArrayList<>();
                ArrayList<EntryPair> checkPairP = new ArrayList<>();
                //TODO: ARRUMAR IMPRESSAO DE "     CHEQUES "

                boolean findCheckProblem = false;
                for (int i = 0; i < checkPair.size(); i++) {
                    double checkSum = 0.0;
                    String v = "";
                    if (checkPair.get(i).entry.errorType == 0) {
                        String[] entryContent = checkPair.get(i).entry.content.split(" ");
                        v = entryContent[entryContent.length - 2].replace(".", "");
                        v = v.replace(",", ".");
                        if (v.charAt(v.length() - 1) == '0') {
                            v = v.substring(0, v.length() - 1);
                        }
                        // System.out.println(checkPair.get(i).entry.content);
                        //Soma os valores dos lancamentos para comparar com o valor total do extrato bancario 
                        for (int j = 0; j < checkPair.get(i).pair.size(); j++) {
                            String[] entryContentG = checkPair.get(i).pair.get(j).content.split(" ");
                            String v1 = entryContentG[entryContentG.length - 3];
                            checkSum = checkSum + round(Double.parseDouble(v1), 2);
                            //     System.out.println(checkPair.get(i).pair.get(j).content);
                        }
                        if (v.equals(Double.toString(round(checkSum, 2)))) {
                            //verifica se algum pagamento voltou 
                            for (int j = 0; j < checkPair.get(i).pair.size(); j++) {
                                String[] entryContentG = checkPair.get(i).pair.get(j).content.split(" ");
                                String v1 = entryContentG[entryContentG.length - 3];
                                //busca no banco um credito de mesmo valor
                                for (int k = 0; k < c.creditEntry.size(); k++) {
                                    String[] entryContentC = c.creditEntry.get(k).content.split(" ");
                                    String vC = entryContentC[entryContentC.length - 2].replace(".", "");
                                    vC = vC.replace(",", ".");
                                    if (vC.charAt(vC.length() - 1) == '0') {
                                        vC = vC.substring(0, vC.length() - 1);
                                    }
                                    if (vC.equals(v1) && entryContentG[1].equals(entryContentC[0])) {
                                        if (!findCheckProblem) {
                                            p = new Paragraph(new Phrase(lineSpacing, "                    ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                                            document.add(p);
                                            p = new Paragraph(new Phrase(lineSpacing, "     CHEQUES ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                                            document.add(p);
                                            findCheckProblem = true;
                                        }
                                        // System.out.println("v1 " + v1);
                                        //   System.out.println("vC " + vC);
                                        Font f1 = FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize);
                                        f1.setColor(BaseColor.RED);
                                        p = new Paragraph(new Phrase(lineSpacing, "                ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                                        document.add(p);
                                        p = new Paragraph(new Phrase(lineSpacing, "         CHEQUE(S) BAIXADO(S) COM POSSIVEL DEVOLUCAO DE VALOR", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                                        document.add(p);
                                        p = new Paragraph(new Phrase(lineSpacing, "             " + checkPair.get(i).entry.content, f1));
                                        document.add(p);
                                        p = new Paragraph(new Phrase(lineSpacing, "               " + checkPair.get(i).pair.get(j).content, f1));
                                        document.add(p);
                                        f1.setColor(BaseColor.BLUE);
                                        p = new Paragraph(new Phrase(lineSpacing, "               " + c.creditEntry.get(k).content, f1));
                                        document.add(p);
                                    }
                                }// for (int j = 0; j < checkPair.get(i).pair.size(); j++)

                            }//for (int j = 0; j < checkPair.get(i).pair.size(); j++)

                            // System.out.println("TRUE");
                            ///////////////////////////////////////////////////    
                            // caso de cheque baixado com diferenca de valor //
                            ///////////////////////////////////////////////////
                        } else {
                            //ver diferenca
                            //se cheque baixado a menor, verificar se existe credito no mesmo valor
                            String dif = Double.toString(round(round(checkSum, 2) - round(Double.valueOf(v), 2), 2));
                            if (round(round(checkSum, 2) - round(Double.valueOf(v), 2), 2) > 0) {
                                if (!findCheckProblem) {
                                    p = new Paragraph(new Phrase(lineSpacing, "                    ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                                    document.add(p);
                                    p = new Paragraph(new Phrase(lineSpacing, "     CHEQUES ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                                    document.add(p);
                                    findCheckProblem = true;
                                }
                                p = new Paragraph(new Phrase(lineSpacing, "                ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                                document.add(p);
                                p = new Paragraph(new Phrase(lineSpacing, "         CHEQUE(S) COMPENSADO(S) A MENOR", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                                document.add(p);
                                Font f1 = FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize);
                                f1.setColor(BaseColor.RED);
                                p = new Paragraph(new Phrase(lineSpacing, "               " + checkPair.get(i).entry.content, f1));
                                document.add(p);
                                for (int j = 0; j < checkPair.get(i).pair.size(); j++) {
                                    p = new Paragraph(new Phrase(lineSpacing, "                 " + checkPair.get(i).pair.get(j).content, f1));
                                    document.add(p);
                                }
                                f1.setColor(BaseColor.BLUE);
                                p = new Paragraph(new Phrase(lineSpacing, "                 DIFERENCA: " + dif, f1));
                                document.add(p);
                            } else {
                                dif = dif.replace("-", "");
                                // System.out.println("Dif: " + dif);
                                for (int k = 0; k < c.creditEntry.size(); k++) {
                                    String[] entryContentC = c.creditEntry.get(k).content.split(" ");
                                    String vC = entryContentC[entryContentC.length - 2].replace(".", "");
                                    vC = vC.replace(",", ".");
                                    if (vC.charAt(vC.length() - 1) == '0') {
                                        vC = vC.substring(0, vC.length() - 1);
                                    }
                                    //   System.out.println("vC: " + vC);
                                    //    System.out.println("entryContent[1]: " + entryContent[1] + " entryContentC[0]: " + entryContentC[0]);
                                    if (vC.equals(dif) && entryContent[0].equals(entryContentC[0])) {
                                        if (!findCheckProblem) {
                                            p = new Paragraph(new Phrase(lineSpacing, "                    ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                                            document.add(p);
                                            p = new Paragraph(new Phrase(lineSpacing, "     CHEQUES ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                                            document.add(p);
                                            findCheckProblem = true;
                                        }
                                        Font f1 = FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize);
                                        f1.setColor(BaseColor.RED);
                                        p = new Paragraph(new Phrase(lineSpacing, "                ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                                        document.add(p);
                                        p = new Paragraph(new Phrase(lineSpacing, "         CHEQUE(S) BAIXADO(S) COM DEVOLUCAO DE VALOR", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                                        document.add(p);
                                        p = new Paragraph(new Phrase(lineSpacing, "             " + checkPair.get(i).entry.content, f1));
                                        document.add(p);
                                        for (int j = 0; j < checkPair.get(i).pair.size(); j++) {
                                            p = new Paragraph(new Phrase(lineSpacing, "                 " + checkPair.get(i).pair.get(j).content, f1));
                                            document.add(p);
                                        }
                                        f1.setColor(BaseColor.BLUE);
                                        p = new Paragraph(new Phrase(lineSpacing, "                 " + c.creditEntry.get(k).content, f1));
                                        document.add(p);
                                    }
                                }

                            }

                            //   System.out.println("FALSE");
                        }
                        // cheque nao baixado ou baixado na data errada    
                    } else if (checkPair.get(i).entry.errorType == 1) {
                        checkPairD.add(checkPair.get(i));
                    } else {
                        checkPairP.add(checkPair.get(i));

                    }

                }//for (int i = 0; i < checkPair.size(); i++)
                Font f1 = FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize);
                if (checkPairD.size() > 0) {
                    if (!findCheckProblem) {
                        p = new Paragraph(new Phrase(lineSpacing, "                    ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                        document.add(p);
                        p = new Paragraph(new Phrase(lineSpacing, "     CHEQUES ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                        document.add(p);
                        findCheckProblem = true;
                    }
                    p = new Paragraph(new Phrase(lineSpacing, "                ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                    document.add(p);
                    p = new Paragraph(new Phrase(lineSpacing, "         CHEQUE(S) BAIXADO(S) NA DATA ERRADA", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                    document.add(p);
                    for (int i = 0; i < checkPairD.size(); i++) {
                        f1.setColor(BaseColor.DARK_GRAY);
                        p = new Paragraph(new Phrase(lineSpacing, "             " + checkPairD.get(i).entry.content, f1));
                        document.add(p);
                        for (int j = 0; j < checkPairD.get(i).pair.size(); j++) {
                            p = new Paragraph(new Phrase(lineSpacing, "                 " + checkPairD.get(i).pair.get(j).content, f1));
                            document.add(p);
                        }
                    }
                }
                if (checkPairP.size() > 0) {
                    if (!findCheckProblem) {
                        p = new Paragraph(new Phrase(lineSpacing, "                    ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                        document.add(p);
                        p = new Paragraph(new Phrase(lineSpacing, "     CHEQUES ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                        document.add(p);
                        findCheckProblem = true;
                    }
                    p = new Paragraph(new Phrase(lineSpacing, "                ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                    document.add(p);
                    p = new Paragraph(new Phrase(lineSpacing, "         CHEQUE(S) NAO BAIXADO(S)", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                    document.add(p);
                    for (int i = 0; i < checkPairP.size(); i++) {
                        f1.setColor(BaseColor.RED);
                        p = new Paragraph(new Phrase(lineSpacing, "             " + checkPairP.get(i).entry.content, f1));
                        document.add(p);
                        for (int j = 0; j < checkPairP.get(i).pair.size(); j++) {
                            p = new Paragraph(new Phrase(lineSpacing, "                 " + checkPairP.get(i).pair.get(j).content, f1));
                            document.add(p);
                        }
                    }
                }

                /////////////////////////////////////////////
                ///////      Verifica os cheques    ////////
                ///////////////////////////////////////////
                /////////////////////////////////////////////
                ///////     Verifica os salarios    ////////
                ///////////////////////////////////////////
                List<String> condKeysWage = Arrays.asList(new String[]{"0001", "0003", "0004", "0005", "0015", "0022",
                    "0023", "0027", "0028", "0029", "0033", "0037",
                    "0038", "0039", "0041", "0045", "0048", "0049",
                    "0050", "0051", "0056", "0057", "0061"});

                if (condKeysWage.contains(ExcelReader.condKeysG[index])) {

                    double[] wageDif = {0.0};
                    double[] wage = {0.0};
                    ArrayList<EntryPair> w = new ArrayList<>();

                    wageFinder(g, c, wageDif, wage, w);

                    // System.out.println("wage: " + wage[0]);
                    //System.out.println("wageDif:  " + wageDif[0]);
                    boolean findWageProblem = false;
                    for (int i = 0; i < w.size(); i++) {
                        if (w.get(i).entry.errorType != 0) {
                            if (!findWageProblem) {
                                p = new Paragraph(new Phrase(lineSpacing, "                ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                                document.add(p);
                                p = new Paragraph(new Phrase(lineSpacing, "     PAGAMENTO(S) FUNCIONARIO(S) ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                                document.add(p);
                                p = new Paragraph(new Phrase(lineSpacing, "                ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                                document.add(p);
                                findWageProblem = true;
                            }
                            String[] entryContent = w.get(i).entry.content.split(" ");
                            List<String> l = Arrays.asList(entryContent);
                            String[] dt = entryContent[0].split("/");

                            //System.out.println(w.get(i).entry.content + " "+  dt.length + " "+ dt[0]);
                            if (w.get(i).entry.errorType == -1 && wageDif[0] != 0 && Integer.parseInt(dt[0]) <= 10 && w.get(i).pair.size() > 0) {

                                p = new Paragraph(new Phrase(lineSpacing, "         PAGAMENTO(S) DE SALDO SALARIO BAIXADO(S) COM DIFERENCA DE VALOR ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                                document.add(p);
                                p = new Paragraph(new Phrase(lineSpacing, "               DIFERENCA: " + round(Math.abs(wageDif[0]), 2), FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                                document.add(p);
                                f1.setColor(BaseColor.RED);
                                p = new Paragraph(new Phrase(lineSpacing, "               " + w.get(i).entry.content, f1));
                                document.add(p);
                                for (int j = 0; j < w.get(i).pair.size(); j++) {
                                    String[] entryContent1 = w.get(i).pair.get(j).content.split(" ");
                                    List<String> l2 = Arrays.asList(entryContent1);
                                    if (!l2.contains(entryContent[0]) && j == 0) {
                                        p = new Paragraph(new Phrase(lineSpacing, "               VERIFICAR DATA DA(S) BAIXA(S)" + round(Math.abs(wageDif[0]), 2), FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                                        document.add(p);
                                    }
                                    p = new Paragraph(new Phrase(lineSpacing, "                 " + w.get(i).pair.get(j).content, f1));
                                    document.add(p);
                                }
                            } else if (w.get(i).entry.errorType == -1 && (wageDif[0] == 0 || (wageDif[0] != 0 && w.get(i).pair.isEmpty())) && Integer.parseInt(dt[0]) <= 10) {
                                p = new Paragraph(new Phrase(lineSpacing, "         PAGAMENTO(S) DE SALDO SALARIO NAO LANCADO(S) OU NAO BAIXADO(S)", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                                document.add(p);
                                f1.setColor(BaseColor.RED);
                                p = new Paragraph(new Phrase(lineSpacing, "               " + w.get(i).entry.content, f1));
                                document.add(p);
                            } else if (w.get(i).entry.errorType == 1 && wageDif[0] == 0 && Integer.parseInt(dt[0]) <= 10) {
                                p = new Paragraph(new Phrase(lineSpacing, "         PAGAMENTO(S) DE SALDO SALARIO BAIXADO(S) NA DATA ERRADA ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                                document.add(p);
                                f1.setColor(BaseColor.RED);
                                p = new Paragraph(new Phrase(lineSpacing, "               " + w.get(i).entry.content, f1));
                                document.add(p);
                                for (int j = 0; j < w.get(i).pair.size(); j++) {
                                    p = new Paragraph(new Phrase(lineSpacing, "                 " + w.get(i).pair.get(j).content, f1));
                                    document.add(p);
                                }
                            }

                        }
                        //System.out.println("wage: " + wage[1]);
                        //System.out.println("wageDif:  " + wageDif[1]);
                    }
                    
                    wageDif[0] = 0;
                    wage[0] = 0;
                     w = new ArrayList<>();

                    advFinder(g, c, wageDif, wage, w);
                    
                    for (int i = 0; i < w.size(); i++) {
                        if (w.get(i).entry.errorType != 0) {
                            if (!findWageProblem) {
                                p = new Paragraph(new Phrase(lineSpacing, "                ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                                document.add(p);
                                p = new Paragraph(new Phrase(lineSpacing, "     PAGAMENTO(S) FUNCIONARIO(S) ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                                document.add(p);
                                p = new Paragraph(new Phrase(lineSpacing, "                ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                                document.add(p);
                                findWageProblem = true;
                            }
                            String[] entryContent = w.get(i).entry.content.split(" ");
                            List<String> l = Arrays.asList(entryContent);
                            String[] dt = entryContent[0].split("/");
                            if (w.get(i).entry.errorType == -1 && wageDif[0] != 0 && Integer.parseInt(dt[0]) > 10 && w.get(i).pair.size() > 0) {
                                p = new Paragraph(new Phrase(lineSpacing, "         PAGAMENTO(S) DE ADTO. SALARIO BAIXADO(S) COM DIFERENCA DE VALOR ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                                document.add(p);
                                p = new Paragraph(new Phrase(lineSpacing, "               DIFERENCA: " + round(Math.abs(wageDif[0]), 2), FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                                document.add(p);
                                f1.setColor(BaseColor.RED);
                                p = new Paragraph(new Phrase(lineSpacing, "               " + w.get(i).entry.content, f1));
                                document.add(p);
                                for (int j = 0; j < w.get(i).pair.size(); j++) {
                                    String[] entryContent1 = w.get(i).pair.get(j).content.split(" ");
                                    List<String> l2 = Arrays.asList(entryContent1);
                                    if (!l2.contains(entryContent[0]) && j == 0) {
                                        p = new Paragraph(new Phrase(lineSpacing, "               VERIFICAR DATA DA(S) BAIXA(S)" + round(Math.abs(wageDif[0]), 2), FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                                        document.add(p);
                                    }
                                    p = new Paragraph(new Phrase(lineSpacing, "                 " + w.get(i).pair.get(j).content, f1));
                                    document.add(p);
                                }
                            } else if (w.get(i).entry.errorType == -1 && (wageDif[0] == 0 || (wageDif[0] != 0 && w.get(i).pair.isEmpty())) && Integer.parseInt(dt[0]) > 10) {
                                p = new Paragraph(new Phrase(lineSpacing, "         PAGAMENTO(S) DE ADTO. SALARIO NAO LANCADO(S) OU NAO BAIXADO(S)", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                                document.add(p);
                                f1.setColor(BaseColor.RED);
                                p = new Paragraph(new Phrase(lineSpacing, "               " + w.get(i).entry.content, f1));
                                document.add(p);
                            } else if (w.get(i).entry.errorType == 1 && wageDif[0] == 0 && Integer.parseInt(dt[0]) > 10) {
                                p = new Paragraph(new Phrase(lineSpacing, "         PAGAMENTO(S) DE ADTO. SALARIO BAIXADO(S) NA DATA ERRADA ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                                document.add(p);
                                f1.setColor(BaseColor.RED);
                                p = new Paragraph(new Phrase(lineSpacing, "               " + w.get(i).entry.content, f1));
                                document.add(p);
                                for (int j = 0; j < w.get(i).pair.size(); j++) {
                                    p = new Paragraph(new Phrase(lineSpacing, "                 " + w.get(i).pair.get(j).content, f1));
                                    document.add(p);
                                }
                            }

                            //System.out.println(w.get(i).entry.content + " "+  dt.length + " "+ dt[0]);
                            
                        }
                        //System.out.println("wage: " + wage[1]);
                        //System.out.println("wageDif:  " + wageDif[1]);
                    }
                }
                /////////////////////////////////////////////
                ///////     Verifica os salarios    ////////
                ///////////////////////////////////////////
                /////////////////////////////////////////////
                ///////verifica diferenca de debitos////////
                ///////////////////////////////////////////

                ArrayList<EntryPair> wrongDate = new ArrayList<>();
                ArrayList<Entry> notFoundC = new ArrayList<>();
                ArrayList<Entry> notFoundG = new ArrayList<>();

                for (int i = 0; i < c.debitEntry.size(); i++) {
                    if (c.debitEntry.get(i).errorType == -1) {
                        String[] entryContent = c.debitEntry.get(i).content.split(" ");
                        String v = entryContent[entryContent.length - 2].replace(".", "");
                        v = v.replace(",", ".");

                        if (v.charAt(v.length() - 1) == '0') {
                            v = v.substring(0, v.length() - 1);
                        }
                        //System.out.println(c.debitEntry.get(i).content + " " + c.debitEntry.get(i).errorType);
                        List<String> l = Arrays.asList(entryContent);

                        for (int j = 0; j < g.debitEntry.size(); j++) {
                            if (g.debitEntry.get(j).errorType == -1) {
                                String[] entryContentG = g.debitEntry.get(j).content.split(" ");
                                //System.out.println("  " + g.debitEntry.get(j).content + " " + g.debitEntry.get(j).errorType);
                                List<String> lG = Arrays.asList(entryContentG);
                                if (lG.contains(v)) {
                                    if (entryContentG[1].equals(entryContent[0])) {
                                        c.debitEntry.get(i).errorType = 0;
                                        g.debitEntry.get(j).errorType = 0;
                                        //System.out.println("  " + g.debitEntry.get(j).content + " " + g.debitEntry.get(j).errorType);
                                        //      System.out.println("268 " + c.debitEntry.get(i).content);
                                        //     System.out.println("269 " + g.debitEntry.get(j).content);
                                        break;
                                    } else {
                                        //     System.out.println("272 " + c.debitEntry.get(i).content);
                                        //     System.out.println("273 " + g.debitEntry.get(j).content);
                                        Entry e = new Entry(c.debitEntry.get(i).content);
                                        e.errorType = 1;
                                        EntryPair ep = new EntryPair(e);
                                        e = new Entry(g.debitEntry.get(j).content);
                                        e.errorType = 1;
                                        ep.pair.add(e);
                                        wrongDate.add(ep);
                                        c.debitEntry.get(i).errorType = 0;
                                        g.debitEntry.get(j).errorType = 0;
                                        break;
                                    }
                                }
                            }
                        }//for (int j = 0; j < g.debitEntry.size(); j++) {
                    }//if (c.debitEntry.get(i).errorType == -1)
                    if (c.debitEntry.get(i).errorType == -1) {
                        notFoundC.add(c.debitEntry.get(i));
                    }

                }//for (int i = 0; i < c.debitEntry.size(); i++) 
                System.out.println(g.name + " G");
                for (int j = 0; j < g.debitEntry.size(); j++) {
                    if (g.debitEntry.get(j).errorType == -1) {
                        System.out.println(g.debitEntry.get(j).content);
                        notFoundG.add(g.debitEntry.get(j));
                    }
                }
                System.out.println(g.name + " C");
                for (int j = 0; j < c.debitEntry.size(); j++) {
                    if (c.debitEntry.get(j).errorType == -1) {
                        System.out.println(c.debitEntry.get(j).content);
                        //notFoundG.add(g.debitEntry.get(j));
                    }
                }
                ArrayList<EntryPair> seq = new ArrayList<>();
                Entry e;
                EntryPair ep;
                String seqAnt = "";
                Double sumSeq = 0.0;
                ArrayList<Entry> temp = new ArrayList<>();

                //PROCURA CASOS 1 PARA N
                for (int j = 0; j < notFoundG.size(); j++) {
                    if (j == 0) {
                        e = new Entry(notFoundG.get(j).content);
                        String[] entryContentG = notFoundG.get(j).content.split(" ");
                        sumSeq = sumSeq + round(Double.parseDouble(entryContentG[entryContentG.length - 3]), 2);
                        seqAnt = entryContentG[0];
                        temp.add(e);
                        notFoundG.get(j).errorType = 0;
                        if(notFoundG.size()==1){
                            ep = new EntryPair(new Entry(Double.toString(round(sumSeq, 2))));
                             ep.pair.addAll(temp);
                             seq.add(ep);
                             break;
                        }
                        else
                            continue;
                    }
                    String[] entryContentG = notFoundG.get(j).content.split(" ");
                    if (Integer.parseInt(entryContentG[0]) == (Integer.parseInt(seqAnt) + 1)) {
                        seqAnt = entryContentG[0];
                        if (temp.size() == 1) {
                            notFoundG.get(j - 1).errorType = 0;
                        }
                        e = new Entry(notFoundG.get(j).content);
                        sumSeq = sumSeq + round(Double.parseDouble(entryContentG[entryContentG.length - 3]), 2);
                        temp.add(e);
                        notFoundG.get(j).errorType = 0;
                        // System.out.println("YEP");
                        if (j == notFoundG.size() - 1) {
                            ep = new EntryPair(new Entry(Double.toString(round(sumSeq, 2))));
                            ep.pair.addAll(temp);
                            if (temp.size() > 1) {
                                seq.add(ep);
                                notFoundG.get(j).errorType = 0;

                            }

                            //System.out.println(ep.entry.content + " " + temp.size() + " FIM");
                        }
                    } else {

                        ep = new EntryPair(new Entry(Double.toString(round(sumSeq, 2))));
                        ep.pair.addAll(temp);
                        List<String> l = Arrays.asList(temp.get(0).content.split(" "));
                        
                            notFoundG.get(j).errorType = 0;
                            seq.add(ep);
                        

                        temp.clear();
                        sumSeq = 0.0;
                        sumSeq = sumSeq + round(Double.parseDouble(entryContentG[entryContentG.length - 3]), 2);
                        e = new Entry(notFoundG.get(j).content);
                        temp.add(e);
                        seqAnt = entryContentG[0];
                        //System.out.println(ep.entry.content + " " + temp.size() + " NOPE");
                    }

                }

                //VERIFICA CASOS 1 PARA N    
                for (int i = 0; i < notFoundC.size(); i++) {
                    String[] entryContent = notFoundC.get(i).content.split(" ");
                    String v = entryContent[entryContent.length - 2].replace(".", "");
                    v = v.replace(",", ".");
                   
                    if (v.charAt(v.length() - 1) == '0') {
                        v = v.substring(0, v.length() - 1);
                    }
                    for (int j = 0; j < seq.size(); j++) {
                        // System.out.println(v+" "+seq.get(j).entry);
                        if (v.equals(seq.get(j).entry.content)) {
                            String[] entryContentG = seq.get(j).pair.get(0).content.split(" ");
                            if (entryContent[0].equals(entryContentG[1])) {
                                notFoundC.get(i).errorType = 0;
                                seq.get(j).entry.errorType = 0;
                            } else {
                                notFoundC.get(i).errorType = 1;
                                seq.get(j).entry.errorType = 1;
                            }
                            seq.get(j).entry.content = notFoundC.get(i).content;
                        }

                    }

                }

                boolean findWrongPay = false;
                f1.setColor(BaseColor.GRAY);
                for (int i = 0; i < wrongDate.size(); i++) {
                    if (!findWrongPay) {
                        p = new Paragraph(new Phrase(lineSpacing, "                ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                        document.add(p);
                        p = new Paragraph(new Phrase(lineSpacing, "     PAGAMENTO(S)", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                        document.add(p);
                        findWrongPay = true;
                        p = new Paragraph(new Phrase(lineSpacing, "                ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                        document.add(p);
                        p = new Paragraph(new Phrase(lineSpacing, "         PAGAMENTO(S) BAIXADO(S) NA DATA ERRADA", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                        document.add(p);
                    }

                    p = new Paragraph(new Phrase(lineSpacing, "               " + wrongDate.get(i).entry.content, f1));
                    document.add(p);
                    for (int j = 0; j < wrongDate.get(i).pair.size(); j++) {
                        p = new Paragraph(new Phrase(lineSpacing, "                     " + wrongDate.get(i).pair.get(j).content, f1));
                        document.add(p);
                    }
                }//for (int i = 0; i < wrongDate.size(); i++)

                for (int i = 0; i < seq.size(); i++) {
                    if (seq.get(i).entry.errorType == 1) {
                        if (!findWrongPay) {
                            p = new Paragraph(new Phrase(lineSpacing, "                ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                            document.add(p);
                            p = new Paragraph(new Phrase(lineSpacing, "     PAGAMENTO(S)", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                            document.add(p);
                            findWrongPay = true;
                            p = new Paragraph(new Phrase(lineSpacing, "                ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                            document.add(p);
                            p = new Paragraph(new Phrase(lineSpacing, "         PAGAMENTO(S) BAIXADO(S) NA DATA ERRADA", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                            document.add(p);
                        }

                        p = new Paragraph(new Phrase(lineSpacing, "               " + seq.get(i).entry.content, f1));
                        document.add(p);
                        for (int j = 0; j < seq.get(i).pair.size(); j++) {
                            p = new Paragraph(new Phrase(lineSpacing, "                     " + seq.get(i).pair.get(j).content, f1));
                            document.add(p);
                        }
                    }
                }//for (int i = 0; i < wrongDate.size(); i++)

                boolean findBankProblem = false;
                f1.setColor(BaseColor.RED);
                for (int i = 0; i < notFoundC.size(); i++) {
                    if (notFoundC.get(i).errorType == -1) {
                        if (!findWrongPay) {
                            p = new Paragraph(new Phrase(lineSpacing, "                ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                            document.add(p);
                            p = new Paragraph(new Phrase(lineSpacing, "     PAGAMENTO(S)", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                            document.add(p);
                            findWrongPay = true;
                            findBankProblem = true;
                            p = new Paragraph(new Phrase(lineSpacing, "                ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                            document.add(p);
                            p = new Paragraph(new Phrase(lineSpacing, "         BANCO: PAGAMENTO(S) NAO LANCADO(S) OU NAO BAIXADO(S)", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                            document.add(p);
                        }
                        if (!findBankProblem) {
                            p = new Paragraph(new Phrase(lineSpacing, "                ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                            document.add(p);
                            p = new Paragraph(new Phrase(lineSpacing, "         BANCO: PAGAMENTO(S) NAO LANCADO(S) OU NAO BAIXADO(S)", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                            document.add(p);
                            findBankProblem = true;
                        }

                        String[] entryContent = notFoundC.get(i).content.split(" ");
                        String v = entryContent[entryContent.length - 2].replace(".", "");
                        v = v.replace(",", ".");
                       
                        if (v.charAt(v.length() - 1) == '0') {
                            v = v.substring(0, v.length() - 1);
                        }
                        List<String> l = Arrays.asList(entryContent);
                        if (l.contains("CARTAO") && l.contains("CREDITO")) {
                            p = new Paragraph(new Phrase(lineSpacing, "               " + notFoundC.get(i).content, f1));
                            document.add(p);
                            for (int j = 0; j < seq.size(); j++) {
                                if (seq.get(j).entry.errorType == -1) {
                                    double dif = (Double.parseDouble(v) - Double.parseDouble(seq.get(j).entry.content));
                                    if (dif < 35 && dif > 0) {
                                        boolean manut = true;
                                        for (int k = 0; k < seq.get(j).pair.size(); k++) {
                                            String[] entryContentG = seq.get(j).pair.get(k).content.split(" ");
                                            List<String> lg = Arrays.asList(entryContentG);
                                            if (!lg.contains("00000205")) {

                                                manut = false;
                                                break;
                                            }
                                            seq.get(j).entry.errorType = 0;
                                            if (k == 0) {
                                                p = new Paragraph(new Phrase(lineSpacing, "                   GOSOFT: ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                                                document.add(p);
                                            }
                                            p = new Paragraph(new Phrase(lineSpacing, "                   " + seq.get(j).pair.get(k).content, f1));
                                            document.add(p);
                                        }
                                        if (manut) {
                                            p = new Paragraph(new Phrase(lineSpacing, "                   DIFERENCA: " + round(dif, 2) + " ANUIDADE? ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                                            document.add(p);
                                            p = new Paragraph(new Phrase(lineSpacing, "                   ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                                            document.add(p);
                                        }

                                    }

                                }
                            }

                            continue;
                        }

                        p = new Paragraph(new Phrase(lineSpacing, "               " + notFoundC.get(i).content, f1));
                        document.add(p);
                    }
                }

                boolean findGosoftProblem = false;
                f1.setColor(BaseColor.RED);
                for (int i = 0; i < notFoundG.size(); i++) {
                    if (notFoundG.get(i).errorType == -1) {
                        if (!findWrongPay) {
                            p = new Paragraph(new Phrase(lineSpacing, "                ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                            document.add(p);
                            p = new Paragraph(new Phrase(lineSpacing, "     PAGAMENTO(S)", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                            document.add(p);
                            findWrongPay = true;
                            findGosoftProblem = true;
                            p = new Paragraph(new Phrase(lineSpacing, "                ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                            document.add(p);
                            p = new Paragraph(new Phrase(lineSpacing, "         GOSOFT: PAGAMENTO(S) NAO ENCONTRADO(S)", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                            document.add(p);
                        }
                        if (!findGosoftProblem) {
                            p = new Paragraph(new Phrase(lineSpacing, "                ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                            document.add(p);
                            p = new Paragraph(new Phrase(lineSpacing, "         GOSOFT: PAGAMENTO(S) NAO ENCONTRADO(S)", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                            document.add(p);
                            findGosoftProblem = true;
                        }
                        p = new Paragraph(new Phrase(lineSpacing, "               " + notFoundG.get(i).content, f1));
                        document.add(p);
                    }
                }
                for (int i = 0; i < seq.size(); i++) {
                    if (seq.get(i).entry.errorType == -1) {
                        if (!findWrongPay) {
                            p = new Paragraph(new Phrase(lineSpacing, "                ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                            document.add(p);
                            p = new Paragraph(new Phrase(lineSpacing, "     PAGAMENTO(S)", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                            document.add(p);
                            findWrongPay = true;
                            findGosoftProblem = true;
                            p = new Paragraph(new Phrase(lineSpacing, "                ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                            document.add(p);
                            p = new Paragraph(new Phrase(lineSpacing, "         GOSOFT: PAGAMENTO(S) NAO ENCONTRADO(S)", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                            document.add(p);
                        }
                        if (!findGosoftProblem) {
                            p = new Paragraph(new Phrase(lineSpacing, "                ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                            document.add(p);
                            p = new Paragraph(new Phrase(lineSpacing, "         GOSOFT: PAGAMENTO(S) NAO ENCONTRADO(S)", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                            document.add(p);
                            findGosoftProblem = true;
                        }
                        for (int j = 0; j < seq.get(i).pair.size(); j++) {
                            p = new Paragraph(new Phrase(lineSpacing, "               " + seq.get(i).pair.get(j).content, f1));
                            document.add(p);
                        }
                    }
                }
                p = new Paragraph(new Phrase(lineSpacing, "                ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                document.add(p);
                p = new Paragraph(new Phrase(lineSpacing, "     NOTAS(S)", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                document.add(p);
                 p = new Paragraph(new Phrase(lineSpacing, "                ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                document.add(p);
                  if(findDifCredit){
                    p = new Paragraph(new Phrase(lineSpacing, "         *Diferenca entre a soma de todos os creditos encontrados no extrato e todos os creditos encontrados no gosoft", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                    document.add(p);
                }


            } catch (DocumentException de) {
                System.err.println(de.getMessage());
            } catch (IOException ioe) {
                System.err.println(ioe.getMessage());
            }
            document.close();

        }// for index<condKeysG.length

    }
}
