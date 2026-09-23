package com.kaua.cofrebtc;

import android.app.*;
import android.os.Bundle;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.content.*;
import android.graphics.Color;
import android.text.InputType;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Arrays;

public class MainActivity extends Activity {
    private LinearLayout content;
    private VaultStore store;
    private JSONArray wallets;
    private char[] sessionPassword;
    private boolean unlocked = false;

    @Override public void onCreate(Bundle b) { super.onCreate(b); getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE); setContentView(R.layout.activity_main); content=findViewById(R.id.content); store=new VaultStore(this); showLock(); }
    @Override protected void onPause(){ super.onPause(); if (unlocked) lock(); }

    private TextView label(String s){ TextView t=new TextView(this); t.setText(s); t.setTextColor(Color.WHITE); t.setTextSize(16); t.setPadding(0,10,0,10); return t; }
    private EditText field(String hint, boolean password){ EditText e=new EditText(this); e.setHint(hint); e.setTextColor(Color.WHITE); e.setHintTextColor(Color.rgb(150,150,160)); e.setSingleLine(false); e.setInputType(password ? (InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD) : InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE); e.setPadding(18,14,18,14); return e; }
    private Button btn(String text){ Button b=new Button(this); b.setText(text); b.setAllCaps(false); b.setMinHeight(52); return b; }
    private void clear(){ content.removeAllViews(); }

    private void showLock(){ clear(); TextView info=label(store.exists()?"Digite a senha mestra para abrir o cofre.":"Crie uma senha mestra forte. Ela não é salva."); content.addView(info); EditText p=field("Senha mestra",true); content.addView(p); if(!store.exists()){ EditText p2=field("Repita a senha",true); content.addView(p2); Button create=btn("Criar cofre"); content.addView(create); create.setOnClickListener(v->{ if(p.getText().length()<12){toast("Use pelo menos 12 caracteres.");return;} if(!p.getText().toString().equals(p2.getText().toString())){toast("As senhas não coincidem.");return;} sessionPassword=p.getText().toString().toCharArray(); wallets=new JSONArray(); try{store.save(wallets,sessionPassword); unlocked=true; showVault();}catch(Exception e){lock();toast("Erro ao criar cofre.");}}); } else { Button open=btn("Entrar"); content.addView(open); open.setOnClickListener(v->{ char[] pass=p.getText().toString().toCharArray(); try{ wallets=store.load(pass); sessionPassword=pass; unlocked=true; showVault(); }catch(Exception e){VaultCrypto.wipe(pass);toast("Senha incorreta ou cofre corrompido.");}}); } p.requestFocus(); }

    private void showVault(){ clear(); content.addView(label("Carteiras armazenadas: "+wallets.length())); for(int i=0;i<wallets.length();i++){ final int idx=i; try{ JSONObject w=wallets.getJSONObject(i); Button open=btn("🟠 "+w.optString("name","Carteira")); content.addView(open); open.setOnClickListener(v->showWallet(idx)); }catch(Exception ignored){} } Button add=btn("＋ Adicionar carteira"); content.addView(add); add.setOnClickListener(v->addWallet()); Button lock=btn("🔒 Bloquear"); content.addView(lock); lock.setOnClickListener(v->lock()); }

    private void addWallet(){ clear(); content.addView(label("Nova carteira")); EditText name=field("Nome (ex.: Bitcoin principal)",false); content.addView(name); EditText seed=field("Seed phrase — nunca compartilhe",false); seed.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE); content.addView(seed); Button save=btn("Salvar criptografada"); content.addView(save); save.setOnClickListener(v->{String n=name.getText().toString().trim(),s=seed.getText().toString().trim(); if(n.isEmpty()||s.isEmpty()){toast("Preencha nome e seed.");return;} try{JSONObject w=new JSONObject();w.put("name",n);w.put("seed",s);wallets.put(w);store.save(wallets,sessionPassword);seed.setText("");showVault();toast("Carteira salva no cofre criptografado.");}catch(Exception e){toast("Erro ao salvar.");}}); Button back=btn("Cancelar"); content.addView(back); back.setOnClickListener(v->showVault()); }

    private void showWallet(int idx){ clear(); try{JSONObject w=wallets.getJSONObject(idx); content.addView(label(w.optString("name"))); TextView warning=label("⚠️ Nunca tire print nem envie esta seed para ninguém."); content.addView(warning); Button reveal=btn("👁 Mostrar seed"); content.addView(reveal); reveal.setOnClickListener(v->{ reveal.setEnabled(false); EditText box=field("Seed",false); box.setText(w.optString("seed")); box.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE); content.addView(box,1); }); Button copy=btn("Copiar seed"); content.addView(copy); copy.setOnClickListener(v->{ ClipboardManager cm=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE); cm.setPrimaryClip(ClipData.newPlainText("seed",w.optString("seed"))); toast("Copiada. Apague o clipboard após usar."); }); Button del=btn("Excluir carteira"); content.addView(del); del.setOnClickListener(v->{new AlertDialog.Builder(this).setTitle("Excluir carteira?").setMessage("A seed será removida do cofre após salvar a alteração.").setNegativeButton("Cancelar",null).setPositiveButton("Excluir",(d,x)->{wallets.remove(idx);try{store.save(wallets,sessionPassword);showVault();}catch(Exception e){toast("Erro ao salvar.");}}).show();}); Button back=btn("Voltar"); content.addView(back); back.setOnClickListener(v->showVault()); }catch(Exception e){showVault();} }

    private void lock(){ unlocked=false; if(sessionPassword!=null){Arrays.fill(sessionPassword,'\0');sessionPassword=null;} wallets=null; showLock(); }
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
}
