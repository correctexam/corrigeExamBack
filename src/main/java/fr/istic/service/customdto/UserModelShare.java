package fr.istic.service.customdto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class UserModelShare {
    private String name;


    private String firstname;
    private String login;

    public UserModelShare(){}

    public UserModelShare(String name, String firstname, String login) {
        this.name = name;
        this.firstname = firstname;
        this.login = login;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getFirstname() {
        return firstname;
    }
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }
    public String getLogin() {
        return login;
    }
    public void setLogin(String login) {
        this.login = login;
    }

    @Override
    public String toString() {
        return "UserModelShare [firstname=" + firstname + ", login=" + login + ", name=" + name + "]";
    }

}
