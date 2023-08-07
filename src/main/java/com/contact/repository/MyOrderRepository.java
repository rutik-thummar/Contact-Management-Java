package com.contact.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.contact.model.MyOrder;

@Repository
public interface MyOrderRepository extends JpaRepository<MyOrder, Integer> {

	@Query(value = "select * from my_order  where order_id= :orderId", nativeQuery = true)
	MyOrder fingByOrderId(@Param("orderId") String orderId);
}
