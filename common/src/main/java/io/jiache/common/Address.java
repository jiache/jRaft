package io.jiache.common;

import io.jiache.util.Assert;

/**
 * Created by jiacheng on 17-9-24.
 */
public class Address {
    private String host;
    private int port;

    public Address() {
    }

    public Address(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Address(String address) {
        Assert.checkNull(address, "address");
        String[] components = address.split(":");
        Assert.check(components.length == 2, "address must be address:port");
        this.host = components[0];
        this.port = Integer.parseInt(components[1]);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Address address = (Address) o;

        if (port != address.port) return false;
        return host != null ? host.equals(address.host) : address.host == null;
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return "Address{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
