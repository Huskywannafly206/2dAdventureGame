package io.github.com.quillraven.ui.view;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.github.tommyettinger.textra.TextraLabel;
import com.github.tommyettinger.textra.TypingLabel;
import io.github.com.quillraven.ui.model.GameViewModel;

import java.util.Map;

public class GameView extends View<GameViewModel> {
    private final HorizontalGroup lifeGroup;
    private ProgressBar xpBar;
    private Label levelLabel;

    public GameView(Stage stage, Skin skin, GameViewModel viewModel) {
        super(stage, skin, viewModel);

        this.lifeGroup = findActor("lifeGroup");
        updateLife(viewModel.getLifePoints());
        updateXp(viewModel.getXp());
        updateLevel(viewModel.getLevel());
    }

    @Override
    protected void setupPropertyChanges() {
        viewModel.onPropertyChange(GameViewModel.LIFE_POINTS, Integer.class, this::updateLife);
        viewModel.onPropertyChange(GameViewModel.PLAYER_DAMAGE, Map.Entry.class, this::showDamage);
        viewModel.onPropertyChange(GameViewModel.XP_CHANGED, Float.class, this::updateXp);
        viewModel.onPropertyChange(GameViewModel.LEVEL_CHANGED, Integer.class, this::updateLevel);
    }

    @Override
    protected void setupUI() {
        setFillParent(true);

        // ── Bottom-left: life hearts ──
        Table bottomLeft = new Table();
        bottomLeft.align(Align.bottomLeft);

        HorizontalGroup horizontalGroup = new HorizontalGroup();
        horizontalGroup.setName("lifeGroup");
        horizontalGroup.padLeft(5.0f);
        horizontalGroup.padBottom(5.0f);
        horizontalGroup.space(5.0f);
        bottomLeft.add(horizontalGroup).left().row();

        // ── XP bar row ──
        Table xpRow = new Table();
        levelLabel = new Label("Lv.1", skin, "small");
        levelLabel.setName("levelLabel");
        xpRow.add(levelLabel).padLeft(5f).padBottom(3f);

        // ProgressBar uses "default-horizontal" style from the skin
        xpBar = new ProgressBar(0f, 100f, 0.5f, false, skin);
        xpBar.setName("xpBar");
        xpRow.add(xpBar).width(80f).padLeft(4f).padBottom(3f);
        bottomLeft.add(xpRow).left();

        add(bottomLeft).expand().align(Align.bottomLeft);
    }

    /**
     * Updates the life display with appropriate heart icons.
     */
    private void updateLife(int lifePoints) {
        lifeGroup.clear();

        int maxLife = viewModel.getMaxLife();
        while (maxLife > 0) {
            int imgIdx = MathUtils.clamp(lifePoints, 0, 4);
            Image image = new Image(skin, "life_0" + imgIdx);
            lifeGroup.addActor(image);

            maxLife -= 4;
            lifePoints -= 4;
        }
    }

    private void updateXp(float xp) {
        if (xpBar == null) return;
        float ratio = viewModel.getXpToNextLevel() > 0
            ? (xp / viewModel.getXpToNextLevel()) * 100f
            : 0f;
        xpBar.setValue(ratio);
    }

    private void updateLevel(int level) {
        if (levelLabel == null) return;
        levelLabel.setText("Lv." + level);
    }

    private Vector2 toStageCoords(Vector2 gamePosition) {
        Vector2 resultPosition = viewModel.toScreenCoords(gamePosition);
        stage.getViewport().unproject(resultPosition);
        resultPosition.y = stage.getViewport().getWorldHeight() - resultPosition.y;
        return resultPosition;
    }

    /**
     * Shows animated damage text at the specified position.
     */
    private void showDamage(Map.Entry<Vector2, Integer> damAndPos) {
        final Vector2 position = damAndPos.getKey();
        int damage = damAndPos.getValue();

        TextraLabel textraLabel = new TypingLabel("[%75]{JUMP=2.0;0.5;0.9}{RAINBOW}" + damage, skin, "small");
        stage.addActor(textraLabel);

        textraLabel.addAction(
            Actions.parallel(
                Actions.sequence(Actions.delay(1.25f), Actions.removeActor()),
                Actions.forever(Actions.run(() -> {
                    Vector2 stageCoords = toStageCoords(position);
                    textraLabel.setPosition(stageCoords.x, stageCoords.y);
                }))
            )
        );
    }
}
