package com.lasalle.meeting;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import java.io.Serializable;

/**
 * @file AffichageEspaceDeTravail.java
 * @brief Déclaration de la classe AffichageEspaceDeTravail
 * @author KELLER-LAVALLEE Joachim
 * $LastChangedRevision: 93 $
 * $LastChangedDate: 2021-06-10 16:36:23 +0200 (jeu. 10 juin 2021) $
 */

/**
 * @class AffichageEspaceDeTravail
 * @brief L'activité d'affichage d'un espace de travail de l'application Meeting
 */
public class AffichageEspaceDeTravail extends AppCompatActivity
{
    /**
     * Les constantes
     */
    private static final String TAG = "_AffichageEspaceTravail"; //!< TAG pour les logs

    /**
     * Les attributs
     */
    private EspaceDeTravail espaceDeTravail; //!< L'espace de travail

    /**
     * @brief Méthode appelée à la création de l'activité
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_affichage_espace_de_travail);
        Intent intent = getIntent();
        espaceDeTravail = (EspaceDeTravail)intent.getSerializableExtra("unEspaceDeTravail");
        espaceDeTravail.initialiserCommunication(handler);

        afficher();
    }

    /**
     * @brief Affiche les propriétés de l'espace de travail
     */
    private void afficher()
    {
        afficherAdresseIP();
        afficherNom();
        afficherLieu();
        afficherDescription();
        afficherSuperficie();
        afficherTemperature();
        afficherIndiceDeConfort();
        afficherDisponibilite();
        afficherFavori();
        afficherBoutons();
    }

    /**
     * @brief Affiche l'adresse IP du portier
     */
    public void afficherAdresseIP()
    {
        TextView affichageAdresseIP = (TextView)findViewById(R.id.affichageAdresseIP);
        affichageAdresseIP.setText(espaceDeTravail.getAdresseIP());

        Log.d(TAG, "afficherAdresseIP() " + espaceDeTravail.getAdresseIP());
    }

    /**
     * @brief Affiche le nom de l'espace de travail
     */
    public void afficherNom()
    {
        TextView affichageNom = (TextView)findViewById(R.id.affichageNom);
        affichageNom.setText(espaceDeTravail.getNom());

        Log.d(TAG, "afficherNom() " + espaceDeTravail.getNom());
    }

    /**
     * @brief Affiche le lieu de l'espace de travail
     */
    public void afficherLieu()
    {
        TextView affichageLieu = (TextView)findViewById(R.id.affichageLieu);
        affichageLieu.setText(espaceDeTravail.getLieu());

        Log.d(TAG, "afficherLieu() " + espaceDeTravail.getLieu());
    }

    /**
     * @brief Affiche la description de l'espace de travail
     */
    public void afficherDescription()
    {
        TextView affichageDescription = (TextView)findViewById(R.id.affichageDescription);
        affichageDescription.setText(espaceDeTravail.getDescription());

        Log.d(TAG, "afficherDescription() " + espaceDeTravail.getDescription());
    }

    /**
     * @brief Affiche la superficie de l'espace de travail
     */
    public void afficherSuperficie()
    {
        TextView affichageSuperficie = (TextView)findViewById(R.id.affichageSuperficie);
        affichageSuperficie.setText(String.valueOf(espaceDeTravail.getSuperficie()));

        Log.d(TAG, "afficherSuperficie() " + espaceDeTravail.getSuperficie());
    }

    /**
     * @brief Affiche la température de l'espace de travail
     */
    public void afficherTemperature()
    {
        TextView affichageTemperature = (TextView)findViewById(R.id.affichageTemperature);
        affichageTemperature.setText(String.valueOf(espaceDeTravail.getTemperature()));

        Log.d(TAG, "afficherTemperature() " + espaceDeTravail.getTemperature());
    }

