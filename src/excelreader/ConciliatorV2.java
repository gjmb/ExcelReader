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
            if (l.contains("CHEQUE")) {
                EntryPair ep = new EntryPair(new Entry(c.debitEntry.get(i).content));
                p.add(ep);
                c.debitEntry.remove(c.debitEntry.get(i));
                // System.out.println(c.name);
                // System.out.println(ep.entry.content);
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
                                g.debitEntry.get(j).errorType = 1;
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

    public static void wageTotal(Condominium c, double[] wage, String[] dtW, String[] dtA, ArrayList<EntryPair> p) {
        String[] w = null;
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
                if (Integer.parseInt(dt[0]) <= 10) {
                    dtW = dt;
                    w = entryContent;
                    wage[0] = round(round(wage[0],2) + round(Double.parseDouble(v), 2),2);
                } else {
                    dtA = dt;
                    a = entryContent;
                    wage[1] = round(round(wage[1],2) + round(Double.parseDouble(v), 2),2);
                }
            } // if l.contains("PAGAMENTO") && l.contains("FUNCIONARIOS")
        } // for int i = 0; i < c.debitEntry.size(); i++
        if(w==null)
            return;
        w[w.length - 2] = Double.toString(wage[0]);
        a[a.length - 2] = Double.toString(wage[1]);
        String wContent = "";
        String aContent = "";
        for (int i = 0; i < w.length; i++) {
            wContent = wContent + w[i] + " ";
        }
        for (int i = 0; i < a.length; i++) {
            aContent = aContent + a[i] + " ";
        }
        p.add(new EntryPair(new Entry(wContent)));
        p.add(new EntryPair(new Entry(aContent)));

    } // wageTotal

    public static void wageFinder(CondominiumG g, Condominium c, double[] wageDif, double[] wage, ArrayList<EntryPair> p) {
        String[] dtW = null;
        String[] dtA = null;
       
        //Busca por pagamentos individuais 
        for (int i = 0; i < g.debitEntry.size(); i++) {
            String[] entryContentG = g.debitEntry.get(i).content.split(" ");
            List<String> l = Arrays.asList(entryContentG);
            if (l.contains("SALDO") && l.contains("00000167") && g.debitEntry.get(i).errorType==-1) {
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
                            ep.entry.errorType = 0;
                            Entry e = new Entry(g.debitEntry.get(i).content);
                            e.errorType = 0;
                            ep.pair.add(e);
                            p.add(ep);
                            g.debitEntry.get(i).errorType=0;
                            debitEntry.errorType=0;
                            break;
                        }
                    }// if vG.equals(v)   
                }// for Entry debitEntry : c.debitEntry
            }//if l.contains("SALDO") && l.contains("00000167")
        } // for (int i = 0; i < g.debitEntry.size(); i++)  
        wageTotal(c, wage, dtW, dtA, p);
        if(p.size()==0)
            return;
        if(wage[0]==0.0){
            p.removeAll(p);
            return;
        }
        EntryPair ep=null;
        //TODO: ARRUMAR 
        if(p.size()==1)
        ep = p.get(0);
        else
        ep=p.get(1);
        ep.entry.errorType = 0;
        String[] entryContent = ep.entry.content.split(" ");
        String v = entryContent[entryContent.length - 2].replace(".", "");
                    v = v.replace(",", ".");
                    if (v.charAt(v.length() - 1) == '0') {
                        v = v.substring(0, v.length() - 1);
                    }
        double sum = round(Double.parseDouble(v), 2);
        for (int i = 0; i < g.debitEntry.size(); i++) {
            String[] entryContentG = g.debitEntry.get(i).content.split(" ");
            List<String> l = Arrays.asList(entryContentG);
            if (l.contains("SALDO") && l.contains("00000167")&& g.debitEntry.get(i).errorType==-1) {
                String vG = entryContentG[entryContentG.length - 3];
                wageDif[0] = round(round(wageDif[0],2) + round(Double.parseDouble(vG), 2),2);
                Entry e = new Entry(g.debitEntry.get(i).content);
                e.errorType = 0;
                ep.pair.add(e);
            }
        }// for (int i = 0; i < g.debitEntry.size(); i++)
        wageDif[0] = round(round(wageDif[0],2) - round(sum, 2),2);
    }// public static void wageFinder
    //TODO: TESTAR wageFinder

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
                /////////////////////////////////////////////
                ///////verifica diferenca de credito////////
                ///////////////////////////////////////////
                double tg = creditTotalG(g);
                double t = creditTotal(c);
                if (tg > t) {
                    p = new Paragraph(new Phrase(lineSpacing, "     DIFERENCA DE CREDITO ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
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
                            p = new Paragraph(new Phrase(lineSpacing, "                 CREDITO DE MESMO VALOR: " + g.creditEntry.get(i).content, f1));
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
                            p = new Paragraph(new Phrase(lineSpacing, "                 DEBITO DE MESMO VALOR: " + g.debitEntry.get(i).content, f1));
                            document.add(p);

                        }
                    }

                } else if (t > tg) {
                    p = new Paragraph(new Phrase(lineSpacing, "     DIFERENCA DE CREDITO ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
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
                            p = new Paragraph(new Phrase(lineSpacing, "                 CREDITO DE MESMO VALOR: " + c.creditEntry.get(i).content, f1));
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
                            p = new Paragraph(new Phrase(lineSpacing, "                 DEBITOS DE MESMO VALOR: " + c.debitEntry.get(i).content, f1));
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
                if (checkPair.size() > 0) {
                    p = new Paragraph(new Phrase(lineSpacing, "     CHEQUES ", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
                    document.add(p);
                }
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
                                       // System.out.println("v1 " + v1);
                                     //   System.out.println("vC " + vC);
                                        Font f1 = FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize);
                                        f1.setColor(BaseColor.RED);
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

                                p = new Paragraph(new Phrase(lineSpacing, "         CHEQUE COMPENSADO A MENOR", FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize)));
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
                                        Font f1 = FontFactory.getFont(FontFactory.TIMES_ROMAN, fntSize);
                                        f1.setColor(BaseColor.RED);
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
            double[] wageDif = {0.0, 0.0};
            double[] wage = {0.0, 0.0};
            ArrayList<EntryPair> w=new ArrayList<>();
            
            wageFinder(g, c, wageDif, wage, w);
            
            System.out.println("wage: "+wage[0]);
            System.out.println("wageDif:  "+wageDif[0]);
            
            for(int i = 0; i<w.size();i++){
               System.out.println(w.get(i).entry.content);
                for (int j = 0; j < w.get(i).pair.size(); j++) {
                           System.out.println("     "+w.get(i).pair.get(j).content);
                        }               
            }
            
            
            
            
            /////////////////////////////////////////////
            ///////     Verifica os salarios    ////////
            ///////////////////////////////////////////
            
            
            } catch (DocumentException de) {
                System.err.println(de.getMessage());
            } catch (IOException ioe) {
                System.err.println(ioe.getMessage());
            }
            document.close();

        }// for index<condKeysG.length

    }
}
