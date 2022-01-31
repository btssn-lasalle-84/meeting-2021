package com.lasalle.meeting;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @file Communication.java
 * @brief Déclaration de la classe Communication
 * @author KELLER-LAVALLEE Joachim
 * $LastChangedRevision: 94 $
 * $LastChangedDate: 2021-06-11 12:16:56 +0200 (ven. 11 juin 2021) $
 */

/**
 * @class Communication
 * @brief Communication entre l'application et le portier
 */
public class Communication implements Runnable
{
    /**
     * Les constantes
     */
    private static final String TAG = "_Communication"; //!< TAG pour les logs

    /**
     * Les attributs
     */
    private InetAddress adresseIP = null; //!< Adresse IP du portier
    public final static String adresseMulticast = "239.0.0.42"; //!< Adresse multicast des portiers
    private final static int PORT = 5000; //!< Port d'écoute des portiers
    public final static int TYPE_RECEPTION = 1; //!< Code du message indiquant une réception de données
    private final ReentrantLock mutex = new ReentrantLock();
    private DatagramSocket socket = null; //!< Socket UDP
    private LinkedBlockingQueue<DatagramPacket> queueEmission; //!< Queue d'émission des trames
    private Handler handler; //!< Handler permettant l'échange de Message avec l'activité

    /**
     * Protocole
     */
    public static final String DELIMITEUR_EN_TETE = "$";
    public static final String DELIMITEUR_CHAMP = ";";
    public static final String DELIMITEUR_FIN = "\r\n";
    public static final int TRAME_INCONNUE = -1;
    public static final int DEMANDE_INFORMATIONS = 1;
    public static final int DEMANDE_DISPONIBILITE = 3;
    public static final int MODIFICATION_INFORMATIONS = 1;
    public static final int MODIFICATION_DISPONIBILITE = 3;
    public static final int NB_CHAMPS_INFORMATIONS = 4;
    public static final int NB_CHAMPS_DISPONIBILITE = 1;
    public static final int NB_CHAMPS_DISPONIBILITE_CODE = 2;
    public static final int NB_CHAMPS_DEMANDE_INFORMATIONS = 7;
    public static final int NB_CHAMPS_DEMANDE_DISPONIBILITE = 1;
    public static final int NB_CHAMPS_MODIFICATION_DISPONIBILITE = 2;
    public static final int NB_CHAMPS_RETOUR_MODIFICATION_DISPONIBILITE = 3;
    public static final int CHAMP_NOM = 0;
    public static final int CHAMP_DESCRIPTION = 1;
    public static final int CHAMP_LIEU = 2;
    public static final int CHAMP_SUPERFICIE = 3;
    public static final int CHAMP_DISPONIBILITE = 4;
    public static final int CHAMP_INDICE_DE_CONFORT = 5;
    public static final int CHAMP_TEMPERATURE = 6;
    public static final int CHAMP_CODE = 1;

