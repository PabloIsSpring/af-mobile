package com.facens.afmobile.model;

public class Visita {

    private String id;
    private String titulo;
    private String descricao;
    private String data;
    private String categoria;
    private boolean favorito;
    private double latitude;
    private double longitude;
    private String temperatura;
    private int codigoClima;
    private String condicaoTempo;

    public Visita () {
    }

    public Visita (String id, String titulo, String decricao, String data, String categoria, boolean favorito,
                   double latitude, double longitude, String temperatura, int codigoClima, String condicaoTempo) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = decricao;
        this.data = data;
        this.categoria = categoria;
        this.favorito = favorito;
        this.latitude = latitude;
        this.longitude = longitude;
        this.temperatura = temperatura;
        this.codigoClima = codigoClima;
        this.condicaoTempo = condicaoTempo;
    }

    public Visita (String titulo, String decricao, String data, String categoria, boolean favorito,
                   double latitude, double longitude, String temperatura, int codigoClima, String condicaoTempo) {
        this.titulo = titulo;
        this.descricao = decricao;
        this.data = data;
        this.categoria = categoria;
        this.favorito = favorito;
        this.latitude = latitude;
        this.longitude = longitude;
        this.temperatura = temperatura;
        this.codigoClima = codigoClima;
        this.condicaoTempo = condicaoTempo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public boolean isFavorito() {
        return favorito;
    }

    public void setFavorito(boolean favorito) {
        this.favorito = favorito;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(String temperatura) {
        this.temperatura = temperatura;
    }

    public int getCodigoClima() {
        return codigoClima;
    }

    public void setCodigoClima(int codigoClima) {
        this.codigoClima = codigoClima;
    }

    public String getCondicaoTempo() {
        return condicaoTempo;
    }

    public void setCondicaoTempo(String condicaoTempo) {
        this.condicaoTempo = condicaoTempo;
    }
}