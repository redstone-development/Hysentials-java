/*
 * HyCord - Discord integration mod
 * Copyright (C) 2021 DeDiamondPro
 *
 * HyCord is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HyCord is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HyCord.  If not, see <https://www.gnu.org/licenses/>.
 */

package cc.woverflow.hysentials.util;

import de.jcm.discordgamesdk.Core;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DiscordCore {
    public static void init() throws IOException {
        File dir = new File("config/Hysentials/game-sdk");
        if (!dir.exists() && !dir.mkdir()) {
            throw new IllegalStateException("Could not create Hysentials game-sdk folder");
        }

        String fileName;
        if (SystemUtils.IS_OS_WINDOWS)
            fileName = "discord_game_sdk.dll";
        else if (SystemUtils.IS_OS_MAC)
            fileName = "discord_game_sdk.dylib";
        else if (SystemUtils.IS_OS_LINUX)
            fileName = "discord_game_sdk.so";
        else
            throw new RuntimeException("cannot determine OS type: " + System.getProperty("os.name"));

        File sdk = new File("config/Hysentials/game-sdk/" + fileName);
        File jni = new File("config/Hysentials/game-sdk/" + ((SystemUtils.IS_OS_WINDOWS ? "discord_game_sdk_jni.dll" : "libdiscord_game_sdk_jni" +
                (SystemUtils.IS_OS_MAC ? ".dylib" : ".so"))));
        if (!sdk.exists())
            downloadSdk(sdk, fileName);
        if(!jni.exists())
            extractJni(jni);
        if(sdk.exists() && jni.exists())
            loadNative(sdk, jni);
        else
            throw new IllegalStateException("Could not download GameSDK");
    }

    private static void downloadSdk(File sdk, String fileName) throws IOException {
        String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);
        if (arch.equals("amd64")) arch = "x86_64";

        URL downloadUrl = new URL("https://dl-game-sdk.discordapp.net/3.1.0/discord_game_sdk.zip");
        URLConnection con = downloadUrl.openConnection();
        con.setRequestProperty("User-Agent", "Hysentials");
        ZipInputStream zin = new ZipInputStream(con.getInputStream());
        ZipEntry entry;
        while ((entry = zin.getNextEntry()) != null) {
            if (entry.getName().equals("lib/" + arch + "/" + fileName)) {
                System.out.println("Found file");
                Files.copy(zin, sdk.toPath(), StandardCopyOption.REPLACE_EXISTING);
                break;
            }
            zin.closeEntry();
        }
        zin.close();
    }

    private static void extractJni(File jni) throws IOException {
        String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);
        if (arch.equals("x86_64")) arch = "amd64";
        String path = "/native/" + (SystemUtils.IS_OS_WINDOWS ? "windows" : (SystemUtils.IS_OS_MAC ? "macos" : "linux"))
                + "/" + arch + "/" + (SystemUtils.IS_OS_WINDOWS ? "discord_game_sdk_jni.dll" : "libdiscord_game_sdk_jni" +
                (SystemUtils.IS_OS_MAC ? ".dylib" : ".so"));
        InputStream in = DiscordCore.class.getResourceAsStream(path);
        Files.copy(in, jni.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private static void loadNative(File sdk, File jni) {
        if (SystemUtils.IS_OS_WINDOWS)
            System.load(sdk.getAbsolutePath());
        System.load(jni.getAbsolutePath());
        Core.initDiscordNative(sdk.getAbsolutePath());
    }
}