    /**
     * @brief Constructeur par défaut de la classe Communication
     * @param handler Handler
     */
    public Communication(Handler handler)
    {
        this.handler = handler;

        try
        {
            socket = new DatagramSocket(PORT);
            Log.d(TAG, "Création de la socket UDP sur le port " + PORT);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        queueEmission = new LinkedBlockingQueue<DatagramPacket>();
    }

    public Communication()
    {
        this.handler = null;

        try
        {
            socket = new DatagramSocket();
            Log.d(TAG, "Création d'une socket UDP");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        queueEmission = new LinkedBlockingQueue<DatagramPacket>();
    }

    public void setHandler(Handler handler)
    {
        this.handler = handler;
    }

    /**
     * @brief Envoyer la trame
     * @param trame la trame à envoyer
     * @param adressePortier l'adresse IP du portier
     */
    public void envoyer(String trame, String adressePortier)
    {
        if(socket == null || socket.isClosed())
            return;

        Log.d(TAG, "envoyer() : adressePortier = " + adressePortier);

        final InetAddress adresseIPDistante;
        try
        {
            adresseIPDistante = InetAddress.getByName(adressePortier);
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
            return;
        }

        // Crée et démarre un thread pour envoyer la trame
        new Thread()
        {
            @Override public void run()
            {
                byte[] emission = new byte[1024];

                try
                {
                    emission = trame.getBytes();
                    DatagramPacket paquetRetour = new DatagramPacket(emission, emission.length, adresseIPDistante, PORT);
                    socket.send(paquetRetour);
                    Log.d(TAG, "envoyer() : " + trame + " à " + adresseIPDistante + ":" + PORT);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Log.d(TAG, "Erreur émission !");
                }
            }
        }.start();
    }

    /**
     * @brief Recevoir les trames des portiers
     */
    public void recevoir()
    {
        byte[] reception = new byte[1024];

        while (socket != null && !socket.isClosed())
        {
            try
            {
                final DatagramPacket paquetRecu = new DatagramPacket(reception, reception.length);
                socket.receive(paquetRecu);
                final String donnees = new String(paquetRecu.getData(), paquetRecu.getOffset(), paquetRecu.getLength());
                Log.d(TAG, "Réception [" + paquetRecu.getAddress().getHostAddress() + ":" + paquetRecu.getPort() + "] -> " + donnees);

                if(verifierTrame(donnees))
                {
                    // Envoie les données reçues à l'activité
                    envoyerMessage(TYPE_RECEPTION, paquetRecu.getAddress().getHostAddress(), paquetRecu.getPort(), donnees);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.d(TAG, "Erreur réception !");
            }
        }

        Log.d(TAG, "recevoir()");
    }

    /**
     * @brief Envoie un message
     */
    private void envoyerMessage(int type, String adresse, int port, String donnees)
    {
        if(handler == null)
            return;
        Message msg = Message.obtain();
        msg.what = type;
        Bundle b = new Bundle();
        b.putString("adresseIP", adresse);
        b.putInt("port", port);
        b.putString("donnees", donnees);
        msg.setData(b);
        mutex.lock();
        handler.sendMessage(msg);
        mutex.unlock();
        Log.d(TAG, "envoyerMessage() -> handler.sendMessage()");
    }

    /**
     * @brief Fabrique la trame de demande
     * @param typeTrame le type de trame de demande
     * @return trame la trame fabriquée
     */
    public String fabriquerTrameDemande(int typeTrame)
    {
        /*
         * Protocole
         *
         * Demande informations du portier :
         * $GET;1\r\n
         *
         * Demande disponibilité du portier :
         * $GET;3\r\n
         */

        String trame = null;
        Log.d(TAG, "fabriquerTrameDemande() type = " + typeTrame);

        switch(typeTrame)
        {
            case DEMANDE_INFORMATIONS:
                trame = DELIMITEUR_EN_TETE + "GET;1" + DELIMITEUR_FIN;
                break;
            case DEMANDE_DISPONIBILITE:
                trame = DELIMITEUR_EN_TETE + "GET;3" + DELIMITEUR_FIN;
                break;
            default:
                Log.d(TAG, "fabriquerTrameDemande() : type de trame inconnu !");
        }

        Log.d(TAG,"fabriquerTrameDemande() trame = " + trame);

        return trame;
    }

    /**
     * @brief Fabrique la trame de modification
     * @param typeTrame le type de trame de modification
     * @return trame la trame fabriquée
     */
    public String fabriquerTrameModification(int typeTrame, List<String> parametres)
    {
        /*
         * Protocole
         *
         * Actualiser les informations d’un portier :
         * $SET;1;nomSalle;description;emplacement;surface\r\n
         *
         * Actualiser la disponibilité d’un portier :
         * $SET;3;disponibilité\r\n
         *
         * Exemple d'utilisation :
         *   List<String> parametres = Arrays.asList("B11", "Salle de cours", "Batiment BTS", "25");
         *   String trame = communication.fabriquerTrameModification(Communication.MODIFICATION_INFORMATIONS, parametres);
         */

        // Vérifications
        if(parametres == null)
            return null;

        if(parametres.size() < 1)
            return null;

        Log.d(TAG, "fabriquerTrameModification() type = " + typeTrame);
        Log.d(TAG, "fabriquerTrameModification() Nb parametres = " + parametres.size());
        for(int i=0;i<parametres.size();++i)
        {
            Log.d(TAG, "fabriquerTrameModification() parametres[" + i + "] = " + parametres.get(i));
        }

        String trame = null;

        switch(typeTrame)
        {
            case MODIFICATION_INFORMATIONS:
                if(parametres.size() != NB_CHAMPS_INFORMATIONS)
                    return null;
                trame = DELIMITEUR_EN_TETE + "SET;1;" + parametres.get(CHAMP_NOM) + DELIMITEUR_CHAMP + parametres.get(CHAMP_DESCRIPTION) + DELIMITEUR_CHAMP + parametres.get(CHAMP_LIEU) + DELIMITEUR_CHAMP + parametres.get(CHAMP_SUPERFICIE) + DELIMITEUR_FIN;
                break;

            case MODIFICATION_DISPONIBILITE:
                if(parametres.size() >= NB_CHAMPS_DISPONIBILITE && parametres.size() <= (NB_CHAMPS_DISPONIBILITE+1))
                {
                    if (parametres.get(0).equals("0"))
                        trame = DELIMITEUR_EN_TETE + "SET;3;" + parametres.get(0) + DELIMITEUR_FIN;
                    else
                        trame = DELIMITEUR_EN_TETE + "SET;3;" + parametres.get(0) + ";" + parametres.get(1) + DELIMITEUR_FIN;
                }
                break;

            default:
                Log.d(TAG, "fabriquerTrameModification() : type de trame inconnu !");
        }

        Log.d(TAG,"fabriquerTrameModification() trame = " + trame);

        return trame;
    }

    /**
     * @brief Vérifie la trame
     * @param trame la trame à vérifier
     */
    public boolean verifierTrame(String trame)
    {
        Log.d(TAG, "verifierTrame() " + (trame.startsWith(DELIMITEUR_EN_TETE) && trame.endsWith(DELIMITEUR_FIN)));

        return (trame.startsWith(DELIMITEUR_EN_TETE) && trame.endsWith(DELIMITEUR_FIN));
    }

    /**
     * @brief Arrête la socket, donc la communication avec les portiers
     */
    public void arreter()
    {
        if(socket == null)
            return;
        socket.close();
    }

    /**
     * @brief Assure la réception des trames
     */
    @Override
    public void run()
    {
        Log.d(TAG, "Démarre le thread réception");
        recevoir();
    }

    /**
     * @brief Détermine le type de trame
     * @param champs tableau contenant les champs de la trame
     * @return typeTrame le type de trame
     */
    public static int recupererTypeTrame(String[] champs)
    {
        int typeTrame = Communication.TRAME_INCONNUE;

        switch(champs.length)
        {
            case Communication.NB_CHAMPS_DEMANDE_INFORMATIONS:
                Log.d(TAG, "handleMessage() Trame DEMANDE_INFORMATIONS");
                typeTrame = Communication.DEMANDE_INFORMATIONS;
                break;
            case Communication.NB_CHAMPS_DEMANDE_DISPONIBILITE:
                Log.d(TAG, "handleMessage() Trame DEMANDE_DISPONIBILITE");
                typeTrame = Communication.DEMANDE_DISPONIBILITE;
                break;
            case Communication.NB_CHAMPS_RETOUR_MODIFICATION_DISPONIBILITE:
                Log.d(TAG, "handleMessage() Trame MODIFICATION_DISPONIBILITE");
                typeTrame = Communication.MODIFICATION_DISPONIBILITE;
        }

        return typeTrame;
    }
}
