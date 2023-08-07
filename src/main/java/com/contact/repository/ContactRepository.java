package com.contact.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.contact.model.Contact;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Integer> {
	@Modifying
	@Query(value = "select * from contact  where user_id= :userId", nativeQuery = true)
	List<Contact> findContactsByUser(@Param("userId") int userId);
}