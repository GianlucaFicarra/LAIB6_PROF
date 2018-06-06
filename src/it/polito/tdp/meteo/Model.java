package it.polito.tdp.meteo;

import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import it.polito.tdp.meteo.bean.Citta;
import it.polito.tdp.meteo.bean.CittaUmidita;
import it.polito.tdp.meteo.db.MeteoDAO;

public class Model {

	private final static int COST = 100;
	private final static int NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN = 3;
	private final static int NUMERO_GIORNI_CITTA_MAX = 6;
	private final static int NUMERO_GIORNI_TOTALI = 15;

	/*(3) mi serve un eleneco delle citt� presenti nel DB su cui calcolare l'umidita
	 * quando costruisco il modello questo si chiedera quali sono le citt� nel DB e le chieder� al dao
	 * costruisco un meteoDAO nel costruttore a cui chiedero di popolare le citta*/
	private List<Citta> leCitta;
	private List<Citta> best;

	/**
	 * Tutte le città presenti nel database. La lista viene letta al momento della
	 * costruzione del Model.
	 * 
	 * @return lista delle città presenti
	 */
	public List<Citta> getLeCitta() {
		return leCitta;
	}

	public Model() {
		MeteoDAO dao = new MeteoDAO();
		this.leCitta = dao.getAllCitta();
		//restituisce lista di citta presenti del DB salvate cosi nel MODEL
	}

	/**
	 * Calcolo dell'umidità media di una città in un mese
	 * 
	 * @param mese
	 * @param citta
	 * @return valore dell'umidità media
	 */
	public Double getUmiditaMedia(Month mese, Citta citta) { //(4) inizio punto 1
		
		/*data citta e mese dammi il numero che rappresenta umidita media,
		 * sfrutto la query per farmi svolgere questa operazione,
		 *questa info ce l'ha il DB a cui chiedo le info dove citta � torino e mese corrente
		 *OSS nel DB non ho mese ma data completa, uso MONTH(data)=numero del mese corrente
		 *usata per capire se la data ha il mio mese corrente.
		 *
		 *-->questo fatto con un metodo del dao a cui passo citta e mese*/
		
		MeteoDAO dao = new MeteoDAO();
		return dao.getUmiditaMedia(mese, citta);
	}

	/**
	 * Calcola la sequenza ottimale di visita delle città nel mese specificato
	 * 
	 * @param mese
	 *            il mese da analizzare
	 * @return la lista delle città da visitare nei primi 15 giorni del mese
	 */
	//(8)
	public List<Citta> calcolaSequenza(Month mese) {
		List<Citta> parziale = new ArrayList<>();
		this.best = null;

		MeteoDAO dao = new MeteoDAO();

		// carica dentro ciascuna delle leCitta la lista dei rilevamenti nel mese
		// considerato (e solo quello)
		for (Citta c : leCitta) {
			c.setRilevamenti(dao.getRilevamentiLocalitaMese(mese, c));
		}
		
		// la ricorsiva non ha bisogno della dimensione poich� � una costante
		cerca(parziale, 0);
		return best;
	}


	/*(6) INIZIO SECONDO PUNTO
	 * metodo che model offre al controller per la sequenza pi� conveniente
	 * scelto il mese, utente clicca su calcola, e controller chiama questa
	 * che restituisce lista che controller stampera.
	 * per creare la lista uso la ricorsiva CERCA che riceve soluz parziale, liv,
	 */
	
	
	/**
	 * Procedura ricorsiva per il calcolo delle città ottimali. Per informazioni
	 * sull'impostazione della ricorsione, vedere il file {@code ricorsione.txt}
	 * nella cartella di progetto.
	 * 
	 * @param parziale
	 *            soluzione parziale in via di costruzione
	 * @param livello
	 *            livello della ricorsione, cioè il giorno a cui si sta cercando di
	 *            definire la città
	 */
	private void cerca(List<Citta> parziale, int livello) {
	
		// caso terminale
		if (livello == NUMERO_GIORNI_TOTALI) {
		
			//(9)calcolo il costo della soluzione e vedo se � il migliore
			Double costo = calcolaCosto(parziale);
			if (best == null || costo < calcolaCosto(best)) {
				best = new ArrayList<>(parziale); //deepcopy
			}

			
		} else { //altrimenti provo nuove soluzioni

			//caso intermedio provo una alla volta le varie citta per vedere se non viola le regole
			for (Citta prova : leCitta) {

				if (aggiuntaValida(prova, parziale)) {

					parziale.add(prova);
					cerca(parziale, livello + 1);
					parziale.remove(parziale.size() - 1);

				}
			}

		}

	}

