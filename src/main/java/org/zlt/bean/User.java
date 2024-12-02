package org.zlt.bean;

public class User {
    private String name;
    private String sex;

    private String national;
    private String address;
    private String birth;
    private String idCard;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getNational() {
        return national;
    }

    public void setNational(String national) {
        this.national = national;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBirth() {
        return birth;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }
    @Override
    public String toString() {
        return "{" +
                "\"name\": \"" + name + "\"," +
                "\"sex\": \"" + sex + "\"," +
                "\"national\": \"" + national + "\"," +
                "\"address\": \"" + address + "\"," +
                "\"birth\": \"" + birth + "\"," +
                "\"idCard\": \"" + idCard + "\"" +
                "}";
    }
}
