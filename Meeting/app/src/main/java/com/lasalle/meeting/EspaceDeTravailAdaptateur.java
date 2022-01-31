package com.lasalle.meeting;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Vector;

/**
 * @file EspaceDeTravailAdaptateur.java
 * @brief Déclaration de la classe EspaceDeTravailAdaptateur
 * @author KELLER-LAVALLEE Joachim
 * $LastChangedRevision: 86 $
 * $LastChangedDate: 2021-06-04 17:29:36 +0200 (ven. 04 juin 2021) $
 */

/**
 * @class EspaceDeTravailAdaptateur
 * @brief L'affichage d'un espace de travail dans la liste des espaces de travail sur la page d'accueil
 */
public class EspaceDeTravailAdaptateur extends ArrayAdapter<EspaceDeTravail>
{
    private static final String TAG = "_EspaceDeTravailAdaptateur";

    public EspaceDeTravailAdaptateur(Context context, int resource, Vector<EspaceDeTravail> espacesDeTravail)
    {
        super(context, resource, espacesDeTravail);
        Log.d(TAG, "EspaceDeTravailAdaptateur()");
    }

    private static class ViewHolder
    {
        TextView nomEspaceDeTravail;
        TextView descriptionEspaceDeTravail;
        TextView disponibiliteEspaceDeTravail;
        TextView indiceDeConfortEspaceDeTravail;
        ImageView favoriEspaceDeTravail;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent)
    {
        EspaceDeTravail espaceDeTravail = null;
        ViewHolder viewHolder;

        if (view == null)
        {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.element_espace_travail, parent, false);
            viewHolder.nomEspaceDeTravail = (TextView)view.findViewById(R.id.nomEspaceDeTravail);
            viewHolder.descriptionEspaceDeTravail = (TextView)view.findViewById(R.id.descriptionEspaceDeTravail);
            viewHolder.disponibiliteEspaceDeTravail = (TextView)view.findViewById(R.id.disponibiliteEspaceDeTravail);
            viewHolder.indiceDeConfortEspaceDeTravail = (TextView)view.findViewById(R.id.indiceDeConfortEspaceDeTravail);
            viewHolder.favoriEspaceDeTravail = (ImageView) view.findViewById(R.id.favoriEspaceDeTravail);
            view.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder)view.getTag();
        }

        espaceDeTravail = getItem(position);
        if (espaceDeTravail != null)
        {
            //Log.d(TAG, "Nom : " + espaceDeTravail.getNom());
            viewHolder.nomEspaceDeTravail.setText(espaceDeTravail.getNom());
            viewHolder.descriptionEspaceDeTravail.setText(espaceDeTravail.getDescription());

            if(!espaceDeTravail.getEstReserve())
            {
                viewHolder.disponibiliteEspaceDeTravail.setText("Libre");
                viewHolder.disponibiliteEspaceDeTravail.setTextColor(Color.parseColor("#00FF00")); // Color.rgb(0,255,0)
            }
            else
            {
                viewHolder.disponibiliteEspaceDeTravail.setText("Occupé");
                viewHolder.disponibiliteEspaceDeTravail.setTextColor(Color.rgb(255,0,0));
            }

            switch(espaceDeTravail.getIndiceDeConfort())
            {
                case EspaceDeTravail.INDICE_CHAUD:
                    viewHolder.indiceDeConfortEspaceDeTravail.setText("Chaud");
                    break;

                case EspaceDeTravail.INDICE_TIEDE:
                    viewHolder.indiceDeConfortEspaceDeTravail.setText("Tiède");
                    break;

                case EspaceDeTravail.INDICE_LEGEREMENT_TIEDE:
                    viewHolder.indiceDeConfortEspaceDeTravail.setText("Légèrement tiède");
                    break;

                case EspaceDeTravail.INDICE_NEUTRE:
                    viewHolder.indiceDeConfortEspaceDeTravail.setText("Neutre");
                    break;

                case EspaceDeTravail.INDICE_LEGEREMENT_FRAIS:
                    viewHolder.indiceDeConfortEspaceDeTravail.setText("Légèrement frais");
                    break;

                case EspaceDeTravail.INDICE_FRAIS:
                    viewHolder.indiceDeConfortEspaceDeTravail.setText("Frais");
                    break;

                case EspaceDeTravail.INDICE_FROID:
                    viewHolder.indiceDeConfortEspaceDeTravail.setText("Froid");
                    break;
            }

            if(!espaceDeTravail.getEstFavori())
            {
                viewHolder.favoriEspaceDeTravail.setVisibility(View.INVISIBLE);
            }
            else
            {
                viewHolder.favoriEspaceDeTravail.setVisibility(View.VISIBLE);
            }
        }

        return view;
    }
}
