package com.august.authenticate.entity;

import jakarta.persistence.*;
import lombok.*;

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
