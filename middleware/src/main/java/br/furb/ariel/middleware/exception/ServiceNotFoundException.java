package br.furb.ariel.middleware.exception;

public class ServiceNotFoundException extends MiddlewareException {

    private final String route;

    public ServiceNotFoundException(String route) {
        super("Service not found by route " + route);
        this.route = route;
    }

    public String getRoute() {
        return route;
    }
}
