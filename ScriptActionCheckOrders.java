package com.itzblaze.toon;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.eq2online.macros.scripting.api.*;
import net.eq2online.macros.scripting.parser.ScriptAction;
import net.eq2online.macros.scripting.parser.ScriptContext;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@APIVersion(26)
public class ScriptActionCheckOrders extends ScriptAction {
    public ScriptActionCheckOrders() {
        super(ScriptContext.MAIN,"checkorders");
    }

    @Override
    public boolean isThreadSafe() {
        return false;
    }

    @Override
    public IReturnValue execute(IScriptActionProvider provider, IMacro macro, IMacroAction instance, String rawParams, String[] params) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://dizmac.xyz/api/v1/order/newOrder?key=redacted");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    StringBuilder b = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = null;
                    while((line = reader.readLine()) != null) {
                        b.append(line);
                    }
                    if(b.length() > 10) {
                        final JsonParser parser = new JsonParser();
                        JsonObject orderData = (JsonObject) parser.parse(b.toString());
                        String token = orderData.get("token").getAsString();
                        String totalCoins = orderData.get("coins").getAsString();
                        String discordId = orderData.get("id").getAsString();
                        String ign = orderData.get("ign").getAsString();
                        String bin1 = orderData.get("bin1").getAsString();
                        String bin2 = orderData.get("bin2").getAsString();
                        boolean sent = orderData.get("sent").getAsBoolean();
                        boolean completed = orderData.get("completed").getAsBoolean();
                        provider.setVariable(macro,params[1],ign.toLowerCase());
                        provider.setVariable(macro,params[2],bin1);
                        provider.setVariable(macro,params[3],bin2);
                        provider.setVariable(macro,params[4],token);
                        // done stuff
                        provider.setVariable(macro,params[0],new ReturnValue(true));
                    } else {
                        provider.setVariable(macro,params[0],new ReturnValue(false));
                    }



                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        return null;
    }

    @Override
    public void onInit() {
        context.getCore().registerScriptAction(this);
    }
}
