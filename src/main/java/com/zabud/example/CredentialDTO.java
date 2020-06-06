/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package com.zabud.example;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CredentialDTO implements Serializable {

  private static final long serialVersionUID = -3702606787696787882L;
  private String nit;
  private String regimen;
  private String token;
  private String secondaryToken;
  private String status;
  
}
