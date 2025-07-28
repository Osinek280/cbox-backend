package com.cbox.cbox.services;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StudentCsvRepresentation {

  @CsvBindByName(column = "firstname")
  private String firstname;
  @CsvBindByName(column = "lastname")
  private String lastname;
  @CsvBindByName(column = "age")
  private int age;
}
