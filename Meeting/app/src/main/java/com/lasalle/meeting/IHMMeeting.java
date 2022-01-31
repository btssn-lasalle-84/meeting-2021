package com.lasalle.meeting;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

/**
 * @file IHMMeeting.java
 * @brief Déclaration de la classe IHMMeeting
 * @author KELLER-LAVALLEE Joachim
 * $LastChangedRevision: 94 $
 * $LastChangedDate: 2021-06-11 12:16:56 +0200 (ven. 11 juin 2021) $
 */

/**
 * @class IHMMeeting
 * @brief L'activité principale de l'application Meeting
 */
public class IHMMeeting extends AppCompatActivity
{
    /**
     * Les constantes
     */
    private static final String TAG = "_IHMMeeting"; //!< TAG pour les logs
    private static final String PREFERENCES = "preferences"; //!< Préférences de l'application
    private static final String PREFERENCES_CODE = "code"; //!< Code pour libérer l'espace de travail enregistré dans les préférences
    private static final String PREFERENCES_EST_FAVORI = "false"; //!< Favori enregistré dans les préférences

    /**
     * Les ressources de l'IHM
     */
    private SwipeRefreshLayout swipeRefreshLayout; //!< Pour le Pull To Refresh
    private ListView listeEspacesDeTravail; //!< Affichage des espaces de travail sous forme de liste
    private TextView titreEspacesDeTravail;  //!< Affichage du titre principal
    private AlertDialog.Builder boiteDeDialogueAPropos; //!< Boîte de dialogue A propos
    private AlertDialog boiteDeDialogueRechercher; //!< Boîte de dialogue Rechercher
    private AlertDialog.Builder boiteDeDialogueFiltrerParDisponibilite; //!< Boîte de dialogue Filtrer par disponibilité
    private AlertDialog.Builder boiteDeDialogueFiltrerParNiveauDeConfort; //!< Boîte de dialogue Filtrer par niveau de confort
    private String motCle; //!< Mot-clé pour rechercher un espace de travail
    private boolean estReserve; //<! Disponibilité sélectionnée pour filtrer la liste des espaces de travail
    private int indiceDeConfort; //<! Niveau de confort sélectionné pour filtrer la liste des espaces de travail

    /**
     * Les attributs
     */
    private static Vector<EspaceDeTravail> espacesDeTravail; //!< Conteneur pour les espaces de travail
    private static Vector<EspaceDeTravail> espacesDeTravailFiltres; //!< Conteneur pour les espaces de travail filtrés
    private EspaceDeTravailAdaptateur adaptateur; //!< Adaptateur pour les espaces de travail
    private static Communication communication = null; //!< Attribut permettant d'envoyer des requêtes
    private WifiManager wm = null; //!< Attribut permettant de voir la connexion au WiFi
    private int choixFiltre = -1; //!< Choix du dernier type de filtrage
    private static SharedPreferences preferences; //!< Pour le stockage de données

    /**
     * @brief Méthode appelée à la création de l'activité
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate()");

        initialiserPreferences();

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                Log.d(TAG, "Pull To Refresh");
                initialiserEspacesDeTravail();
            }
        });

        initialiserListeEspacesDeTravail();

        afficherListeEspacesDeTravail();

        demarrerReseau();

        initialiserBoitesDeDialogue();
    }

    /**
     * @brief Initialise les préférences
     */
    private void initialiserPreferences()
    {
        preferences = getBaseContext().getSharedPreferences(PREFERENCES, MODE_PRIVATE);
    }

    /**
     * @brief Démarre la connexion wifi et la communication
     */
    private void demarrerReseau()
    {
        wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wm.isWifiEnabled())
        {
            Log.d(TAG, "WiFi indisponible !");
            wm.setWifiEnabled(true);
        }
        else
        {
            Log.d(TAG, "WiFi disponible");
        }

        communication = new Communication(handler);

        // Démarre la réception des trames des portiers
        Thread tCommunicationUDP = new Thread(communication, "Communication");
        tCommunicationUDP.start(); // execute la méthode run()

