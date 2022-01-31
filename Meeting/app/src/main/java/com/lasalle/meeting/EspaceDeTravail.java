package com.lasalle.meeting;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * @file EspaceDeTravail.java
 * @brief Déclaration de la classe EspaceDeTravail
 * @author KELLER-LAVALLEE Joachim
 * $LastChangedRevision: 94 $
 * $LastChangedDate: 2021-06-11 12:16:56 +0200 (ven. 11 juin 2021) $
 */

/**
 * @class EspaceDeTravail
 * @brief L'espace de travail
 */
public class EspaceDeTravail implements Serializable
{
    /**
     * Les constantes
     */
    private static final String TAG = "_EspaceDeTravail"; //!< TAG pour les logs
    public static final int INDICE_CHAUD = 3;
    public static final int INDICE_TIEDE = 2;
    public static final int INDICE_LEGEREMENT_TIEDE = 1;
    public static final int INDICE_NEUTRE = 0;
    public static final int INDICE_LEGEREMENT_FRAIS = -1;
    public static final int INDICE_FRAIS = -2;
    public static final int INDICE_FROID = -3;

    /**
     * Les attributs
     */
    private String adresseIP; //!< Adresse IP du portier
    private String nom; //!< Nom de l'espace de travail
    private String lieu; //!< Lieu de l'espace de travail
    private String description; //!< Description de l'espace de travail
    private int superficie; //!< Superficie de l'espace de travail
    private double temperature; //!< Température de l'espace de travail
    private int indiceDeConfort; //!< Indice de confort de l'espace de travail
    private boolean estReserve; //!< Disponibilité de l'espace de travail
    private String code; //!< Code pour libérer l'espace de travail
    private boolean estFavori; //!< Si l'espace de travail est en favori
    private Communication communication = null; //!< Attribut permettant d'envoyer des requêtes

    /**
     * @brief Constructeur par défaut de la classe EspaceDeTravail
     */
    public EspaceDeTravail(String adresseIP)
    {
        this.adresseIP = adresseIP;
        this.nom = "";
        this.lieu = "";
        this.description = "";
        this.superficie = 0;
        this.temperature = 0.;
        this.indiceDeConfort = 0;
        this.estReserve = false;
        this.code = "";
        this.estFavori = false;

        fromJSON(IHMMeeting.recupererDonneesEspaceDeTravail(this));
    }

    /**
     * @brief Accesseur de l'attribut adresseIP
     * @return adresseIP adresse IP de l'espace de travail
     */
    public String getAdresseIP()
    {
        return adresseIP;
    }

    /**
     * @brief Accesseur de l'attribut nom
     * @return nom nom de l'espace de travail
     */
    public String getNom()
    {
        return nom;
    }

    /**
     * @brief Accesseur de l'attribut lieu
     * @return lieu lieu de l'espace de travail
     */
    public String getLieu()
    {
        return lieu;
    }

    /**
     * @brief Accesseur de l'attribut description
     * @return description description de l'espace de travail
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @brief Accesseur de l'attribut superficie
     * @return superficie superficie de l'espace de travail
     */
    public int getSuperficie()
    {
        return superficie;
    }

    /**
     * @brief Accesseur de l'attribut temperature
     * @return temperature température de l'espace de travail
     */
    public double getTemperature()
    {
        return temperature;
    }

    /**
     * @brief Accesseur de l'attribut indiceDeConfort
     * @return indiceDeConfort indice de confort de l'espace de travail
     */
    public int getIndiceDeConfort()
    {
        return indiceDeConfort;
    }

    /**
     * @brief Accesseur de l'attribut estReserve
     * @return estReserve disponibilité de l'espace de travail
     */
    public boolean getEstReserve()
    {
        return estReserve;
    }

    /**
     * @brief Accesseur de l'attribut code
     * @return code code pour libérer l'espace de travail
     */
    public String getCode()
    {
        return code;
    }

    /**
     * @brief Accesseur de l'attribut estFavori
     * @return estFavori si l'espace de travail est dans les favoris
     */
    public boolean getEstFavori()
    {
        return estFavori;
    }

    /**
     * @brief Mutateur de l'attribut estReserve
     * @param estReserve disponibilité de l'espace de travail
     */
    public void setEstReserve(boolean estReserve)
    {
        this.estReserve = estReserve;
    }

    /**
     * @brief Mutateur de l'attribut code
     * @param code code pour libérer l'espace de travail
     */
    public void setCode(String code)
    {
        this.code = code;
        //IHMMeeting.sauvegarderDonneesEspaceDeTravail(this);

        Log.d(TAG, "setCode() " + code);
    }

    /**
     * @brief Mutateur de l'attribut estFavori
     * @param estFavori si l'espace de travail est dans les favoris
     */
    public void setEstFavori(boolean estFavori)
    {
        this.estFavori = estFavori;
        IHMMeeting.sauvegarderDonneesEspaceDeTravail(this);

        Log.d(TAG, "setEstFavori() " + estFavori);
    }

    /**
     * @brief Réserve l'espace de travail
     */
    public void reserver()
    {
        if(communication == null)
            return;
        Log.d(TAG, "reserver()");

        String trame = "\0";
        List<String> parametres = Arrays.asList("0");
        trame = communication.fabriquerTrameModification(Communication.MODIFICATION_DISPONIBILITE, parametres);
        communication.envoyer(trame, adresseIP);

        setEstReserve(true);
    }

