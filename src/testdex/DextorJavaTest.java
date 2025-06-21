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
        // Core.app.post() is crucial here to ensure Mindustry's content systems are fully initialized
        // before your mod attempts to add new blocks.
        Core.app.post(() -> {
            // Instantiate your custom drill block.
            // Mindustry's content loading system will automatically discover this block
            // because it's instantiated in the loadContent() method of your Mod class.
            OverchargeDrill customOverchargeDrill = new OverchargeDrill("overcharge-drill");

            // --- Set Block Properties ---
            // Set basic block properties directly on the instance
            customOverchargeDrill.size = 2; // Sets the size of the block (e.g., 2x2 tiles)
            customOverchargeDrill.tier = 3; // Sets the mining tier (what ores it can mine)
            customOverchargeDrill.drillTime = 180f; // Base drill time in ticks (60 ticks = 1 second)

            // Set custom properties defined in OverchargeDrill
            customOverchargeDrill.maxCharge = 100f;
            customOverchargeDrill.chargeChance = 0.1f; // 0.1 for 1 in 10 chance, 1.0 for guaranteed
            customOverchargeDrill.overchargeDuration = 450f; // Overcharge duration in ticks (e.g., 7.5 seconds)
            customOverchargeDrill.cooldownDuration = 900f;  // Cooldown duration in ticks (e.g., 15 seconds)

            // Define the resources required to build the drill.
            // The requirements() method takes Category and BuildVisibility, then the ItemStacks.
            customOverchargeDrill.requirements(
                Category.production,        // Sets the tech tree category
                BuildVisibility.factory,    // Makes it visible in the factory build menu
                new ItemStack(Items.lead, 100),
                new ItemStack(Items.titanium, 75),
                new ItemStack(Items.silicon, 50)
            );

            // Other properties
            customOverchargeDrill.researchCostMultiplier = 0.5f; // Adjusts research cost
        });
    }
}
