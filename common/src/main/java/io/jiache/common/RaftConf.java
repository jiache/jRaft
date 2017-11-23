package io.jiache.common;

import java.util.List;

public class RaftConf {
    private String token;
    private List<Address> addressList;
    private Integer leaderIndex;
    private List<Address> secretaryAddressList;
    private Address agentAddress;

    public RaftConf() {
    }

    public RaftConf(String token, List<Address> addressList, Integer leaderIndex, List<Address> secretaryAddressList, Address agentAddress) {
        this.token = token;
        this.addressList = addressList;
        this.leaderIndex = leaderIndex;
        this.secretaryAddressList = secretaryAddressList;
        this.agentAddress = agentAddress;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<Address> getAddressList() {
        return addressList;
    }

    public void setAddressList(List<Address> addressList) {
        this.addressList = addressList;
    }

    public Integer getLeaderIndex() {
        return leaderIndex;
    }

    public void setLeaderIndex(Integer leaderIndex) {
        this.leaderIndex = leaderIndex;
    }

    public List<Address> getSecretaryAddressList() {
        return secretaryAddressList;
    }

    public void setSecretaryAddressList(List<Address> secretaryAddressList) {
        this.secretaryAddressList = secretaryAddressList;
    }

    public Address getAgentAddress() {
        return agentAddress;
    }

    public void setAgentAddress(Address agentAddress) {
        this.agentAddress = agentAddress;
    }
}
