package com.example.accurancymobileapp.ui;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.accurancymobileapp.R;
public class AnimationSpinner {
        private AnimationSpinner() {
            // Classe utilitária: não precisa ser instanciada.
        }

        public interface AoSelecionar {
            void selecionar(int posicao);
        }

        public static void configurar(
                @NonNull Context context,
                @NonNull Spinner spinner,
                @NonNull String[] opcoes,
                @NonNull AoSelecionar callback
        ) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    context,
                    R.layout.item_spinner_destaques,
                    opcoes
            ) {
                @NonNull
                @Override
                public View getView(
                        int position,
                        View convertView,
                        @NonNull ViewGroup parent
                ) {
                    TextView item;

                    if (convertView instanceof TextView) {
                        item = (TextView) convertView;
                    } else {
                        item = (TextView) LayoutInflater.from(context)
                                .inflate(
                                        R.layout.item_spinner_destaques,
                                        parent,
                                        false
                                );
                    }

                    item.setText(getItem(position));
                    return item;
                }

                @Override
                public View getDropDownView(
                        int position,
                        View convertView,
                        @NonNull ViewGroup parent
                ) {
                    TextView item;

                    if (convertView instanceof TextView) {
                        item = (TextView) convertView;
                    } else {
                        item = (TextView) LayoutInflater.from(context)
                                .inflate(
                                        R.layout.item_spinner_destaques_dropdown,
                                        parent,
                                        false
                                );
                    }

                    item.setText(getItem(position));
                    return item;
                }
            };

            spinner.setAdapter(adapter);

            spinner.setPopupBackgroundDrawable(
                    context.getDrawable(R.drawable.bg_spinner_popup)
            );

            spinner.setDropDownVerticalOffset(
                    (int) (4 * context.getResources()
                            .getDisplayMetrics().density)
            );

            // Ignora a seleção inicial automática do Android.
            spinner.setOnTouchListener((view, event) -> {
                if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                    view.setTag(R.id.tag_spinner_animado, true);
                }
                return false;
            });

            spinner.setOnItemSelectedListener(
                    new android.widget.AdapterView.OnItemSelectedListener() {

                        @Override
                        public void onItemSelected(
                                android.widget.AdapterView<?> parent,
                                View view,
                                int position,
                                long id
                        ) {
                            Object tag = spinner.getTag(R.id.tag_spinner_animado);

                            if (Boolean.TRUE.equals(tag)) {
                                spinner.setTag(R.id.tag_spinner_animado, false);
                                animarSelecao(spinner);
                            }

                            callback.selecionar(position);
                        }

                        @Override
                        public void onNothingSelected(
                                android.widget.AdapterView<?> parent
                        ) {
                        }
                    }
            );
        }

        private static void animarSelecao(View view) {
            ObjectAnimator reduzirX =
                    ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 0.97f, 1f);

            ObjectAnimator reduzirY =
                    ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, 0.97f, 1f);

            ObjectAnimator fade =
                    ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0.75f, 1f);

            AnimatorSet animacao = new AnimatorSet();
            animacao.playTogether(reduzirX, reduzirY, fade);
            animacao.setDuration(180);
            animacao.start();
        }
}
