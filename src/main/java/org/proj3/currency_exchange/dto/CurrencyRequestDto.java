package org.proj3.currency_exchange.dto;

public class CurrencyRequestDto {
    private String code;
    private String fullName;
    private String sign;

    public void setCode(String code) {
        this.code = code;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getCode() {
        return this.code;
    }

    public String getFullName() {
        return this.fullName;
    }

    public String getSign() {
        return this.sign;
    }
}
