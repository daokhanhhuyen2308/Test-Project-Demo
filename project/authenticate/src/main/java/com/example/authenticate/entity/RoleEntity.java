package com.example.authenticate.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleEntity {
        @Id
        private String name;
        private String description;

        @ManyToMany(
                mappedBy = "roles",
                cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
        @ToString.Exclude
        private Set<UserEntity> users;
}
