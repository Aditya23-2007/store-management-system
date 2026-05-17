package com.procurement.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "vendors")
public class Vendor {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    private String contact;
    private String email;
    
    @Column(name = "gst_number")
    private String gstNumber;
    
    @Column(nullable = false)
    private Double rating;

    public Vendor() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
}
