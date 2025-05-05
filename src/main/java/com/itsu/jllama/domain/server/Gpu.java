package com.itsu.jllama.domain.server;

public class Gpu {

    private String name;

    private double vRam;

    private String version;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getvRam() {
        return vRam;
    }

    public void setvRam(double vRam) {
        this.vRam = vRam;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
