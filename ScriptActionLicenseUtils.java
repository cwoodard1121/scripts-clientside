package com.itzblaze;

import net.eq2online.macros.core.Macros;
import net.eq2online.macros.scripting.api.IMacro;
import net.eq2online.macros.scripting.api.IScriptActionProvider;


import java.io.*;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class LicenseUtils {
    static final String server = null;

    public static String getServer() throws IOException {
        BufferedReader reader = (new BufferedReader(new InputStreamReader((new URL("https://raw.githubusercontent.com/qcscripts/server/main/link")).openStream())));
        String s = reader.readLine();
        reader.close();
        return s;
    }

    public static boolean isLicenseActive() {
        String url = null;
        String result = null;
        try {
            String license = LicenseUtils.getLicense();
            url = new BufferedReader(new InputStreamReader(new URL("https://raw.githubusercontent.com/qcscripts/server/main/link").openStream())).readLine();
            result = new BufferedReader(new InputStreamReader(new URL(url + ":3000/?license=" + license + "&uuid=" + Scripting.getUniqueID().toString() + "&ip=" + LicenseUtils.getIp() + "&term=licensecheck&licensesp=29556").openStream())).readLine();
            if(Boolean.parseBoolean(result)) {
                return Boolean.parseBoolean(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    public static boolean isLicenseActive(String license) {
        String url = null;
        String result = null;
        try {
            url = new BufferedReader(new InputStreamReader(new URL("https://raw.githubusercontent.com/qcscripts/server/main/link").openStream())).readLine();
            result = new BufferedReader(new InputStreamReader(new URL(url + ":3000/?license=" + license + "&uuid=" + Scripting.getUniqueID().toString() + "&ip=" + LicenseUtils.getIp() + "&term=licensecheck&licensesp=29556").openStream())).readLine();
            if(Boolean.parseBoolean(result)) {
                return Boolean.parseBoolean(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getIp() throws IOException {
        BufferedReader reader = (new BufferedReader(new InputStreamReader((new URL("http://checkip.amazonaws.com/")).openStream())));
        String s = reader.readLine();
        reader.close();
        return s;
    }
    public static String getLicense() throws IOException {
        char sep = File.separatorChar;
        String path = Macros.getInstance().getMacrosDirectory().getAbsolutePath() + sep + "logs" + sep + "license.txt";
        File file = new File(path);
        BufferedReader buf = new BufferedReader(new FileReader(file));
        String license = buf.readLine();
        buf.close();
        return license;
    }

    public static void loadScript(String license, String uuid, String project, IScriptActionProvider provider, IMacro macro,String[] params) throws IOException {
        String ip = "0.0.0.0.0.0.0";
        try {
            ip = getIp();
        } catch(IOException e) {
            e.printStackTrace();
        }
        String url = server == null ? getServer() : server;
        HashMap<String,String> paramss = new HashMap<>();
        String finalIp = ip;
        HttpUtility.newRequest(url + ":3000/" + "?license=" + license + "&uuid=" + uuid + "&ip=" + getIp() + "&licensesp=" + "29556" +  "&term=authenticating", HttpUtility.METHOD_POST, paramss, new HttpUtility.Callback() {
            @Override
            public void OnSuccess(String param1String) {
                StringBuilder builder = new StringBuilder();
                for(Map.Entry e : Constants.mappings.entrySet()) {
                    builder.append((String) e.getKey() + "^" + (String) e.getValue() + "`");
                }
                String mappings = builder.toString();
                String mappingsSent = Base64.getUrlEncoder().encodeToString(mappings.getBytes());
                String newurl = url + ":3000/?license=" + license + "&uuid=" + uuid +  "&ip=" + finalIp + "&licensesp=29556" +  "&term=request&temptoken=" + param1String + "&script=" + project + "&mappings=" + mappingsSent;
                HttpUtility.newRequest(newurl, HttpUtility.METHOD_POST, paramss, new HttpUtility.Callback() {
                        @Override
                        public void OnSuccess(String param1String) {
                            String decode = decodeScript(param1String);
                            ScriptFile script = new ScriptFile(project, decode);
                            for(ScriptFile file : ScriptStorage.scripts) {
                                if(file.data == decode) {
                                    provider.setVariable(macro,params[1],"duplicate");
                                    return;
                                }
                            }
                            ScriptStorage.scripts.add(script);
                            provider.setVariable(macro,params[1],"true");
                            decode = null;
                            param1String = null;
                        }

                        @Override
                        public void OnError(int param1Int, String param1String) {
                            provider.setVariable(macro,params[1],"error");
                        }
                    });
            }

            @Override
            public void OnError(int param1Int, String param1String) {
                provider.setVariable(macro,params[1],"error");
            }
        });
    }




    public static String getScriptData(String scriptId) {
        String data = "$${sysout(" + "null " + scriptId + ")}$$";
        for (ScriptFile script : ScriptStorage.scripts) {
            if(script.name.matches(scriptId)) {
                data = script.data;
                break;
            }
        }
        return data;
    }

    public static String decodeScript(String data) {
        return byteToString(Base64.getMimeDecoder().decode(data));
    }

    private static String byteToString(byte[] data) {
        return new String(data);
    }

}
