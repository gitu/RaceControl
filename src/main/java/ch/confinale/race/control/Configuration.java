package ch.confinale.race.control;


public final class Configuration { 
    private String firebaseName;
    private String comPort;
    private String username;
    private String password;
    private Boolean provideOwnRXTX;

    public String getFirebaseName() {
        return firebaseName;
    }

    public void setFirebaseName(String firebaseName) {
        this.firebaseName = firebaseName;
    }

    public String getComPort() {
        return comPort;
    }

    public void setComPort(String comPort) {
        this.comPort = comPort;
    }


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



    public Boolean getProvideOwnRXTX() {
        return provideOwnRXTX;
    }

    public void setProvideOwnRXTX(Boolean provideOwnRXTX) {
        this.provideOwnRXTX = provideOwnRXTX;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "firebaseName='" + firebaseName + '\'' +
                ", comPort='" + comPort + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", provideOwnRXTX=" + provideOwnRXTX +
                '}';
    }
}