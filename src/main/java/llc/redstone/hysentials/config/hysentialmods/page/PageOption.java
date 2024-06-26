/*
 * This file is part of OneConfig.
 * OneConfig - Next Generation Config Library for Minecraft: Java Edition
 * Copyright (C) 2021~2023 Polyfrost.
 *   <https://polyfrost.cc> <https://github.com/Polyfrost/>
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *   OneConfig is licensed under the terms of version 3 of the GNU Lesser
 * General Public License as published by the Free Software Foundation, AND
 * under the Additional Terms Applicable to OneConfig, as published by Polyfrost,
 * either version 1.0 of the Additional Terms, or (at your option) any later
 * version.
 *
 *   This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 * License.  If not, see <https://www.gnu.org/licenses/>. You should
 * have also received a copy of the Additional Terms Applicable
 * to OneConfig, as published by Polyfrost. If not, see
 * <https://polyfrost.cc/legal/oneconfig/additional-terms>
 */

package llc.redstone.hysentials.config.hysentialmods.page;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.gui.OneConfigGui;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.utils.InputHandler;
import llc.redstone.hysentials.config.HysentialsModCard;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PageOption extends BasicOption {
    private List<HysentialsModCard> element = new ArrayList<>();
    private List<Config> config;


    public PageOption(Field field, Object parent, String name, String description, String category, String subcategory, boolean group) {
        super(field, parent, name, description, category, subcategory, 0);
        try {
            if (group) {
                config = (List<Config>) get();
            } else {
                config = Collections.singletonList((Config) get());
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        if (config != null) {
            for (Config config : config) {
                if (config != null) {
                    element.add(new HysentialsModCard(config.mod, true, !config.enabled, false, this.config.size()));
                }
            }
        }
    }

    public static PageOption create(Field field, Object parent) {
        PageAnnotation page = field.getAnnotation(PageAnnotation.class);
        return new PageOption(field, parent, page.name(), page.description(), page.category(), page.subcategory(), page.group());
    }

    @Override
    public void draw(long vg, int x, int y, InputHandler inputHandler) {
        if (OneConfigGui.INSTANCE == null) return;
        final NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
        for (HysentialsModCard element : element) {
            if (config.size() == 1) {
                //draw in the center gui is 1024x696
                element.draw(vg, x + 1024 / 2f - 260 /2f, y, inputHandler);
            } else if (config.size() == 2) {
                //draw 2 in the center
                element.draw(vg, x + 1024 / 2f - 260, y, inputHandler);
                x += 260;
            } else if (config.size() == 3) {
                //draw 3 in the center
                element.draw(vg, x + 1024 / 2f - 260 * 1.5f, y, inputHandler);
                x += 260;
            }
        }
    }


    @Override
    public int getHeight() {
        return 135;
    }
}
