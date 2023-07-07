package com.cl.common_base.widget.waterview.bean;

/**
 * 创建时间: 2018/1/9
 * 创建人:  赖天兵
 * 描述:
 */

public class Water {
    private String number;
    private String name;

    private String loseEfficacy;

    private String orderNo;

    private String oxygen;

    private String tips;

    public Water(String number, String name, String loseEfficacy, String orderNo, String oxygen, String tips) {
        this.number = number;
        this.name = name;
        this.loseEfficacy = loseEfficacy;
        this.orderNo = orderNo;
        this.oxygen = oxygen;
        this.tips = tips;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLoseEfficacy() {
        return loseEfficacy;
    }

    public void setLoseEfficacy(String loseEfficacy) {
        this.loseEfficacy = loseEfficacy;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getOxygen() {
        return oxygen;
    }

    public void setOxygen(String oxygen) {
        this.oxygen = oxygen;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public String getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Water water = (Water) o;

        if (!number.equals(water.number)) {
            return false;
        }
        return name.equals(water.name);
    }

    @Override
    public int hashCode() {
        int result = number.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