    /**
     * @brief Affiche l'indice de confort de l'espace de travail
     */
    public void afficherIndiceDeConfort()
    {
        TextView affichageIndiceDeConfort = (TextView)findViewById(R.id.affichageIndiceDeConfort);

        switch(espaceDeTravail.getIndiceDeConfort())
        {
            case EspaceDeTravail.INDICE_CHAUD:
                affichageIndiceDeConfort.setText("Chaud");
                break;

            case EspaceDeTravail.INDICE_TIEDE:
                affichageIndiceDeConfort.setText("Tiède");
                break;

            case EspaceDeTravail.INDICE_LEGEREMENT_TIEDE:
                affichageIndiceDeConfort.setText("Légèrement tiède");
                break;

            case EspaceDeTravail.INDICE_NEUTRE:
                affichageIndiceDeConfort.setText("Neutre");
                break;

            case EspaceDeTravail.INDICE_LEGEREMENT_FRAIS:
                affichageIndiceDeConfort.setText("Légèrement frais");
                break;

            case EspaceDeTravail.INDICE_FRAIS:
                affichageIndiceDeConfort.setText("Frais");
                break;

            case EspaceDeTravail.INDICE_FROID:
                affichageIndiceDeConfort.setText("Froid");
                break;
        }

        Log.d(TAG, "afficherIndiceDeConfort() " + espaceDeTravail.getIndiceDeConfort());
    }

    /**
     * @brief Affiche si l'espace de travail est en favori
     */
    public void afficherFavori()
    {
        ImageView iconeFavori = (ImageView)findViewById(R.id.iconeFavori);

        if(!espaceDeTravail.getEstFavori())
        {
            Log.d(TAG, "afficherFavori() Non favori");
            iconeFavori.setVisibility(View.INVISIBLE);
        }
        else
        {
            Log.d(TAG, "afficherFavori() Favori");
            iconeFavori.setVisibility(View.VISIBLE);
        }

        Log.d(TAG, "afficherFavori() " + espaceDeTravail.getEstFavori());
    }

    /**
     * @brief Affiche la disponibilité de l'espace de travail
     */
    public void afficherDisponibilite()
    {
        TextView affichageDisponibilite = (TextView)findViewById(R.id.affichageDisponibilite);

        if(!espaceDeTravail.getEstReserve())
        {
            Log.d(TAG, "afficherDisponibilite() Libre");
            affichageDisponibilite.setText("Libre");
            affichageDisponibilite.setTextColor(Color.parseColor("#00FF00")); // Color.rgb(0,255,0)
        }
        else
        {
            Log.d(TAG, "afficherDisponibilite() Occupé");
            affichageDisponibilite.setText("Occupé");
            affichageDisponibilite.setTextColor(Color.rgb(255,0,0));
        }

        Log.d(TAG, "afficherDisponibilite() " + espaceDeTravail.getEstReserve());
    }

