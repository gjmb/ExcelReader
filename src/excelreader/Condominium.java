/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package excelreader;

import java.util.ArrayList;

/**
 *
 * @author gabri
 */

public class Condominium {
  public String name = "";
  public ArrayList <String> taxDate = new ArrayList<>();
  public ArrayList <String> taxValue = new ArrayList<>();
  public ArrayList <String> appRescDate = new ArrayList<>();
  public ArrayList <String> appRescValue = new ArrayList<>();
  
  //TODO: criar estrutura para mapear valor com o conteudo
  public ArrayList <Entry> debitEntry = new ArrayList<>();
  public ArrayList <Entry> creditEntry = new ArrayList<>();

  public Condominium(String n){
    this.name=n;
  }

}