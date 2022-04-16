package br.furb.ariel.middleware.healthcheck;

public class HealthCheckDTO {

    private boolean ok;

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }
}
