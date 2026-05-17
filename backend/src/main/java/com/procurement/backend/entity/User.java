package com.procurement.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    private String id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String role;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "department_id")
    private String departmentId;

    public User() {}

    public User(String id, String email, String role, String name, String departmentId) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.name = name;
        this.departmentId = departmentId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }
}
