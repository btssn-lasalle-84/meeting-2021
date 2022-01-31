package com.lasalle.meeting;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Arrays;
import java.util.List;

/**
 * @file ModificationEspaceDeTravail.java
 * @brief Déclaration de la classe ModificationEspaceDeTravail
 * @author KELLER-LAVALLEE Joachim
 */

/**
 * @class ModificationEspaceDeTravail
 * @brief L'activité de modification d'un espace de travail de l'application Meeting
 */
public class ModificationEspaceDeTravail extends AppCompatActivity
{
    /**
     * Les constantes
     */
    private static final String TAG = "_ModificationEspaceDeTravail"; //!< TAG pour les logs

    /**
     * Les widgets
     */
    EditText editionNom;
    EditText editionLieu;
    EditText editionDescription;
    EditText editionSuperficie;

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
        setContentView(R.layout.activity_modification_espace_de_travail);
        Intent intent = getIntent();
        espaceDeTravail = (EspaceDeTravail)intent.getSerializableExtra("unEspaceDeTravail");
        espaceDeTravail.initialiserCommunication(handler);

        afficherEditionNom();
        afficherEditionLieu();
        afficherEditionDescription();
        afficherEditionSuperficie();
        afficherBoutons();
    }

    /**
     * @brief Affiche la zone d'édition du nom de l'espace de travail
     */
    public void afficherEditionNom()
    {
        editionNom = (EditText) findViewById(R.id.editionNom);
        editionNom.setText(espaceDeTravail.getNom());

        Log.d(TAG, "afficherEditionNom() " + espaceDeTravail.getNom());
    }
    /**
     * @brief Affiche la zone d'édition du lieu de l'espace de travail
     */
    public void afficherEditionLieu()
    {
        editionLieu = (EditText) findViewById(R.id.editionLieu);
        editionLieu.setText(espaceDeTravail.getLieu());

        Log.d(TAG, "afficherEditionLieu() " + espaceDeTravail.getLieu());
    }

    /**
     * @brief Affiche la zone d'édition de la description de l'espace de travail
     */
    public void afficherEditionDescription()
    {
        editionDescription = (EditText) findViewById(R.id.editionDescription);
        editionDescription.setText(espaceDeTravail.getDescription());

        Log.d(TAG, "afficherEditionDescription() " + espaceDeTravail.getDescription());
    }

    /**
     * @brief Affiche la zone d'édition de la superficie de l'espace de travail
     */
    public void afficherEditionSuperficie()
    {
        editionSuperficie = (EditText) findViewById(R.id.editionSuperficie);
        int superficie = espaceDeTravail.getSuperficie();
        editionSuperficie.setText(String.valueOf(superficie));

        Log.d(TAG, "afficherEditionSuperficie() " + espaceDeTravail.getSuperficie());
    }

    /**
     * @brief Affiche le bouton "Enregistrer"
     */
    public void afficherBoutons()
    {
        Button boutonEnregistrer = (Button)findViewById(R.id.boutonEnregistrer);

        boutonEnregistrer.setOnClickListener(
            new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    String champs[] = new String[] { editionNom.getText().toString(), editionDescription.getText().toString(), editionLieu.getText().toString(), editionSuperficie.getText().toString() };
                    List<String> parametres = Arrays.asList(champs);
                    espaceDeTravail.modifierInformations(parametres);

                    finish();
                }
            }
        );
    }

    /**
     * @brief Termine l'activité de modification d'un espace de travail
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
                    Log.d(TAG, "handleMessage() : typeTrame : " + typeTrame);

                    break;
                default:
                    Log.d(TAG,"handleMessage() : code inconnu ! ");
            }
        }
    };
}
