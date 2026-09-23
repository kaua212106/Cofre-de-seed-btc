package com.kaua.cofrebtc;

import android.content.Context;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

final class VaultStore {
    private final File file;
    VaultStore(Context c) { file = new File(c.getFilesDir(), "cofre.btc"); }
    boolean exists() { return file.exists(); }

    void save(JSONArray wallets, char[] password) throws Exception {
        JSONObject root = new JSONObject();
        root.put("version", 1);
        root.put("wallets", wallets);
        String blob = VaultCrypto.encrypt(root.toString(), password);
        File tmp = new File(file.getParentFile(), "cofre.btc.tmp");
        Files.write(tmp.toPath(), blob.getBytes(StandardCharsets.US_ASCII));
        if (!tmp.renameTo(file)) throw new Exception("Não foi possível finalizar o cofre");
    }

    JSONArray load(char[] password) throws Exception {
        String blob = new String(Files.readAllBytes(file.toPath()), StandardCharsets.US_ASCII);
        JSONObject root = new JSONObject(VaultCrypto.decrypt(blob, password));
        return root.getJSONArray("wallets");
    }
}
