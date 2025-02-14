package org.proj3.currency_exchange.dto;

public class CurrencyRequestDto {
    private String code;
    private String name;
    private String sign;

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

    public String getSign() {
        return this.sign;
    }
}
