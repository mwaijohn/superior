package superior.com.superior.database;

public class Logins {
    String username;
    String password;
    String email;
    String location;
    String loc_code;

    public Logins(String username, String password, String email, String location, String loc_code) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.location = location;
        this.loc_code = loc_code;
    }

    public Logins(){}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLoc_code() {
        return loc_code;
    }

    public void setLoc_code(String loc_code) {
        this.loc_code = loc_code;
    }
}
