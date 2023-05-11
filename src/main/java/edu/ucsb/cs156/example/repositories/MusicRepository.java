package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Music;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MusicRepository extends CrudRepository<Music, Long> {
 
}