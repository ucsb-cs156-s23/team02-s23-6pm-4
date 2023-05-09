package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Painting;

import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PaintingRepository extends CrudRepository<Painting, String> {
 
}