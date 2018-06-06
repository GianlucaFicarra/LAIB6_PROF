package it.polito.tdp.meteo.bean;

public class CittaUmidita {
	private Citta citta ;
	private Double umidita ;
	
	
	public CittaUmidita(Citta citta, Double umid) {
		super();
		this.citta = citta;
		this.umidita = umid;
	}


	public Citta getCitta() {
		return citta;
	}


	public void setCitta(Citta citta) {
		this.citta = citta;
	}


	public Double getUmidita() {
		return umidita;
	}


	public void setUmidita(Double umidita) {
		this.umidita = umidita;
	} 
	
	

}
