package testdex;

import arc.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;
import arc.Core;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.meta.BuildVisibility;

import testdex.block.OverchargeDrill;

public class DextorJavaTest extends Mod{

    public DextorJavaTest(){
        Log.info("Loaded DextorJavaTest constructor.");

        //listen for game load event
        Events.on(ClientLoadEvent.class, e -> {
            //show dialog upon startup
            Time.runTask(10f, () -> {
                BaseDialog dialog = new BaseDialog("frog");
                dialog.cont.add("Dextor come this way").row();
                //mod sprites are prefixed with the mod name (this mod is called 'example-java-mod' in its config)
                dialog.cont.image(Core.atlas.find("dextor-java-test-dextor")).pad(20f).row();
                dialog.cont.button("yessir", dialog::hide).size(100f, 50f);
                dialog.show();
            });
        });
    }
    @Override
    public void loadContent() {
        OverchargeDrill customOverchargeDrill = new OverchargeDrill("overcharge-drill") {{
            requirements(Category.production, ItemStack.with());
            size = 3;
            drillTime = 600;
            maxCharge = 40;
            chargeChance = 0.5f;
        }};
    }
}
