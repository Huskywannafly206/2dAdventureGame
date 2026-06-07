package io.github.com.group31.puzzle;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import io.github.com.group31.component.Graphic;
import io.github.com.group31.component.Physic;
import io.github.com.group31.component.Tiled;
import io.github.com.group31.ui.model.GameViewModel;

public class ScarecrowPuzzleManager {
    public static final ScarecrowPuzzleManager INSTANCE = new ScarecrowPuzzleManager();

    private int hits1 = 0;
    private int hits2 = 0;
    private int hits3 = 0;
    private boolean solved = false;

    private Entity silverCupEntity = null;
    private GameViewModel viewModel = null;

    private ScarecrowPuzzleManager() {
    }

    public void reset() {
        hits1 = 0;
        hits2 = 0;
        hits3 = 0;
        solved = false;
        silverCupEntity = null;
    }

    public void setViewModel(GameViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public void registerSilverCup(Entity entity) {
        this.silverCupEntity = entity;
        if (!solved) {
            hideSilverCup();
        } else {
            showSilverCup();
        }
    }

    private void hideSilverCup() {
        if (silverCupEntity != null) {
            Graphic g = Graphic.MAPPER.get(silverCupEntity);
            if (g != null) {
                g.getColor().a = 0f;
            }
            Physic p = Physic.MAPPER.get(silverCupEntity);
            if (p != null && p.getBody() != null) {
                p.getBody().setActive(false);
            }
        }
    }

    public void showSilverCup() {
        if (silverCupEntity != null) {
            Graphic g = Graphic.MAPPER.get(silverCupEntity);
            if (g != null) {
                g.getColor().a = 1f;
            }
            Physic p = Physic.MAPPER.get(silverCupEntity);
            if (p != null && p.getBody() != null) {
                p.getBody().setActive(true);
            }
            if (viewModel != null && silverCupEntity != null) {
                Tiled tiled = Tiled.MAPPER.get(silverCupEntity);
                if (tiled != null && tiled.getMapObjectRef() != null) {
                    float cx = tiled.getMapObjectRef().getProperties().get("x", 0f, Float.class) * io.github.com.group31.GdxGame.UNIT_SCALE;
                    float cy = tiled.getMapObjectRef().getProperties().get("y", 0f, Float.class) * io.github.com.group31.GdxGame.UNIT_SCALE;
                    viewModel.showFloatingText("[GOLD]Silver Cup xuat hien![]", cx, cy + 1f);
                }
            }
        }
    }

    public int getHits(int index) {
        return switch (index) {
            case 1 -> hits1;
            case 2 -> hits2;
            case 3 -> hits3;
            default -> 0;
        };
    }

    public void onScarecrowHit(int index, Engine engine) {
        if (solved) return;

        if (index == 1) {
            hits1 = (hits1 + 1) % 2; // wraps 0 -> 1 -> 0
        } else if (index == 2) {
            hits2 = (hits2 + 1) % 3; // wraps 0 -> 1 -> 2 -> 0
        } else if (index == 3) {
            hits3 = (hits3 + 1) % 4; // wraps 0 -> 1 -> 2 -> 3 -> 0
        }

        if (viewModel != null) {
            viewModel.scarecrowHitsChanged(index, getHits(index));
        }

        if (hits1 == 1 && hits2 == 2 && hits3 == 3) {
            solved = true;
            showSilverCup();
        }
    }

    public boolean isSolved() {
        return solved;
    }
}
