package edu.fudan.main.domain;

import org.neo4j.ogm.annotation.*;


@NodeEntity
public abstract class User {

    @Id
    private Long userId;

    @Property
    private String name;

    @Property
    private String password;

    @Property@Index(unique = true)
    private String email;

    @Property
    private UserType type;

    public User(Long id, String name, String password, String email, UserType type) {
        this.userId = id;
        this.name = name;
        this.password = password;
        this.email = email;
        this.type = type;
    }

    public User() {

    }

    public UserType getType() {
        return type;
    }

    public void setType(UserType type) {
        this.type = type;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getId() {
        return userId;
    }

    public void setId(Long id) {
        this.userId = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        if (this.userId.equals(((User) o).userId)) return true;
        return false;
    }
}