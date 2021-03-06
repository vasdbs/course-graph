package edu.fudan.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import edu.fudan.exception.IllegalUserTypeException;

public enum UserType {
    STUDENT("STUDENT"),
    TEACHER("TEACHER");

    private String text;

    UserType(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return this.text;
    }

    @JsonCreator
    public static UserType fromText(String text) {
        for (UserType t : UserType.values()) {
            if(t.toString().equals(text)) {
                return t;
            }
        }
        throw new IllegalUserTypeException();
    }
}