    /**
     * @brief Affiche les boutons "Réserver", "Libérer", "Editer les informations", "Ajouter aux favoris" et "Retirer des favoris"
     */
    public void afficherBoutons()
    {
        Button boutonReserver = (Button)findViewById(R.id.boutonReserver);
        Button boutonLiberer = (Button)findViewById(R.id.boutonLiberer);
        Button boutonModifier = (Button)findViewById(R.id.boutonModifier);
        Button boutonAjouterFavori = (Button)findViewById(R.id.boutonAjouterFavori);
        Button boutonRetirerFavori = (Button)findViewById(R.id.boutonRetirerFavori);

        boutonReserver.setOnClickListener(
            new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    espaceDeTravail.reserver();
                    afficherDisponibilite();
                    afficherBoutons();
                }
            }
        );

        boutonLiberer.setOnClickListener(
            new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    afficherBoiteLiberation();
                    afficherDisponibilite();
                    afficherBoutons();
                }
            }
        );

        if(!espaceDeTravail.getEstReserve())
        {
            boutonReserver.setVisibility(View.VISIBLE);
            boutonLiberer.setVisibility(View.GONE);
        }
        else
        {
            boutonReserver.setVisibility(View.GONE);
            boutonLiberer.setVisibility(View.VISIBLE);
        }

        boutonModifier.setOnClickListener(
            new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    Intent intent = new Intent(AffichageEspaceDeTravail.this, ModificationEspaceDeTravail.class);
                    espaceDeTravail.initialiserCommunication(null);
                    intent.putExtra("unEspaceDeTravail", (Serializable)espaceDeTravail);
                    startActivityForResult(intent, 0);
                }
            }
        );

        boutonAjouterFavori.setOnClickListener(
            new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    espaceDeTravail.setEstFavori(true);
                    afficherFavori();
                    afficherBoutons();
                }
            }
        );

        boutonRetirerFavori.setOnClickListener(
            new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    espaceDeTravail.setEstFavori(false);
                    afficherFavori();
                    afficherBoutons();
                }
            }
        );

        if(!espaceDeTravail.getEstFavori())
        {
            boutonAjouterFavori.setVisibility(View.VISIBLE);
            boutonRetirerFavori.setVisibility(View.GONE);
        }
        else
        {
            boutonAjouterFavori.setVisibility(View.GONE);
            boutonRetirerFavori.setVisibility(View.VISIBLE);
        }
    }

    /**
     * @brief Affiche la boîte de dialogue de saisie du code de libération
     */
    public void afficherBoiteLiberation()
    {
        AlertDialog.Builder boiteLiberation = new AlertDialog.Builder(this);
        boiteLiberation.setTitle("Libérer l'espace de travail");
        boiteLiberation.setMessage("Saisissez le code pour libérer l'espace de travail :");
        LayoutInflater inflater = this.getLayoutInflater();
        View vue = inflater.inflate(R.layout.boite_liberation, null);
        boiteLiberation.setView(vue);

        boiteLiberation.setPositiveButton("Libérer", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                EditText saisieCode = (EditText) ((AlertDialog) dialog).findViewById(R.id.saisieCode);
                Log.d(TAG, "onClick() code = " + saisieCode.getText().toString());
                espaceDeTravail.liberer(saisieCode.getText().toString());
            }
        });
        boiteLiberation.setNegativeButton("Annuler", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {

            }
        });

        boiteLiberation.show();
    }

    /**
     * @brief Traite le retour de l'activité de modification d'un espace de travail
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult() requestCode=" + requestCode + " - resultCode=" + resultCode + "");

        finish();
    }

    /**
     * @brief Termine l'activité d'affichage d'un espace de travail
     */
    @Override
    public void finish()
    {
        Log.d(TAG, "finish()");

        Intent intent = new Intent();
        //intent.putExtra("unEspaceDeTravail", espaceDeTravail);
        setResult(RESULT_OK, intent);
        super.finish();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            Bundle b = msg.getData();
            Log.d(TAG, "handleMessage() " + b);

            switch(msg.what)
            {
                case Communication.TYPE_RECEPTION:
                    String trame = b.getString("donnees");
                    Log.d(TAG, "handleMessage() Réception [" + b.getString("adresseIP") + ":" + b.getInt("port") + "] -> " + trame);

                    String[] champs = trame.split(";");
                    int typeTrame = Communication.recupererTypeTrame(champs);

                    switch(typeTrame)
                    {

                        case Communication.MODIFICATION_DISPONIBILITE:
                            if(espaceDeTravail.getAdresseIP().equals(b.getString("adresseIP")))
                            {
                                if(espaceDeTravail.extraireCode(trame))
                                {
                                    if(espaceDeTravail.getCode().isEmpty())
                                        espaceDeTravail.setEstReserve(false);
                                    else
                                        espaceDeTravail.setEstReserve(true);
                                }
                                afficherDisponibilite();
                                afficherBoutons();
                            }
                            break;

                        case Communication.MODIFICATION_INFORMATIONS:
                            if(espaceDeTravail.getAdresseIP().equals(b.getString("adresseIP")))
                            {
                                if(espaceDeTravail.extraireInformations(trame))
                                {
                                    espaceDeTravail.extraireInformations(trame);
                                }
                                afficherNom();
                                afficherDescription();
                                afficherLieu();
                                afficherSuperficie();
                                afficherDisponibilite();
                                afficherBoutons();
                            }
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
}
