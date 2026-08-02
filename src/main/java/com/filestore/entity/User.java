package com.filestore.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CollectionId;
import tools.jackson.databind.node.StringNode;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Full Named Required")
    @Column(name = "full_name", nullable = false)
    private String fullName ;

    @Email(message = "Please provide a valid email")
    @NotBlank(message = "Email is required")
    @Column(name = "email",nullable = false,unique = true)
    private String email;

    @NotBlank(message = "Password is required")
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "created_at",updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate(){
        updatedAt = LocalDateTime.now();
    }



}
