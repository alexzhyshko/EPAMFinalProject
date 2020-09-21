package main.java.dto;

import java.time.LocalDateTime;

public class Order {

	public int id;
	public Route route;
	public float price;
	public User customer;
	public Car car;
	public Driver driver;
	public int timeToArrival;
	public LocalDateTime dateOfOrder;
	public String status;
	public int statusid;
}
