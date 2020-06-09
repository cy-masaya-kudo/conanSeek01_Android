package jp.co.cybird.android.conanseek.activity.effect;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.EventListener;

import jp.souling.android.conanseek01.R;

/**
 * machine出現
 */
public class MachineEffect extends Fragment {

    FrameLayout background;

    ImageView machine;
    ImageView machineWhite;

    ImageView halo1;
    ImageView halo2;
    ImageView halo3;

    ImageView shine;

    int step = 0;


    @Override
    public void onDestroy() {
        super.onDestroy();

        machine.setImageDrawable(null);
        machineWhite.setImageDrawable(null);
        halo1.setImageDrawable(null);
        halo2.setImageDrawable(null);
        halo3.setImageDrawable(null);
        shine.setImageDrawable(null);
    }

    public static MachineEffect newInstance() {
        
        Bundle args = new Bundle();
        
        MachineEffect fragment = new MachineEffect();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.effect_machine, container, false);

        background = (FrameLayout) view.findViewById(R.id.background);
        background.setAlpha(0f);

        machine = (ImageView) view.findViewById(R.id.machine);
        machineWhite = (ImageView) view.findViewById(R.id.machine_white);

        halo1 = (ImageView) view.findViewById(R.id.halo1);
        halo2 = (ImageView) view.findViewById(R.id.halo2);
        halo3 = (ImageView) view.findViewById(R.id.halo3);

        shine = (ImageView) view.findViewById(R.id.shine);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //background
        ViewCompat.animate(background)
                .alpha(1f)
                .setDuration(300)
                .setListener(listener)
                .start();

    }

    ViewPropertyAnimatorListenerAdapter listener = new ViewPropertyAnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(View view) {
            super.onAnimationEnd(view);

            step++;

            if (step == 1) {
                haloAnimation();
                whiteAppear();
                shining();
            } else if (step == 2) {
                haloAnimation();
                whiteThin();
                machine.setVisibility(View.GONE);
            } else if (step == 3) {

                if (awayListener != null)
                    awayListener.machineDidFlyaway();
            }
        }
    };

    private void whiteThin() {
        ViewCompat.animate(machineWhite)
                .scaleX(.4f)
                .scaleY(1.6f)
                .setInterpolator(new BounceInterpolator())
                .setDuration(80)
                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(View view) {
                        super.onAnimationEnd(view);

                        ViewCompat.animate(machineWhite)
                                .scaleX(1.2f)
                                .scaleY(.8f)
                                .setInterpolator(new BounceInterpolator())
                                .setDuration(80)
                                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(View view) {
                                        super.onAnimationEnd(view);

                                        ViewCompat.animate(machineWhite)
                                                .scaleX(.14f)
                                                .scaleY(4.2f)
                                                .setInterpolator(new BounceInterpolator())
                                                .setDuration(80)
                                                .setListener(listener)
                                                .start();
                                    }
                                })
                                .start();
                    }
                })
                .start();
    }

    private void whiteAppear() {
        ViewCompat.animate(machineWhite)
                .alpha(1.0f)
                .setDuration(3000)
                .setListener(listener)
                .start();
    }

    private void shining() {
        ViewCompat.animate(shine)
                .setDuration(6000)
                .alpha(1.0f)
                .rotation(360 * 3)
                .start();
    }

    private void haloAnimation() {
        halo1.setAlpha(0f);
        halo2.setAlpha(0f);
        halo3.setAlpha(0f);
        halo1.setScaleX(0f);
        halo2.setScaleX(0f);
        halo3.setScaleX(0f);
        halo1.setScaleY(0f);
        halo2.setScaleY(0f);
        halo3.setScaleY(0f);

        float scale = 6.0f;

        ViewCompat.animate(halo1)
                .setDuration(400).scaleX(scale).scaleY(scale).alpha(1f)
                .setStartDelay(0)
                .start();
        ViewCompat.animate(halo2)
                .setDuration(400).scaleX(scale).scaleY(scale).alpha(1f)
                .setStartDelay(80)
                .start();
        ViewCompat.animate(halo3)
                .setDuration(400).scaleX(scale).scaleY(scale).alpha(1f)
                .setStartDelay(100)
                .start();

    }


    public interface MachineListener extends EventListener {
        void machineDidFlyaway();
    }

    protected MachineListener awayListener = null;

    public void setMachineListener(MachineListener l) {
        awayListener = l;
    }


}
