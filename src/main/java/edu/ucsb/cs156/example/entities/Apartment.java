package edu.ucsb.cs156.example.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "apartment")
public class Apartment {
  @Id
  private String code;
  private String name;
  private String address;
  private String city;
  private String state;
  private int rooms;
  private String description;
}