	/**
	 * Calcola il costo di una determinata soluzione (totale)
	 * 
	 * <p>
	 * Attenzione: questa funzione assume che i dati siano <b>tutti</b> presenti nel
	 * database, ma nel nostro esempio ciò non accade (in alcuni giorni il dato è
	 * mancante, per cui il risultato calcolato sarà errato).
	 * 
	 * @param parziale
	 *            la soluzione (totale) proposta
	 * @return il valore del costo, che tiene conto delle umidità nei 15 giorni e
	 *         del costo di cambio città
	 */
	//sapendo citta della lista e mese e rilevamenti mi dice il costo
	private Double calcolaCosto(List<Citta> parziale) {

		double costo = 0.0;
		
		/*calcolo costo solo nella soluz terminale, calcolarlo nelle soluzioni parziali
		 * non mi da nessun aiuto in pi� nel filtraggio delle soluzioni,
		 * 
		 * sommatoria di tutte le unita in ogni citta considerando il rilevamento del giorno giusto
		 * prendo la citta in quel giorno e ne prendo l'umidita
		 */
		
		for (int giorno = 1; giorno <= NUMERO_GIORNI_TOTALI; giorno++) {
			// dove mi trovo?
			Citta c = parziale.get(giorno - 1);
			// che umidità ho in quel giorno in quella città?
			double umid = c.getRilevamenti().get(giorno - 1).getUmidita();
			costo += umid;

			// ATTENZIONE: c.getRilevamenti().get(giorno-1) assume che siano presenti TUTTI
			// i giorni nel database
			// Se vi fossero dei giorni mancanti (e nel nostro DB ce ne sono!), allora il
			// giorno 'giorno-1' potrebbe
			// non corrispondere al dato giusto!
		}

		// a cui sommo 100 * numero di volte in cui cambio città
		for (int giorno = 2; giorno <= NUMERO_GIORNI_TOTALI; giorno++) {
			if (!parziale.get(giorno - 1).equals(parziale.get(giorno - 2))) {
				costo += COST;
			}
		}

		return costo;
	}

	/**
	 * Verifica se, data la soluzione {@code parziale} già definita, sia lecito
	 * aggiungere la città {@code prova}, rispettando i vincoli sui numeri giorni
	 * minimi e massimi di permanenza.
	 * 
	 * @param prova
	 *            la città che sto cercando di aggiungere
	 * @param parziale
	 *            la sequenza di città già composta
	 * @return {@code true} se {@code prova} è lecita, {@code false} se invece viola
	 *         qualche vincolo (e quindi non è lecita)
	 */
	private boolean aggiuntaValida(Citta prova, List<Citta> parziale) {

		// verifica giorni massimi
		int conta = 0;
		for (Citta precedente : parziale)
			if (precedente.equals(prova))
				conta++;
		if (conta >= NUMERO_GIORNI_CITTA_MAX)
			return false;

		
		// verifica giorni minimi
		if (parziale.size() == 0) // primo giorno
			return true;
		if (parziale.size() == 1 || parziale.size() == 2) { // secondo o terzo giorno: non posso cambiare
			return parziale.get(parziale.size() - 1).equals(prova);
		}
		if (parziale.get(parziale.size() - 1).equals(prova)) // giorni successivi, posso SEMPRE rimanere
			return true;
		
		
		// sto cambiando citta
		if (parziale.get(parziale.size() - 1).equals(parziale.get(parziale.size() - 2))
				&& parziale.get(parziale.size() - 2).equals(parziale.get(parziale.size() - 3)))
			return true;

		
		
		return false;
	}

}
