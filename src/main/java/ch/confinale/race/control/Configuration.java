package ch.confinale.race.control;


public final class Configuration { 
    private String firebaseName;
    private String comPort;

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

    @Override
    public String toString() {
        return "Configuration{" +
                "firebaseName='" + firebaseName + '\'' +
                ", comPort='" + comPort + '\'' +
                '}';
    }
}