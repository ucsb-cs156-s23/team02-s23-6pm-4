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
@Entity(name = "restaurant")
public class Restaurant {
  @Id
  private String code;
  private String name;  
  private String descript;
  private String yelp_rating;
}
