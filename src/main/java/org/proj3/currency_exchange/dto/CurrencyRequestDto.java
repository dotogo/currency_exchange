package org.proj3.currency_exchange.dto;

public class CurrencyRequestDto {
    private String code;
    private String name;
    private String sign;

    public CurrencyRequestDto() {

    }

    public CurrencyRequestDto(String code, String name, String sign) {
        this.code = code;
        this.name = name;
        this.sign = sign;
    }

    public void setCode(String code) {
        this.code = code;
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