    /**
     * @brief Libère l'espace de travail
     */
    public void liberer(String code)
    {
        if(communication == null)
            return;
        Log.d(TAG, "liberer()");

        String trame = "\0";
        List<String> parametres = Arrays.asList("1", code);
        trame = communication.fabriquerTrameModification(Communication.MODIFICATION_DISPONIBILITE, parametres);
        communication.envoyer(trame, adresseIP);

        setEstReserve(false);
    }

    /**
     * @brief Modifie les informations de l'espace de travail
     * @param parametres champs de la trame de modification d'informations
     */
    public void modifierInformations(List<String> parametres)
    {
        if(communication == null)
            return;
        Log.d(TAG, "modifierInformations()");

        String trame = "\0";
        trame = communication.fabriquerTrameModification(Communication.MODIFICATION_INFORMATIONS, parametres);
        communication.envoyer(trame, adresseIP);
    }

    /**
     * @brief Extrait les informations d'une trame DEMANDE_INFORMATIONS
     * @param trame la trame à décoder
     */
    public boolean extraireInformations(String trame)
    {
        /**
         * Protocole :
         * $nom;description;lieu;superficie;disponibilité;niveauDeConfort;température\r\n
         */

        trame = trame.replace("$", "");
        trame = trame.replace("\r\n", "");
        String[] champs = trame.split(";");

        if(champs.length == Communication.NB_CHAMPS_DEMANDE_INFORMATIONS)
        {
            this.nom = champs[Communication.CHAMP_NOM];
            this.description = champs[Communication.CHAMP_DESCRIPTION];
            this.lieu = champs[Communication.CHAMP_LIEU];
            if (!champs[Communication.CHAMP_SUPERFICIE].isEmpty())
            {
                this.superficie = Integer.parseInt(champs[Communication.CHAMP_SUPERFICIE]);
            }
            if (!champs[Communication.CHAMP_DISPONIBILITE].isEmpty())
            {
                if (Integer.parseInt(champs[Communication.CHAMP_DISPONIBILITE]) == 1)
                {
                    this.estReserve = false;
                }
                else
                {
                    this.estReserve = true;
                }
            }
            if (!champs[Communication.CHAMP_TEMPERATURE].isEmpty())
            {
                this.temperature = Double.parseDouble(champs[Communication.CHAMP_TEMPERATURE]);
            }
            if(!champs[Communication.CHAMP_INDICE_DE_CONFORT].isEmpty())
            {
                this.indiceDeConfort = Integer.parseInt(champs[Communication.CHAMP_INDICE_DE_CONFORT]);
            }

            Log.d(TAG, "extraireInformations() nom : " + nom + " - description : " + description + " - lieu : " + lieu + " - superficie : " + superficie + " - estReserve : " + estReserve + " - temperature : " + temperature + " - indiceDeConfort : " + indiceDeConfort);

            return true;
        }

        return false;
    }

    /**
     * @brief Extrait le code
     * @param trame la trame à décoder
     */
    public boolean extraireCode(String trame)
    {
        /**
         * Protocole :
         * $nom;code;message\r\n
         */

        trame = trame.replace("$", "");
        trame = trame.replace("\r\n", "");
        String[] champs = trame.split(";");

        if(champs.length == Communication.NB_CHAMPS_MODIFICATION_DISPONIBILITE)
        {
            this.nom = champs[Communication.CHAMP_NOM];

            if (!champs[1].isEmpty())
            {
                this.code = champs[Communication.CHAMP_CODE];
            }

            Log.d(TAG, "extraireCode() nom : " + nom + " - code : " + code);

            return true;
        }

        return false;
    }

    /**
     * @brief Initialise une communication
     * @param handler le handler pour échanger des messages avec l'activité
     */
    public void initialiserCommunication(Handler handler)
    {
        Log.d(TAG, "initialiserCommunication()");
        if(communication == null)
        {
            communication = new Communication();
            communication.setHandler(handler);

            // Démarre la réception des trames des portiers
            Thread tCommunicationUDP = new Thread(communication, getAdresseIP());
            tCommunicationUDP.start(); // execute la méthode run()
        }
        else
        {
            communication.arreter();
            communication.setHandler(null);
            communication = null;
        }
    }

    /**
     * @brief Envoie une trame de demande d'informations au portier si clic sur bouton rafraîchir
     */
    public void demanderInformations()
    {
        communication.envoyer(communication.fabriquerTrameDemande(Communication.DEMANDE_INFORMATIONS), adresseIP);
    }

    /**
     * @brief Création de données JSON
     * @return String les données (code et favori) formaté en JSON
     */
    public String toJSON()
    {
        JSONObject objet = new JSONObject();
        try
        {
            objet.put("code", this.code);
            objet.put("estFavori", this.estFavori);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            Log.i(TAG, "toJSON() Erreur !");
        }

        //Log.i(TAG, "toJSON() JSON = " + objet.toString());
        return objet.toString();
    }

    /**
     * @brief Récupération de données JSON
     * @param strJSON les données (code et favori) formatés en JSON à extraire
     */
    public void fromJSON(String strJSON)
    {
        try
        {
            //Log.i(TAG, "fromJSON() JSON = " + strJSON);
            JSONObject json = new JSONObject(strJSON);

            this.code = json.getString("code");
            this.estFavori = json.getBoolean("estFavori");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            Log.i(TAG, "fromJSON() Erreur !");
        }
    }
}
