package it.polito.tdp.meteo.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import it.polito.tdp.meteo.bean.Citta;
import it.polito.tdp.meteo.bean.Rilevamento;

public class MeteoDAO {

	/**
	 * Elenco di tutte le città presenti nel database.
	 * @return
	 */
	public List<Citta> getAllCitta() { //continuazione punto (3)
		String sql = "SELECT DISTINCT localita FROM situazione ORDER BY localita";

		/*creo la connessioneione al DB e tramite la query estrapolo
		 * le citta dal DB salvandole in una lista*/
		
		List<Citta> result = new ArrayList<>();

		try {
			Connection conn = DBConnect.getInstance().getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();

			while (res.next()) { //scorro il cursore sulle righe del risultato
				result.add(new Citta(res.getString("localita"))); //creo citta usante costruttore che vuole solo il nome
			}

			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	/*(4) chiedo al db le informazioni dove la citta ed il mese sono quelli correnti passati
	non avendo colonna mese, prendo tutte le date il cui mese � quello corrente, uso MONTH(data)
	non passo mese ma oggetto che rappresenta un dato. quindi passo direttamente MONTH*/
	
	/**
	 * Dato un mese ed una città, estrare dal DB l'umidità media relativa a tale mese e tale città.
	 * <p>Tutti i calcoli sono delegati al database.
	 * 
	 * @param mese
	 * @param citta
	 * @return
	 */
	public Double getUmiditaMedia(Month mese, Citta citta) {

		String sql = "SELECT AVG(Umidita) AS U FROM situazione " + 
				"WHERE localita=? " + 
				"AND MONTH(data)=? ";

		try {
			Connection conn = DBConnect.getInstance().getConnection();
			PreparedStatement st = conn.prepareStatement(sql);

			st.setString(1, citta.getNome());  //sostituisco i valori
			st.setInt(2, mese.getValue());

			ResultSet res = st.executeQuery();

			res.next(); // posiziona sulla prima (ed unica) riga

			Double u = res.getDouble("U");

			conn.close();
			
			return u;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Carica dal database l'elenco completo di tutti i rilevamenti
	 * @return
	 */
	public List<Rilevamento> getAllRilevamenti() {

		final String sql = "SELECT Localita, Data, Umidita FROM situazione ORDER BY data ASC";

		List<Rilevamento> rilevamenti = new ArrayList<Rilevamento>();

		try {
			Connection conn = DBConnect.getInstance().getConnection();
			PreparedStatement st = conn.prepareStatement(sql);

			ResultSet rs = st.executeQuery();

			while (rs.next()) {

				Rilevamento r = new Rilevamento(rs.getString("Localita"), rs.getDate("Data"), rs.getInt("Umidita"));
				rilevamenti.add(r);
			}

			conn.close();
			return rilevamenti;

		} catch (SQLException e) {

			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Carica i rilevamenti di una determinata città in un determinato mese.
	 * 
	 * @param mese
	 * @param localita
	 * @return
	 */
	public List<Rilevamento> getRilevamentiLocalitaMese(Month mese, Citta localita) {
		
		final String sql = "SELECT Localita, Data, Umidita FROM situazione WHERE MONTH(Data)=? AND Localita=? ORDER BY data ASC";
		
		List<Rilevamento> rilevamenti = new ArrayList<Rilevamento>();

		try {
			Connection conn = DBConnect.getInstance().getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			
			st.setInt(1, mese.getValue());
			st.setString(2, localita.getNome());

			ResultSet rs = st.executeQuery();

			while (rs.next()) {

				Rilevamento r = new Rilevamento(rs.getString("Localita"), rs.getDate("Data"), rs.getInt("Umidita"));
				rilevamenti.add(r);
			}

			conn.close();
			return rilevamenti;

		} catch (SQLException e) {

			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
