package ru.timur.project.Hubr.enums;

public enum Country {
    NAURU("Nauru"),
    NEPAL("Nepal"),
    NEWFOUNDLAND("Newfoundland"),
    NETHERLANDS("Netherlands"),
    NEW_ZEALAND("New Zealand"),
    NICARAGUA("Nicaragua"),
    NIGER("Niger"),
    NIGERIA("Nigeria"),
    NORWAY("Norway"),
    OMAN("Oman"),
    OTTOMAN_EMPIRE("Ottoman Empire"),
    PAKISTAN("Pakistan"),
    PANAMA("Panama"),
    PAPUA_NEW_GUINEA("Papua New Guinea"),
    PARAGUAY("Paraguay"),
    PERU("Peru"),
    PHILIPPINES("Philippines"),
    POLAND("Poland"),
    PORTUGAL("Portugal"),
    PRUSSIA("Prussia"),
    QATAR("Qatar"),
    ROMANIA("Romania"),
    ROME("Rome"),
    RUSSIAN_FEDERATION("Russian Federation");

    private String country;

    Country(String country) {
        this.country = country;
    }

    public String getCountry() {
        return country;
    }
}