        if(communication != null)
        {
            // Demande les informations des portiers joignables
            communication.envoyer(communication.fabriquerTrameDemande(Communication.DEMANDE_INFORMATIONS), Communication.adresseMulticast);
        }
    }

    /**
     * @brief Méthode appelée au démarrage de l'activité MainActivity
     */
    @Override
    protected void onStart()
    {
        super.onStart();
        Log.d(TAG, "onStart()");

        initialiserEspacesDeTravail();
    }

    /**
     * @brief Méthode appelée après onStart() ou après onPause()
     */
    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d(TAG, "onResume()");
    }

    /**
     * @brief Méthode appelée après qu'une boîte de dialogue s'est affichée (on reprend sur un onResume()) ou avant onStop() (activité plus visible)
     */
    @Override
    protected void onPause()
    {
        super.onPause();
        Log.d(TAG, "onPause()");
    }

    /**
     * @brief Méthode appelée lorsque l'activité n'est plus visible
     */
    @Override
    protected void onStop()
    {
        super.onStop();
        Log.d(TAG, "onStop()");
    }

    /**
     * @brief Méthode appelée à la destruction de l'application (après onStop() et détruite par le système Android)
     */
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    /**
     * @brief Crée les espaces de travail détectés
     */
    private void initialiserEspacesDeTravail()
    {
        Log.d(TAG, "initialiserEspacesDeTravail()");

        espacesDeTravail.clear();

        if(communication != null)
        {
            // Demande les informations des portiers joignables
            communication.envoyer(communication.fabriquerTrameDemande(Communication.DEMANDE_INFORMATIONS), Communication.adresseMulticast);
        }

        swipeRefreshLayout.setRefreshing(false); // arrête le Pull To Refresh
    }

    /**
     * @brief Ajoute un espace de travail si pas encore détecté
     * @param espaceDeTravail un espace de travail
     */
    private void ajouterEspaceDeTravail(EspaceDeTravail espaceDeTravail)
    {
        int position = verifierPresenceEspaceDeTravail(espaceDeTravail);

        if(position == -1)
        {
            espacesDeTravail.add(espaceDeTravail);
            rafraichirListeEspacesFiltres();
        }
    }

    /**
     * @brief Modifie un espace de travail si déjà détecté
     * @param espaceDeTravail l'espace de travail modifié
     */
    private void modifierEspaceDeTravail(EspaceDeTravail espaceDeTravail)
    {
        int position = verifierPresenceEspaceDeTravail(espaceDeTravail);
        Log.d(TAG, "modifierEspaceDeTravail() : position = " + position);

        if(position != -1)
        {
            //espacesDeTravail.removeElementAt(position);
            //espacesDeTravail.add(espaceDeTravail);
            espacesDeTravail.set(position, espaceDeTravail);
            rafraichirListeEspacesFiltres();
        }
    }

    /**
     * @brief Supprime un espace de travail si déjà détecté
     * @param espaceDeTravail l'espace de travail à supprimer
     */
    private void supprimerEspaceDeTravail(EspaceDeTravail espaceDeTravail)
    {
        int position = verifierPresenceEspaceDeTravail(espaceDeTravail);

        if(position != -1)
        {
            espacesDeTravail.removeElementAt(position);
            rafraichirListeEspacesFiltres();
        }
    }

    /**
     * @brief Vérifie la présence d'un espace de travail dans le conteneur des espaces de travail détectés
     * @param espaceDeTravail l'espace de travail à vérifier
     * @return int la position dans le conteneur sinon -1 en cas d'absence
     */
    private int verifierPresenceEspaceDeTravail(EspaceDeTravail espaceDeTravail)
    {
        for(int i = 0; i < espacesDeTravail.size(); ++i)
        {
            if(espaceDeTravail.getNom().equals(espacesDeTravail.elementAt(i).getNom()))
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * @brief Vérifie la présence d'un espace de travail dans le conteneur des espaces de travail détectés
     * @param adresseIP l'adresse IP d' un espace de travail à vérifier
     * @return int la position dans le conteneur sinon -1 en cas d'absence
     */
    private int verifierPresenceEspaceDeTravail(String adresseIP)
    {
        for(int i = 0; i < espacesDeTravail.size(); ++i)
        {
            if(espacesDeTravail.elementAt(i).getAdresseIP().equals(adresseIP))
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * @brief Initialise la vue pour les espaces de travail
     */
    private void initialiserListeEspacesDeTravail()
    {
        Log.d(TAG, "initialiserListeEspacesDeTravail()");

        espacesDeTravail = new Vector<EspaceDeTravail>();
        espacesDeTravailFiltres = new Vector<EspaceDeTravail>();
        choixFiltre = R.id.actionAfficherTous;

        listeEspacesDeTravail = (ListView)findViewById(R.id.listeEspacesDeTravail);
        titreEspacesDeTravail = (TextView) findViewById(R.id.titreEspacesDeTravail);
        titreEspacesDeTravail.setText("Tous les espaces de travail détectés");
    }

    /**
     * @brief Affiche la liste des espaces de travail
     */
    private void afficherListeEspacesDeTravail()
    {
        Log.d(TAG, "afficherListeEspacesDeTravail()");

        adaptateur = new EspaceDeTravailAdaptateur(this, R.layout.element_espace_travail, espacesDeTravailFiltres);

        listeEspacesDeTravail.setAdapter(adaptateur);
        adaptateur.setNotifyOnChange(true);

        listeEspacesDeTravail.setOnItemClickListener(
                new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> a, View v, int position, long id)
                    {
                        Log.d(TAG, "Position : " + position + " - " + " Nom : " + espacesDeTravailFiltres.get(position).getNom());
                        Intent intent = new Intent(IHMMeeting.this, AffichageEspaceDeTravail.class);
                        intent.putExtra("unEspaceDeTravail", (Serializable)espacesDeTravailFiltres.get(position));
                        startActivityForResult(intent, 0);
                    }
                }
        );
    }

    /**
     * @brief Traite le retour de l'activité d'affichage d'un espace de travail
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult() requestCode=" + requestCode + " - resultCode=" + resultCode + "");
        /*EspaceDeTravail espaceDeTravail = (EspaceDeTravail)data.getSerializableExtra("unEspaceDeTravail");
        Log.d(TAG, "onActivityResult() espaceDeTravail : " + espaceDeTravail.getNom() + " - " + espaceDeTravail.getDescription() + " - " + espaceDeTravail.getLieu() + " - " + espaceDeTravail.getSuperficie() + " - " + espaceDeTravail.getEstReserve());
        modifierEspaceDeTravail(espaceDeTravail);*/
    }

    /**
     * @brief Permet de récupérer les trames
     * @param msg message
     */
    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            Bundle b = msg.getData();

            switch(msg.what)
            {
                case Communication.TYPE_RECEPTION:
                    String trame = b.getString("donnees");
                    Log.d(TAG, "handleMessage() Réception [" + b.getString("adresseIP") + ":" + b.getInt("port") + "] -> " + trame);

                    String[] champs = trame.split(";");
                    int typeTrame = Communication.recupererTypeTrame(champs);

                    switch(typeTrame)
                    {
                        case Communication.DEMANDE_INFORMATIONS:
                            // un nouvel espace de travail ?
                            int numeroEspaceDeTravail = verifierPresenceEspaceDeTravail(b.getString("adresseIP"));
                            if(numeroEspaceDeTravail == -1)
                            {
                                EspaceDeTravail espaceDeTravail = new EspaceDeTravail(b.getString("adresseIP"));
                                espaceDeTravail.extraireInformations(trame);
                                ajouterEspaceDeTravail(espaceDeTravail);
                            }
                            else
                            {
                                espacesDeTravail.get(numeroEspaceDeTravail).extraireInformations(trame);
                            }
                        break;

                        case Communication.MODIFICATION_DISPONIBILITE:
                            numeroEspaceDeTravail = verifierPresenceEspaceDeTravail(b.getString("adresseIP"));
                            espacesDeTravail.get(numeroEspaceDeTravail).extraireCode(trame);
                        break;

                        default:
                            Log.d(TAG, "handleMessage() : type de trame inconnu !");
                    }
                break;

                default:
                    Log.d(TAG,"handleMessage() : code inconnu ! ");
            }
        }
    };

    /**
     * @brief Méthode pour la création du menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * @brief Méthode appelée lors de la sélection d'une entrée de menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        switch(id)
        {
            case R.id.actionAfficherTous:
                Log.d(TAG, "onOptionsItemSelected() actionAfficherTous");
                choixFiltre = R.id.actionAfficherTous;
                titreEspacesDeTravail.setText("Tous les espaces de travail détectés");
                filtrerTous();
                break;

            case R.id.actionAfficherFavoris:
                Log.d(TAG, "onOptionsItemSelected() actionAfficherFavoris");
                choixFiltre = R.id.actionAfficherFavoris;
                titreEspacesDeTravail.setText("Favoris");
                filtrerParFavoris();
                break;

            case R.id.actionRechercher:
                Log.d(TAG, "onOptionsItemSelected() actionRechercher");
                choixFiltre = R.id.actionRechercher;
                titreEspacesDeTravail.setText("Recherche");
                boiteDeDialogueRechercher.show();
                EditText saisieMotCle = (EditText) ((AlertDialog) boiteDeDialogueRechercher).findViewById(R.id.saisieMotCle);
                saisieMotCle.setText(motCle);
                break;

            case R.id.actionFiltrerParDisponibilite:
                Log.d(TAG, "onOptionsItemSelected() actionFiltrerParDisponibilite");
                choixFiltre = R.id.actionFiltrerParDisponibilite;
                titreEspacesDeTravail.setText("Espaces de travail filtrés par disponibilité");
                boiteDeDialogueFiltrerParDisponibilite.show();
                break;

            case R.id.actionFiltrerParNiveauDeConfort:
                Log.d(TAG, "onOptionsItemSelected() actionFiltrerParNiveauDeConfort");
                choixFiltre = R.id.actionFiltrerParNiveauDeConfort;
                titreEspacesDeTravail.setText("Espaces de travail filtrés par niveau de confort");
                boiteDeDialogueFiltrerParNiveauDeConfort.show();
                break;

            case R.id.actionAPropos:
                Log.d(TAG, "onOptionsItemSelected() actionAPropos");
                boiteDeDialogueAPropos.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * @brief Initialise les boites de dialogue
     */
    public void initialiserBoitesDeDialogue()
    {
        boiteDeDialogueAPropos = new AlertDialog.Builder(this);
        boiteDeDialogueAPropos.setTitle("À propos");
        String message = "Projet Meeting version " + BuildConfig.VERSION_NAME + "\n\nAuteur : KELLER-LAVALLEE Joachim\nBTS SNIR La Salle Avignon 2021";
        boiteDeDialogueAPropos.setMessage(message);

        initialiserBoiteDeDialogueRechercher();
        initialiserBoiteDeDialogueFiltrerParDisponibilite();
        initialiserBoiteDeDialogueFiltrerParNiveauDeConfort();
    }

    /**
     * @brief Initialise la boite de dialogue de recherche
     */
    private void initialiserBoiteDeDialogueRechercher()
    {
        AlertDialog.Builder boiteDeDialogueRechercher = new AlertDialog.Builder(this);
        boiteDeDialogueRechercher.setTitle("Rechercher un espace de travail");
        LayoutInflater inflater = this.getLayoutInflater();
        View vue = inflater.inflate(R.layout.boite_recherche, null);
        boiteDeDialogueRechercher.setView(vue);

        boiteDeDialogueRechercher.setPositiveButton("Rechercher", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                EditText saisieMotCle = (EditText) ((AlertDialog) dialog).findViewById(R.id.saisieMotCle);
                motCle = saisieMotCle.getText().toString();
                titreEspacesDeTravail.setText("Résultats de la recherche : " + motCle);
                rechercher(motCle);
            }
        });
        boiteDeDialogueRechercher.setNegativeButton("Annuler", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {

            }
        });

        this.boiteDeDialogueRechercher = boiteDeDialogueRechercher.create();
    }

    /**
     * @brief Initialise la boite de dialogue de filtrage par disponibilité
     */
    private void initialiserBoiteDeDialogueFiltrerParDisponibilite()
    {
        boiteDeDialogueFiltrerParDisponibilite = new AlertDialog.Builder(this);
        boiteDeDialogueFiltrerParDisponibilite.setTitle("Filtrer par disponibilité");
        LayoutInflater inflater = this.getLayoutInflater();
        View vue = inflater.inflate(R.layout.boite_filtre_disponibilite, null);
        boiteDeDialogueFiltrerParDisponibilite.setView(vue);

        boiteDeDialogueFiltrerParDisponibilite.setPositiveButton("Filtrer", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                if(estReserve)
                {
                    titreEspacesDeTravail.setText("Espaces de travail occupés");
                }
                else
                {
                    titreEspacesDeTravail.setText("Espaces de travail libres");
                }

                filtrerParDisponibilite(estReserve);
            }
        });
        boiteDeDialogueFiltrerParDisponibilite.setNegativeButton("Annuler", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {

            }
        });
    }

    /**
     * @brief Initialise la boite de dialogue de filtrage par niveau de confort
     */
    private void initialiserBoiteDeDialogueFiltrerParNiveauDeConfort()
    {
        boiteDeDialogueFiltrerParNiveauDeConfort = new AlertDialog.Builder(this);
        boiteDeDialogueFiltrerParNiveauDeConfort.setTitle("Filtrer par niveau de confort");
        LayoutInflater inflater = this.getLayoutInflater();
        View vue = inflater.inflate(R.layout.boite_filtre_niveau_de_confort, null);
        boiteDeDialogueFiltrerParNiveauDeConfort.setView(vue);

        boiteDeDialogueFiltrerParNiveauDeConfort.setPositiveButton("Filtrer", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                switch(indiceDeConfort)
                {
                    case EspaceDeTravail.INDICE_CHAUD:
                        titreEspacesDeTravail.setText("Espaces de travail dont le niveau de confort est Chaud");
                        break;

                    case EspaceDeTravail.INDICE_TIEDE:
                        titreEspacesDeTravail.setText("Espaces de travail dont le niveau de confort est Tiède");
                        break;

                    case EspaceDeTravail.INDICE_LEGEREMENT_TIEDE:
                        titreEspacesDeTravail.setText("Espaces de travail dont le niveau de confort est Légèrement tiède");
                        break;

                    case EspaceDeTravail.INDICE_NEUTRE:
                        titreEspacesDeTravail.setText("Espaces de travail dont le niveau de confort est Neutre");
                        break;

                    case EspaceDeTravail.INDICE_LEGEREMENT_FRAIS:
                        titreEspacesDeTravail.setText("Espaces de travail dont le niveau de confort est Légèrement frais");
                        break;

                    case EspaceDeTravail.INDICE_FRAIS:
                        titreEspacesDeTravail.setText("Espaces de travail dont le niveau de confort est Frais");
                        break;

                    case EspaceDeTravail.INDICE_FROID:
                        titreEspacesDeTravail.setText("Espaces de travail dont le niveau de confort est Froid");
                        break;
                }

                filtrerParNiveauDeConfort(indiceDeConfort);
            }
        });
        boiteDeDialogueFiltrerParNiveauDeConfort.setNegativeButton("Annuler", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {

            }
        });
    }

    /**
     * @brief Méthode appelée lorsque l'on coche un bouton radio
     */
    public void onRadioButtonClicked(View vue)
    {
        boolean estCoche = ((RadioButton) vue).isChecked();

        switch (vue.getId())
        {
            case R.id.boutonRadioLibre:
                if (estCoche)
                {
                    estReserve = false;
                }
                break;

            case R.id.boutonRadioOccupe:
                if (estCoche)
                {
                    estReserve = true;
                }
                break;

            case R.id.boutonRadioChaud:
                if (estCoche)
                {
                    indiceDeConfort = EspaceDeTravail.INDICE_CHAUD;
                }
                break;

            case R.id.boutonRadioTiede:
                if (estCoche)
                {
                    indiceDeConfort = EspaceDeTravail.INDICE_TIEDE;
                }
                break;

            case R.id.boutonRadioLegerementTiede:
                if (estCoche)
                {
                    indiceDeConfort = EspaceDeTravail.INDICE_LEGEREMENT_TIEDE;
                }
                break;

            case R.id.boutonRadioNeutre:
                if (estCoche)
                {
                    indiceDeConfort = EspaceDeTravail.INDICE_NEUTRE;
                }
                break;

            case R.id.boutonRadioLegerementFrais:
                if (estCoche)
                {
                    indiceDeConfort = EspaceDeTravail.INDICE_LEGEREMENT_FRAIS;
                }
                break;

            case R.id.boutonRadioFrais:
                if (estCoche)
                {
                    indiceDeConfort = EspaceDeTravail.INDICE_FRAIS;
                }
                break;

            case R.id.boutonRadioFroid:
                if (estCoche)
                {
                    indiceDeConfort = EspaceDeTravail.INDICE_FROID;
                }
                break;
        }
    }

    /**
     * @brief Recherche les espaces de travail par un mot-clé
     */
    private void rechercher(String motCle)
    {
        espacesDeTravailFiltres.clear();

        for(int i = 0; i < espacesDeTravail.size(); i++)
        {
            if( containsIgnoreCase(espacesDeTravail.elementAt(i).getNom(), motCle) || containsIgnoreCase(espacesDeTravail.elementAt(i).getDescription(), motCle) || containsIgnoreCase(espacesDeTravail.elementAt(i).getLieu(), motCle) || containsIgnoreCase(espacesDeTravail.elementAt(i).getAdresseIP(), motCle) )
            {
                espacesDeTravailFiltres.add(espacesDeTravail.elementAt(i));
            }
        }

        Log.d(TAG, "rechercher() " + motCle);

        rafraichirAffichageListeEspaces();
    }

    /**
     * @brief Filtre les espaces de travail par disponibilité
     */
    private void filtrerParDisponibilite(boolean disponibilite)
    {
        espacesDeTravailFiltres.clear();

        for(int i = 0; i < espacesDeTravail.size(); i++)
        {
            if(espacesDeTravail.elementAt(i).getEstReserve() == disponibilite)
            {
                espacesDeTravailFiltres.add(espacesDeTravail.elementAt(i));
            }
        }

        Log.d(TAG, "filtrerParDisponibilite() " + disponibilite);

        rafraichirAffichageListeEspaces();
    }

    /**
     * @brief Filtre les espaces de travail par niveau de confort
     */
    private void filtrerParNiveauDeConfort(int indiceDeConfort)
    {
        espacesDeTravailFiltres.clear();

        for(int i = 0; i < espacesDeTravail.size(); i++)
        {
            if(espacesDeTravail.elementAt(i).getIndiceDeConfort() == indiceDeConfort)
            {
                espacesDeTravailFiltres.add(espacesDeTravail.elementAt(i));
            }
        }

        Log.d(TAG, "filtrerParNiveauDeConfort() " + indiceDeConfort);

        rafraichirAffichageListeEspaces();
    }

    /**
     * @brief Filtre les espaces de travail par favori
     */
    private void filtrerParFavoris()
    {
        espacesDeTravailFiltres.clear();

        for(int i = 0; i < espacesDeTravail.size(); i++)
        {
            if(espacesDeTravail.elementAt(i).getEstFavori())
            {
                espacesDeTravailFiltres.add(espacesDeTravail.elementAt(i));
            }
        }

        Log.d(TAG, "filtrerParFavoris()");

        rafraichirAffichageListeEspaces();
    }

    /**
     * @brief Récupère tous les espaces de travail détectés
     */
    private void filtrerTous()
    {
        espacesDeTravailFiltres.clear();

        for(int i = 0; i < espacesDeTravail.size(); i++)
        {
            espacesDeTravailFiltres.add(espacesDeTravail.elementAt(i));
        }

        Log.d(TAG, "filtrerTous");

        rafraichirAffichageListeEspaces();
    }

    /**
     * @brief Fabrique la liste des espaces de travail à afficher
     */
    private void rafraichirListeEspacesFiltres()
    {
        switch(choixFiltre)
        {
            case R.id.actionAfficherTous:
                titreEspacesDeTravail.setText("Tous les espaces de travail détectés");
                filtrerTous();
                break;

            case R.id.actionAfficherFavoris:
                titreEspacesDeTravail.setText("Favoris");
                filtrerParFavoris();
                break;

            case R.id.actionFiltrerParDisponibilite:
                titreEspacesDeTravail.setText("Espaces de travail filtrés par disponibilité");
                filtrerParDisponibilite(estReserve);
                break;

            case R.id.actionFiltrerParNiveauDeConfort:
                titreEspacesDeTravail.setText("Espaces de travail filtrés par niveau de confort");
                filtrerParNiveauDeConfort(indiceDeConfort);
                break;
        }
    }

    /**
     * @brief Met à jour l'affichage de la liste
     */
    private void rafraichirAffichageListeEspaces()
    {
        trierEspacesDeTravail("nom", espacesDeTravailFiltres);
        swipeRefreshLayout.setRefreshing(false);
        adaptateur.notifyDataSetChanged();
    }

    /**
     * @brief Trie les espaces de travail
     * @author Thierry Vaira
     */
    private void trierEspacesDeTravail(final String champ, Vector<EspaceDeTravail> lesEspacesDeTravail)
    {
        //Log.d(TAG, "trierEspacesDeTravail() champ = " + champ + " - nb = " + lesEspacesDeTravail.size());
        Collections.sort(lesEspacesDeTravail, new Comparator<EspaceDeTravail>()
        {
            @Override
            public int compare(EspaceDeTravail e1, EspaceDeTravail e2)
            {
                if(champ.equals("nom"))
                {
                    return e1.getNom().compareTo(e2.getNom());
                }
                else if(champ.equals("adresseIP"))
                {
                    return e1.getAdresseIP().compareTo(e2.getAdresseIP());
                }
                else if(champ.equals("superficie"))
                {
                    return (e1.getSuperficie() - e2.getSuperficie());
                }
                return e1.getNom().compareTo(e2.getNom());
            }
        });
    }

    /**
     * @brief Récupère les données stockées d'un espace de travail
     */
    public static String recupererDonneesEspaceDeTravail(EspaceDeTravail espaceDeTravail)
    {
        String donnees = "";

        espaceDeTravail.setCode(preferences.getString(PREFERENCES_CODE, null));
        espaceDeTravail.setEstFavori(preferences.getBoolean(PREFERENCES_EST_FAVORI, false));

        donnees = espaceDeTravail.toJSON();

        return donnees;
    }

    /**
     * @brief Enregistre les données d'un espace de travail
     */
    public static void sauvegarderDonneesEspaceDeTravail(EspaceDeTravail espaceDeTravail)
    {
        preferences.edit().putString(PREFERENCES_CODE, espaceDeTravail.getCode()).apply();
        preferences.edit().putBoolean(PREFERENCES_EST_FAVORI, espaceDeTravail.getEstFavori()).apply();
    }

    /**
     * @brief Utilitaire de recherche insensible à la casse
     * @author Thierry Vaira
     */
    public static boolean containsIgnoreCase(String str, String searchStr)
    {
        if(str == null || searchStr == null) return false;

        final int length = searchStr.length();
        if (length == 0)
            return true;

        for (int i = str.length() - length; i >= 0; i--)
        {
            if (str.regionMatches(true, i, searchStr, 0, length))
                return true;
        }
        return false;
    }
}
