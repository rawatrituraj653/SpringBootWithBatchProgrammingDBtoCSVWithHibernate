package com.st.model;



import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;


@Data
@Entity
public class Travel {
	
	@Id
	private String flightId;
	private String flightName;
	private String pilotName;
	private String agentId;
	private  double ticketCost;
	private double discount;
	private double gst;
	private double finalAmount;
}

