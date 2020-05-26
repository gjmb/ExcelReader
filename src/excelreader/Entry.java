package excelreader;

public class Entry{
  public String content ="";
  //-1: PAGAMENTO NÃO ENCONTRADO
  //0 : ENCONTRADO
  //1 : DATA DE BAIXA ERRADA
  public int errorType=-1;
  public Entry(String content){
    this.content=content;
  }
}
