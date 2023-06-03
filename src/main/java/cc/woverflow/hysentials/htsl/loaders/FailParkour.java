package cc.woverflow.hysentials.htsl.loaders;

import cc.woverflow.hysentials.htsl.Loader;
import org.json.JSONObject;

import java.util.List;

import static cc.woverflow.hysentials.htsl.Loader.LoaderObject.*;

public class FailParkour extends Loader {
    public FailParkour(String reason) {
        super("Fail Parkour", "failParkour", reason);

        if (reason != null && !reason.equals("Failed!")) {
            add(click(10));
            add(chat(reason));
        }
    }

    @Override
    public Loader load(int index, List<String> args, List<String> compileErorrs) {
        return new FailParkour(args.get(0));
    }
}