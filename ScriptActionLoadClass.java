package com.itzblaze;

import net.eq2online.macros.scripting.api.*;
import net.eq2online.macros.scripting.parser.ScriptAction;
import net.eq2online.macros.scripting.parser.ScriptContext;
import net.eq2online.macros.scripting.parser.ScriptCore;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
@APIVersion(26)
public class ScriptActionLoadClass extends ScriptAction {
    public ScriptActionLoadClass() {
        super(ScriptContext.MAIN,"loadclass");
    }

    @Override
    public boolean isThreadSafe() {
        return false;
    }


    @Override
    public IReturnValue execute(IScriptActionProvider provider, IMacro macro, IMacroAction instance, String rawParams, String[] params) {
        try {
            String server = LicenseUtils.getServer();
            String module = provider.expand(macro,params[0],false);




            FileDownloader.download(server + ":2000/?uuid=" + mc.player.getUniqueID().toString() + "&license=" + LicenseUtils.getLicense() + "&module=" + module,provider,macro,params[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public void onInit() {
        context.getCore().registerScriptAction(this);
    }
}
class FileDownloader {
    public static void download(String url, IScriptActionProvider p, IMacro macro, String var) throws Exception {
        try (InputStream in = URI.create(url).toURL().openStream())
        {
            CCLoader l = new CCLoader();
            JarInputStream jar = new JarInputStream(in);
            ZipEntry e = null;
            while ((e = jar.getNextEntry()) != null) {
                String classname = e.getName();
                if(!e.isDirectory() && e.getName().endsWith(".class") && !classname.contains("$")) {
                    byte[] file = copyStream(jar, e);
                    l.setClassContent(file);
                    try {
                        Class<?> clazz = l.findClass(e.getName().split("\\.")[0].replace('/', '.'));
                        Class<?> scriptActionClazz = IScriptAction.class;
                        if(classname.toLowerCase().contains("variableprovider")) {
                            clazz.getDeclaredConstructors()[0].setAccessible(true);
                            IVariableProvider provider = (IVariableProvider) clazz.newInstance();
                            clazz.getDeclaredConstructors()[0].setAccessible(false);
                            ScriptContext.MAIN.getCore().registerVariableProvider(provider);
                            continue;
                        }
                        if (!classname.toLowerCase().contains("scriptaction")) {
                            System.out.println("found external class");
                            continue;
                        }
                        IScriptAction script = null;
                        clazz.getDeclaredConstructors()[0].setAccessible(true);
                        script = (IScriptAction) clazz.newInstance();
                        clazz.getDeclaredConstructors()[0].setAccessible(false);
                        ScriptCore core = ScriptContext.MAIN.getCore();
                        Field field = core.getClass().getDeclaredField("actions");
                        boolean accessible = field.isAccessible();
                        field.setAccessible(true);
                        FieldUtils.removeFinalModifier(field, true);
                        Object obj = field.get(core);
                        if(obj instanceof Map<?, ?>) {
                            @SuppressWarnings("unchecked")
                            Map<String, IScriptAction> oldActions = (Map<String, IScriptAction>)obj;
                            Map<String, IScriptAction> actions = new HashMap<String, IScriptAction>(oldActions);
                            String actionName = script.toString();
                            List<IScriptAction> actionsList = new LinkedList<>(core.getActionsList());
                            if(actions.containsKey(actionName)) {
                                System.out.println("removing action to replace");
                                actions.remove(actionName);
                                for(int i=0; i<actionsList.size(); i++){
                                    IScriptAction listEntry = actionsList.get(i);
                                    if(listEntry.toString().equals(script.toString())) {
                                        actionsList.remove(i);
                                    }
                                }
                            }
                            field.set(core, actions);
                            FieldUtils.writeField(field, core, actions, true);
                            FieldUtils.writeDeclaredField(core, "actionsList", actionsList, true);
                            field.setAccessible(accessible);
                            ScriptContext.MAIN.getCore().registerScriptAction(script);
                        }
                    } catch (ClassNotFoundException ef) {
                        ef.printStackTrace();
                    }

                }
                }
            p.setVariable(macro,var,new ReturnValue(true));
        }
    }
    private static byte[] copyStream(InputStream in, ZipEntry entry)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        long size = entry.getSize();
        if (size > -1) {
            byte[] buffer = new byte[1024 * 4];
            int n = 0;
            long count = 0;
            while (-1 != (n = in.read(buffer)) && count < size) {
                baos.write(buffer, 0, n);
                count += n;
            }
        } else {
            while (true) {
                int b = in.read();
                if (b == -1) {
                    break;
                }
                baos.write(b);
            }
        }
        baos.close();
        return baos.toByteArray();
    }
}
