package fr.istic.service.customdto;

import io.quarkus.runtime.annotations.RegisterForReflection;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection
@Schema(description = "Brief user information used when sharing a resource.")
public class UserModelShare {
    @Schema(description = "Last name of the user.")
    private String name;


    @Schema(description = "First name of the user.")
    private String firstname;
    @Schema(description = "Login of the user.")
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
