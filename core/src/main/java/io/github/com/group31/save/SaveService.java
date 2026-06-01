package io.github.com.group31.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

public class SaveService {
    private static final String SAVE_FILE = "save.json";
    private final Json json;

    public SaveService() {
        this.json = new Json();
    }

    public boolean hasSaveFile() {
        return Gdx.files.local(SAVE_FILE).exists();
    }

    public void clearSave() {
        if (hasSaveFile()) {
            Gdx.files.local(SAVE_FILE).delete();
        }
    }

    public void save(SaveData data) {
        FileHandle file = Gdx.files.local(SAVE_FILE);
        String jsonStr = json.toJson(data);
        file.writeString(jsonStr, false);
    }

    public SaveData load() {
        if (!hasSaveFile()) return null;
        FileHandle file = Gdx.files.local(SAVE_FILE);
        return json.fromJson(SaveData.class, file.readString());
    }
}
