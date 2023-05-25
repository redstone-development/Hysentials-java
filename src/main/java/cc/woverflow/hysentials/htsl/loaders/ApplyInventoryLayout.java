package cc.woverflow.hysentials.htsl.loaders;

import cc.woverflow.hysentials.htsl.Loader;
import org.json.JSONObject;

import static cc.woverflow.hysentials.htsl.Loader.LoaderObject.*;

public class ApplyInventoryLayout extends Loader {

    public ApplyInventoryLayout(String layout) {
        super("Apply Inventory Layout", layout);

        if (layout != null) {
            add(click(10));
            add(option(layout));
        }
    }
}
